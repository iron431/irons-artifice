package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.menu.GunModifierMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, IronsArtifice.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GunModifierMenu>> GUN_MENU = MENUS.register(
            "gun",
            () -> new MenuType<>(GunModifierMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    private MenuRegistry() {}

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
