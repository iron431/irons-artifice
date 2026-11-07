package io.redspace.irons_artifice.modifier;

import io.redspace.irons_artifice.data.Copyable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class OnShotEffects implements Copyable<OnShotEffects> {
    private final List<OnShotEffect> effects = new ArrayList<>();

    public void add(OnShotEffect effect) {
        this.effects.add(effect);
    }

    /**
     * Returns the first effect of {@code type}, or creates/stores one via {@code factory} if absent.
     */
    @SuppressWarnings("unchecked")
    public <T extends OnShotEffect> T getOrCreate(Class<T> type, Supplier<T> factory) {
        for (OnShotEffect effect : effects) {
            if (type.isInstance(effect)) {
                return (T) effect;
            }
        }
        T created = factory.get();
        effects.add(created);
        return created;
    }

    public void remove(Class<? extends OnShotEffect> type) {
        effects.removeIf(type::isInstance);
    }

    public boolean contains(OnShotEffect effect) {
        return this.effects.contains(effect);
    }

    public List<OnShotEffect> all() {
        return List.copyOf(this.effects);
    }

    public boolean isEmpty() {
        return this.effects.isEmpty();
    }

    @Override
    public OnShotEffects copy() {
        OnShotEffects copy = new OnShotEffects();
        copy.effects.addAll(this.effects);
        return copy;
    }
}
