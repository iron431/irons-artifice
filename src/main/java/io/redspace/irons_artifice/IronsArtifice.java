package io.redspace.irons_artifice;

import io.redspace.irons_artifice.config.ClientConfig;
import io.redspace.irons_artifice.config.ServerConfig;
import io.redspace.irons_artifice.events.CommonSetup;
import io.redspace.irons_artifice.gametest.GameTestReport;
import io.redspace.irons_artifice.network.PayloadRegistry;
import io.redspace.irons_artifice.registry.CriterionRegistry;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ExtendedRaiderTypes;
import io.redspace.irons_artifice.registry.ItemRegistry;
import io.redspace.irons_artifice.registry.MenuRegistry;
import io.redspace.irons_artifice.registry.ParticleRegistry;
import io.redspace.irons_artifice.registry.SoundRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(IronsArtifice.MODID)
public class IronsArtifice {
    public static final String MODID = "irons_artifice";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.irons_artifice"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> ItemRegistry.FLINTLOCK_PISTOL.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (var i : ItemRegistry.ITEMS.getEntries()) {
                    output.accept(i.get());
                }
            }).build());

    public IronsArtifice(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        CriterionRegistry.register();
        ItemRegistry.register(modEventBus);
        EntityRegistry.register(modEventBus);
        MenuRegistry.register(modEventBus);
        ParticleRegistry.register(modEventBus);
        SoundRegistry.register(modEventBus);
        modEventBus.addListener(GameTestReport::onRegisterGameTests);
        PayloadRegistry.register();
        modEventBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(ExtendedRaiderTypes::register));
        modEventBus.addListener(CommonSetup::entityAttributes);
        modEventBus.addListener(CommonSetup::buildCreativeTabs);
        CREATIVE_MODE_TABS.register(modEventBus);

        ModContainer container = context.getContainer();
        container.addConfig(new ModConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, container));
        container.addConfig(new ModConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, container));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

}
