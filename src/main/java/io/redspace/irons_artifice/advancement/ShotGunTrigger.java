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

public class ShotGunTrigger extends SimpleCriterionTrigger<ShotGunTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack gun, int shotsInLastSecond) {
        this.trigger(player, instance -> instance.matches(gun, shotsInLastSecond));
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<ItemPredicate> gun,
            Optional<MinMaxBounds.Ints> shotsInLastSecond
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ItemPredicate.CODEC.optionalFieldOf("gun").forGetter(TriggerInstance::gun),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("shots_in_last_second").forGetter(TriggerInstance::shotsInLastSecond)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> shotGun() {
            return CriterionRegistry.SHOT_GUN.get().createCriterion(
                    new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> shotsInLastSecond(int min) {
            return CriterionRegistry.SHOT_GUN.get().createCriterion(
                    new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(MinMaxBounds.Ints.atLeast(min))));
        }

        public boolean matches(ItemStack gunStack, int shots) {
            if (gun.isPresent() && !gun.get().test(gunStack)) {
                return false;
            }
            return shotsInLastSecond.isEmpty() || shotsInLastSecond.get().matches(shots);
        }
    }
}
