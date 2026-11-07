package io.redspace.irons_artifice.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies the per-gun overrides of base shot components from {@link ServerConfig#GUN_OVERRIDES}, keyed by item id, then component id.
 */
public final class GunConfig {
    private static final String ITEM_KEY = "item";

    private static @Nullable List<? extends UnmodifiableConfig> parsedFrom;
    private static Map<Identifier, List<ComponentOverride>> overridesByItem = Map.of();

    public static void handleConfigModifications(Item item, ShotComponentMap components) {
        if (!ServerConfig.SPEC.isLoaded() || !ServerConfig.GUN_OVERRIDES_ENABLED.get()) {
            return;
        }
        List<ComponentOverride> overrides = overrides().get(BuiltInRegistries.ITEM.getKey(item));
        if (overrides == null) {
            return;
        }
        for (ComponentOverride override : overrides) {
            apply(override, components);
        }
    }

    private static synchronized Map<Identifier, List<ComponentOverride>> overrides() {
        List<? extends UnmodifiableConfig> entries = ServerConfig.GUN_OVERRIDES.get();
        if (entries != parsedFrom) {
            overridesByItem = parse(entries);
            parsedFrom = entries;
        }
        return overridesByItem;
    }

    private static Map<Identifier, List<ComponentOverride>> parse(List<? extends UnmodifiableConfig> entries) {
        Map<Identifier, List<ComponentOverride>> byItem = new HashMap<>();
        for (UnmodifiableConfig entry : entries) {
            try {
                String itemId = (String) entry.valueMap().get(ITEM_KEY);
                Identifier item = Identifier.tryParse(itemId);
                if (item == null) {
                    IronsArtifice.LOGGER.warn("Gun config: \"{}\" is not a valid item id", itemId);
                    continue;
                }
                List<ComponentOverride> overrides = byItem.computeIfAbsent(item, key -> new ArrayList<>());
                entry.valueMap().forEach((key, value) -> {
                    if (key.equals(ITEM_KEY)) {
                        return;
                    }
                    Identifier component = Identifier.tryParse(key);
                    if (component == null) {
                        IronsArtifice.LOGGER.warn("Gun config: \"{}\" on {} is not a valid component id", key, item);
                        return;
                    }
                    overrides.add(new ComponentOverride(component, value));
                });
            } catch (Exception e) {
                IronsArtifice.LOGGER.error("Failed to parse gun config table: {}", e.getMessage());
            }
        }
        byItem.forEach(GunConfig::warnUnmatched);
        return byItem;
    }

    private static void warnUnmatched(Identifier itemId, List<ComponentOverride> overrides) {
        if (!(BuiltInRegistries.ITEM.getOptional(itemId).orElse(null) instanceof GunItem gun)) {
            IronsArtifice.LOGGER.warn("Gun config: {} is not a gun", itemId);
            return;
        }
        ShotComponentMap probe = gun.getGun().baseProfile();
        for (ComponentOverride override : overrides) {
            if (!apply(override, probe)) {
                IronsArtifice.LOGGER.warn("Gun config: \"{} = {}\" is not a valid number or boolean component for {}",
                        override.component(), override.value(), itemId);
            }
        }
    }

    /**
     * @return whether a component of the matching kind was found and set
     */
    @SuppressWarnings("unchecked")
    private static boolean apply(ComponentOverride override, ShotComponentMap components) {
        ComponentType<?> type = components.findType(override.component());
        if (type == null) {
            return false;
        }
        Object current = components.getOrDefault(type);
        if (current instanceof Value value && override.value() instanceof Number number) {
            components.set((ComponentType<Value>) type, value.withBase(number.doubleValue()));
            return true;
        }
        if (current instanceof Boolean && override.value() instanceof Boolean flag) {
            components.set((ComponentType<Boolean>) type, flag);
            return true;
        }
        return false;
    }

    /**
     * Provides empty table for all registered gun items (including addons)
     */
    static List<? extends UnmodifiableConfig> stubPerGun() {
        return BuiltInRegistries.ITEM.stream()
                .filter(GunItem.class::isInstance)
                .map(gun -> {
                    Config entry = Config.inMemory();
                    entry.valueMap().put(ITEM_KEY, BuiltInRegistries.ITEM.getKey(gun).toString());
                    return entry;
                })
                .toList();
    }

    static boolean isGunEntry(Object element) {
        return element instanceof UnmodifiableConfig entry && entry.valueMap().get(ITEM_KEY) instanceof String;
    }

    private record ComponentOverride(Identifier component, Object value) {
    }

    private GunConfig() {
    }
}
