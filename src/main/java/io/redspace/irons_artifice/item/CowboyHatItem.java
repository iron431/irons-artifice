package io.redspace.irons_artifice.item;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.advancement.GunCriteria;
import io.redspace.irons_artifice.damage.DamageSources;
import io.redspace.irons_artifice.registry.ItemRegistry;
import io.redspace.irons_artifice.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class CowboyHatItem extends BaseGeoArmorItem {
    private static final int DURABILITY_MULTIPLIER = 37;

    public static final ArmorMaterial COWBOY_HAT_MATERIAL = new HatArmorMaterial(IronsArtifice.MODID + ":cowboy_hat",
            DURABILITY_MULTIPLIER, Map.of(ArmorItem.Type.HELMET, 3), 15);

    public CowboyHatItem(Properties properties) {
        super(COWBOY_HAT_MATERIAL, ArmorItem.Type.HELMET, properties);
    }

    public static final int COOLDOWN_TICKS = 100;


    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, tooltipComponents, tooltipFlag);
        tooltipComponents.add(
                Component.literal(" ").append(Component.translatable("item.irons_artifice.cowboy_hat.ability", (int) (COOLDOWN_TICKS / 20)))
                        .withStyle(ChatFormatting.GOLD));
    }


    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBulletKill(LivingDeathEvent event) {
        if (!event.getSource().is(DamageSources.BULLET_DAMAGE_TYPE) || !(event.getSource().getEntity() instanceof LivingEntity livingAttacker)) {
            return;
        }
        ItemStack hat = livingAttacker.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack gun = livingAttacker.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!hat.is(ItemRegistry.COWBOY_HAT.get()) ||
                !(gun.getItem() instanceof GunItem gunItem) ||
                !MagazineContents.has(gun) ||
                (livingAttacker instanceof Player player && player.getCooldowns().isOnCooldown(hat.getItem()))) {
            return;
        }
        MagazineContents contents = MagazineContents.get(gun);
        if (contents.isFull(gunItem.magazineCapacity())) {
            return;
        }
        performInstantReload(livingAttacker, gunItem, contents, gun, hat);
    }

    private static void performInstantReload(LivingEntity livingAttacker, GunItem gunItem, MagazineContents contents, ItemStack gunstack, ItemStack stack) {
        int missing = contents.missing(gunItem.magazineCapacity());
        MagazineContents.set(gunstack, contents.with(gunItem.magazineCapacity()));
        livingAttacker.level().playSound(null, livingAttacker.getX(), livingAttacker.getY(), livingAttacker.getZ(), SoundRegistry.INSTANT_RELOAD.get(), SoundSource.NEUTRAL, 1, 1);
        if (GunItem.isReloading(gunstack)) {
            ReloadState.remove(gunstack);
            GunplayManager.cancelGunAnimation(livingAttacker, gunstack);
        }
        if (livingAttacker instanceof Player player) {
            player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS);
            player.displayClientMessage(Component.translatable("item.irons_artifice.cowboy_hat.ability.gain_ammo", missing).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            if (player instanceof ServerPlayer serverPlayer) {
                GunCriteria.markInstaReload(serverPlayer);
            }
        }
    }
}
