### Additions
- Added Soulfire Coin Modifier
  - Chance to create a Soul-Empowering Coin on hit
- Added Bullet Box item
  - Can store up to 256 Bullets, like a bundle
- Added Korean translations, thanks to spongebob98636e

### Changes
- Blackpowder Charge Modifier now stacks explicitly, gaining +1 Radius for each additional modifier

### Fixes
- Fixed Muzzle Flash spawning when using shaders
- (1.21.1) Fixed attachment rendering when using shaders
- Fixed Sable compat
- Fixed creative tab ordering

### API
- Added `SimpleMobEffectPostHit`, a generic handler for applying mob effects as a `PostHitEffect`
- Added `ShotComponents.ON_SHOT` component, of type `OnShotEffects`, which is a stack of `OnShotEffect`
  - Added `OnShotEffect` handler, giving `void onShot(ServerLevel level, LivingEntity shooter, ShotProfile profile);` whenever the gun shoots
- Added `BulletContainerItem` and related bundle-like handling classes
  - Added `BulletContainerContents`, a resizable bundle-like item component for Bullet Container Items
