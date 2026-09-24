package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.attribute.GunStat;
import io.redspace.irons_artifice.attribute.GunStatAttribute;
import io.redspace.irons_artifice.attribute.GunToggleAttribute;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attribute.Sentiment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Gun stats, carried by every living entity. A stat a gun always defines for itself gets its real value from the gun's
 * {@link GunStat#BASE_ID} modifier, the way a sword supplies attack damage. Defaults are neutral values only: once an entity has used a
 * stat its base is saved with it, so a balance number placed here could never be changed for that entity again.
 * <p>
 * Ranges are wide on purpose: they only exist to keep values finite, not to balance anything.
 */
public final class AttributeRegistry {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, IronsArtifice.MODID);

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
    }

    public static final Holder<Attribute> GUN_DAMAGE = stat("gun_damage", "damage", 0, 0, 1_000_000, Sentiment.POSITIVE, false);
    /**
     * Floored like {@link #RELOAD_SPEED}: at zero a gun would never finish cycling, and the delay is fixed when the shot is fired
     */
    public static final Holder<Attribute> FIRE_RATE = stat("fire_rate", "fire_rate", 1, 0.05, 1024, Sentiment.POSITIVE, true);
    /**
     * Floored because a reload keeps the speed it started with: at zero it would never finish, even once whatever caused it is gone
     */
    public static final Holder<Attribute> RELOAD_SPEED = stat("reload_speed", "reload_speed_multiplier", 1, 0.05, 1024, Sentiment.POSITIVE, true);
    /**
     * May go negative: a negative spread offsets movement and bayonet penalties before the final spread is floored at zero
     */
    public static final Holder<Attribute> BULLET_SPREAD = stat("bullet_spread", "spread", 0, -1024, 1024, Sentiment.NEGATIVE, false);
    public static final Holder<Attribute> BULLET_SPEED = stat("bullet_speed", "bullet_speed", 0, 0, 1024, Sentiment.POSITIVE, false);
    public static final Holder<Attribute> BULLET_GRAVITY = stat("bullet_gravity", "gravity", 0, -16, 16, Sentiment.NEGATIVE, false);
    public static final Holder<Attribute> BULLET_KNOCKBACK = stat("bullet_knockback", "knockback", 0, 0, 1024, Sentiment.POSITIVE, false);
    public static final Holder<Attribute> PIERCING = stat("piercing", "piercing", 0, 0, 1024, Sentiment.POSITIVE, false);
    public static final Holder<Attribute> RICOCHET = stat("ricochet", "ricochet", 0, 0, 1024, Sentiment.POSITIVE, false);
    /**
     * Damage is split between a shot's bullets, so the cap costs nothing but entities
     */
    public static final Holder<Attribute> PROJECTILE_COUNT = stat("projectile_count", "projectile_count", 0, 0, 64, Sentiment.POSITIVE, false);
    public static final Holder<Attribute> CAMERA_RECOIL = stat("camera_recoil", "camera_recoil_multiplier", 1, 0, 1024, Sentiment.NEGATIVE, true);
    /**
     * Spread multiplier while airborne. Neutral here; how badly a gun handles in the air is the gun's to say
     */
    public static final Holder<Attribute> IN_AIR_PENALTY = stat("in_air_penalty", "in_air_penalty", 1, 0, 1024, Sentiment.NEGATIVE, true);
    public static final Holder<Attribute> AMMO_CONSUME_CHANCE = stat("ammo_consume_chance", "ammo_consume_chance", 1, 0, 1024, Sentiment.NEGATIVE, true);
    public static final Holder<Attribute> BLOCK_DAMAGE = stat("block_damage", "block_damage_multiplier", 1, 0, 1024, Sentiment.POSITIVE, true);
    public static final Holder<Attribute> AUTO_FIRE = toggle("auto_fire", "force_auto_fire");
    public static final Holder<Attribute> BREAKS_BLOCKS = toggle("breaks_blocks", "enables_block_damage");
    public static final Holder<Attribute> FIRE_DELAY = stat("fire_delay", "fire_delay", 0, 0, 72000, Sentiment.NEGATIVE, false);
    public static final Holder<Attribute> CHARACTER_BLOWBACK = stat("character_blowback", "character_blowback", 0, 0, 1024, Sentiment.NEUTRAL, false);
    /**
     * Velocity kept per tick. Neutral here, like {@link #IN_AIR_PENALTY}: every gun supplies its own
     */
    public static final Holder<Attribute> BULLET_DRAG = stat("bullet_drag", "bullet_drag", 1, 0, 1024, Sentiment.POSITIVE, false);
    /**
     * Velocity kept per tick underwater, on top of {@link #BULLET_DRAG}. Anything from 1 up means no extra drag
     */
    public static final Holder<Attribute> UNDERWATER_DRAG = stat("underwater_drag", "underwater_drag", 1, 0, 1024, Sentiment.POSITIVE, false);
    public static final Holder<Attribute> ACCELERATING = stat("accelerating", "accelerating", 0, 0, 1024, Sentiment.POSITIVE, false);
    public static final Holder<Attribute> LEECH = stat("leech", "leech", 0, 0, 1024, Sentiment.NEUTRAL, false);
    public static final Holder<Attribute> SEEKING = stat("seeking", "seeking", 0, 0, 1024, Sentiment.POSITIVE, false);

    /**
     * Every registered gun stat, including ones registered by other mods. Only complete once attributes are registered
     */
    public static List<Holder<Attribute>> gunStats() {
        return BuiltInRegistries.ATTRIBUTE.listElements()
                .filter(holder -> holder.value() instanceof GunStat)
                .<Holder<Attribute>>map(holder -> holder)
                .toList();
    }

    private static Holder<Attribute> stat(String name, String shortName, double defaultValue, double min, double max, Sentiment sentiment, boolean percent) {
        return ATTRIBUTES.register(name, () -> {
            GunStatAttribute attribute = new GunStatAttribute(descriptionId(name), shortNameKey(shortName), defaultValue, min, max);
            attribute.setSentiment(sentiment);
            if (percent) {
                attribute.percentDisplay();
            }
            return attribute;
        });
    }

    private static Holder<Attribute> toggle(String name, String shortName) {
        return ATTRIBUTES.register(name, () -> new GunToggleAttribute(descriptionId(name), shortNameKey(shortName)));
    }

    private static String descriptionId(String name) {
        return "attribute." + IronsArtifice.MODID + "." + name;
    }

    private static String shortNameKey(String shortName) {
        return IronsArtifice.MODID + ".component_type." + shortName;
    }
}
