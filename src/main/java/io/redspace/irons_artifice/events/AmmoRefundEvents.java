package io.redspace.irons_artifice.events;

import io.redspace.irons_artifice.api.AmmoEvent;
import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.registry.AttributeRegistry;
import io.redspace.irons_artifice.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Ammo refund roll for anything that lowers a gun's chance to consume ammo
 */
@EventBusSubscriber
public final class AmmoRefundEvents {
    /**
     * How much an Enchanted Bullet lowers the chance to consume ammo
     */
    public static final double INFINITY_CHANCE = 0.125;

    @SubscribeEvent
    public static void preventAmmoConsumption(AmmoEvent.Consume event) {
        if (event.getAmmoToConsume() <= 0) {
            return;
        }
        var shooter = event.getEntity();
        if (!shouldConsumeAmmo(shooter, event.getShotProfile())) {
            event.setCanceled(true);
            PlayableSound.of(SoundRegistry.INFINITY_BULLET, 1, 0.9f, 1.1f).play(shooter.level(), shooter.position(), SoundSource.NEUTRAL);
            if (shooter instanceof Player player) {
                player.sendOverlayMessage(Component.translatable("irons_artifice.tooltip.refunded_ammo", event.getAmmoToConsume()).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }

    private static boolean shouldConsumeAmmo(LivingEntity shooter, ShotProfile profile) {
        double consumeChance = profile.value(AttributeRegistry.AMMO_CONSUME_CHANCE);
        if (consumeChance >= 1) {
            return true;
        }
        return shooter.getRandom().nextDouble() <= consumeChance;
    }
}
