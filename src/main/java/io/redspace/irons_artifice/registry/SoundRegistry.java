package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class SoundRegistry {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, IronsArtifice.MODID);

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> BULLET_IMPACT_GENERIC = registerSoundEvent("entity.bullet.impact.generic");
    public static final DeferredHolder<SoundEvent, SoundEvent> BULLET_IMPACT_RICOCHET = registerSoundEvent("entity.bullet.impact.ricochet");
    public static final DeferredHolder<SoundEvent, SoundEvent> BULLET_ECHO_GENERIC = registerSoundEvent("entity.bullet.echo.generic");
    public static final DeferredHolder<SoundEvent, SoundEvent> BULLET_ECHO_GENERIC_PISTOL = registerSoundEvent("entity.bullet.echo.generic_pistol");
    public static final DeferredHolder<SoundEvent, SoundEvent> BULLET_ECHO_MUZZLELOADER = registerSoundEvent("entity.bullet.echo.muzzleloader");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVOLVER_SHOOT = registerSoundEvent("item.example_revolver.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> COCK_HAMMER = registerSoundEvent("item.generic.cock_hammer");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTNING_ACCENT_SHOOT = registerSoundEvent("modifier.chain_lightning.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTNING_ACCENT_IMPACT = registerSoundEvent("modifier.chain_lightning.impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROZEN_JACKET_ACCENT_SHOOT = registerSoundEvent("modifier.frozen_jacket.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEATHER_ACCENT = registerSoundEvent("item.generic.leather_accent");
    public static final DeferredHolder<SoundEvent, SoundEvent> INSTANT_RELOAD = registerSoundEvent("item.cowboy_hat.instant_reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> INFINITY_BULLET = registerSoundEvent("modifier.enchanted_bullet.proc");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIRATE_AMBUSH = registerSoundEvent("entity.drowned_pirate.ambush");

    public static final DeferredHolder<SoundEvent, SoundEvent> FLINTLOCK_SHOOT = registerSoundEvent("item.flintlock.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLINTLOCK_EQUIP = registerSoundEvent("item.flintlock.equip");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLINTLOCK_RELOAD_INSERT_BULLET = registerSoundEvent("item.flintlock.reload.insert_bullet");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLINTLOCK_RELOAD_PACK_BULLET = registerSoundEvent("item.flintlock.reload.pack_bullet");

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSKET_SHOOT = registerSoundEvent("item.musket.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSKET_EQUIP = registerSoundEvent("item.musket.equip");

    public static final DeferredHolder<SoundEvent, SoundEvent> ARQUEBUS_EQUIP = registerSoundEvent("item.arquebus.equip");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARQUEBUS_OPEN_BREECH = registerSoundEvent("item.arquebus.open_breech");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARQUEBUS_CLOSE_BREECH = registerSoundEvent("item.arquebus.close_breech");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARQUEBUS_LOAD = registerSoundEvent("item.arquebus.load_breech");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARQUEBUS_SHOOT = registerSoundEvent("item.arquebus.shoot");

    public static final DeferredHolder<SoundEvent, SoundEvent> BLACKPOWDER_REVOLVER_RELOAD_START = registerSoundEvent("item.blackpowder_revolver.reload.start");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLACKPOWDER_REVOLVER_RELOAD_MID = registerSoundEvent("item.blackpowder_revolver.reload.mid");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLACKPOWDER_REVOLVER_RELOAD_END = registerSoundEvent("item.blackpowder_revolver.reload.end");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLACKPOWDER_REVOLVER_SHOOT = registerSoundEvent("item.blackpowder_revolver.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLACKPOWDER_REVOLVER_EQUIP = registerSoundEvent("item.blackpowder_revolver.equip");

    public static final DeferredHolder<SoundEvent, SoundEvent> SIX_SHOOTER_SHOOT = registerSoundEvent("item.six_shooter.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIX_SHOOTER_EQUIP = registerSoundEvent("item.six_shooter.equip");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIX_SHOOTER_HOLSTER = registerSoundEvent("item.six_shooter.holster");

    public static final DeferredHolder<SoundEvent, SoundEvent> BLUNDERBUSS_RELOAD_OPEN = registerSoundEvent("item.blunderbuss.reload.break_action_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLUNDERBUSS_RELOAD_LOAD = registerSoundEvent("item.blunderbuss.reload.mid");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLUNDERBUSS_RELOAD_CLOSE = registerSoundEvent("item.blunderbuss.reload.break_action_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLUNDERBUSS_SHOOT = registerSoundEvent("item.blunderbuss.shoot");

    public static final DeferredHolder<SoundEvent, SoundEvent> CLOCKWORK_RIFLE_INSERT_MAG = registerSoundEvent("item.clockwork_rifle.reload.insert_mag");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOCKWORK_RIFLE_EJECT_MAG = registerSoundEvent("item.clockwork_rifle.reload.eject_mag");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOCKWORK_RIFLE_SHOOT = registerSoundEvent("item.clockwork_rifle.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOCKWORK_RIFLE_EQUIP = registerSoundEvent("item.clockwork_rifle.equip");

    public static DeferredHolder<SoundEvent, SoundEvent> AK47_SHOOT = registerSoundEvent("item.ak47.shoot");
    public static DeferredHolder<SoundEvent, SoundEvent> AK47_INSERT_MAG = registerSoundEvent("item.ak47.reload.insert_mag");
    public static DeferredHolder<SoundEvent, SoundEvent> AK47_EJECT_MAG = registerSoundEvent("item.ak47.reload.eject_mag");
    public static DeferredHolder<SoundEvent, SoundEvent> AK47_RACK = registerSoundEvent("item.ak47.reload.rack");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(IronsArtifice.id(name)));
    }
}
