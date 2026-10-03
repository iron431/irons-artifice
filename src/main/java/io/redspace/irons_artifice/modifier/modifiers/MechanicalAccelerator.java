package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.data.RecentShots;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.modifier.GunModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.function.Consumer;

@EventBusSubscriber
public final class MechanicalAccelerator implements GunModifier {
    public static final double DAMAGE_PER_SHOT = 0.05;

    @Override
    public void apply(ShotComponentMap components) {
        components.modifyValue(ShotComponents.ACCELERATING, new ValueModifier(1, ValueModifier.Operation.ADD));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.accelerating", (int) (DAMAGE_PER_SHOT * 100)).withStyle(ChatFormatting.GREEN));
    }

    @SubscribeEvent
    public static void onCompose(ComposeShotEvent event) {
        ShotProfile profile = event.getShotProfile();
        double percent = getAcceleratePercent(event.getEntity(), event.getShotProfile());
        if (percent <= 0) {
            return;
        }
        profile.modifyValue(ShotComponents.DAMAGE, new ValueModifier(
                percent,
                ValueModifier.Operation.MULTIPLY_TOTAL
        ));
    }

    public static double getAcceleratePercent(LivingEntity entity, ShotProfile shotProfile) {
        int accelerateCount = (int) shotProfile.value(ShotComponents.ACCELERATING);
        int shots = RecentShots.count(entity);
        return shots * DAMAGE_PER_SHOT * accelerateCount;
    }
}
