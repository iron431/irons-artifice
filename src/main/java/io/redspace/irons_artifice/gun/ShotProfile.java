package io.redspace.irons_artifice.gun;

import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.FireMode;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.item.MagazineContents;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public record ShotProfile(ItemStack itemStack, GunProfile gun, MagazineContents magazineContents,
                          ShotComponentMap components) {

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

    /**
     * Adds {@code modifier} to the stored {@link Value} for {@code type}, creating it from its default if absent.
     */
    public void modifyValue(ComponentType<Value> type, ValueModifier modifier) {
        components.modifyValue(type, modifier);
    }

    public <T> void remove(ComponentType<T> type) {
        components.remove(type);
    }

    /**
     * Returns the computed value for {@code type}, or the default value
     */
    public double value(ComponentType<Value> type) {
        return components.getOrDefault(type).compute();
    }

    public ShotProfile copy() {
        return new ShotProfile(itemStack, gun, magazineContents, components.copy());
    }

    public ShotProfile deepCopy() {
        return new ShotProfile(itemStack, gun, magazineContents, components.deepCopy());
    }

    public int fireDelayTicks() {
        return Math.max(1, (int) Math.round(value(ShotComponents.FIRE_DELAY) / Math.max(1e-6, value(ShotComponents.FIRE_RATE))));
    }

    public FireMode fireMode() {
        return components.getOrDefault(ShotComponents.FORCE_AUTO_FIRE) ? FireMode.AUTO : gun.fireMode();
    }
}
