package io.redspace.irons_artifice.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface BulletDamageSource {
    DamageSource create(Level level, Entity bullet, @Nullable Entity owner);
}
