package io.redspace.irons_artifice.registry;

import com.mojang.serialization.MapCodec;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.attribute.BooleanGunAttribute;
import io.redspace.irons_artifice.attribute.GunRangedAttribute;
import io.redspace.irons_artifice.client.particle.BulletTrailParticleType;
import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.client.particle.FairyDustParticleOption;
import io.redspace.irons_artifice.client.particle.FairyDustParticleType;
import io.redspace.irons_artifice.client.particle.MuzzleFlashParticleOption;
import io.redspace.irons_artifice.client.particle.MuzzleFlashParticleType;
import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.entity.ChainEntity;
import io.redspace.irons_artifice.entity.Gunslinger;
import io.redspace.irons_artifice.entity.Illificer;
import io.redspace.irons_artifice.gun.Guns;
import io.redspace.irons_artifice.item.CowboyHatItem;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.TricorneItem;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.modifier.modifiers.AntigravityModifier;
import io.redspace.irons_artifice.modifier.modifiers.BayonetAttachmentModifier;
import io.redspace.irons_artifice.modifier.modifiers.BlackpowderChargeModifier;
import io.redspace.irons_artifice.modifier.modifiers.BreachModifier;
import io.redspace.irons_artifice.modifier.modifiers.BufferSpringModifier;
import io.redspace.irons_artifice.modifier.modifiers.ChainLightningModifier;
import io.redspace.irons_artifice.modifier.modifiers.ChainShotModifier;
import io.redspace.irons_artifice.modifier.modifiers.EnchantedBulletModifier;
import io.redspace.irons_artifice.modifier.modifiers.FrozenJacketModifier;
import io.redspace.irons_artifice.modifier.modifiers.GasVentModifier;
import io.redspace.irons_artifice.modifier.modifiers.GunOilModifier;
import io.redspace.irons_artifice.modifier.modifiers.HairTriggerModifier;
import io.redspace.irons_artifice.modifier.modifiers.HeavyModifier;
import io.redspace.irons_artifice.modifier.modifiers.HookShotModifier;
import io.redspace.irons_artifice.modifier.modifiers.IncendiaryTipModifier;
import io.redspace.irons_artifice.modifier.modifiers.LeechModifier;
import io.redspace.irons_artifice.modifier.modifiers.MechanicalAccelerator;
import io.redspace.irons_artifice.modifier.modifiers.MechanicalRepeaterModifier;
import io.redspace.irons_artifice.modifier.modifiers.OverchargedPowderModifier;
import io.redspace.irons_artifice.modifier.modifiers.ScattershotModifier;
import io.redspace.irons_artifice.modifier.modifiers.SeekingModifier;
import io.redspace.irons_artifice.modifier.modifiers.SingularityChargeModifier;
import io.redspace.irons_artifice.modifier.modifiers.SpiralTipModifier;
import io.redspace.irons_artifice.modifier.modifiers.SpyglassAttachmentModifier;
import io.redspace.irons_artifice.modifier.modifiers.SteelCoreModifier;
import io.redspace.irons_artifice.modifier.modifiers.SuppressorAttachmentModifier;
import io.redspace.irons_artifice.modifier.modifiers.TrickshotModifier;
import io.redspace.irons_artifice.modifier.modifiers.VenomCapsuleModifier;
import io.redspace.irons_artifice.modifier.modifiers.WindChamberModifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IronsArtificeRegistries {
    public static final class Attributes {
        private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, IronsArtifice.MODID);

        public static void register(IEventBus modEventBus) {
            ATTRIBUTES.register(modEventBus);
            modEventBus.addListener(Attributes::modifyEntityAttributes);
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

    public static final class Entities {
        public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(IronsArtifice.MODID);

        public static final DeferredHolder<EntityType<?>, EntityType<Bullet>> BULLET = ENTITY_TYPES.registerEntityType(
                "bullet",
                Bullet::new,
                MobCategory.MISC,
                builder -> builder.sized(0.25f, 0.25f)
        );

        public static final DeferredHolder<EntityType<?>, EntityType<ChainEntity>> CHAIN = ENTITY_TYPES.registerEntityType(
                "chain",
                ChainEntity::new,
                MobCategory.MISC,
                builder -> builder.sized(0.5f, 0.5f).clientTrackingRange(64).updateInterval(1)
        );

        public static final DeferredHolder<EntityType<?>, EntityType<Gunslinger>> GUNSLINGER = ENTITY_TYPES.registerEntityType(
                "gunslinger",
                Gunslinger::new,
                MobCategory.MONSTER,
                builder -> builder.sized(0.6f, 1.95f).clientTrackingRange(64)
        );

        public static final DeferredHolder<EntityType<?>, EntityType<Illificer>> ILLIFICER = ENTITY_TYPES.registerEntityType(
                "illificer",
                Illificer::new,
                MobCategory.MONSTER,
                builder -> builder.sized(0.6f, 1.95f).clientTrackingRange(64)
        );

        public static void register(IEventBus modEventBus) {
            ENTITY_TYPES.register(modEventBus);
        }
    }

    public static final class Items {
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IronsArtifice.MODID);

        public static void register(IEventBus modEventBus) {
            ITEMS.register(modEventBus);
        }

        public static final DeferredItem<GunItem> FLINTLOCK_PISTOL = ITEMS.registerItem("flintlock",
                properties -> new GunItem(properties, Guns.FLINTLOCK_PISTOL)
        );
        public static final DeferredItem<GunItem> MUSKET = ITEMS.registerItem("musket",
                properties -> new GunItem(properties, Guns.MUSKET)
        );
        public static final DeferredItem<GunItem> BLUNDERBUSS = ITEMS.registerItem("blunderbuss",
                properties -> new GunItem(properties, Guns.BLUNDERBUSS)
        );
        public static final DeferredItem<GunItem> BLACKPOWDER_REVOLVER = ITEMS.registerItem("blackpowder_revolver",
                properties -> new GunItem(properties, Guns.BLACKPOWDER_REVOLVER)
        );
        public static final DeferredItem<GunItem> SIX_SHOOTER = ITEMS.registerItem("six_shooter",
                properties -> new GunItem(properties, Guns.SIX_SHOOTER)
        );
        public static final DeferredItem<GunItem> ARQUEBUS = ITEMS.registerItem("arquebus",
                properties -> new GunItem(properties, Guns.ARQUEBUS)
        );
        public static final DeferredItem<GunItem> CLOCKWORK_RIFLE = ITEMS.registerItem("clockwork_rifle",
                properties -> new GunItem(properties, Guns.CLOCKWORK_RIFLE)
        );

        public static final DeferredItem<Item> COWBOY_HAT = ITEMS.registerItem("cowboy_hat", CowboyHatItem::new);
        public static final DeferredItem<Item> TRICORNE_HAT = ITEMS.registerItem("tricorne", TricorneItem::new);

        public static final DeferredItem<ModifierItem> INCENDIARY_TIP_MODIFIER = ITEMS.registerItem(
                "incendiary_tip_modifier", properties -> new ModifierItem(properties.stacksTo(1), new IncendiaryTipModifier()));
        public static final DeferredItem<ModifierItem> CHAIN_LIGHTNING = ITEMS.registerItem(
                "voltaic_core_modifier", properties -> new ModifierItem(properties.stacksTo(1), new ChainLightningModifier()));
        public static final DeferredItem<ModifierItem> FROZEN_JACKET = ITEMS.registerItem(
                "frozen_jacket_modifier", properties -> new ModifierItem(properties.stacksTo(1), new FrozenJacketModifier()));
        public static final DeferredItem<ModifierItem> SPIRAL_TIP_MODIFIER = ITEMS.registerItem(
                "spiral_tip_modifier", properties -> new ModifierItem(properties.stacksTo(1), new SpiralTipModifier()));
        public static final DeferredItem<ModifierItem> BLACKPOWDER_CHARGE = ITEMS.registerItem(
                "blackpowder_charge_modifier", properties -> new ModifierItem(properties.stacksTo(1), new BlackpowderChargeModifier()));
        public static final DeferredItem<ModifierItem> CHAIN_SHOT = ITEMS.registerItem(
                "chain_shot_modifier", properties -> new ModifierItem(properties.stacksTo(1), new ChainShotModifier()));
        public static final DeferredItem<ModifierItem> HOOK_SHOT_MODIFIER = ITEMS.registerItem(
                "hook_shot_modifier", properties -> new ModifierItem(properties.stacksTo(1), new HookShotModifier()));
        public static final DeferredItem<ModifierItem> VENOM_CAPSULE = ITEMS.registerItem(
                "venom_capsule_modifier", properties -> new ModifierItem(properties.stacksTo(1), new VenomCapsuleModifier()));
        public static final DeferredItem<ModifierItem> SCATTERSHOT = ITEMS.registerItem(
                "scattershot_modifier", properties -> new ModifierItem(properties.stacksTo(1), new ScattershotModifier()));
        public static final DeferredItem<ModifierItem> BREACHING_SHELL = ITEMS.registerItem(
                "breaching_shell_modifier", properties -> new ModifierItem(properties.stacksTo(1), new BreachModifier()));
        public static final DeferredItem<ModifierItem> WIND_CHAMBER = ITEMS.registerItem(
                "wind_chamber_modifier", properties -> new ModifierItem(properties.stacksTo(1), new WindChamberModifier()));
        public static final DeferredItem<ModifierItem> OVERCHARGED_POWDER = ITEMS.registerItem(
                "overcharged_powder_modifier", properties -> new ModifierItem(properties.stacksTo(1), new OverchargedPowderModifier()));
        public static final DeferredItem<ModifierItem> ANTIGRAVITY_MODIFIER = ITEMS.registerItem(
                "antigravity_powder_modifier", properties -> new ModifierItem(properties.stacksTo(1), new AntigravityModifier()));
        public static final DeferredItem<ModifierItem> SEEKING_POWDER = ITEMS.registerItem(
                "seeking_powder_modifier", properties -> new ModifierItem(properties.stacksTo(1), new SeekingModifier()));
        public static final DeferredItem<ModifierItem> SINGULARITY_CHARGE_MODIFIER = ITEMS.registerItem(
                "singularity_charge_modifier", properties -> new ModifierItem(properties.stacksTo(1), new SingularityChargeModifier()));
        public static final DeferredItem<ModifierItem> ENCHANTED_BULLET_MODIFIER = ITEMS.registerItem(
                "enchanted_bullet_modifier", properties -> new ModifierItem(properties.stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true), new EnchantedBulletModifier()));
        public static final DeferredItem<ModifierItem> TRICK_BULLET_MODIFIER = ITEMS.registerItem(
                "trick_bullet_modifier", properties -> new ModifierItem(properties.stacksTo(1), new TrickshotModifier()));
        public static final DeferredItem<ModifierItem> STEEL_CORE = ITEMS.registerItem(
                "steel_core_modifier", properties -> new ModifierItem(properties.stacksTo(1), new SteelCoreModifier()));
        public static final DeferredItem<ModifierItem> LEAD_CORE = ITEMS.registerItem(
                "lead_core_modifier", properties -> new ModifierItem(properties.stacksTo(1), new HeavyModifier()));
        public static final DeferredItem<ModifierItem> BLOODLETTING_TIP_MODIFIER = ITEMS.registerItem(
                "bloodletting_tip_modifier", properties -> new ModifierItem(properties.stacksTo(1), new LeechModifier()));
        public static final DeferredItem<ModifierItem> HAIR_TRIGGER = ITEMS.registerItem(
                "hair_trigger_modifier", properties -> new ModifierItem(properties.stacksTo(1), new HairTriggerModifier()));
        public static final DeferredItem<ModifierItem> GAS_VENT = ITEMS.registerItem(
                "gas_vent_modifier", properties -> new ModifierItem(properties.stacksTo(1), new GasVentModifier()));
        public static final DeferredItem<ModifierItem> GUN_OIL = ITEMS.registerItem(
                "gun_oil_modifier", properties -> new ModifierItem(properties.stacksTo(1), new GunOilModifier()));
        public static final DeferredItem<ModifierItem> BUFFER_SPRING = ITEMS.registerItem(
                "buffer_spring_modifier", properties -> new ModifierItem(properties.stacksTo(1), new BufferSpringModifier()));
        public static final DeferredItem<ModifierItem> MECHANICAL_REPEATER = ITEMS.registerItem(
                "mechanical_repeater_modifier", properties -> new ModifierItem(properties.stacksTo(1), new MechanicalRepeaterModifier()));
        public static final DeferredItem<ModifierItem> MECHANICAL_ACCELERATOR_MODIFIER = ITEMS.registerItem(
                "mechanical_accelerator_modifier", properties -> new ModifierItem(properties.stacksTo(1), new MechanicalAccelerator()));
        public static final DeferredItem<ModifierItem> SCOPE_ATTACHMENT_MODIFIER = ITEMS.registerItem(
                "scope_attachment_modifier", properties -> new ModifierItem(properties.stacksTo(1), new SpyglassAttachmentModifier()));
        public static final DeferredItem<ModifierItem> BAYONET_ATTACHMENT_MODIFIER = ITEMS.registerItem(
                "bayonet_attachment_modifier", properties -> new ModifierItem(properties.stacksTo(1), new BayonetAttachmentModifier()));
        public static final DeferredItem<ModifierItem> SUPRESSOR_ATTACHMENT_MODIFIER = ITEMS.registerItem(
                "suppressor_attachment_modifier", properties -> new ModifierItem(properties.stacksTo(1), new SuppressorAttachmentModifier()));

        public static final DeferredItem<Item> BULLET = ITEMS.registerSimpleItem("bullet");
        public static final DeferredItem<Item> BLACKPOWDER = ITEMS.registerSimpleItem("blackpowder");
        public static final DeferredItem<Item> SIMPLE_MECHANICAL_COMPONENTS = ITEMS.registerSimpleItem("simple_mechanical_components");
        public static final DeferredItem<Item> MECHANICAL_COMPONENTS = ITEMS.registerSimpleItem("mechanical_components");
        public static final DeferredItem<Item> CLOCKWORK_COMPONENTS = ITEMS.registerSimpleItem("clockwork_components");

        public static final DeferredItem<SpawnEggItem> ILLIFICER_SPAWN_EGG = ITEMS.registerItem(
                "illificer_spawn_egg",
                properties -> new SpawnEggItem(properties.spawnEgg(Entities.ILLIFICER.get()))
        );
    }

    public static final class Particles {
        public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
                DeferredRegister.create(Registries.PARTICLE_TYPE, IronsArtifice.MODID);

        public static final DeferredHolder<ParticleType<?>, ParticleType<BlockParticleOption>> BLOCK_IMPACT =
                PARTICLE_TYPES.register("block_impact", () -> new ParticleType<BlockParticleOption>(false) {
                    @Override
                    public MapCodec<BlockParticleOption> codec() {
                        return BlockParticleOption.codec(this);
                    }

                    @Override
                    public StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> streamCodec() {
                        return BlockParticleOption.streamCodec(this);
                    }
                });
        public static final DeferredHolder<ParticleType<?>, ParticleType<BlockParticleOption>> BLOCK_DUST =
                PARTICLE_TYPES.register("block_dust", () -> new ParticleType<BlockParticleOption>(false) {
                    @Override
                    public MapCodec<BlockParticleOption> codec() {
                        return BlockParticleOption.codec(this);
                    }

                    @Override
                    public StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> streamCodec() {
                        return BlockParticleOption.streamCodec(this);
                    }
                });

        public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> BULLET_TRAIL =
                PARTICLE_TYPES.register("bullet_trail", () -> new BulletTrailParticleType(false));


        public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> BULLET_IMPACT =
                PARTICLE_TYPES.register("bullet_impact", () -> new BulletTrailParticleType(false));

        public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_LARGE =
                PARTICLE_TYPES.register("muzzle_flash_large", () -> new MuzzleFlashParticleType(false));
        public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_TRIANGLE =
                PARTICLE_TYPES.register("muzzle_flash_triangle", () -> new MuzzleFlashParticleType(false));
        public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_SMALL_STAR =
                PARTICLE_TYPES.register("muzzle_flash_small_star", () -> new MuzzleFlashParticleType(false));

        public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> EXPLOSION_96 =
                PARTICLE_TYPES.register("explosion", () -> new MuzzleFlashParticleType(false));

        public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> LIGHTNING_TRAIL =
                PARTICLE_TYPES.register("lightning_trail", () -> new BulletTrailParticleType(false));

        public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> FIRE_TRAIL =
                PARTICLE_TYPES.register("fire_trail", () -> new BulletTrailParticleType(false));

        public static final DeferredHolder<ParticleType<?>, ParticleType<FairyDustParticleOption>> FAIRY_DUST =
                PARTICLE_TYPES.register("fairy_dust", () -> new FairyDustParticleType(false));

        public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> SPLASH =
                PARTICLE_TYPES.register("splash", () -> new ParticleType<ColorParticleOption>(false) {
                    @Override
                    public MapCodec<ColorParticleOption> codec() {
                        return ColorParticleOption.codec(this);
                    }

                    @Override
                    public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                        return ColorParticleOption.streamCodec(this);
                    }
                });

        public static void register(IEventBus modEventBus) {
            PARTICLE_TYPES.register(modEventBus);
        }
    }

    public static final class Sounds {
        private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, IronsArtifice.MODID);

        public static void register(IEventBus eventBus) {
            SOUND_EVENTS.register(eventBus);
        }

        public static final DeferredHolder<SoundEvent, SoundEvent> BULLET_IMPACT_GENERIC = registerSoundEvent("entity.bullet.impact.generic");
        public static final DeferredHolder<SoundEvent, SoundEvent> BULLET_IMPACT_RICOCHET = registerSoundEvent("entity.bullet.impact.ricochet");
        public static final DeferredHolder<SoundEvent, SoundEvent> BULLET_ECHO_GENERIC = registerSoundEvent("entity.bullet.echo.generic");
        public static final DeferredHolder<SoundEvent, SoundEvent> BULLET_ECHO_GENERIC_PISTOL = registerSoundEvent("entity.bullet.echo.generic_pistol");
        public static final DeferredHolder<SoundEvent, SoundEvent> BULLET_ECHO_MUZZLELOADER = registerSoundEvent("entity.bullet.echo.muzzleloader");
        public static final DeferredHolder<SoundEvent, SoundEvent> REVOLVER_SHOOT = registerSoundEvent("item.example_revolver.shoot");
        public static final DeferredHolder<SoundEvent, SoundEvent> COCK_HAMMER = registerSoundEvent("item.generic.cock_hammer");
        public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTNING_ACCENT_SHOOT = registerSoundEvent("modifier.chain_lightning.shoot");
        public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTNING_ACCENT_IMPACT = registerSoundEvent("modifier.chain_lightning.impact");
        public static final DeferredHolder<SoundEvent, SoundEvent> FROZEN_JACKET_ACCENT_SHOOT = registerSoundEvent("modifier.frozen_jacket.shoot");
        public static final DeferredHolder<SoundEvent, SoundEvent> LEATHER_ACCENT = registerSoundEvent("item.generic.leather_accent");
        public static final DeferredHolder<SoundEvent, SoundEvent> INSTANT_RELOAD = registerSoundEvent("item.cowboy_hat.instant_reload");
        public static final DeferredHolder<SoundEvent, SoundEvent> INFINITY_BULLET = registerSoundEvent("modifier.enchanted_bullet.proc");
        public static final DeferredHolder<SoundEvent, SoundEvent> PIRATE_AMBUSH = registerSoundEvent("entity.drowned_pirate.ambush");

        public static final DeferredHolder<SoundEvent, SoundEvent> FLINTLOCK_SHOOT = registerSoundEvent("item.flintlock.shoot");
        public static final DeferredHolder<SoundEvent, SoundEvent> FLINTLOCK_EQUIP = registerSoundEvent("item.flintlock.equip");
        public static final DeferredHolder<SoundEvent, SoundEvent> FLINTLOCK_RELOAD_INSERT_BULLET = registerSoundEvent("item.flintlock.reload.insert_bullet");
        public static final DeferredHolder<SoundEvent, SoundEvent> FLINTLOCK_RELOAD_PACK_BULLET = registerSoundEvent("item.flintlock.reload.pack_bullet");

        public static final DeferredHolder<SoundEvent, SoundEvent> MUSKET_SHOOT = registerSoundEvent("item.musket.shoot");
        public static final DeferredHolder<SoundEvent, SoundEvent> MUSKET_EQUIP = registerSoundEvent("item.musket.equip");

        public static final DeferredHolder<SoundEvent, SoundEvent> ARQUEBUS_EQUIP = registerSoundEvent("item.arquebus.equip");
        public static final DeferredHolder<SoundEvent, SoundEvent> ARQUEBUS_OPEN_BREECH = registerSoundEvent("item.arquebus.open_breech");
        public static final DeferredHolder<SoundEvent, SoundEvent> ARQUEBUS_CLOSE_BREECH = registerSoundEvent("item.arquebus.close_breech");
        public static final DeferredHolder<SoundEvent, SoundEvent> ARQUEBUS_LOAD = registerSoundEvent("item.arquebus.load_breech");
        public static final DeferredHolder<SoundEvent, SoundEvent> ARQUEBUS_SHOOT = registerSoundEvent("item.arquebus.shoot");

        public static final DeferredHolder<SoundEvent, SoundEvent> BLACKPOWDER_REVOLVER_RELOAD_START = registerSoundEvent("item.blackpowder_revolver.reload.start");
        public static final DeferredHolder<SoundEvent, SoundEvent> BLACKPOWDER_REVOLVER_RELOAD_MID = registerSoundEvent("item.blackpowder_revolver.reload.mid");
        public static final DeferredHolder<SoundEvent, SoundEvent> BLACKPOWDER_REVOLVER_RELOAD_END = registerSoundEvent("item.blackpowder_revolver.reload.end");
        public static final DeferredHolder<SoundEvent, SoundEvent> BLACKPOWDER_REVOLVER_SHOOT = registerSoundEvent("item.blackpowder_revolver.shoot");
        public static final DeferredHolder<SoundEvent, SoundEvent> BLACKPOWDER_REVOLVER_EQUIP = registerSoundEvent("item.blackpowder_revolver.equip");

        public static final DeferredHolder<SoundEvent, SoundEvent> SIX_SHOOTER_SHOOT = registerSoundEvent("item.six_shooter.shoot");
        public static final DeferredHolder<SoundEvent, SoundEvent> SIX_SHOOTER_EQUIP = registerSoundEvent("item.six_shooter.equip");
        public static final DeferredHolder<SoundEvent, SoundEvent> SIX_SHOOTER_HOLSTER = registerSoundEvent("item.six_shooter.holster");

        public static final DeferredHolder<SoundEvent, SoundEvent> BLUNDERBUSS_RELOAD_OPEN = registerSoundEvent("item.blunderbuss.reload.break_action_open");
        public static final DeferredHolder<SoundEvent, SoundEvent> BLUNDERBUSS_RELOAD_LOAD = registerSoundEvent("item.blunderbuss.reload.mid");
        public static final DeferredHolder<SoundEvent, SoundEvent> BLUNDERBUSS_RELOAD_CLOSE = registerSoundEvent("item.blunderbuss.reload.break_action_close");
        public static final DeferredHolder<SoundEvent, SoundEvent> BLUNDERBUSS_SHOOT = registerSoundEvent("item.blunderbuss.shoot");

        public static final DeferredHolder<SoundEvent, SoundEvent> CLOCKWORK_RIFLE_INSERT_MAG = registerSoundEvent("item.clockwork_rifle.reload.insert_mag");
        public static final DeferredHolder<SoundEvent, SoundEvent> CLOCKWORK_RIFLE_EJECT_MAG = registerSoundEvent("item.clockwork_rifle.reload.eject_mag");
        public static final DeferredHolder<SoundEvent, SoundEvent> CLOCKWORK_RIFLE_SHOOT = registerSoundEvent("item.clockwork_rifle.shoot");
        public static final DeferredHolder<SoundEvent, SoundEvent> CLOCKWORK_RIFLE_EQUIP = registerSoundEvent("item.clockwork_rifle.equip");

        private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
            return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(IronsArtifice.id(name)));
        }
    }
}
