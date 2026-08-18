package io.redspace.irons_artifice.item;

import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.renderer.GeoArmorRenderer;
import com.google.common.base.Suppliers;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.client.armor.GenericArmorModel;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@EventBusSubscriber
public class TricorneItem extends BaseGeoItem {
    public static final ArmorMaterial TRICORNE_MATERIAL = new ArmorMaterial(37, Map.of(ArmorType.HELMET, 3),
            15,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0,
            0,
            ItemTags.REPAIRS_LEATHER_ARMOR, ResourceKey.create(EquipmentAssets.ROOT_ID, IronsArtifice.id("empty")));

    public TricorneItem(Properties properties) {
        super(properties.humanoidArmor(TRICORNE_MATERIAL, ArmorType.HELMET));
        geoRenderProvider.setValue(new GeoRenderProvider() {
            private final Supplier<GeoArmorRenderer<?, ?>> renderer =
                    Suppliers.memoize(() -> new GeoArmorRenderer<>(new GenericArmorModel<>("tricorne")));

            @Override
            public @org.jspecify.annotations.Nullable GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
                return renderer.get();
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }

    public static final double DAMAGE_BUFF_PERCENT = 0.25;

    @SubscribeEvent
    public static void attributeTooltip(AddAttributeTooltipsEvent event) {
        if (event.getStack().is(ItemRegistry.TRICORNE_HAT)) {
            event.addTooltipLines(
                    Component.literal(" ").append(Component.translatable("item.irons_artifice.tricorne.ability", (int) (DAMAGE_BUFF_PERCENT * 100)))
                            .withStyle(ChatFormatting.GOLD));
        }
    }

    @SubscribeEvent
    public static void handleTricorneAbility(ComposeShotEvent event) {
        if (event.getEntity().getItemBySlot(EquipmentSlot.HEAD).is(ItemRegistry.TRICORNE_HAT)) {
            ShotProfile shotProfile = event.getShotProfile();
            if (shotProfile.magazineContents().count() == shotProfile.gun().magazineCapacity()) {
                shotProfile.get(ShotComponents.DAMAGE).addModifier(new ValueModifier(DAMAGE_BUFF_PERCENT, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL));
            }
        }
    }
}
