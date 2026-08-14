package io.redspace.irons_artifice.utils;

import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ModifierPatchHandler {

    public static void rebuild(ItemStack gun, Container modifiers) {
        DataComponentPatch next = combine(modifiers);
        DataComponentPatch previous = gun.getOrDefault(DataComponentRegistry.MODIFIER_PATCH, DataComponentPatch.EMPTY);
        if (previous.equals(next)) {
            return;
        }

        Set<DataComponentType<?>> nextTypes = new HashSet<>();
        for (var entry : next.entrySet()) {
            nextTypes.add(entry.getKey());
        }
        for (var entry : previous.entrySet()) {
            DataComponentType<?> type = entry.getKey();
            if (!nextTypes.contains(type)) {
                gun.remove(type);
            }
        }

        if (next.isEmpty()) {
            gun.remove(DataComponentRegistry.MODIFIER_PATCH);
            return;
        }
        gun.applyComponents(next);
        gun.set(DataComponentRegistry.MODIFIER_PATCH, next);
    }

    public static DataComponentPatch combine(Container modifiers) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        Map<String, Identifier> attachments = new LinkedHashMap<>();
        boolean empty = true;
        for (ItemStack stack : modifiers) {
            if (stack.isEmpty() || !(stack.getItem() instanceof ModifierItem modifierItem)) {
                continue;
            }
            Optional<DataComponentPatch> patch = modifierItem.getModifier().getPatch();
            if (patch.isEmpty() || patch.get().isEmpty()) {
                continue;
            }
            empty = false;
            applyPatchToBuilder(builder, patch.get(), attachments);
        }
        if (!attachments.isEmpty()) {
            builder.set(DataComponentRegistry.ATTACHMENT.get(), new AttachmentMap(attachments));
        }
        return empty ? DataComponentPatch.EMPTY : builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <T> void applyPatchToBuilder(
            DataComponentPatch.Builder builder,
            DataComponentPatch patch,
            Map<String, Identifier> attachments
    ) {
        Set<Map.Entry<DataComponentType<T>, Optional<T>>> entries = (Set) patch.entrySet();
        entries.forEach(entry -> {
            if (entry.getValue().isEmpty()) {
                return;
            }
            if (entry.getKey() == DataComponentRegistry.ATTACHMENT.get()) {
                attachments.putAll(((AttachmentMap) entry.getValue().get()).attachments());
                return;
            }
            builder.set(entry.getKey(), entry.getValue().get());
        });
    }
}
