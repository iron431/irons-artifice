package io.redspace.irons_artifice.item.kinetic;

import io.redspace.irons_artifice.registry.DataComponentRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Mod-owned stand-in for vanilla's {@code minecraft:kinetic_weapon} item component and the charge attack it
 * drives, with the same fields, order and defaults.
 */
public record KineticWeapon(
        int contactCooldownTicks,
        int delayTicks,
        Optional<KineticWeapon.Condition> dismountConditions,
        Optional<KineticWeapon.Condition> knockbackConditions,
        Optional<KineticWeapon.Condition> damageConditions,
        float forwardMovement,
        float damageMultiplier,
        Optional<Holder<SoundEvent>> sound,
        Optional<Holder<SoundEvent>> hitSound
) {
    public static final int HIT_FEEDBACK_TICKS = 10;

    public static final Codec<KineticWeapon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("contact_cooldown_ticks", 10).forGetter(KineticWeapon::contactCooldownTicks),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("delay_ticks", 0).forGetter(KineticWeapon::delayTicks),
            KineticWeapon.Condition.CODEC.optionalFieldOf("dismount_conditions").forGetter(KineticWeapon::dismountConditions),
            KineticWeapon.Condition.CODEC.optionalFieldOf("knockback_conditions").forGetter(KineticWeapon::knockbackConditions),
            KineticWeapon.Condition.CODEC.optionalFieldOf("damage_conditions").forGetter(KineticWeapon::damageConditions),
            Codec.FLOAT.optionalFieldOf("forward_movement", 0.0F).forGetter(KineticWeapon::forwardMovement),
            Codec.FLOAT.optionalFieldOf("damage_multiplier", 1.0F).forGetter(KineticWeapon::damageMultiplier),
            SoundEvent.CODEC.optionalFieldOf("sound").forGetter(KineticWeapon::sound),
            SoundEvent.CODEC.optionalFieldOf("hit_sound").forGetter(KineticWeapon::hitSound)
    ).apply(instance, KineticWeapon::new));

    private static final StreamCodec<ByteBuf, Optional<KineticWeapon.Condition>> OPTIONAL_CONDITION_STREAM_CODEC =
            KineticWeapon.Condition.STREAM_CODEC.apply(ByteBufCodecs::optional);
    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<Holder<SoundEvent>>> OPTIONAL_SOUND_STREAM_CODEC =
            SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional);

    /** Written by hand because {@link StreamCodec#composite} reaches only six components. */
    public static final StreamCodec<RegistryFriendlyByteBuf, KineticWeapon> STREAM_CODEC = StreamCodec.of(
            (buffer, kineticWeapon) -> {
                ByteBufCodecs.VAR_INT.encode(buffer, kineticWeapon.contactCooldownTicks());
                ByteBufCodecs.VAR_INT.encode(buffer, kineticWeapon.delayTicks());
                OPTIONAL_CONDITION_STREAM_CODEC.encode(buffer, kineticWeapon.dismountConditions());
                OPTIONAL_CONDITION_STREAM_CODEC.encode(buffer, kineticWeapon.knockbackConditions());
                OPTIONAL_CONDITION_STREAM_CODEC.encode(buffer, kineticWeapon.damageConditions());
                ByteBufCodecs.FLOAT.encode(buffer, kineticWeapon.forwardMovement());
                ByteBufCodecs.FLOAT.encode(buffer, kineticWeapon.damageMultiplier());
                OPTIONAL_SOUND_STREAM_CODEC.encode(buffer, kineticWeapon.sound());
                OPTIONAL_SOUND_STREAM_CODEC.encode(buffer, kineticWeapon.hitSound());
            },
            buffer -> {
                int contactCooldownTicks = ByteBufCodecs.VAR_INT.decode(buffer);
                int delayTicks = ByteBufCodecs.VAR_INT.decode(buffer);
                Optional<KineticWeapon.Condition> dismountConditions = OPTIONAL_CONDITION_STREAM_CODEC.decode(buffer);
                Optional<KineticWeapon.Condition> knockbackConditions = OPTIONAL_CONDITION_STREAM_CODEC.decode(buffer);
                Optional<KineticWeapon.Condition> damageConditions = OPTIONAL_CONDITION_STREAM_CODEC.decode(buffer);
                float forwardMovement = ByteBufCodecs.FLOAT.decode(buffer);
                float damageMultiplier = ByteBufCodecs.FLOAT.decode(buffer);
                Optional<Holder<SoundEvent>> sound = OPTIONAL_SOUND_STREAM_CODEC.decode(buffer);
                Optional<Holder<SoundEvent>> hitSound = OPTIONAL_SOUND_STREAM_CODEC.decode(buffer);
                return new KineticWeapon(contactCooldownTicks, delayTicks, dismountConditions, knockbackConditions,
                        damageConditions, forwardMovement, damageMultiplier, sound, hitSound);
            }
    );

    public static @Nullable KineticWeapon get(ItemStack stack) {
        return stack.get(DataComponentRegistry.KINETIC_WEAPON);
    }

    public static void set(ItemStack stack, KineticWeapon kineticWeapon) {
        stack.set(DataComponentRegistry.KINETIC_WEAPON, kineticWeapon);
    }

    public static boolean has(ItemStack stack) {
        return stack.has(DataComponentRegistry.KINETIC_WEAPON);
    }

    public int computeDamageUseDuration() {
        return this.delayTicks + this.damageConditions.map(KineticWeapon.Condition::maxDurationTicks).orElse(0);
    }

    public record Condition(int maxDurationTicks, float minSpeed, float minRelativeSpeed) {
        public static final Codec<KineticWeapon.Condition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_duration_ticks").forGetter(KineticWeapon.Condition::maxDurationTicks),
                Codec.FLOAT.optionalFieldOf("min_speed", 0.0F).forGetter(KineticWeapon.Condition::minSpeed),
                Codec.FLOAT.optionalFieldOf("min_relative_speed", 0.0F).forGetter(KineticWeapon.Condition::minRelativeSpeed)
        ).apply(instance, KineticWeapon.Condition::new));

        public static final StreamCodec<ByteBuf, KineticWeapon.Condition> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                KineticWeapon.Condition::maxDurationTicks,
                ByteBufCodecs.FLOAT,
                KineticWeapon.Condition::minSpeed,
                ByteBufCodecs.FLOAT,
                KineticWeapon.Condition::minRelativeSpeed,
                KineticWeapon.Condition::new
        );

        public boolean test(int ticksUsed, double attackerSpeed, double relativeSpeed, double entityFactor) {
            return ticksUsed <= this.maxDurationTicks && attackerSpeed >= this.minSpeed * entityFactor && relativeSpeed >= this.minRelativeSpeed * entityFactor;
        }

        public static Optional<KineticWeapon.Condition> ofAttackerSpeed(int untilTicks, float minAttackerSpeed) {
            return Optional.of(new KineticWeapon.Condition(untilTicks, minAttackerSpeed, 0.0F));
        }

        public static Optional<KineticWeapon.Condition> ofRelativeSpeed(int untilTicks, float minRelativeSpeed) {
            return Optional.of(new KineticWeapon.Condition(untilTicks, 0.0F, minRelativeSpeed));
        }
    }
}
