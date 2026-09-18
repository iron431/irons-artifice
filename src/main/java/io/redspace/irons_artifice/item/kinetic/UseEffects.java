package io.redspace.irons_artifice.item.kinetic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Mod-owned stand-in for vanilla's {@code minecraft:use_effects} item component, which arrives with
 * the spear in 26.1 and does not exist at 1.21.1. Same three fields, same defaults: at 26.1 every
 * item carries {@code UseEffects.DEFAULT}, and {@code LocalPlayer} reads
 * {@code speedMultiplier()} where 1.21.1 hardcodes {@code 0.2F} and
 * {@code isSlowDueToUsingItem()} -- {@code isUsingItem() && !canSprint()} -- where 1.21.1 writes a
 * plain {@code isUsingItem()}.
 * <p>
 * {@code interactVibrations} is carried for completeness of the record; its only 26.1 consumer is
 * {@code ShelfBlock}, which has no analogue here.
 *
 * @param canSprint        whether the holder may start or keep a sprint while using the item
 * @param interactVibrations whether using the item emits interaction vibrations
 * @param speedMultiplier  what the holder's movement impulse is scaled by while using the item
 */
public record UseEffects(boolean canSprint, boolean interactVibrations, float speedMultiplier) {

    /** What an item with no component of its own behaves like -- vanilla's {@code UseEffects.DEFAULT}. */
    public static final UseEffects DEFAULT = new UseEffects(false, true, 0.2F);

    public static final Codec<UseEffects> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("can_sprint", DEFAULT.canSprint()).forGetter(UseEffects::canSprint),
            Codec.BOOL.optionalFieldOf("interact_vibrations", DEFAULT.interactVibrations()).forGetter(UseEffects::interactVibrations),
            Codec.FLOAT.optionalFieldOf("speed_multiplier", DEFAULT.speedMultiplier()).forGetter(UseEffects::speedMultiplier)
    ).apply(instance, UseEffects::new));

    public static final StreamCodec<ByteBuf, UseEffects> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            UseEffects::canSprint,
            ByteBufCodecs.BOOL,
            UseEffects::interactVibrations,
            ByteBufCodecs.FLOAT,
            UseEffects::speedMultiplier,
            UseEffects::new
    );

    public static UseEffects of(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.USE_EFFECTS.get(), DEFAULT);
    }

    /**
     * Whether using this item costs the holder nothing at all in movement -- full speed and free to
     * sprint. 1.21.1 has one combined {@code isUsingItem()} guard covering both the impulse scale and
     * the sprint refusal, so this is the shape that guard can be answered with.
     */
    public boolean unrestricting() {
        return this.canSprint && this.speedMultiplier >= 1.0F;
    }

    /**
     * Whether {@code entity}'s current use item is {@link #unrestricting() unrestricting}. Reads the
     * entity's use item and falls back to {@link #DEFAULT} the way {@link #of} does; an entity using
     * no item is therefore never unrestricted.
     */
    public static boolean useIsUnrestricted(LivingEntity entity) {
        return UseEffects.of(entity.getUseItem()).unrestricting();
    }
}
