package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.data.RecentShots;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.function.Consumer;

/**
 * The accelerating stat only counts installed copies, the bonus itself is applied in {@link #onCompose}
 */
@EventBusSubscriber
public final class MechanicalAccelerator implements GunModifier {
    public static final double DAMAGE_PER_SHOT = 0.05;
    private static final Identifier DAMAGE_BONUS_MODIFIER = IronsArtifice.id("compose/accelerating");

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
        profile.addModifier(AttributeRegistry.GUN_DAMAGE, DAMAGE_BONUS_MODIFIER, percent, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    public static double getAcceleratePercent(LivingEntity entity, ShotProfile shotProfile) {
        int accelerateCount = (int) shotProfile.value(AttributeRegistry.ACCELERATING);
        int shots = RecentShots.count(entity);
        return shots * DAMAGE_PER_SHOT * accelerateCount;
    }
}
