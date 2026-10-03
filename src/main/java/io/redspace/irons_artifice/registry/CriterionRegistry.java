package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.advancement.GunCombatTrigger;
import io.redspace.irons_artifice.advancement.GunModifiedTrigger;
import io.redspace.irons_artifice.advancement.ShotGunTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class CriterionRegistry {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, IronsArtifice.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, ShotGunTrigger> SHOT_GUN =
            TRIGGERS.register("shot_gun", ShotGunTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, GunCombatTrigger> GUN_COMBAT =
            TRIGGERS.register("gun_combat", GunCombatTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, GunModifiedTrigger> GUN_MODIFIED =
            TRIGGERS.register("gun_modified", GunModifiedTrigger::new);

    public static void register(IEventBus modEventBus) {
        TRIGGERS.register(modEventBus);
    }
}
