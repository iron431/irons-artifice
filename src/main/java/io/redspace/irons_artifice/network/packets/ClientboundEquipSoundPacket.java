package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.client.ClientHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundEquipSoundPacket(SoundSource source, Item item) {

    public static void encode(ClientboundEquipSoundPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.source);
        buf.writeId(BuiltInRegistries.ITEM, msg.item);
    }

    public static ClientboundEquipSoundPacket decode(FriendlyByteBuf buf) {
        return new ClientboundEquipSoundPacket(
                buf.readEnum(SoundSource.class),
                buf.readById(BuiltInRegistries.ITEM)
        );
    }

    public static void handle(ClientboundEquipSoundPacket payload, Supplier<NetworkEvent.Context> context) {
        ClientHelper.handleEquipSound(payload);
    }
}
