package io.redspace.irons_artifice;

import io.redspace.irons_artifice.client.BayonetAnimations;
import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.client.Keybinds;
import io.redspace.irons_artifice.client.entity.ChainEntityRenderer;
import io.redspace.irons_artifice.client.entity.GunslingerRenderer;
import io.redspace.irons_artifice.client.entity.SoulfireCoinRenderer;
import io.redspace.irons_artifice.client.entity.illificer.IllificerRenderer;
import io.redspace.irons_artifice.client.gui.ClientBulletContainerItemTooltip;
import io.redspace.irons_artifice.client.gun.AttachmentGeoRenderer;
import io.redspace.irons_artifice.client.gun.AttachmentRenderableRegistry;
import io.redspace.irons_artifice.client.gun.GunGeoModel;
import io.redspace.irons_artifice.client.gun.GunInHandRenderer;
import io.redspace.irons_artifice.client.gun.SimpleItemGeoModel;
import io.redspace.irons_artifice.client.gui.AmmoCountHudOverlay;
import io.redspace.irons_artifice.client.gui.GunScopeOverlay;
import io.redspace.irons_artifice.client.particle.BlockDustParticle;
import io.redspace.irons_artifice.client.particle.BulletImpactParticle;
import io.redspace.irons_artifice.client.particle.BulletTrailParticle;
import io.redspace.irons_artifice.client.particle.FairyDustParticle;
import io.redspace.irons_artifice.client.particle.ImpactBlockParticle;
import io.redspace.irons_artifice.client.particle.LightningTrailEmitterParticle;
import io.redspace.irons_artifice.client.particle.MuzzleFlashParticle;
import io.redspace.irons_artifice.client.particle.SplashParticle;
import io.redspace.irons_artifice.client.gun.GunArmPoses;
import io.redspace.irons_artifice.client.particle.TintedExplosionParticle;
import io.redspace.irons_artifice.gun.ArmPoseKind;
import io.redspace.irons_artifice.item.BulletContainerTooltip;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.menu.GunModifierScreen;
import io.redspace.irons_artifice.modifier.modifiers.BayonetAttachmentModifier;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.MenuRegistry;
import io.redspace.irons_artifice.registry.ParticleRegistry;
import io.redspace.irons_artifice.utils.MinecraftInstanceHelper;
import io.redspace.ironslib.kinetic_weapon.client.KineticAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import com.google.common.base.Suppliers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class IronsArtificeClient {
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "ammo_hud", AmmoCountHudOverlay::render);
        event.registerAbove(VanillaGuiOverlay.PORTAL.id(), "gun_scope", GunScopeOverlay::render);
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        AttachmentRenderableRegistry.register(
                IronsArtifice.id("spyglass_scope"),
                new AttachmentGeoRenderer(new SimpleItemGeoModel<>(IronsArtifice.MODID,
                        "spyglass_scope",
                        "model/spyglass_scope",
                        "empty"))
        );
        AttachmentRenderableRegistry.register(
                IronsArtifice.id("iron_bayonet"),
                new AttachmentGeoRenderer(new SimpleItemGeoModel<>(IronsArtifice.MODID,
                        "iron_bayonet",
                        "model/iron_bayonet",
                        "empty"))
        );
        AttachmentRenderableRegistry.register(
                IronsArtifice.id("suppressor"),
                new AttachmentGeoRenderer(new SimpleItemGeoModel<>(IronsArtifice.MODID,
                        "suppressor",
                        "model/suppressor",
                        "empty"))
        );
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        KineticAnimations.register(BayonetAttachmentModifier.BAYONET_ANIMATION, BayonetAnimations.INSTANCE);
        MinecraftInstanceHelper.instance = () -> Minecraft.getInstance().player;
        event.enqueueWork(() -> {
            GunArmPoses.register();
            MenuScreens.register(MenuRegistry.GUN_MENU.get(), GunModifierScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.BULLET.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CHAIN.get(), ChainEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SOUL.get(), SoulfireCoinRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GUNSLINGER.get(), GunslingerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ILLIFICER.get(), IllificerRenderer::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(Keybinds.OPEN_MODIFIER_MENU);
        event.register(Keybinds.RELOAD);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpecial(ParticleRegistry.BLOCK_IMPACT.get(), new ImpactBlockParticle.Provider());
        event.registerSpriteSet(ParticleRegistry.BLOCK_DUST.get(), BlockDustParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BULLET_TRAIL.get(), BulletTrailParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BULLET_IMPACT.get(), BulletImpactParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.MUZZLE_FLASH_LARGE.get(), MuzzleFlashParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.MUZZLE_FLASH_TRIANGLE.get(), MuzzleFlashParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.MUZZLE_FLASH_SMALL_STAR.get(), MuzzleFlashParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.FAIRY_DUST.get(), FairyDustParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.LIGHTNING_TRAIL.get(), LightningTrailEmitterParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.EXPLOSION_96.get(), TintedExplosionParticle.Provider::new);

        event.registerSpecial(ParticleRegistry.SPLASH.get(), new SplashParticle.Provider());
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(BulletContainerTooltip.class, tooltip -> new ClientBulletContainerItemTooltip(tooltip.contents()));
    }

    /**
     * Runs inside the {@link GunItem} constructor, before the profile and registry key exist, so both are read on
     * first use.
     */
    public static IClientItemExtensions gunExtension(GunItem gun) {
        Supplier<GunInHandRenderer> renderer = Suppliers.memoize(() -> {
            ResourceLocation modelId = BuiltInRegistries.ITEM.getKey(gun);
            return new GunInHandRenderer(new GunGeoModel(modelId));
        });
        return new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return gun.getGun().armPoseKind() == ArmPoseKind.PISTOL
                        ? GunArmPoses.PISTOL
                        : GunArmPoses.RIFLE;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer.get();
            }
        };
    }

    @Mod.EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ClientHelper.reset();
        }
    }
}
