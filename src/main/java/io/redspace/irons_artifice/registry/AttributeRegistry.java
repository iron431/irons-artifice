package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.attribute.GunAttribute;
import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attribute.Sentiment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AttributeRegistry {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, IronsArtifice.MODID);

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(AttributeRegistry::modifyEntityAttributes);
    }

    // fixme: attribute sentiment doesn't have great tie to Value's Positive/Negative types.
    //  however, those currently live on the modifiers
    //  kill two birds with one stone to move value's positive/negative, but they can't (typically) live on the component... unless we do larger rework...
    public static final DeferredHolder<Attribute, GunAttribute> DAMAGE = shotAttribute(ShotComponents.DAMAGE, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> PROJECTILE_COUNT = shotAttribute(ShotComponents.PROJECTILE_COUNT, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> SPREAD = shotAttribute(ShotComponents.SPREAD, Sentiment.NEGATIVE);
    public static final DeferredHolder<Attribute, GunAttribute> IN_AIR_PENALTY = shotAttribute(ShotComponents.IN_AIR_PENALTY, Sentiment.NEGATIVE);
    public static final DeferredHolder<Attribute, GunAttribute> FIRE_RATE = shotAttribute(ShotComponents.FIRE_RATE, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> RELOAD_SPEED = shotAttribute(ShotComponents.RELOAD_SPEED_MULTIPLIER, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> AMMO_CONSUME_CHANCE = shotAttribute(ShotComponents.AMMO_CONSUME_CHANCE, Sentiment.NEGATIVE);
    public static final DeferredHolder<Attribute, GunAttribute> ACCELERATING = shotAttribute(ShotComponents.ACCELERATING, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> BULLET_SPEED = shotAttribute(ShotComponents.BULLET_SPEED, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> GRAVITY = shotAttribute(ShotComponents.GRAVITY, Sentiment.NEGATIVE);
    public static final DeferredHolder<Attribute, GunAttribute> KNOCKBACK = shotAttribute(ShotComponents.KNOCKBACK, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> BULLET_DRAG = shotAttribute(ShotComponents.BULLET_DRAG, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> UNDERWATER_DRAG = shotAttribute(ShotComponents.UNDERWATER_DRAG, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> BLOCK_DAMAGE = shotAttribute(ShotComponents.BLOCK_DAMAGE_MULTIPLIER, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> PIERCING = shotAttribute(ShotComponents.PIERCING, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> RICOCHET = shotAttribute(ShotComponents.RICOCHET, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> SEEKING = shotAttribute(ShotComponents.SEEKING, Sentiment.POSITIVE);
    public static final DeferredHolder<Attribute, GunAttribute> LEECH = shotAttribute(ShotComponents.LEECH, Sentiment.NEUTRAL);
    public static final DeferredHolder<Attribute, GunAttribute> CAMERA_RECOIL = shotAttribute(ShotComponents.CAMERA_RECOIL_MULTIPLIER, Sentiment.NEGATIVE);
    public static final DeferredHolder<Attribute, GunAttribute> CHARACTER_BLOWBACK = shotAttribute(ShotComponents.CHARACTER_BLOWBACK, Sentiment.NEUTRAL);

    private static DeferredHolder<Attribute, GunAttribute> shotAttribute(ComponentType<Value> component, Sentiment sentiment) {
        String name = component.getName().getPath();
        return ATTRIBUTES.register(name, () -> new GunAttribute("attribute." + IronsArtifice.MODID + "." + name, component, sentiment));
    }

    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(attribute -> {
            if (!event.has(entity, attribute)) {
                event.add(entity, attribute);
            }
        }));
    }
}
