package io.redspace.irons_artifice.registry;

import net.minecraft.world.entity.raid.Raid;

public final class ExtendedRaiderTypes {
    public static final Raid.RaiderType ILLIFICER = Raid.RaiderType.create(
            "IRONS_ARTIFICE_ILLIFICER",
            EntityRegistry.ILLIFICER.get(),
            new int[]{0, 0, 0, 1, 0, 1, 2, 3}
    );

    /**
     * Forge extends the enum when this class initializes, which must happen after entity registration and before the
     * first raid rolls its wave table.
     */
    public static void register() {
    }
}
