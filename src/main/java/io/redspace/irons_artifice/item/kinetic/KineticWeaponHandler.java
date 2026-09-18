package io.redspace.irons_artifice.item.kinetic;

import io.redspace.irons_artifice.network.packets.ClientboundKineticHitPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Predicate;

/**
 * The kinetic charge, server side. 1.21.1 has no spear, so none of the vanilla surfaces this needs
 * exist: the per-attacker "who have I already stabbed" map and {@code stabAttack} live on
 * {@code LivingEntity} in 26.1, the ray march lives in {@code ProjectileUtil.getHitEntitiesAlong},
 * and the loop that drives them lives in {@code KineticWeapon.damageEntities}. All of it is
 * reimplemented here, reached from the mod item's own {@code onUseTick} override the way vanilla
 * reaches it from {@code ItemStack.onUseTick} (see {@code snippets/item-overrides.java.txt}).
 * <p>
 * The recent-stab map is keyed weakly on the attacker so a charge that ends without passing through
 * the item's {@code releaseUsing} or {@code finishUsingItem} -- a mob's AI calling
 * {@code stopUsingItem()} directly, an entity unloading mid-charge -- cannot hold an entity alive.
 * The map is also rebuilt whenever a use starts, which is what actually makes a new charge start
 * with a clean slate: that is detected from the tick counter rather than from an event, because
 * {@code LivingEntity.stopUsingItem()} fires none.
 */
public final class KineticWeaponHandler {

    private KineticWeaponHandler() {
    }

    /**
     * How much of a player's action a mob's counts for when a condition threshold is tested. Vanilla
     * reads 1.0 for a player and 0.2 for anything else, which is what lets a mob land a charge at the
     * speed a mob can actually reach.
     */
    private static final float PLAYER_ACTION_FACTOR = 1.0F;
    private static final float MOB_ACTION_FACTOR = 0.2F;

    /** Seconds to ticks, for turning a per-tick displacement into the blocks-per-second the conditions are written in. */
    private static final double TICKS_PER_SECOND = 20.0;

    /** Extra knockback a stab adds on top of the attacker's own, as in {@code LivingEntity.stabAttack}. */
    private static final float STAB_KNOCKBACK_BONUS = 0.4F;

    /**
     * Server thread only. Weak keys: an attacker that goes away takes its charge's memory with it.
     * Each value maps an entity this charge has already run through to the game time it did so, which
     * is what {@code contactCooldownTicks} is measured against.
     */
    private static final Map<LivingEntity, Map<Entity, Long>> RECENT_KINETIC_ENEMIES = new WeakHashMap<>();

    /**
     * One charging tick. Mirrors {@code KineticWeapon.damageEntities}: nothing happens until the
     * weapon's own delay has elapsed, then every entity the attacker's reach segment clips is tested
     * against the three conditions and stabbed if any of them pass.
     *
     * @param ticksRemaining the value {@code LivingEntity.updateUsingItem} passes down, before it decrements
     */
    public static void tickCharge(ItemStack stack, LivingEntity attacker, int ticksRemaining, EquipmentSlot weaponSlot) {
        KineticWeapon kineticWeapon = KineticWeapon.get(stack);
        if (kineticWeapon == null || !(attacker.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int ticksUsed = stack.getUseDuration(attacker) - ticksRemaining;
        if (ticksUsed <= 0) {
            // First tick of this use. Vanilla builds the map in startUsingItem; 1.21.1 fires no event
            // there for a mob whose AI starts the charge, so the tick counter is what marks the start.
            RECENT_KINETIC_ENEMIES.put(attacker, new IdentityHashMap<>());
        }
        if (ticksUsed < kineticWeapon.delayTicks()) {
            return;
        }
        ticksUsed -= kineticWeapon.delayTicks();

        Vec3 attackerLookVector = attacker.getLookAngle();
        double attackerSpeedProjection = attackerLookVector.dot(getMotion(attacker));
        float actionFactor = attacker instanceof Player ? PLAYER_ACTION_FACTOR : MOB_ACTION_FACTOR;
        AttackRange attackRange = AttackRange.of(attacker, stack);
        double baseMobDamage = attacker.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        boolean affected = false;

        for (Entity hit : entitiesAlongReach(attacker, attackRange)) {
            Entity target = hit instanceof EnderDragonPart dragonPart ? dragonPart.parentMob : hit;
            if (wasRecentlyStabbed(attacker, target, kineticWeapon.contactCooldownTicks())) {
                continue;
            }
            rememberStabbedEntity(attacker, target);
            double targetSpeedProjection = attackerLookVector.dot(getMotion(target));
            double relativeSpeed = Math.max(0.0, attackerSpeedProjection - targetSpeedProjection);
            boolean dealsDismount = testCondition(kineticWeapon.dismountConditions(), ticksUsed, attackerSpeedProjection, relativeSpeed, actionFactor);
            boolean dealsKnockback = testCondition(kineticWeapon.knockbackConditions(), ticksUsed, attackerSpeedProjection, relativeSpeed, actionFactor);
            boolean dealsDamage = testCondition(kineticWeapon.damageConditions(), ticksUsed, attackerSpeedProjection, relativeSpeed, actionFactor);
            if (dealsDismount || dealsKnockback || dealsDamage) {
                float damageDealt = (float) baseMobDamage + Mth.floor(relativeSpeed * kineticWeapon.damageMultiplier());
                affected |= stabAttack(serverLevel, attacker, weaponSlot, target, damageDealt, dealsDamage, dealsKnockback, dealsDismount);
            }
        }

        if (affected) {
            // Vanilla broadcasts entity event 2 here and lets LivingEntity.onKineticHit throttle the
            // feedback client side. Entity event bytes are a per-entity-type namespace that any mob
            // subclass may claim at 1.21.1, and receiving one would mean mixing into
            // LivingEntity.handleEntityEvent; the mod already owns a payload registry, so a typed
            // payload with the same audience (trackers plus the attacker itself) carries it instead.
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(attacker,
                    new ClientboundKineticHitPacket(attacker.getId(), kineticWeapon.hitSound()));
        }
    }

    /** Drops a charge's recent-stab memory. Called when a use ends through an item callback. */
    public static void endCharge(LivingEntity attacker) {
        RECENT_KINETIC_ENEMIES.remove(attacker);
    }

    /**
     * How many of the entities this charge has already run through match {@code predicate}.
     * {@code LivingEntity.stabbedEntities} in 26.1, reading the field this map stands in for; an AI
     * goal uses it to tell that its charge has connected and can be ended.
     */
    public static int stabbedEntities(LivingEntity attacker, Predicate<Entity> predicate) {
        Map<Entity, Long> memory = RECENT_KINETIC_ENEMIES.get(attacker);
        if (memory == null) {
            return 0;
        }
        return (int) memory.keySet().stream().filter(predicate).count();
    }

    /**
     * How fast the entity is actually travelling, in blocks per second, along the world axes.
     * <p>
     * A non-player passenger reports its vehicle's motion, so a mob charging on a horse hits with the
     * horse's speed. 26.1 reads {@code getKnownSpeed()}, its own per-tick position delta; 1.21.1 has
     * only {@code getKnownMovement()}, which is the same quantity for a {@code ServerPlayer} (fed
     * from the movement packet) and is the entity's velocity for everything else -- and unlike
     * {@code position() - xOld} it is not zero at the point in the tick this runs, because
     * {@code setOldPosAndRot} has just run.
     */
    public static Vec3 getMotion(Entity entity) {
        Entity source = !(entity instanceof Player) && entity.isPassenger() ? entity.getRootVehicle() : entity;
        return source.getKnownMovement().scale(TICKS_PER_SECOND);
    }

    private static boolean testCondition(Optional<KineticWeapon.Condition> condition, int ticksUsed,
                                         double attackerSpeed, double relativeSpeed, double actionFactor) {
        return condition.isPresent() && condition.get().test(ticksUsed, attackerSpeed, relativeSpeed, actionFactor);
    }

    // ---------------------------------------------------------------------------------------------
    // The reach segment. 26.1 ProjectileUtil.getHitEntitiesAlong plus getManyEntityHitResult.
    // ---------------------------------------------------------------------------------------------

    /**
     * Every entity whose hitbox the attacker's reach segment clips, in no particular order.
     * <p>
     * The segment runs from the eye, starts at the weapon's minimum reach and ends at its maximum
     * plus however far the attacker is moving forward this tick -- so a fast charge reaches further,
     * which is what stops a charge from tunnelling through a target between ticks. A block that
     * {@code ClipContext.Block.COLLIDER} stops the segment on shortens it, and a block closer than
     * the minimum reach cancels the swing outright.
     */
    private static List<Entity> entitiesAlongReach(LivingEntity attacker, AttackRange attackRange) {
        Level level = attacker.level();
        Vec3 look = attacker.getLookAngle();
        Vec3 eyePosition = attacker.getEyePosition();
        Vec3 from = eyePosition.add(look.scale(attackRange.effectiveMinRange(attacker)));
        double movementComponent = attacker.getKnownMovement().dot(look);
        Vec3 to = eyePosition.add(look.scale(attackRange.effectiveMaxRange(attacker) + Math.max(0.0, movementComponent)));

        BlockHitResult blocked = level.clip(new ClipContext(eyePosition, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, attacker));
        if (blocked.getType() != HitResult.Type.MISS) {
            to = blocked.getLocation();
            if (eyePosition.distanceToSqr(to) < eyePosition.distanceToSqr(from)) {
                return List.of();
            }
        }

        float margin = attackRange.hitboxMargin();
        AABB searchArea = AABB.ofSize(from, margin, margin, margin).expandTowards(to.subtract(from)).inflate(1.0);
        List<Entity> collector = new ArrayList<>();
        for (Entity candidate : level.getEntities(attacker, searchArea, entity -> canHitEntity(attacker, entity))) {
            AABB hitbox = candidate.getBoundingBox();
            if (hitbox.contains(from) || hitbox.clip(from, to).isPresent()) {
                collector.add(candidate);
            } else if (margin > 0.0F && hitbox.inflate(margin).clip(from, to).isPresent()) {
                collector.add(candidate);
            }
        }
        return collector;
    }

    /** {@code PiercingWeapon.canHitEntity} -- what a kinetic weapon is allowed to run through. */
    private static boolean canHitEntity(Entity attacker, Entity target) {
        if (target.isInvulnerable() || !target.isAlive() || !target.canBeHitByProjectile()) {
            return false;
        }
        if (target instanceof Player targetPlayer && attacker instanceof Player attackingPlayer
                && !attackingPlayer.canHarmPlayer(targetPlayer)) {
            return false;
        }
        return !attacker.isPassengerOfSameVehicle(target);
    }

    // ---------------------------------------------------------------------------------------------
    // The stab. 26.1 LivingEntity.stabAttack and the three recent-stab methods beside it.
    // ---------------------------------------------------------------------------------------------

    private static boolean wasRecentlyStabbed(LivingEntity attacker, Entity target, int allowedTime) {
        Map<Entity, Long> memory = RECENT_KINETIC_ENEMIES.get(attacker);
        if (memory == null) {
            return false;
        }
        Long stampedAt = memory.get(target);
        return stampedAt != null && attacker.level().getGameTime() - stampedAt < allowedTime;
    }

    private static void rememberStabbedEntity(LivingEntity attacker, Entity target) {
        Map<Entity, Long> memory = RECENT_KINETIC_ENEMIES.get(attacker);
        if (memory != null) {
            memory.put(target, attacker.level().getGameTime());
        }
    }

    /**
     * One entity run through. Damage, knockback and dismount are independent: a charge too slow to
     * hurt can still unseat a rider, which is what the three separate condition blocks in the
     * component are for.
     *
     * @return whether the target was affected at all, which is what decides if the attacker gets hit feedback
     */
    private static boolean stabAttack(ServerLevel serverLevel, LivingEntity attacker, EquipmentSlot weaponSlot,
                                      Entity target, float baseDamage, boolean dealsDamage,
                                      boolean dealsKnockback, boolean dismounts) {
        ItemStack weaponItem = attacker.getItemBySlot(weaponSlot);
        DamageSource damageSource = attacker instanceof Player player
                ? attacker.damageSources().playerAttack(player)
                : attacker.damageSources().mobAttack(attacker);
        float postEnchantmentDamage = EnchantmentHelper.modifyDamage(serverLevel, weaponItem, target, damageSource, baseDamage);
        Vec3 oldMovement = target.getDeltaMovement();
        boolean dealtDamage = dealsDamage && target.hurt(damageSource, postEnchantmentDamage);
        boolean affected = dealsKnockback | dealtDamage;
        if (dealsKnockback) {
            causeExtraKnockback(attacker, target, STAB_KNOCKBACK_BONUS + knockbackWith(serverLevel, attacker, weaponItem, target, damageSource));
        }
        if (dismounts && target.isPassenger()) {
            affected = true;
            target.stopRiding();
        }
        // 1.21.1's ItemStack.hurtEnemy only takes a Player, where 26.1's takes any LivingEntity; a mob's
        // stab therefore skips the weapon's post-hit hooks, exactly as a mob's melee swing does here.
        if (target instanceof LivingEntity livingTarget && attacker instanceof Player attackingPlayer) {
            weaponItem.hurtEnemy(livingTarget, attackingPlayer);
        }
        if (dealtDamage) {
            EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
        }
        if (!affected) {
            return false;
        }
        attacker.setLastHurtMob(target);
        return true;
    }

    /**
     * {@code LivingEntity.getKnockback} is protected at 1.21.1, so its body is repeated here: the
     * attacker's own knockback attribute, put through whatever enchantments the weapon carries.
     */
    private static float knockbackWith(ServerLevel serverLevel, LivingEntity attacker, ItemStack weaponItem,
                                       Entity target, DamageSource damageSource) {
        float base = (float) attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        return EnchantmentHelper.modifyKnockback(serverLevel, weaponItem, target, damageSource, base);
    }

    /**
     * {@code LivingEntity.causeExtraKnockback} -- the shove, and the attacker's own recoil off it.
     * Vanilla's signature also takes the target's movement from before the shove and never reads it,
     * so it is not carried here.
     */
    private static void causeExtraKnockback(LivingEntity attacker, Entity target, float knockback) {
        if (knockback > 0.0F && target instanceof LivingEntity livingTarget) {
            livingTarget.knockback(knockback,
                    Mth.sin(attacker.getYRot() * Mth.DEG_TO_RAD),
                    -Mth.cos(attacker.getYRot() * Mth.DEG_TO_RAD));
            attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6, 1.0, 0.6));
        }
    }
}
