package io.redspace.irons_artifice.gun;

import io.redspace.irons_artifice.data.FireCycleCueStack;
import io.redspace.irons_artifice.data.FireMode;
import io.redspace.irons_artifice.data.HandOccupancy;
import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.data.ReloadCueStack;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.item.TopLoadConfig;
import io.redspace.irons_artifice.item.animation_adjuster.AnimationAdjuster;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Definition of a specific gun and its properties. Immutable; construct via {@link #builder}.
 */
public final class GunProfile {
    private final Supplier<ShotComponentMap> baseProfileSupplier;
    private final int magazineCapacity;
    private final int modifierSlots;
    private final int reloadTimeTicks;
    private final FireMode fireMode;
    private final @Nullable TopLoadConfig topLoadConfig;
    private final ArmPoseKind armPoseKind;
    private final ReloadCueStack reloadCues;
    private final @Nullable PlayableSound equipSound;
    private final FireCycleCueStack fireCycleCues;
    private final List<AnimationAdjuster> animationAdjusters;
    private final Map<GunState, HandOccupancy> occupancyOverrides;

    private GunProfile(Builder builder) {
        this.baseProfileSupplier = Objects.requireNonNull(builder.baseProfileSupplier, "baseProfileSupplier");
        this.magazineCapacity = builder.magazineCapacity;
        this.modifierSlots = builder.modifierSlots;
        this.reloadTimeTicks = builder.reloadTimeTicks;
        this.fireMode = Objects.requireNonNull(builder.fireMode, "fireMode");
        this.topLoadConfig = builder.topLoadConfig;
        this.armPoseKind = Objects.requireNonNull(builder.armPoseKind, "armPoseKind");
        this.reloadCues = Objects.requireNonNull(builder.reloadCues, "reloadCues");
        this.equipSound = builder.equipSound;
        this.fireCycleCues = Objects.requireNonNull(builder.fireCycleCues, "fireCycleCues");
        this.animationAdjusters = List.copyOf(builder.animationAdjusters);
        this.occupancyOverrides = Map.copyOf(builder.occupancyOverrides);
    }

    /**
     * @param magazineCapacity    rounds the magazine holds
     * @param modifierSlots       number of modifier slots on the gun
     * @param reloadTimeTicks     ticks required to reload
     * @param baseProfileSupplier supplies innate (autoattack) shot component map; see {@link io.redspace.irons_artifice.data.ShotComponentTemplate}
     */
    public static Builder builder(int magazineCapacity, int modifierSlots, int reloadTimeTicks, FireMode fireMode, ArmPoseKind armPoseKind, Supplier<ShotComponentMap> baseProfileSupplier) {
        return new Builder(magazineCapacity, modifierSlots, reloadTimeTicks, fireMode, armPoseKind, baseProfileSupplier);
    }

    /* ************
     * Accessors
     * ************/

    public Supplier<ShotComponentMap> baseProfileSupplier() {
        return baseProfileSupplier;
    }

    public int magazineCapacity() {
        return magazineCapacity;
    }

    public int modifierSlots() {
        return modifierSlots;
    }

    public int reloadTimeTicks() {
        return reloadTimeTicks;
    }

    public FireMode fireMode() {
        return fireMode;
    }

    public @Nullable TopLoadConfig topLoadConfig() {
        return topLoadConfig;
    }

    public ArmPoseKind armPoseKind() {
        return armPoseKind;
    }

    public ReloadCueStack reloadCues() {
        return reloadCues;
    }

    public @Nullable PlayableSound equipSound() {
        return equipSound;
    }

    public FireCycleCueStack fireCycleCues() {
        return fireCycleCues;
    }

    public List<AnimationAdjuster> animationAdjusters() {
        return animationAdjusters;
    }

    public Map<GunState, HandOccupancy> occupancyOverrides() {
        return occupancyOverrides;
    }

    /* ************
     * Derived
     * ************/

    /**
     * @return a fresh innate component map for this gun
     */
    public ShotComponentMap baseProfile() {
        return baseProfileSupplier.get();
    }

    public HandOccupancy defaultOccupancy() {
        return armPoseKind == ArmPoseKind.RIFLE ? HandOccupancy.BOTH : HandOccupancy.MAINHAND;
    }

    public HandOccupancy occupancyFor(GunState state) {
        return occupancyOverrides.getOrDefault(state, defaultOccupancy());
    }

    @Override
    public String toString() {
        return "GunProfile[capacity=" + magazineCapacity
                + ", slots=" + modifierSlots
                + ", reloadTicks=" + reloadTimeTicks
                + ", fireMode=" + fireMode
                + ", armPose=" + armPoseKind
                + "]";
    }

    public static final class Builder {
        public static final int DEFAULT_MAGAZINE_CAPACITY = 1;
        public static final int DEFAULT_MODIFIER_SLOTS = 5;
        public static final int DEFAULT_RELOAD_TIME_TICKS = 40;

        private final Supplier<ShotComponentMap> baseProfileSupplier;
        private final int magazineCapacity;
        private final int modifierSlots;
        private final int reloadTimeTicks;
        private final FireMode fireMode;
        private final ArmPoseKind armPoseKind;

        private @Nullable TopLoadConfig topLoadConfig = null;
        private ReloadCueStack reloadCues = ReloadCueStack.EMPTY;
        private @Nullable PlayableSound equipSound = null;
        private FireCycleCueStack fireCycleCues = FireCycleCueStack.EMPTY;
        private List<AnimationAdjuster> animationAdjusters = List.of();
        private Map<GunState, HandOccupancy> occupancyOverrides = Map.of();

        private Builder(int magazineCapacity, int modifierSlots, int reloadTimeTicks, FireMode fireMode, ArmPoseKind armPoseKind, Supplier<ShotComponentMap> baseProfileSupplier) {
            this.baseProfileSupplier = baseProfileSupplier;
            this.magazineCapacity = magazineCapacity;
            this.modifierSlots = modifierSlots;
            this.reloadTimeTicks = reloadTimeTicks;
            this.fireMode = fireMode;
            this.armPoseKind = armPoseKind;
        }

        public Builder topLoadConfig(@Nullable TopLoadConfig topLoadConfig) {
            this.topLoadConfig = topLoadConfig;
            return this;
        }

        public Builder reloadCues(ReloadCueStack reloadCues) {
            this.reloadCues = reloadCues;
            return this;
        }

        public Builder equipSound(@Nullable PlayableSound equipSound) {
            this.equipSound = equipSound;
            return this;
        }

        public Builder fireCycleCues(FireCycleCueStack fireCycleCues) {
            this.fireCycleCues = fireCycleCues;
            return this;
        }

        public Builder animationAdjusters(AnimationAdjuster... animationAdjusters) {
            this.animationAdjusters = List.of(animationAdjusters);
            return this;
        }

        public Builder occupancy(Map<GunState, HandOccupancy> occupancyOverrides) {
            this.occupancyOverrides = occupancyOverrides;
            return this;
        }

        public Builder occupancy(GunState state, HandOccupancy occupancy) {
            this.occupancyOverrides = Map.of(state, occupancy);
            return this;
        }

        public GunProfile build() {
            return new GunProfile(this);
        }
    }
}
