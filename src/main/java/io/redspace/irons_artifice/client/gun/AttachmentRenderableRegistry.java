package io.redspace.irons_artifice.client.gun;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AttachmentRenderableRegistry {
    private static final Map<ResourceLocation, AttachmentGeoRenderer> attachments = new HashMap<>();

    public static void register(ResourceLocation identifier, AttachmentGeoRenderer renderer) {
        attachments.put(identifier, renderer);
    }

    public static Optional<AttachmentGeoRenderer> get(ResourceLocation identifier) {
        return Optional.ofNullable(attachments.get(identifier));
    }
}
