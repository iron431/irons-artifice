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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Optional;

public class GunCombatTrigger extends SimpleCriterionTrigger<GunCombatTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, CombatSnapshot snapshot) {
        LootContext victimContext = snapshot.victim() == null
                ? null
                : EntityPredicate.createContext(player, snapshot.victim());
        this.trigger(player, instance -> instance.matches(snapshot, victimContext));
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

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<Boolean> killed,
            Optional<MinMaxBounds.Doubles> damage,
            Optional<MinMaxBounds.Doubles> distance,
            Optional<MinMaxBounds.Ints> pelletsOnTarget,
            Optional<MinMaxBounds.Ints> lineageKills,
            Optional<Boolean> ricocheted,
            Optional<Boolean> fullMagazine,
            Optional<Boolean> instaReloaded,
            Optional<Boolean> submerged,
            Optional<GunCombatSource> source,
            Optional<ItemPredicate> gun,
            Optional<ContextAwarePredicate> entity
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.BOOL.optionalFieldOf("killed").forGetter(TriggerInstance::killed),
                MinMaxBounds.Doubles.CODEC.optionalFieldOf("damage").forGetter(TriggerInstance::damage),
                MinMaxBounds.Doubles.CODEC.optionalFieldOf("distance").forGetter(TriggerInstance::distance),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("pellets_on_target").forGetter(TriggerInstance::pelletsOnTarget),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("lineage_kills").forGetter(TriggerInstance::lineageKills),
                Codec.BOOL.optionalFieldOf("ricocheted").forGetter(TriggerInstance::ricocheted),
                Codec.BOOL.optionalFieldOf("full_magazine").forGetter(TriggerInstance::fullMagazine),
                Codec.BOOL.optionalFieldOf("insta_reloaded").forGetter(TriggerInstance::instaReloaded),
                Codec.BOOL.optionalFieldOf("submerged").forGetter(TriggerInstance::submerged),
                GunCombatSource.CODEC.optionalFieldOf("source").forGetter(TriggerInstance::source),
                ItemPredicate.CODEC.optionalFieldOf("gun").forGetter(TriggerInstance::gun),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> of(TriggerInstance instance) {
            return CriterionRegistry.GUN_COMBAT.get().createCriterion(instance);
        }

        public static Criterion<TriggerInstance> impact(MinMaxBounds.Doubles damage, MinMaxBounds.Doubles distance) {
            return of(new TriggerInstance(
                    Optional.empty(), Optional.empty(), Optional.of(damage), Optional.of(distance),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.of(GunCombatSource.BULLET), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> pelletsOnTarget(int min) {
            return of(new TriggerInstance(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.of(MinMaxBounds.Ints.atLeast(min)), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.of(GunCombatSource.BULLET), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> ricochetKill() {
            return of(new TriggerInstance(
                    Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.of(GunCombatSource.BULLET), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> lineageKills(int min) {
            return of(new TriggerInstance(
                    Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.of(MinMaxBounds.Ints.atLeast(min)), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.of(GunCombatSource.BULLET), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> bayonetKill() {
            return of(new TriggerInstance(
                    Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.of(GunCombatSource.BAYONET), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> submergedKill() {
            return of(new TriggerInstance(
                    Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(true),
                    Optional.of(GunCombatSource.BULLET), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> fullMagazineKill(ContextAwarePredicate player) {
            return of(new TriggerInstance(
                    Optional.of(player), Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(),
                    Optional.of(GunCombatSource.BULLET), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> instaReloadKill() {
            return of(new TriggerInstance(
                    Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(true), Optional.empty(),
                    Optional.of(GunCombatSource.BULLET), Optional.empty(), Optional.empty()));
        }

        public boolean matches(CombatSnapshot snapshot, LootContext victimContext) {
            if (killed.isPresent() && killed.get() != snapshot.killed()) {
                return false;
            }
            if (damage.isPresent() && !damage.get().matches(snapshot.damage())) {
                return false;
            }
            if (distance.isPresent() && !distance.get().matches(snapshot.distance())) {
                return false;
            }
            if (pelletsOnTarget.isPresent() && !pelletsOnTarget.get().matches(snapshot.pelletsOnTarget())) {
                return false;
            }
            if (lineageKills.isPresent() && !lineageKills.get().matches(snapshot.lineageKills())) {
                return false;
            }
            if (ricocheted.isPresent() && ricocheted.get() != snapshot.ricocheted()) {
                return false;
            }
            if (fullMagazine.isPresent() && fullMagazine.get() != snapshot.fullMagazine()) {
                return false;
            }
            if (instaReloaded.isPresent() && instaReloaded.get() != snapshot.instaReloaded()) {
                return false;
            }
            if (submerged.isPresent() && submerged.get() != snapshot.submerged()) {
                return false;
            }
            if (source.isPresent() && source.get() != snapshot.source()) {
                return false;
            }
            if (gun.isPresent() && !gun.get().test(snapshot.gun())) {
                return false;
            }
            return entity.isEmpty() || (victimContext != null && entity.get().matches(victimContext));
        }
    }
}
