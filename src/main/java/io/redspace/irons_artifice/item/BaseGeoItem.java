package io.redspace.irons_artifice.item;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.llamalad7.mixinextras.lib.apache.commons.mutable.MutableObject;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BaseGeoItem extends Item implements GeoItem {
    public final MutableObject<GeoRenderProvider> geoRenderProvider = new MutableObject<>();
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public BaseGeoItem(Item.Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(this.geoRenderProvider.getValue());
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
    }

    @Override
    public @NotNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
