package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.particle.BulletTrailParticleType;
import io.redspace.irons_artifice.client.particle.ColorParticleOption;
import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.client.particle.FairyDustParticleOption;
import io.redspace.irons_artifice.client.particle.FairyDustParticleType;
import io.redspace.irons_artifice.client.particle.MuzzleFlashParticleOption;
import io.redspace.irons_artifice.client.particle.MuzzleFlashParticleType;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, IronsArtifice.MODID);

    public static final RegistryObject<ParticleType<BlockParticleOption>> BLOCK_IMPACT =
            PARTICLE_TYPES.register("block_impact", () -> new ParticleType<BlockParticleOption>(false, BlockParticleOption.DESERIALIZER) {
                @Override
                public Codec<BlockParticleOption> codec() {
                    return BlockParticleOption.codec(this);
                }
            });
    public static final RegistryObject<ParticleType<BlockParticleOption>> BLOCK_DUST =
            PARTICLE_TYPES.register("block_dust", () -> new ParticleType<BlockParticleOption>(false, BlockParticleOption.DESERIALIZER) {
                @Override
                public Codec<BlockParticleOption> codec() {
                    return BlockParticleOption.codec(this);
                }
            });

    public static final RegistryObject<ParticleType<ColorTransitionParticleOption>> BULLET_TRAIL =
            PARTICLE_TYPES.register("bullet_trail", () -> new BulletTrailParticleType(false));


    public static final RegistryObject<ParticleType<ColorTransitionParticleOption>> BULLET_IMPACT =
            PARTICLE_TYPES.register("bullet_impact", () -> new BulletTrailParticleType(false));

    public static final RegistryObject<ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_LARGE =
            PARTICLE_TYPES.register("muzzle_flash_large", () -> new MuzzleFlashParticleType(false));
    public static final RegistryObject<ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_TRIANGLE =
            PARTICLE_TYPES.register("muzzle_flash_triangle", () -> new MuzzleFlashParticleType(false));
    public static final RegistryObject<ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_SMALL_STAR =
            PARTICLE_TYPES.register("muzzle_flash_small_star", () -> new MuzzleFlashParticleType(false));

    public static final RegistryObject<ParticleType<MuzzleFlashParticleOption>> EXPLOSION_96 =
            PARTICLE_TYPES.register("explosion", () -> new MuzzleFlashParticleType(false));

    public static final RegistryObject<ParticleType<ColorTransitionParticleOption>> LIGHTNING_TRAIL =
            PARTICLE_TYPES.register("lightning_trail", () -> new BulletTrailParticleType(false));

    public static final RegistryObject<ParticleType<ColorTransitionParticleOption>> FIRE_TRAIL =
            PARTICLE_TYPES.register("fire_trail", () -> new BulletTrailParticleType(false));

    public static final RegistryObject<ParticleType<FairyDustParticleOption>> FAIRY_DUST =
            PARTICLE_TYPES.register("fairy_dust", () -> new FairyDustParticleType(false));

    public static final RegistryObject<ParticleType<ColorParticleOption>> SPLASH =
            PARTICLE_TYPES.register("splash", () -> new ParticleType<ColorParticleOption>(false, ColorParticleOption.DESERIALIZER) {
                @Override
                public Codec<ColorParticleOption> codec() {
                    return ColorParticleOption.codec(this);
                }
            });

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }
}
