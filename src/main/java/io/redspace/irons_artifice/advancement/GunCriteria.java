package io.redspace.irons_artifice.advancement;

import io.redspace.irons_artifice.api.GunShootEvent;
import io.redspace.irons_artifice.damage.DamageSources;
import io.redspace.irons_artifice.data.RecentShots;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.entity.IExtendedLivingEntity;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.ironslib.kinetic_weapon.KineticWeapon;
import io.redspace.irons_artifice.registry.CriterionRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber
public final class GunCriteria {
    private GunCriteria() {
    }

    public static ShotCombatTracker tracker(ServerPlayer player) {
        return ((IExtendedLivingEntity) player).irons_artifice$getShotCombat();
    }

    public static void markInstaReload(ServerPlayer player) {
        tracker(player).markInstaReload(player.level().getGameTime());
    }

    public static void triggerModified(ServerPlayer player, ItemStack gun, int occupied, int capacity) {
        CriterionRegistry.GUN_MODIFIED.trigger(player, gun, occupied, capacity);
    }

    @SubscribeEvent
    public static void onShoot(GunShootEvent.Post event) {
        LivingEntity shooter = event.getEntity();
        RecentShots.trackShot(shooter);
        if (shooter instanceof ServerPlayer player) {
            CriterionRegistry.SHOT_GUN.trigger(player, event.getShotProfile().itemStack(), RecentShots.count(player));
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        DamageSource damageSource = event.getSource();
        LivingEntity victim = event.getEntity();
        if (!(damageSource.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (damageSource.is(DamageSources.BULLET_DAMAGE_TYPE)) {
            Bullet bullet = bulletFrom(damageSource);
            if (bullet == null) {
                return;
            }
            ShotRecord record = bullet.getShotRecord();
            if (record == null) {
                return;
            }
            ShotCombatTracker state = tracker(player);
            int pellets = state.recordPelletHit(record.fireId(), victim.getId());
            boolean killed = false;
            int totalKills = 0;
            // the event fires just before the damage lands, so the kill is projected from the pending amount
            if (victim.getHealth() - event.getAmount() <= 0) {
                killed = true;
                totalKills = state.recordKill(record.fireId(), victim.getUUID());
            }
            float damage = state.recordDamage(record.fireId(), event.getAmount());
            triggerCombat(player, killed, damage, player.distanceTo(victim), pellets, totalKills, record, bullet.getProfile().itemStack(), victim, GunCombatSource.BULLET);
        } else if (player.getMainHandItem().getItem() instanceof GunItem && KineticWeapon.has(player.getMainHandItem())) {
            boolean killed = victim.getHealth() - event.getAmount() <= 0;
            triggerCombat(player, killed, event.getAmount(), player.distanceTo(victim), 0, killed ? 1 : 0, null, player.getMainHandItem(), victim, GunCombatSource.BAYONET);
        }

    }

    private static void triggerCombat(
            ServerPlayer player,
            boolean killed,
            float damage,
            double distance,
            int pellets,
            int lineageKills,
            @Nullable ShotRecord record,
            ItemStack gun,
            Entity victim,
            GunCombatSource source
    ) {
        ShotCombatTracker state = tracker(player);
        CriterionRegistry.GUN_COMBAT.trigger(player, new GunCombatTrigger.CombatSnapshot(
                killed,
                damage,
                distance,
                pellets,
                lineageKills,
                record != null && record.ricocheted(),
                record != null && record.fullMagazine(),
                state.instaReloaded(player.level().getGameTime()),
                player.isUnderWater(),
                source,
                gun,
                victim
        ));
    }

    private static @Nullable Bullet bulletFrom(DamageSource source) {
        if (source.getDirectEntity() instanceof Bullet bullet) {
            return bullet;
        }
        if (source.getEntity() instanceof Bullet bullet) {
            return bullet;
        }
        return null;
    }
}
