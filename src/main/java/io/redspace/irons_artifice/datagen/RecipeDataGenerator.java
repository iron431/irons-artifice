package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.registry.IronsArtificeRegistries;
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
    public RecipeDataGenerator(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        /* **********************************
         * Blackpowder
         ********************************** */
        ShapelessRecipeBuilder.shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.BLACKPOWDER.get(), 6)
                .requires(Items.GUNPOWDER)
                .requires(Items.CHARCOAL)
                .requires(Items.REDSTONE)
                .unlockedBy("has_gunpowder", this.has(Items.GUNPOWDER))
                .save(this.output, recipeId("blackpowder_from_gunpowder"));
        ShapelessRecipeBuilder.shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.BLACKPOWDER.get(), 2)
                .requires(Items.CHARCOAL)
                .requires(Items.REDSTONE)
                .unlockedBy("has_redstone", this.has(Items.REDSTONE))
                .save(this.output, recipeId("blackpowder"));
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.BLACKPOWDER.get(), 1)
                .pattern("##")
                .define('#', Items.CHARCOAL)
                .unlockedBy("has_charcoal", this.has(Items.CHARCOAL))
                .save(this.output, recipeId("blackpowder_from_charcoal"));
        /* **********************************
         * Bullets
         ********************************** */
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.BULLET.get(), 16)
                .pattern("#")
                .pattern("^")
                .define('#', commonTag("ingots/iron"))
                .define('^', IronsArtificeRegistries.Items.BLACKPOWDER.get())
                .unlockedBy("has_blackpowder", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output, recipeId("bullet_from_iron"));
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.BULLET.get(), 4)
                .pattern("#")
                .pattern("^")
                .define('#', commonTag("ingots/copper"))
                .define('^', IronsArtificeRegistries.Items.BLACKPOWDER.get())
                .unlockedBy("has_blackpowder", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output, recipeId("bullet_from_copper"));
        /* **********************************
         * Armor
         ********************************** */
        // Cowboy Hat
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.COWBOY_HAT.get())
                .pattern("B#B")
                .pattern("***")
                .define('*', commonTag("leathers"))
                .define('B', IronsArtificeRegistries.Items.BULLET)
                .define('#', Items.LEATHER_HELMET)
                .unlockedBy("precursor", this.has(IronsArtificeRegistries.Items.BULLET))
                .save(this.output);
        // Tricorne
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.TRICORNE_HAT.get())
                .pattern("***")
                .pattern("B#F")
                .define('*', commonTag("leathers"))
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('#', Items.LEATHER_HELMET)
                .define('F', Items.FEATHER)
                .unlockedBy("precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        /* **********************************
         * Mechanical Components
         ********************************** */
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS.get())
                .pattern("CIC")
                .pattern("INI")
                .pattern("CIC")
                .define('N', commonTag("nuggets/copper"))
                .define('C', Items.COPPER_CHAIN.unaffected())
                .define('I', commonTag("ingots/copper"))
                .unlockedBy("has_redstone", this.has(Items.REDSTONE))
                .save(this.output);
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.MECHANICAL_COMPONENTS.get())
                .pattern("BCR")
                .pattern("CMC")
                .pattern("RCN")
                .define('B', commonTag("storage_blocks/iron"))
                .define('N', commonTag("nuggets/iron"))
                .define('C', Items.IRON_CHAIN)
                .define('R', Items.REDSTONE)
                .define('M', IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_simple", this.has(IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.CLOCKWORK_COMPONENTS.get())
                .pattern("MI ")
                .pattern("IRI")
                .pattern(" IM")
                .define('I', commonTag("ingots/gold"))
                .define('R', Items.REDSTONE)
                .define('M', IronsArtificeRegistries.Items.MECHANICAL_COMPONENTS)
                .unlockedBy("has_mechanical", this.has(IronsArtificeRegistries.Items.MECHANICAL_COMPONENTS))
                .save(this.output);
        /* **********************************
         * Guns
         ********************************** */
        // Flintlock
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.FLINTLOCK_PISTOL.get())
                .pattern("I  ")
                .pattern(" IF")
                .pattern(" LB")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('F', Items.FLINT_AND_STEEL)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(Items.IRON_INGOT))
                .save(this.output);
        // Musket
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.MUSKET.get())
                .pattern("I  ")
                .pattern(" MF")
                .pattern(" LB")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('F', Items.FLINT_AND_STEEL)
                .define('M', IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(Items.IRON_INGOT))
                .save(this.output);
        // Blackpowder Revolver
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.BLACKPOWDER_REVOLVER.get())
                .pattern("I  ")
                .pattern(" HM")
                .pattern(" LB")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('H', Items.HOPPER)
                .define('M', IronsArtificeRegistries.Items.MECHANICAL_COMPONENTS)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(Items.IRON_INGOT))
                .save(this.output);
        // Six Shooter
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.SIX_SHOOTER.get())
                .pattern("I  ")
                .pattern(" HM")
                .pattern(" IL")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('H', Items.HOPPER)
                .define('M', IronsArtificeRegistries.Items.MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(Items.IRON_INGOT))
                .save(this.output);
        // Blunderbuss
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.BLUNDERBUSS.get())
                .pattern("MI ")
                .pattern("IMI")
                .pattern(" IL")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('M', IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(Items.IRON_INGOT))
                .save(this.output);
        // Arquebus
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.ARQUEBUS.get())
                .pattern("I  ")
                .pattern(" IM")
                .pattern(" LB")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('M', IronsArtificeRegistries.Items.CLOCKWORK_COMPONENTS)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(Items.IRON_INGOT))
                .save(this.output);
        // Clockwork Rifle
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.CLOCKWORK_RIFLE.get())
                .pattern("I  ")
                .pattern(" HR")
                .pattern(" LM")
                .define('I', commonTag("ingots/netherite"))
                .define('L', ItemTags.LOGS)
                .define('M', IronsArtificeRegistries.Items.CLOCKWORK_COMPONENTS)
                .define('H', Items.HOPPER)
                .define('R', Items.REPEATER)
                .unlockedBy("has_precursor", this.has(Items.NETHERITE_INGOT))
                .save(this.output);
        /* **********************************
         * Modifier
         ********************************** */
        // Overcharged Powder
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.OVERCHARGED_POWDER.get())
                .pattern("BBB")
                .pattern("PRP")
                .pattern("BBB")
                .define('R', commonTag("storage_blocks/redstone"))
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('P', Items.BLAZE_POWDER)
                .unlockedBy("has_precursor", this.has(Items.BLAZE_POWDER))
                .save(this.output);
        // Steel Core
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.STEEL_CORE.get())
                .pattern(" I ")
                .pattern(" S ")
                .pattern("IBI")
                .define('S', commonTag("storage_blocks/iron"))
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('I', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Incendiary Tip
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.INCENDIARY_TIP_MODIFIER.get())
                .pattern(" P ")
                .pattern("PIP")
                .pattern("IBI")
                .define('P', Items.BLAZE_POWDER)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('I', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Hair Trigger
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.HAIR_TRIGGER.get())
                .pattern("C")
                .pattern("R")
                .pattern("R")
                .define('R', commonTag("ingots/copper"))
                .define('C', IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        // Chain Lightning
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.CHAIN_LIGHTNING.get())
                .pattern(" R ")
                .pattern("RIR")
                .pattern("IBI")
                .define('R', Items.LIGHTNING_ROD)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('I', commonTag("ingots/copper"))
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Frozen Jacket
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.FROZEN_JACKET.get())
                .pattern(" * ")
                .pattern("*R*")
                .pattern("RBR")
                .define('R', Items.BLUE_ICE)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('*', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Antigravity Powder
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.ANTIGRAVITY_MODIFIER.get())
                .pattern("BPB")
                .define('P', Items.ENDER_PEARL)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Wind Chamber
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.WIND_CHAMBER.get())
                .pattern("  P")
                .pattern("CB ")
                .pattern(" C ")
                .define('P', Items.WIND_CHARGE)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('C', commonTag("ingots/copper"))
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Gas Vent
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.GAS_VENT.get())
                .pattern("BPB")
                .define('P', Items.HOPPER)
                .define('B', IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        // Blackpowder Charge
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.BLACKPOWDER_CHARGE.get())
                .pattern("BSB")
                .pattern("BBB")
                .pattern("BBB")
                .define('S', Items.STRING)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Mechanical Repeater
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.MECHANICAL_REPEATER.get())
                .pattern("#B#")
                .pattern("***")
                .define('#', Items.IRON_CHAIN)
                .define('*', commonTag("ingots/gold"))
                .define('B', IronsArtificeRegistries.Items.CLOCKWORK_COMPONENTS)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.CLOCKWORK_COMPONENTS))
                .save(this.output);
        // Chain Shot
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.CHAIN_SHOT.get())
                .pattern("###")
                .pattern("# #")
                .pattern("B B")
                .define('#', Items.IRON_CHAIN)
                .define('B', IronsArtificeRegistries.Items.BULLET)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BULLET))
                .save(this.output);
        // Buffer Spring
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.BUFFER_SPRING.get())
                .pattern("I I")
                .pattern("IBI")
                .pattern("I I")
                .define('I', commonTag("ingots/iron"))
                .define('B', IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        // Breaching
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.BREACHING_SHELL.get())
                .pattern(" R ")
                .pattern("RBR")
                .pattern("III")
                .define('R', Items.FLINT)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('I', commonTag("ingots/copper"))
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Venom
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.VENOM_CAPSULE.get())
                .pattern(" EE")
                .pattern(" GE")
                .pattern("B  ")
                .define('E', Items.SPIDER_EYE)
                .define('G', Items.GLASS_BOTTLE)
                .define('B', IronsArtificeRegistries.Items.BULLET)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BULLET))
                .save(this.output);
        // Scattershot
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.SCATTERSHOT.get())
                .pattern(" BB")
                .pattern("#PB")
                .pattern(" # ")
                .define('#', Items.STRING)
                .define('P', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('B', IronsArtificeRegistries.Items.BULLET)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BULLET))
                .save(this.output);
        // Lead Core
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.LEAD_CORE.get())
                .pattern(" I ")
                .pattern(" S ")
                .pattern("IBI")
                .define('S', Items.DEEPSLATE_BRICKS)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('I', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Trick Bullet
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.TRICK_BULLET_MODIFIER.get())
                .pattern(" I ")
                .pattern(" S ")
                .pattern("IBI")
                .define('S', commonTag("storage_blocks/gold"))
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('I', commonTag("ingots/gold"))
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Gun Oil
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.GUN_OIL.get())
                .pattern("LMR")
                .define('L', Items.HONEY_BOTTLE)
                .define('R', Items.REDSTONE)
                .define('M', IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        // Singularity Charge
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.SINGULARITY_CHARGE_MODIFIER.get())
                .pattern(" #B")
                .pattern("#*#")
                .pattern("B# ")
                .define('#', Items.AMETHYST_SHARD)
                .define('*', Items.ENDER_EYE)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Enchanted Bullet
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.ENCHANTED_BULLET_MODIFIER.get())
                .pattern(" ##")
                .pattern("B*#")
                .pattern(" B ")
                .define('#', Items.LAPIS_LAZULI)
                .define('*', IronsArtificeRegistries.Items.BULLET)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Seeking Powder
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.SEEKING_POWDER.get())
                .pattern("B*B")
                .define('*', Items.AMETHYST_CLUSTER)
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Accelerating
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.MECHANICAL_ACCELERATOR_MODIFIER.get())
                .pattern("#B#")
                .pattern("***")
                .define('#', Items.COPPER_CHAIN.unaffected())
                .define('*', commonTag("ingots/copper"))
                .define('B', IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        // Scope
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.SCOPE_ATTACHMENT_MODIFIER.get())
                .pattern("#")
                .pattern("*")
                .define('#', Items.SPYGLASS)
                .define('*', IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        // Bayonet
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.BAYONET_ATTACHMENT_MODIFIER.get())
                .pattern("*")
                .pattern("#")
                .define('#', Items.IRON_SPEAR)
                .define('*', IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.SIMPLE_MECHANICAL_COMPONENTS))
                .save(this.output);
        // Spiral Tip
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.SPIRAL_TIP_MODIFIER.get())
                .pattern(" * ")
                .pattern("#B#")
                .define('B', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('*', Items.NAUTILUS_SHELL)
                .define('#', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
                .save(this.output);
        // Suppressor
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.SUPRESSOR_ATTACHMENT_MODIFIER.get())
                .pattern("*C#")
                .define('C', IronsArtificeRegistries.Items.CLOCKWORK_COMPONENTS)
                .define('#', commonTag("leathers"))
                .define('*', commonTag("ingots/gold"))
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.CLOCKWORK_COMPONENTS))
                .save(this.output);
        // Hook Shot
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.HOOK_SHOT_MODIFIER.get())
                .pattern("**B")
                .pattern(" C*")
                .pattern("C *")
                .define('B', IronsArtificeRegistries.Items.BULLET)
                .define('C', Items.IRON_CHAIN)
                .define('*', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BULLET))
                .save(this.output);
        // Bloodletting Tip
        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, IronsArtificeRegistries.Items.BLOODLETTING_TIP_MODIFIER.get())
                .pattern(" BB")
                .pattern("#*B")
                .pattern("$# ")
                .define('#', IronsArtificeRegistries.Items.BLACKPOWDER)
                .define('B', Items.QUARTZ)
                .define('*', Items.GHAST_TEAR)
                .define('$', Items.REDSTONE)
                .unlockedBy("has_precursor", this.has(IronsArtificeRegistries.Items.BLACKPOWDER))
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
