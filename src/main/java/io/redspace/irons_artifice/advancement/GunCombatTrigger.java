package io.redspace.irons_artifice.advancement;

import com.google.gson.JsonObject;
import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import javax.annotation.Nullable;

public class GunCombatTrigger extends SimpleCriterionTrigger<GunCombatTrigger.TriggerInstance> {
    static final ResourceLocation ID = IronsArtifice.id("gun_combat");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(
                player,
                readBoolean(json, "killed"),
                MinMaxBounds.Doubles.fromJson(json.get("damage")),
                MinMaxBounds.Doubles.fromJson(json.get("distance")),
                MinMaxBounds.Ints.fromJson(json.get("pellets_on_target")),
                MinMaxBounds.Ints.fromJson(json.get("lineage_kills")),
                readBoolean(json, "ricocheted"),
                readBoolean(json, "full_magazine"),
                readBoolean(json, "insta_reloaded"),
                readBoolean(json, "submerged"),
                json.has("source") ? GunCombatSource.CODEC.byName(GsonHelper.getAsString(json, "source")) : null,
                ItemPredicate.fromJson(json.get("gun")),
                EntityPredicate.fromJson(json, "entity", context));
    }

    public void trigger(ServerPlayer player, CombatSnapshot snapshot) {
        LootContext victimContext = snapshot.victim() == null
                ? null
                : EntityPredicate.createContext(player, snapshot.victim());
        this.trigger(player, instance -> instance.matches(snapshot, victimContext));
    }

    @Nullable
    private static Boolean readBoolean(JsonObject json, String key) {
        return json.has(key) ? GsonHelper.getAsBoolean(json, key) : null;
    }

    private static void writeBoolean(JsonObject json, String key, @Nullable Boolean value) {
        if (value != null) {
            json.addProperty(key, value);
        }
    }

    public record CombatSnapshot(
            boolean killed,
            float damage,
            double distance,
            int pelletsOnTarget,
            int lineageKills,
            boolean ricocheted,
            boolean fullMagazine,
            boolean instaReloaded,
            boolean submerged,
            GunCombatSource source,
            ItemStack gun,
            Entity victim
    ) {
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final Boolean killed;
        private final MinMaxBounds.Doubles damage;
        private final MinMaxBounds.Doubles distance;
        private final MinMaxBounds.Ints pelletsOnTarget;
        private final MinMaxBounds.Ints lineageKills;
        @Nullable
        private final Boolean ricocheted;
        @Nullable
        private final Boolean fullMagazine;
        @Nullable
        private final Boolean instaReloaded;
        @Nullable
        private final Boolean submerged;
        @Nullable
        private final GunCombatSource source;
        private final ItemPredicate gun;
        private final ContextAwarePredicate entity;

        public TriggerInstance(
                ContextAwarePredicate player,
                @Nullable Boolean killed,
                MinMaxBounds.Doubles damage,
                MinMaxBounds.Doubles distance,
                MinMaxBounds.Ints pelletsOnTarget,
                MinMaxBounds.Ints lineageKills,
                @Nullable Boolean ricocheted,
                @Nullable Boolean fullMagazine,
                @Nullable Boolean instaReloaded,
                @Nullable Boolean submerged,
                @Nullable GunCombatSource source,
                ItemPredicate gun,
                ContextAwarePredicate entity
        ) {
            super(ID, player);
            this.killed = killed;
            this.damage = damage;
            this.distance = distance;
            this.pelletsOnTarget = pelletsOnTarget;
            this.lineageKills = lineageKills;
            this.ricocheted = ricocheted;
            this.fullMagazine = fullMagazine;
            this.instaReloaded = instaReloaded;
            this.submerged = submerged;
            this.source = source;
            this.gun = gun;
            this.entity = entity;
        }

        public static TriggerInstance impact(MinMaxBounds.Doubles damage, MinMaxBounds.Doubles distance) {
            return new TriggerInstance(
                    ContextAwarePredicate.ANY, null, damage, distance,
                    MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, null, null, null,
                    null, GunCombatSource.BULLET, ItemPredicate.ANY, ContextAwarePredicate.ANY);
        }

        public static TriggerInstance pelletsOnTarget(int min) {
            return new TriggerInstance(
                    ContextAwarePredicate.ANY, null, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY,
                    MinMaxBounds.Ints.atLeast(min), MinMaxBounds.Ints.ANY, null, null,
                    null, null, GunCombatSource.BULLET, ItemPredicate.ANY, ContextAwarePredicate.ANY);
        }

        public static TriggerInstance ricochetKill() {
            return new TriggerInstance(
                    ContextAwarePredicate.ANY, true, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.ANY, true, null, null, null,
                    GunCombatSource.BULLET, ItemPredicate.ANY, ContextAwarePredicate.ANY);
        }

        public static TriggerInstance lineageKills(int min) {
            return new TriggerInstance(
                    ContextAwarePredicate.ANY, true, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.atLeast(min), null, null, null,
                    null, GunCombatSource.BULLET, ItemPredicate.ANY, ContextAwarePredicate.ANY);
        }

        public static TriggerInstance bayonetKill() {
            return new TriggerInstance(
                    ContextAwarePredicate.ANY, true, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.ANY, null, null, null, null,
                    GunCombatSource.BAYONET, ItemPredicate.ANY, ContextAwarePredicate.ANY);
        }

        public static TriggerInstance submergedKill() {
            return new TriggerInstance(
                    ContextAwarePredicate.ANY, true, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.ANY, null, null, null, true,
                    GunCombatSource.BULLET, ItemPredicate.ANY, ContextAwarePredicate.ANY);
        }

        public static TriggerInstance fullMagazineKill(ContextAwarePredicate player) {
            return new TriggerInstance(
                    player, true, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.ANY, null, true, null, null,
                    GunCombatSource.BULLET, ItemPredicate.ANY, ContextAwarePredicate.ANY);
        }

        public static TriggerInstance instaReloadKill() {
            return new TriggerInstance(
                    ContextAwarePredicate.ANY, true, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.ANY, null, null, true, null,
                    GunCombatSource.BULLET, ItemPredicate.ANY, ContextAwarePredicate.ANY);
        }

        public boolean matches(CombatSnapshot snapshot, @Nullable LootContext victimContext) {
            if (killed != null && killed != snapshot.killed()) {
                return false;
            }
            if (!damage.matches(snapshot.damage())) {
                return false;
            }
            if (!distance.matches(snapshot.distance())) {
                return false;
            }
            if (!pelletsOnTarget.matches(snapshot.pelletsOnTarget())) {
                return false;
            }
            if (!lineageKills.matches(snapshot.lineageKills())) {
                return false;
            }
            if (ricocheted != null && ricocheted != snapshot.ricocheted()) {
                return false;
            }
            if (fullMagazine != null && fullMagazine != snapshot.fullMagazine()) {
                return false;
            }
            if (instaReloaded != null && instaReloaded != snapshot.instaReloaded()) {
                return false;
            }
            if (submerged != null && submerged != snapshot.submerged()) {
                return false;
            }
            if (source != null && source != snapshot.source()) {
                return false;
            }
            if (!gun.matches(snapshot.gun())) {
                return false;
            }
            return victimContext != null ? entity.matches(victimContext) : entity == ContextAwarePredicate.ANY;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            writeBoolean(json, "killed", killed);
            json.add("damage", damage.serializeToJson());
            json.add("distance", distance.serializeToJson());
            json.add("pellets_on_target", pelletsOnTarget.serializeToJson());
            json.add("lineage_kills", lineageKills.serializeToJson());
            writeBoolean(json, "ricocheted", ricocheted);
            writeBoolean(json, "full_magazine", fullMagazine);
            writeBoolean(json, "insta_reloaded", instaReloaded);
            writeBoolean(json, "submerged", submerged);
            if (source != null) {
                json.addProperty("source", source.getSerializedName());
            }
            json.add("gun", gun.serializeToJson());
            json.add("entity", entity.toJson(context));
            return json;
        }
    }
}
