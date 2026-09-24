package io.redspace.irons_artifice.data;

import com.google.common.base.Suppliers;
import io.redspace.irons_artifice.attribute.GunStat;
import io.redspace.irons_artifice.client.sounds.GunShotSoundSettings;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A gun's innate shot: its stats, and a factory for its non-numeric {@link ShotComponentMap}.
 * <p>
 * Every call to {@link #get()} builds a new map. Callers may mutate the result freely
 */
public interface ShotComponentTemplate extends Supplier<ShotComponentMap> {

    /**
     * @return a freshly built map
     */
    @Override
    ShotComponentMap get();

    /**
     * The gun's own stats, as {@link GunStat#BASE_ID} modifiers for the main hand. This is what the gun item carries as its default attribute modifiers.
     * <p>
     * Only available once attributes are registered
     */
    ItemAttributeModifiers baseStats();

    /**
     * @param damage            base damage per shot ({@link AttributeRegistry#GUN_DAMAGE})
     * @param spread            base spread in degrees ({@link AttributeRegistry#BULLET_SPREAD})
     * @param characterBlowback push-back applied to the shooter ({@link AttributeRegistry#CHARACTER_BLOWBACK})
     * @param fireDelayTicks    ticks between shots ({@link AttributeRegistry#FIRE_DELAY})
     * @param cameraRecoil      camera recoil profile ({@link ShotComponents#CAMERA_RECOIL})
     */
    static Builder builder(double damage, double spread, double characterBlowback, int fireDelayTicks, RecoilProfile cameraRecoil) {
        return new Builder(damage, spread, cameraRecoil, characterBlowback, fireDelayTicks);
    }

    final class Builder {
        public static final int DEFAULT_PROJECTILE_COUNT = 1;
        public static final double DEFAULT_GRAVITY = 0.05;
        public static final double DEFAULT_KNOCKBACK = 0.3;
        public static final double DEFAULT_IN_AIR_PENALTY = 1.5;

        private final RecoilProfile cameraRecoil;

        // absolute values; insertion order is the order they appear on the item
        private final Map<Holder<Attribute>, Double> stats = new LinkedHashMap<>();

        // everything else, applied in order after the base components
        private final List<Consumer<ShotComponentMap>> steps = new ArrayList<>();

        private Builder(double damage, double spread, RecoilProfile cameraRecoil, double characterBlowback, int fireDelayTicks) {
            this.cameraRecoil = cameraRecoil;
            stat(AttributeRegistry.GUN_DAMAGE, damage);
            stat(AttributeRegistry.BULLET_SPREAD, spread);
            stat(AttributeRegistry.PROJECTILE_COUNT, DEFAULT_PROJECTILE_COUNT);
            stat(AttributeRegistry.BULLET_SPEED, Bullet.BASE_SPEED);
            stat(AttributeRegistry.BULLET_GRAVITY, DEFAULT_GRAVITY);
            stat(AttributeRegistry.BULLET_KNOCKBACK, DEFAULT_KNOCKBACK);
            stat(AttributeRegistry.IN_AIR_PENALTY, DEFAULT_IN_AIR_PENALTY);
            stat(AttributeRegistry.BULLET_DRAG, Bullet.BASE_DRAG);
            stat(AttributeRegistry.UNDERWATER_DRAG, Bullet.BASE_UNDERWATER_DRAG);
            stat(AttributeRegistry.CHARACTER_BLOWBACK, characterBlowback);
            stat(AttributeRegistry.FIRE_DELAY, fireDelayTicks);
        }

        /* ************
         * Defaulted stats
         * ************/

        public Builder projectileCount(int projectileCount) {
            return stat(AttributeRegistry.PROJECTILE_COUNT, projectileCount);
        }

        public Builder bulletSpeed(double bulletSpeed) {
            return stat(AttributeRegistry.BULLET_SPEED, bulletSpeed);
        }

        /**
         * Helper for {@code bulletSpeed(Bullet.BASE_SPEED * multiplier)}
         */
        public Builder bulletSpeedMultiplier(double multiplier) {
            return bulletSpeed(Bullet.BASE_SPEED * multiplier);
        }

        public Builder gravity(double gravity) {
            return stat(AttributeRegistry.BULLET_GRAVITY, gravity);
        }

        public Builder knockback(double knockback) {
            return stat(AttributeRegistry.BULLET_KNOCKBACK, knockback);
        }

        /**
         * Spread multiplier while the shooter is airborne
         */
        public Builder inAirPenalty(double inAirPenalty) {
            return stat(AttributeRegistry.IN_AIR_PENALTY, inAirPenalty);
        }

        /**
         * Velocity the bullet keeps each tick
         */
        public Builder drag(double drag) {
            return stat(AttributeRegistry.BULLET_DRAG, drag);
        }

        /**
         * Velocity the bullet keeps each tick underwater, on top of {@link #drag}. 1 or more means no extra drag
         */
        public Builder underwaterDrag(double underwaterDrag) {
            return stat(AttributeRegistry.UNDERWATER_DRAG, underwaterDrag);
        }

        /* ************
         * Common optional components
         * ************/

        public Builder gunshotSound(Supplier<GunShotSoundStack> factory) {
            return set(ShotComponents.GUNSHOT_SOUND, factory);
        }

        public Builder gunshotSound(GunShotSoundSettings shot, GunShotSoundSettings echo, PlayableSound dryFire) {
            return gunshotSound(() -> new GunShotSoundStack(shot, echo, dryFire));
        }

        /**
         * Gunshot sound with the standard dry-fire click.
         */
        public Builder gunshotSound(GunShotSoundSettings shot, GunShotSoundSettings echo, Holder<SoundEvent> dryFire) {
            return gunshotSound(() -> new GunShotSoundStack(shot, echo, PlayableSound.of(dryFire, 0.75f, 1.4f, 1.6f)));
        }

        public Builder muzzleFlash(MuzzleFlashType... types) {
            return set(ShotComponents.MUZZLE_FLASH, () -> MuzzleFlashSettings.of(types));
        }

        /* ************
         * Generic
         * ************/

        /**
         * Sets the value a gun stat has on this gun before any modifiers. For a toggle, any value above zero turns it on.
         */
        public Builder stat(Holder<Attribute> stat, double value) {
            stats.put(stat, value);
            return this;
        }

        /**
         * Sets a component from a factory. The factory runs once per {@link ShotComponentTemplate#get()}.
         */
        public <T> Builder set(ComponentType<T> type, Supplier<? extends T> factory) {
            steps.add(map -> map.set(type, factory.get()));
            return this;
        }

        /**
         * Arbitrary step run against the freshly built map, after all base components are set.
         * Same contract as {@code GunModifier.apply}.
         */
        public Builder modify(Consumer<ShotComponentMap> step) {
            steps.add(step);
            return this;
        }

        public ShotComponentTemplate build() {
            // snapshot everything so later mutation of this builder cannot leak into the template
            List<Consumer<ShotComponentMap>> frozenSteps = List.copyOf(steps);
            Map<Holder<Attribute>, Double> frozenStats = new LinkedHashMap<>(stats);
            RecoilProfile cameraRecoil = this.cameraRecoil;
            // guns are defined statically, which may be before attributes exist
            Supplier<ItemAttributeModifiers> baseStats = Suppliers.memoize(() -> {
                ItemAttributeModifiers.Builder modifiers = ItemAttributeModifiers.builder();
                // a stat's resolved base is its attribute default plus this modifier
                frozenStats.forEach((stat, value) -> modifiers.add(
                        stat.getDelegate(),
                        new AttributeModifier(GunStat.BASE_ID, value - stat.value().getDefaultValue(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND));
                return modifiers.build();
            });
            return new ShotComponentTemplate() {
                @Override
                public ShotComponentMap get() {
                    ShotComponentMap map = new ShotComponentMap();
                    map.set(ShotComponents.CAMERA_RECOIL, cameraRecoil);
                    for (Consumer<ShotComponentMap> step : frozenSteps) {
                        step.accept(map);
                    }
                    return map;
                }

                @Override
                public ItemAttributeModifiers baseStats() {
                    return baseStats.get();
                }
            };
        }
    }
}
