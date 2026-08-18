package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;

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
    public Optional<DataComponentPatch> getPatch() {
        return Optional.of(DataComponentPatch.builder()
                .set(DataComponentRegistry.GUN_SPYGLASS.get(), Unit.INSTANCE)
                .set(DataComponentRegistry.ATTACHMENT.get(), new AttachmentMap(Map.of(
                        "attachment_optic", IronsArtifice.id("spyglass_scope")
                )))
                .build());
    }
}
