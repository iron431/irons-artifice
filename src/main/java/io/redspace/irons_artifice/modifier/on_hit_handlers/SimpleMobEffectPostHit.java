package io.redspace.irons_artifice.modifier.on_hit_handlers;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.modifier.PostHitEffect;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;

public final class SimpleMobEffectPostHit implements PostHitEffect {

    private final Holder<MobEffect> mobEffect;
    private int durationTicks;
    private int amplifier;

    public SimpleMobEffectPostHit(Holder<MobEffect> mobEffect, int durationTicks, int amplifier) {
        this.mobEffect = mobEffect;
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
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (mobEffect.value().getCategory() == MobEffectCategory.HARMFUL && !Utils.canHarm(bullet.getOwner(), entity)) {
            return;
        }
        MobEffectInstance existing = living.getEffect(mobEffect);
        int duration = Math.max(durationTicks, (existing != null ? existing.getDuration() : 0));
        int amp = Math.max(amplifier, existing != null ? existing.getAmplifier() : 0);
        living.addEffect(new MobEffectInstance(mobEffect, duration, amp), bullet.getOwner());
    }
}
