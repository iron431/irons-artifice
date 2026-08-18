package io.redspace.irons_artifice.gun;

import io.redspace.irons_artifice.client.sounds.GunShotSoundSettings;
import io.redspace.irons_artifice.data.FireCycleCue;
import io.redspace.irons_artifice.data.FireCycleCueStack;
import io.redspace.irons_artifice.data.FireMode;
import io.redspace.irons_artifice.data.GunShotSoundStack;
import io.redspace.irons_artifice.data.HandOccupancy;
import io.redspace.irons_artifice.data.MuzzleFlashSettings;
import io.redspace.irons_artifice.data.MuzzleFlashType;
import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.data.ReloadCue;
import io.redspace.irons_artifice.data.ReloadCueStack;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.item.AnimationAdjuster;
import io.redspace.irons_artifice.item.TopLoadConfig;
import io.redspace.irons_artifice.data.RecoilProfile;
import io.redspace.irons_artifice.registry.SoundRegistry;
import net.minecraft.sounds.SoundEvents;

import java.util.Map;

// todo: should probably be converted into an item component, rather than hardcoded to gunitem
public final class Guns {

    public static final GunProfile FLINTLOCK_PISTOL = new GunProfile(
            () -> {
                var map = basicGun();
                map.set(ShotComponents.DAMAGE, Value.of(20));
                map.set(ShotComponents.BULLET_SPEED, Value.of(Bullet.BASE_SPEED * 0.75));
                map.set(ShotComponents.GUNSHOT_SOUND, new GunShotSoundStack(
                        GunShotSoundSettings.standardShot(SoundRegistry.FLINTLOCK_SHOOT, 1f),
                        GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_MUZZLELOADER, 1.1f),
                        PlayableSound.of(PlayableSound.holder(SoundEvents.DISPENSER_FAIL), 0.75f, 1.4f, 1.6f)
                ));
                map.set(ShotComponents.CAMERA_RECOIL, RecoilProfile.of(35f, .45f, 1.7f, 0));
                map.set(ShotComponents.CHARACTER_BLOWBACK, Value.of(0.5));
                map.set(ShotComponents.SPREAD, Value.of(3));
                map.set(ShotComponents.FIRE_DELAY, Value.of(1));
                map.set(ShotComponents.MUZZLE_FLASH, MuzzleFlashSettings.of(1.5f, MuzzleFlashType.LARGE));
                return map;
            },
            1,
            5,
            60,
            FireMode.SEMI,
            null,
            ArmPoseKind.PISTOL,
            ReloadCueStack.of(
                    new ReloadCue(0.0f, PlayableSound.of(SoundRegistry.SIX_SHOOTER_HOLSTER, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(0.4f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_INSERT_BULLET, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(1.3f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_PACK_BULLET, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(2.75f, PlayableSound.of(SoundRegistry.LEATHER_ACCENT, 1.25f, 0.85f, 0.95f)),
                    new ReloadCue(2.88f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 0.85f, 0.95f))
            ),
            PlayableSound.of(SoundRegistry.FLINTLOCK_EQUIP, 0.75f, 0.9f, 1.1f),
            FireCycleCueStack.EMPTY,
            AnimationAdjuster.LOWER_HAMMER,
            Map.of("reload", HandOccupancy.BOTH)
    );

    public static final GunProfile MUSKET = new GunProfile(
            () -> {
                var map = basicGun();
                map.set(ShotComponents.DAMAGE, Value.of(18));
                map.set(ShotComponents.BULLET_SPEED, Value.of(Bullet.BASE_SPEED * 1.5));
                map.set(ShotComponents.CAMERA_RECOIL, RecoilProfile.of(30f, .45f, 1.9f, 111));
                map.set(ShotComponents.SPREAD, Value.of(0.5));
                map.set(ShotComponents.FIRE_DELAY, Value.of(1));
                map.set(ShotComponents.GUNSHOT_SOUND, new GunShotSoundStack(
                        GunShotSoundSettings.standardShot(SoundRegistry.MUSKET_SHOOT, 1f),
                        GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_MUZZLELOADER, 1.15f),
                        PlayableSound.of(PlayableSound.holder(SoundEvents.DISPENSER_FAIL), 0.75f, 1.4f, 1.6f)
                ));
                map.set(ShotComponents.MUZZLE_FLASH, MuzzleFlashSettings.of(2.5f, MuzzleFlashType.LARGE));
                return map;
            },
            1,
            5,
            60,
            FireMode.SEMI,
            null,
            ArmPoseKind.RIFLE,
            ReloadCueStack.of(
                    new ReloadCue(0.0f, PlayableSound.of(SoundRegistry.SIX_SHOOTER_HOLSTER, 1.25f, 0.75f, 0.85f)),
                    new ReloadCue(0.9f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_INSERT_BULLET, 1.25f, 0.95f, 0.85f)),
                    new ReloadCue(1.7f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_PACK_BULLET, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(2.88f, PlayableSound.of(SoundRegistry.LEATHER_ACCENT, 1.25f, 0.85f, 0.95f)),
                    new ReloadCue(2.88f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 0.85f, 0.95f))
            ),
            PlayableSound.of(SoundRegistry.MUSKET_EQUIP, 0.75f, 0.9f, 1.1f),
            FireCycleCueStack.EMPTY,
            AnimationAdjuster.LOWER_HAMMER,
            Map.of()
    );

    public static final GunProfile BLACKPOWDER_REVOLVER = new GunProfile(
            () -> {
                var map = basicGun();
                map.set(ShotComponents.CAMERA_RECOIL, RecoilProfile.of(25f, .33f, 1.7f, 0));
                map.set(ShotComponents.CHARACTER_BLOWBACK, Value.of(0.125));
                map.set(ShotComponents.FIRE_DELAY, Value.of(10));
                map.set(ShotComponents.DAMAGE, Value.of(10));
                map.set(ShotComponents.GUNSHOT_SOUND, new GunShotSoundStack(
                        GunShotSoundSettings.standardShot(SoundRegistry.BLACKPOWDER_REVOLVER_SHOOT, 1f),
                        GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_GENERIC, 0.8f),
                        PlayableSound.of(PlayableSound.holder(SoundEvents.DISPENSER_FAIL), 0.75f, 1.4f, 1.6f)
                ));
                map.set(ShotComponents.MUZZLE_FLASH, MuzzleFlashSettings.of(1.5f, MuzzleFlashType.LARGE));
                return map;
            },
            6,
            5,
            40,
            FireMode.SEMI,
            null,
            ArmPoseKind.PISTOL,
            ReloadCueStack.of(
                    new ReloadCue(0f, PlayableSound.of(SoundRegistry.BLACKPOWDER_REVOLVER_RELOAD_START, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(1.33f, PlayableSound.of(SoundRegistry.BLACKPOWDER_REVOLVER_RELOAD_MID, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(1.71f, PlayableSound.of(SoundRegistry.BLACKPOWDER_REVOLVER_RELOAD_END, 1.25f, 0.95f, 1.05f))
            ),
            PlayableSound.of(SoundRegistry.BLACKPOWDER_REVOLVER_EQUIP, 0.75f, 0.9f, 1.1f),
            FireCycleCueStack.of(
                    new FireCycleCue(1.0f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1f, 0.9f, 1.1f))
            ),
            AnimationAdjuster.NONE,
            Map.of("reload", HandOccupancy.BOTH)
    );


    public static final GunProfile SIX_SHOOTER = new GunProfile(
            () -> {
                var map = basicGun();
                map.set(ShotComponents.CAMERA_RECOIL, RecoilProfile.of(15f, .5f, 2.7f, 465));
                map.set(ShotComponents.FIRE_DELAY, Value.of(3));
                map.set(ShotComponents.SPREAD, Value.of(2.5));
                map.set(ShotComponents.DAMAGE, Value.of(8));
                map.set(ShotComponents.GUNSHOT_SOUND, new GunShotSoundStack(
                        GunShotSoundSettings.standardShot(SoundRegistry.SIX_SHOOTER_SHOOT, 1f),
                        GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_GENERIC, 0.8f),
                        PlayableSound.of(PlayableSound.holder(SoundEvents.DISPENSER_FAIL), 0.75f, 1.4f, 1.6f)
                ));
                return map;
            },
            6,
            5,
            20,
            FireMode.SEMI,
            null,
            ArmPoseKind.PISTOL,
            ReloadCueStack.of(
                    new ReloadCue(0.1f, PlayableSound.of(SoundRegistry.SIX_SHOOTER_HOLSTER, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(0.38f, PlayableSound.of(SoundRegistry.SIX_SHOOTER_EQUIP, 1.25f, 0.95f, 1.05f))
            ),
            PlayableSound.of(SoundRegistry.SIX_SHOOTER_EQUIP, 0.75f, 0.95f, 1.05f),
            FireCycleCueStack.EMPTY,
            AnimationAdjuster.NONE,
            Map.of("fire", HandOccupancy.BOTH)
    );


    public static final GunProfile BLUNDERBUSS = new GunProfile(
            () -> {
                var map = basicGun();
                map.set(ShotComponents.DAMAGE, Value.of(22));
                map.set(ShotComponents.PROJECTILE_COUNT, Value.of(8));
                map.set(ShotComponents.CAMERA_RECOIL, RecoilProfile.of(30f, .35f, 2f, 999));
                map.set(ShotComponents.CHARACTER_BLOWBACK, Value.of(0.75));
                map.set(ShotComponents.SPREAD, Value.of(7));
                map.set(ShotComponents.FIRE_DELAY, Value.of(1));
                map.set(ShotComponents.GUNSHOT_SOUND, new GunShotSoundStack(
                        GunShotSoundSettings.standardShot(SoundRegistry.BLUNDERBUSS_SHOOT, 1f),
                        GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_MUZZLELOADER, 0.75f),
                        PlayableSound.of(PlayableSound.holder(SoundEvents.DISPENSER_FAIL), 0.75f, 1.4f, 1.6f)
                ));
                map.set(ShotComponents.MUZZLE_FLASH, MuzzleFlashSettings.of(2f, MuzzleFlashType.LARGE));
                return map;
            },
            2,
            5,
            30,
            FireMode.SEMI,
            null,
            ArmPoseKind.RIFLE,
            ReloadCueStack.of(
                    new ReloadCue(0.25f, PlayableSound.of(SoundRegistry.BLUNDERBUSS_RELOAD_OPEN, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(0.90f, PlayableSound.of(SoundRegistry.BLUNDERBUSS_RELOAD_LOAD, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(1.15f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 1.1f, 1.3f)),
                    new ReloadCue(1.27f, PlayableSound.of(SoundRegistry.BLUNDERBUSS_RELOAD_CLOSE, 1.25f, 0.9f, 1.1f))
            ),
            PlayableSound.of(SoundRegistry.BLUNDERBUSS_RELOAD_CLOSE, 0.75f, 0.9f, 1.1f),
            FireCycleCueStack.EMPTY,
            AnimationAdjuster.DOUBLE_BARREL_HAMMER,
            Map.of()
    );


    public static final GunProfile ARQUEBUS = new GunProfile(
            () -> {
                var map = basicGun();//
                map.set(ShotComponents.CAMERA_RECOIL, RecoilProfile.of(30f, .45f, -1.9f, 222));
                map.set(ShotComponents.SPREAD, Value.of(1));
                map.set(ShotComponents.FIRE_DELAY, Value.of(20));
                map.set(ShotComponents.DAMAGE, Value.of(12));
                map.set(ShotComponents.MUZZLE_FLASH, MuzzleFlashSettings.of(2f, MuzzleFlashType.LARGE));
                map.set(ShotComponents.GUNSHOT_SOUND, new GunShotSoundStack(
                        GunShotSoundSettings.standardShot(SoundRegistry.ARQUEBUS_SHOOT, 1f),
                        GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_GENERIC, 1.5f),
                        PlayableSound.of(PlayableSound.holder(SoundEvents.DISPENSER_FAIL), 0.75f, 1.4f, 1.6f)
                ));
                return map;
            },
            4,
            7,
            50,
            FireMode.SEMI,
            new TopLoadConfig(0.75, 1.75, 0.33),
            ArmPoseKind.RIFLE,
            ReloadCueStack.of(
                    new ReloadCue(0.00f, PlayableSound.of(SoundRegistry.ARQUEBUS_OPEN_BREECH, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(0.60f, PlayableSound.of(SoundRegistry.ARQUEBUS_LOAD, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(0.95f, PlayableSound.of(SoundRegistry.ARQUEBUS_LOAD, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(1.30f, PlayableSound.of(SoundRegistry.ARQUEBUS_LOAD, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(1.65f, PlayableSound.of(SoundRegistry.ARQUEBUS_LOAD, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(2.13f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 1f, 1.1f)),
                    new ReloadCue(2.50f, PlayableSound.of(SoundRegistry.ARQUEBUS_CLOSE_BREECH, 1.25f, 0.95f, 1.1f))
            ),
            PlayableSound.of(SoundRegistry.ARQUEBUS_EQUIP, 0.5f, 0.9f, 1.1f),
            FireCycleCueStack.of(
                    new FireCycleCue(0.25f / 0.75f, PlayableSound.of(SoundRegistry.ARQUEBUS_OPEN_BREECH, 1f, 0.9f, 1.1f)),
                    new FireCycleCue(0.6f / 0.75f, PlayableSound.of(SoundRegistry.ARQUEBUS_CLOSE_BREECH, 1f, 0.9f, 1.1f))
            ),
            AnimationAdjuster.LOWER_HAMMER,
            Map.of()
    );

    public static final GunProfile CLOCKWORK_RIFLE = new GunProfile(
            () -> {
                var map = basicGun();
                map.set(ShotComponents.CAMERA_RECOIL, RecoilProfile.of(7.5f, .35f, 0.6f, 6969));
                map.set(ShotComponents.CHARACTER_BLOWBACK, Value.of(0.05));
                map.set(ShotComponents.FIRE_DELAY, Value.of(4));
                map.set(ShotComponents.SPREAD, Value.of(2));
                map.set(ShotComponents.DAMAGE, Value.of(6));
                map.set(ShotComponents.MUZZLE_FLASH, MuzzleFlashSettings.of(2f, MuzzleFlashType.TRIANGLE, MuzzleFlashType.SMALL_STAR));
                map.set(ShotComponents.GUNSHOT_SOUND, new GunShotSoundStack(
                        GunShotSoundSettings.standardShot(SoundRegistry.CLOCKWORK_RIFLE_SHOOT, 1f),
                        GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_GENERIC, 1f),
                        PlayableSound.of(PlayableSound.holder(SoundEvents.DISPENSER_FAIL), 0.75f, 1.4f, 1.6f)
                ));
                return map;
            },
            10,
            6,
            30,
            FireMode.AUTO,
            null,
            ArmPoseKind.RIFLE,
            ReloadCueStack.of(
                    new ReloadCue(0.38f, PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_EJECT_MAG, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(1.04f, PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_INSERT_MAG, 1.25f, 0.9f, 1.1f))
            ),
            PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_EQUIP, 0.75f, 0.9f, 1.1f),
            FireCycleCueStack.EMPTY,
            AnimationAdjuster.HARMONICA_MAGAZINE,
            Map.of()
    );

    private static ShotComponentMap basicGun() {
        ShotComponentMap map = new ShotComponentMap();
        map.set(ShotComponents.PROJECTILE_COUNT, Value.of(1));
        map.set(ShotComponents.SPREAD, Value.of(1.0));
        map.set(ShotComponents.DAMAGE, Value.of(6.0));
        map.set(ShotComponents.BULLET_SPEED, Value.of(Bullet.BASE_SPEED));
        map.set(ShotComponents.GRAVITY, Value.of(0.05));
        map.set(ShotComponents.KNOCKBACK, Value.of(0.3));
        map.set(ShotComponents.CAMERA_RECOIL, RecoilProfile.of(10f, .33f, 1.7f, 431));
        map.set(ShotComponents.CHARACTER_BLOWBACK, Value.of(0.0));
        map.set(ShotComponents.FIRE_DELAY, Value.of(20));
        return map;
    }
}
