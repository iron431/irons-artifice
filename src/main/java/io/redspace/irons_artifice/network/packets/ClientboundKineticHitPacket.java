package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.KineticHitFeedback;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

/**
 * A kinetic charge just ran something through. Sent to everyone tracking the attacker and to the attacker itself.
 * <p>
 * The weapon's hit sound rides along rather than being looked up client side: the receiving client knows another
 * entity is using <em>something</em> only from a synced flag, and its copy of the stack need not be the one that
 * landed the stab, so the lookup came back empty and the sound never played. The server has the component in hand
 * when it decides to send. The throttle stays client side, where it governs the animation and the sound together.
 *
 * @param attackerId the charging entity
 * @param hitSound   the weapon's hit sound, absent if it declares none
 */
public record ClientboundKineticHitPacket(int attackerId, Optional<Holder<SoundEvent>> hitSound) implements CustomPacketPayload {

    public static final Type<ClientboundKineticHitPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(IronsArtifice.MODID, "kinetic_hit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundKineticHitPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ClientboundKineticHitPacket::attackerId,
                    SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional),
                    ClientboundKineticHitPacket::hitSound,
                    ClientboundKineticHitPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * The body runs only on the client, so naming a client-only class here is safe: the verifier loads
     * {@link KineticHitFeedback} when this method is first invoked, which never happens on a dedicated server.
     */
    public static void handle(ClientboundKineticHitPacket payload, IPayloadContext context) {
        KineticHitFeedback.onKineticHit(payload.attackerId(), payload.hitSound());
    }
}
