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
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class GunModifiedTrigger extends SimpleCriterionTrigger<GunModifiedTrigger.TriggerInstance> {
    static final ResourceLocation ID = IronsArtifice.id("gun_modified");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(
                player,
                ItemPredicate.fromJson(json.get("gun")),
                MinMaxBounds.Ints.fromJson(json.get("slots_occupied")),
                json.has("slots_full") ? GsonHelper.getAsBoolean(json, "slots_full") : null);
    }

    public void trigger(ServerPlayer player, ItemStack gun, int occupied, int capacity) {
        this.trigger(player, instance -> instance.matches(gun, occupied, capacity));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate gun;
        private final MinMaxBounds.Ints slotsOccupied;
        @Nullable
        private final Boolean slotsFull;

        public TriggerInstance(ContextAwarePredicate player, ItemPredicate gun, MinMaxBounds.Ints slotsOccupied, @Nullable Boolean slotsFull) {
            super(ID, player);
            this.gun = gun;
            this.slotsOccupied = slotsOccupied;
            this.slotsFull = slotsFull;
        }

        public static TriggerInstance anyModifier() {
            return new TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY, MinMaxBounds.Ints.atLeast(1), null);
        }

        public static TriggerInstance allSlotsFilled() {
            return new TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY, MinMaxBounds.Ints.ANY, true);
        }

        public boolean matches(ItemStack gunStack, int occupied, int capacity) {
            if (!gun.matches(gunStack)) {
                return false;
            }
            if (!slotsOccupied.matches(occupied)) {
                return false;
            }
            if (slotsFull != null && slotsFull != (capacity > 0 && occupied >= capacity)) {
                return false;
            }
            return true;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.add("gun", gun.serializeToJson());
            json.add("slots_occupied", slotsOccupied.serializeToJson());
            if (slotsFull != null) {
                json.addProperty("slots_full", slotsFull);
            }
            return json;
        }
    }
}
