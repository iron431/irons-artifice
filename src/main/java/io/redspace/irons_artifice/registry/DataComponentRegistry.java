package io.redspace.irons_artifice.registry;

import com.mojang.serialization.Codec;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.ReloadState;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.item.component.ItemAttributeModifiers;
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
    /**
     * How far past the model's muzzle the flash appears and the ramrod reaches, in blocks. A property of what is attached to the gun,
     * not of a shot, so not a gun stat
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> MUZZLE_OFFSET =
            COMPONENTS.registerComponentType("muzzle_offset", builder -> builder
                    .persistent(Codec.FLOAT)
                    .networkSynchronized(ByteBufCodecs.FLOAT));

    /**
     * Stat changes a modifier item applies to the gun it is installed in. Slot groups are ignored.
     * An entry with the id {@link io.redspace.irons_artifice.attribute.GunStat#BASE_ID} replaces the gun's own base for that stat.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemAttributeModifiers>> GUN_MODIFIER_STATS =
            COMPONENTS.registerComponentType("gun_modifier_stats", builder -> builder
                    .persistent(ItemAttributeModifiers.CODEC)
                    .networkSynchronized(ItemAttributeModifiers.STREAM_CODEC)
                    .cacheEncoding());

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }
}
