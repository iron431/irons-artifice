### Changes
- Gun stats are now attributes. Armor, mob effects, enchantments, `/attribute` and other mods can modify them
  - Every living entity carries all of them: `gun_damage`, `fire_rate`, `fire_delay`, `reload_speed`, `bullet_spread`, `bullet_speed`, `bullet_gravity`, `bullet_knockback`, `bullet_drag`, `underwater_drag`, `piercing`, `ricochet`, `seeking`, `leech`, `accelerating`, `projectile_count`, `camera_recoil`, `character_blowback`, `in_air_penalty`, `ammo_consume_chance`, `block_damage`, `auto_fire`, `breaks_blocks`
  - A gun's own stats are main hand attribute modifiers on the gun item, the same way a sword supplies attack damage. They can be changed per-stack with the vanilla `attribute_modifiers` component
  - Existing gun and modifier numbers are unchanged
  - `fire_rate` and `reload_speed` cannot drop below 5%, so nothing can stop a gun from cycling or a reload from finishing. `projectile_count` cannot exceed 64
- A modifier item's stat changes are now data on the item (the `irons_artifice:gun_modifier_stats` component), so they can be changed with default component modification or given to any `ModifierItem` stack
  - Entries for attributes that are not gun stats (movement speed, armor...) are given to whoever holds the gun in their main hand, and show on the gun's tooltip

### API
#### BREAKING CHANGES
- Gun stats are now vanilla attributes (`AttributeRegistry`) instead of `Value` shot components
  - Removed `Value`, `ValueModifier`, `ValueStackModifier`, and every numeric or boolean constant in `ShotComponents`. `ShotComponents` now only holds non-numeric components (sounds, particles, muzzle flash, on-hit effects, recoil pattern)
  - `ShotProfile#value` takes a `Holder<Attribute>`. Booleans are read with `ShotProfile#flag`. `Value#base` reads are replaced by `ShotProfile#baseValue`
  - `ShotProfile#modifyValue` and `ShotComponentMap#modifyValue` are replaced by `ShotProfile#addModifier(stat, id, amount, operation)`. Modifiers need an id and are unique per stat and id, a second modifier with the same id replaces the first
  - `ShotProfile` holds its stats as an `AttributeMap` (`ShotProfile#stats`). `copy()` and `deepCopy()` both give the copy its own stats
  - `ShotComponentTemplate.Builder#value(type, base)` is replaced by `stat(attribute, value)`. `GunProfile.builder` takes a `ShotComponentTemplate`, which now also provides the gun's stats as `baseStats()`
  - Attribute defaults are neutral. The In-Air Penalty (1.5), Bullet Drag (0.98) and Underwater Drag (0.95) every shot used to start with are now base stats of the gun (`ShotComponentTemplate.Builder#inAirPenalty`, `#drag`, `#underwaterDrag`), the same values unless set
  - The muzzle offset is not a stat. It is the `irons_artifice:muzzle_offset` item component on the gun, which the Suppressor sets through its patch
  - The id `GunStat.BASE_ID`, and ids ending in `/installed_<n>` or `/held_<n>`, are reserved for the gun in the main hand. Resolving a shot removes them from gun stats before applying the gun being fired
- A modifier item's number changes are now the `irons_artifice:gun_modifier_stats` item component instead of code
  - `GunModifier#apply` is only for non-numeric shot components, and is now optional
  - Use `GunModifier.NONE` for modifiers that only change stats
  - Removed the modifier classes that only changed numbers: `AntigravityModifier`, `BreachModifier`, `BufferSpringModifier`, `GasVentModifier`, `GunOilModifier`, `HairTriggerModifier`, `HeavyBoltModifier`, `HeavyModifier`, `MechanicalRepeaterModifier`, `OverchargedPowderModifier`, `ScattershotModifier`, `SpiralTipModifier`, `SteelCoreModifier`, `WindChamberModifier`. Their numbers are in `ModifierStats`
  - `EnchantedBulletModifier` is now `events.AmmoRefundEvents`. It rolls the refund for anything that lowers `ammo_consume_chance`, not just the Enchanted Bullet
  - `GunModifier#appendTooltip` controls where a modifier's own text goes relative to its generated stat lines
  - A `GunModifier#getPatch` should not contain `attribute_modifiers`. The gun's own stats are put back regardless, but whatever else that stack carried is replaced, and removing the modifier does not restore it
- `Utils#formatValueModifierDescription` and `Utils#getComponentTranslate` are now `GunModifierTooltip#formatModifier` and `GunModifierTooltip#statName`, and take attributes
#### Additions
- Added `GunStat`, implemented by every gun stat attribute. Addon attributes implementing it are attached to every living entity, synced, and take part in shot resolution; use `GunStatAttribute` / `GunToggleAttribute`
- Added `GunStatResolver`, which resolves a shot's stats from the shooter and the fired gun stack
  - On the server this is exact even on the tick a gun is drawn or swapped, before the game has applied the new main hand's modifiers
  - The client predicts with the same code, but cannot know which stack the server last applied. It assumes its current main hand, and recognises what any gun contributes by id, which covers swapping from one gun to another
- Added `ModifierStats`, a builder for `gun_modifier_stats`
- Added `GunModifierTooltip`, which renders a `gun_modifier_stats` component the way modifier items do
- Added `GunContainer#forEachInstalled`, the modifier items in a gun that fit its modifier slots
- Added `ShotProfile#addModifier`, `removeModifier`, `setBase`, `setFlat`, `baseValue`, `flag`, `projectileCount`, `empty`
- Added `Bullet#canBreakBlocks`
