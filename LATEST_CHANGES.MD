## Unofficial 1.21.1 port changes

### Additions
- Bayonet Charge (spear lunge) backported to 1.21.1: hold Use with a bayonet-equipped gun to charge. The charge hits entities along your view every tick, with effects based on your momentum (dismount riders when very fast early in the charge, knockback while engaged, damage based on relative speed). Charging does not slow your movement, and you can start/keep sprinting while charging — mounted charges work too.

### Fixes
- Fixed muzzle flashes anchoring only to the local player's gun: flashes from other players and gun-wielding mobs (pirates, illificers, etc.) now emit from the shooter's actual muzzle bone instead of always falling back to a stale eye-position approximation.
- Fixed muzzle flashes being lost when several shots were fired by one entity within a single frame.

### Port internals
- Added `BayonetLunge`, a server-side reimplementation of the vanilla 26.x `KINETIC_WEAPON`/spear charge (upstream gets it from data components unavailable on 1.21.1), driven by `LivingEntityUseItemEvent`s. Mob bayonet AI now uses the same engine instead of its bespoke manual stab (matching upstream's `stabbedEntities` behavior).
- Added `ItemRendererMixin` + `RenderingEntityTracker` so the in-hand gun renderer knows which entity it is rendering for (upstream's `ITEM_OWNER_ID_TICKET` equivalent).
- Added `LocalPlayerMixin` replicating the vanilla spear's use effects (no 20% input slowdown, sprint allowed while charging).
- Added `ClientboundBayonetHitPacket` for first person hit feedback (hit sound + animation flinch).
- Consolidated duplicated helpers: `GunItem#hasBayonet`, `GunInHandRenderer#withBonePose`, `ClientHelper#spawnImpactBurst`, `Utils#randomUnitVector`.

---

### Additions
- Added Suppressor Attachment Modifier
- Added Hook Shot Modifier
- Added Bloodletting Tip Modifier
- Added `irons_artifice:guns` tag
- Added `irons_artifice:ammo` tag

### Changes
- Buffed Clockwork Rifle damage 6 -> 7
- Nerfed Frozen Jacket damage per shrapnel 50%->33%
- Fire Rate is now displayed in the tooltip at the effective firerate instead of the raw numerical rate (rounded to ticks)

### Fixes
- Fixed Bullets not firing projectile impact event
- Reworked fire delay handling to reduce network jitter that could cause fire rate stuttering for automatic guns, especially on laggy connections.

### API
#### BREAKING CHANGES
- `GunplayManager#tryFire` now returns an enum `FireOutcome` instead of a boolean
  - Contains additional context for why the shot failed, or fired.
- Fire Delay is now an entity attachment, not an item component
- Hand Occupancy is now keyed by the new `GunState` enum (`RELOAD`, `FIRE`) instead of raw strings. `GunProfile#occupancyFor` now takes a `GunState`.
- `GunItem#currentOccupancy` now must take an entity
- Reworked `AnimationAdjuster`
  - No longer an enum, but an interface. Can now be implemented by addons.
  - `GunProfile` now takes a list of animation adjusters, instead of only one
  - Removed `NONE` animation adjuster
- `GunProfile` is now a final class instead of a record. Constructor is now private, and uses `GunProfile.Builder`
- `ShotProfile#get` has been renamed to `ShotProfile#peek`. This is to clarify that returned value is not stored (so mutating it was a silent failure)
#### Additions
- All `GunItems`s, including those added by addons, are automatically registered with a gun renderer and arm pose extension
- Added item tags `irons_artifice:guns` and `irons_artifice:ammo` (`IronsArtificeTags.GUNS`, `IronsArtificeTags.AMMO`)
  - For ammo, there is currently no support for delineating which stacks where chosen when loaded into a gun
- Added `api.GunBones`: named constants for every bone the client helpers look up. Use these in your own models if you weren't already.
- Added `api.GunAnimations`: named constants for the animation names (`idle`, `fire`, `reload`, `equip`) and controller names the gunplay system triggers. Use these in your own animation files if you weren't already.
- `GunInHandRenderer` render hooks are now `protected` so the renderer can be more easily subclassed.
- `IronsArtificeClient.armPoseExtension` is now public.
- Datagen helpers are now public and reusable: `ItemModelDataGenerator.gunModel(...)` and `generateTemplatedItem(...)`, `GECKOLIB_GUN_DISPLAY` / `REVOLVER_GUN_DISPLAY`.
- The published artifact now includes a sources jar.
- Added `ShotComponentTemplate`, a `Supplier<ShotComponentMap>` for building shot components (instead of the in-line supplier in the old GunProfile constructor). Build one with `ShotComponentTemplate.builder(damage, spread, characterBlowback, fireDelayTicks, cameraRecoil)`
- Added `ShotProfile#modify(type, consumer)` to modify components in one line
- Added `ShotProfile#modifyValue(type, ValueModifier)` and `ShotComponentMap#modifyValue(type, ValueModifier)`, helpers to `modify` components typed as values with a `ValueModifier`
