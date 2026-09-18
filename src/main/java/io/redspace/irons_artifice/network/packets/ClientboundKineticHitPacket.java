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
 * A kinetic charge just ran something through. Sent to everyone tracking the attacker and to the
 * attacker itself, which is the audience {@code Level.broadcastEntityEvent} reaches in 26.1 -- where
 * this is entity event 2, read by {@code LivingEntity.onKineticHit}. That byte carries no meaning of
 * its own at 1.21.1 and would need a mixin on {@code handleEntityEvent} to receive, so the same
 * signal travels as a payload.
 * <p>
 * The weapon's hit sound rides along rather than being looked up client side. 26.1 resolves it from
 * the attacker's {@code getUseItem()}, which it can do because that is the server's own stack seen
 * through a shared component map; here the receiving client knows another entity is using
 * <em>something</em> only from a synced flag, and its copy of the stack need not be the one that
 * landed the stab -- so the lookup came back empty and the sound never played. The server has the
 * component in hand when it decides to send, so it sends what to play. The ten-tick throttle stays
 * client side, where it governs the ease-down animation and the sound together.
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
     * Registered with {@code registrar.playToClient(TYPE, STREAM_CODEC, ClientboundKineticHitPacket::handle)}.
     * The body only runs on the client, so touching a client-only class here is safe: the verifier
     * loads {@link KineticHitFeedback} when this method is first invoked, which never happens on a
     * dedicated server. A mod that funnels every payload through a client-helper facade (artifice
     * did) should call that facade instead.
     */
    public static void handle(ClientboundKineticHitPacket payload, IPayloadContext context) {
        KineticHitFeedback.onKineticHit(payload.attackerId(), payload.hitSound());
    }
}
