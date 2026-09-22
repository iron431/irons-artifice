package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ParticleStack;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.entity.ChainEntity;
import io.redspace.irons_artifice.gun.HitEntityAccumulator;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Consumer;

public final class HookShotModifier implements GunModifier {
    public static final float MAX_RANGE = 32;

    @Override
    public void apply(ShotComponentMap components) {
        components.getOrCreate(ShotComponents.ON_HIT).add((ServerLevel level, Bullet bullet, HitResult hitResult, HitEntityAccumulator accumulator) -> {
            if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity livingVictim && bullet.getOwner() instanceof LivingEntity livingOwner) {
                double distance = livingVictim.distanceTo(livingOwner);
                if (distance < MAX_RANGE) {
                    ChainEntity chain = new ChainEntity(level, livingVictim, livingOwner);
                    chain.setPrimaryStrength(0.075f / (float) distance);
                    chain.setSecondaryStrength(0);
                    chain.setDuration(10);
                    chain.setMaxRange(MAX_RANGE);
                    level.addFreshEntity(chain);
                }
            }
        });
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).addAccent(
                new ParticleStack.ParticleAccent(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CHAIN.defaultBlockState()), 0.5)
        );
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.hook_shoot").withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("irons_artifice.tooltip.max_range", Utils.DECIMAL_FORMAT.format(MAX_RANGE)).withStyle(ChatFormatting.AQUA));
    }
}
