package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.concurrent.CompletableFuture;

public class RecipeDataGenerator extends RecipeProvider {
    protected RecipeDataGenerator(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        /* **********************************
         * Blackpowder
         ********************************** */
        ShapelessRecipeBuilder.shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.BLACKPOWDER.get(), 6)
                .requires(Items.GUNPOWDER)
                .requires(Items.CHARCOAL)
                .requires(Items.REDSTONE)
                .unlockedBy("has_gunpowder", this.has(Items.GUNPOWDER))
                .save(this.output, recipeId("blackpowder_from_gunpowder"));
        ShapelessRecipeBuilder.shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.BLACKPOWDER.get(), 2)
                .requires(Items.CHARCOAL)
                .requires(Items.REDSTONE)
                .unlockedBy("has_redstone", this.has(Items.REDSTONE))
                .save(this.output, recipeId("blackpowder"));
        /* **********************************
         * Bullets
         ********************************** */
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.BULLET.get(), 16)
                .pattern("#")
                .pattern("^")
                .define('#', commonTag("ingots/iron"))
                .define('^', ItemRegistry.BLACKPOWDER.get())
                .unlockedBy("has_blackpowder", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output, recipeId("bullet_from_iron"));
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.BULLET.get(), 4)
                .pattern("#")
                .pattern("^")
                .define('#', commonTag("ingots/copper"))
                .define('^', ItemRegistry.BLACKPOWDER.get())
                .unlockedBy("has_blackpowder", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output, recipeId("bullet_from_copper"));
        /* **********************************
         * Armor
         ********************************** */
        // Cowboy Hat
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.COWBOY_HAT.get())
                .pattern("B#B")
                .pattern("***")
                .define('*', commonTag("leathers"))
                .define('B', ItemRegistry.BULLET)
                .define('#', Items.LEATHER_HELMET)
                .unlockedBy("precursor", this.has(ItemRegistry.BULLET))
                .save(this.output);
        // Tricorne
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.TRICORNE_HAT.get())
                .pattern("***")
                .pattern("B#F")
                .define('*', commonTag("leathers"))
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('#', Items.LEATHER_HELMET)
                .define('F', Items.FEATHER)
                .unlockedBy("precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        /* **********************************
         * Mechanical Components
         ********************************** */
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS.get())
                .pattern("CNC")
                .pattern("IRI")
                .pattern("NCN")
                .define('N', commonTag("nuggets/copper"))
                .define('C', Items.COPPER_CHAIN.unaffected())
                .define('I', commonTag("ingots/copper"))
                .define('R', Items.REDSTONE)
                .unlockedBy("has_redstone", this.has(Items.REDSTONE))
                .save(this.output);
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.MECHANICAL_COMPONENTS.get())
                .pattern("BC ")
                .pattern("CMC")
                .pattern(" CN")
                .define('B', commonTag("storage_blocks/iron"))
                .define('N', commonTag("nuggets/iron"))
                .define('C', Items.IRON_CHAIN)
                .define('M', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_simple", this.has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.CLOCKWORK_COMPONENTS.get())
                .pattern("MI ")
                .pattern("IRI")
                .pattern(" IM")
                .define('I', commonTag("ingots/gold"))
                .define('R', Items.REDSTONE)
                .define('M', ItemRegistry.MECHANICAL_COMPONENTS)
                .unlockedBy("has_mechanical", this.has(ItemRegistry.MECHANICAL_COMPONENTS))
                .save(this.output);
        /* **********************************
         * Guns
         ********************************** */
        // Flintlock
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.FLINTLOCK_PISTOL.get())
                .pattern("I  ")
                .pattern(" IF")
                .pattern(" LB")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('F', Items.FLINT_AND_STEEL)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(Items.IRON_INGOT))
                .save(this.output);
        // Musket
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.MUSKET.get())
                .pattern("I  ")
                .pattern(" MF")
                .pattern(" LB")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('F', Items.FLINT_AND_STEEL)
                .define('M', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(Items.IRON_INGOT))
                .save(this.output);
        // Blackpowder Revolver
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.BLACKPOWDER_REVOLVER.get())
                .pattern("I  ")
                .pattern(" HM")
                .pattern(" LB")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('H', Items.HOPPER)
                .define('M', ItemRegistry.MECHANICAL_COMPONENTS)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(Items.IRON_INGOT))
                .save(this.output);
        // Six Shooter
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.SIX_SHOOTER.get())
                .pattern("I  ")
                .pattern(" HM")
                .pattern(" ML")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('H', Items.HOPPER)
                .define('M', ItemRegistry.MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(Items.IRON_INGOT))
                .save(this.output);
        // Blunderbuss
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.BLUNDERBUSS.get())
                .pattern("MI ")
                .pattern("IMI")
                .pattern(" IL")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('M', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(Items.IRON_INGOT))
                .save(this.output);
        // Arquebus
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.ARQUEBUS.get())
                .pattern("I  ")
                .pattern(" IM")
                .pattern(" LB")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('M', ItemRegistry.CLOCKWORK_COMPONENTS)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(Items.IRON_INGOT))
                .save(this.output);
        // Clockwork Rifle
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.CLOCKWORK_RIFLE.get())
                .pattern("I  ")
                .pattern(" HR")
                .pattern(" LM")
                .define('I', commonTag("ingots/netherite"))
                .define('L', ItemTags.LOGS)
                .define('M', ItemRegistry.CLOCKWORK_COMPONENTS)
                .define('H', Items.HOPPER)
                .define('R', Items.REPEATER)
                .unlockedBy("has_precursor", this.has(Items.NETHERITE_INGOT))
                .save(this.output);
        /* **********************************
         * Modifier
         ********************************** */
        // Overcharged Powder
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.OVERCHARGED_POWDER.get())
                .pattern("BBB")
                .pattern("PRP")
                .pattern("BBB")
                .define('R', commonTag("storage_blocks/redstone"))
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('P', Items.BLAZE_POWDER)
                .unlockedBy("has_precursor", this.has(Items.BLAZE_POWDER))
                .save(this.output);
        // Steel Core
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.STEEL_CORE.get())
                .pattern(" I ")
                .pattern(" S ")
                .pattern("IBI")
                .define('S', commonTag("storage_blocks/iron"))
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('I', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Incendiary Tip
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.INCENDIARY_TIP_MODIFIER.get())
                .pattern(" P ")
                .pattern("PIP")
                .pattern("IBI")
                .define('P', Items.BLAZE_POWDER)
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('I', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Hair Trigger
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.HAIR_TRIGGER.get())
                .pattern("C")
                .pattern("R")
                .define('R', Items.BREEZE_ROD)
                .define('C', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        // Chain Lightning
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.CHAIN_LIGHTNING.get())
                .pattern(" R ")
                .pattern("RIR")
                .pattern("IBI")
                .define('R', Items.LIGHTNING_ROD)
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('I', commonTag("ingots/copper"))
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Frozen Jacket
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.FROZEN_JACKET.get())
                .pattern(" * ")
                .pattern("*R*")
                .pattern("RBR")
                .define('R', Items.BLUE_ICE)
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('*', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Antigravity Powder
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.ANTIGRAVITY_MODIFIER.get())
                .pattern("BPB")
                .define('P', Items.ENDER_PEARL)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Wind Chamber
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.WIND_CHAMBER.get())
                .pattern("  P")
                .pattern("CB ")
                .pattern(" C ")
                .define('P', Items.WIND_CHARGE)
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('C', commonTag("ingots/copper"))
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Gas Vent
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.GAS_VENT.get())
                .pattern("BPB")
                .define('P', Items.HOPPER)
                .define('B', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        // Blackpowder Charge
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.BLACKPOWDER_CHARGE.get())
                .pattern("BSB")
                .pattern("BBB")
                .pattern("BBB")
                .define('S', Items.STRING)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Mechanical Repeater
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.MECHANICAL_REPEATER.get())
                .pattern("#B#")
                .pattern("***")
                .define('#', Items.IRON_CHAIN)
                .define('*', commonTag("ingots/gold"))
                .define('B', ItemRegistry.CLOCKWORK_COMPONENTS)
                .unlockedBy("has_precursor", this.has(ItemRegistry.CLOCKWORK_COMPONENTS))
                .save(this.output);
        // Chain Shot
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.CHAIN_SHOT.get())
                .pattern("###")
                .pattern("# #")
                .pattern("B B")
                .define('#', Items.IRON_CHAIN)
                .define('B', ItemRegistry.BULLET)
                .unlockedBy("has_precursor", this.has(ItemRegistry.BULLET))
                .save(this.output);
        // Buffer Spring
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.BUFFER_SPRING.get())
                .pattern("I I")
                .pattern("IBI")
                .pattern("I I")
                .define('I', commonTag("ingots/iron"))
                .define('B', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        // Breaching
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.BREACHING_SHELL.get())
                .pattern(" R ")
                .pattern("RBR")
                .pattern("III")
                .define('R', Items.FLINT)
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('I', commonTag("ingots/copper"))
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Venom
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.VENOM_CAPSULE.get())
                .pattern(" EE")
                .pattern(" GE")
                .pattern("B  ")
                .define('E', Items.SPIDER_EYE)
                .define('G', Items.GLASS_BOTTLE)
                .define('B', ItemRegistry.BULLET)
                .unlockedBy("has_precursor", this.has(ItemRegistry.BULLET))
                .save(this.output);
        // Scattershot
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.SCATTERSHOT.get())
                .pattern(" BB")
                .pattern("#PB")
                .pattern(" # ")
                .define('#', Items.STRING)
                .define('P', ItemRegistry.BLACKPOWDER)
                .define('B', ItemRegistry.BULLET)
                .unlockedBy("has_precursor", this.has(ItemRegistry.BULLET))
                .save(this.output);
        // Lead Core
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.LEAD_CORE.get())
                .pattern(" I ")
                .pattern(" S ")
                .pattern("IBI")
                .define('S', Items.DEEPSLATE_BRICKS)
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('I', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Trick Bullet
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.TRICK_BULLET_MODIFIER.get())
                .pattern(" I ")
                .pattern(" S ")
                .pattern("IBI")
                .define('S', commonTag("storage_blocks/gold"))
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('I', commonTag("ingots/gold"))
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Gun Oil
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.GUN_OIL.get())
                .pattern("LMR")
                .define('L', Items.HONEY_BOTTLE)
                .define('R', Items.REDSTONE)
                .define('M', ItemRegistry.MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(ItemRegistry.MECHANICAL_COMPONENTS))
                .save(this.output);
        // Singularity Charge
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.SINGULARITY_CHARGE_MODIFIER.get())
                .pattern(" #B")
                .pattern("#*#")
                .pattern("B# ")
                .define('#', Items.AMETHYST_SHARD)
                .define('*', Items.ENDER_EYE)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Enchanted Bullet
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.ENCHANTED_BULLET_MODIFIER.get())
                .pattern(" ##")
                .pattern("B*#")
                .pattern(" B ")
                .define('#', Items.LAPIS_LAZULI)
                .define('*', ItemRegistry.BULLET)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Seeking Powder
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.SEEKING_POWDER.get())
                .pattern("B*B")
                .define('*', Items.AMETHYST_CLUSTER)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(ItemRegistry.BLACKPOWDER))
                .save(this.output);
        // Accelerating
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.MECHANICAL_ACCELERATOR_MODIFIER.get())
                .pattern("#B#")
                .pattern("***")
                .define('#', Items.COPPER_CHAIN.unaffected())
                .define('*', commonTag("ingots/copper"))
                .define('B', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        // Scope
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ItemRegistry.SCOPE_ATTACHMENT_MODIFIER.get())
                .pattern("#")
                .pattern("*")
                .define('#', Items.SPYGLASS)
                .define('*', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
    }

    private static TagKey<Item> commonTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }

    private static ResourceKey<Recipe<?>> recipeId(Identifier identifier) {
        return ResourceKey.create(Registries.RECIPE, identifier);
    }

    private static ResourceKey<Recipe<?>> recipeId(String name) {
        return ResourceKey.create(Registries.RECIPE, IronsArtifice.id(name));
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new RecipeDataGenerator(registries, output);
        }

        @Override
        public String getName() {
            return IronsArtifice.MODID + "_recipes";
        }
    }
}
