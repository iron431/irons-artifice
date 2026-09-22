package io.redspace.irons_artifice.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * GameRenderer#getFov is private on 1.21.1; expose it for the crosshair spread math.
 */
@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Invoker("getFov")
    double irons_artifice$getFov(Camera camera, float partialTick, boolean useFOVSetting);
}
