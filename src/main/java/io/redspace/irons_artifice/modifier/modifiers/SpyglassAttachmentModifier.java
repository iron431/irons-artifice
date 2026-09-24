package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class SpyglassAttachmentModifier implements GunModifier {
    @Override
    public void appendTooltip(Consumer<Component> builder, Runnable statLines) {
        builder.accept(Component.translatable("irons_artifice.modifier.scope").withStyle(ChatFormatting.AQUA));
        statLines.run();
    }

    @Override
    public Optional<DataComponentPatch> getPatch() {
        return Optional.of(DataComponentPatch.builder()
                .set(DataComponentRegistry.GUN_SPYGLASS.get(), Unit.INSTANCE)
                .set(DataComponentRegistry.ATTACHMENT.get(), new AttachmentMap(Map.of(
                        GunBones.SOCKET_OPTIC, IronsArtifice.id("spyglass_scope")
                )))
                .build());
    }
}
