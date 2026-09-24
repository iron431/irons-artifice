package io.redspace.irons_artifice.attribute;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import org.jspecify.annotations.Nullable;

public class GunStatAttribute extends RangedAttribute implements GunStat {
    private final String shortNameKey;
    private Sentiment sentiment = Sentiment.POSITIVE;
    private boolean percentDisplay;

    public GunStatAttribute(String descriptionId, String shortNameKey, double defaultValue, double minValue, double maxValue) {
        super(descriptionId, defaultValue, minValue, maxValue);
        this.shortNameKey = shortNameKey;
        // the client resolves shots for prediction, so it needs whatever the server has
        setSyncable(true);
    }

    /**
     * Displays flat values as percentages, for stats where 1 means 100%
     */
    public GunStatAttribute percentDisplay() {
        this.percentDisplay = true;
        return this;
    }

    @Override
    public GunStatAttribute setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
        super.setSentiment(sentiment);
        return this;
    }

    @Override
    public Sentiment sentiment() {
        return sentiment;
    }

    @Override
    public String shortNameKey() {
        return shortNameKey;
    }

    @Override
    public Identifier getBaseId() {
        return BASE_ID;
    }

    @Override
    public MutableComponent toValueComponent(AttributeModifier.@Nullable Operation op, double value, TooltipFlag flag) {
        if (percentDisplay && IAttributeExtension.isNullOrAddition(op)) {
            return Component.translatable("neoforge.value.percent", FORMAT.format(value * 100));
        }
        return super.toValueComponent(op, value, flag);
    }
}
