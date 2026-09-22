package io.redspace.irons_artifice.item;

import io.redspace.irons_artifice.damage.DamageSources;
import io.redspace.irons_artifice.network.packets.ClientboundBayonetHitPacket;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * 1.21.1 backport of the vanilla spear's charge attack ("lunge"), which the upstream mod gets for free
 * from the 26.x {@code KINETIC_WEAPON} data component installed by the bayonet modifier
 * (see upstream {@code BayonetAttachmentModifier#createVanillaSpear}). None of those components exist on
 * 1.21.1, so the behavior is reimplemented here and driven by item-use events from
 * {@link io.redspace.irons_artifice.events.ServerEvents}.
 * <p>
 * While a gun with a bayonet is being used (charged), after a short engage delay the weapon hits
 * entities along the wielder's view ray every tick. What happens on hit depends on momentum, matching
 * the vanilla spear's three charge stages:
 * <ul>
 *     <li>Engaged (early, fast): dismounts riders, knocks back, and damages.</li>
 *     <li>Tired: no more dismounting, still knocks back and damages.</li>
 *     <li>Disengaged (held out long): damage only, then nothing once its window lapses.</li>
 * </ul>
 * Speeds are in blocks per second, projected onto the attacker's view vector, vehicle-aware so mounted
 * charges work like vanilla's.
 */
public final class BayonetLunge {
    /** Ticks a target stays immune to repeat contact from the same charge. */
    public static final int CONTACT_COOLDOWN_TICKS = 10;
    /** Ticks the charge needs before the weapon is effective. */
    public static final int ENGAGE_DELAY_TICKS = 10;
    // Charge stage windows (measured from engagement) and their minimum speeds, from vanilla's spear.
    private static final Condition DISMOUNT_CONDITION = new Condition((int) (2.5F * 20), 11.0F, 0);
    private static final Condition KNOCKBACK_CONDITION = new Condition((int) (6.75F * 20), 5.1F, 0);
    private static final Condition DAMAGE_CONDITION = new Condition((int) (11.25F * 20), 0, 4.6F);

    public static final float DAMAGE_MULTIPLIER = 1.0F;
    /** Knockback intensity of a charge hit; vanilla's value is not component-driven, tuned to spear feel. */
    public static final float KNOCKBACK_STRENGTH = 0.9F;
    /** Reach of the charge, from the vanilla spear's attack range (survival). */
    public static final double REACH = 4.5;
    /** Extra tolerance around target hitboxes, from the vanilla spear's attack range. */
    public static final double HITBOX_MARGIN = 0.125;

    private static final int TICKS_PER_SECOND = 20;

    /** Per-attacker transient charge state. Server-side only; weak keys so dead attackers self-clean. */
    private static final Map<LivingEntity, LungeState> ACTIVE = new WeakHashMap<>();

    private BayonetLunge() {
    }

    /** Use-item events and item extension callbacks fire on both sides; the state map is server-only. */
    private static boolean isServerSide(LivingEntity entity) {
        return !entity.level().isClientSide();
    }

    private record Condition(int maxDurationTicks, float minAttackerSpeed, float minRelativeSpeed) {
        boolean test(int engagedTicks, double attackerSpeed, double relativeSpeed) {
            return withinWindow(engagedTicks)
                    && attackerSpeed >= minAttackerSpeed
                    && relativeSpeed >= minRelativeSpeed;
        }

        boolean withinWindow(int engagedTicks) {
            return engagedTicks <= maxDurationTicks;
        }
    }

    private static final class LungeState {
        int hits;
        final Map<Integer, Long> lastContactByTargetId = new HashMap<>();
    }

    /** Call when the entity starts using a bayonet gun; discards state from any previous charge. */
    public static void useStarted(LivingEntity entity) {
        if (isServerSide(entity)) {
            ACTIVE.remove(entity);
        }
    }

    /** Call when the entity stops using the item for any reason. */
    public static void useStopped(LivingEntity entity) {
        if (isServerSide(entity)) {
            ACTIVE.remove(entity);
        }
    }

    /** Successful hits landed so far during the current charge (upstream equivalent: {@code stabbedEntities}). */
    public static int getHits(LivingEntity entity) {
        LungeState state = ACTIVE.get(entity);
        return state == null ? 0 : state.hits;
    }

    /** Server-side tick of an actively charged bayonet. */
    public static void tick(LivingEntity attacker, ItemStack stack) {
        if (!isServerSide(attacker) || !GunItem.hasBayonet(stack)) {
            return;
        }
        int engagedTicks = attacker.getTicksUsingItem() - ENGAGE_DELAY_TICKS;
        if (engagedTicks < 0 || !DAMAGE_CONDITION.withinWindow(engagedTicks)) {
            return;
        }
        Level level = attacker.level();
        Vec3 eye = attacker.getEyePosition();
        Vec3 look = attacker.getViewVector(1.0F);
        double reach = REACH;
        // Keep the stab honest: don't lunge through solid blocks.
        BlockHitResult blockHit = level.clip(new ClipContext(eye, eye.add(look.scale(reach)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, attacker));
        if (blockHit.getType() != HitResult.Type.MISS) {
            reach = Math.max(0, eye.distanceTo(blockHit.getLocation()) - 0.1);
        }
        Vec3 end = eye.add(look.scale(reach));
        AABB sweep = attacker.getBoundingBox()
                .expandTowards(look.scale(reach))
                .inflate(Math.max(1.0, HITBOX_MARGIN));
        var candidates = level.getEntities(attacker, sweep, entity ->
                entity instanceof LivingEntity
                        && entity.isAlive()
                        && !entity.isSpectator()
                        && !attacker.isPassengerOfSameVehicle(entity)
                        && Utils.canHarm(attacker, entity));
        if (candidates.isEmpty()) {
            return;
        }
        candidates.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(eye)));

        Vec3 attackerMotion = motionOf(attacker);
        double attackerSpeed = attackerMotion.dot(look) * TICKS_PER_SECOND;
        long gameTime = level.getGameTime();
        LungeState state = ACTIVE.computeIfAbsent(attacker, entity -> new LungeState());
        boolean hitAny = false;
        for (Entity candidate : candidates) {
            LivingEntity target = (LivingEntity) candidate;
            Long lastContact = state.lastContactByTargetId.get(target.getId());
            if (lastContact != null && gameTime - lastContact < CONTACT_COOLDOWN_TICKS) {
                continue;
            }
            Optional<Vec3> intersection = target.getBoundingBox().inflate(HITBOX_MARGIN).clip(eye, end);
            if (intersection.isEmpty()) {
                continue;
            }
            double relativeSpeed = attackerMotion.subtract(motionOf(target)).dot(look) * TICKS_PER_SECOND;
            boolean applied = false;
            if (target.isPassenger() && DISMOUNT_CONDITION.test(engagedTicks, attackerSpeed, relativeSpeed)) {
                target.stopRiding();
                applied = true;
            }
            if (DAMAGE_CONDITION.test(engagedTicks, attackerSpeed, relativeSpeed)) {
                float damage = (float) Mth.floor(relativeSpeed * DAMAGE_MULTIPLIER);
                applied |= target.hurt(DamageSources.bayonet(level, attacker), Math.max(damage, 1));
            }
            if (KNOCKBACK_CONDITION.test(engagedTicks, attackerSpeed, relativeSpeed)) {
                // knockback takes the vector from the target back to the source of the blow
                target.knockback(KNOCKBACK_STRENGTH, -look.x, -look.z);
                applied = true;
            }
            if (!applied) {
                continue;
            }
            state.hits++;
            state.lastContactByTargetId.put(target.getId(), gameTime);
            hitAny = true;
        }
        if (hitAny) {
            // the attacker's own feedback comes through the packet; bystanders hear the world sound
            ServerPlayer exclude = attacker instanceof ServerPlayer serverPlayer ? serverPlayer : null;
            Vec3 at = attacker.getEyePosition().add(look.scale(Math.min(reach, 2.5)));
            level.playSound(exclude, at.x, at.y, at.z, SoundEvents.TRIDENT_HIT, attacker.getSoundSource(), 1.0F, 1.0F);
            if (attacker instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, ClientboundBayonetHitPacket.INSTANCE);
            }
        }
    }

    /** Vehicle-aware momentum, mirroring vanilla {@code KineticWeapon.getMotion}, scaled per tick. */
    private static Vec3 motionOf(LivingEntity entity) {
        Entity momentumSource = entity.getRootVehicle();
        return momentumSource.getDeltaMovement();
    }
}
