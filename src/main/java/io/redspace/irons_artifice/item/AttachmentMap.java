package io.redspace.irons_artifice.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public record AttachmentMap(Map<String, Identifier> attachments) {
    public static final AttachmentMap EMPTY = new AttachmentMap(Map.of());

    public static final Codec<AttachmentMap> CODEC = Codec.unboundedMap(Codec.STRING, Identifier.CODEC)
            .xmap(AttachmentMap::new, AttachmentMap::attachments);

    public static final StreamCodec<ByteBuf, AttachmentMap> STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new,
            ByteBufCodecs.STRING_UTF8,
            Identifier.STREAM_CODEC
    ).map(AttachmentMap::new, map -> new HashMap<>(map.attachments()));

    public AttachmentMap {
        attachments = Map.copyOf(attachments);
    }

    public boolean isEmpty() {
        return attachments.isEmpty();
    }
}
