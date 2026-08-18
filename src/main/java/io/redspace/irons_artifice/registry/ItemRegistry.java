package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.gun.Guns;
import io.redspace.irons_artifice.item.CowboyHatItem;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.TricorneItem;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.modifier.modifiers.AntigravityModifier;
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
import io.redspace.irons_artifice.modifier.modifiers.IncendiaryTipModifier;
import io.redspace.irons_artifice.modifier.modifiers.MechanicalAccelerator;
import io.redspace.irons_artifice.modifier.modifiers.MechanicalRepeaterModifier;
import io.redspace.irons_artifice.modifier.modifiers.OverchargedPowderModifier;
import io.redspace.irons_artifice.modifier.modifiers.ScattershotModifier;
import io.redspace.irons_artifice.modifier.modifiers.SeekingModifier;
import io.redspace.irons_artifice.modifier.modifiers.SingularityChargeModifier;
import io.redspace.irons_artifice.modifier.modifiers.SpyglassAttachmentModifier;
import io.redspace.irons_artifice.modifier.modifiers.SteelCoreModifier;
import io.redspace.irons_artifice.modifier.modifiers.TrickshotModifier;
import io.redspace.irons_artifice.modifier.modifiers.VenomCapsuleModifier;
import io.redspace.irons_artifice.modifier.modifiers.WindChamberModifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ItemRegistry {
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
    public static final DeferredItem<ModifierItem> BLACKPOWDER_CHARGE = ITEMS.registerItem(
            "blackpowder_charge_modifier", properties -> new ModifierItem(properties.stacksTo(1), new BlackpowderChargeModifier()));
    public static final DeferredItem<ModifierItem> CHAIN_SHOT = ITEMS.registerItem(
            "chain_shot_modifier", properties -> new ModifierItem(properties.stacksTo(1), new ChainShotModifier()));
    public static final DeferredItem<ModifierItem> VENOM_CAPSULE = ITEMS.registerItem(
            "venom_capsule_modifier", properties -> new ModifierItem(properties.stacksTo(1), new VenomCapsuleModifier()));
    public static final DeferredItem<ModifierItem> SCATTERSHOT = ITEMS.registerItem(
            "scattershot_modifier", properties -> new ModifierItem(properties.stacksTo(1), new ScattershotModifier()));
    public static final DeferredItem<ModifierItem> BREACHING_SHELL = ITEMS.registerItem(
            "breaching_shell_modifier", properties -> new ModifierItem(properties.stacksTo(1), new BreachModifier()));
    public static final DeferredItem<ModifierItem> OVERCHARGED_POWDER = ITEMS.registerItem(
            "overcharged_powder_modifier", properties -> new ModifierItem(properties.stacksTo(1), new OverchargedPowderModifier()));
    public static final DeferredItem<ModifierItem> ANTIGRAVITY_MODIFIER = ITEMS.registerItem(
            "antigravity_powder_modifier", properties -> new ModifierItem(properties.stacksTo(1), new AntigravityModifier()));
    public static final DeferredItem<ModifierItem> SEEKING_POWDER = ITEMS.registerItem(
            "seeking_powder_modifier", properties -> new ModifierItem(properties.stacksTo(1), new SeekingModifier()));
    public static final DeferredItem<ModifierItem> WIND_CHAMBER = ITEMS.registerItem(
            "wind_chamber_modifier", properties -> new ModifierItem(properties.stacksTo(1), new WindChamberModifier()));
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

    //    public static final DeferredItem<ModifierItem> FAIRY_DUST = ITEMS.registerItem(
    //            "fairy_dust_modifier", properties -> new ModifierItem(properties.stacksTo(1), new FairyDustModifier()));

    public static final DeferredItem<Item> BULLET = ITEMS.registerSimpleItem("bullet");
    public static final DeferredItem<Item> BLACKPOWDER = ITEMS.registerSimpleItem("blackpowder");
    public static final DeferredItem<Item> SIMPLE_MECHANICAL_COMPONENTS = ITEMS.registerSimpleItem("simple_mechanical_components");
    public static final DeferredItem<Item> MECHANICAL_COMPONENTS = ITEMS.registerSimpleItem("mechanical_components");
    public static final DeferredItem<Item> CLOCKWORK_COMPONENTS = ITEMS.registerSimpleItem("clockwork_components");

    public static final DeferredItem<SpawnEggItem> ILLIFICER_SPAWN_EGG = ITEMS.registerItem(
            "illificer_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(EntityRegistry.ILLIFICER.get()))
    );
}
