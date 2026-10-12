package io.redspace.irons_artifice.item;

import com.mojang.serialization.Codec;
import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public record AttachmentMap(Map<String, ResourceLocation> attachments) {
    public static final AttachmentMap EMPTY = new AttachmentMap(Map.of());

    public static final Codec<AttachmentMap> CODEC = Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC)
            .xmap(AttachmentMap::new, AttachmentMap::attachments);

    public static final String TAG_KEY = IronsArtifice.id("attachment").toString();

    public static AttachmentMap of(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_KEY)) {
            return EMPTY;
        }
        return CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_KEY)).resultOrPartial(IronsArtifice.LOGGER::error).orElse(EMPTY);
    }

    public static void set(ItemStack stack, AttachmentMap map) {
        CODEC.encodeStart(NbtOps.INSTANCE, map).resultOrPartial(IronsArtifice.LOGGER::error)
                .ifPresent(encoded -> stack.getOrCreateTag().put(TAG_KEY, encoded));
    }

    public static void write(FriendlyByteBuf buf, AttachmentMap map) {
        buf.writeMap(map.attachments, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeResourceLocation);
    }

    public static AttachmentMap read(FriendlyByteBuf buf) {
        return new AttachmentMap(buf.readMap(HashMap::new, FriendlyByteBuf::readUtf, FriendlyByteBuf::readResourceLocation));
    }

    public AttachmentMap {
        attachments = Map.copyOf(attachments);
    }

    public boolean isEmpty() {
        return attachments.isEmpty();
    }
}
