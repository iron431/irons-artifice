package io.redspace.irons_artifice.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.data.ParticleBurst;
import io.redspace.irons_artifice.network.packets.ClientboundMuzzleFlashPacket;
import io.redspace.irons_artifice.network.packets.MuzzleFlashVisuals;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
public final class MuzzleFlashEmitter {
    /**
     * Excess flashes for one entity within a single frame are extra shots; the queue only guards
     * against pathological buildup (dropping in the oldest-first direction keeps the newest visuals).
     */
    private static final int MAX_PENDING_PER_ENTITY = 4;

    private static final Map<Integer, Deque<ClientboundMuzzleFlashPacket>> PENDING = new HashMap<>();

    private MuzzleFlashEmitter() {
    }

    public static void enqueue(ClientboundMuzzleFlashPacket packet) {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) {
            return;
        }
        Deque<ClientboundMuzzleFlashPacket> queue = PENDING.computeIfAbsent(packet.entityId(), id -> new ArrayDeque<>());
        if (queue.size() >= MAX_PENDING_PER_ENTITY) {
            queue.pollFirst();
        }
        queue.addLast(packet);
    }

    public static void tryEmit(int entityId, PoseStack poseStack) {
        Deque<ClientboundMuzzleFlashPacket> queue = PENDING.remove(entityId);
        ClientLevel level = Minecraft.getInstance().level;
        if (queue == null || level == null) {
            return;
        }
        // Every queued flash for this entity belongs at the muzzle that is being rendered right now;
        // spawning them all avoids stale backup flashes for shots fired within the same frame.
        for (ClientboundMuzzleFlashPacket packet : queue) {
            spawn(level, packet, worldPosFromBone(poseStack, packet.extraForwardOffset()));
        }
    }

    /**
     * Fallback flush for flashes that were not matched to a rendered gun muzzle this frame.
     * Must run on RenderFrameEvent.Post (after rendering) so that {@link #tryEmit} at the
     * muzzle bone gets first chance during the frame's render pass — a tick-based flush fires
     * before rendering and would always steal the flash to the backup position.
     */
    @SubscribeEvent
    static void onRenderFrame(RenderFrameEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || PENDING.isEmpty() || Minecraft.getInstance().isPaused()) {
            return;
        }
        Iterator<Deque<ClientboundMuzzleFlashPacket>> iterator = PENDING.values().iterator();
        while (iterator.hasNext()) {
            Deque<ClientboundMuzzleFlashPacket> queue = iterator.next();
            for (ClientboundMuzzleFlashPacket packet : queue) {
                spawn(level, packet, packet.backupPos());
            }
            iterator.remove();
        }
    }

    /** Drop leftover state (e.g. on logout) so flashes cannot leak into the next session. */
    public static void reset() {
        PENDING.clear();
        RenderingEntityTracker.clear();
    }

    private static Vec3 worldPosFromBone(PoseStack poseStack, float extraForwardOffset) {
        Vector3f origin = poseStack.last().pose().transformPosition(new Vector3f());
        Vector3f forward = poseStack.last().pose().transformDirection(new Vector3f(0f, 0f, -1f));
        if (forward.lengthSquared() > 1.0e-6f) {
            forward.normalize();
        }
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
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
            Vec3 motion = msg.entityMotion().scale(0.5).add(Utils.randomUnitVector(level.getRandom()).scale(0.02));
            level.addAlwaysVisibleParticle(flash, true, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        });
        for (ParticleBurst burst : visuals.airBursts()) {
            spawnBurst(level, burst, pos);
        }
    }

    private static void spawnBurst(ClientLevel level, ParticleBurst burst, Vec3 pos) {
        for (int i = 0; i < burst.count(); i++) {
            Vec3 motion = Utils.randomUnitVector(level.getRandom()).scale(burst.velocityScale());
            level.addAlwaysVisibleParticle(burst.particle(), burst.force(), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        }
    }
}
