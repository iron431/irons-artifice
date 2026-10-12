package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import io.redspace.irons_artifice.utils.ModifierPatchHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class SpyglassAttachmentModifier extends ValueStackModifier {
    public SpyglassAttachmentModifier() {
        super(Map.of(
                ShotComponents.SPREAD, new ValueModifier(-1, ValueModifier.Operation.ADD, ValueModifier.Type.HARMFUL)
        ));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.scope").withStyle(ChatFormatting.AQUA));
        super.getDescriptionText(builder);
    }

    @Override
    public Optional<CompoundTag> getPatch() {
        CompoundTag patch = new CompoundTag();
        patch.putBoolean(GunItem.GUN_SPYGLASS_TAG_KEY, true);
        ModifierPatchHandler.set(patch, AttachmentMap.TAG_KEY, AttachmentMap.CODEC, new AttachmentMap(Map.of(
                GunBones.SOCKET_OPTIC, IronsArtifice.id("spyglass_scope")
        )));
        return Optional.of(patch);
    }
}
