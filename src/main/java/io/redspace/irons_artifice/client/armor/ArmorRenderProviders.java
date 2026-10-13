package io.redspace.irons_artifice.client.armor;

import com.google.common.base.Suppliers;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.BaseGeoArmorItem;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.EntityRenderersEvent;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Client-only holder for the hats' armor renderers. A {@link GeoArmorRenderer} extends {@code HumanoidModel}, so
 * naming one where the item class can reach it makes a dedicated server load a client class while registering
 * items, and the dist cleaner kills mod loading.
 */
@Mod.EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ArmorRenderProviders {
    private static final Map<Item, Supplier<GeoArmorRenderer<BaseGeoArmorItem>>> RENDERERS = new HashMap<>();

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        install(ItemRegistry.COWBOY_HAT.get(), "cowboy_hat");
        install(ItemRegistry.TRICORNE_HAT.get(), "tricorne");
    }

    private static void install(Item item, String name) {
        RENDERERS.put(item, Suppliers.memoize(() -> new GeoArmorRenderer<>(new GenericArmorModel<BaseGeoArmorItem>(name))));
    }

    /**
     * Runs inside the item constructor, before the item is registered or its renderer installed, so the renderer is
     * looked up on each render.
     */
    public static IClientItemExtensions extension(BaseGeoArmorItem item) {
        return new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                Supplier<GeoArmorRenderer<BaseGeoArmorItem>> renderer = RENDERERS.get(item);
                if (renderer == null) {
                    return original;
                }
                GeoArmorRenderer<BaseGeoArmorItem> armorRenderer = renderer.get();
                armorRenderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return armorRenderer;
            }
        };
    }
}
