package io.redspace.irons_artifice.data;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public final class ComponentType<T> {
    final ResourceLocation name;

    final Supplier<T> defaultValue;

    public ComponentType(ResourceLocation name, Supplier<T> defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public T provideDefaultValue() {
        return defaultValue.get();
    }

    public ResourceLocation getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("ComponentType[%s]", name);
    }
}
