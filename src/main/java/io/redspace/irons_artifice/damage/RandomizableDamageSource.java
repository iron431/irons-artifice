package io.redspace.irons_artifice.damage;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RandomizableDamageSource extends DamageSource {
    private List<String> deathMessageIds = List.of();

    public RandomizableDamageSource(Holder<DamageType> type, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        super(type, directEntity, causingEntity);
    }

    public RandomizableDamageSource(Holder<DamageType> type, @Nullable Entity entity) {
        super(type, entity);
    }

    public RandomizableDamageSource(Holder<DamageType> type) {
        super(type);
    }

    public RandomizableDamageSource setDeathMessages(String... deathMessageIds) {
        this.deathMessageIds = deathMessageIds.length == 0 ? List.of() : List.of(deathMessageIds);
        return this;
    }

    public RandomizableDamageSource addDeathMessage(String deathMessageId) {
        List<String> next = new ArrayList<>(this.deathMessageIds.size() + 1);
        next.addAll(this.deathMessageIds);
        next.add(deathMessageId);
        this.deathMessageIds = Collections.unmodifiableList(next);
        return this;
    }

    public List<String> getDeathMessageIds() {
        return deathMessageIds;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity victim) {
        String messageId = resolveMessageId(victim);
        if (this.getEntity() == null && this.getDirectEntity() == null) {
            LivingEntity killCredit = victim.getKillCredit();
            String playerMsg = messageId + ".player";
            return killCredit != null
                    ? Component.translatable(playerMsg, victim.getDisplayName(), killCredit.getDisplayName())
                    : Component.translatable(messageId, victim.getDisplayName());
        }

        Component attackerName = this.getEntity() == null
                ? this.getDirectEntity().getDisplayName()
                : this.getEntity().getDisplayName();
        ItemStack held = this.getEntity() instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
        return !held.isEmpty() && held.has(DataComponents.CUSTOM_NAME)
                ? Component.translatable(messageId + ".item", victim.getDisplayName(), attackerName, held.getDisplayName())
                : Component.translatable(messageId, victim.getDisplayName(), attackerName);
    }

    private String resolveMessageId(LivingEntity victim) {
        if (deathMessageIds.isEmpty()) {
            return "death.attack." + this.type().msgId();
        }
        return deathMessageIds.get(victim.getRandom().nextInt(deathMessageIds.size()));
    }

    @Override
    public String toString() {
        return "RandomizableDamageSource (" + this.type().msgId() + ", messages=" + Arrays.toString(deathMessageIds.toArray()) + ")";
    }
}
