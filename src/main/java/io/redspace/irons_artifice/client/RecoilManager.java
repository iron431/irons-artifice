package io.redspace.irons_artifice.client;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.utils.RecoilHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Client-side <b>predicted</b> camera recoil.
 *
 * <p>Recoil is a pure decaying aim offsetSeconds applied only to the rendered camera angles (never to the
 * player's actual rotation). The server maintains the same offsetSeconds via {@code ServerRecoil} and fires
 * bullets along it, so the crosshair and the shots agree. Both sides use {@link RecoilHelper} for the
 * impulse and decay, keeping the independent simulations aligned.
 */
@EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
public final class RecoilManager {
    private static final float EPSILON = 0.01F;
    // strength of 1 means cursor is just mirroring the real value perfectly.
    // the cursor (otherwise) acts as a smoother interpolation to the exact pitch/yaw, let snaps transition over multiple ticks instead of always 1.
    // snappy looks better though so
    private static final float CURSOR_STRENGTH = 0.9F;

    // Current recoil offsetSeconds (degrees) and the previous-tick value for partial-tick interpolation.
    private static float pitch = 0.0F;
    private static float yaw = 0.0F;
    private static float pitchCursorO = 0.0F;
    private static float yawCursorO = 0.0F;
    private static float pitchCursor = 0.0F;
    private static float yawCursor = 0.0F;


    /**
     * Applies a recoil impulse from a local fire. {@code roundIndex} drives the yaw pattern.
     */
    public static void applyRecoil(ShotProfile shotProfile) {
        LocalPlayer player = Minecraft.getInstance().player;
        Vec2 permanentRecoil = RecoilHelper.calculatePermanentRecoil(shotProfile);
        Vec2 cameraRecoil = RecoilHelper.calculateCameraRecoil(shotProfile);
        if (player != null) {
            player.setXRot(player.getXRot() - permanentRecoil.x);
            player.setYRot(player.getYRot() + permanentRecoil.y);
        }
        pitch += cameraRecoil.x;
        yaw += cameraRecoil.y;
    }

    @SubscribeEvent
    static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float partialTick = (float) event.getPartialTick();
        float p = Mth.lerp(partialTick, pitchCursorO, pitchCursor);
        float y = Mth.lerp(partialTick, yawCursorO, yawCursor);
        if (p == 0.0F && y == 0.0F) {
            return;
        }
        event.setPitch(event.getPitch() - p);
        event.setYaw(event.getYaw() + y);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }
        pitchCursorO = pitchCursor;
        yawCursorO = yawCursor;
        pitch = decay(pitch);
        yaw = decay(yaw);
        updateCameraCursor();
    }

    private static float decay(float value) {
        float decayed = RecoilHelper.decay(value, 1);
        return Math.abs(decayed) < EPSILON ? 0.0F : decayed;
    }

    private static void updateCameraCursor() {
        pitchCursor = Mth.lerp(CURSOR_STRENGTH, pitchCursor, pitch);
        yawCursor = Mth.lerp(CURSOR_STRENGTH, yawCursor, yaw);
        if (Math.abs(pitchCursor) < EPSILON) {
            pitchCursor = 0;
        }
        if (Math.abs(yawCursor) < EPSILON) {
            yawCursor = 0;
        }
    }


    public static float localRecoilMagnitude() {
        return Mth.sqrt(yaw * yaw + pitch * pitch);
    }


}
