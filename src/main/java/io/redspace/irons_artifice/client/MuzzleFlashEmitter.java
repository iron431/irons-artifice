package io.redspace.irons_artifice.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.data.ParticleBurst;
import io.redspace.irons_artifice.network.packets.ClientboundMuzzleFlashPacket;
import io.redspace.irons_artifice.network.packets.MuzzleFlashVisuals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
public final class MuzzleFlashEmitter {
    private static final Map<Integer, Pending> PENDING = new HashMap<>();

    private record Pending(ClientboundMuzzleFlashPacket packet, long queuedAtGameTime) {
    }

    public static void enqueue(ClientboundMuzzleFlashPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || Minecraft.getInstance().player == null) {
            return;
        }
        PENDING.put(packet.entityId(), new Pending(packet, level.getGameTime()));
    }

    public static void tryEmit(int entityId, PoseStack poseStack) {
        Pending pending = PENDING.remove(entityId);
        if (pending == null) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        spawn(level, pending.packet(), worldPosFromBone(poseStack, pending.packet().extraForwardOffset()));
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || PENDING.isEmpty() || Minecraft.getInstance().isPaused()) {
            return;
        }
        long gameTime = level.getGameTime();
        Iterator<Pending> iterator = PENDING.values().iterator();
        while (iterator.hasNext()) {
            Pending pending = iterator.next();
            if (gameTime > pending.queuedAtGameTime()) {
                spawn(level, pending.packet(), pending.packet().backupPos());
                iterator.remove();
            }
        }
    }

    private static Vec3 worldPosFromBone(PoseStack poseStack, float extraForwardOffset) {
        Vector3f origin = poseStack.last().pose().transformPosition(new Vector3f());
        Vector3f forward = poseStack.last().pose().transformDirection(new Vector3f(0f, 0f, -1f));
        if (forward.lengthSquared() > 1.0e-6f) {
            forward.normalize();
        }
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        return camera.add(origin.x, origin.y, origin.z)
                .add(forward.x * extraForwardOffset, forward.y * extraForwardOffset, forward.z * extraForwardOffset);
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
