package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.menu.GunModifierMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundOpenModifierMenuPacket() implements CustomPacketPayload {
    public static final ServerboundOpenModifierMenuPacket INSTANCE = new ServerboundOpenModifierMenuPacket();

    public static final Type<ServerboundOpenModifierMenuPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(IronsArtifice.MODID, "open_gun_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpenModifierMenuPacket> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundOpenModifierMenuPacket payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack held = serverPlayer.getMainHandItem();
        if (!(held.getItem() instanceof GunItem)) {
            return;
        }
        serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inventory, player) -> new GunModifierMenu(id, inventory, new GunContainer(held)),
                Component.translatable("container.irons_artifice.gun")
        ));
    }
}
