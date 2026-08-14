package io.redspace.irons_artifice.modifier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ModifierItem extends Item {
    private final GunModifier modifier;

    public ModifierItem(Properties properties, GunModifier modifier) {
        super(properties);
        this.modifier = modifier;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(@NonNull ItemStack itemStack, @NonNull TooltipContext context, @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder, @NonNull TooltipFlag tooltipFlag) {
        builder.accept(Component.empty());
        builder.accept(Component.translatable("irons_artifice.tooltip.when_used_as_modifier").withStyle(ChatFormatting.GRAY));
        modifier.getDescriptionText((component) -> builder.accept(Component.literal(" ").append(component)));
    }

    public GunModifier getModifier() {
        return modifier;
    }
}
