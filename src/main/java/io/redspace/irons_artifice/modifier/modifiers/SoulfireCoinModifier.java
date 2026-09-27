package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.entity.Soul;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.modifier.on_hit_handlers.SoulSpawnOnHit;
import io.redspace.irons_artifice.modifier.on_shot_handlers.RollSoulCoinChanceOnShot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class SoulfireCoinModifier implements GunModifier {
    public static final double SOUL_CHANCE = 0.15;

    @Override
    public void apply(ShotComponentMap components) {
        components.modifyValue(ShotComponents.SOUL_CHANCE, new ValueModifier(SOUL_CHANCE, ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL));
        components.getOrCreate(ShotComponents.ON_SHOT).getOrCreate(RollSoulCoinChanceOnShot.class, RollSoulCoinChanceOnShot::new);
        components.getOrCreate(ShotComponents.ON_HIT).getOrCreate(SoulSpawnOnHit.class, SoulSpawnOnHit::new);
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.component_type.soul_chance_on_hit", (int) (SOUL_CHANCE * 100)).withStyle(ChatFormatting.GREEN));
        builder.accept(Component.translatable("irons_artifice.modifier.soulfire_coin", (int) (Soul.DAMAGE_BONUS * 100)).withStyle(ChatFormatting.AQUA));
    }
}
