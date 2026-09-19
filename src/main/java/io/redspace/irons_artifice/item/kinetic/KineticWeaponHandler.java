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
 * The kinetic charge, server side: the reach segment, the stab, and the per-attacker memory of who this charge has
 * already run through. Driven from the item's own {@code onUseTick}.
 * <p>
 * That memory is keyed weakly on the attacker, so a charge ending outside {@code releaseUsing} or
 * {@code finishUsingItem} cannot hold an entity alive. It is rebuilt whenever a use starts, which is what gives a
 * new charge a clean slate; the start is read off the tick counter, since {@code stopUsingItem()} fires no event.
 */
public final class KineticWeaponHandler {

    private KineticWeaponHandler() {
    }

    /**
     * How much of a player's action a mob's counts for against a condition threshold. This is what lets a mob land
     * a charge at the speed a mob can reach.
     */
    private static final float PLAYER_ACTION_FACTOR = 1.0F;
    private static final float MOB_ACTION_FACTOR = 0.2F;

    /** Seconds to ticks, for turning a per-tick displacement into the blocks-per-second the conditions are written in. */
    private static final double TICKS_PER_SECOND = 20.0;

    /** Extra knockback a stab adds on top of the attacker's own. */
    private static final float STAB_KNOCKBACK_BONUS = 0.4F;

    /**
     * Server thread only. Weak keys: an attacker that goes away takes its charge's memory with it.
     * Each value maps an entity this charge has already run through to the game time it did so, which
     * is what {@code contactCooldownTicks} is measured against.
     */
    private static final Map<LivingEntity, Map<Entity, Long>> RECENT_KINETIC_ENEMIES = new WeakHashMap<>();

    /**
     * One charging tick. Nothing happens until the weapon's delay has elapsed, then every entity the reach segment
     * clips is tested against the three conditions and stabbed if any pass.
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
            // First tick of this use. No event fires when a mob's AI starts a charge, so the tick counter marks it.
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
            // A typed payload rather than an entity event byte: those are a per-entity-type namespace any mob
            // subclass may claim, and receiving one would mean mixing into LivingEntity.handleEntityEvent.
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(attacker,
                    new ClientboundKineticHitPacket(attacker.getId(), kineticWeapon.hitSound()));
        }
    }

    /** Drops a charge's recent-stab memory. Called when a use ends through an item callback. */
    public static void endCharge(LivingEntity attacker) {
        RECENT_KINETIC_ENEMIES.remove(attacker);
    }

    /**
     * How many entities this charge has already run through match {@code predicate}. An AI goal uses it to tell
     * that its charge has connected and can end.
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
     * A non-player passenger reports its vehicle's motion, so a mob charging on a horse hits with the horse's
     * speed. {@code getKnownMovement()} rather than {@code position() - xOld}, which is zero at the point in the
     * tick this runs: {@code setOldPosAndRot} has just gone.
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
    // The reach segment
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

    /** What a kinetic weapon is allowed to run through. */
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
    // The stab
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
        // ItemStack.hurtEnemy takes only a Player, so a mob's stab skips the weapon's post-hit hooks, as a mob's
        // melee swing does.
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
     * {@code LivingEntity.getKnockback} is protected, so its body is repeated: the attacker's knockback attribute,
     * put through whatever enchantments the weapon carries.
     */
    private static float knockbackWith(ServerLevel serverLevel, LivingEntity attacker, ItemStack weaponItem,
                                       Entity target, DamageSource damageSource) {
        float base = (float) attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        return EnchantmentHelper.modifyKnockback(serverLevel, weaponItem, target, damageSource, base);
    }

    /** The shove, and the attacker's own recoil off it. */
    private static void causeExtraKnockback(LivingEntity attacker, Entity target, float knockback) {
        if (knockback > 0.0F && target instanceof LivingEntity livingTarget) {
            livingTarget.knockback(knockback,
                    Mth.sin(attacker.getYRot() * Mth.DEG_TO_RAD),
                    -Mth.cos(attacker.getYRot() * Mth.DEG_TO_RAD));
            attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6, 1.0, 0.6));
        }
    }
}
