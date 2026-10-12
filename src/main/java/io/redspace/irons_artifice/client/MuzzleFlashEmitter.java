package io.redspace.irons_artifice.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.compat.IrisHelper;
import io.redspace.irons_artifice.data.ParticleBurst;
import io.redspace.irons_artifice.network.packets.ClientboundMuzzleFlashPacket;
import io.redspace.irons_artifice.network.packets.MuzzleFlashVisuals;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Mod.EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
public final class MuzzleFlashEmitter {
    private static final Map<Integer, ClientboundMuzzleFlashPacket> PENDING = new HashMap<>();

    public static void enqueue(ClientboundMuzzleFlashPacket packet) {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) {
            return;
        }
        PENDING.put(packet.entityId(), packet);
    }

    public static void tryEmit(int entityId, PoseStack poseStack) {
        // The shadow map is drawn from the sun's point of view, so its poses do not map back to the world.
        if (IrisHelper.isRenderingShadowPass()) {
            return;
        }
        ClientboundMuzzleFlashPacket packet = PENDING.remove(entityId);
        if (packet == null) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        spawn(level, packet, worldPosFromBone(poseStack, packet.extraForwardOffset()));
    }

    @SubscribeEvent
    public static void onRenderFrame(TickEvent.RenderTickEvent event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (event.phase != TickEvent.Phase.END || level == null || PENDING.isEmpty() || Minecraft.getInstance().isPaused()) {
            return;
        }
        Iterator<ClientboundMuzzleFlashPacket> iterator = PENDING.values().iterator();
        while (iterator.hasNext()) {
            ClientboundMuzzleFlashPacket packet = iterator.next();
            spawn(level, packet, packet.backupPos());
            iterator.remove();
        }
    }

    private static Vec3 worldPosFromBone(PoseStack poseStack, float extraForwardOffset) {
        // The shader draws a vertex at projection * modelView * pose, and hand and entity passes both draw under the
        // camera's projection, so modelView * pose maps bone space into camera space however the renderer split the
        // camera transform between the two. Camera space looks down -Z with +Y up and -X to the left; the camera
        // publishes where those axes point in the world, which is a plainer description than camera.rotation(),
        // whose quaternion maps +Z, not -Z, onto the look direction.
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Matrix4f boneToCamera = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poseStack.last().pose());
        Vector3f forward = boneToCamera.transformDirection(new Vector3f(0f, 0f, -1f));
        if (forward.lengthSquared() > 1.0e-6f) {
            forward.normalize();
        }
        Vector3f muzzle = boneToCamera.transformPosition(new Vector3f()).fma(extraForwardOffset, forward);
        Vector3f look = camera.getLookVector();
        Vector3f up = camera.getUpVector();
        Vector3f left = camera.getLeftVector();
        return camera.getPosition().add(
                -left.x * muzzle.x + up.x * muzzle.y - look.x * muzzle.z,
                -left.y * muzzle.x + up.y * muzzle.y - look.y * muzzle.z,
                -left.z * muzzle.x + up.z * muzzle.y - look.z * muzzle.z);
    }

    private static void spawn(ClientLevel level, ClientboundMuzzleFlashPacket msg, Vec3 pos) {
        MuzzleFlashVisuals visuals = msg.visuals();
        if (level.isFluidAtPosition(BlockPos.containing(pos), s -> s.is(FluidTags.WATER))
                && !visuals.underwaterBursts().isEmpty()) {
            for (ParticleBurst burst : visuals.underwaterBursts()) {
                spawnBurst(level, burst, pos);
            }
            return;
        }
        visuals.flash().ifPresent(flash -> {
            Vec3 random = new Vec3(level.getRandom().nextDouble() - 0.5, level.getRandom().nextDouble() - 0.5, level.getRandom().nextDouble() - 0.5).scale(2).scale(0.02);
            Vec3 motion = msg.entityMotion().scale(0.5).add(random);
            level.addAlwaysVisibleParticle(flash, true, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        });
        for (ParticleBurst burst : visuals.airBursts()) {
            spawnBurst(level, burst, pos);
        }
    }

    private static void spawnBurst(ClientLevel level, ParticleBurst burst, Vec3 pos) {
        for (int i = 0; i < burst.count(); i++) {
            Vec3 motion = new Vec3(
                    level.getRandom().nextDouble() - 0.5,
                    level.getRandom().nextDouble() - 0.5,
                    level.getRandom().nextDouble() - 0.5
            ).scale(2).scale(burst.velocityScale());
            level.addAlwaysVisibleParticle(burst.particle(), burst.force(), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        }
    }
}
