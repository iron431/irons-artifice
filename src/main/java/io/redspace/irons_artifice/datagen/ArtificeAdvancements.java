package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.advancement.GunCombatTrigger;
import io.redspace.irons_artifice.advancement.GunModifiedTrigger;
import io.redspace.irons_artifice.advancement.ShotGunTrigger;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class ArtificeAdvancements implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                        ItemRegistry.BLACKPOWDER.get(),
                        title("root"),
                        description("blackpowder_heart"),
                        ResourceLocation.withDefaultNamespace("block/stripped_dark_oak_log"),
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .addCriterion("blackpowder", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.BLACKPOWDER.get()))
                .save(writer, id("blackpowder_heart"));

        AdvancementHolder arms = child(writer, root, "arms", ItemRegistry.FLINTLOCK_PISTOL.get(), AdvancementType.TASK, false,
                ShotGunTrigger.TriggerInstance.shotGun());
        AdvancementHolder artifice = child(writer, root, "artifice", ItemRegistry.INCENDIARY_TIP_MODIFIER.get(), AdvancementType.TASK, false,
                GunModifiedTrigger.TriggerInstance.anyModifier());
        AdvancementHolder fullyLoaded = child(writer, artifice, "fully_loaded", ItemRegistry.BULLET.get(), AdvancementType.GOAL, false,
                GunModifiedTrigger.TriggerInstance.allSlotsFilled());


        AdvancementHolder magFed = Advancement.Builder.advancement()
                .parent(arms)
                .display(ItemRegistry.SIX_SHOOTER.get(), title("mag_fed"), description("mag_fed"), null, AdvancementType.TASK, true, true, false)
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("six_shooter", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.SIX_SHOOTER.get()))
                .addCriterion("revolver", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.BLACKPOWDER_REVOLVER.get()))
                .addCriterion("clockwork_rifle", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.CLOCKWORK_RIFLE.get()))
                .save(writer, id("mag_fed"));
        AdvancementHolder arquebus = child(writer, magFed, "seven_sockets", ItemRegistry.ARQUEBUS.get(), AdvancementType.TASK, false,
                InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.ARQUEBUS.get()));

        AdvancementHolder wholeArsenal = Advancement.Builder.advancement()
                .parent(arquebus)
                .display(ItemRegistry.CLOCKWORK_RIFLE.get(), title("the_whole_arsenal"), description("the_whole_arsenal"), null, AdvancementType.GOAL, true, true, false)
                .addCriterion("guns", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemRegistry.FLINTLOCK_PISTOL.get(),
                        ItemRegistry.MUSKET.get(),
                        ItemRegistry.BLUNDERBUSS.get(),
                        ItemRegistry.BLACKPOWDER_REVOLVER.get(),
                        ItemRegistry.SIX_SHOOTER.get(),
                        ItemRegistry.ARQUEBUS.get(),
                        ItemRegistry.CLOCKWORK_RIFLE.get()
                ))
                .save(writer, id("the_whole_arsenal"));

        AdvancementHolder fanTheHammer = child(writer, magFed, "fan_the_hammer", ItemRegistry.HAIR_TRIGGER.get(), AdvancementType.CHALLENGE, false,
                ShotGunTrigger.TriggerInstance.shotsInLastSecond(15));

        child(writer, arms, "peer_review", ItemRegistry.ILLIFICER_SPAWN_EGG.get(), AdvancementType.TASK, true,
                KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity()
                                .of(registries.lookupOrThrow(Registries.ENTITY_TYPE), EntityRegistry.ILLIFICER.get()),
                        DamageSourcePredicate.Builder.damageType()
                                .tag(TagPredicate.is(TagKey.create(Registries.DAMAGE_TYPE, IronsArtifice.id("bullet"))))));

        child(writer, fullyLoaded, "professionals_have_standards", ItemRegistry.SCOPE_ATTACHMENT_MODIFIER.get(), AdvancementType.CHALLENGE, false,
                GunCombatTrigger.TriggerInstance.impact(MinMaxBounds.Doubles.atLeast(20), MinMaxBounds.Doubles.atLeast(100)));
        child(writer, fullyLoaded, "ventilated", ItemRegistry.SCATTERSHOT.get(), AdvancementType.CHALLENGE, false,
                GunCombatTrigger.TriggerInstance.pelletsOnTarget(12));
        child(writer, fullyLoaded, "through_and_through", ItemRegistry.STEEL_CORE.get(), AdvancementType.CHALLENGE, false,
                GunCombatTrigger.TriggerInstance.lineageKills(5));
        child(writer, fullyLoaded, "dont_bring_a_gun_to_a_knife_fight", ItemRegistry.BAYONET_ATTACHMENT_MODIFIER.get(), AdvancementType.CHALLENGE, false,
                GunCombatTrigger.TriggerInstance.bayonetKill());
        child(writer, fullyLoaded, "davy_joness_locker", ItemRegistry.SPIRAL_TIP_MODIFIER.get(), AdvancementType.CHALLENGE, true,
                GunCombatTrigger.TriggerInstance.submergedKill());
        child(writer, root, "pistols_at_dawn", ItemRegistry.TRICORNE_HAT.get(), AdvancementType.CHALLENGE, true,
                GunCombatTrigger.TriggerInstance.fullMagazineKill(EntityPredicate.wrap(
                        EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment()
                                .head(ItemPredicate.Builder.item().of(registries.lookupOrThrow(Registries.ITEM), ItemRegistry.TRICORNE_HAT.get()))))));
        child(writer, root, "fistful_of_lead", ItemRegistry.COWBOY_HAT.get(), AdvancementType.CHALLENGE, true,
                GunCombatTrigger.TriggerInstance.instaReloadKill());
        AdvancementHolder overOverOverkill = child(writer, wholeArsenal, "over_over_overkill", ItemRegistry.SINGULARITY_CHARGE_MODIFIER.get(), AdvancementType.CHALLENGE, true,
                GunCombatTrigger.TriggerInstance.impact(MinMaxBounds.Doubles.atLeast(100), MinMaxBounds.Doubles.atLeast(0)));
        child(writer, overOverOverkill, "ultrakill", ItemRegistry.SINGULARITY_CHARGE_MODIFIER.get(), AdvancementType.CHALLENGE, true,
                GunCombatTrigger.TriggerInstance.impact(MinMaxBounds.Doubles.atLeast(1000), MinMaxBounds.Doubles.atLeast(0)));
    }

    private static AdvancementHolder child(
            Consumer<AdvancementHolder> writer,
            AdvancementHolder parent,
            String path,
            ItemLike icon,
            AdvancementType type,
            boolean hidden,
            Criterion<?> criterion
    ) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(icon, title(path), description(path), null, type, true, true, hidden)
                .addCriterion(path, criterion)
                .save(writer, id(path));
    }

    private static Component title(String path) {
        return Component.translatable("advancements.irons_artifice." + path + ".title");
    }

    private static Component description(String path) {
        return Component.translatable("advancements.irons_artifice." + path + ".description");
    }

    private static String id(String path) {
        return IronsArtifice.id(path).toString();
    }
}
