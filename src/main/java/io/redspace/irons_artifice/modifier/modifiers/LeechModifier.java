package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.api.GunShootEvent;
import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;
import java.util.function.Consumer;

@EventBusSubscriber
public final class LeechModifier extends ValueStackModifier {
    public static final float HEALTH_COST = 2.0f;
    public static final double DAMAGE_BONUS = 0.25;
    private static final int TRAIL_FROM = 0xff6b6b;
    private static final int TRAIL_TO = 0x7a0000;
    private static final int MUZZLE_TINT = 0xc41e3a;

    public LeechModifier() {
        super(Map.of(
                ShotComponents.LEECH, new ValueModifier(1, ValueModifier.Operation.ADD)
        ));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        super.getDescriptionText(builder);
        builder.accept(Component.translatable(
                "irons_artifice.modifier.leech_damage",
                (int) (DAMAGE_BONUS * 100 + 100)
        ).withStyle(ChatFormatting.GREEN));
        builder.accept(Component.translatable(
                "irons_artifice.modifier.leech_cost",
                (int) (HEALTH_COST / 2)
        ).withStyle(ChatFormatting.RED));
    }

    @SubscribeEvent
    public static void applyLeechingShot(ComposeShotEvent event) {
        ShotProfile profile = event.getShotProfile();
        if (!canLeech(event.getEntity(), profile)) {
            return;
        }
        profile.modify(ShotComponents.PARTICLE_TRAIL, trail -> trail.add(ColorTransitionParticleOption.bulletTrail(TRAIL_FROM, TRAIL_TO)));
        profile.modify(ShotComponents.MUZZLE_FLASH, flash -> flash.addTint(MUZZLE_TINT));
        profile.modifyValue(ShotComponents.DAMAGE, new ValueModifier(
                DAMAGE_BONUS,
                ValueModifier.Operation.MULTIPLY_TOTAL
        ));
    }

    @SubscribeEvent
    public static void payHealthAfterLeechingShot(GunShootEvent.Post event) {
        if (!canLeech(event.getEntity(), event.getShotProfile())) {
            return;
        }
        LivingEntity shooter = event.getEntity();
        if (hasInfiniteMaterials(shooter)) {
            return;
        }
        shooter.setHealth(Math.max(0.1f, shooter.getHealth() - HEALTH_COST));
        shooter.hurtMarked = true;
    }

    private static boolean canLeech(LivingEntity shooter, ShotProfile profile) {
        int leechShots = (int) profile.value(ShotComponents.LEECH);
        if (leechShots <= 0) {
            return false;
        }
        if (hasInfiniteMaterials(shooter)) {
            return true;
        }
        float minHealth = Math.max(HEALTH_COST, shooter.getMaxHealth() - leechShots * HEALTH_COST);
        return shooter.getHealth() > minHealth;
    }

    private static boolean hasInfiniteMaterials(LivingEntity shooter) {
        return shooter instanceof Player player && player.hasInfiniteMaterials();
    }
}
