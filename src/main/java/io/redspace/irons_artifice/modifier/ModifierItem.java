package io.redspace.irons_artifice.modifier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;
import javax.annotation.Nonnull;


public class ModifierItem extends Item {
    private final GunModifier modifier;

    public ModifierItem(Properties properties, GunModifier modifier) {
        super(properties);
        this.modifier = modifier;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(@Nonnull ItemStack itemStack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag tooltipFlag) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("irons_artifice.tooltip.when_used_as_modifier").withStyle(ChatFormatting.GRAY));
        modifier.getDescriptionText((component) -> tooltip.add(Component.literal(" ").append(component)));
    }

    public GunModifier getModifier() {
        return modifier;
    }
}
