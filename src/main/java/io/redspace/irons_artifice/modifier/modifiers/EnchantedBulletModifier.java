package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.api.ConsumeAmmoEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;
import java.util.function.Consumer;

@EventBusSubscriber
public final class EnchantedBulletModifier extends ValueStackModifier {
    public static final double INFINITY_CHANCE = 0.1;

    public EnchantedBulletModifier() {
        super(Map.of(
                ShotComponents.AMMO_CONSUME_CHANCE, new ValueModifier(-INFINITY_CHANCE, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL)
        ));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        super.getDescriptionText(builder);
//        builder.accept(Component.translatable("irons_artifice.modifier.infinity", (int) (INFINITY_CHANCE * 100)).withStyle(ChatFormatting.GREEN));
    }

    @SubscribeEvent
    public static void preventAmmoConsumption(ConsumeAmmoEvent event){

    }
}
