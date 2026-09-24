package io.redspace.irons_artifice.advancement;

import io.redspace.irons_artifice.api.GunShootEvent;
import io.redspace.irons_artifice.damage.DamageSources;
import io.redspace.irons_artifice.data.RecentShots;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.ironslib.kinetic_weapon.KineticWeapon;
import io.redspace.irons_artifice.registry.CriterionRegistry;
import io.redspace.irons_artifice.registry.DataAttachmentRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber
public final class GunCriteria {
    private GunCriteria() {
    }

    public static ShotCombatTracker tracker(ServerPlayer player) {
        return player.getData(DataAttachmentRegistry.SHOT_COMBAT);
    }

    public static void markInstaReload(ServerPlayer player) {
        ShotCombatTracker state = tracker(player);
        state.markInstaReload(player.level().getGameTime());
        player.setData(DataAttachmentRegistry.SHOT_COMBAT, state);
    }

    public static void triggerModified(ServerPlayer player, ItemStack gun, int occupied, int capacity) {
        CriterionRegistry.GUN_MODIFIED.get().trigger(player, gun, occupied, capacity);
    }

    @SubscribeEvent
    public static void onShoot(GunShootEvent.Post event) {
        LivingEntity shooter = event.getEntity();
        RecentShots.trackShot(shooter);
        if (shooter instanceof ServerPlayer player) {
            CriterionRegistry.SHOT_GUN.get().trigger(player, event.getShotProfile().itemStack(), RecentShots.count(player));
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
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
            // event fires after damage is applied, so this comparison is accurate
            if (victim.getHealth() <= 0) {
                killed = true;
                totalKills = state.recordKill(record.fireId(), victim.getUUID());
            }
            float damage = state.recordDamage(record.fireId(), event.getNewDamage());
            player.setData(DataAttachmentRegistry.SHOT_COMBAT, state);
            triggerCombat(player, killed, damage, player.distanceTo(victim), pellets, totalKills, record, bullet.getProfile().itemStack(), victim, GunCombatSource.BULLET);
        } else if (player.getWeaponItem().getItem() instanceof GunItem && KineticWeapon.has(player.getWeaponItem())) {
            boolean killed = victim.getHealth() <= 0;
            triggerCombat(player, killed, event.getNewDamage(), player.distanceTo(victim), 0, killed ? 1 : 0, null, player.getWeaponItem(), victim, GunCombatSource.BAYONET);
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
        CriterionRegistry.GUN_COMBAT.get().trigger(player, new GunCombatTrigger.CombatSnapshot(
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
