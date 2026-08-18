package io.redspace.irons_artifice.modifier.on_hit_handlers;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.modifier.PostHitEffect;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;

public class PoisonPostHit implements PostHitEffect {
    private int durationTicks;
    private int amplifier;

    public PoisonPostHit(int durationTicks, int amplifier) {
        this.durationTicks = durationTicks;
        this.amplifier = amplifier;
    }

    public void addDuration(int ticks) {
        this.durationTicks += ticks;
    }

    public void addAmplifier(int amount) {
        this.amplifier += amount;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public int getAmplifier() {
        return amplifier;
    }

    @Override
    public void postHit(ServerLevel level, Bullet bullet, HitResult hitResult, Entity entity) {
        if (!(entity instanceof LivingEntity living) || !Utils.canHarm(bullet.getOwner(), entity)) {
            return;
        }
        MobEffectInstance existing = living.getEffect(MobEffects.POISON);
        int duration = durationTicks + (existing != null ? existing.getDuration() : 0);
        int amp = existing != null ? Math.max(existing.getAmplifier(), amplifier) : amplifier;
        living.addEffect(new MobEffectInstance(MobEffects.POISON, Math.min(duration, 20 * 30), Math.max(0, amp)), bullet.getOwner());
    }
}
