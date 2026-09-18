package io.redspace.irons_artifice.modifier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class ModifierItem extends Item {
    private final GunModifier modifier;

    public ModifierItem(Properties properties, GunModifier modifier) {
        super(properties);
        this.modifier = modifier;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        Consumer<Component> builder = tooltipComponents::add;
        builder.accept(Component.empty());
        builder.accept(Component.translatable("irons_artifice.tooltip.when_used_as_modifier").withStyle(ChatFormatting.GRAY));
        modifier.getDescriptionText((component) -> builder.accept(Component.literal(" ").append(component)));
    }

    public GunModifier getModifier() {
        return modifier;
    }
}
