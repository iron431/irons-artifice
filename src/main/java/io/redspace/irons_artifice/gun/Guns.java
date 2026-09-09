package io.redspace.irons_artifice.gun;

import io.redspace.irons_artifice.client.sounds.GunShotSoundSettings;
import io.redspace.irons_artifice.data.FireCycleCue;
import io.redspace.irons_artifice.data.FireCycleCueStack;
import io.redspace.irons_artifice.data.FireMode;
import io.redspace.irons_artifice.data.HandOccupancy;
import io.redspace.irons_artifice.data.MuzzleFlashType;
import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.data.RecoilProfile;
import io.redspace.irons_artifice.data.ReloadCue;
import io.redspace.irons_artifice.data.ReloadCueStack;
import io.redspace.irons_artifice.data.ShotComponentTemplate;
import io.redspace.irons_artifice.item.animation_adjuster.AnimationAdjuster;
import io.redspace.irons_artifice.item.TopLoadConfig;
import io.redspace.irons_artifice.registry.SoundRegistry;
import net.minecraft.sounds.SoundEvents;

// todo: should probably be converted into an item component, rather than hardcoded to gunitem
public final class Guns {

    public static final GunProfile FLINTLOCK_PISTOL = GunProfile.builder(1, 5, 40, FireMode.SEMI, ArmPoseKind.PISTOL,
                    ShotComponentTemplate.builder(20, 3, 0.5, 1, RecoilProfile.of(35f, .45f, 1.7f, 0))
                            .bulletSpeedMultiplier(0.75)
                            .gunshotSound(
                                    GunShotSoundSettings.standardShot(SoundRegistry.FLINTLOCK_SHOOT, 1f),
                                    GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_MUZZLELOADER, 1.1f),
                                    PlayableSound.holder(SoundEvents.DISPENSER_FAIL))
                            .muzzleFlash(MuzzleFlashType.LARGE)
                            .build())
            .reloadCues(ReloadCueStack.of(
                    new ReloadCue(0.00f, PlayableSound.of(SoundRegistry.SIX_SHOOTER_HOLSTER, 0.75f, 0.95f, 1.05f)),
                    new ReloadCue(0.20f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_INSERT_BULLET, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(1.00f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_PACK_BULLET, 1.25f, 1.05f, 1.15f)),
                    new ReloadCue(1.75f, PlayableSound.of(SoundRegistry.LEATHER_ACCENT, 0.75f, 0.85f, 0.95f)),
                    new ReloadCue(1.85f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 0.85f, 0.95f))
            ))
            .equipSound(PlayableSound.of(SoundRegistry.FLINTLOCK_EQUIP, 0.75f, 0.9f, 1.1f))
            .animationAdjusters(AnimationAdjuster.LOWER_HAMMER, AnimationAdjuster.MUZZLE_LOAD_OFFSET)
            .occupancy(GunState.RELOAD, HandOccupancy.BOTH)
            .build();

    public static final GunProfile MUSKET = GunProfile.builder(1, 5, 60, FireMode.SEMI, ArmPoseKind.RIFLE,
                    ShotComponentTemplate.builder(18, 0.5, 0, 1, RecoilProfile.of(30f, .45f, 1.9f, 111))
                            .bulletSpeedMultiplier(1.5)
                            .gunshotSound(
                                    GunShotSoundSettings.standardShot(SoundRegistry.MUSKET_SHOOT, 1f),
                                    GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_MUZZLELOADER, 1.15f),
                                    PlayableSound.holder(SoundEvents.DISPENSER_FAIL))
                            .muzzleFlash(MuzzleFlashType.LARGE)
                            .build())
            .reloadCues(ReloadCueStack.of(
                    new ReloadCue(0.0f, PlayableSound.of(SoundRegistry.SIX_SHOOTER_HOLSTER, 1.25f, 0.75f, 0.85f)),
                    new ReloadCue(0.9f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_INSERT_BULLET, 1.25f, 0.95f, 0.85f)),
                    new ReloadCue(1.7f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_PACK_BULLET, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(2.88f, PlayableSound.of(SoundRegistry.LEATHER_ACCENT, 1.25f, 0.85f, 0.95f)),
                    new ReloadCue(2.88f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 0.85f, 0.95f))
            ))
            .equipSound(PlayableSound.of(SoundRegistry.MUSKET_EQUIP, 0.75f, 0.9f, 1.1f))
            .animationAdjusters(AnimationAdjuster.LOWER_HAMMER, AnimationAdjuster.MUZZLE_LOAD_OFFSET)
            .build();

    public static final GunProfile BLACKPOWDER_REVOLVER = GunProfile.builder(6, 5, 40, FireMode.SEMI, ArmPoseKind.PISTOL,
                    ShotComponentTemplate.builder(10, 1, 0.125, 10, RecoilProfile.of(25f, .33f, 1.7f, 0))
                            .gunshotSound(
                                    GunShotSoundSettings.standardShot(SoundRegistry.BLACKPOWDER_REVOLVER_SHOOT, 1f),
                                    GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_GENERIC_PISTOL, 0.9f),
                                    PlayableSound.holder(SoundEvents.DISPENSER_FAIL))
                            .muzzleFlash(MuzzleFlashType.LARGE)
                            .build())
            .reloadCues(ReloadCueStack.of(
                    new ReloadCue(0f, PlayableSound.of(SoundRegistry.BLACKPOWDER_REVOLVER_RELOAD_START, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(1.33f, PlayableSound.of(SoundRegistry.BLACKPOWDER_REVOLVER_RELOAD_MID, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(1.71f, PlayableSound.of(SoundRegistry.BLACKPOWDER_REVOLVER_RELOAD_END, 1.25f, 0.95f, 1.05f))
            ))
            .equipSound(PlayableSound.of(SoundRegistry.BLACKPOWDER_REVOLVER_EQUIP, 0.75f, 0.9f, 1.1f))
            .fireCycleCues(FireCycleCueStack.of(
                    new FireCycleCue(1.0f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1f, 0.9f, 1.1f))
            ))
            .occupancy(GunState.RELOAD, HandOccupancy.BOTH)
            .build();

    public static final GunProfile SIX_SHOOTER = GunProfile.builder(6, 5, 20, FireMode.SEMI, ArmPoseKind.PISTOL,
                    ShotComponentTemplate.builder(8, 2.5, 0, 3, RecoilProfile.of(15f, .5f, 2.7f, 465))
                            .gunshotSound(
                                    GunShotSoundSettings.standardShot(SoundRegistry.SIX_SHOOTER_SHOOT, 1f),
                                    GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_GENERIC_PISTOL, 1.1f),
                                    PlayableSound.holder(SoundEvents.DISPENSER_FAIL))
                            .build())
            .reloadCues(ReloadCueStack.of(
                    new ReloadCue(0.1f, PlayableSound.of(SoundRegistry.SIX_SHOOTER_HOLSTER, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(0.38f, PlayableSound.of(SoundRegistry.SIX_SHOOTER_EQUIP, 1.25f, 0.95f, 1.05f))
            ))
            .equipSound(PlayableSound.of(SoundRegistry.SIX_SHOOTER_EQUIP, 0.75f, 0.95f, 1.05f))
            .occupancy(GunState.FIRE, HandOccupancy.BOTH)
            .build();

    public static final GunProfile BLUNDERBUSS = GunProfile.builder(2, 5, 30, FireMode.SEMI, ArmPoseKind.RIFLE,
                    ShotComponentTemplate.builder(22, 7, 0.75, 1, RecoilProfile.of(30f, .35f, 2f, 999))
                            .projectileCount(8)
                            .gunshotSound(
                                    GunShotSoundSettings.standardShot(SoundRegistry.BLUNDERBUSS_SHOOT, 1f),
                                    GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_MUZZLELOADER, 0.75f),
                                    PlayableSound.holder(SoundEvents.DISPENSER_FAIL))
                            .muzzleFlash(MuzzleFlashType.LARGE)
                            .build())
            .reloadCues(ReloadCueStack.of(
                    new ReloadCue(0.25f, PlayableSound.of(SoundRegistry.BLUNDERBUSS_RELOAD_OPEN, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(0.90f, PlayableSound.of(SoundRegistry.BLUNDERBUSS_RELOAD_LOAD, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(1.15f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 1.1f, 1.3f)),
                    new ReloadCue(1.27f, PlayableSound.of(SoundRegistry.BLUNDERBUSS_RELOAD_CLOSE, 1.25f, 0.9f, 1.1f))
            ))
            .equipSound(PlayableSound.of(SoundRegistry.BLUNDERBUSS_RELOAD_CLOSE, 0.75f, 0.9f, 1.1f))
            .animationAdjusters(AnimationAdjuster.DOUBLE_BARREL_HAMMER)
            .build();

    public static final GunProfile ARQUEBUS = GunProfile.builder(4, 7, 50, FireMode.SEMI, ArmPoseKind.RIFLE,
                    ShotComponentTemplate.builder(12, 1, 0, 20, RecoilProfile.of(30f, .45f, -1.9f, 222))
                            .gunshotSound(
                                    GunShotSoundSettings.standardShot(SoundRegistry.ARQUEBUS_SHOOT, 1f),
                                    GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_GENERIC, 1.5f),
                                    PlayableSound.holder(SoundEvents.DISPENSER_FAIL))
                            .muzzleFlash(MuzzleFlashType.LARGE)
                            .build())
            .topLoadConfig(new TopLoadConfig(0.75, 1.75, 0.33))
            .reloadCues(ReloadCueStack.of(
                    new ReloadCue(0.00f, PlayableSound.of(SoundRegistry.ARQUEBUS_OPEN_BREECH, 1.25f, 0.95f, 1.05f)),
                    new ReloadCue(0.60f, PlayableSound.of(SoundRegistry.ARQUEBUS_LOAD, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(0.95f, PlayableSound.of(SoundRegistry.ARQUEBUS_LOAD, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(1.30f, PlayableSound.of(SoundRegistry.ARQUEBUS_LOAD, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(1.65f, PlayableSound.of(SoundRegistry.ARQUEBUS_LOAD, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(2.13f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 1f, 1.1f)),
                    new ReloadCue(2.50f, PlayableSound.of(SoundRegistry.ARQUEBUS_CLOSE_BREECH, 1.25f, 0.95f, 1.1f))
            ))
            .equipSound(PlayableSound.of(SoundRegistry.ARQUEBUS_EQUIP, 0.5f, 0.9f, 1.1f))
            .fireCycleCues(FireCycleCueStack.of(
                    new FireCycleCue(0.25f / 0.75f, PlayableSound.of(SoundRegistry.ARQUEBUS_OPEN_BREECH, 1f, 0.9f, 1.1f)),
                    new FireCycleCue(0.6f / 0.75f, PlayableSound.of(SoundRegistry.ARQUEBUS_CLOSE_BREECH, 1f, 0.9f, 1.1f))
            ))
            .animationAdjusters(AnimationAdjuster.LOWER_HAMMER)
            .build();

    public static final GunProfile CLOCKWORK_RIFLE = GunProfile.builder(10, 6, 30, FireMode.AUTO, ArmPoseKind.RIFLE,
                    ShotComponentTemplate.builder(7, 2, 0.05, 4, RecoilProfile.of(7.5f, .35f, 0.6f, 6969))
                            .gunshotSound(
                                    GunShotSoundSettings.standardShot(SoundRegistry.CLOCKWORK_RIFLE_SHOOT, 1f),
                                    GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_GENERIC, 1f),
                                    PlayableSound.holder(SoundEvents.DISPENSER_FAIL))
                            .muzzleFlash(MuzzleFlashType.TRIANGLE, MuzzleFlashType.SMALL_STAR)
                            .build())
            .reloadCues(ReloadCueStack.of(
                    new ReloadCue(0.38f, PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_EJECT_MAG, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(1.04f, PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_INSERT_MAG, 1.25f, 0.9f, 1.1f))
            ))
            .equipSound(PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_EQUIP, 0.75f, 0.9f, 1.1f))
            .animationAdjusters(AnimationAdjuster.HARMONICA_MAGAZINE)
            .build();

    public static final GunProfile AK47 = GunProfile.builder(30, 6, 45, FireMode.AUTO, ArmPoseKind.RIFLE,
                    ShotComponentTemplate.builder(7, 1.5, 0.025, 2, RecoilProfile.of(8f, .5f, 0.78f, 474747))
                            .gunshotSound(
                                    GunShotSoundSettings.standardShot(SoundRegistry.AK47_SHOOT, 1f),
                                    GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_GENERIC, 1f),
                                    PlayableSound.holder(SoundEvents.DISPENSER_FAIL))
                            .muzzleFlash(MuzzleFlashType.TRIANGLE, MuzzleFlashType.SMALL_STAR)
                            .build())
            .reloadCues(ReloadCueStack.of(
                    new ReloadCue(0.45f, PlayableSound.of(SoundRegistry.AK47_EJECT_MAG, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(1.25f, PlayableSound.of(SoundRegistry.AK47_INSERT_MAG, 1.25f, 0.9f, 1.1f)),
                    new ReloadCue(1.8f, PlayableSound.of(SoundRegistry.AK47_RACK, 1.25f, 0.9f, 1.1f))
            ))
            .equipSound(PlayableSound.of(SoundRegistry.AK47_RACK, 0.75f, 0.9f, 1.1f))
            .animationAdjusters(AnimationAdjuster.HIDE_MAGAZINE_BULLET)
            .build();
}
