package io.redspace.irons_artifice.item;

import com.google.common.base.Suppliers;
import io.redspace.irons_artifice.advancement.GunCriteria;
import io.redspace.irons_artifice.client.armor.GenericArmorModel;
import io.redspace.irons_artifice.damage.DamageSources;
import io.redspace.irons_artifice.registry.ItemRegistry;
import io.redspace.irons_artifice.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@EventBusSubscriber
public class CowboyHatItem extends BaseGeoArmorItem {
    public static final ArmorMaterial COWBOY_HAT_MATERIAL = new ArmorMaterial(37, new EnumMap<>(Map.of(Type.HELMET, 3)),
            15,
            Holder.direct(SoundEvents.ARMOR_EQUIP_LEATHER),
            0,
            0,
            () -> Ingredient.of(Items.LEATHER), List.of());

    public CowboyHatItem(Properties properties) {
        super(Holder.direct(COWBOY_HAT_MATERIAL), Type.HELMET, properties);
        geoRenderProvider.setValue(new GeoRenderProvider() {
            private final Supplier<GeoArmorRenderer<CowboyHatItem>> renderer =
                    Suppliers.memoize(() -> new GeoArmorRenderer<>(new GenericArmorModel<>("cowboy_hat")));

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot, @Nullable HumanoidModel<T> original) {
                return renderer.get();
            }
        });
    }

    public static final int COOLDOWN_TICKS = 100;


    @SubscribeEvent
    public static void attributeTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(ItemRegistry.COWBOY_HAT.get())) {
            event.getToolTip().add(
                    Component.literal(" ").append(Component.translatable("item.irons_artifice.cowboy_hat.ability", (int) (COOLDOWN_TICKS / 20)))
                            .withStyle(ChatFormatting.GOLD));
        }
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
