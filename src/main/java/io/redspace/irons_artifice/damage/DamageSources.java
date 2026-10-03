package io.redspace.irons_artifice.damage;

import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class DamageSources {
    public static final ResourceKey<DamageType> BULLET_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, IronsArtifice.id("bullet"));

    public static RandomizableDamageSource bullet(Level level, Entity bullet, @Nullable Entity owner) {
        return bullet(level.registryAccess(), bullet, owner);
    }

    public static RandomizableDamageSource bullet(RegistryAccess registryAccess, Entity bullet, @Nullable Entity owner) {
        return new RandomizableDamageSource(
                registryAccess.getOrThrow(BULLET_DAMAGE_TYPE),
                bullet,
                owner
        ).setDeathMessages(
                "death.attack.irons_artifice.bullet",
                "death.attack.irons_artifice.bullet.gunned_down",
                "death.attack.irons_artifice.bullet.lead",
                "death.attack.irons_artifice.bullet.ventilated",
                "death.attack.irons_artifice.bullet.cut_down",
                "death.attack.irons_artifice.bullet.stopped_cold",
                "death.attack.irons_artifice.bullet.bullet"
        );
    }
}
