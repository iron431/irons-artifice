[<img alt="Curse Forge" src="https://cf.way2muchnoise.eu/1650193.svg?badge_style=flat"/>](https://www.curseforge.com/minecraft/mc-mods/irons-artifice)
<a href="https://discord.gg/TRzEdrndM2"><img src="https://img.shields.io/discord/1104430139275743293.svg?label=&amp;logo=discord&amp;logoColor=ffffff&amp;color=7389D8&amp;labelColor=6A7EC2&amp;style=for-the-badge" alt="" width="129" height="28" /></a>

![alt text](https://media.forgecdn.net/attachments/1867/330/iaa_title-png.png)

# Iron's Arms 'n Artifice

[Curseforge Page](https://www.curseforge.com/minecraft/mc-mods/irons-artifice)

## General

If you love the mod and would like to help support its ongoing development consider becoming a patron

<a href="https://www.patreon.com/Iron431"><img src="https://shields.io/badge/-Patreon-f86754?style=for-the-badge&amp;logo=patreon&amp;logoColor=white" alt="" width="106" height="28" /></a>
<a href="https://bmc.link/iron431"><img src="https://shields.io/badge/-Buy%20Me%20a%20Coffee-FFDD00?style=for-the-badge&amp;logo=buymeacoffee&amp;logoColor=white" alt="" width="162" height="28" /></a>

## Basic Documentation
### Guns
Guns are items, and should be registered from the `GunItem` class. `GunItem`s hold a `GunProfile`, which is built from the `GunProfile.Builder`, and defines the properties of the gun: magazine capacity, reload time, reload or fire cycle sound effects, and what the gun shoots (via `ShotComponentTemplate`).

What a gun shoots has two halves:

**Gun stats** are every number (and on/off toggle) about a shot: Damage, Spread, Fire Rate, Recoil, Piercing, etc. They are regular vanilla attributes, found in `AttributeRegistry`. Anything that can carry an attribute modifier can change them: armor, mob effects, enchantments, `/attribute`, other mods. A gun supplies its own base for a stat the same way a sword supplies attack damage: as a main hand attribute modifier on the gun item (id `GunStat.BASE_ID`), so `/give`, loot functions and default component modification all work on gun stats.
Every gun stat is attached to every living entity and synced to clients, so armor, effects, commands and other mods can change any of them. New stats are any attribute implementing `GunStat`. A number that is a property of what is bolted to the gun rather than of a shot, like the muzzle offset a Suppressor adds, is an ordinary item component (`irons_artifice:muzzle_offset`), not a stat.
Attribute defaults are neutral (0, or 1 for a multiplier). Anything that is really a property of the gun is one of its base stats, set in `ShotComponentTemplate.Builder`: the In-Air Penalty and the drag coefficients, for example, are 1 on the attribute and 1.5 / 0.98 / 0.95 on every default gun.
The id `GunStat.BASE_ID`, and ids ending in `/installed_<n>` or `/held_<n>`, are reserved for the gun in the main hand. Do not use them for your own modifiers on gun stats: resolving a shot removes them before applying the gun being fired.

**Shot Components**, held in the `ShotComponentMap`, are everything that is not a number: Muzzle Flash, Sounds, Particle Trails, On-Hit effects, the Recoil pattern. Shot components are keyed via by `ComponentType<T>`. Default types are in the `ShotComponents` class. New shot components can be created by simply creating a new key, and wiring its functionality. Keys must give a default value.

`GunplayManager#compose` resolves both into a `ShotProfile`. Read stats with `ShotProfile#value(stat)`, never from the shooter's attributes directly: the game only applies a held item's modifiers on the server, a tick late, while shots are also predicted on the client and can be fired the moment a gun is drawn.

Gun Items are automatically registered with a Geckolib renderer and model, and use the item's registered name for resource lookups (i.e. `<namespace>/geckolib/animations/item/<item_name>.animation.json`).

Standard gun animation names can be found in `GunAnimations`, standard bone names in `GunBones`, and a template geckolib model at https://github.com/iron431/irons-artifice/blob/main/src/main/resources/assets/irons_artifice/geckolib/template_gun.bbmodel

### Modifiers
Modifiers affect a gunshot. Modifiers have two halves: their item part, and their modifier functionality. The item can be registered from the `ModifierItem` class.

Number changes are data on the item: the `irons_artifice:gun_modifier_stats` component, a list of attribute modifiers in the same format as vanilla's `attribute_modifiers` (see `ModifierStats` for a builder and every default modifier). Modifiers that just change numbers need nothing else, pass `GunModifier.NONE`. An entry using the id `GunStat.BASE_ID` replaces the gun's own base for that stat instead of stacking with it. Tooltip lines are generated from the entries; an entry's `display` can hide its line or replace it with text.
Entries are not limited to gun stats. An entry for any other attribute (movement speed, armor...) is given to whoever holds the gun in their main hand, and shows on the gun's tooltip the way any held item's attributes do.

Everything else is an implementation of the `GunModifier` interface, passed into the constructor. Modifiers exhibit their functionality by modifying a bullet's shot components via `apply(ShotComponentMap components);`, called any time a gun item's shot is resolved. Every method is optional, a modifier that works purely through events overrides none of them.
Modifiers can also affect the item components of the gun item they get installed into (See the bayonet for example). Do not patch `attribute_modifiers` this way: the gun's own stats are put back regardless, but whatever else that stack carried is replaced, and removing the modifier does not restore it. Stat changes belong in `gun_modifier_stats`.

Developer's note: Modifiers are designed to be stackable without limits: no limits gun type, stack count, or interaction effects. 
For balance, a single modifier should affect how the gun feels. That being said, modifiers should not be balanced expecting to be stacked -- certainly not up to 5-7 -- lest it be *required that they stack* in order to be effective. 
When well-balanced, modifiers tend to have diminishing returns, or too high of an opportunity cost to make stacking 5-7 of a single one viable.

### Mobs
The attack goal `RangedGunAttackGoal` can be applied to any `Mob`, and will enable if they are holding a gun. Modifiers in their held gun work. The goal has basic navigation, shooting, and bayonet-charging funtionality: Mobs try to keep their distance, strafe, and then stand still for a volley of shots. 
They automatically reload, and attack with a bayonet if equipped and their target gets too close.

All hooks in `GunplayManager`, such as `attemptStartReload`, `compose`, or `tryFire` work for both players and mobs. Implement your own goals if you need!

`IGunslingerMob` is an interface that gives additional hooks that `RangedGunAttackGoal` automatically triggers on various gun-slinging events.

BY DEFAULT, MOB GUN STATS ARE NERFED. This can be controlled via `IGunslingerMob` hooks. As of 1.0.0, mobs get -25% damage, -25% bullet speed, and +3, +2, or +1 spread based on the game difficulty.
See `IGunSlingingMob#applyDefaultMobNerfs`.

### Events
Events are in the `api` package. I will not list them out because I will not maintain README. As of 1.0.0, events give basic hooks for Ammo Consumption, building a Shot's components, and actually firing a gun. 
To change a stat for a single shot, add a modifier in `ComposeShotEvent` with `ShotProfile#addModifier(stat, id, amount, operation)`. Modifiers are unique per stat and id, so give each source its own id.
Several default modifiers (and both the hats) actually use the events (instead of hard-coding their buffs into the mod). Look at them for more info!

All vanilla events still fire for bullets -- entity spawn, projectile impact, living damage, etc. If an Artifice event doesn't support what you need, think outside box, or bring it up in Discord!
