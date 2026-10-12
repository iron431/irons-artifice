package io.redspace.irons_artifice.item;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class TricorneItem extends BaseGeoArmorItem {
    private static final int DURABILITY_MULTIPLIER = 37;

    public static final ArmorMaterial TRICORNE_MATERIAL = new HatArmorMaterial(IronsArtifice.MODID + ":tricorne",
            DURABILITY_MULTIPLIER, Map.of(ArmorItem.Type.HELMET, 3), 15);

    public TricorneItem(Properties properties) {
        super(TRICORNE_MATERIAL, ArmorItem.Type.HELMET, properties);
    }

    public static final double DAMAGE_BUFF_PERCENT = 0.25;

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, tooltipComponents, tooltipFlag);
        tooltipComponents.add(
                Component.literal(" ").append(Component.translatable("item.irons_artifice.tricorne.ability", (int) (DAMAGE_BUFF_PERCENT * 100)))
                        .withStyle(ChatFormatting.GOLD));
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
