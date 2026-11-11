package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.registry.IronsArtificeItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class RecipeDataGenerator extends RecipeProvider {
    public RecipeDataGenerator(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        /* **********************************
         * Blackpowder
         ********************************** */
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, IronsArtificeItems.BLACKPOWDER.get(), 6)
                .requires(Items.GUNPOWDER)
                .requires(Items.CHARCOAL)
                .requires(Items.REDSTONE)
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(output, recipeId("blackpowder_from_gunpowder"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, IronsArtificeItems.BLACKPOWDER.get(), 2)
                .requires(Items.CHARCOAL)
                .requires(Items.REDSTONE)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output, recipeId("blackpowder"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.BLACKPOWDER.get(), 1)
                .pattern("##")
                .define('#', Items.CHARCOAL)
                .unlockedBy("has_charcoal", has(Items.CHARCOAL))
                .save(output, recipeId("blackpowder_from_charcoal"));
        /* **********************************
         * Bullets
         ********************************** */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.BULLET.get(), 16)
                .pattern("#")
                .pattern("^")
                .define('#', forgeTag("ingots/iron"))
                .define('^', IronsArtificeItems.BLACKPOWDER.get())
                .unlockedBy("has_blackpowder", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output, recipeId("bullet_from_iron"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.BULLET.get(), 4)
                .pattern("#")
                .pattern("^")
                .define('#', forgeTag("ingots/copper"))
                .define('^', IronsArtificeItems.BLACKPOWDER.get())
                .unlockedBy("has_blackpowder", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output, recipeId("bullet_from_copper"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, IronsArtificeItems.BULLET_BOX.get())
                .pattern("###")
                .pattern("***")
                .pattern("###")
                .define('*', IronsArtificeItems.BULLET.get())
                .define('#', forgeTag("ingots/gold"))
                .unlockedBy("has_bullet", has(IronsArtificeItems.BULLET.get()))
                .save(output);
        /* **********************************
         * Armor
         ********************************** */
        // Cowboy Hat
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.COWBOY_HAT.get())
                .pattern("B#B")
                .pattern("***")
                .define('*', forgeTag("leather"))
                .define('B', IronsArtificeItems.BULLET.get())
                .define('#', Items.LEATHER_HELMET)
                .unlockedBy("precursor", has(IronsArtificeItems.BULLET.get()))
                .save(output);
        // Tricorne
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.TRICORNE_HAT.get())
                .pattern("***")
                .pattern("B#F")
                .define('*', forgeTag("leather"))
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .define('#', Items.LEATHER_HELMET)
                .define('F', Items.FEATHER)
                .unlockedBy("precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        /* **********************************
         * Mechanical Components
         ********************************** */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get())
                .pattern("III")
                .pattern("IBI")
                .pattern("III")
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .define('I', forgeTag("ingots/copper"))
                .unlockedBy("has_blackpowder", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.MECHANICAL_COMPONENTS.get())
                .pattern("BCR")
                .pattern("CMC")
                .pattern("RCN")
                .define('B', forgeTag("storage_blocks/iron"))
                .define('N', forgeTag("nuggets/iron"))
                .define('C', Items.CHAIN)
                .define('R', Items.REDSTONE)
                .define('M', IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get())
                .unlockedBy("has_simple", has(IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.CLOCKWORK_COMPONENTS.get())
                .pattern("MI ")
                .pattern("IRI")
                .pattern(" IM")
                .define('I', forgeTag("ingots/gold"))
                .define('R', Items.REDSTONE)
                .define('M', IronsArtificeItems.MECHANICAL_COMPONENTS.get())
                .unlockedBy("has_mechanical", has(IronsArtificeItems.MECHANICAL_COMPONENTS.get()))
                .save(output);
        /* **********************************
         * Guns
         ********************************** */
        // Flintlock
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.FLINTLOCK_PISTOL.get())
                .pattern("I  ")
                .pattern(" IF")
                .pattern(" LB")
                .define('I', forgeTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('F', Items.FLINT_AND_STEEL)
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .unlockedBy("has_precursor", has(Items.IRON_INGOT))
                .save(output);
        // Musket
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.MUSKET.get())
                .pattern("I  ")
                .pattern(" MF")
                .pattern(" LB")
                .define('I', forgeTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('F', Items.FLINT_AND_STEEL)
                .define('M', IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get())
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .unlockedBy("has_precursor", has(Items.IRON_INGOT))
                .save(output);
        // Blackpowder Revolver
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.BLACKPOWDER_REVOLVER.get())
                .pattern("I  ")
                .pattern(" HM")
                .pattern(" LB")
                .define('I', forgeTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('H', Items.HOPPER)
                .define('M', IronsArtificeItems.MECHANICAL_COMPONENTS.get())
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .unlockedBy("has_precursor", has(Items.IRON_INGOT))
                .save(output);
        // Six Shooter
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.SIX_SHOOTER.get())
                .pattern("I  ")
                .pattern(" HM")
                .pattern(" IL")
                .define('I', forgeTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('H', Items.HOPPER)
                .define('M', IronsArtificeItems.MECHANICAL_COMPONENTS.get())
                .unlockedBy("has_precursor", has(Items.IRON_INGOT))
                .save(output);
        // Blunderbuss
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.BLUNDERBUSS.get())
                .pattern("MI ")
                .pattern("IMI")
                .pattern(" IL")
                .define('I', forgeTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('M', IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get())
                .unlockedBy("has_precursor", has(Items.IRON_INGOT))
                .save(output);
        // Arquebus
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.ARQUEBUS.get())
                .pattern("I  ")
                .pattern(" IM")
                .pattern(" LB")
                .define('I', forgeTag("ingots/iron"))
                .define('L', ItemTags.LOGS)
                .define('M', IronsArtificeItems.CLOCKWORK_COMPONENTS.get())
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .unlockedBy("has_precursor", has(Items.IRON_INGOT))
                .save(output);
        // Clockwork Rifle
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.CLOCKWORK_RIFLE.get())
                .pattern("I  ")
                .pattern(" HR")
                .pattern(" LM")
                .define('I', forgeTag("ingots/netherite"))
                .define('L', ItemTags.LOGS)
                .define('M', IronsArtificeItems.CLOCKWORK_COMPONENTS.get())
                .define('H', Items.HOPPER)
                .define('R', Items.REPEATER)
                .unlockedBy("has_precursor", has(Items.NETHERITE_INGOT))
                .save(output);
        /* **********************************
         * Modifier
         ********************************** */
        // Overcharged Powder
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.OVERCHARGED_POWDER.get())
                .pattern("BBB")
                .pattern("PRP")
                .pattern("BBB")
                .define('R', forgeTag("storage_blocks/redstone"))
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .define('P', Items.BLAZE_POWDER)
                .unlockedBy("has_precursor", has(Items.BLAZE_POWDER))
                .save(output);
        // Steel Core
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.STEEL_CORE.get())
                .pattern(" I ")
                .pattern(" S ")
                .pattern("IBI")
                .define('S', forgeTag("storage_blocks/iron"))
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .define('I', forgeTag("ingots/iron"))
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Incendiary Tip
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.INCENDIARY_TIP_MODIFIER.get())
                .pattern(" P ")
                .pattern("PIP")
                .pattern("IBI")
                .define('P', Items.BLAZE_POWDER)
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .define('I', forgeTag("ingots/iron"))
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Hair Trigger
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.HAIR_TRIGGER.get())
                .pattern("C")
                .pattern("R")
                .pattern("R")
                .define('R', forgeTag("ingots/copper"))
                .define('C', IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get()))
                .save(output);
        // Chain Lightning
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.CHAIN_LIGHTNING.get())
                .pattern(" R ")
                .pattern("RIR")
                .pattern("IBI")
                .define('R', Items.LIGHTNING_ROD)
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .define('I', forgeTag("ingots/copper"))
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Frozen Jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.FROZEN_JACKET.get())
                .pattern(" * ")
                .pattern("*R*")
                .pattern("RBR")
                .define('R', Items.BLUE_ICE)
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .define('*', forgeTag("ingots/iron"))
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Antigravity Powder
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.ANTIGRAVITY_MODIFIER.get())
                .pattern("BPB")
                .define('P', Items.ENDER_PEARL)
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Wind Chamber
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.WIND_CHAMBER.get())
                .pattern("  P")
                .pattern("CB ")
                .pattern(" C ")
                .define('P', Items.PHANTOM_MEMBRANE)
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .define('C', forgeTag("ingots/copper"))
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Gas Vent
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.GAS_VENT.get())
                .pattern("BPB")
                .define('P', Items.HOPPER)
                .define('B', IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get()))
                .save(output);
        // Blackpowder Charge
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.BLACKPOWDER_CHARGE.get())
                .pattern("BSB")
                .pattern("BBB")
                .pattern("BBB")
                .define('S', Items.STRING)
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Mechanical Repeater
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.MECHANICAL_REPEATER.get())
                .pattern("#B#")
                .pattern("***")
                .define('#', Items.CHAIN)
                .define('*', forgeTag("ingots/gold"))
                .define('B', IronsArtificeItems.CLOCKWORK_COMPONENTS.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.CLOCKWORK_COMPONENTS.get()))
                .save(output);
        // Chain Shot
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.CHAIN_SHOT.get())
                .pattern("###")
                .pattern("# #")
                .pattern("B B")
                .define('#', Items.CHAIN)
                .define('B', IronsArtificeItems.BULLET.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.BULLET.get()))
                .save(output);
        // Buffer Spring
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.BUFFER_SPRING.get())
                .pattern("I I")
                .pattern("IBI")
                .pattern("I I")
                .define('I', forgeTag("ingots/iron"))
                .define('B', IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get()))
                .save(output);
        // Breaching
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.BREACHING_SHELL.get())
                .pattern(" R ")
                .pattern("RBR")
                .pattern("III")
                .define('R', Items.FLINT)
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .define('I', forgeTag("ingots/copper"))
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Venom
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.VENOM_CAPSULE.get())
                .pattern(" EE")
                .pattern(" GE")
                .pattern("B  ")
                .define('E', Items.SPIDER_EYE)
                .define('G', Items.GLASS_BOTTLE)
                .define('B', IronsArtificeItems.BULLET.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.BULLET.get()))
                .save(output);
        // Scattershot
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.SCATTERSHOT.get())
                .pattern(" BB")
                .pattern("#PB")
                .pattern(" # ")
                .define('#', Items.STRING)
                .define('P', IronsArtificeItems.BLACKPOWDER.get())
                .define('B', IronsArtificeItems.BULLET.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.BULLET.get()))
                .save(output);
        // Lead Core
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.LEAD_CORE.get())
                .pattern(" I ")
                .pattern(" S ")
                .pattern("IBI")
                .define('S', Items.DEEPSLATE_BRICKS)
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .define('I', forgeTag("ingots/iron"))
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Trick Bullet
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.TRICK_BULLET_MODIFIER.get())
                .pattern(" I ")
                .pattern(" S ")
                .pattern("IBI")
                .define('S', forgeTag("storage_blocks/gold"))
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .define('I', forgeTag("ingots/gold"))
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Gun Oil
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.GUN_OIL.get())
                .pattern("LMR")
                .define('L', Items.HONEY_BOTTLE)
                .define('R', Items.REDSTONE)
                .define('M', IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get()))
                .save(output);
        // Soulfire Coin
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.SOULFIRE_COIN.get())
                .pattern("SLS")
                .define('S', Items.NETHERITE_SCRAP)
                .define('L', Items.SOUL_LANTERN)
                .unlockedBy("has_precursor", has(Items.SOUL_LANTERN))
                .save(output);
        // Singularity Charge
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.SINGULARITY_CHARGE_MODIFIER.get())
                .pattern(" #B")
                .pattern("#*#")
                .pattern("B# ")
                .define('#', Items.AMETHYST_SHARD)
                .define('*', Items.ENDER_EYE)
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Enchanted Bullet
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.ENCHANTED_BULLET_MODIFIER.get())
                .pattern(" ##")
                .pattern("B*#")
                .pattern(" B ")
                .define('#', Items.LAPIS_LAZULI)
                .define('*', IronsArtificeItems.BULLET.get())
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Seeking Powder
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.SEEKING_POWDER.get())
                .pattern("B*B")
                .define('*', Items.AMETHYST_CLUSTER)
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Accelerating
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.MECHANICAL_ACCELERATOR_MODIFIER.get())
                .pattern("#B#")
                .pattern("***")
                .define('#', Items.CHAIN)
                .define('*', forgeTag("ingots/copper"))
                .define('B', IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get()))
                .save(output);
        // Scope
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.SCOPE_ATTACHMENT_MODIFIER.get())
                .pattern("#")
                .pattern("*")
                .define('#', Items.SPYGLASS)
                .define('*', IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get()))
                .save(output);
        // Bayonet
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.BAYONET_ATTACHMENT_MODIFIER.get())
                .pattern("*")
                .pattern("#")
                .define('#', Items.IRON_SWORD)
                .define('*', IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get())
                .unlockedBy("has_precursor", has(IronsArtificeItems.SIMPLE_MECHANICAL_COMPONENTS.get()))
                .save(output);
        // Spiral Tip
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.SPIRAL_TIP_MODIFIER.get())
                .pattern(" * ")
                .pattern("#B#")
                .define('B', IronsArtificeItems.BLACKPOWDER.get())
                .define('*', Items.NAUTILUS_SHELL)
                .define('#', forgeTag("ingots/iron"))
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);
        // Suppressor
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.SUPRESSOR_ATTACHMENT_MODIFIER.get())
                .pattern("*C#")
                .define('C', IronsArtificeItems.CLOCKWORK_COMPONENTS.get())
                .define('#', forgeTag("leather"))
                .define('*', forgeTag("ingots/gold"))
                .unlockedBy("has_precursor", has(IronsArtificeItems.CLOCKWORK_COMPONENTS.get()))
                .save(output);
        // Hook Shot
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.HOOK_SHOT_MODIFIER.get())
                .pattern("**B")
                .pattern(" C*")
                .pattern("C *")
                .define('B', IronsArtificeItems.BULLET.get())
                .define('C', Items.CHAIN)
                .define('*', forgeTag("ingots/iron"))
                .unlockedBy("has_precursor", has(IronsArtificeItems.BULLET.get()))
                .save(output);
        // Bloodletting Tip
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IronsArtificeItems.BLOODLETTING_TIP_MODIFIER.get())
                .pattern(" BB")
                .pattern("#*B")
                .pattern("$# ")
                .define('#', IronsArtificeItems.BLACKPOWDER.get())
                .define('B', Items.QUARTZ)
                .define('*', Items.GHAST_TEAR)
                .define('$', Items.REDSTONE)
                .unlockedBy("has_precursor", has(IronsArtificeItems.BLACKPOWDER.get()))
                .save(output);

    }

    private static TagKey<Item> forgeTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", path));
    }

    private static ResourceLocation recipeId(String name) {
        return IronsArtifice.id(name);
    }
}
