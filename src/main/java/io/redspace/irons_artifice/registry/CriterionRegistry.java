package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.advancement.GunCombatTrigger;
import io.redspace.irons_artifice.advancement.GunModifiedTrigger;
import io.redspace.irons_artifice.advancement.ShotGunTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public final class CriterionRegistry {
    public static final ShotGunTrigger SHOT_GUN = new ShotGunTrigger();
    public static final GunCombatTrigger GUN_COMBAT = new GunCombatTrigger();
    public static final GunModifiedTrigger GUN_MODIFIED = new GunModifiedTrigger();

    public static void register() {
        CriteriaTriggers.register(SHOT_GUN);
        CriteriaTriggers.register(GUN_COMBAT);
        CriteriaTriggers.register(GUN_MODIFIED);
    }
}
