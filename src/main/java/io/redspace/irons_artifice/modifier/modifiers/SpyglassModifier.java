package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Unit;

import java.util.Map;
import java.util.Optional;

public class SpyglassModifier implements GunModifier {
    @Override
    public void apply(ShotComponentMap components) {
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
