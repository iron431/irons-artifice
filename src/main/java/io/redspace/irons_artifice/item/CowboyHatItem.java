package io.redspace.irons_artifice.item;

import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.renderer.GeoArmorRenderer;
import com.google.common.base.Suppliers;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.armor.GenericArmorModel;
import io.redspace.irons_artifice.damage.DamageSources;
import io.redspace.irons_artifice.registry.ItemRegistry;
import io.redspace.irons_artifice.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Map;
import java.util.function.Supplier;

@EventBusSubscriber
public class CowboyHatItem extends BaseGeoItem {
    public static final ArmorMaterial COWBOY_HAT_MATERIAL = new ArmorMaterial(37, Map.of(ArmorType.HELMET, 3),
            15,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0,
            0,
            ItemTags.REPAIRS_LEATHER_ARMOR, ResourceKey.create(EquipmentAssets.ROOT_ID, IronsArtifice.id("empty")));

    public CowboyHatItem(Properties properties) {
        super(properties.humanoidArmor(COWBOY_HAT_MATERIAL, ArmorType.HELMET));
        geoRenderProvider.setValue(new GeoRenderProvider() {
            private final Supplier<GeoArmorRenderer<?, ?>> renderer =
                    Suppliers.memoize(() -> new GeoArmorRenderer<>(new GenericArmorModel<>("cowboy_hat")));

            @Override
            public @org.jspecify.annotations.Nullable GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
                return renderer.get();
            }
        });
    }

    public static final int COOLDOWN_TICKS = 100;


    @SubscribeEvent
    public static void attributeTooltip(AddAttributeTooltipsEvent event) {
        if (event.getStack().is(ItemRegistry.COWBOY_HAT)) {
            event.addTooltipLines(
                    Component.literal(" ").append(Component.translatable("item.irons_artifice.cowboy_hat.ability", (int) (COOLDOWN_TICKS / 20)))
                            .withStyle(ChatFormatting.GOLD));
        }
    }


    @SubscribeEvent
    public static void onBulletKill(LivingDeathEvent event) {
        if (!event.getSource().is(DamageSources.BULLET_DAMAGE_TYPE) || !(event.getSource().getEntity() instanceof LivingEntity livingAttacker)) {
            return;
        }
        ItemStack hat = livingAttacker.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack gun = livingAttacker.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!hat.is(ItemRegistry.COWBOY_HAT) ||
                !(gun.getItem() instanceof GunItem gunItem) ||
                !MagazineContents.has(gun) ||
                (livingAttacker instanceof Player player && player.getCooldowns().isOnCooldown(hat))) {
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
            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
            player.sendOverlayMessage(Component.translatable("item.irons_artifice.cowboy_hat.ability.gain_ammo", missing).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
