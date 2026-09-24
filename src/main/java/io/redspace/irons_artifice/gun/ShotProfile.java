package io.redspace.irons_artifice.gun;

import io.redspace.irons_artifice.attribute.GunStat;
import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.FireMode;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * @param components everything about the shot that is not a number: sounds, particles, on-hit effects...
 * @param stats      the shot's resolved gun stats. Private to this shot, see {@link GunStatResolver}
 */
public record ShotProfile(ItemStack itemStack, GunProfile gun, MagazineContents magazineContents,
                          ShotComponentMap components, AttributeMap stats) {

    /**
     * A shot from no gun at all: every stat at its default
     */
    public static ShotProfile empty() {
        return new ShotProfile(ItemStack.EMPTY, Guns.MUSKET, MagazineContents.EMPTY, new ShotComponentMap(), GunStatResolver.newStats());
    }

    /**
     * Returns the stored value for {@code type}, or a fresh default if absent.
     * The returned instance is not stored; do not mutate it. Use {@link #modify} to change a component.
     */
    public <T> T peek(ComponentType<T> type) {
        return components.getOrDefault(type);
    }

    /**
     * Ensures {@code type} is stored, then passes the stored instance to {@code consumer}.
     */
    public <T> void modify(ComponentType<T> type, Consumer<T> consumer) {
        consumer.accept(components.getOrCreate(type));
    }

    public <T> void remove(ComponentType<T> type) {
        components.remove(type);
    }

    /**
     * Returns the resolved value of a gun stat. An attribute that is not a gun stat resolves to its default.
     */
    public double value(Holder<Attribute> stat) {
        return stats.hasAttribute(stat) ? stats.getValue(stat) : stat.value().getDefaultValue();
    }

    /**
     * Whether a toggle stat is on
     */
    public boolean flag(Holder<Attribute> toggle) {
        return value(toggle) > 0;
    }

    /**
     * Returns the stat before any modifiers: what the gun itself supplies, on top of the shooter's own base
     */
    public double baseValue(Holder<Attribute> stat) {
        if (!stats.hasAttribute(stat)) {
            return stat.value().getDefaultValue();
        }
        double base = stats.getBaseValue(stat);
        if (stats.hasModifier(stat, GunStat.BASE_ID)) {
            base += stats.getModifierValue(stat, GunStat.BASE_ID);
        }
        return base;
    }

    /**
     * Adds a modifier to this shot only, replacing any modifier with the same id. Ids are unique per stat, so unrelated sources must not share one.
     */
    public void addModifier(Holder<Attribute> stat, Identifier id, double amount, AttributeModifier.Operation operation) {
        GunStatResolver.setModifier(stats, stat, new AttributeModifier(id, amount, operation));
    }

    public boolean removeModifier(Holder<Attribute> stat, Identifier id) {
        if (!stats.hasModifier(stat, id)) {
            return false;
        }
        AttributeInstance instance = stats.getInstance(stat);
        return instance != null && instance.removeModifier(id);
    }

    /**
     * Replaces what the gun supplies for the stat, keeping every modifier. The shot then starts from exactly {@code base}, whatever the
     * shooter's own base is.
     */
    public void setBase(Holder<Attribute> stat, double base) {
        addModifier(stat, GunStat.BASE_ID, base - stats.getBaseValue(stat), AttributeModifier.Operation.ADD_VALUE);
    }

    /**
     * Fixes the stat to a value, dropping every modifier
     */
    public void setFlat(Holder<Attribute> stat, double value) {
        AttributeInstance instance = stats.getInstance(stat);
        if (instance != null) {
            instance.removeModifiers();
            instance.setBaseValue(value);
        }
    }

    /**
     * Own stats, and a new component map that shares its values. For handing one resolved shot to each of its bullets: whatever a bullet
     * later does to its stats stays with that bullet.
     */
    public ShotProfile copy() {
        return new ShotProfile(itemStack, gun, magazineContents, components.copy(), GunStatResolver.copyOf(stats));
    }

    public ShotProfile deepCopy() {
        return new ShotProfile(itemStack, gun, magazineContents, components.deepCopy(), GunStatResolver.copyOf(stats));
    }

    /**
     * Bullets per shot. The stat itself may be fractional once outside modifiers are involved, and firing, the damage split and the tooltip
     * have to agree on one number.
     */
    public int projectileCount() {
        return Math.max(1, (int) Math.round(value(AttributeRegistry.PROJECTILE_COUNT)));
    }

    public int fireDelayTicks() {
        return (int) Math.round(value(AttributeRegistry.FIRE_DELAY) / Math.max(1e-6, value(AttributeRegistry.FIRE_RATE)));
    }

    public FireMode fireMode() {
        return flag(AttributeRegistry.AUTO_FIRE) ? FireMode.AUTO : gun.fireMode();
    }
}
