package io.redspace.irons_artifice.modifier;

import io.redspace.irons_artifice.data.Copyable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class PostHitEffects implements Copyable<PostHitEffects> {
    private final List<PostHitEffect> effects = new ArrayList<>();

    public void add(PostHitEffect effect) {
        this.effects.add(effect);
    }

    /**
     * Returns the first effect of {@code type}, or creates/stores one via {@code factory} if absent.
     */
    @SuppressWarnings("unchecked")
    public <T extends PostHitEffect> T getOrCreate(Class<T> type, Supplier<T> factory) {
        for (PostHitEffect effect : effects) {
            if (type.isInstance(effect)) {
                return (T) effect;
            }
        }
        T created = factory.get();
        effects.add(created);
        return created;
    }

    public void remove(Class<? extends PostHitEffect> type) {
        effects.removeIf(type::isInstance);
    }

    public boolean contains(PostHitEffect effect) {
        return this.effects.contains(effect);
    }

    public List<PostHitEffect> all() {
        return List.copyOf(this.effects);
    }

    public boolean isEmpty() {
        return this.effects.isEmpty();
    }

    @Override
    public PostHitEffects copy() {
        PostHitEffects copy = new PostHitEffects();
        copy.effects.addAll(this.effects);
        return copy;
    }
}
