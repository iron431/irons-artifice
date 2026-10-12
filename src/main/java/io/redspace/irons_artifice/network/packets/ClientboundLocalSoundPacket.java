package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.data.PlayableSound;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundLocalSoundPacket(SoundSource source, PlayableSound sound) {

    public static void encode(ClientboundLocalSoundPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.source);
        PlayableSound.write(buf, msg.sound);
    }

    public static ClientboundLocalSoundPacket decode(FriendlyByteBuf buf) {
        return new ClientboundLocalSoundPacket(
                buf.readEnum(SoundSource.class),
                PlayableSound.read(buf)
        );
    }

    public static void handle(ClientboundLocalSoundPacket payload, Supplier<NetworkEvent.Context> context) {
        ClientHelper.handleLocalSound(payload);
    }
}
