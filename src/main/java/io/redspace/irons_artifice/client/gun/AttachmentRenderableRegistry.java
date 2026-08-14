package io.redspace.irons_artifice.client.gun;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AttachmentRenderableRegistry {
    private static final Map<Identifier, AttachmentRenderable> attachments = new HashMap<>();

    public static void register(Identifier identifier, AttachmentRenderable renderable) {
        attachments.put(identifier, renderable);
    }

    public static Optional<AttachmentRenderable> get(Identifier identifier) {
        return Optional.ofNullable(attachments.get(identifier));
    }
}
