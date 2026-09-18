package io.redspace.irons_artifice.client.armor;

import com.google.common.base.Suppliers;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.BaseGeoArmorItem;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.function.Supplier;

/**
 * Client-only holder for the hats' armour renderers, the counterpart of the per-gun render providers
 * {@code IronsArtificeClient.registerRenderers} installs.
 * <p>
 * A {@link GeoArmorRenderer} extends {@code HumanoidModel}, so naming one anywhere the item class can
 * reach -- an anonymous {@link GeoRenderProvider} in the item's constructor, say -- makes a dedicated
 * server load a client class while it is registering items, and the dist cleaner kills mod loading.
 * <p>
 * Armour at this GeckoLib version is reached through {@link GeoRenderProvider} rather than
 * {@code IClientItemExtensions}: GeckoLib's own armour layer looks the renderer up through the provider
 * and calls {@code prepForRender} on it first, and {@code GeoArmorRenderer#renderToBuffer} logs an error
 * and draws nothing for a renderer it was handed any other way.
 */
@EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
public class ArmorRenderProviders {

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        install((BaseGeoArmorItem) ItemRegistry.COWBOY_HAT.get(), "cowboy_hat");
        install((BaseGeoArmorItem) ItemRegistry.TRICORNE_HAT.get(), "tricorne");
    }

    private static void install(BaseGeoArmorItem item, String name) {
        item.geoRenderProvider.setValue(new GeoRenderProvider() {
            private final Supplier<GeoArmorRenderer<BaseGeoArmorItem>> renderer =
                    Suppliers.memoize(() -> new GeoArmorRenderer<>(new GenericArmorModel<BaseGeoArmorItem>(name)));

            @Override
            public <T extends LivingEntity> @Nullable HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot, @Nullable HumanoidModel<T> original) {
                return this.renderer.get();
            }
        });
    }
}
