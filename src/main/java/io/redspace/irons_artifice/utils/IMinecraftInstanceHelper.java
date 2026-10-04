package io.redspace.irons_artifice.utils;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface IMinecraftInstanceHelper {
    @Nullable
    Player player();
}
