package io.redspace.irons_artifice.modifier;

import io.redspace.irons_artifice.data.Copyable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class OnHitEffects implements Copyable<OnHitEffects> {
    private final List<OnHitEffect> effects = new ArrayList<>();

    public void add(OnHitEffect effect) {
        this.effects.add(effect);
    }

    /**
     * Returns the first effect of {@code type}, or creates/stores one via {@code factory} if absent.
     */
    @SuppressWarnings("unchecked")
    public <T extends OnHitEffect> T getOrCreate(Class<T> type, Supplier<T> factory) {
        for (OnHitEffect effect : effects) {
            if (type.isInstance(effect)) {
                return (T) effect;
            }
        }
        T created = factory.get();
        effects.add(created);
        return created;
    }

    public void remove(Class<? extends OnHitEffect> type) {
        effects.removeIf(type::isInstance);
    }

    public boolean contains(OnHitEffect effect) {
        return this.effects.contains(effect);
    }

    public List<OnHitEffect> all() {
        return List.copyOf(this.effects);
    }

    public boolean isEmpty() {
        return this.effects.isEmpty();
    }

    @Override
    public OnHitEffects copy() {
        OnHitEffects copy = new OnHitEffects();
        copy.effects.addAll(this.effects);
        return copy;
    }
}
