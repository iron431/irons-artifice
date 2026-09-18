package io.redspace.irons_artifice.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.irons_artifice.registry.CriterionRegistry;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class GunModifiedTrigger extends SimpleCriterionTrigger<GunModifiedTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack gun, int occupied, int capacity) {
        this.trigger(player, instance -> instance.matches(gun, occupied, capacity));
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<ItemPredicate> gun,
            Optional<MinMaxBounds.Ints> slotsOccupied,
            Optional<Boolean> slotsFull
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ItemPredicate.CODEC.optionalFieldOf("gun").forGetter(TriggerInstance::gun),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("slots_occupied").forGetter(TriggerInstance::slotsOccupied),
                Codec.BOOL.optionalFieldOf("slots_full").forGetter(TriggerInstance::slotsFull)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> anyModifier() {
            return CriterionRegistry.GUN_MODIFIED.get().createCriterion(
                    new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(MinMaxBounds.Ints.atLeast(1)), Optional.empty()));
        }

        public static Criterion<TriggerInstance> allSlotsFilled() {
            return CriterionRegistry.GUN_MODIFIED.get().createCriterion(
                    new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(true)));
        }

        public boolean matches(ItemStack gunStack, int occupied, int capacity) {
            if (gun.isPresent() && !gun.get().test(gunStack)) {
                return false;
            }
            if (slotsOccupied.isPresent() && !slotsOccupied.get().matches(occupied)) {
                return false;
            }
            if (slotsFull.isPresent() && slotsFull.get() != (capacity > 0 && occupied >= capacity)) {
                return false;
            }
            return true;
        }
    }
}
