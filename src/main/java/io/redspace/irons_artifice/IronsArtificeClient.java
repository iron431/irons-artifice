package io.redspace.irons_artifice;

import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.client.Keybinds;
import io.redspace.irons_artifice.client.entity.ChainEntityRenderer;
import io.redspace.irons_artifice.client.entity.GunslingerRenderer;
import io.redspace.irons_artifice.client.entity.IllificerRenderer;
import io.redspace.irons_artifice.client.gui.GunPreviewRenderState;
import io.redspace.irons_artifice.client.gui.GunPreviewRenderer;
import io.redspace.irons_artifice.client.gun.AttachmentGeoRenderer;
import io.redspace.irons_artifice.client.gun.AttachmentRenderableRegistry;
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
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.menu.GunModifierScreen;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import io.redspace.irons_artifice.registry.MenuRegistry;
import io.redspace.irons_artifice.registry.ParticleRegistry;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.google.common.base.Suppliers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mod(value = IronsArtifice.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
public class IronsArtificeClient {
    public IronsArtificeClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, IronsArtifice.id("ammo_hud"), AmmoCountHudOverlay::render);
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, IronsArtifice.id("gun_scope"), GunScopeOverlay::render);
    }

    @SubscribeEvent
    public static void registerPictureInPictureRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(GunPreviewRenderState.class, GunPreviewRenderer::new);
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        for (GunItem gun : guns()) {
            Identifier modelId = BuiltInRegistries.ITEM.getKey(gun);
            gun.geoRenderProvider.setValue(new GeoRenderProvider() {
                private final Supplier<GeoItemRenderer<GunItem>> renderer =
                        Suppliers.memoize(() -> new GunInHandRenderer(new DefaultedItemGeoModel<>(modelId)));

                @Override
                public @Nullable GeoItemRenderer<GunItem> getGeoItemRenderer() {
                    return this.renderer.get();
                }
            });
        }////
        AttachmentRenderableRegistry.register(
                IronsArtifice.id("spyglass_scope"),
                new AttachmentGeoRenderer(new SimpleItemGeoModel<>(IronsArtifice.MODID,
                        "spyglass_scope",
                        "model/spyglass_scope",
                        "empty"))
        );
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.GUN_MENU.get(), GunModifierScreen::new);
    }

    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.BULLET.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CHAIN.get(), ChainEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GUNSLINGER.get(), GunslingerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ILLIFICER.get(), IllificerRenderer::new);
    }

    @SubscribeEvent
    static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(Keybinds.OPEN_MODIFIER_MENU);
        event.register(Keybinds.RELOAD);
    }

    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
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
    public static void onLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientHelper.reset();
    }

    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions pistolPose = armPoseExtension(GunArmPoses.PISTOL.getValue());
        IClientItemExtensions riflePose = armPoseExtension(GunArmPoses.RIFLE.getValue());

        List<Item> pistols = new ArrayList<>();
        List<Item> rifles = new ArrayList<>();
        for (GunItem gun : guns()) {
            if (gun.getGun().armPoseKind() == ArmPoseKind.PISTOL) {
                pistols.add(gun);
            } else {
                rifles.add(gun);
            }
        }
        if (!pistols.isEmpty()) {
            event.registerItem(pistolPose, pistols.toArray(Item[]::new));
        }
        if (!rifles.isEmpty()) {
            event.registerItem(riflePose, rifles.toArray(Item[]::new));
        }
    }

    private static IClientItemExtensions armPoseExtension(HumanoidModel.ArmPose pose) {
        return new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return pose;
            }
        };
    }

    private static List<GunItem> guns() {
        List<GunItem> guns = new ArrayList<>();
        for (DeferredHolder<Item, ? extends Item> holder : ItemRegistry.ITEMS.getEntries()) {
            if (holder.get() instanceof GunItem gunItem) {
                guns.add(gunItem);
            }
        }
        return guns;
    }
}
