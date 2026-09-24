package io.redspace.irons_artifice.attribute;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.BooleanAttribute;

/**
 * On/off gun stat. Follows {@link BooleanAttribute}'s convention: +1 {@code ADD_VALUE} enables, -1 {@code ADD_MULTIPLIED_TOTAL} force-disables.
 */
public class GunToggleAttribute extends BooleanAttribute implements GunStat {
    private final String shortNameKey;
    private Sentiment sentiment = Sentiment.POSITIVE;

    public GunToggleAttribute(String descriptionId, String shortNameKey) {
        super(descriptionId, false);
        this.shortNameKey = shortNameKey;
        setSyncable(true);
    }

    @Override
    public GunToggleAttribute setSentiment(Sentiment sentiment) {
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

    /**
     * A gun may turn a toggle on as one of its base stats, like any other stat
     */
    @Override
    public Identifier getBaseId() {
        return BASE_ID;
    }
}
