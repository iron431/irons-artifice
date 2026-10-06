package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.modifier.on_hit_handlers.BlackpowderChargeOnHit;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class BlackpowderChargeModifier implements GunModifier {
    @Override
    public void apply(ShotComponentMap components) {
        components.getOrCreate(ShotComponents.ON_HIT)
                .getOrCreate(BlackpowderChargeOnHit.class, BlackpowderChargeOnHit::new)
                .addStack();
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.blackpowder_charge").withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("irons_artifice.component_type.explosion_radius", Utils.DECIMAL_FORMAT.format(BlackpowderChargeOnHit.RADIUS_PER_STACK)).withStyle(ChatFormatting.GREEN));
    }
}
