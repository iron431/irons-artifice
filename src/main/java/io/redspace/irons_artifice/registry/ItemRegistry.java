package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.gun.ArmPoseKind;
import io.redspace.irons_artifice.gun.FireCycleCue;
import io.redspace.irons_artifice.gun.FireCycleCueStack;
import io.redspace.irons_artifice.gun.Guns;
import io.redspace.irons_artifice.gun.HandOccupancy;
import io.redspace.irons_artifice.gun.ReloadCue;
import io.redspace.irons_artifice.gun.ReloadCueStack;
import io.redspace.irons_artifice.item.AnimationAdjuster;
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
import io.redspace.irons_artifice.modifier.modifiers.FrozenJacketModifier;
import io.redspace.irons_artifice.modifier.modifiers.GasVentModifier;
import io.redspace.irons_artifice.modifier.modifiers.GunOilModifier;
import io.redspace.irons_artifice.modifier.modifiers.HairTriggerModifier;
import io.redspace.irons_artifice.modifier.modifiers.HeavyModifier;
import io.redspace.irons_artifice.modifier.modifiers.IncendiaryTipModifier;
import io.redspace.irons_artifice.modifier.modifiers.MechanicalRepeaterModifier;
import io.redspace.irons_artifice.modifier.modifiers.OverchargedPowderModifier;
import io.redspace.irons_artifice.modifier.modifiers.ScattershotModifier;
import io.redspace.irons_artifice.modifier.modifiers.SeekingModifier;
import io.redspace.irons_artifice.modifier.modifiers.SingularityChargeModifier;
import io.redspace.irons_artifice.modifier.modifiers.SpyglassModifier;
import io.redspace.irons_artifice.modifier.modifiers.SteelCoreModifier;
import io.redspace.irons_artifice.modifier.modifiers.TrickshotModifier;
import io.redspace.irons_artifice.modifier.modifiers.VenomCapsuleModifier;
import io.redspace.irons_artifice.modifier.modifiers.WindChamberModifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

public final class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IronsArtifice.MODID);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static final DeferredItem<GunItem> FLINTLOCK_PISTOL = ITEMS.registerItem("flintlock",
            properties -> new GunItem(properties.stacksTo(1), Guns.FLINTLOCK_PISTOL, IronsArtifice.id("flintlock"), ArmPoseKind.PISTOL,
                    ReloadCueStack.of(
                            new ReloadCue(0.0f / 3.0f, PlayableSound.of(SoundRegistry.SIX_SHOOTER_HOLSTER, 1.25f, 0.95f, 1.05f)),
                            new ReloadCue(0.4f / 3.0f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_INSERT_BULLET, 1.25f, 0.95f, 1.05f)),
                            new ReloadCue(1.3f / 3.0f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_PACK_BULLET, 1.25f, 0.95f, 1.05f)),
                            new ReloadCue(2.75f / 3.0f, PlayableSound.of(SoundRegistry.LEATHER_ACCENT, 1.25f, 0.85f, 0.95f)),
                            new ReloadCue(2.88f / 3.0f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 0.85f, 0.95f))
                    ), PlayableSound.of(SoundRegistry.FLINTLOCK_EQUIP, 0.75f, 0.9f, 1.1f), FireCycleCueStack.of(), AnimationAdjuster.LOWER_HAMMER,
                    Map.of("reload", HandOccupancy.BOTH)),
            properties -> properties.stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    /// ///
    public static final DeferredItem<GunItem> MUSKET = ITEMS.registerItem("musket",
            properties -> new GunItem(properties.stacksTo(1), Guns.MUSKET, IronsArtifice.id("musket"), ArmPoseKind.RIFLE, ReloadCueStack.of(
                    new ReloadCue(0.0f / 3.0f, PlayableSound.of(SoundRegistry.SIX_SHOOTER_HOLSTER, 1.25f, 0.75f, 0.85f)),
                    new ReloadCue(0.9f / 3.0f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_INSERT_BULLET, 1.25f, 0.95f, 0.85f)),
                    new ReloadCue(1.7f / 3.0f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_PACK_BULLET, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(2.88f / 3.0f, PlayableSound.of(SoundRegistry.LEATHER_ACCENT, 1.25f, 0.85f, 0.95f)),
                    new ReloadCue(2.88f / 3.0f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 0.85f, 0.95f))
            ), PlayableSound.of(SoundRegistry.MUSKET_EQUIP, 0.75f, 0.9f, 1.1f), FireCycleCueStack.of(), AnimationAdjuster.LOWER_HAMMER, Map.of()),
            properties -> properties.stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    public static final DeferredItem<GunItem> BLUNDERBUSS = ITEMS.registerItem("blunderbuss",
            properties -> new GunItem(properties.stacksTo(1), Guns.BLUNDERBUSS, IronsArtifice.id("blunderbuss"), ArmPoseKind.RIFLE, ReloadCueStack.of(
                    new ReloadCue(0.25f / 1.5f, PlayableSound.of(SoundRegistry.BLUNDERBUSS_RELOAD_OPEN, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(0.90f / 1.5f, PlayableSound.of(SoundRegistry.BLUNDERBUSS_RELOAD_LOAD, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(1.15f / 1.5f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 1.1f, 1.3f)),
                    new ReloadCue(1.27f / 1.5f, PlayableSound.of(SoundRegistry.BLUNDERBUSS_RELOAD_CLOSE, 1.25f, 0.9f, 1.1f))
            ), PlayableSound.of(SoundRegistry.BLUNDERBUSS_RELOAD_CLOSE, 0.75f, 0.9f, 1.1f), FireCycleCueStack.of(), AnimationAdjuster.DOUBLE_BARREL_HAMMER, Map.of()),
            properties -> properties.stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    public static final DeferredItem<GunItem> BLACKPOWDER_REVOLVER = ITEMS.registerItem("blackpowder_revolver",
            properties -> new GunItem(properties, Guns.BLACKPOWDER_REVOLVER, IronsArtifice.id("blackpowder_revolver"), ArmPoseKind.PISTOL, ReloadCueStack.of(
                    new ReloadCue(0f, PlayableSound.of(SoundRegistry.BLACKPOWDER_REVOLVER_RELOAD_START, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(1.33f / 2f, PlayableSound.of(SoundRegistry.BLACKPOWDER_REVOLVER_RELOAD_MID, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(1.71f / 2f, PlayableSound.of(SoundRegistry.BLACKPOWDER_REVOLVER_RELOAD_END, 1.25f, 0.95f, 1.05f))
            ), PlayableSound.of(SoundRegistry.BLACKPOWDER_REVOLVER_EQUIP, 0.75f, 0.9f, 1.1f),
                    FireCycleCueStack.of(
                            new FireCycleCue(1.0f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1f, 0.9f, 1.1f))
                    ), AnimationAdjuster.NONE, Map.of("reload", HandOccupancy.BOTH)),
            properties -> properties.stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    public static final DeferredItem<GunItem> SIX_SHOOTER = ITEMS.registerItem("six_shooter",
            properties -> new GunItem(properties.stacksTo(1), Guns.SIX_SHOOTER, IronsArtifice.id("six_shooter"), ArmPoseKind.PISTOL, ReloadCueStack.of(
                    new ReloadCue(0.1f, PlayableSound.of(SoundRegistry.SIX_SHOOTER_HOLSTER, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(0.38f / 1.25f, PlayableSound.of(SoundRegistry.SIX_SHOOTER_EQUIP, 1.25f, 0.95f, 1.05f))
            ), PlayableSound.of(SoundRegistry.SIX_SHOOTER_EQUIP, 0.75f, 0.95f, 1.05f), FireCycleCueStack.of(), null, Map.of("fire", HandOccupancy.BOTH)),
            properties -> properties.stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    public static final DeferredItem<GunItem> ARQUEBUS = ITEMS.registerItem("arquebus",
            properties -> new GunItem(properties.stacksTo(1), Guns.ARQUEBUS, IronsArtifice.id("arquebus"), ArmPoseKind.RIFLE,
                    ReloadCueStack.of(
                            new ReloadCue(0.0f / 1.5f, PlayableSound.of(SoundRegistry.ARQUEBUS_OPEN_BREECH, 1.25f, 0.95f, 1.05f)),
                            new ReloadCue(0.6f / 1.5f, PlayableSound.of(SoundRegistry.ARQUEBUS_LOAD, 1.25f, 0.9f, 1.1f)),
                            new ReloadCue(1f / 1.5f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 1f, 1.1f)),
                            new ReloadCue(1.3f / 1.5f, PlayableSound.of(SoundRegistry.ARQUEBUS_CLOSE_BREECH, 1.25f, 0.95f, 1.1f))
                    ), PlayableSound.of(SoundRegistry.ARQUEBUS_EQUIP, 0.5f, 0.9f, 1.1f), FireCycleCueStack.of(), AnimationAdjuster.LOWER_HAMMER, Map.of()),
            properties -> properties.stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    public static final DeferredItem<GunItem> CLOCKWORK_RIFLE = ITEMS.registerItem("clockwork_rifle",
            properties -> new GunItem(properties.stacksTo(1), Guns.CLOCKWORK_RIFLE, IronsArtifice.id("clockwork_rifle"), ArmPoseKind.RIFLE, ReloadCueStack.of(
                    new ReloadCue(0.38f / 1.5f, PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_EJECT_MAG, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(1.04f / 1.5f, PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_INSERT_MAG, 1.25f, 0.9f, 1.1f))
            ), PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_EQUIP, 0.75f, 0.9f, 1.1f), FireCycleCueStack.of(), AnimationAdjuster.HARMONICA_MAGAZINE, Map.of()),
            properties -> properties.stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );

    public static final DeferredItem<Item> COWBOY_HAT = ITEMS.registerItem("cowboy_hat", CowboyHatItem::new);
    public static final DeferredItem<Item> TRICORNE_HAT = ITEMS.registerItem("tricorne", TricorneItem::new);

    public static final DeferredItem<ModifierItem> INCENDIARY_TIP_MODIFIER = ITEMS.registerItem(
            "incendiary_tip_modifier", properties -> new ModifierItem(properties.stacksTo(1), new IncendiaryTipModifier()));
    public static final DeferredItem<ModifierItem> CHAIN_LIGHTNING = ITEMS.registerItem(
            "voltaic_core_modifier", properties -> new ModifierItem(properties.stacksTo(1), new ChainLightningModifier()));
    public static final DeferredItem<ModifierItem> FROZEN_JACKET = ITEMS.registerItem(
            "frozen_jacket_modifier", properties -> new ModifierItem(properties.stacksTo(1), new FrozenJacketModifier()));
    public static final DeferredItem<ModifierItem> VENOM_CAPSULE = ITEMS.registerItem(
            "venom_capsule_modifier", properties -> new ModifierItem(properties.stacksTo(1), new VenomCapsuleModifier()));
    public static final DeferredItem<ModifierItem> BLACKPOWDER_CHARGE = ITEMS.registerItem(
            "blackpowder_charge_modifier", properties -> new ModifierItem(properties.stacksTo(1), new BlackpowderChargeModifier()));
    public static final DeferredItem<ModifierItem> CHAIN_SHOT = ITEMS.registerItem(
            "chain_shot_modifier", properties -> new ModifierItem(properties.stacksTo(1), new ChainShotModifier()));
    public static final DeferredItem<ModifierItem> SCATTERSHOT = ITEMS.registerItem(
            "scattershot_modifier", properties -> new ModifierItem(properties.stacksTo(1), new ScattershotModifier()));
    public static final DeferredItem<ModifierItem> BREACHING_SHELL = ITEMS.registerItem(
            "breaching_shell_modifier", properties -> new ModifierItem(properties.stacksTo(1), new BreachModifier()));
    public static final DeferredItem<ModifierItem> OVERCHARGED_POWDER = ITEMS.registerItem(
            "overcharged_powder_modifier", properties -> new ModifierItem(properties.stacksTo(1), new OverchargedPowderModifier()));
    public static final DeferredItem<ModifierItem> ANTIGRAVITY_MODIFIER = ITEMS.registerItem(
            "antigravity_powder_modifier", properties -> new ModifierItem(properties.stacksTo(1), new AntigravityModifier()));
    public static final DeferredItem<ModifierItem> LEAD_CORE = ITEMS.registerItem(
            "lead_core_modifier", properties -> new ModifierItem(properties.stacksTo(1), new HeavyModifier()));
    public static final DeferredItem<ModifierItem> TRICK_BULLET_MODIFIER = ITEMS.registerItem(
            "trick_bullet_modifier", properties -> new ModifierItem(properties.stacksTo(1), new TrickshotModifier()));
    public static final DeferredItem<ModifierItem> SINGULARITY_CHARGE_MODIFIER = ITEMS.registerItem(
            "singularity_charge_modifier", properties -> new ModifierItem(properties.stacksTo(1), new SingularityChargeModifier()));
    public static final DeferredItem<ModifierItem> STEEL_CORE = ITEMS.registerItem(
            "steel_core_modifier", properties -> new ModifierItem(properties.stacksTo(1), new SteelCoreModifier()));
    public static final DeferredItem<ModifierItem> ENCHANTED_BULLET_MODIFIER = ITEMS.registerItem(
            "enchanted_bullet_modifier", properties -> new ModifierItem(properties.stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true), new SeekingModifier()));
    public static final DeferredItem<ModifierItem> HAIR_TRIGGER = ITEMS.registerItem(
            "hair_trigger_modifier", properties -> new ModifierItem(properties.stacksTo(1), new HairTriggerModifier()));
    public static final DeferredItem<ModifierItem> GUN_OIL = ITEMS.registerItem(
            "gun_oil_modifier", properties -> new ModifierItem(properties.stacksTo(1), new GunOilModifier()));
    public static final DeferredItem<ModifierItem> GAS_VENT = ITEMS.registerItem(
            "gas_vent_modifier", properties -> new ModifierItem(properties.stacksTo(1), new GasVentModifier()));
    public static final DeferredItem<ModifierItem> WIND_CHAMBER = ITEMS.registerItem(
            "wind_chamber_modifier", properties -> new ModifierItem(properties.stacksTo(1), new WindChamberModifier()));
    public static final DeferredItem<ModifierItem> BUFFER_SPRING = ITEMS.registerItem(
            "buffer_spring_modifier", properties -> new ModifierItem(properties.stacksTo(1), new BufferSpringModifier()));
    public static final DeferredItem<ModifierItem> MECHANICAL_REPEATER = ITEMS.registerItem(
            "mechanical_repeater_modifier", properties -> new ModifierItem(properties.stacksTo(1), new MechanicalRepeaterModifier()));
    public static final DeferredItem<ModifierItem> SPYGLASS_ATTACHMENT_MODIFIER = ITEMS.registerItem(
            "spyglass_modifier", properties -> new ModifierItem(properties.stacksTo(1), new SpyglassModifier()));

    //    public static final DeferredItem<ModifierItem> FAIRY_DUST = ITEMS.registerItem(
    //            "fairy_dust_modifier", properties -> new ModifierItem(properties.stacksTo(1), new FairyDustModifier()));

    public static final DeferredItem<Item> BULLET = ITEMS.registerSimpleItem("bullet");
    public static final DeferredItem<Item> BLACKPOWDER = ITEMS.registerSimpleItem("blackpowder");
    public static final DeferredItem<Item> SIMPLE_MECHANICAL_COMPONENTS = ITEMS.registerSimpleItem("simple_mechanical_components");
    public static final DeferredItem<Item> MECHANICAL_COMPONENTS = ITEMS.registerSimpleItem("mechanical_components");
    public static final DeferredItem<Item> CLOCKWORK_COMPONENTS = ITEMS.registerSimpleItem("clockwork_components");
}
