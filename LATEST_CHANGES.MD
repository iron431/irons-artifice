### Additions
- Added gun attributes. Armor, items, potion effects and enchantments, from datapacks or other mods, can now modify gun stats such as damage, fire rate, spread, reload speed and recoil. They also work with the `/attribute` command

### Changes

### Fixes
- Fixed stacking Hair Triggers giving some guns an infinite fire rate

### API
#### Soft Breaking Changes
- **Value modifier tooltip colors now come from the modified `ComponentType`'s sentiment, not the modifier.** Modifiers your addon applies to Iron's components take that component's color. Your own component types render neutral (yellow) until you pass a `ComponentType.Sentiment` to the new constructor
- `ValueModifier.Type`, the `ValueModifier(amount, operation, type)` constructor and `ValueModifier#type` are deprecated. `ValueModifier` is now a record of `amount` and `operation`. The old constructor still compiles but ignores `type`, and `type()` always returns `NEUTRAL`
  - Record patterns that take three components no longer compile, and `equals` no longer compares the type
- `Utils#formatValueModifierDescription(ValueModifier, Component)` is deprecated and always renders as neutral. Pass the `ComponentType`, or use the new overload that takes a `ComponentType.Sentiment`
- `ValueModifier.Operation` has a new constant, `MULTIPLY_BASE`, between `ADD` and `MULTIPLY_TOTAL`. Exhaustive switches over `Operation` need a case for it
- `ShotProfile#fireDelayTicks()` now returns at least 1

#### Additions
- Added gun attributes, registered in `IronsArtificeAttributes` and carried by every living entity. When an entity composes a shot, its gun attribute modifiers are added to the matching shot component
  - **For static stat bonuses (items, armor, mobeffects), prefer attribute modifiers over `ComposeShotEvent` listeners.** They show in tooltips, sync to clients, and need no code
  - **Attributes are applied before `ComposeShotEvent` fires.**
  - Number attributes: `irons_artifice:` `damage`, `projectile_count`, `spread`, `in_air_penalty`, `fire_rate`, `reload_speed_multiplier`, `ammo_consume_chance`, `accelerating`, `bullet_speed`, `gravity`, `knockback`, `bullet_drag`, `underwater_drag`, `block_damage_multiplier`, `piercing`, `ricochet`, `seeking`, `leech`, `camera_recoil_multiplier`, `character_blowback`
  - Toggle attributes: `irons_artifice:force_auto_fire`, `irons_artifice:breaks_blocks`
  - Number attributes default to 0 and act as bonuses on top of the gun. `ADD_VALUE`, `ADD_MULTIPLIED_BASE` and `ADD_MULTIPLIED_TOTAL` become `ADD`, `MULTIPLY_BASE` and `MULTIPLY_TOTAL` value modifiers. A changed base value is added as a flat bonus
  - Toggle attributes follow NeoForge's `BooleanAttribute` convention: +1 `ADD_VALUE` enables, -1 `ADD_MULTIPLIED_TOTAL` disables, and disabling wins. A toggle with no modifiers and a default base leaves the gun's own value alone
- Added `IGunAttribute`, which exposes its linked `ComponentType`, with the implementations `GunRangedAttribute` (for `Value` components) and `BooleanGunAttribute` (for `Boolean` components)
  - **A registered `IGunAttribute` applies to shots automatically, but you must add it to entities yourself in `EntityAttributeModificationEvent`**
- Added `ComponentType.Sentiment` (`POSITIVE`, `NEUTRAL`, `NEGATIVE`), whether a higher value is better for the shooter. It mirrors `Attribute.Sentiment`, and also sets the linked attribute's tooltip color
- Added `ValueModifier.Operation.MULTIPLY_BASE`, which multiplies the base plus all `ADD` modifiers, like vanilla's `ADD_MULTIPLIED_BASE`
- Added `Utils#formatValueModifierDescription(ValueModifier, Component, ComponentType.Sentiment)`
- Added `IronsArtificeItems`, `IronsArtificeEntities`, `IronsArtificeSounds`, `IronsArtificeParticles` and `IronsArtificeAttributes`, the public registry classes
  - `ItemRegistry`, `EntityRegistry`, `SoundRegistry` and `ParticleRegistry` are deprecated. Their fields point to the new registries
- `CriterionRegistry`, `DataAttachmentRegistry`, `DataComponentRegistry`, `MenuRegistry` and `ExtendedRaiderTypes` are now marked `@ApiStatus.Internal`
