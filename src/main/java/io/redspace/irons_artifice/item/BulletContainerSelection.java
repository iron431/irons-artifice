package io.redspace.irons_artifice.item;

import io.redspace.irons_artifice.entity.IExtendedLivingEntity;
import net.minecraft.world.entity.player.Player;

public final class BulletContainerSelection {
    private BulletContainerSelection() {
    }

    public static int get(Player player) {
        return ((IExtendedLivingEntity) player).irons_artifice$getBulletSelection();
    }

    public static void set(Player player, int index) {
        ((IExtendedLivingEntity) player).irons_artifice$setBulletSelection(index);
    }

    public static void clear(Player player) {
        set(player, BulletContainerContents.NO_SELECTION);
    }
}
