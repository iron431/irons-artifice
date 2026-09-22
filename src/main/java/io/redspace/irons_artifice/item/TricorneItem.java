package io.redspace.irons_artifice.item;

import com.google.common.base.Suppliers;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.client.armor.GenericArmorModel;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@EventBusSubscriber
public class TricorneItem extends BaseGeoArmorItem {
    public static final ArmorMaterial TRICORNE_MATERIAL = new ArmorMaterial(new EnumMap<>(Map.of(Type.HELMET, 3)),
            15,
            SoundEvents.ARMOR_EQUIP_LEATHER /* already a Holder<SoundEvent> on 1.21.1 */,
            () -> Ingredient.of(Items.LEATHER),
            List.of(),
            0,
            0);

    public TricorneItem(Properties properties) {
        super(Holder.direct(TRICORNE_MATERIAL), Type.HELMET, properties);
        geoRenderProvider.setValue(new GeoRenderProvider() {
            private final Supplier<GeoArmorRenderer<TricorneItem>> renderer =
                    Suppliers.memoize(() -> new GeoArmorRenderer<>(new GenericArmorModel<>("tricorne")));

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot, @Nullable HumanoidModel<T> original) {
                return renderer.get();
            }
        });
    }

    public static final double DAMAGE_BUFF_PERCENT = 0.25;

    @SubscribeEvent
    public static void attributeTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(ItemRegistry.TRICORNE_HAT.get())) {
            event.getToolTip().add(
                    Component.literal(" ").append(Component.translatable("item.irons_artifice.tricorne.ability", (int) (DAMAGE_BUFF_PERCENT * 100)))
                            .withStyle(ChatFormatting.GOLD));
        }
    }

    @SubscribeEvent
    public static void handleTricorneAbility(ComposeShotEvent event) {
        if (!event.getEntity().getItemBySlot(EquipmentSlot.HEAD).is(ItemRegistry.TRICORNE_HAT.get())) {
            return;
        }
        ShotProfile shotProfile = event.getShotProfile();
        if (shotProfile.magazineContents().count() != shotProfile.gun().magazineCapacity()) {
            return;
        }
        shotProfile.modifyValue(ShotComponents.DAMAGE, new ValueModifier(DAMAGE_BUFF_PERCENT, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL));
    }
}
