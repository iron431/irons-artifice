package io.redspace.irons_artifice.gun;

import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.FireMode;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.item.MagazineContents;
import net.minecraft.world.item.ItemStack;

public record ShotProfile(ItemStack itemStack, GunProfile gun, MagazineContents magazineContents,
                          ShotComponentMap components) {

    public <T> T get(ComponentType<T> type) {
        return components.getOrDefault(type);
    }

    public <T> void remove(ComponentType<T> type) {
        components.remove(type);
    }

    public double value(ComponentType<Value> type) {
        return components.getOrDefault(type).compute();
    }

    public ShotProfile copy() {
        return new ShotProfile(itemStack, gun, magazineContents, components.copy());
    }

    public ShotProfile deepCopy() {
        return new ShotProfile(itemStack, gun, magazineContents, components.deepCopy());
    }

    public double fireDelayTicks() {
        return value(ShotComponents.FIRE_DELAY) / Math.max(1e-6, value(ShotComponents.FIRE_RATE));
    }

    public FireMode fireMode() {
        return components.getOrDefault(ShotComponents.FORCE_AUTO_FIRE) ? FireMode.AUTO : gun.fireMode();
    }
}
