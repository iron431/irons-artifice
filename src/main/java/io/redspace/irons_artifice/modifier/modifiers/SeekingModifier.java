package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ParticleStack;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.modifier.GunModifier;
import net.minecraft.core.particles.ParticleTypes;

public final class SeekingModifier implements GunModifier {
    @Override
    public void apply(ShotComponentMap components) {
//        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).add(
//                ColorTransitionParticleOption.bulletTrail(0xcb9fff, 0x420036)
//        );
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).addAccent(
                new ParticleStack.ParticleAccent(ParticleTypes.ENCHANT, 0.125)
        );
//        components.getOrCreate(ShotComponents.MUZZLE_FLASH).addTint(0xcb9fff);
    }
}
