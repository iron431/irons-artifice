package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.data.ParticleBurst;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.entity.SoulfireCoin;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.modifier.on_hit_handlers.SoulCoinSpawnOnHit;
import io.redspace.irons_artifice.modifier.on_shot_handlers.RollSoulCoinChanceOnShot;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class SoulfireCoinModifier implements GunModifier {
    public static final double SOUL_CHANCE = 0.50;
    public static final int TRAIL_COLOR_FROM = 0x8aeafb;
    public static final int TRAIL_COLOR_TO = 0x492f9c;
    public static final int MUZZLEFLASH = 0x99d7ff;

    @Override
    public void apply(ShotComponentMap components) {
        components.modifyValue(ShotComponents.SOUL_CHANCE, new ValueModifier(SOUL_CHANCE, ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL));
        components.getOrCreate(ShotComponents.ON_SHOT).getOrCreate(RollSoulCoinChanceOnShot.class, RollSoulCoinChanceOnShot::new);
        components.getOrCreate(ShotComponents.ON_HIT).getOrCreate(SoulCoinSpawnOnHit.class, SoulCoinSpawnOnHit::new);
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).add(ColorTransitionParticleOption.bulletTrail(
                TRAIL_COLOR_FROM, TRAIL_COLOR_TO
        ));
        components.getOrCreate(ShotComponents.MUZZLE_FLASH).addTint(MUZZLEFLASH);
        components.getOrCreate(ShotComponents.MUZZLE_FLASH).addAirBurst(new ParticleBurst(ParticleTypes.SOUL_FIRE_FLAME, 2, 0.4f, false));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.component_type.soul_chance_on_hit", (int) (SOUL_CHANCE * 100)).withStyle(ChatFormatting.GREEN));
        builder.accept(Component.translatable("irons_artifice.modifier.soulfire_coin").withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("irons_artifice.modifier.soulfire_coin_empower", (int) (SoulfireCoin.DAMAGE_BONUS * 100)).withStyle(ChatFormatting.AQUA));
    }
}
