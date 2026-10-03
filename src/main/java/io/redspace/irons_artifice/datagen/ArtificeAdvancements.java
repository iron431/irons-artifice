package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.advancement.GunCombatTrigger;
import io.redspace.irons_artifice.advancement.GunModifiedTrigger;
import io.redspace.irons_artifice.advancement.ShotGunTrigger;
import io.redspace.irons_artifice.registry.IronsArtificeEntities;
import io.redspace.irons_artifice.registry.IronsArtificeItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.KilledTrigger;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.TagPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class ArtificeAdvancements implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                        IronsArtificeItems.BLACKPOWDER.get(),
                        title("root"),
                        description("blackpowder_heart"),
                        Identifier.withDefaultNamespace("block/stripped_dark_oak_log"),
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .addCriterion("blackpowder", InventoryChangeTrigger.TriggerInstance.hasItems(IronsArtificeItems.BLACKPOWDER.get()))
                .save(writer, id("blackpowder_heart"));

        AdvancementHolder arms = child(writer, root, "arms", IronsArtificeItems.FLINTLOCK_PISTOL.get(), AdvancementType.TASK, false,
                ShotGunTrigger.TriggerInstance.shotGun());
        AdvancementHolder artifice = child(writer, root, "artifice", IronsArtificeItems.INCENDIARY_TIP_MODIFIER.get(), AdvancementType.TASK, false,
                GunModifiedTrigger.TriggerInstance.anyModifier());
        AdvancementHolder fullyLoaded = child(writer, artifice, "fully_loaded", IronsArtificeItems.BULLET.get(), AdvancementType.GOAL, false,
                GunModifiedTrigger.TriggerInstance.allSlotsFilled());


        AdvancementHolder magFed = Advancement.Builder.advancement()
                .parent(arms)
                .display(IronsArtificeItems.SIX_SHOOTER.get(), title("mag_fed"), description("mag_fed"), null, AdvancementType.TASK, true, true, false)
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("six_shooter", InventoryChangeTrigger.TriggerInstance.hasItems(IronsArtificeItems.SIX_SHOOTER.get()))
                .addCriterion("revolver", InventoryChangeTrigger.TriggerInstance.hasItems(IronsArtificeItems.BLACKPOWDER_REVOLVER.get()))
                .addCriterion("clockwork_rifle", InventoryChangeTrigger.TriggerInstance.hasItems(IronsArtificeItems.CLOCKWORK_RIFLE.get()))
                .save(writer, id("mag_fed"));
        AdvancementHolder arquebus = child(writer, magFed, "seven_sockets", IronsArtificeItems.ARQUEBUS.get(), AdvancementType.TASK, false,
                InventoryChangeTrigger.TriggerInstance.hasItems(IronsArtificeItems.ARQUEBUS.get()));

        AdvancementHolder wholeArsenal = Advancement.Builder.advancement()
                .parent(arquebus)
                .display(IronsArtificeItems.CLOCKWORK_RIFLE.get(), title("the_whole_arsenal"), description("the_whole_arsenal"), null, AdvancementType.GOAL, true, true, false)
                .addCriterion("guns", InventoryChangeTrigger.TriggerInstance.hasItems(
                        IronsArtificeItems.FLINTLOCK_PISTOL.get(),
                        IronsArtificeItems.MUSKET.get(),
                        IronsArtificeItems.BLUNDERBUSS.get(),
                        IronsArtificeItems.BLACKPOWDER_REVOLVER.get(),
                        IronsArtificeItems.SIX_SHOOTER.get(),
                        IronsArtificeItems.ARQUEBUS.get(),
                        IronsArtificeItems.CLOCKWORK_RIFLE.get()
                ))
                .save(writer, id("the_whole_arsenal"));

        AdvancementHolder fanTheHammer = child(writer, magFed, "fan_the_hammer", IronsArtificeItems.HAIR_TRIGGER.get(), AdvancementType.CHALLENGE, false,
                ShotGunTrigger.TriggerInstance.shotsInLastSecond(15));

        child(writer, arms, "peer_review", IronsArtificeItems.ILLIFICER_SPAWN_EGG.get(), AdvancementType.TASK, true,
                KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity()
                                .of(registries.lookupOrThrow(Registries.ENTITY_TYPE), IronsArtificeEntities.ILLIFICER.get()),
                        DamageSourcePredicate.Builder.damageType()
                                .tag(TagPredicate.is(TagKey.create(Registries.DAMAGE_TYPE, IronsArtifice.id("bullet"))))));

        child(writer, fullyLoaded, "professionals_have_standards", IronsArtificeItems.SCOPE_ATTACHMENT_MODIFIER.get(), AdvancementType.CHALLENGE, false,
                GunCombatTrigger.TriggerInstance.impact(MinMaxBounds.Doubles.atLeast(20), MinMaxBounds.Doubles.atLeast(100)));
        child(writer, fullyLoaded, "ventilated", IronsArtificeItems.SCATTERSHOT.get(), AdvancementType.CHALLENGE, false,
                GunCombatTrigger.TriggerInstance.pelletsOnTarget(12));
        child(writer, fullyLoaded, "through_and_through", IronsArtificeItems.STEEL_CORE.get(), AdvancementType.CHALLENGE, false,
                GunCombatTrigger.TriggerInstance.lineageKills(5));
        child(writer, fullyLoaded, "dont_bring_a_gun_to_a_knife_fight", IronsArtificeItems.BAYONET_ATTACHMENT_MODIFIER.get(), AdvancementType.CHALLENGE, false,
                GunCombatTrigger.TriggerInstance.bayonetKill());
        child(writer, fullyLoaded, "davy_joness_locker", IronsArtificeItems.SPIRAL_TIP_MODIFIER.get(), AdvancementType.CHALLENGE, true,
                GunCombatTrigger.TriggerInstance.submergedKill());
        child(writer, root, "pistols_at_dawn", IronsArtificeItems.TRICORNE_HAT.get(), AdvancementType.CHALLENGE, true,
                GunCombatTrigger.TriggerInstance.fullMagazineKill(EntityPredicate.wrap(
                        EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment()
                                .head(ItemPredicate.Builder.item().of(registries.lookupOrThrow(Registries.ITEM), IronsArtificeItems.TRICORNE_HAT.get()))))));
        child(writer, root, "fistful_of_lead", IronsArtificeItems.COWBOY_HAT.get(), AdvancementType.CHALLENGE, true,
                GunCombatTrigger.TriggerInstance.instaReloadKill());
        AdvancementHolder overOverOverkill = child(writer, wholeArsenal, "over_over_overkill", IronsArtificeItems.SINGULARITY_CHARGE_MODIFIER.get(), AdvancementType.CHALLENGE, true,
                GunCombatTrigger.TriggerInstance.impact(MinMaxBounds.Doubles.atLeast(100), MinMaxBounds.Doubles.atLeast(0)));
        child(writer, overOverOverkill, "ultrakill", IronsArtificeItems.SINGULARITY_CHARGE_MODIFIER.get(), AdvancementType.CHALLENGE, true,
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
