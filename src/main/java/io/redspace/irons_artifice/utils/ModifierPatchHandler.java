package io.redspace.irons_artifice.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.modifier.ModifierItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A modifier patch is a {@link CompoundTag} of stack tag keys. Rebuilding copies patched entries onto the item's tag, and removes old patched keys.
 */
public final class ModifierPatchHandler {
    public static final String TAG_KEY = IronsArtifice.id("modifier_patch").toString();

    private static final DynamicOps<Tag> NBT_OPS = RegistryOps.create(NbtOps.INSTANCE,
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

    public static void rebuild(ItemStack gun, Container modifiers) {
        CompoundTag next = combine(modifiers);
        CompoundTag previous = gun.getTagElement(TAG_KEY);
        if (previous == null) {
            previous = new CompoundTag();
        }
        if (previous.equals(next)) {
            return;
        }

        for (String key : previous.getAllKeys()) {
            if (!next.contains(key)) {
                gun.removeTagKey(key);
            }
        }

        if (next.isEmpty()) {
            gun.removeTagKey(TAG_KEY);
            return;
        }
        CompoundTag tag = gun.getOrCreateTag();
        for (String key : next.getAllKeys()) {
            tag.put(key, next.get(key).copy());
        }
        tag.put(TAG_KEY, next);
    }

    public static CompoundTag combine(Container modifiers) {
        CompoundTag patch = new CompoundTag();
        Map<String, ResourceLocation> attachments = new LinkedHashMap<>();
        for (int slot = 0; slot < modifiers.getContainerSize(); slot++) {
            ItemStack stack = modifiers.getItem(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof ModifierItem modifierItem)) {
                continue;
            }
            Optional<CompoundTag> modifierPatch = modifierItem.getModifier().getPatch();
            if (modifierPatch.isEmpty() || modifierPatch.get().isEmpty()) {
                continue;
            }
            applyPatch(patch, modifierPatch.get(), attachments);
        }
        if (!attachments.isEmpty()) {
            set(patch, AttachmentMap.TAG_KEY, AttachmentMap.CODEC, new AttachmentMap(attachments));
        }
        return patch;
    }

    public static <T> void set(CompoundTag patch, String key, Codec<T> codec, T value) {
        codec.encodeStart(NBT_OPS, value).resultOrPartial(IronsArtifice.LOGGER::error)
                .ifPresent(encoded -> patch.put(key, encoded));
    }

    private static void applyPatch(CompoundTag patch, CompoundTag modifierPatch, Map<String, ResourceLocation> attachments) {
        for (String key : modifierPatch.getAllKeys()) {
            Tag value = modifierPatch.get(key);
            if (key.equals(AttachmentMap.TAG_KEY)) {
                AttachmentMap.CODEC.parse(NbtOps.INSTANCE, value).resultOrPartial(IronsArtifice.LOGGER::error)
                        .ifPresent(map -> attachments.putAll(map.attachments()));
                continue;
            }
            patch.put(key, value.copy());
        }
    }
}
