package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.client.particle.FairyDustParticleOption;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.registry.IronsArtificeRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.function.Consumer;

public final class FairyDustModifier implements GunModifier {
    private static final float TRAIL_RADIUS = 1f;

    @Override
    public void apply(ShotComponentMap components) {
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).add(new FairyDustParticleOption(
                IronsArtificeRegistries.Particles.FAIRY_DUST.get(), (float) Math.random() * Mth.PI, TRAIL_RADIUS, Vec3.ZERO
        ));
        components.getOrCreate(ShotComponents.MUZZLE_FLASH).addTint(new Vector3f(1f, 0.85f, 1f));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.fairy_dust").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
