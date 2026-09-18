package io.redspace.irons_artifice.item.kinetic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Mod-owned stand-in for vanilla's {@code minecraft:attack_range} item component. 1.21.1 reach is
 * attribute driven, so there is nowhere for a weapon to declare a reach of its own; the kinetic weapon
 * needs one, because {@link KineticWeaponHandler} marches a segment from the attacker's eye and the
 * length of that segment is the whole point of a kinetic weapon.
 * <p>
 * Same fields and same semantics as the vanilla record: the creative pair only applies to players,
 * and {@code mobFactor} scales a mob's reach so a charging mob does not get a player's.
 */
public record AttackRange(
        float minReach,
        float maxReach,
        float minCreativeReach,
        float maxCreativeReach,
        float hitboxMargin,
        float mobFactor
) {
    public static final Codec<AttackRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.floatRange(0.0F, 64.0F).optionalFieldOf("min_reach", 0.0F).forGetter(AttackRange::minReach),
            Codec.floatRange(0.0F, 64.0F).optionalFieldOf("max_reach", 3.0F).forGetter(AttackRange::maxReach),
            Codec.floatRange(0.0F, 64.0F).optionalFieldOf("min_creative_reach", 0.0F).forGetter(AttackRange::minCreativeReach),
            Codec.floatRange(0.0F, 64.0F).optionalFieldOf("max_creative_reach", 5.0F).forGetter(AttackRange::maxCreativeReach),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("hitbox_margin", 0.3F).forGetter(AttackRange::hitboxMargin),
            Codec.floatRange(0.0F, 2.0F).optionalFieldOf("mob_factor", 1.0F).forGetter(AttackRange::mobFactor)
    ).apply(instance, AttackRange::new));

    public static final StreamCodec<ByteBuf, AttackRange> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            AttackRange::minReach,
            ByteBufCodecs.FLOAT,
            AttackRange::maxReach,
            ByteBufCodecs.FLOAT,
            AttackRange::minCreativeReach,
            ByteBufCodecs.FLOAT,
            AttackRange::maxCreativeReach,
            ByteBufCodecs.FLOAT,
            AttackRange::hitboxMargin,
            ByteBufCodecs.FLOAT,
            AttackRange::mobFactor,
            AttackRange::new
    );

    /**
     * The reach for an entity that carries no {@code ENTITY_INTERACTION_RANGE} attribute. At 1.21.1
     * that attribute is registered as {@code player.entity_interaction_range} and only players are
     * built with it, where the vanilla record can read it off any {@code LivingEntity}; this is that
     * attribute's own default value.
     */
    public static final float DEFAULT_MELEE_REACH = 3.0F;

    public static AttackRange defaultFor(LivingEntity entity) {
        float interactionRange = entity.getAttributes().hasAttribute(Attributes.ENTITY_INTERACTION_RANGE)
                ? (float) entity.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)
                : DEFAULT_MELEE_REACH;
        return new AttackRange(0.0F, interactionRange, 0.0F, interactionRange, 0.0F, 1.0F);
    }

    /** The reach {@code stack} gives {@code entity}, falling back to {@link #defaultFor}. */
    public static AttackRange of(LivingEntity entity, ItemStack stack) {
        AttackRange attackRange = stack.get(DataComponentRegistry.ATTACK_RANGE);
        return attackRange != null ? attackRange : defaultFor(entity);
    }

    public float effectiveMinRange(Entity entity) {
        if (entity instanceof Player player) {
            return player.isCreative() ? this.minCreativeReach : this.minReach;
        }
        return this.minReach * this.mobFactor;
    }

    public float effectiveMaxRange(Entity entity) {
        if (entity instanceof Player player) {
            return player.isCreative() ? this.maxCreativeReach : this.maxReach;
        }
        return this.maxReach * this.mobFactor;
    }
}
