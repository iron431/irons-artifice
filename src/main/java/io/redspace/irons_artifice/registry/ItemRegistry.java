package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.gun.Guns;
import io.redspace.irons_artifice.item.BulletContainerItem;
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
import io.redspace.irons_artifice.modifier.modifiers.SoulfireCoinModifier;
import io.redspace.irons_artifice.modifier.modifiers.SeekingModifier;
import io.redspace.irons_artifice.modifier.modifiers.SingularityChargeModifier;
import io.redspace.irons_artifice.modifier.modifiers.SpiralTipModifier;
import io.redspace.irons_artifice.modifier.modifiers.SpyglassAttachmentModifier;
import io.redspace.irons_artifice.modifier.modifiers.SteelCoreModifier;
import io.redspace.irons_artifice.modifier.modifiers.SuppressorAttachmentModifier;
import io.redspace.irons_artifice.modifier.modifiers.TrickshotModifier;
import io.redspace.irons_artifice.modifier.modifiers.VenomCapsuleModifier;
import io.redspace.irons_artifice.modifier.modifiers.WindChamberModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IronsArtifice.MODID);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static final RegistryObject<GunItem> FLINTLOCK_PISTOL = ITEMS.register("flintlock",
            () -> new GunItem(new Item.Properties(), Guns.FLINTLOCK_PISTOL)
    );
    public static final RegistryObject<GunItem> MUSKET = ITEMS.register("musket",
            () -> new GunItem(new Item.Properties(), Guns.MUSKET)
    );
    public static final RegistryObject<GunItem> BLUNDERBUSS = ITEMS.register("blunderbuss",
            () -> new GunItem(new Item.Properties(), Guns.BLUNDERBUSS)
    );
    public static final RegistryObject<GunItem> BLACKPOWDER_REVOLVER = ITEMS.register("blackpowder_revolver",
            () -> new GunItem(new Item.Properties(), Guns.BLACKPOWDER_REVOLVER)
    );
    public static final RegistryObject<GunItem> SIX_SHOOTER = ITEMS.register("six_shooter",
            () -> new GunItem(new Item.Properties(), Guns.SIX_SHOOTER)
    );
    public static final RegistryObject<GunItem> ARQUEBUS = ITEMS.register("arquebus",
            () -> new GunItem(new Item.Properties(), Guns.ARQUEBUS)
    );
    public static final RegistryObject<GunItem> CLOCKWORK_RIFLE = ITEMS.register("clockwork_rifle",
            () -> new GunItem(new Item.Properties(), Guns.CLOCKWORK_RIFLE)
    );

    public static final RegistryObject<Item> COWBOY_HAT = ITEMS.register("cowboy_hat", () -> new CowboyHatItem(new Item.Properties()));
    public static final RegistryObject<Item> TRICORNE_HAT = ITEMS.register("tricorne", () -> new TricorneItem(new Item.Properties()));

    public static final RegistryObject<ModifierItem> INCENDIARY_TIP_MODIFIER = ITEMS.register(
            "incendiary_tip_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new IncendiaryTipModifier()));
    public static final RegistryObject<ModifierItem> CHAIN_LIGHTNING = ITEMS.register(
            "voltaic_core_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new ChainLightningModifier()));
    public static final RegistryObject<ModifierItem> FROZEN_JACKET = ITEMS.register(
            "frozen_jacket_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new FrozenJacketModifier()));
    public static final RegistryObject<ModifierItem> SPIRAL_TIP_MODIFIER = ITEMS.register(
            "spiral_tip_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new SpiralTipModifier()));
    public static final RegistryObject<ModifierItem> BLACKPOWDER_CHARGE = ITEMS.register(
            "blackpowder_charge_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new BlackpowderChargeModifier()));
    public static final RegistryObject<ModifierItem> CHAIN_SHOT = ITEMS.register(
            "chain_shot_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new ChainShotModifier()));
    public static final RegistryObject<ModifierItem> HOOK_SHOT_MODIFIER = ITEMS.register(
            "hook_shot_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new HookShotModifier()));
    public static final RegistryObject<ModifierItem> VENOM_CAPSULE = ITEMS.register(
            "venom_capsule_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new VenomCapsuleModifier()));
    public static final RegistryObject<ModifierItem> SCATTERSHOT = ITEMS.register(
            "scattershot_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new ScattershotModifier()));
    public static final RegistryObject<ModifierItem> BREACHING_SHELL = ITEMS.register(
            "breaching_shell_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new BreachModifier()));
    public static final RegistryObject<ModifierItem> WIND_CHAMBER = ITEMS.register(
            "wind_chamber_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new WindChamberModifier()));
    public static final RegistryObject<ModifierItem> OVERCHARGED_POWDER = ITEMS.register(
            "overcharged_powder_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new OverchargedPowderModifier()));
    public static final RegistryObject<ModifierItem> ANTIGRAVITY_MODIFIER = ITEMS.register(
            "antigravity_powder_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new AntigravityModifier()));
    public static final RegistryObject<ModifierItem> SEEKING_POWDER = ITEMS.register(
            "seeking_powder_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new SeekingModifier()));
    public static final RegistryObject<ModifierItem> SINGULARITY_CHARGE_MODIFIER = ITEMS.register(
            "singularity_charge_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new SingularityChargeModifier()));
    public static final RegistryObject<ModifierItem> ENCHANTED_BULLET_MODIFIER = ITEMS.register(
            "enchanted_bullet_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new EnchantedBulletModifier()) {
                @Override
                public boolean isFoil(ItemStack stack) {
                    return true;
                }
            });
    public static final RegistryObject<ModifierItem> TRICK_BULLET_MODIFIER = ITEMS.register(
            "trick_bullet_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new TrickshotModifier()));
    public static final RegistryObject<ModifierItem> STEEL_CORE = ITEMS.register(
            "steel_core_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new SteelCoreModifier()));
    public static final RegistryObject<ModifierItem> LEAD_CORE = ITEMS.register(
            "lead_core_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new HeavyModifier()));
    public static final RegistryObject<ModifierItem> BLOODLETTING_TIP_MODIFIER = ITEMS.register(
            "bloodletting_tip_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new LeechModifier()));
    public static final RegistryObject<ModifierItem> HAIR_TRIGGER = ITEMS.register(
            "hair_trigger_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new HairTriggerModifier()));
    public static final RegistryObject<ModifierItem> GAS_VENT = ITEMS.register(
            "gas_vent_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new GasVentModifier()));
    public static final RegistryObject<ModifierItem> GUN_OIL = ITEMS.register(
            "gun_oil_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new GunOilModifier()));
    public static final RegistryObject<ModifierItem> SOULFIRE_COIN = ITEMS.register(
            "soulfire_coin_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new SoulfireCoinModifier()));
    public static final RegistryObject<ModifierItem> BUFFER_SPRING = ITEMS.register(
            "buffer_spring_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new BufferSpringModifier()));
    public static final RegistryObject<ModifierItem> MECHANICAL_REPEATER = ITEMS.register(
            "mechanical_repeater_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new MechanicalRepeaterModifier()));
    public static final RegistryObject<ModifierItem> MECHANICAL_ACCELERATOR_MODIFIER = ITEMS.register(
            "mechanical_accelerator_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new MechanicalAccelerator()));
    public static final RegistryObject<ModifierItem> SCOPE_ATTACHMENT_MODIFIER = ITEMS.register(
            "scope_attachment_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new SpyglassAttachmentModifier()));
    public static final RegistryObject<ModifierItem> BAYONET_ATTACHMENT_MODIFIER = ITEMS.register(
            "bayonet_attachment_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new BayonetAttachmentModifier()));
    public static final RegistryObject<ModifierItem> SUPRESSOR_ATTACHMENT_MODIFIER = ITEMS.register(
            "suppressor_attachment_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new SuppressorAttachmentModifier()));

    public static final RegistryObject<Item> BULLET = ITEMS.register("bullet", () -> new Item(new Item.Properties()));
    public static final RegistryObject<BulletContainerItem> BULLET_BOX = ITEMS.register("bullet_box", () -> new BulletContainerItem(new Item.Properties(), 256));
    public static final RegistryObject<Item> BLACKPOWDER = ITEMS.register("blackpowder", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SIMPLE_MECHANICAL_COMPONENTS = ITEMS.register("simple_mechanical_components", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MECHANICAL_COMPONENTS = ITEMS.register("mechanical_components", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CLOCKWORK_COMPONENTS = ITEMS.register("clockwork_components", () -> new Item(new Item.Properties()));

    // The deferred variant holds the EntityType lookup back until after item registration.
    public static final RegistryObject<SpawnEggItem> ILLIFICER_SPAWN_EGG = ITEMS.register(
            "illificer_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.ILLIFICER, 0xFFFFFF, 0xFFFFFF, new Item.Properties())
    );
}
