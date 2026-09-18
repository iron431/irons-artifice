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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class RecipeDataGenerator extends RecipeProvider {
    public RecipeDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        /* **********************************
         * Blackpowder
         ********************************** */
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ItemRegistry.BLACKPOWDER.get(), 6)
                .requires(Items.GUNPOWDER)
                .requires(Items.CHARCOAL)
                .requires(Items.REDSTONE)
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(output, recipeId("blackpowder_from_gunpowder"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ItemRegistry.BLACKPOWDER.get(), 2)
                .requires(Items.CHARCOAL)
                .requires(Items.REDSTONE)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output, recipeId("blackpowder"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.BLACKPOWDER.get(), 1)
                .pattern("##")
                .define('#', Items.CHARCOAL)
                .unlockedBy("has_charcoal", has(Items.CHARCOAL))
                .save(output, recipeId("blackpowder_from_charcoal"));
        /* **********************************
         * Bullets
         ********************************** */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.BULLET.get(), 16)
                .pattern("#")
                .pattern("^")
                .define('#', commonTag("ingots/iron"))
                .define('^', ItemRegistry.BLACKPOWDER.get())
                .unlockedBy("has_blackpowder", has(ItemRegistry.BLACKPOWDER))
                .save(output, recipeId("bullet_from_iron"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.BULLET.get(), 4)
                .pattern("#")
                .pattern("^")
                .define('#', commonTag("ingots/copper"))
                .define('^', ItemRegistry.BLACKPOWDER.get())
                .unlockedBy("has_blackpowder", has(ItemRegistry.BLACKPOWDER))
                .save(output, recipeId("bullet_from_copper"));
        /* **********************************
         * Armor
         ********************************** */
        // Cowboy Hat
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.COWBOY_HAT.get())
                .pattern("B#B")
                .pattern("***")
                .define('*', commonTag("leathers"))
                .define('B', ItemRegistry.BULLET)
                .define('#', Items.LEATHER_HELMET)
                .unlockedBy("precursor", has(ItemRegistry.BULLET))
                .save(output);
        // Tricorne
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.TRICORNE_HAT.get())
                .pattern("***")
                .pattern("B#F")
                .define('*', commonTag("leathers"))
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('#', Items.LEATHER_HELMET)
                .define('F', Items.FEATHER)
                .unlockedBy("precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        /* **********************************
         * Mechanical Components
         ********************************** */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS.get())
                .pattern("CIC")
                .pattern("INI")
                .pattern("CIC")
                .define('N', commonTag("nuggets/copper"))
                .define('C', Items.CHAIN)
                .define('I', commonTag("ingots/copper"))
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.MECHANICAL_COMPONENTS.get())
                .pattern("BCR")
                .pattern("CMC")
                .pattern("RCN")
                .define('B', commonTag("storage_blocks/iron"))
                .define('N', commonTag("nuggets/iron"))
                .define('C', Items.CHAIN)
                .define('R', Items.REDSTONE)
                .define('M', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_simple", has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.CLOCKWORK_COMPONENTS.get())
                .pattern("MI ")
                .pattern("IRI")
                .pattern(" IM")
                .define('I', commonTag("ingots/gold"))
                .define('R', Items.REDSTONE)
                .define('M', ItemRegistry.MECHANICAL_COMPONENTS)
                .unlockedBy("has_mechanical", has(ItemRegistry.MECHANICAL_COMPONENTS))
                .save(output);
        /* **********************************
         * Guns
         ********************************** */
        // Flintlock
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.FLINTLOCK_PISTOL.get())
                .pattern("I  ")
                .pattern(" IF")
                .pattern(" LB")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('F', Items.FLINT_AND_STEEL)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", has(Items.IRON_INGOT))
                .save(output);
        // Musket
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.MUSKET.get())
                .pattern("I  ")
                .pattern(" MF")
                .pattern(" LB")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('F', Items.FLINT_AND_STEEL)
                .define('M', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", has(Items.IRON_INGOT))
                .save(output);
        // Blackpowder Revolver
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.BLACKPOWDER_REVOLVER.get())
                .pattern("I  ")
                .pattern(" HM")
                .pattern(" LB")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('H', Items.HOPPER)
                .define('M', ItemRegistry.MECHANICAL_COMPONENTS)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", has(Items.IRON_INGOT))
                .save(output);
        // Six Shooter
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.SIX_SHOOTER.get())
                .pattern("I  ")
                .pattern(" HM")
                .pattern(" IL")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('H', Items.HOPPER)
                .define('M', ItemRegistry.MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", has(Items.IRON_INGOT))
                .save(output);
        // Blunderbuss
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.BLUNDERBUSS.get())
                .pattern("MI ")
                .pattern("IMI")
                .pattern(" IL")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('M', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", has(Items.IRON_INGOT))
                .save(output);
        // Arquebus
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.ARQUEBUS.get())
                .pattern("I  ")
                .pattern(" IM")
                .pattern(" LB")
                .define('I', commonTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('M', ItemRegistry.CLOCKWORK_COMPONENTS)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", has(Items.IRON_INGOT))
                .save(output);
        // Clockwork Rifle
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.CLOCKWORK_RIFLE.get())
                .pattern("I  ")
                .pattern(" HR")
                .pattern(" LM")
                .define('I', commonTag("ingots/netherite"))
                .define('L', ItemTags.LOGS)
                .define('M', ItemRegistry.CLOCKWORK_COMPONENTS)
                .define('H', Items.HOPPER)
                .define('R', Items.REPEATER)
                .unlockedBy("has_precursor", has(Items.NETHERITE_INGOT))
                .save(output);
        /* **********************************
         * Modifier
         ********************************** */
        // Overcharged Powder
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.OVERCHARGED_POWDER.get())
                .pattern("BBB")
                .pattern("PRP")
                .pattern("BBB")
                .define('R', commonTag("storage_blocks/redstone"))
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('P', Items.BLAZE_POWDER)
                .unlockedBy("has_precursor", has(Items.BLAZE_POWDER))
                .save(output);
        // Steel Core
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.STEEL_CORE.get())
                .pattern(" I ")
                .pattern(" S ")
                .pattern("IBI")
                .define('S', commonTag("storage_blocks/iron"))
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('I', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Incendiary Tip
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.INCENDIARY_TIP_MODIFIER.get())
                .pattern(" P ")
                .pattern("PIP")
                .pattern("IBI")
                .define('P', Items.BLAZE_POWDER)
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('I', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Hair Trigger
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.HAIR_TRIGGER.get())
                .pattern("C")
                .pattern("R")
                .define('R', Items.BREEZE_ROD)
                .define('C', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(output);
        // Chain Lightning
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.CHAIN_LIGHTNING.get())
                .pattern(" R ")
                .pattern("RIR")
                .pattern("IBI")
                .define('R', Items.LIGHTNING_ROD)
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('I', commonTag("ingots/copper"))
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Frozen Jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.FROZEN_JACKET.get())
                .pattern(" * ")
                .pattern("*R*")
                .pattern("RBR")
                .define('R', Items.BLUE_ICE)
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('*', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Antigravity Powder
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.ANTIGRAVITY_MODIFIER.get())
                .pattern("BPB")
                .define('P', Items.ENDER_PEARL)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Wind Chamber
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.WIND_CHAMBER.get())
                .pattern("  P")
                .pattern("CB ")
                .pattern(" C ")
                .define('P', Items.WIND_CHARGE)
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('C', commonTag("ingots/copper"))
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Gas Vent
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.GAS_VENT.get())
                .pattern("BPB")
                .define('P', Items.HOPPER)
                .define('B', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(output);
        // Blackpowder Charge
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.BLACKPOWDER_CHARGE.get())
                .pattern("BSB")
                .pattern("BBB")
                .pattern("BBB")
                .define('S', Items.STRING)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Mechanical Repeater
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.MECHANICAL_REPEATER.get())
                .pattern("#B#")
                .pattern("***")
                .define('#', Items.CHAIN)
                .define('*', commonTag("ingots/gold"))
                .define('B', ItemRegistry.CLOCKWORK_COMPONENTS)
                .unlockedBy("has_precursor", has(ItemRegistry.CLOCKWORK_COMPONENTS))
                .save(output);
        // Chain Shot
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.CHAIN_SHOT.get())
                .pattern("###")
                .pattern("# #")
                .pattern("B B")
                .define('#', Items.CHAIN)
                .define('B', ItemRegistry.BULLET)
                .unlockedBy("has_precursor", has(ItemRegistry.BULLET))
                .save(output);
        // Buffer Spring
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.BUFFER_SPRING.get())
                .pattern("I I")
                .pattern("IBI")
                .pattern("I I")
                .define('I', commonTag("ingots/iron"))
                .define('B', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(output);
        // Breaching
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.BREACHING_SHELL.get())
                .pattern(" R ")
                .pattern("RBR")
                .pattern("III")
                .define('R', Items.FLINT)
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('I', commonTag("ingots/copper"))
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Venom
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.VENOM_CAPSULE.get())
                .pattern(" EE")
                .pattern(" GE")
                .pattern("B  ")
                .define('E', Items.SPIDER_EYE)
                .define('G', Items.GLASS_BOTTLE)
                .define('B', ItemRegistry.BULLET)
                .unlockedBy("has_precursor", has(ItemRegistry.BULLET))
                .save(output);
        // Scattershot
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.SCATTERSHOT.get())
                .pattern(" BB")
                .pattern("#PB")
                .pattern(" # ")
                .define('#', Items.STRING)
                .define('P', ItemRegistry.BLACKPOWDER)
                .define('B', ItemRegistry.BULLET)
                .unlockedBy("has_precursor", has(ItemRegistry.BULLET))
                .save(output);
        // Lead Core
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.LEAD_CORE.get())
                .pattern(" I ")
                .pattern(" S ")
                .pattern("IBI")
                .define('S', Items.DEEPSLATE_BRICKS)
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('I', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Trick Bullet
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.TRICK_BULLET_MODIFIER.get())
                .pattern(" I ")
                .pattern(" S ")
                .pattern("IBI")
                .define('S', commonTag("storage_blocks/gold"))
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('I', commonTag("ingots/gold"))
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Gun Oil
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.GUN_OIL.get())
                .pattern("LMR")
                .define('L', Items.HONEY_BOTTLE)
                .define('R', Items.REDSTONE)
                .define('M', ItemRegistry.MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", has(ItemRegistry.MECHANICAL_COMPONENTS))
                .save(output);
        // Singularity Charge
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.SINGULARITY_CHARGE_MODIFIER.get())
                .pattern(" #B")
                .pattern("#*#")
                .pattern("B# ")
                .define('#', Items.AMETHYST_SHARD)
                .define('*', Items.ENDER_EYE)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Enchanted Bullet
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.ENCHANTED_BULLET_MODIFIER.get())
                .pattern(" ##")
                .pattern("B*#")
                .pattern(" B ")
                .define('#', Items.LAPIS_LAZULI)
                .define('*', ItemRegistry.BULLET)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Seeking Powder
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.SEEKING_POWDER.get())
                .pattern("B*B")
                .define('*', Items.AMETHYST_CLUSTER)
                .define('B', ItemRegistry.BLACKPOWDER)
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Accelerating
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.MECHANICAL_ACCELERATOR_MODIFIER.get())
                .pattern("#B#")
                .pattern("***")
                .define('#', Items.CHAIN)
                .define('*', commonTag("ingots/copper"))
                .define('B', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(output);
        // Scope
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.SCOPE_ATTACHMENT_MODIFIER.get())
                .pattern("#")
                .pattern("*")
                .define('#', Items.SPYGLASS)
                .define('*', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(output);
        // Bayonet
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.BAYONET_ATTACHMENT_MODIFIER.get())
                .pattern("*")
                .pattern("#")
                .define('#', Items.IRON_SWORD)
                .define('*', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_precursor", has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(output);
        // Spiral Tip
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.SPIRAL_TIP_MODIFIER.get())
                .pattern(" * ")
                .pattern("#B#")
                .define('B', ItemRegistry.BLACKPOWDER)
                .define('*', Items.NAUTILUS_SHELL)
                .define('#', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);
        // Suppressor
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.SUPRESSOR_ATTACHMENT_MODIFIER.get())
                .pattern("*C#")
                .define('C', ItemRegistry.CLOCKWORK_COMPONENTS)
                .define('#', commonTag("leathers"))
                .define('*', commonTag("ingots/gold"))
                .unlockedBy("has_precursor", has(ItemRegistry.CLOCKWORK_COMPONENTS))
                .save(output);
        // Hook Shot
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.HOOK_SHOT_MODIFIER.get())
                .pattern("**B")
                .pattern(" C*")
                .pattern("C *")
                .define('B', ItemRegistry.BULLET)
                .define('C', Items.CHAIN)
                .define('*', commonTag("ingots/iron"))
                .unlockedBy("has_precursor", has(ItemRegistry.BULLET))
                .save(output);
        // Bloodletting Tip
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.BLOODLETTING_TIP_MODIFIER.get())
                .pattern(" BB")
                .pattern("#*B")
                .pattern("$# ")
                .define('#', ItemRegistry.BLACKPOWDER)
                .define('B', Items.QUARTZ)
                .define('*', Items.GHAST_TEAR)
                .define('$', Items.REDSTONE)
                .unlockedBy("has_precursor", has(ItemRegistry.BLACKPOWDER))
                .save(output);

    }

    private static TagKey<Item> commonTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private static ResourceLocation recipeId(String name) {
        return IronsArtifice.id(name);
    }
}
