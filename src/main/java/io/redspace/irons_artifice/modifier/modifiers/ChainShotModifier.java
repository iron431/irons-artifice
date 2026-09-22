package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ParticleStack;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.entity.ChainEntity;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.modifier.on_hit_handlers.ChainShotOnHit;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

public final class ChainShotModifier implements GunModifier {
    @Override
    public void apply(ShotComponentMap components) {
        components.getOrCreate(ShotComponents.POST_HIT_EFFECTS).add(new ChainShotOnHit());
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).addAccent(
                new ParticleStack.ParticleAccent(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CHAIN.defaultBlockState()), 0.5)
        );
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.chain_shot").withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("irons_artifice.tooltip.max_range", Utils.DECIMAL_FORMAT.format(ChainEntity.SPAWN_RANGE)).withStyle(ChatFormatting.AQUA));
    }
}
