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
 * Mod-owned stand-in for vanilla's {@code minecraft:use_effects} item component, with the same three fields and
 * defaults. Nothing reads {@code interactVibrations}; it is carried to keep the record whole.
 *
 * @param canSprint        whether the holder may start or keep a sprint while using the item
 * @param interactVibrations whether using the item emits interaction vibrations
 * @param speedMultiplier  what the holder's movement impulse is scaled by while using the item
 */
public record UseEffects(boolean canSprint, boolean interactVibrations, float speedMultiplier) {

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
     * Whether using this item costs the holder nothing in movement: full speed and free to sprint. One
     * {@code isUsingItem()} guard covers both the impulse scale and the sprint refusal, so it is answered as one.
     */
    public boolean unrestricting() {
        return this.canSprint && this.speedMultiplier >= 1.0F;
    }

    /**
     * Whether {@code entity}'s use item is {@link #unrestricting() unrestricting}. Falls back to {@link #DEFAULT}
     * as {@link #of} does, so an entity using no item is never unrestricted.
     */
    public static boolean useIsUnrestricted(LivingEntity entity) {
        return UseEffects.of(entity.getUseItem()).unrestricting();
    }
}
