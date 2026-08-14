package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.ReloadState;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class DataComponentRegistry {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, IronsArtifice.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MagazineContents>> MAGAZINE =
            COMPONENTS.registerComponentType("magazine", builder -> builder
                    .persistent(MagazineContents.CODEC)
                    .networkSynchronized(MagazineContents.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ReloadState>> RELOAD_STATE =
            COMPONENTS.registerComponentType("reload_state", builder -> builder
                    .persistent(ReloadState.CODEC)
                    .networkSynchronized(ReloadState.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FireDelayState>> FIRE_DELAY_STATE =
            COMPONENTS.registerComponentType("fire_delay_state", builder -> builder
                    .persistent(FireDelayState.CODEC)
                    .networkSynchronized(FireDelayState.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DataComponentPatch>> MODIFIER_PATCH =
            COMPONENTS.registerComponentType("modifier_patch", builder -> builder
                    .persistent(DataComponentPatch.CODEC)
                    .networkSynchronized(DataComponentPatch.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> GUN_SPYGLASS =
            COMPONENTS.registerComponentType("gun_spyglass", builder -> builder
                    .persistent(Unit.CODEC)
                    .networkSynchronized(Unit.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AttachmentMap>> ATTACHMENT =
            COMPONENTS.registerComponentType("attachment", builder -> builder
                    .persistent(AttachmentMap.CODEC)
                    .networkSynchronized(AttachmentMap.STREAM_CODEC));

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }
}
