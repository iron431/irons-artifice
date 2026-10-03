package io.redspace.irons_artifice.registry;

import com.mojang.serialization.MapCodec;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.particle.BulletTrailParticleType;
import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.client.particle.FairyDustParticleOption;
import io.redspace.irons_artifice.client.particle.FairyDustParticleType;
import io.redspace.irons_artifice.client.particle.MuzzleFlashParticleOption;
import io.redspace.irons_artifice.client.particle.MuzzleFlashParticleType;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IronsArtificeParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, IronsArtifice.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<BlockParticleOption>> BLOCK_IMPACT =
            PARTICLE_TYPES.register("block_impact", () -> new ParticleType<BlockParticleOption>(false) {
                @Override
                public MapCodec<BlockParticleOption> codec() {
                    return BlockParticleOption.codec(this);
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> streamCodec() {
                    return BlockParticleOption.streamCodec(this);
                }
            });
    public static final DeferredHolder<ParticleType<?>, ParticleType<BlockParticleOption>> BLOCK_DUST =
            PARTICLE_TYPES.register("block_dust", () -> new ParticleType<BlockParticleOption>(false) {
                @Override
                public MapCodec<BlockParticleOption> codec() {
                    return BlockParticleOption.codec(this);
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> streamCodec() {
                    return BlockParticleOption.streamCodec(this);
                }
            });

    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> BULLET_TRAIL =
            PARTICLE_TYPES.register("bullet_trail", () -> new BulletTrailParticleType(false));


    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> BULLET_IMPACT =
            PARTICLE_TYPES.register("bullet_impact", () -> new BulletTrailParticleType(false));

    public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_LARGE =
            PARTICLE_TYPES.register("muzzle_flash_large", () -> new MuzzleFlashParticleType(false));
    public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_TRIANGLE =
            PARTICLE_TYPES.register("muzzle_flash_triangle", () -> new MuzzleFlashParticleType(false));
    public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_SMALL_STAR =
            PARTICLE_TYPES.register("muzzle_flash_small_star", () -> new MuzzleFlashParticleType(false));

    public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> EXPLOSION_96 =
            PARTICLE_TYPES.register("explosion", () -> new MuzzleFlashParticleType(false));

    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> LIGHTNING_TRAIL =
            PARTICLE_TYPES.register("lightning_trail", () -> new BulletTrailParticleType(false));

    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> FIRE_TRAIL =
            PARTICLE_TYPES.register("fire_trail", () -> new BulletTrailParticleType(false));

    public static final DeferredHolder<ParticleType<?>, ParticleType<FairyDustParticleOption>> FAIRY_DUST =
            PARTICLE_TYPES.register("fairy_dust", () -> new FairyDustParticleType(false));

    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> SPLASH =
            PARTICLE_TYPES.register("splash", () -> new ParticleType<ColorParticleOption>(false) {
                @Override
                public MapCodec<ColorParticleOption> codec() {
                    return ColorParticleOption.codec(this);
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                    return ColorParticleOption.streamCodec(this);
                }
            });

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }
}
