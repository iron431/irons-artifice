### Additions

### Changes

### Fixes

### API
#### Soft Breaking Changes
- `ValueModifier.Type`, the `ValueModifier(amount, operation, type)` constructor and `ValueModifier#type` are deprecated. The type is ignored, and `type()` always returns `NEUTRAL`
  - `ValueModifier` is now a record of `amount` and `operation` only. The old three-argument constructor still compiles, but `type` is ignored. Component Types now take a `Sentiment` enum instead
- Value modifier tooltip colors now come from `ComponentType` sentiment instead of the modifier's `Type`. 
- Component Types now default to "neutral" sentiment -- addon component types now render their modifiers as neutral (yellow) until specified.
- `Utils#formatValueModifierDescription(ValueModifier, Component)` is deprecated and always renders as neutral. Pass the `ComponentType`, or use the new overload that takes a `ComponentType.Sentiment`

#### Additions
- Added gun attributes, registered in `IronsArtificeAttributes` and carried by every living entity. When an entity composes a shot, its gun attribute modifiers are added to the matching shot component
  - Number attributes: `irons_artifice:` `damage`, `projectile_count`, `spread`, `in_air_penalty`, `fire_rate`, `reload_speed_multiplier`, `ammo_consume_chance`, `accelerating`, `bullet_speed`, `gravity`, `knockback`, `bullet_drag`, `underwater_drag`, `block_damage_multiplier`, `piercing`, `ricochet`, `seeking`, `leech`, `camera_recoil_multiplier`, `character_blowback`
  - Toggle attributes: `irons_artifice:force_auto_fire`, `irons_artifice:breaks_blocks`
  - Number attributes default to 0 and act as bonuses on top of the gun. `ADD_VALUE`, `ADD_MULTIPLIED_BASE` and `ADD_MULTIPLIED_TOTAL` become `ADD`, `MULTIPLY_BASE` and `MULTIPLY_TOTAL` value modifiers. A changed base value is added as a flat bonus
  - Toggle attributes follow NeoForge's `BooleanAttribute` convention: +1 `ADD_VALUE` enables, -1 `ADD_MULTIPLIED_TOTAL` disables, and disabling wins. A toggle with no modifiers and a default base leaves the gun's own value alone
- Added `IGunAttribute`, with the implementations `GunRangedAttribute` (for `Value` components) and `BooleanGunAttribute` (for `Boolean` components)
- Added `ComponentType.Sentiment` (`POSITIVE`, `NEUTRAL`, `NEGATIVE`), whether a higher value is better for the shooter (mirror Attribute Modifier Sentiment)
- Added `ValueModifier.Operation.MULTIPLY_BASE`
- Added `Utils#formatValueModifierDescription(ValueModifier, Component, ComponentType.Sentiment)`
- Added `IronsArtificeItems`, `IronsArtificeEntities`, `IronsArtificeSounds`, `IronsArtificeParticles` and `IronsArtificeAttributes`, the public registry classes
  - `ItemRegistry`, `EntityRegistry`, `SoundRegistry` and `ParticleRegistry` are intact but deprecated, pointing to the new registries
- `CriterionRegistry`, `DataAttachmentRegistry`, `DataComponentRegistry`, `MenuRegistry` and `ExtendedRaiderTypes` are now marked `@ApiStatus.Internal`
