package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.api.GunShootEvent;
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
        components.getOrCreate(ShotComponents.ACCELERATING).addModifier(new ValueModifier(1, ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.accelerating", (int) (DAMAGE_PER_SHOT * 100)).withStyle(ChatFormatting.GREEN));
    }

    @SubscribeEvent
    public static void onShoot(GunShootEvent.Post event) {
        if (event.getShotProfile().value(ShotComponents.ACCELERATING) > 0) {
            RecentShots.trackShot(event.getEntity());
//            // todo: dedicated hud element
//            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
//                // fixme: actionbar overreliance
//                serverPlayer.sendSystemMessage(Component.translatable("irons_artifice.tooltip.accelerated_percent", (int) (100 * (1 + MechanicalAccelerator.getAcceleratePercent(event.getEntity(), event.getShotProfile())))), true);
//            }
        }
    }

    @SubscribeEvent
    public static void onCompose(ComposeShotEvent event) {
        ShotProfile profile = event.getShotProfile();
        double percent = getAcceleratePercent(event.getEntity(), event.getShotProfile());
        if (percent <= 0) {
            return;
        }
        profile.get(ShotComponents.DAMAGE).addModifier(new ValueModifier(
                percent,
                ValueModifier.Operation.MULTIPLY_TOTAL,
                ValueModifier.Type.BENEFICIAL
        ));
    }

    public static double getAcceleratePercent(LivingEntity entity, ShotProfile shotProfile) {
        int accelerateCount = (int) shotProfile.value(ShotComponents.ACCELERATING);
        int shots = RecentShots.count(entity);
        return shots * DAMAGE_PER_SHOT * accelerateCount;
    }
}
