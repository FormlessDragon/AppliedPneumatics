package com.wintercogs.appliedpneumatics.datagen;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.wintercogs.appliedpneumatics.common.items.APItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder
{

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput)
    {
        // 1K
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_1K.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', AEBlocks.QUARTZ_GLASS)
                .define('B', Items.REDSTONE)
                .define('C', AEItems.CELL_COMPONENT_1K)
                .define('D', Items.IRON_INGOT)
                .define('E', Items.COPPER_INGOT)
                .unlockedBy("unlock_air_cell_1k", has(AEItems.CELL_COMPONENT_1K))
                .save(recipeOutput);

        // 4K
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_4K.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', AEBlocks.QUARTZ_GLASS)
                .define('B', Items.REDSTONE)
                .define('C', AEItems.CELL_COMPONENT_4K)
                .define('D', Items.IRON_INGOT)
                .define('E', Items.COPPER_INGOT)
                .unlockedBy("unlock_air_cell_4k", has(AEItems.CELL_COMPONENT_4K))
                .save(recipeOutput);

        // 16K
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_16K.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', AEBlocks.QUARTZ_GLASS)
                .define('B', Items.REDSTONE)
                .define('C', AEItems.CELL_COMPONENT_16K)
                .define('D', Items.IRON_INGOT)
                .define('E', Items.COPPER_INGOT)
                .unlockedBy("unlock_air_cell_16k", has(AEItems.CELL_COMPONENT_16K))
                .save(recipeOutput);

        // 64K
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_64K.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', AEBlocks.QUARTZ_GLASS)
                .define('B', Items.REDSTONE)
                .define('C', AEItems.CELL_COMPONENT_64K)
                .define('D', Items.IRON_INGOT)
                .define('E', Items.COPPER_INGOT)
                .unlockedBy("unlock_air_cell_64k", has(AEItems.CELL_COMPONENT_64K))
                .save(recipeOutput);

        // 256K
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_256K.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', AEBlocks.QUARTZ_GLASS)
                .define('B', Items.REDSTONE)
                .define('C', AEItems.CELL_COMPONENT_256K)
                .define('D', Items.IRON_INGOT)
                .define('E', Items.COPPER_INGOT)
                .unlockedBy("unlock_air_cell_256k", has(AEItems.CELL_COMPONENT_256K))
                .save(recipeOutput);

        // 1M
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_1M.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', AEBlocks.QUARTZ_GLASS)
                .define('B', Items.REDSTONE)
                .define('C', AEItems.CELL_COMPONENT_256K) // 临时统一用 256K 组件
                .define('D', Items.IRON_INGOT)
                .define('E', Items.COPPER_INGOT)
                .unlockedBy("unlock_air_cell_1m", has(AEItems.CELL_COMPONENT_256K))
                .save(recipeOutput);

        // 4M
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_4M.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', AEBlocks.QUARTZ_GLASS)
                .define('B', Items.REDSTONE)
                .define('C', AEItems.CELL_COMPONENT_256K)
                .define('D', Items.IRON_INGOT)
                .define('E', Items.COPPER_INGOT)
                .unlockedBy("unlock_air_cell_4m", has(AEItems.CELL_COMPONENT_256K))
                .save(recipeOutput);

        // 16M
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_16M.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', AEBlocks.QUARTZ_GLASS)
                .define('B', Items.REDSTONE)
                .define('C', AEItems.CELL_COMPONENT_256K)
                .define('D', Items.IRON_INGOT)
                .define('E', Items.COPPER_INGOT)
                .unlockedBy("unlock_air_cell_16m", has(AEItems.CELL_COMPONENT_256K))
                .save(recipeOutput);

        // 64M
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_64M.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', AEBlocks.QUARTZ_GLASS)
                .define('B', Items.REDSTONE)
                .define('C', AEItems.CELL_COMPONENT_256K)
                .define('D', Items.IRON_INGOT)
                .define('E', Items.COPPER_INGOT)
                .unlockedBy("unlock_air_cell_64m", has(AEItems.CELL_COMPONENT_256K))
                .save(recipeOutput);

        // 256M
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_256M.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', AEBlocks.QUARTZ_GLASS)
                .define('B', Items.REDSTONE)
                .define('C', AEItems.CELL_COMPONENT_256K)
                .define('D', Items.IRON_INGOT)
                .define('E', Items.COPPER_INGOT)
                .unlockedBy("unlock_air_cell_256m", has(AEItems.CELL_COMPONENT_256K))
                .save(recipeOutput);


    }
}
