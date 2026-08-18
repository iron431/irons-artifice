package io.redspace.irons_artifice.client;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.network.packets.ServerboundFireGunPacket;
import io.redspace.irons_artifice.network.packets.ServerboundOpenModifierMenuPacket;
import io.redspace.irons_artifice.network.packets.ServerboundReloadGunPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
public final class InputHandler {

    private static boolean attackHeldLastTick = false;

    @SubscribeEvent
    static void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof GunItem)) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    // lowest priority means our screens open last and are on top
    // looking at you, curios
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        handleFireInput(minecraft, player);

        while (Keybinds.OPEN_MODIFIER_MENU.consumeClick()) {
            if (player.getMainHandItem().getItem() instanceof GunItem) {
                ClientPacketDistributor.sendToServer(ServerboundOpenModifierMenuPacket.INSTANCE);
            }
        }

        while (Keybinds.RELOAD.consumeClick()) {
            if (minecraft.screen == null && player.getMainHandItem().getItem() instanceof GunItem
                    && !GunItem.isReloading(player.getMainHandItem())) {
                ClientPacketDistributor.sendToServer(ServerboundReloadGunPacket.INSTANCE);
            }
        }
    }

    private static void handleFireInput(Minecraft minecraft, LocalPlayer player) {
        boolean canInput = minecraft.screen == null && minecraft.mouseHandler.isMouseGrabbed();
        boolean attackHeld = canInput && minecraft.options.keyAttack.isDown();

        if (!(player.getMainHandItem().getItem() instanceof GunItem gunItem)) {
            attackHeldLastTick = attackHeld;
            return;
        }
        ItemStack held = player.getMainHandItem();
        ShotProfile profile = GunplayManager.compose(player, gunItem.getGun(), held);

        boolean triggerPulled = switch (profile.fireMode()) {
            case SEMI -> attackHeld && !attackHeldLastTick;
            case AUTO -> attackHeld;
        };
        attackHeldLastTick = attackHeld;

        if (triggerPulled) {
            tryFire(profile, player);
        }
    }

    private static void tryFire(ShotProfile profile, LocalPlayer player) {
        if (GunplayManager.tryFire(player, player.getLookAngle())) {
            ClientPacketDistributor.sendToServer(new ServerboundFireGunPacket(player.getLookAngle()));
            RecoilManager.applyRecoil(profile);
        }
    }
}
