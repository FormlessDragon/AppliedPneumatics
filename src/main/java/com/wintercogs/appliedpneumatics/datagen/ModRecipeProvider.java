package com.wintercogs.appliedpneumatics.datagen;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import gripe._90.megacells.definition.MEGAItems;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
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
                .define('D', ModItems.COMPRESSED_IRON_INGOT)
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
                .define('D', ModItems.COMPRESSED_IRON_INGOT)
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
                .define('D', ModItems.COMPRESSED_IRON_INGOT)
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
                .define('D', ModItems.COMPRESSED_IRON_INGOT)
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
                .define('D', ModItems.COMPRESSED_IRON_INGOT)
                .define('E', Items.COPPER_INGOT)
                .unlockedBy("unlock_air_cell_256k", has(AEItems.CELL_COMPONENT_256K))
                .save(recipeOutput);

        if(AppliedPneumatics.MEGA_CELL_LOADED)
        {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_1M.get())
                    .pattern("ABA")
                    .pattern("BCB")
                    .pattern("DDD")
                    .define('A', AEItems.SKY_DUST)
                    .define('B', AEBlocks.QUARTZ_VIBRANT_GLASS)
                    .define('C', MEGAItems.CELL_COMPONENT_1M)
                    .define('D', ModItems.COMPRESSED_IRON_INGOT)
                    .unlockedBy("unlock_air_cell_1m", has(MEGAItems.CELL_COMPONENT_1M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_4M.get())
                    .pattern("ABA")
                    .pattern("BCB")
                    .pattern("DDD")
                    .define('A', AEItems.SKY_DUST)
                    .define('B', AEBlocks.QUARTZ_VIBRANT_GLASS)
                    .define('C', MEGAItems.CELL_COMPONENT_4M)
                    .define('D', ModItems.COMPRESSED_IRON_INGOT)
                    .unlockedBy("unlock_air_cell_4m", has(MEGAItems.CELL_COMPONENT_4M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_16M.get())
                    .pattern("ABA")
                    .pattern("BCB")
                    .pattern("DDD")
                    .define('A', AEItems.SKY_DUST)
                    .define('B', AEBlocks.QUARTZ_VIBRANT_GLASS)
                    .define('C', MEGAItems.CELL_COMPONENT_16M)
                    .define('D', ModItems.COMPRESSED_IRON_INGOT)
                    .unlockedBy("unlock_air_cell_16m", has(MEGAItems.CELL_COMPONENT_16M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_64M.get())
                    .pattern("ABA")
                    .pattern("BCB")
                    .pattern("DDD")
                    .define('A', AEItems.SKY_DUST)
                    .define('B', AEBlocks.QUARTZ_VIBRANT_GLASS)
                    .define('C', MEGAItems.CELL_COMPONENT_64M)
                    .define('D', ModItems.COMPRESSED_IRON_INGOT)
                    .unlockedBy("unlock_air_cell_64m", has(MEGAItems.CELL_COMPONENT_64M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_256M.get())
                    .pattern("ABA")
                    .pattern("BCB")
                    .pattern("DDD")
                    .define('A', AEItems.SKY_DUST)
                    .define('B', AEBlocks.QUARTZ_VIBRANT_GLASS)
                    .define('C', MEGAItems.CELL_COMPONENT_256M)
                    .define('D', ModItems.COMPRESSED_IRON_INGOT)
                    .unlockedBy("unlock_air_cell_256m", has(MEGAItems.CELL_COMPONENT_256M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));
        }

        // ME气压接口
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APBlocks.ME_PRESSURE_INTERFACE_BLOCK)
                .pattern("ABA")
                .pattern("C D")
                .pattern("ABA")
                .define('A', ModItems.COMPRESSED_IRON_INGOT)
                .define('B', Tags.Items.GLASS_BLOCKS)
                .define('C', AEItems.ANNIHILATION_CORE)
                .define('D', AEItems.FORMATION_CORE)
                .unlockedBy("unlock_me_pressure_interface_block", has(AEItems.CELL_COMPONENT_256K))
                .save(recipeOutput);

        // ME气压室阀门
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APBlocks.ME_PRESSURE_CHAMBER_VALVE)
                .requires(ModBlocks.PRESSURE_CHAMBER_VALVE)
                .requires(AEBlocks.INTERFACE.block())
                .unlockedBy("unlock_me_pressure_chamber_valve", has(ModBlocks.PRESSURE_CHAMBER_VALVE))
                .save(recipeOutput);

        // ME气压室墙壁
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APBlocks.ME_PRESSURE_CHAMBER_WALL)
                .requires(ModBlocks.PRESSURE_CHAMBER_WALL)
                .requires(AEBlocks.INTERFACE.block())
                .unlockedBy("unlock_me_pressure_chamber_wall", has(ModBlocks.PRESSURE_CHAMBER_WALL))
                .save(recipeOutput);

        // ME气压室玻璃
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APBlocks.ME_PRESSURE_CHAMBER_GLASS)
                .requires(ModBlocks.PRESSURE_CHAMBER_GLASS)
                .requires(AEBlocks.INTERFACE.block())
                .unlockedBy("unlock_me_pressure_chamber_wall", has(ModBlocks.PRESSURE_CHAMBER_GLASS))
                .save(recipeOutput);

        // ME气压室聚能玻璃
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APBlocks.ME_PRESSURE_CHAMBER_VIBRANT_GLASS)
                .requires(AEBlocks.QUARTZ_VIBRANT_GLASS.block())
                .requires(AEBlocks.INTERFACE.block())
                .unlockedBy("unlock_me_pressure_chamber_wall", has(AEBlocks.QUARTZ_VIBRANT_GLASS.block()))
                .save(recipeOutput);
    }
}
