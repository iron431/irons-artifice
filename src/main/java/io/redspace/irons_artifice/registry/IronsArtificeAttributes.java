package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.attribute.BooleanGunAttribute;
import io.redspace.irons_artifice.attribute.GunRangedAttribute;
import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IronsArtificeAttributes {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, IronsArtifice.MODID);

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(IronsArtificeAttributes::modifyEntityAttributes);
    }

    public static final DeferredHolder<Attribute, GunRangedAttribute> DAMAGE = rangedAttribute(ShotComponents.DAMAGE);
    public static final DeferredHolder<Attribute, GunRangedAttribute> PROJECTILE_COUNT = rangedAttribute(ShotComponents.PROJECTILE_COUNT);
    public static final DeferredHolder<Attribute, GunRangedAttribute> SPREAD = rangedAttribute(ShotComponents.SPREAD);
    public static final DeferredHolder<Attribute, GunRangedAttribute> IN_AIR_PENALTY = rangedAttribute(ShotComponents.IN_AIR_PENALTY);
    public static final DeferredHolder<Attribute, GunRangedAttribute> FIRE_RATE = rangedAttribute(ShotComponents.FIRE_RATE);
    public static final DeferredHolder<Attribute, GunRangedAttribute> RELOAD_SPEED = rangedAttribute(ShotComponents.RELOAD_SPEED_MULTIPLIER);
    public static final DeferredHolder<Attribute, GunRangedAttribute> AMMO_CONSUME_CHANCE = rangedAttribute(ShotComponents.AMMO_CONSUME_CHANCE);
    public static final DeferredHolder<Attribute, GunRangedAttribute> ACCELERATING = rangedAttribute(ShotComponents.ACCELERATING);
    public static final DeferredHolder<Attribute, GunRangedAttribute> BULLET_SPEED = rangedAttribute(ShotComponents.BULLET_SPEED);
    public static final DeferredHolder<Attribute, GunRangedAttribute> GRAVITY = rangedAttribute(ShotComponents.GRAVITY);
    public static final DeferredHolder<Attribute, GunRangedAttribute> KNOCKBACK = rangedAttribute(ShotComponents.KNOCKBACK);
    public static final DeferredHolder<Attribute, GunRangedAttribute> BULLET_DRAG = rangedAttribute(ShotComponents.BULLET_DRAG);
    public static final DeferredHolder<Attribute, GunRangedAttribute> UNDERWATER_DRAG = rangedAttribute(ShotComponents.UNDERWATER_DRAG);
    public static final DeferredHolder<Attribute, GunRangedAttribute> BLOCK_DAMAGE = rangedAttribute(ShotComponents.BLOCK_DAMAGE_MULTIPLIER);
    public static final DeferredHolder<Attribute, GunRangedAttribute> PIERCING = rangedAttribute(ShotComponents.PIERCING);
    public static final DeferredHolder<Attribute, GunRangedAttribute> RICOCHET = rangedAttribute(ShotComponents.RICOCHET);
    public static final DeferredHolder<Attribute, GunRangedAttribute> SEEKING = rangedAttribute(ShotComponents.SEEKING);
    public static final DeferredHolder<Attribute, GunRangedAttribute> LEECH = rangedAttribute(ShotComponents.LEECH);
    public static final DeferredHolder<Attribute, GunRangedAttribute> CAMERA_RECOIL = rangedAttribute(ShotComponents.CAMERA_RECOIL_MULTIPLIER);
    public static final DeferredHolder<Attribute, GunRangedAttribute> CHARACTER_BLOWBACK = rangedAttribute(ShotComponents.CHARACTER_BLOWBACK);
    public static final DeferredHolder<Attribute, BooleanGunAttribute> FORCE_AUTO_FIRE = booleanAttribute(ShotComponents.FORCE_AUTO_FIRE);
    public static final DeferredHolder<Attribute, BooleanGunAttribute> BREAKS_BLOCKS = booleanAttribute(ShotComponents.BREAKS_BLOCKS);

    private static DeferredHolder<Attribute, GunRangedAttribute> rangedAttribute(ComponentType<Value> component) {
        return ATTRIBUTES.register(component.getName().getPath(), () -> new GunRangedAttribute(descriptionId(component), component));
    }

    private static DeferredHolder<Attribute, BooleanGunAttribute> booleanAttribute(ComponentType<Boolean> component) {
        return ATTRIBUTES.register(component.getName().getPath(), () -> new BooleanGunAttribute(descriptionId(component), component));
    }

    private static String descriptionId(ComponentType<?> component) {
        return "attribute." + IronsArtifice.MODID + "." + component.getName().getPath();
    }

    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(attribute -> {
            if (!event.has(entity, attribute)) {
                event.add(entity, attribute);
            }
        }));
    }
}
