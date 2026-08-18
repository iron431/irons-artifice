package io.redspace.irons_artifice;

import io.redspace.irons_artifice.config.ClientConfig;
import io.redspace.irons_artifice.config.ServerConfig;
import io.redspace.irons_artifice.events.CommonSetup;
import io.redspace.irons_artifice.network.PayloadRegistry;
import io.redspace.irons_artifice.registry.DataAttachmentRegistry;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import io.redspace.irons_artifice.registry.MenuRegistry;
import io.redspace.irons_artifice.registry.ParticleRegistry;
import io.redspace.irons_artifice.registry.SoundRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(IronsArtifice.MODID)
public class IronsArtifice {
    public static final String MODID = "irons_artifice";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.irons_artifice"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ItemRegistry.FLINTLOCK_PISTOL.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (var i : ItemRegistry.ITEMS.getEntries()) {
                    output.accept(i.get());
                }
            }).build());

    public IronsArtifice(IEventBus modEventBus, ModContainer modContainer) {
        ItemRegistry.register(modEventBus);
        DataComponentRegistry.register(modEventBus);
        EntityRegistry.register(modEventBus);
        MenuRegistry.register(modEventBus);
        DataAttachmentRegistry.register(modEventBus);
        ParticleRegistry.register(modEventBus);
        SoundRegistry.register(modEventBus);
        modEventBus.addListener(PayloadRegistry::register);
        modEventBus.addListener(CommonSetup::entityAttributes);
        modEventBus.addListener(CommonSetup::buildCreativeTabs);
        CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

}
