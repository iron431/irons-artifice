package io.redspace.irons_artifice.data;

import java.util.HashMap;
import java.util.Map;

public class ShotComponentMap {

    private final Map<ComponentType<?>, Object> components;

    public ShotComponentMap() {
        this.components = new HashMap<>();
    }

    /**
     * Returns the stored value for {@code type}, or a fresh default if absent. THE VALUE IS NOT STORED, DO NOT MUTATE
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(ComponentType<T> type) {
        Object value = components.get(type);
        return value != null ? (T) value : type.provideDefaultValue();
    }

    /**
     * Returns the stored value for {@code type}, or creating and setting to default value
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(ComponentType<T> type) {
        Object value = components.get(type);
        if (value == null) {
            T created = type.provideDefaultValue();
            components.put(type, created);
            return created;
        }
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(ComponentType<T> type) {
        return (T) components.remove(type);
    }

    public <T> void set(ComponentType<T> type, T value) {
        components.put(type, value);
    }

    public boolean has(ComponentType<?> type) {
        return components.containsKey(type);
    }

    public void applyFrom(ShotComponentMap map) {
        this.components.putAll(map.components);
    }

    /**
     * New map, shared value references
     */
    public ShotComponentMap copy() {
        ShotComponentMap copy = new ShotComponentMap();
        copy.components.putAll(this.components);
        return copy;
    }

    /**
     * New map; {@link Copyable} values are cloned, others shared
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ShotComponentMap deepCopy() {
        ShotComponentMap copy = new ShotComponentMap();
        for (Map.Entry<ComponentType<?>, Object> entry : components.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Copyable copyable) {
                copy.components.put(entry.getKey(), copyable.copy());
            } else {
                copy.components.put(entry.getKey(), value);
            }
        }
        return copy;
    }

    public boolean isEmpty() {
        return this.components.isEmpty();
    }
}
