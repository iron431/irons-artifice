package io.redspace.irons_artifice.item;

import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.modifier.ModifierItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public record AttachmentRenderableSet(Set<Identifier> attachmentIds) {

    public static AttachmentRenderableSet fromStack(ItemStack stack) {
        GunContainer gunContainer = new GunContainer(stack);
        HashSet<Identifier> ids = new HashSet<>();
        for (var item : gunContainer) {
            if (!item.isEmpty() && item.getItem() instanceof ModifierItem modifierItem && modifierItem.attachmentRenderableId().isPresent()) {
                ids.add(modifierItem.attachmentRenderableId().get());
            }
        }
        return new AttachmentRenderableSet(ids);
    }
}
