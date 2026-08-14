package io.redspace.irons_artifice.client.gun;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AttachmentRenderableRegistry {
    private static final Map<Identifier, AttachmentGeoRenderer> attachments = new HashMap<>();

    public static void register(Identifier identifier, AttachmentGeoRenderer renderer) {
        attachments.put(identifier, renderer);
    }

    public static Optional<AttachmentGeoRenderer> get(Identifier identifier) {
        return Optional.ofNullable(attachments.get(identifier));
    }
}
