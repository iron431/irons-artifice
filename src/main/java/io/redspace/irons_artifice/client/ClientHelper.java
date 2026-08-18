package io.redspace.irons_artifice.client;

import com.geckolib.animation.AnimationController;
import com.geckolib.constant.DataTickets;
import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.client.particle.FairyDustParticleOption;
import io.redspace.irons_artifice.client.particle.ITrailParticle;
import io.redspace.irons_artifice.client.sounds.EquipSoundInstance;
import io.redspace.irons_artifice.client.sounds.GunShotSoundInstance;
import io.redspace.irons_artifice.client.sounds.GunShotSoundSettings;
import io.redspace.irons_artifice.data.ParticleStack;
import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.data.HandOccupancy;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.network.packets.ClientboundBulletImpactPacket;
import io.redspace.irons_artifice.network.packets.ClientboundBulletTrailPacket;
import io.redspace.irons_artifice.network.packets.ClientboundCancelGunAnimationPacket;
import io.redspace.irons_artifice.network.packets.ClientboundEquipSoundPacket;
import io.redspace.irons_artifice.network.packets.ClientboundGunAnimationPacket;
import io.redspace.irons_artifice.network.packets.ClientboundGunshotSoundPacket;
import io.redspace.irons_artifice.network.packets.ClientboundLocalSoundPacket;
import io.redspace.irons_artifice.network.packets.ClientboundMuzzleFlashPacket;
import io.redspace.irons_artifice.registry.ParticleRegistry;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ClientHelper {

    private static long localDryFireTime;

    public static void handleLocalDryFire(Player player, PlayableSound sound) {
        if (player.level().getGameTime() > localDryFireTime + 8) {
            localDryFireTime = player.level().getGameTime();
            player.level().playSound(player, player.getX(), player.getY(), player.getZ(), sound.soundEventHolder(), player.getSoundSource(), sound.volume(), sound.samplePitch(player.getRandom()));
        }
    }

    public static void handleBulletTrail(ClientboundBulletTrailPacket msg) {
        ClientLevel level = Minecraft.getInstance().level;
        List<ParticleOptions> particles = msg.particles();
        List<ParticleStack.ParticleAccent> accents = msg.accents();
        if (level == null || particles.isEmpty()) {
            return;
        }

        Vec3 from = msg.from();
        Vec3 to = msg.to();
        double distance = from.distanceTo(to);
        int steps = (int) (distance * Bullet.TRAIL_DENSITY);
        if (steps <= 0) {
            return;
        }

        Vec3 dir = to.subtract(from).normalize();
        float speed = 12 / ((float) Bullet.TRAIL_DENSITY * 15f);
        for (int i = 0; i < steps; i++) {
            float f = (i + 1.0F) / steps;
            Vec3 pos = from.lerp(to, f);
            ParticleOptions particle = particles.get(i % particles.size());
            if (particle.getType() instanceof ITrailParticle trailParticle) {
                particle = trailParticle.applyTrailInterpolation(particle, (1 - f));
            }
            if (particle instanceof FairyDustParticleOption fairy) {
                particle = new FairyDustParticleOption(fairy.getType(), fairy.getPhase(), fairy.getRadius(), dir);
            }
            level.addAlwaysVisibleParticle(particle, true,
                    pos.x, pos.y, pos.z,
                    dir.x * speed, dir.y * speed, dir.z * speed);
            for (ParticleStack.ParticleAccent accent : accents) {
                if (level.getRandom().nextDouble() < accent.chance()) {
                    level.addAlwaysVisibleParticle(accent.options(), true,
                            pos.x, pos.y, pos.z,
                            dir.x * speed, dir.y * speed, dir.z * speed);
                }
            }
        }
    }

    public static void handleBulletImpact(ClientboundBulletImpactPacket msg) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Vec3 direction = msg.deltaMovement().normalize();
        Vec3 pos = msg.position().subtract(direction.scale(0.05));
        float speed = (float) msg.deltaMovement().length();
        Vec3 reflected = Utils.reflect(direction, msg.normal());
        level.addAlwaysVisibleParticle(new ColorTransitionParticleOption(
                ParticleRegistry.BULLET_IMPACT.get(), 0x862900, 0x0F0600, 0.125f, 0f, 1f, 0.75f, 0.35f, 0.35f, 0
        ), true, pos.x, pos.y, pos.z, direction.x, direction.y, direction.z);
        BlockPos impactedBlock = BlockPos.containing(pos.add(direction.scale(0.1)));
        BlockState blockState = level.getBlockState(impactedBlock);
        float particleSpeed = (10 + speed) * 0.02f;
        int particleCount = 3 + (int) (msg.damage() / 2);
        for (int i = 0; i < particleCount; i++) {
            Vec3 motion = new Vec3(level.getRandom().nextFloat() * 2 - 1, level.getRandom().nextFloat() * 2 - 1, level.getRandom().nextFloat() * 2 - 1).subtract(direction.scale(0.25)).normalize();
            motion = motion.scale(particleSpeed);
            level.addParticle(new BlockParticleOption(ParticleRegistry.BLOCK_IMPACT.get(), blockState), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
            motion = motion.scale(0.25);
            level.addParticle(new BlockParticleOption(ParticleRegistry.BLOCK_DUST.get(), blockState), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        }
        for (int i = 0; i < particleCount; i++) {
            Vec3 motion = new Vec3(level.getRandom().nextFloat() * 2 - 1, level.getRandom().nextFloat() * 2 - 1, level.getRandom().nextFloat() * 2 - 1)
                    .add(reflected.scale(3)).normalize();
            motion = motion.scale(particleSpeed * 2);
            level.addParticle(new BlockParticleOption(ParticleRegistry.BLOCK_IMPACT.get(), blockState), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        }
    }

    public static void handleMuzzleFlash(ClientboundMuzzleFlashPacket msg) {
        ClientLevel level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if (level == null || player == null) {
            return;
        }
        Vec3 offset = msg.offset();
        if (msg.entityId() == player.getId() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            double length = offset.length();
            float direction = player.getMainArm() == HumanoidArm.LEFT ? -1.0F : 1.0F;
            offset = offset.scale(1 / length).scale(Math.max(1.25, 0.75 * length));
            offset = offset.add(player.getForward().cross(new Vec3(0, 1, 0))
                    .scale(0.5 * direction));
        }
        Vec3 pos = msg.position().add(offset);
        Vec3 random = new Vec3(level.getRandom().nextDouble() - 0.5, level.getRandom().nextDouble() - 0.5, level.getRandom().nextDouble() - 0.5).scale(2).scale(0.02);
        Vec3 motion = msg.entityMotion().scale(0.5).add(random);
        if (level.isFluidAtPosition(BlockPos.containing(pos), s -> s.is(FluidTags.WATER))) {
            //bubbles
            for (int i = 0; i < 40; i++) {
                random = new Vec3(level.getRandom().nextDouble() - 0.5, level.getRandom().nextDouble() - 0.5, level.getRandom().nextDouble() - 0.5).scale(2).scale(1.75);
                level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE, false, pos.x, pos.y, pos.z, random.x, random.y, random.z);
            }
        } else {
            level.addAlwaysVisibleParticle(msg.particle(), true, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        }
    }

    public static void handleGunAnimationPacket(ClientboundGunAnimationPacket msg) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(msg.entityId());
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        ItemStack stack = livingEntity.getItemInHand(msg.interactionHand());
        if (!(stack.getItem() instanceof GunItem gun)) {
            return;
        }
        playClientGunAnimation(gun, msg.instanceId(), msg.animName(), msg.speed(), msg.offsetSeconds(), msg.skipAtSeconds(), msg.skipToSeconds());
    }

    public static void playClientGunAnimation(GunItem gun, long instanceId, String animName, double speed, double offsetSeconds, double skipAtSeconds, double skipToSeconds) {
        var manager = gun.getAnimatableInstanceCache().getManagerForId(instanceId);
        manager.setAnimatableData(DataTickets.ITEM_RENDER_PERSPECTIVE, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        AnimationController<?> controller = gun.getAnimatableInstanceCache().getManagerForId(instanceId).getAnimationControllers().get(GunItem.TRIGGERED_ANIMATION_CONTROLLER);
        if (controller == null) {
            return;
        }
        controller.triggerAnimation(animName);
        controller.setAnimationSpeed(speed);
        // IMPORTANT: set even if zero (controller workaround doesn't handle context-free transitions)
        controller.setTimelineTime(offsetSeconds);
        gun.configureActionTimelineSkip(instanceId, skipAtSeconds, skipToSeconds);
    }

    public static void handleCancelGunAnimationPacket(ClientboundCancelGunAnimationPacket msg) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(msg.entityId());
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        ItemStack stack = livingEntity.getItemInHand(msg.interactionHand());
        if (!(stack.getItem() instanceof GunItem gun)) {
            return;
        }
        cancelClientGunAnimation(gun, msg.instanceId());
    }

    public static void cancelClientGunAnimation(GunItem gun, long instanceId) {
        AnimationController<?> controller = gun.getAnimatableInstanceCache().getManagerForId(instanceId)
                .getAnimationControllers().get(GunItem.TRIGGERED_ANIMATION_CONTROLLER);
        if (controller == null) {
            return;
        }
        controller.stopTriggeredAnimation();
        controller.reset();
    }

    public static void handleGunshotSound(ClientboundGunshotSoundPacket msg) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Vec3 pos = new Vec3(msg.x(), msg.y(), msg.z());
        RandomSource random = RandomSource.create();
        for (GunShotSoundSettings settings : msg.sounds()) {
            var instance = new GunShotSoundInstance(settings, msg.source(), random, pos);
            if (instance.getDelay() > 0) {
                Minecraft.getInstance().getSoundManager().playDelayed(instance, instance.getDelay());
            } else {
                Minecraft.getInstance().getSoundManager().play(instance);
            }
        }
    }

    public static void handleLocalSound(ClientboundLocalSoundPacket msg) {
        PlayableSound sound = msg.sound();
        RandomSource random = SoundInstance.createUnseededRandom();
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                sound.soundEventHolder().value().location(),
                msg.source(),
                sound.volume(),
                sound.samplePitch(random),
                random,
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0,
                0.0,
                0.0,
                true
        ));
    }

    public static void handleEquipSound(ClientboundEquipSoundPacket payload) {
        if (!(payload.item() instanceof GunItem gunItem)) {
            return;
        }
        PlayableSound sound = gunItem.getGun().equipSound();
        if (sound == null) {
            return;
        }
        int delay = 2;
        RandomSource random = SoundInstance.createUnseededRandom();
        Minecraft.getInstance().getSoundManager().playDelayed(new EquipSoundInstance(
                sound.soundEventHolder().value(),
                payload.source(),
                sound.volume(),
                sound.samplePitch(random),
                random,
                payload.item(),
                delay
        ), delay);
    }

    public static void reset() {
        localDryFireTime = 0;
    }

    public static InteractionHand getHandHoldingTwoHandedGun(LocalPlayer player) {
        if (GunItem.currentOccupancy(player, InteractionHand.MAIN_HAND) == HandOccupancy.BOTH) {
            return InteractionHand.MAIN_HAND;
        }
        if (GunItem.currentOccupancy(player, InteractionHand.OFF_HAND) == HandOccupancy.BOTH) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }
}
