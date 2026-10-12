package io.redspace.irons_artifice.advancement;

import com.google.gson.JsonObject;
import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ShotGunTrigger extends SimpleCriterionTrigger<ShotGunTrigger.TriggerInstance> {
    static final ResourceLocation ID = IronsArtifice.id("shot_gun");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(
                player,
                ItemPredicate.fromJson(json.get("gun")),
                MinMaxBounds.Ints.fromJson(json.get("shots_in_last_second")));
    }

    public void trigger(ServerPlayer player, ItemStack gun, int shotsInLastSecond) {
        this.trigger(player, instance -> instance.matches(gun, shotsInLastSecond));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate gun;
        private final MinMaxBounds.Ints shotsInLastSecond;

        public TriggerInstance(ContextAwarePredicate player, ItemPredicate gun, MinMaxBounds.Ints shotsInLastSecond) {
            super(ID, player);
            this.gun = gun;
            this.shotsInLastSecond = shotsInLastSecond;
        }

        public static TriggerInstance shotGun() {
            return new TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY, MinMaxBounds.Ints.ANY);
        }

        public static TriggerInstance shotsInLastSecond(int min) {
            return new TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY, MinMaxBounds.Ints.atLeast(min));
        }

        public boolean matches(ItemStack gunStack, int shots) {
            if (!gun.matches(gunStack)) {
                return false;
            }
            return shotsInLastSecond.matches(shots);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.add("gun", gun.serializeToJson());
            json.add("shots_in_last_second", shotsInLastSecond.serializeToJson());
            return json;
        }
    }
}
