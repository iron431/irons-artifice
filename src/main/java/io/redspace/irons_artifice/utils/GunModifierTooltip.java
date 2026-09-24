package io.redspace.irons_artifice.utils;

import io.redspace.irons_artifice.attribute.GunStat;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.BooleanAttribute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Renders the stat changes of a gun modifier item
 */
public final class GunModifierTooltip {

    /**
     * Sentences and toggles first, then numeric changes from most to least impactful, then base overrides
     */
    public static void appendStatLines(ItemStack modifierStack, Consumer<Component> builder) {
        ItemAttributeModifiers stats = modifierStack.getOrDefault(DataComponentRegistry.GUN_MODIFIER_STATS, ItemAttributeModifiers.EMPTY);
        List<ItemAttributeModifiers.Entry> numeric = new ArrayList<>();
        List<ItemAttributeModifiers.Entry> baseOverrides = new ArrayList<>();
        for (ItemAttributeModifiers.Entry entry : stats.modifiers()) {
            if (entry.display() instanceof ItemAttributeModifiers.Display.Hidden) {
                continue;
            }
            if (entry.display() instanceof ItemAttributeModifiers.Display.OverrideText override) {
                builder.accept(override.component());
            } else if (entry.attribute().value() instanceof BooleanAttribute) {
                builder.accept(formatToggle(entry.attribute(), entry.modifier()));
            } else if (entry.modifier().is(GunStat.BASE_ID)) {
                baseOverrides.add(entry);
            } else {
                numeric.add(entry);
            }
        }
        numeric.sort(Comparator.comparingDouble(entry -> -Math.abs(entry.modifier().amount())));
        for (ItemAttributeModifiers.Entry entry : numeric) {
            builder.accept(formatModifier(entry.attribute(), entry.modifier()));
        }
        for (ItemAttributeModifiers.Entry entry : baseOverrides) {
            builder.accept(formatBaseOverride(entry.attribute(), entry.modifier()));
        }
    }

    public static Component statName(Holder<Attribute> attribute) {
        return Component.translatable(attribute.value() instanceof GunStat stat ? stat.shortNameKey() : attribute.value().getDescriptionId());
    }

    public static Component formatModifier(Holder<Attribute> attribute, AttributeModifier modifier) {
        double value = modifier.amount();
        String identifier = value < 0 ? "minus" : "plus";
        switch (modifier.operation()) {
            case ADD_VALUE -> value = Math.abs(value);
            case ADD_MULTIPLIED_BASE -> {
                identifier += "_percent_base";
                value = Math.abs(value) * 100;
            }
            case ADD_MULTIPLIED_TOTAL -> {
                identifier += "_percent";
                value = (1 + value) * 100;
            }
        }
        return Component.translatable(String.format("irons_artifice.value_modifier.%s", identifier), Utils.DECIMAL_FORMAT.format(value), statName(attribute))
                .withColor(color(attribute, modifier.amount() >= 0));
    }

    public static Component formatToggle(Holder<Attribute> attribute, AttributeModifier modifier) {
        if (modifier.amount() > 0) {
            return statName(attribute).copy().withStyle(ChatFormatting.AQUA);
        }
        // the short name of a toggle already reads "Enables ...", so name the attribute itself
        return Component.translatable("irons_artifice.modifier.disable", Component.translatable(attribute.value().getDescriptionId())).withStyle(ChatFormatting.AQUA);
    }

    public static Component formatBaseOverride(Holder<Attribute> attribute, AttributeModifier modifier) {
        double base = attribute.value().getDefaultValue() + modifier.amount();
        return Component.translatable("irons_artifice.value.set_base", statName(attribute), Utils.DECIMAL_FORMAT.format(base)).withStyle(ChatFormatting.YELLOW);
    }

    private static int color(Holder<Attribute> attribute, boolean increase) {
        Attribute.Sentiment sentiment;
        if (attribute.value() instanceof GunStat stat) {
            sentiment = stat.sentiment();
        } else {
            // vanilla only exposes sentiment through the style it picks
            ChatFormatting style = attribute.value().getStyle(true);
            sentiment = style == ChatFormatting.BLUE ? Attribute.Sentiment.POSITIVE : style == ChatFormatting.RED ? Attribute.Sentiment.NEGATIVE : Attribute.Sentiment.NEUTRAL;
        }
        return switch (sentiment) {
            case NEUTRAL -> ChatFormatting.YELLOW.getColor();
            case POSITIVE -> (increase ? ChatFormatting.GREEN : ChatFormatting.RED).getColor();
            case NEGATIVE -> (increase ? ChatFormatting.RED : ChatFormatting.GREEN).getColor();
        };
    }
}
