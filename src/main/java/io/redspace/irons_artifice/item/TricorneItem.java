package io.redspace.irons_artifice.item;

import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;

import java.util.List;
import java.util.Map;

@EventBusSubscriber
public class TricorneItem extends BaseGeoArmorItem {
    private static final int DURABILITY_MULTIPLIER = 37;

    /** Held directly, for the reason given on {@link CowboyHatItem#COWBOY_HAT_MATERIAL}. */
    public static final Holder<ArmorMaterial> TRICORNE_MATERIAL = Holder.direct(new ArmorMaterial(
            Map.of(ArmorItem.Type.HELMET, 3),
            15,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(Items.LEATHER),
            List.of(),
            0,
            0));

    public TricorneItem(Properties properties) {
        super(TRICORNE_MATERIAL, ArmorItem.Type.HELMET,
                properties.durability(ArmorItem.Type.HELMET.getDurability(DURABILITY_MULTIPLIER)));
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
        if (!event.getEntity().getItemBySlot(EquipmentSlot.HEAD).is(ItemRegistry.TRICORNE_HAT)) {
            return;
        }
        ShotProfile shotProfile = event.getShotProfile();
        if (shotProfile.magazineContents().count() != shotProfile.gun().magazineCapacity()) {
            return;
        }
        shotProfile.modifyValue(ShotComponents.DAMAGE, new ValueModifier(DAMAGE_BUFF_PERCENT, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL));
    }
}
