package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.FireOutcome;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.PendingShot;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TestFixtures {

    /**
     * Positive Z: the axis the arenas are laid out along, and the direction to fire when the shot
     * only has to go somewhere repeatable. A shot that has to connect is aimed by
     * {@link #aimAtHitbox} instead -- fired flat, a zombie's shot passes within a fifth of a block
     * of the top of its target's hitbox, and ordinary spread walks it over a few times in a hundred.
     */
    public static final Vec3 FORWARD = new Vec3(0.0, 0.0, 1.0);

    /**
     * A gun stack loaded with {@code rounds} and carrying {@code modifiers} in the container
     * {@link io.redspace.irons_artifice.item.GunplayManager#compose} reads them from.
     */
    public static ItemStack gunWith(Item gun, int rounds, Item... modifiers) {
        ItemStack stack = new ItemStack(gun);
        GunItem.setMagazine(stack, new MagazineContents(rounds));

        if (modifiers.length > 0) {
            GunContainer container = new GunContainer(stack);
            for (int slot = 0; slot < modifiers.length; slot++) {
                container.setItem(slot, new ItemStack(modifiers[slot]));
            }
            container.setChanged();
            return container.getGunStack();
        }
        return stack;
    }

    /**
     * A mock player holding the given gun, for any test that does not fire. It {@link #dismiss}es
     * itself at the end of the current tick, so a {@code startSequence} test built on it loses its
     * shooter mid-sequence with no compile error.
     */
    public static ServerPlayer shooter(GameTestHelper helper, BlockPos relativePos, ItemStack gun) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setPos(helper.absoluteVec(Vec3.atCenterOf(relativePos)));
        player.setItemSlot(EquipmentSlot.MAINHAND, gun);
        resetShotState(player);
        helper.runAfterDelay(0, () -> dismiss(helper, player));
        return player;
    }

    /**
     * Takes a finished test's mock player off the server, so it neither keeps ticking a dangling
     * {@link ReloadState} or {@link FireDelayState} nor receives a gunshot broadcast from a
     * later-running test in the same batch.
     * <p>
     * {@code Entity.discard()} is not enough. It clears {@code ServerLevel#players()}, but
     * {@code PlayerList.broadcast} -- what sends the gunshot sound -- reads {@code PlayerList}'s own
     * separate list, which only {@code PlayerList#remove} clears. That call covers both.
     * <p>
     * Scheduled with {@code runAfterDelay(0, ...)} rather than {@code runBeforeTestEnd}, which
     * schedules for {@code timeoutTicks - 1}: {@code succeed()} marks the test done synchronously,
     * and {@code GameTestInfo.tick()} then skips the queue where that runnable is waiting. Tick 0
     * is drained right after the test body returns, done or not.
     */
    private static void dismiss(GameTestHelper helper, ServerPlayer player) {
        resetShotState(player);
        helper.getLevel().getServer().getPlayerList().remove(player);
    }

    /**
     * A mob with no free will holding the given gun, for any test that fires a shot.
     * <p>
     * A shot that goes off plays a gunshot sound to every real player within 192 blocks
     * ({@code GunShotSoundStack#playGunShotSound}), the shooter included. A mock
     * {@link ServerPlayer} sits in the server's player list behind a fake connection, and this
     * mod's payloads throw "Payload ... may not be sent to the client!" at it. A mob is not a
     * player and never receives that broadcast. The gunplay pipeline takes a {@link LivingEntity}
     * throughout, so nothing else about the test changes.
     */
    public static LivingEntity firingShooter(GameTestHelper helper, BlockPos relativePos, ItemStack gun) {
        LivingEntity shooter = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, relativePos);
        shooter.setItemSlot(EquipmentSlot.MAINHAND, gun);
        resetShotState(shooter);
        return shooter;
    }

    /**
     * Clears the per-entity firing state. Fire delay and pending shots are attachments on the
     * shooter rather than components on the stack, so a shooter reused for a second shot in one
     * test comes back FIRE_DELAY_ACTIVE without this.
     */
    public static void resetShotState(LivingEntity shooter) {
        FireDelayState.clear(shooter);
        PendingShot.clear(shooter);
        ItemStack held = shooter.getMainHandItem();
        if (!held.isEmpty()) {
            ReloadState.remove(held);
        }
    }

    // ---------------------------------------------------------------------------------------
    // The control-versus-variant lane harness.
    //
    // A modifier is only ever "more damage" or "more knockback" or "more bullets" relative to the
    // same gun without it, so every modifier test fires one gun down two lanes of
    // TestCatalog.RANGE_TWO_LANE at once -- lane 0 unmodified, lane 1 carrying the modifiers
    // under test -- and asserts a relation between the two. Nothing below knows a balance number.
    // ---------------------------------------------------------------------------------------

    /** Lane indices for the two-lane range. Lane 0 fires the unmodified control shot. */
    public static final int CONTROL_LANE = 0;
    public static final int VARIANT_LANE = 1;

    /** Further apart than {@code ChainLightningOnHit.RADIUS}. */
    private static final int LANE_X_CONTROL = 1;
    private static final int LANE_X_VARIANT = 9;
    private static final int LANE_Y = 1;
    private static final int LANE_Z_ORIGIN = 1;

    /** Under the 1024 attribute ceiling. */
    private static final double TOUGH_TARGET_HEALTH = 200.0;

    /** Where the shooter for the given lane stands, in structure-relative coordinates. */
    public static BlockPos laneOrigin(int lane) {
        return new BlockPos(lane == CONTROL_LANE ? LANE_X_CONTROL : LANE_X_VARIANT, LANE_Y, LANE_Z_ORIGIN);
    }

    /** A position in the given lane, the given number of blocks downrange of its origin. */
    public static BlockPos targetPos(int lane, int distance) {
        return laneOrigin(lane).offset(0, 0, distance);
    }

    /**
     * A zombie with no free will and raised health: it survives several shots, so its health
     * measures damage, and it does not wander, so its displacement measures knockback. The carved
     * pumpkin is {@link #shieldFromDaylight}.
     */
    public static LivingEntity toughTarget(GameTestHelper helper, BlockPos relativePos) {
        return toughTarget(helper, relativePos, EntityType.ZOMBIE);
    }

    /**
     * As above, with a chosen mob type, for an on-hit effect a zombie cannot show at all. Every
     * undead mob is immune to poison in {@code LivingEntity.canBeAffected} (the
     * {@code minecraft:ignores_poison_and_regen} tag), so
     * {@code ModifierTests.venomCapsulePoisons} fires at a pig -- a zombie would fail its
     * variant half whether or not the modifier works.
     */
    public static LivingEntity toughTarget(GameTestHelper helper, BlockPos relativePos,
                                           EntityType<? extends Mob> entityType) {
        Mob target = helper.spawnWithNoFreeWill(entityType, relativePos);
        AttributeInstance maxHealth = target.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            throw helper.assertionException(Component.literal(
                    "target has no MAX_HEALTH attribute, so its health cannot be raised and the "
                            + "damage it reports would be capped at its default health"));
        }
        maxHealth.setBaseValue(TOUGH_TARGET_HEALTH);
        target.setHealth((float) TOUGH_TARGET_HEALTH);
        shieldFromDaylight(target);
        return target;
    }

    /** Barriers pass skylight and an unhelmeted undead burns in it. */
    static void shieldFromDaylight(LivingEntity living) {
        living.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
    }

    /**
     * What one lane's shot did, measured once the shot has settled. {@code shooter} and
     * {@code target} are the live entities, for anything the other three do not carry -- a mob
     * effect, frozen ticks, the shooter's recoil. {@link #shieldFromDaylight} covers both, so
     * either one's health can be read without a sunburn in it.
     */
    public record LaneResult(float damageTaken,
                             Vec3 targetDisplacement,
                             int bulletsSpawned,
                             LivingEntity target,
                             LivingEntity shooter) {
    }

    /** Two lanes' results, for a test that asserts a relation between them. */
    public record LaneComparison(LaneResult control, LaneResult variant) {
    }

    /** One value per lane. */
    public record PerLane<T>(T control, T variant) {
    }

    /** An entity with its health and position at the moment it was first watched. */
    public record Watched(LivingEntity entity, float healthBefore, Vec3 start) {
        public float damage() {
            return healthBefore - entity.getHealth();
        }

        public Vec3 displacement() {
            return entity.position().subtract(start);
        }
    }

    public static Watched watch(LivingEntity entity) {
        return new Watched(entity, entity.getHealth(), entity.position());
    }

    static void assertFired(GameTestHelper helper, String lane, FireOutcome outcome) {
        helper.assertTrue(outcome == FireOutcome.FIRED,
                "the " + lane + " lane's shot went off (tryFire returned " + outcome + ")");
    }

    public static ShotProfile composedProfile(GameTestHelper helper, LivingEntity shooter) {
        if (!(shooter.getMainHandItem().getItem() instanceof GunItem gunItem)) {
            throw helper.assertionException(Component.literal("the shooter is holding a gun"));
        }
        return GunplayManager.compose(shooter, gunItem.getGun(), shooter.getMainHandItem());
    }

    public static float composedSpread(GameTestHelper helper, LivingEntity shooter) {
        return (float) composedProfile(helper, shooter).value(ShotComponents.SPREAD);
    }

    /** Every clearance in a geometry test derives from its cone; a retune has to be re-derived, not absorbed. */
    public static void assertConeIs(GameTestHelper helper, String lane, float actualDegrees, float designDegrees) {
        helper.assertTrue(actualDegrees == designDegrees,
                "the " + lane + " lane fires through the " + designDegrees
                        + "-degree cone this test's geometry assumes (it fires through " + actualDegrees + ")");
    }

    /** A mob is airborne for its first two ticks and an airborne shooter's spread is multiplied by {@code IN_AIR_PENALTY}. */
    static final int GROUNDING_TICKS = 2;

    /** A flat shot from a zombie's eye leaves a fifth of a block of hitbox above the aim line. */
    static Vec3 aimAtHitbox(LivingEntity shooter, LivingEntity target) {
        return target.getBoundingBox().getCenter().subtract(shooter.getEyePosition()).normalize();
    }

    /**
     * Arms both lanes with the same gun, the variant lane also carrying the given modifiers, and
     * returns the handle that fires them. Call {@link LaneShots#thenCompare} on it; the handle is
     * the only thing that fires a shot or ends the test, so a caller that drops it gets a bare
     * timeout at the tick budget with no message of its own.
     * <p>
     * Nothing fires here. Both lanes fire on one tick, {@link #GROUNDING_TICKS} later, so the two
     * shots see the same world and a block-breaking or area-effect modifier in one lane cannot
     * reach the other's measurements. Bullet counts are sampled either side of each
     * {@code tryFire}, and the health and position baselines on that same tick: bullets travel over
     * a block per tick and vanish on impact, so a later count would miss them.
     *
     * @param targetDistance blocks downrange to place each lane's target
     * @param settleTicks    ticks to wait before measuring; long enough for the bullet to travel
     *                       {@code targetDistance} and for any on-hit effect to run
     */
    public static LaneShots fireLanes(GameTestHelper helper,
                                      Item gun,
                                      int targetDistance,
                                      int settleTicks,
                                      Item... variantModifiers) {
        return fireLanes(helper, gun, targetDistance, settleTicks, EntityType.ZOMBIE, variantModifiers);
    }

    /**
     * As above, with {@code targetType} for both targets instead of a zombie -- see
     * {@link #toughTarget(GameTestHelper, BlockPos, EntityType)}. Both shooters stay zombies;
     * nothing about firing depends on what is holding the gun.
     */
    public static LaneShots fireLanes(GameTestHelper helper,
                                      Item gun,
                                      int targetDistance,
                                      int settleTicks,
                                      EntityType<? extends Mob> targetType,
                                      Item... variantModifiers) {
        LivingEntity controlTarget =
                toughTarget(helper, targetPos(CONTROL_LANE, targetDistance), targetType);
        LivingEntity variantTarget =
                toughTarget(helper, targetPos(VARIANT_LANE, targetDistance), targetType);

        LivingEntity controlShooter =
                firingShooter(helper, laneOrigin(CONTROL_LANE), gunWith(gun, 1));
        LivingEntity variantShooter =
                firingShooter(helper, laneOrigin(VARIANT_LANE), gunWith(gun, 1, variantModifiers));
        shieldFromDaylight(controlShooter);
        shieldFromDaylight(variantShooter);

        return new LaneShots(
                helper,
                settleTicks,
                new ArmedLane(controlShooter, controlTarget),
                new ArmedLane(variantShooter, variantTarget));
    }

    /** One lane, loaded and standing, before its shot goes off. */
    private record ArmedLane(LivingEntity shooter, LivingEntity target) {
    }

    /** One lane's shot as it left the muzzle, plus the geometry it went out with. */
    private record FiredLane(FireOutcome outcome, @Nullable ShotGeometry geometry, LaneShot shot) {
    }

    /**
     * Everything about one shot that decides whether it could have hit, sampled the instant before
     * it went off. {@link LaneShots#assertShooterAddedNoSpread} and
     * {@link LaneShots#assertConeFitsTheTarget} read it, so a scenario that misses for a reason no
     * test would guess says so by name.
     *
     * @param movedSinceLastTick        how far the shooter travelled last tick, which is what
     *                                  {@code getSpreadForEntity} reads its speed from
     * @param gunSpreadDegrees          the composed {@code SPREAD}: what gun and modifiers asked for
     * @param effectiveSpreadDegrees    what {@code getSpreadForEntity} handed the bullet, once the
     *                                  shooter's own state was counted
     * @param reachAtTarget             how far off the aim line the widest shot in that cone can be
     *                                  by the time it arrives
     * @param targetVerticalHalfExtent  how far the hitbox reaches above the point aimed at, which
     *                                  {@link #aimAtHitbox} makes its centre
     */
    private record ShotGeometry(boolean onGround,
                                double movedSinceLastTick,
                                float gunSpreadDegrees,
                                float effectiveSpreadDegrees,
                                double reachAtTarget,
                                double targetVerticalHalfExtent) {
    }

    /**
     * Measures {@link ShotGeometry} before {@code tryFire}, so nothing the shot itself does can
     * perturb it -- recoil blowback included, which stages a {@code deltaMovement} the shooter acts
     * on next tick, after which {@code getSpreadForEntity} reads a moved {@code position()}.
     * <p>
     * Reads public mod API only: {@code GunplayManager.compose} (which posts
     * {@code ComposeShotEvent}, so {@code IGunslingerMob.applyDefaultMobNerfs}' spread is included)
     * and {@code getSpreadForEntity}. Nothing is reimplemented and no production code was opened up.
     *
     * @return {@code null} when the shooter holds no gun: the shot is about to come back
     *         {@code NO_GUN}, and {@link LaneShots#measureAndCompare}'s FIRED check, which runs
     *         first, is the one that should speak
     */
    private static @Nullable ShotGeometry shotGeometry(LivingEntity shooter, LivingEntity target) {
        if (!(shooter.getMainHandItem().getItem() instanceof GunItem gunItem)) {
            return null;
        }
        ShotProfile profile =
                GunplayManager.compose(shooter, gunItem.getGun(), shooter.getMainHandItem());
        float effective = GunplayManager.getSpreadForEntity(profile, shooter);
        AABB box = target.getBoundingBox();
        double range = box.getCenter().distanceTo(shooter.getEyePosition());
        return new ShotGeometry(
                shooter.onGround(),
                shooter.position().distanceTo(shooter.oldPosition()),
                (float) profile.value(ShotComponents.SPREAD),
                effective,
                range * Math.tan(Math.toRadians(effective)),
                box.getYsize() / 2.0);
    }

    /** Two armed lanes, not yet fired; a terminal method fires them, measures, and runs the claim. */
    public static final class LaneShots {
        private final GameTestHelper helper;
        private final int settleTicks;
        private final ArmedLane control;
        private final ArmedLane variant;
        private boolean compared;
        private FiredLane firedControl;
        private FiredLane firedVariant;
        private TestCatalog.Expectation expectation = TestCatalog.Expectation.CLAIM_HOLDS;

        public LaneShots expecting(TestCatalog.Expectation expectation) {
            this.expectation = expectation;
            return this;
        }

        private LaneShots(GameTestHelper helper, int settleTicks, ArmedLane control, ArmedLane variant) {
            this.helper = helper;
            this.settleTicks = settleTicks;
            this.control = control;
            this.variant = variant;
        }

        /** Fires both lanes, waits, checks the control landed, then runs {@code claim} under the expectation. */
        public void thenCompare(Consumer<LaneComparison> claim) {
            schedule((h, c) -> assertControlLanded(h, c, settleTicks), (h, c) -> {
            }, claim);
        }

        /** For a modifier that reaches a target the control shot cannot: the control missing is a precondition, the variant landing is part of the claim. */
        public void thenCompareExpectingControlMiss(Consumer<LaneComparison> claim) {
            schedule(LaneShots::assertControlMissed, LaneShots::assertVariantLanded, claim);
        }

        private void schedule(BiConsumer<GameTestHelper, LaneComparison> precondition,
                              BiConsumer<GameTestHelper, LaneComparison> claimGuard,
                              Consumer<LaneComparison> claim) {
            if (compared) {
                throw new IllegalStateException("one pair of lane shots ends one test; a terminal was already called");
            }
            compared = true;
            helper.startSequence()
                    .thenExecuteAfter(GROUNDING_TICKS, this::fire)
                    .thenExecuteAfter(settleTicks, () -> measureAndCompare(precondition, claimGuard, claim))
                    .thenSucceed();
        }

        /** Fires both lanes on one tick and records what each shot looked like at the muzzle. */
        private void fire() {
            withFailureNet(helper, "firing the lanes", () -> {
                float controlHealthBefore = control.target().getHealth();
                float variantHealthBefore = variant.target().getHealth();
                Vec3 controlPosBefore = control.target().position();
                Vec3 variantPosBefore = variant.target().position();
                ShotGeometry controlGeometry = shotGeometry(control.shooter(), control.target());
                ShotGeometry variantGeometry = shotGeometry(variant.shooter(), variant.target());

                int bulletsBefore = countBullets(helper);
                FireOutcome controlOutcome = GunplayManager.tryFire(
                        control.shooter(), aimAtHitbox(control.shooter(), control.target()));
                int bulletsAfterControl = countBullets(helper);
                FireOutcome variantOutcome = GunplayManager.tryFire(
                        variant.shooter(), aimAtHitbox(variant.shooter(), variant.target()));
                int bulletsAfterVariant = countBullets(helper);

                firedControl = new FiredLane(controlOutcome, controlGeometry,
                        new LaneShot(control.shooter(), control.target(),
                                bulletsAfterControl - bulletsBefore, controlHealthBefore, controlPosBefore));
                firedVariant = new FiredLane(variantOutcome, variantGeometry,
                        new LaneShot(variant.shooter(), variant.target(),
                                bulletsAfterVariant - bulletsAfterControl, variantHealthBefore, variantPosBefore));
            });
        }

        /** Measures both lanes, then runs the precondition and the claim, the claim guarded and under the expectation. */
        private void measureAndCompare(BiConsumer<GameTestHelper, LaneComparison> precondition,
                                       BiConsumer<GameTestHelper, LaneComparison> claimGuard,
                                       Consumer<LaneComparison> claim) {
            withFailureNet(helper, "measuring the settled lanes", () -> {
                if (firedControl == null || firedVariant == null) {
                    // fire() already failed this test through the same net; a bare NPE from
                    // measuring shots never taken would replace its message.
                    return;
                }
                assertFired(helper, "control", firedControl.outcome());
                assertFired(helper, "variant", firedVariant.outcome());
                assertShooterAddedNoSpread("control", firedControl.geometry());
                assertShooterAddedNoSpread("variant", firedVariant.geometry());
                assertConeFitsTheTarget("control", firedControl.geometry());
                assertConeFitsTheTarget("variant", firedVariant.geometry());

                LaneComparison comparison =
                        new LaneComparison(firedControl.shot().settle(), firedVariant.shot().settle());
                precondition.accept(helper, comparison);
                expectation.check(helper, () -> {
                    claimGuard.accept(helper, comparison);
                    claim.accept(comparison);
                });
            });
        }

        /** Checks the shooter added no spread of its own beyond what the gun and its modifiers composed. */
        private void assertShooterAddedNoSpread(String lane, @Nullable ShotGeometry geometry) {
            if (geometry == null) {
                // Not holding a gun; the FIRED check above already failed with NO_GUN, which is the
                // useful message.
                return;
            }
            helper.assertTrue(geometry.onGround(),
                    "the " + lane + " lane's shooter had settled onto the arena floor before firing "
                            + "(it had not, so IN_AIR_PENALTY widened its shot cone and it can miss "
                            + "a target it is aimed straight at)");
            helper.assertTrue(geometry.effectiveSpreadDegrees() == geometry.gunSpreadDegrees(),
                    "the " + lane + " lane's shooter added nothing of its own to the gun's spread "
                            + "(the gun composes to " + geometry.gunSpreadDegrees()
                            + " degrees, the shot went out at " + geometry.effectiveSpreadDegrees()
                            + " -- the shooter had moved " + geometry.movedSinceLastTick()
                            + " blocks on its previous tick, and getSpreadForEntity adds up to 20 "
                            + "degrees for a shooter it reads as moving)");
        }

        /** Checks the shot cone is narrow enough for the target to absorb all of it. */
        private void assertConeFitsTheTarget(String lane, @Nullable ShotGeometry geometry) {
            if (geometry == null) {
                return;
            }
            helper.assertTrue(geometry.reachAtTarget() < geometry.targetVerticalHalfExtent(),
                    "the " + lane + " lane's shot cone fits inside the target it is aimed at ("
                            + geometry.effectiveSpreadDegrees() + " degrees of spread reaches "
                            + geometry.reachAtTarget() + " blocks off the aim line at this range, "
                            + "against " + geometry.targetVerticalHalfExtent() + " blocks from the "
                            + "hitbox centre to its top -- move the target closer or fire a tighter "
                            + "gun, because this one sprays past what it is shooting at)");
        }

        /** Checks the control lane's shot landed within the settle window. */
        private static void assertControlLanded(GameTestHelper helper, LaneComparison comparison, int settleTicks) {
            helper.assertTrue(comparison.control().damageTaken() > 0.0F,
                    "the control lane's shot landed within its " + settleTicks
                            + "-tick settle window (it dealt no damage, so either it missed or "
                            + "the settle window is too short for the distance)");
        }

        static void assertControlMissed(GameTestHelper helper, LaneComparison comparison) {
            helper.assertTrue(comparison.control().damageTaken() <= 0.0F,
                    "the control lane's shot did not reach its target (it dealt "
                            + comparison.control().damageTaken() + ", so this scenario does not stop the unmodified shot)");
        }

        static void assertVariantLanded(GameTestHelper helper, LaneComparison comparison) {
            helper.assertTrue(comparison.variant().damageTaken() > 0.0F,
                    "the variant lane's shot reached its target (it dealt no damage)");
        }
    }

    /** A sequence step catches only {@code GameTestAssertException}, so anything else kills the run. */
    static void withFailureNet(GameTestHelper helper, String what, Runnable step) {
        try {
            step.run();
        } catch (GameTestAssertException assertionFailure) {
            throw assertionFailure;
        } catch (Exception unexpected) {
            GameTestAssertException failure = helper.assertionException(Component.literal(
                    what + " threw " + unexpected));
            failure.initCause(unexpected);
            throw failure;
        }
    }

    /** One lane's shot as sampled at the muzzle, plus the baselines its settled result needs. */
    private record LaneShot(LivingEntity shooter,
                            LivingEntity target,
                            int bulletsSpawned,
                            float healthBefore,
                            Vec3 positionBefore) {

        LaneResult settle() {
            return new LaneResult(healthBefore - target.getHealth(),
                    target.position().subtract(positionBefore),
                    bulletsSpawned,
                    target,
                    shooter);
        }
    }

    /** Counts live bullets anywhere in the test structure. */
    private static int countBullets(GameTestHelper helper) {
        return helper.getEntities(EntityRegistry.BULLET.get()).size();
    }

    /** A shooter and the direction it fires, for a test that measures the bullet rather than a target. */
    public record AimedLane(LivingEntity shooter, Vec3 aim) {
    }

    /**
     * Fires two aimed lanes on one tick after grounding, checks both went off through the cone the
     * test was laid out against, captures each lane's bullet by owner, and runs {@code atTheMuzzle}.
     */
    public static FlightShots fireAimedLanes(GameTestHelper helper, AimedLane control, AimedLane variant,
                                             float designConeDegrees, Consumer<PerLane<Bullet>> atTheMuzzle) {
        FlightShots shots = new FlightShots(helper);
        shots.sequence = helper.startSequence().thenExecuteAfter(GROUNDING_TICKS,
                () -> withFailureNet(helper, "firing the aimed lanes", () -> {
                    fireAimed(helper, "control", control, designConeDegrees);
                    fireAimed(helper, "variant", variant, designConeDegrees);
                    Bullet controlBullet = null;
                    Bullet variantBullet = null;
                    for (Bullet bullet : helper.getEntities(EntityRegistry.BULLET.get())) {
                        if (bullet.getOwner() == control.shooter()) {
                            controlBullet = bullet;
                        } else if (bullet.getOwner() == variant.shooter()) {
                            variantBullet = bullet;
                        }
                    }
                    atTheMuzzle.accept(new PerLane<>(controlBullet, variantBullet));
                    shots.muzzleStepFinished = true;
                }));
        return shots;
    }

    private static void fireAimed(GameTestHelper helper, String lane, AimedLane aimed, float designConeDegrees) {
        LivingEntity shooter = aimed.shooter();
        boolean onGround = shooter.onGround();
        ShotProfile profile = composedProfile(helper, shooter);
        float composed = (float) profile.value(ShotComponents.SPREAD);
        float effective = GunplayManager.getSpreadForEntity(profile, shooter);
        assertFired(helper, lane, GunplayManager.tryFire(shooter, aimed.aim()));
        helper.assertTrue(onGround, "the " + lane + " lane's shooter had settled before firing");
        helper.assertTrue(effective == composed,
                "the " + lane + " lane's shooter added nothing to the gun's spread (composed " + composed
                        + ", fired at " + effective + ")");
        assertConeIs(helper, lane, effective, designConeDegrees);
    }

    /** Steps after the shot. Later steps are skipped once the firing step has failed, so its message survives. */
    public static final class FlightShots {
        private final GameTestHelper helper;
        private GameTestSequence sequence;
        private boolean muzzleStepFinished;

        private FlightShots(GameTestHelper helper) {
            this.helper = helper;
        }

        public FlightShots then(int delayTicks, String what, Runnable step) {
            sequence = sequence.thenExecuteAfter(delayTicks, () -> withFailureNet(helper, what, () -> {
                if (muzzleStepFinished) {
                    step.run();
                }
            }));
            return this;
        }

        public void thenSucceed() {
            sequence.thenSucceed();
        }
    }

    public static Bullet inFlight(GameTestHelper helper, String lane, @Nullable Bullet bullet) {
        if (bullet == null) {
            throw helper.assertionException(Component.literal("the " + lane + " lane put no bullet in the air"));
        }
        helper.assertTrue(!bullet.isRemoved(),
                "the " + lane + " lane's bullet was still in flight when read (it died at " + bullet.position() + ")");
        return bullet;
    }

    /** {@code tickCount} counts exactly the {@code Bullet.tick} calls that ran, so a rate over N ticks needs N here. */
    public static void assertBulletTicksElapsed(GameTestHelper helper, String lane, Bullet bullet,
                                                int tickCountBefore, int expected) {
        int elapsed = inFlight(helper, lane, bullet).tickCount - tickCountBefore;
        helper.assertTrue(elapsed == expected,
                expected + " of the " + lane + " lane's bullet's ticks elapsed between readings (" + elapsed + " did)");
    }
}
