package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.attribute.BooleanGunAttribute;
import io.redspace.irons_artifice.attribute.GunRangedAttribute;
import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class IronsArtificeAttributes {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, IronsArtifice.MODID);

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(IronsArtificeAttributes::modifyEntityAttributes);
    }

    public static final RegistryObject<GunRangedAttribute> DAMAGE = rangedAttribute(ShotComponents.DAMAGE);
    public static final RegistryObject<GunRangedAttribute> PROJECTILE_COUNT = rangedAttribute(ShotComponents.PROJECTILE_COUNT);
    public static final RegistryObject<GunRangedAttribute> SPREAD = rangedAttribute(ShotComponents.SPREAD);
    public static final RegistryObject<GunRangedAttribute> IN_AIR_PENALTY = rangedAttribute(ShotComponents.IN_AIR_PENALTY);
    public static final RegistryObject<GunRangedAttribute> FIRE_RATE = rangedAttribute(ShotComponents.FIRE_RATE);
    public static final RegistryObject<GunRangedAttribute> RELOAD_SPEED = rangedAttribute(ShotComponents.RELOAD_SPEED_MULTIPLIER);
    public static final RegistryObject<GunRangedAttribute> AMMO_CONSUME_CHANCE = rangedAttribute(ShotComponents.AMMO_CONSUME_CHANCE);
    public static final RegistryObject<GunRangedAttribute> ACCELERATING = rangedAttribute(ShotComponents.ACCELERATING);
    public static final RegistryObject<GunRangedAttribute> BULLET_SPEED = rangedAttribute(ShotComponents.BULLET_SPEED);
    public static final RegistryObject<GunRangedAttribute> GRAVITY = rangedAttribute(ShotComponents.GRAVITY);
    public static final RegistryObject<GunRangedAttribute> KNOCKBACK = rangedAttribute(ShotComponents.KNOCKBACK);
    public static final RegistryObject<GunRangedAttribute> BULLET_DRAG = rangedAttribute(ShotComponents.BULLET_DRAG);
    public static final RegistryObject<GunRangedAttribute> UNDERWATER_DRAG = rangedAttribute(ShotComponents.UNDERWATER_DRAG);
    public static final RegistryObject<GunRangedAttribute> BLOCK_DAMAGE = rangedAttribute(ShotComponents.BLOCK_DAMAGE_MULTIPLIER);
    public static final RegistryObject<GunRangedAttribute> PIERCING = rangedAttribute(ShotComponents.PIERCING);
    public static final RegistryObject<GunRangedAttribute> RICOCHET = rangedAttribute(ShotComponents.RICOCHET);
    public static final RegistryObject<GunRangedAttribute> SEEKING = rangedAttribute(ShotComponents.SEEKING);
    public static final RegistryObject<GunRangedAttribute> LEECH = rangedAttribute(ShotComponents.LEECH);
    public static final RegistryObject<GunRangedAttribute> CAMERA_RECOIL = rangedAttribute(ShotComponents.CAMERA_RECOIL_MULTIPLIER);
    public static final RegistryObject<GunRangedAttribute> CHARACTER_BLOWBACK = rangedAttribute(ShotComponents.CHARACTER_BLOWBACK);
    public static final RegistryObject<BooleanGunAttribute> FORCE_AUTO_FIRE = booleanAttribute(ShotComponents.FORCE_AUTO_FIRE);
    public static final RegistryObject<BooleanGunAttribute> BREAKS_BLOCKS = booleanAttribute(ShotComponents.BREAKS_BLOCKS);

    private static RegistryObject<GunRangedAttribute> rangedAttribute(ComponentType<Value> component) {
        return ATTRIBUTES.register(component.getName().getPath(), () -> new GunRangedAttribute(descriptionId(component), component));
    }

    private static RegistryObject<BooleanGunAttribute> booleanAttribute(ComponentType<Boolean> component) {
        return ATTRIBUTES.register(component.getName().getPath(), () -> new BooleanGunAttribute(descriptionId(component), component));
    }

    private static String descriptionId(ComponentType<?> component) {
        return "attribute." + IronsArtifice.MODID + "." + component.getName().getPath();
    }

    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(attribute -> {
            if (!event.has(entity, attribute.get())) {
                event.add(entity, attribute.get());
            }
        }));
    }
}
