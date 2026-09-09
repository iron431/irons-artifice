### API
- Reworked `AnimationAdjuster`
  - No longer an enum, but an interface. Can now be implemented by addons.
  - `GunProfile` now takes a list of animation adjusters, instead of only one
  - Removed `NONE` animation adjuster