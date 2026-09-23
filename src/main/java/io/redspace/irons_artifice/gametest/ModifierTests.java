package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.data.FireMode;
import io.redspace.irons_artifice.data.RecoilState;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gametest.TestCatalog.LaneOptions;
import io.redspace.irons_artifice.gametest.TestCatalog.ModifierTest;
import io.redspace.irons_artifice.gametest.TestFixtures.AimedLane;
import io.redspace.irons_artifice.gametest.TestFixtures.PerLane;
import io.redspace.irons_artifice.gametest.TestFixtures.Watched;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.modifier.modifiers.LeechModifier;
import io.redspace.irons_artifice.modifier.on_hit_handlers.ChainShotOnHit;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.redspace.irons_artifice.gametest.TestCatalog.bespoke;
import static io.redspace.irons_artifice.gametest.TestCatalog.compose;
import static io.redspace.irons_artifice.gametest.TestCatalog.fireInPlace;
import static io.redspace.irons_artifice.gametest.TestCatalog.lanes;
import static io.redspace.irons_artifice.gametest.TestFixtures.toughTarget;
import static io.redspace.irons_artifice.gametest.TestFixtures.watch;

/** One catalog entry per registered modifier, ordered by harness. */
public final class ModifierTests {

    /** A musket's 0.5 degrees plus the 2 a mob adds on normal difficulty. */
    private static final float MOB_FIRED_MUSKET_CONE_DEGREES = 2.5F;
    private static final double KNOCKBACK_MARGIN = 0.05;
    /** Two blocks: the rear target sits on the aim line and cannot be pushed apart from the front one. */
    private static final int REAR_TARGET_GAP = 2;
    private static final int BYSTANDER_OFFSET = 1;
    private static final int SINGULARITY_BYSTANDER_OFFSET = 2;
    private static final double SINGULARITY_PROJECTION_MARGIN = 0.01;
    private static final double DISPLACEMENT_ROUNDING_ROOM = 1.0e-6;
    private static final double BLOWBACK_MARGIN = 0.05;
    private static final int WALL_Z = 5;
    private static final int ARENA_WIDTH = 11;
    private static final int ARENA_HEIGHT = 7;
    /** A fire-delay cycle clears when it finishes; the arquebus's shortened cycle is 13 ticks. */
    private static final int HAIR_TRIGGER_SETTLE_TICKS = 11;
    private static final int SPIRAL_TARGET_DISTANCE = 30;
    private static final int SPIRAL_WATER_START_Z = 2;
    private static final int SPIRAL_WATER_END_Z = 30;
    /** Water cuts an unmodified bullet's speed from its third tick; eight ticks is short of what it then needs to arrive. */
    private static final int SPIRAL_SETTLE_TICKS = 8;
    private static final LaneOptions REAR_TARGET_LANES = LaneOptions.DEFAULT;
    private static final LaneOptions SHRAPNEL_LANES = LaneOptions.DEFAULT;

    static final List<ModifierTest> ENTRIES = List.of(
            // ---- two lanes ----
            lanes(ItemRegistry.LEAD_CORE, "lead_core_knocks_back_further", (helper, lanes) -> {
                double control = lanes.control().targetDisplacement().length();
                double variant = lanes.variant().targetDisplacement().length();
                helper.assertTrue(variant > control * (1 + KNOCKBACK_MARGIN),
                        "lead core knocked the target further than the unmodified shot (control " + control
                                + ", variant " + variant + ")");
            }),
            lanes(ItemRegistry.OVERCHARGED_POWDER, "overcharged_powder_hits_harder", (helper, lanes) ->
                    helper.assertTrue(lanes.variant().damageTaken() > lanes.control().damageTaken(),
                            "overcharged powder dealt more damage than the unmodified shot (control "
                                    + lanes.control().damageTaken() + ", variant " + lanes.variant().damageTaken() + ")")),
            // A musket's one-tick delay rounds to one tick with or without the modifier.
            lanes(ItemRegistry.HAIR_TRIGGER, "hair_trigger_shortens_delay",
                    LaneOptions.DEFAULT.withGun(ItemRegistry.ARQUEBUS).withSettleTicks(HAIR_TRIGGER_SETTLE_TICKS),
                    (helper, lanes) -> helper.assertTrue(
                            FireDelayState.get(lanes.variant().shooter()).duration()
                                    < FireDelayState.get(lanes.control().shooter()).duration(),
                            "hair trigger started a shorter fire delay than the unmodified gun")),
            lanes(ItemRegistry.GAS_VENT, "gas_vent_reduces_recoil", (helper, lanes) -> {
                long now = helper.getLevel().getGameTime();
                float control = RecoilState.current(lanes.control().shooter(), now).pitch();
                float variant = RecoilState.current(lanes.variant().shooter(), now).pitch();
                helper.assertTrue(control > 0.0F, "the unmodified shooter still carries recoil to compare against");
                helper.assertTrue(variant < control, "gas vent left less recoil than the unmodified shot");
            }),
            lanes(ItemRegistry.HOOK_SHOT_MODIFIER, "hook_shot_pulls_target_back", (helper, lanes) -> {
                double control = lanes.control().targetDisplacement().z;
                double variant = lanes.variant().targetDisplacement().z;
                helper.assertTrue(control > 0.0, "the unmodified shot knocked its target downrange (" + control + ")");
                helper.assertTrue(variant < 0.0,
                        "hook shot dragged its target back toward the shooter (it moved " + variant + ")");
            }),
            lanes(ItemRegistry.BLOODLETTING_TIP_MODIFIER, "bloodletting_tip_trades_health_for_damage", (helper, lanes) -> {
                LivingEntity control = lanes.control().shooter();
                LivingEntity variant = lanes.variant().shooter();
                helper.assertValueEqual(control.getHealth(), control.getMaxHealth(),
                        "the unmodified shot cost its shooter no health");
                helper.assertValueEqual(variant.getHealth(), variant.getMaxHealth() - LeechModifier.HEALTH_COST,
                        "bloodletting tip charged its shooter LeechModifier.HEALTH_COST");
                helper.assertTrue(lanes.variant().damageTaken() > lanes.control().damageTaken(),
                        "bloodletting tip dealt more damage than the unmodified shot");
            }),
            // Pellets landing on one target in one tick share invulnerability frames, so damage undercounts them.
            lanes(ItemRegistry.SCATTERSHOT, "scattershot_fires_more_projectiles", (helper, lanes) ->
                    helper.assertTrue(lanes.variant().bulletsSpawned() > lanes.control().bulletsSpawned(),
                            "scattershot spawned more projectiles than the unmodified shot (control "
                                    + lanes.control().bulletsSpawned() + ", variant " + lanes.variant().bulletsSpawned() + ")")),
            lanes(ItemRegistry.STEEL_CORE, "steel_core_pierces_second_target",
                    REAR_TARGET_LANES,
                    helper -> new PerLane<>(
                            watch(toughTarget(helper, TestFixtures.targetPos(TestFixtures.CONTROL_LANE,
                                    REAR_TARGET_LANES.targetDistance() + REAR_TARGET_GAP))),
                            watch(toughTarget(helper, TestFixtures.targetPos(TestFixtures.VARIANT_LANE,
                                    REAR_TARGET_LANES.targetDistance() + REAR_TARGET_GAP)))),
                    (helper, rear, lanes) -> {
                        // The harness guards the cone against the front target only; the rear one is two blocks further.
                        TestFixtures.assertConeIs(helper, "control",
                                TestFixtures.composedSpread(helper, lanes.control().shooter()), MOB_FIRED_MUSKET_CONE_DEGREES);
                        helper.assertTrue(rear.control().damage() <= 0.0F,
                                "the unmodified shot stopped at the front target (the rear one took " + rear.control().damage() + ")");
                        helper.assertTrue(rear.variant().damage() > 0.0F,
                                "steel core's shot carried through to the target " + REAR_TARGET_GAP + " blocks behind");
                    }),
            lanes(ItemRegistry.BLACKPOWDER_CHARGE, "blackpowder_charge_damages_bystander",
                    helper -> bystanders(helper, BYSTANDER_OFFSET, Side.AWAY_FROM_CENTRE),
                    (helper, bystanders, lanes) -> {
                        helper.assertTrue(bystanders.control().damage() <= 0.0F,
                                "the unmodified shot left the bystander untouched (it took " + bystanders.control().damage() + ")");
                        helper.assertTrue(bystanders.variant().damage() > 0.0F,
                                "blackpowder charge damaged the bystander within its blast radius");
                    }),
            lanes(ItemRegistry.CHAIN_LIGHTNING, "chain_lightning_damages_bystander",
                    helper -> bystanders(helper, BYSTANDER_OFFSET, Side.AWAY_FROM_CENTRE),
                    (helper, bystanders, lanes) -> {
                        helper.assertTrue(bystanders.control().damage() <= 0.0F,
                                "the unmodified shot left the bystander untouched (it took " + bystanders.control().damage() + ")");
                        helper.assertTrue(bystanders.variant().damage() > 0.0F,
                                "chain lightning damaged the only bystander in range");
                    }),
            // Two blocks keeps a pulled bystander from colliding with the target and rebounding.
            lanes(ItemRegistry.SINGULARITY_CHARGE_MODIFIER, "singularity_charge_pulls_bystander",
                    helper -> bystanders(helper, SINGULARITY_BYSTANDER_OFFSET, Side.TOWARD_CENTRE),
                    (helper, bystanders, lanes) -> {
                        double control = towardnessProjection(bystanders.control(), lanes.control().target());
                        double variant = towardnessProjection(bystanders.variant(), lanes.variant().target());
                        helper.assertTrue(Math.abs(control) < DISPLACEMENT_ROUNDING_ROOM,
                                "the unmodified lane's bystander held its position (it moved " + control + ")");
                        helper.assertTrue(variant > SINGULARITY_PROJECTION_MARGIN,
                                "singularity charge pulled the bystander toward the impact (projected " + variant + ")");
                    }),
            // Dirt has 0.5 hardness, so one breaching shot breaks it and nothing else can.
            lanes(ItemRegistry.BREACHING_SHELL, "breaching_shell_breaks_blocks",
                    LaneOptions.DEFAULT.expectingControlMiss(),
                    helper -> {
                        fillCrossSection(helper, WALL_Z, WALL_Z, Blocks.DIRT);
                        return null;
                    },
                    (helper, unused, lanes) -> {
                        helper.assertTrue(wallColumnIntact(helper, TestFixtures.CONTROL_LANE),
                                "the unmodified shot left the control lane's wall intact");
                        helper.assertTrue(!wallColumnIntact(helper, TestFixtures.VARIANT_LANE),
                                "breaching shell broke through the variant lane's wall");
                    }),
            lanes(ItemRegistry.INCENDIARY_TIP_MODIFIER, "incendiary_tip_ignites", (helper, lanes) -> {
                helper.assertFalse(lanes.control().target().isOnFire(), "the unmodified shot left the target unlit");
                helper.assertTrue(lanes.variant().target().isOnFire(), "incendiary tip set the target alight");
            }),
            // Undead are immune to poison, so this fires at a pig.
            lanes(ItemRegistry.VENOM_CAPSULE, "venom_capsule_poisons",
                    LaneOptions.DEFAULT.withTargetType(EntityType.PIG),
                    (helper, lanes) -> {
                        helper.assertFalse(lanes.control().target().hasEffect(MobEffects.POISON),
                                "the unmodified shot left the target unpoisoned");
                        helper.assertTrue(lanes.variant().target().hasEffect(MobEffects.POISON),
                                "venom capsule poisoned the target it hit");
                    }),
            // Shrapnel spawns on impact, after bulletsSpawned() is sampled, so a sampler counts it.
            lanes(ItemRegistry.FROZEN_JACKET, "frozen_jacket_freezes",
                    SHRAPNEL_LANES,
                    helper -> {
                        BulletTally tally = new BulletTally();
                        helper.startSequence()
                                .thenExecuteAfter(TestFixtures.GROUNDING_TICKS, () -> {
                                })
                                .thenExecuteFor(SHRAPNEL_LANES.settleTicks(), () -> tally.sample(helper));
                        return tally;
                    },
                    (helper, tally, lanes) -> {
                        helper.assertTrue(
                                lanes.variant().target().getTicksFrozen() > lanes.control().target().getTicksFrozen(),
                                "frozen jacket left the target more frozen than the unmodified shot");
                        helper.assertTrue(tally.variant.size() > tally.control.size(),
                                "frozen jacket's shrapnel added bullets to the variant lane (variant "
                                        + tally.variant.size() + ", control " + tally.control.size() + ")");
                    }),
            // A ghast's 4-block hitbox lets an arquebus be aimed from 30 blocks without spraying past it.
            lanes(ItemRegistry.SPIRAL_TIP_MODIFIER, "spiral_tip_extends_underwater_range",
                    LaneOptions.DEFAULT.withGun(ItemRegistry.ARQUEBUS).withTargetType(EntityType.GHAST)
                            .withTargetDistance(SPIRAL_TARGET_DISTANCE).withSettleTicks(SPIRAL_SETTLE_TICKS)
                            .expectingControlMiss().withMaxTicks(40),
                    helper -> {
                        fillCrossSection(helper, SPIRAL_WATER_START_Z, SPIRAL_WATER_END_Z, Blocks.WATER);
                        return null;
                    },
                    // The control-miss terminal supplies the claim: the variant landed.
                    (helper, unused, lanes) -> {
                    }),
            // ---- compose ----
            compose(ItemRegistry.MECHANICAL_REPEATER, "mechanical_repeater_forces_auto", (helper, control, variant) -> {
                helper.assertTrue(control.profile().fireMode() != FireMode.AUTO, "the unmodified musket is not automatic");
                helper.assertValueEqual(variant.profile().fireMode(), FireMode.AUTO,
                        "mechanical repeater composed the profile into auto fire");
            }),
            compose(ItemRegistry.SCOPE_ATTACHMENT_MODIFIER, "scope_attachment_applies_patch", (helper, control, variant) -> {
                helper.assertFalse(control.stack().has(DataComponentRegistry.GUN_SPYGLASS.get()),
                        "a plain gun carries no spyglass marker");
                helper.assertTrue(variant.stack().has(DataComponentRegistry.GUN_SPYGLASS.get()),
                        "the scope's patch put the spyglass marker on the stack");
                helper.assertTrue(variant.profile().value(ShotComponents.SPREAD) < control.profile().value(ShotComponents.SPREAD),
                        "the scope narrowed composed spread");
            }),
            compose(ItemRegistry.BAYONET_ATTACHMENT_MODIFIER, "bayonet_attachment_applies_patch", (helper, control, variant) -> {
                helper.assertFalse(control.stack().has(DataComponents.KINETIC_WEAPON), "a plain gun has no kinetic-weapon component");
                helper.assertTrue(variant.stack().has(DataComponents.KINETIC_WEAPON),
                        "the bayonet's patch put the kinetic-weapon component on the stack");
            }),
            compose(ItemRegistry.SUPRESSOR_ATTACHMENT_MODIFIER, "suppressor_quietens_and_attaches", (helper, control, variant) -> {
                float plain = control.profile().peek(ShotComponents.GUNSHOT_SOUND).getBaseSound().end();
                float suppressed = variant.profile().peek(ShotComponents.GUNSHOT_SOUND).getBaseSound().end();
                helper.assertTrue(suppressed < plain,
                        "the suppressor's gunshot carries less far (plain " + plain + ", suppressed " + suppressed + ")");
                helper.assertFalse(hasMuzzleAttachment(control.stack()), "a plain gun has an empty muzzle slot");
                helper.assertTrue(hasMuzzleAttachment(variant.stack()), "the suppressor occupies the muzzle slot");
            }),
            // ReloadState.start takes speed as an argument; only attemptStartReload composes the multiplier.
            compose(ItemRegistry.GUN_OIL, "gun_oil_speeds_reload", (helper, control, variant) -> {
                helper.assertValueEqual(GunplayManager.attemptStartReload(control.shooter(), control.stack()),
                        ReloadResult.STARTING_RELOAD, "the unmodified reload started");
                helper.assertValueEqual(GunplayManager.attemptStartReload(variant.shooter(), variant.stack()),
                        ReloadResult.STARTING_RELOAD, "the gun oil reload started");
                ReloadState plain = ReloadState.get(control.stack());
                ReloadState oiled = ReloadState.get(variant.stack());
                helper.assertTrue(plain != null && oiled != null, "both reloads left state on their stacks");
                helper.assertTrue(oiled.durationTicks() < plain.durationTicks(),
                        "gun oil shortened the reload (plain " + plain.durationTicks() + ", oiled " + oiled.durationTicks() + ")");
            }),
            // ---- fire in place ----
            // The blunderbuss composes a non-zero CHARACTER_BLOWBACK; the musket composes zero.
            fireInPlace(ItemRegistry.BUFFER_SPRING, "buffer_spring_reduces_blowback", ItemRegistry.BLUNDERBUSS,
                    (helper, control, variant) -> {
                        double plain = control.push().length();
                        double sprung = variant.push().length();
                        helper.assertTrue(plain > 0.0, "the unmodified shot pushed its shooter");
                        helper.assertTrue(sprung < DISPLACEMENT_ROUNDING_ROOM,
                                "buffer spring zeroed the shooter's push (" + sprung + " against " + plain + ")");
                    }),
            fireInPlace(ItemRegistry.WIND_CHAMBER, "wind_chamber_increases_blowback", ItemRegistry.BLUNDERBUSS,
                    (helper, control, variant) -> {
                        double plain = control.push().length();
                        double vented = variant.push().length();
                        helper.assertTrue(plain > 0.0, "the unmodified shot pushed its shooter");
                        helper.assertTrue(vented > plain * (1 + BLOWBACK_MARGIN),
                                "wind chamber increased the shooter's push (" + vented + " against " + plain + ")");
                    }),
            // ---- bespoke ----
            bespoke(ItemRegistry.SEEKING_POWDER, "seeking_bends_toward_off_axis_target", TestCatalog.RANGE_TWO_LANE, 200,
                    ModifierTests::seekingBendsTowardOffAxisTarget),
            bespoke(ItemRegistry.ANTIGRAVITY_MODIFIER, "antigravity_cancels_bullet_drop", TestCatalog.RANGE_TWO_LANE, 200,
                    ModifierTests::antigravityCancelsBulletDrop),
            bespoke(ItemRegistry.TRICK_BULLET_MODIFIER, "trick_bullet_ricochets", TestCatalog.RANGE_TWO_LANE, 200,
                    ModifierTests::trickBulletRicochets),
            bespoke(ItemRegistry.CHAIN_SHOT, "chain_shot_links_targets", TestCatalog.RANGE_TWO_LANE, 20,
                    ModifierTests::chainShotLinksTargets),
            bespoke(ItemRegistry.MECHANICAL_ACCELERATOR_MODIFIER, "mechanical_accelerator_ramps_damage", TestCatalog.BOX_SMALL, 40,
                    ModifierTests::mechanicalAcceleratorRampsDamage),
            bespoke(ItemRegistry.ENCHANTED_BULLET_MODIFIER, "enchanted_bullet_spares_ammo", TestCatalog.BOX_SMALL, 20,
                    ModifierTests::enchantedBulletSparesAmmo)
    );

    private enum Side { AWAY_FROM_CENTRE, TOWARD_CENTRE }

    private static PerLane<Watched> bystanders(GameTestHelper helper, int offset, Side side) {
        return new PerLane<>(
                watch(toughTarget(helper, bystanderPos(TestFixtures.CONTROL_LANE, offset, side))),
                watch(toughTarget(helper, bystanderPos(TestFixtures.VARIANT_LANE, offset, side))));
    }

    private static BlockPos bystanderPos(int lane, int offset, Side side) {
        int awayFromCentre = lane == TestFixtures.CONTROL_LANE ? -1 : 1;
        int direction = side == Side.TOWARD_CENTRE ? -awayFromCentre : awayFromCentre;
        return TestFixtures.targetPos(lane, LaneOptions.DEFAULT.targetDistance()).offset(direction * offset, 0, 0);
    }

    /** How far {@code watched} moved along the line from where it started to {@code towards}. */
    private static double towardnessProjection(Watched watched, LivingEntity towards) {
        Vec3 reference = towards.getBoundingBox().getCenter().subtract(watched.start());
        return watched.displacement().dot(reference.normalize());
    }

    private static void fillCrossSection(GameTestHelper helper, int fromZ, int toZ, Block block) {
        for (int z = fromZ; z <= toZ; z++) {
            for (int x = 0; x < ARENA_WIDTH; x++) {
                for (int y = 1; y < ARENA_HEIGHT; y++) {
                    helper.setBlock(new BlockPos(x, y, z), block);
                }
            }
        }
    }

    private static boolean wallColumnIntact(GameTestHelper helper, int lane) {
        int x = TestFixtures.laneOrigin(lane).getX();
        for (int y = 1; y < ARENA_HEIGHT; y++) {
            if (!helper.getBlockState(new BlockPos(x, y, WALL_Z)).is(Blocks.DIRT)) {
                return false;
            }
        }
        return true;
    }

    /** Whether this stack's {@code ATTACHMENT} map names something in the muzzle slot. */
    private static boolean hasMuzzleAttachment(ItemStack stack) {
        AttachmentMap attachments = stack.get(DataComponentRegistry.ATTACHMENT.get());
        return attachments != null && attachments.attachments().containsKey("attachment_muzzle");
    }

    /** Distinct bullets seen per lane, sorted by which lane origin each is nearer on X. */
    private static final class BulletTally {
        final Set<UUID> control = new HashSet<>();
        final Set<UUID> variant = new HashSet<>();

        void sample(GameTestHelper helper) {
            double controlX = helper.absoluteVec(Vec3.atCenterOf(TestFixtures.laneOrigin(TestFixtures.CONTROL_LANE))).x;
            double variantX = helper.absoluteVec(Vec3.atCenterOf(TestFixtures.laneOrigin(TestFixtures.VARIANT_LANE))).x;
            for (Bullet bullet : helper.getEntities(EntityRegistry.BULLET.get())) {
                double x = bullet.getX();
                (Math.abs(x - controlX) <= Math.abs(x - variantX) ? control : variant).add(bullet.getUUID());
            }
        }
    }

    // ---- seeking ----

    private static final int SEEKING_TARGET_OFFSET = 3;
    private static final int SEEKING_TARGET_DISTANCE = 12;
    /** The control bullet reaches the far barrier on its third tick. */
    private static final int SEEKING_READING_TICKS = 2;
    private static final double SEEKING_TURN_SHARE = 0.10;
    private static final double HEADING_ROUNDING_ROOM = 1.0e-9;

    private record Reading(Bullet bullet, int tickCount, double value) {
    }

    /** Drag scales the whole velocity and gravity touches only y, so only seeking can change vx/vz. */
    private static void seekingBendsTowardOffAxisTarget(GameTestHelper helper, Item[] variantModifiers,
                                                        TestCatalog.Expectation expectation) {
        LivingEntity controlTarget = offAxisTarget(helper, TestFixtures.CONTROL_LANE, SEEKING_TARGET_OFFSET, SEEKING_TARGET_DISTANCE);
        LivingEntity variantTarget = offAxisTarget(helper, TestFixtures.VARIANT_LANE, -SEEKING_TARGET_OFFSET, SEEKING_TARGET_DISTANCE);
        AimedLane control = forwardLane(helper, TestFixtures.CONTROL_LANE);
        AimedLane variant = forwardLane(helper, TestFixtures.VARIANT_LANE, variantModifiers);
        AtomicReference<PerLane<Reading>> atMuzzle = new AtomicReference<>();

        TestFixtures.fireAimedLanes(helper, control, variant, MOB_FIRED_MUSKET_CONE_DEGREES, bullets ->
                        atMuzzle.set(new PerLane<>(
                                heading(helper, "control", bullets.control()),
                                heading(helper, "variant", bullets.variant()))))
                .then(SEEKING_READING_TICKS, "comparing the two bullets' heading", () -> {
                    double controlTurn = turn(helper, "control", atMuzzle.get().control(), control, controlTarget);
                    double variantTurn = turn(helper, "variant", atMuzzle.get().variant(), variant, variantTarget);
                    double required = headingStraightAt(variant.shooter(), variantTarget) * SEEKING_TURN_SHARE;
                    helper.assertTrue(Math.abs(controlTurn) < HEADING_ROUNDING_ROOM,
                            "the unmodified shot held its heading (it turned " + controlTurn + ")");
                    expectation.check(helper, () -> helper.assertTrue(variantTurn > required,
                            "seeking turned the shot toward its target (turned " + variantTurn + ", required "
                                    + required + ", control " + controlTurn + ")"));
                })
                .thenSucceed();
    }

    private static LivingEntity offAxisTarget(GameTestHelper helper, int lane, int offsetX, int distance) {
        BlockPos origin = TestFixtures.laneOrigin(lane);
        return toughTarget(helper, new BlockPos(origin.getX() + offsetX, origin.getY(), origin.getZ() + distance));
    }

    private static AimedLane forwardLane(GameTestHelper helper, int lane, Item... modifiers) {
        LivingEntity shooter = TestFixtures.firingShooter(helper, TestFixtures.laneOrigin(lane),
                TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 1, modifiers));
        TestFixtures.shieldFromDaylight(shooter);
        return new AimedLane(shooter, TestFixtures.FORWARD);
    }

    private static Reading heading(GameTestHelper helper, String lane, @Nullable Bullet bullet) {
        Bullet flying = TestFixtures.inFlight(helper, lane, bullet);
        return new Reading(flying, flying.tickCount, horizontalHeading(helper, lane, flying));
    }

    private static double horizontalHeading(GameTestHelper helper, String lane, Bullet bullet) {
        Vec3 velocity = TestFixtures.inFlight(helper, lane, bullet).getDeltaMovement();
        helper.assertTrue(velocity.z > 0.0, "the " + lane + " lane's bullet was still travelling downrange (" + velocity + ")");
        return velocity.x / velocity.z;
    }

    /** Heading change since the muzzle, signed positive toward this lane's target. */
    private static double turn(GameTestHelper helper, String lane, Reading muzzle, AimedLane aimed, LivingEntity target) {
        TestFixtures.assertBulletTicksElapsed(helper, lane, muzzle.bullet(), muzzle.tickCount(), SEEKING_READING_TICKS);
        double turned = horizontalHeading(helper, lane, muzzle.bullet()) - muzzle.value();
        double side = target.getBoundingBox().getCenter().x - aimed.shooter().getEyePosition().x;
        return turned * Math.signum(side);
    }

    private static double headingStraightAt(LivingEntity shooter, LivingEntity target) {
        Vec3 toTarget = target.getBoundingBox().getCenter().subtract(shooter.getEyePosition());
        return Math.abs(toTarget.x) / toTarget.z;
    }

    // ---- antigravity ----

    private static final double ANTIGRAVITY_MARGIN = 0.5;

    /** One bullet tick is exactly vy' = vy * drag - gravity, so two readings recover the gravity flown under. */
    private static void antigravityCancelsBulletDrop(GameTestHelper helper, Item[] variantModifiers,
                                                     TestCatalog.Expectation expectation) {
        AimedLane control = forwardLane(helper, TestFixtures.CONTROL_LANE);
        AimedLane variant = forwardLane(helper, TestFixtures.VARIANT_LANE, variantModifiers);
        AtomicReference<PerLane<Reading>> atMuzzle = new AtomicReference<>();

        TestFixtures.fireAimedLanes(helper, control, variant, MOB_FIRED_MUSKET_CONE_DEGREES, bullets ->
                        atMuzzle.set(new PerLane<>(
                                verticalSpeed(helper, "control", bullets.control()),
                                verticalSpeed(helper, "variant", bullets.variant()))))
                .then(1, "comparing the two bullets' vertical acceleration", () -> {
                    double controlGravity = gravityOverOneTick(helper, "control", atMuzzle.get().control());
                    double variantGravity = gravityOverOneTick(helper, "variant", atMuzzle.get().variant());
                    helper.assertTrue(controlGravity > 0.0,
                            "the unmodified bullet lost vertical speed beyond drag (" + controlGravity + ")");
                    expectation.check(helper, () -> helper.assertTrue(variantGravity < controlGravity * ANTIGRAVITY_MARGIN,
                            "antigravity removed most of the gravity (control " + controlGravity
                                    + " per tick, variant " + variantGravity + ")"));
                })
                .thenSucceed();
    }

    private static Reading verticalSpeed(GameTestHelper helper, String lane, @Nullable Bullet bullet) {
        Bullet flying = TestFixtures.inFlight(helper, lane, bullet);
        return new Reading(flying, flying.tickCount, flying.getDeltaMovement().y);
    }

    private static double gravityOverOneTick(GameTestHelper helper, String lane, Reading muzzle) {
        Bullet flying = TestFixtures.inFlight(helper, lane, muzzle.bullet());
        TestFixtures.assertBulletTicksElapsed(helper, lane, flying, muzzle.tickCount(), 1);
        return muzzle.value() * (double) flying.getDrag() - flying.getDeltaMovement().y;
    }

    // ---- trick bullet ----

    private static final int TRICKSHOT_WALL_Z = 8;
    private static final int TRICKSHOT_TARGET_Z = 4;
    private static final int TRICKSHOT_TARGET_OFFSET = 3;
    private static final int TRICKSHOT_SETTLE_TICKS = 20;

    /** A ricochet is a mirror, so aiming at the target's image in the wall aims the bounce. */
    private static void trickBulletRicochets(GameTestHelper helper, Item[] variantModifiers,
                                             TestCatalog.Expectation expectation) {
        // Stone: no gun here can break it, so the bullet takes the ricochet branch.
        fillCrossSection(helper, TRICKSHOT_WALL_Z, TRICKSHOT_WALL_Z, Blocks.STONE);
        Watched controlTarget = watch(offAxisTarget(helper, TestFixtures.CONTROL_LANE, TRICKSHOT_TARGET_OFFSET,
                TRICKSHOT_TARGET_Z - TestFixtures.laneOrigin(TestFixtures.CONTROL_LANE).getZ()));
        Watched variantTarget = watch(offAxisTarget(helper, TestFixtures.VARIANT_LANE, -TRICKSHOT_TARGET_OFFSET,
                TRICKSHOT_TARGET_Z - TestFixtures.laneOrigin(TestFixtures.VARIANT_LANE).getZ()));
        AimedLane control = mirrorLane(helper, TestFixtures.CONTROL_LANE, controlTarget.entity());
        AimedLane variant = mirrorLane(helper, TestFixtures.VARIANT_LANE, variantTarget.entity(), variantModifiers);

        TestFixtures.fireAimedLanes(helper, control, variant, MOB_FIRED_MUSKET_CONE_DEGREES, bullets -> {
                })
                .then(TRICKSHOT_SETTLE_TICKS, "measuring the settled trickshot lanes", () -> {
                    helper.assertTrue(controlTarget.damage() <= 0.0F,
                            "the unmodified shot flew past its target into the wall (it dealt " + controlTarget.damage() + ")");
                    expectation.check(helper, () -> helper.assertTrue(variantTarget.damage() > 0.0F,
                            "the trick bullet bounced off the backstop onto its target"));
                })
                .thenSucceed();
    }

    private static AimedLane mirrorLane(GameTestHelper helper, int lane, LivingEntity target, Item... modifiers) {
        LivingEntity shooter = TestFixtures.firingShooter(helper, TestFixtures.laneOrigin(lane),
                TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 1, modifiers));
        TestFixtures.shieldFromDaylight(shooter);
        double wallFace = helper.absolutePos(new BlockPos(0, 0, TRICKSHOT_WALL_Z)).getZ();
        Vec3 centre = target.getBoundingBox().getCenter();
        Vec3 mirrored = new Vec3(centre.x, centre.y, 2.0 * wallFace - centre.z);
        return new AimedLane(shooter, mirrored.subtract(shooter.getEyePosition()).normalize());
    }

    // ---- chain shot ----

    private static final int CHAIN_IN_RANGE_OFFSET = 2;
    private static final int CHAIN_OUT_OF_RANGE_OFFSET = 16;

    /** Chain shot links a hit to the shooter's previous hit, so it needs the installed handler called more than once. */
    private static void chainShotLinksTargets(GameTestHelper helper, Item[] variantModifiers,
                                              TestCatalog.Expectation expectation) {
        ItemStack gun = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 0, variantModifiers);
        ShotProfile profile = GunplayManager.compose(null, ((GunItem) gun.getItem()).getGun(), gun);
        Optional<ChainShotOnHit> installed = profile.peek(ShotComponents.POST_HIT_EFFECTS).all().stream()
                .filter(ChainShotOnHit.class::isInstance)
                .map(ChainShotOnHit.class::cast)
                .findFirst();

        LivingEntity owner = TestFixtures.firingShooter(helper, new BlockPos(1, 1, 1), ItemStack.EMPTY);
        Bullet bullet = new Bullet(EntityRegistry.BULLET.get(), helper.getLevel());
        bullet.setOwner(owner);
        LivingEntity first = toughTarget(helper, new BlockPos(1, 1, 2));
        LivingEntity second = toughTarget(helper, new BlockPos(1, 1, 2 + CHAIN_IN_RANGE_OFFSET));
        LivingEntity third = toughTarget(helper, new BlockPos(1, 1, 2 + CHAIN_IN_RANGE_OFFSET + CHAIN_OUT_OF_RANGE_OFFSET));
        helper.assertValueEqual(countChainEntities(helper), 0, "no chain links exist before any hit");

        expectation.check(helper, () -> {
            helper.assertTrue(installed.isPresent(), "composing the gun installed a ChainShotOnHit");
            ChainShotOnHit handler = installed.get();
            handler.postHit(helper.getLevel(), bullet, new EntityHitResult(first), first);
            helper.assertValueEqual(countChainEntities(helper), 0, "the first hit has nothing to chain to");
            handler.postHit(helper.getLevel(), bullet, new EntityHitResult(second), second);
            helper.assertValueEqual(countChainEntities(helper), 1, "a hit within SPAWN_RANGE of the last spawned one link");
            handler.postHit(helper.getLevel(), bullet, new EntityHitResult(third), third);
            helper.assertValueEqual(countChainEntities(helper), 1, "a hit outside SPAWN_RANGE spawned no link");
        });
        helper.succeed();
    }

    private static int countChainEntities(GameTestHelper helper) {
        return helper.getEntities(EntityRegistry.CHAIN.get()).size();
    }

    // ---- mechanical accelerator ----

    private static final int ACCEL_TARGET_DISTANCE = 4;
    private static final int ACCEL_LANDING_TICKS = 2;
    /** Over 10 so the second hit clears invulnerability frames; under RecentShots' 20-tick window. */
    private static final int ACCEL_FIRE_GAP_TICKS = 15;

    private static void mechanicalAcceleratorRampsDamage(GameTestHelper helper, Item[] variantModifiers,
                                                         TestCatalog.Expectation expectation) {
        ItemStack gun = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 2, variantModifiers);
        LivingEntity shooter = TestFixtures.firingShooter(helper, new BlockPos(1, 1, 1), gun);
        LivingEntity target = toughTarget(helper, new BlockPos(1, 1, 1 + ACCEL_TARGET_DISTANCE));
        TestFixtures.shieldFromDaylight(shooter);
        float[] healthBefore = new float[1];
        float[] shotDamage = new float[2];

        helper.startSequence()
                .thenExecuteAfter(TestFixtures.GROUNDING_TICKS, () -> {
                    healthBefore[0] = target.getHealth();
                    fireAt(helper, shooter, target, "first");
                })
                .thenExecuteAfter(ACCEL_LANDING_TICKS, () -> {
                    shotDamage[0] = healthBefore[0] - target.getHealth();
                    healthBefore[0] = target.getHealth();
                    TestFixtures.resetShotState(shooter);
                })
                .thenExecuteAfter(ACCEL_FIRE_GAP_TICKS - ACCEL_LANDING_TICKS, () -> fireAt(helper, shooter, target, "second"))
                .thenExecuteAfter(ACCEL_LANDING_TICKS, () -> shotDamage[1] = healthBefore[0] - target.getHealth())
                .thenExecute(() -> {
                    helper.assertTrue(shotDamage[0] > 0.0F, "the first shot landed");
                    expectation.check(helper, () -> helper.assertTrue(shotDamage[1] > shotDamage[0],
                            "the second shot did more damage than the first (" + shotDamage[0] + " then " + shotDamage[1] + ")"));
                })
                .thenSucceed();
    }

    private static void fireAt(GameTestHelper helper, LivingEntity shooter, LivingEntity target, String which) {
        TestFixtures.assertFired(helper, which, GunplayManager.tryFire(shooter, TestFixtures.aimAtHitbox(shooter, target)));
    }

    // ---- enchanted bullet ----

    /** 0.875^100 is about 1.6e-6: the chance a working modifier spares nothing. */
    private static final int AMMO_SHOTS = 100;

    private static void enchantedBulletSparesAmmo(GameTestHelper helper, Item[] variantModifiers,
                                                  TestCatalog.Expectation expectation) {
        LivingEntity control = TestFixtures.firingShooter(helper, new BlockPos(1, 1, 1),
                TestFixtures.gunWith(ItemRegistry.MUSKET.get(), AMMO_SHOTS));
        LivingEntity variant = TestFixtures.firingShooter(helper, new BlockPos(4, 1, 4),
                TestFixtures.gunWith(ItemRegistry.MUSKET.get(), AMMO_SHOTS, variantModifiers));

        for (int i = 0; i < AMMO_SHOTS; i++) {
            TestFixtures.assertFired(helper, "control", GunplayManager.tryFire(control, TestFixtures.FORWARD));
            TestFixtures.resetShotState(control);
            TestFixtures.assertFired(helper, "variant", GunplayManager.tryFire(variant, TestFixtures.FORWARD));
            TestFixtures.resetShotState(variant);
        }
        int controlConsumed = AMMO_SHOTS - GunItem.getMagazine(control.getMainHandItem()).count();
        int variantConsumed = AMMO_SHOTS - GunItem.getMagazine(variant.getMainHandItem()).count();

        helper.assertValueEqual(controlConsumed, AMMO_SHOTS, "the unmodified gun consumed a round every shot");
        expectation.check(helper, () -> helper.assertTrue(variantConsumed < controlConsumed,
                "enchanted bullet spared at least one round in " + AMMO_SHOTS + " shots (consumed " + variantConsumed + ")"));
        helper.succeed();
    }
}
