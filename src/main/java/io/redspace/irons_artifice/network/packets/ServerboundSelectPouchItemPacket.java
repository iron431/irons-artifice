package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.BulletPouchContents;
import io.redspace.irons_artifice.item.BulletPouchItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSelectPouchItemPacket(int slotIndex, int selectedIndex) implements CustomPacketPayload {
    public static final Type<ServerboundSelectPouchItemPacket> TYPE = new Type<>(IronsArtifice.id("select_pouch_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSelectPouchItemPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ServerboundSelectPouchItemPacket::slotIndex,
            ByteBufCodecs.VAR_INT, ServerboundSelectPouchItemPacket::selectedIndex,
            ServerboundSelectPouchItemPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundSelectPouchItemPacket payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (payload.slotIndex < 0 || payload.slotIndex >= menu.slots.size()) {
            return;
        }
        ItemStack stack = menu.getSlot(payload.slotIndex).getItem();
        if (!(stack.getItem() instanceof BulletPouchItem)) {
            return;
        }
        int shown = BulletPouchItem.numberOfStacksToShow(stack);
        boolean clearing = payload.selectedIndex == BulletPouchContents.NO_SELECTION;
        boolean inRange = payload.selectedIndex >= 0 && payload.selectedIndex < shown;
        if (!clearing && !inRange) {
            return;
        }
        BulletPouchItem.toggleSelected(stack, payload.selectedIndex);
    }
}
