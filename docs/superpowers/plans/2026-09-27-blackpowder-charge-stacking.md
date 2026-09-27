# Blackpowder Charge Stacking Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Each extra Blackpowder Charge modifier on a gun adds +1 block to the explosion radius, and the explosion particle grows with the radius.

**Architecture:** `MuzzleFlashParticleOption` gains a `scale` field that defaults to 1, and both particle classes multiply their quad size by it. `BlackpowderChargeOnHit` becomes a stackable effect. It follows the `IgnitePostHit` convention: `getOrCreate(Class, factory)` on the component list, then a mutator call per modifier. The explosion reads its radius from the stack count and spawns its particle at `radius / BASE_RADIUS` scale.

**Tech Stack:** NeoForge 26.1.2.84, Minecraft 26.1.2 (mojmap), Java, Mojang `Codec` / `StreamCodec`.

**Spec:** This plan (from the user's request in chat).

## Global Constraints

- One stack gives radius 3, the same as today. Each further stack adds exactly 1 block.
- Every existing `MuzzleFlashParticleOption` path (gun muzzle flashes, gravity well) keeps scale 1 and looks the same as before.
- Particle scale for the blackpowder explosion = `radius / 3`.
- There are no automated tests in this repo (no `src/test`, no gametest classes). Verify by compiling and checking in game.
- No explanatory or history comments in code (see the user's self-documenting-code preference).

---

### Task 1: Scale parameter on muzzle flash particle option

**Files:**
- Modify: `src/main/java/io/redspace/irons_artifice/client/particle/MuzzleFlashParticleOption.java`
- Modify: `src/main/java/io/redspace/irons_artifice/client/particle/MuzzleFlashParticle.java:33-46,152-156`
- Modify: `src/main/java/io/redspace/irons_artifice/client/particle/TintedExplosionParticle.java:13-16,27-31`

**Interfaces:**
- Produces: `new MuzzleFlashParticleOption(ParticleType<MuzzleFlashParticleOption> type, float r, float g, float b, float scale)` and `float MuzzleFlashParticleOption.scale()`.
- Keeps: the 4-arg constructor `(type, r, g, b)`, which delegates with `scale = 1f`. Existing callers (`MuzzleFlashType.particle`, `GravityWellOnHit`, `BlackpowderChargeOnHit`) stay unchanged in this task.

- [ ] **Step 1: Add `scale` to the option, codec and stream codec**

Replace the body of `MuzzleFlashParticleOption` with:

```java
public class MuzzleFlashParticleOption implements ParticleOptions {
    public static MapCodec<MuzzleFlashParticleOption> codec(ParticleType<MuzzleFlashParticleOption> type) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.FLOAT.fieldOf("r").forGetter(MuzzleFlashParticleOption::r),
                Codec.FLOAT.fieldOf("g").forGetter(MuzzleFlashParticleOption::g),
                Codec.FLOAT.fieldOf("b").forGetter(MuzzleFlashParticleOption::b),
                Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(MuzzleFlashParticleOption::scale)
        ).apply(builder, (r, g, b, scale) -> new MuzzleFlashParticleOption(type, r, g, b, scale)));
    }

    public static StreamCodec<? super RegistryFriendlyByteBuf, MuzzleFlashParticleOption> streamCodec(
            ParticleType<MuzzleFlashParticleOption> type
    ) {
        return StreamCodec.composite(
                ByteBufCodecs.FLOAT, MuzzleFlashParticleOption::r,
                ByteBufCodecs.FLOAT, MuzzleFlashParticleOption::g,
                ByteBufCodecs.FLOAT, MuzzleFlashParticleOption::b,
                ByteBufCodecs.FLOAT, MuzzleFlashParticleOption::scale,
                (r, g, b, scale) -> new MuzzleFlashParticleOption(type, r, g, b, scale)
        );
    }

    private final ParticleType<MuzzleFlashParticleOption> type;
    private final float r;
    private final float g;
    private final float b;
    private final float scale;

    public MuzzleFlashParticleOption(ParticleType<MuzzleFlashParticleOption> type, float r, float g, float b) {
        this(type, r, g, b, 1f);
    }

    public MuzzleFlashParticleOption(ParticleType<MuzzleFlashParticleOption> type, float r, float g, float b, float scale) {
        this.type = type;
        this.r = r;
        this.g = g;
        this.b = b;
        this.scale = scale;
    }

    public float r() {
        return r;
    }

    public float g() {
        return g;
    }

    public float b() {
        return b;
    }

    public float scale() {
        return scale;
    }

    public boolean isTinted() {
        return r != 1f || g != 1f || b != 1f;
    }

    @Override
    public ParticleType<MuzzleFlashParticleOption> getType() {
        return type;
    }
}
```

`optionalFieldOf("scale", 1f)` keeps any `r/g/b`-only JSON or command input valid.

- [ ] **Step 2: Take the scale in `MuzzleFlashParticle`**

The scale goes through the constructor instead of vanilla `Particle.scale(float)`, because that method also resizes the particle's bounding box. That would change collision for every existing muzzle flash, even at scale 1.

Constructor signature and quad size:

```java
    public MuzzleFlashParticle(ClientLevel level, double x, double y, double z,
                               double xa, double ya, double za, SpriteSet sprites,
                               float tintR, float tintG, float tintB, float scale) {
        super(level, x, y, z, xa, ya, za, sprites.first());
        this.sprites = sprites;
        this.tinted = !(tintR < 0f || tintG < 0 || tintB < 0);
        this.lifetime = 3;
        this.xd = xa;
        this.yd = ya;
        this.zd = za;
        this.quadSize = scale;
```

(The rest of the constructor stays the same.)

Provider:

```java
            return new MuzzleFlashParticle(level, x, y, z, xa, ya, za, this.sprite, options.r(), options.g(), options.b(), options.scale());
```

- [ ] **Step 3: Take the scale in `TintedExplosionParticle`**

```java
    public TintedExplosionParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za, SpriteSet sprites, float tintR, float tintG, float tintB, float scale) {
        super(level, x, y, z, xa, ya, za, sprites, tintR, tintG, tintB, scale);
        this.quadSize = 3 * scale;
        this.lifetime = 4;
    }
```

Provider:

```java
            return new TintedExplosionParticle(level, x, y, z, xa, ya, za, this.sprite, options.r(), options.g(), options.b(), options.scale());
```

- [ ] **Step 4: Compile**

Run: `./gradlew compileJava`
Expected: `BUILD SUCCESSFUL`. Any other caller of the particle constructors will fail here. The grep during planning found none outside the two Providers.

- [ ] **Step 5: In-game check that nothing changed**

Run: `./gradlew runClient`. Fire any gun, fire a gun with a Singularity Charge modifier, and fire a gun with a Blackpowder Charge modifier.
Expected: the muzzle flash, the purple gravity-well burst and the blackpowder explosion look exactly as before.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/io/redspace/irons_artifice/client/particle/MuzzleFlashParticleOption.java src/main/java/io/redspace/irons_artifice/client/particle/MuzzleFlashParticle.java src/main/java/io/redspace/irons_artifice/client/particle/TintedExplosionParticle.java
git commit -m "Add scale to muzzle flash particle option"
```

---

### Task 2: Stackable blackpowder charge radius with scaled explosion particle

**Files:**
- Modify: `src/main/java/io/redspace/irons_artifice/modifier/on_hit_handlers/BlackpowderChargeOnHit.java`
- Modify: `src/main/java/io/redspace/irons_artifice/modifier/modifiers/BlackpowderChargeModifier.java:14-16`

**Interfaces:**
- Consumes: `new MuzzleFlashParticleOption(type, r, g, b, scale)` from Task 1; `OnHitEffects.getOrCreate(Class<T>, Supplier<T>)` (existing, `modifier/OnHitEffects.java:20`).
- Produces: `BlackpowderChargeOnHit.addStack()`, `float BlackpowderChargeOnHit.radius()`, `public static final float BASE_RADIUS = 3f`, `public static final float RADIUS_PER_STACK = 1f`.

Convention being followed (`IncendiaryTipModifier.java:34-36`):

```java
components.getOrCreate(ShotComponents.POST_HIT_EFFECTS)
        .getOrCreate(IgnitePostHit.class, () -> new IgnitePostHit(0))
        .addDuration(BURN_TICKS_PER);
```

`GunplayManager.compose` builds a fresh `ShotComponentMap` from `gunProfile.baseProfile()` and applies each modifier item in turn. The first modifier creates the effect and later ones find the same instance. No gun's base template contains a `BlackpowderChargeOnHit`, so the shallow `OnHitEffects.copy()` never shares a mutated instance between shots. The same holds for `IgnitePostHit` today.

- [ ] **Step 1: Make the effect stackable**

In `BlackpowderChargeOnHit`, replace the constants and add stack state:

```java
public class BlackpowderChargeOnHit implements OnHitEffect {
    public static final float BASE_RADIUS = 3f;
    public static final float RADIUS_PER_STACK = 1f;
    private static final float DAMAGE_FRACTION = 1f;

    private int stacks;

    public void addStack() {
        stacks++;
    }

    public float radius() {
        return BASE_RADIUS + (stacks - 1) * RADIUS_PER_STACK;
    }
```

- [ ] **Step 2: Use `radius()` in `onHit`**

Read the radius once at the top of `onHit` and replace every `RADIUS` with `radius`:

```java
    @Override
    public void onHit(ServerLevel level, Bullet bullet, HitResult hitResult, HitEntityAccumulator accumulator) {
        float radius = radius();
        Vec3 center = hitResult.getLocation().subtract(bullet.getDeltaMovement().normalize().scale(0.25));
        float radiusSq = radius * radius;
        AABB area = AABB.ofSize(center, radius * 2, radius * 2, radius * 2);
        float baseDamage = bullet.resolveDamage() * DAMAGE_FRACTION;
        Entity owner = bullet.getOwner();

        for (Entity entity : level.getEntities(bullet, area, e ->
                e.canBeHitByProjectile() && Utils.canHarm(owner, e))) {
            if (accumulator.contains(entity)) {
                continue;
            }
            double distSq = entity.getBoundingBox().getCenter().distanceToSqr(center);
            if (distSq > radiusSq) {
                continue;
            }
            float falloff = 1f - (float) Math.sqrt(distSq) / radius;
            float damage = baseDamage * falloff;
            if (damage <= 0) {
                continue;
            }
            if (entity.hurtServer(level, bullet.damageSources().explosion(bullet, owner instanceof LivingEntity living ? living : null), damage)) {
                accumulator.add(entity);
            }
        }

        ShotProfile profile = bullet.getProfile();
        if (profile != null && profile.peek(ShotComponents.BREAKS_BLOCKS)
                && !(owner instanceof Mob && !level.getGameRules().get(GameRules.MOB_GRIEFING))) {
            float blockDamageMultiplier = (float) profile.value(ShotComponents.BLOCK_DAMAGE_MULTIPLIER);
            BlockPos.betweenClosed(
                    BlockPos.containing(center.x - radius, center.y - radius, center.z - radius),
                    BlockPos.containing(center.x + radius, center.y + radius, center.z + radius)
            ).forEach(pos -> {
                BlockState state = level.getBlockState(pos);
                if (state.isAir()) {
                    return;
                }
                double distSq = Vec3.atCenterOf(pos).distanceToSqr(center);
                if (distSq > radiusSq) {
                    return;
                }
                float falloff = 1f - (float) Math.sqrt(distSq) / radius;
                float damage = baseDamage * blockDamageMultiplier * falloff;
                if (damage <= 0) {
                    return;
                }
                if (BlockDamageManager.applyDamage(level, pos.immutable(), state, damage, bullet)) {
                    bullet.setBrokeBlocksThisTick();
                }
            });
        }
```

- [ ] **Step 3: Scale the explosion particle by `radius / BASE_RADIUS`**

Replace the `EXPLOSION_96` spawn line:

```java
        Utils.spawnParticles(level, new MuzzleFlashParticleOption(ParticleRegistry.EXPLOSION_96.get(), -1, -1, -1, radius / BASE_RADIUS), center.x, center.y + 0.25, center.z, 1, 0, 0, 0, 0, true);
```

At one stack, `radius / BASE_RADIUS == 1`, so the explosion looks the same as today.

- [ ] **Step 4: Stack in the modifier**

`BlackpowderChargeModifier.apply`:

```java
    @Override
    public void apply(ShotComponentMap components) {
        components.getOrCreate(ShotComponents.ON_HIT)
                .getOrCreate(BlackpowderChargeOnHit.class, BlackpowderChargeOnHit::new)
                .addStack();
    }
```

- [ ] **Step 5: Compile**

Run: `./gradlew compileJava`
Expected: `BUILD SUCCESSFUL`, with no remaining reference to the removed `RADIUS` constant.

- [ ] **Step 6: In-game check**

Run: `./gradlew runClient`. Use a gun with at least 3 modifier slots (e.g. the musket).
1. One Blackpowder Charge: shoot a wall of dirt with a gun that breaks blocks, or shoot a crowd of mobs. The blast reaches about 3 blocks and the particle is the same size as before.
2. Two Blackpowder Charges: the blast reaches about 4 blocks and the particle is visibly larger (4/3).
3. Three Blackpowder Charges: the blast reaches about 5 blocks and the particle is 5/3 size.
4. Shoot with one charge again afterwards. The radius is back to 3, which confirms that stacks do not leak between composed shots.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/io/redspace/irons_artifice/modifier/on_hit_handlers/BlackpowderChargeOnHit.java src/main/java/io/redspace/irons_artifice/modifier/modifiers/BlackpowderChargeModifier.java
git commit -m "Stack blackpowder charge explosion radius"
```

---

## Out of scope

- The tooltip text (`irons_artifice.modifier.blackpowder_charge` = "Explode on Hit") does not show the radius. Showing it would mean changing all 8 lang files. That can be done later if wanted.
- The smoke and lava accent particle counts stay fixed and do not grow with the radius.
