package com.wintercogs.appliedpneumatics.datagen;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import com.wintercogs.appliedpneumatics.datagen.builder.CellDisassemblyRecipeBuilder;
import gripe._90.megacells.definition.MEGAItems;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
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
        // 空气元件外壳
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_SHELL.get())
                .pattern("ABA")
                .pattern("B B")
                .pattern("DED")
                .define('A', AEBlocks.QUARTZ_GLASS)
                .define('B', Items.REDSTONE)
                .define('D', ModItems.COMPRESSED_IRON_INGOT)
                .define('E', Items.COPPER_INGOT)
                .unlockedBy("unlock_air_cell_shell", has(ModItems.COMPRESSED_IRON_INGOT))
                .save(recipeOutput);

        // 存储元件1k~256m
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
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.AIR_CELL_1K.get())
                .requires(APItems.AIR_CELL_SHELL)
                .requires(AEItems.CELL_COMPONENT_1K)
                .unlockedBy("unlock_air_cell_1k", has(AEItems.CELL_COMPONENT_1K))
                .save(recipeOutput, AppliedPneumatics.makeId("air_cell_1k_from_shell"));
        CellDisassemblyRecipeBuilder.cell(APItems.AIR_CELL_1K.get())
                .add(APItems.AIR_CELL_SHELL)
                .add(AEItems.CELL_COMPONENT_1K)
                .save(recipeOutput);

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
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.AIR_CELL_4K.get())
                .requires(APItems.AIR_CELL_SHELL)
                .requires(AEItems.CELL_COMPONENT_4K)
                .unlockedBy("unlock_air_cell_4k", has(AEItems.CELL_COMPONENT_4K))
                .save(recipeOutput, AppliedPneumatics.makeId("air_cell_4k_from_shell"));
        CellDisassemblyRecipeBuilder.cell(APItems.AIR_CELL_4K.get())
                .add(APItems.AIR_CELL_SHELL)
                .add(AEItems.CELL_COMPONENT_4K)
                .save(recipeOutput);

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
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.AIR_CELL_16K.get())
                .requires(APItems.AIR_CELL_SHELL)
                .requires(AEItems.CELL_COMPONENT_16K)
                .unlockedBy("unlock_air_cell_4k", has(AEItems.CELL_COMPONENT_16K))
                .save(recipeOutput, AppliedPneumatics.makeId("air_cell_16k_from_shell"));
        CellDisassemblyRecipeBuilder.cell(APItems.AIR_CELL_16K.get())
                .add(APItems.AIR_CELL_SHELL)
                .add(AEItems.CELL_COMPONENT_16K)
                .save(recipeOutput);

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
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.AIR_CELL_64K.get())
                .requires(APItems.AIR_CELL_SHELL)
                .requires(AEItems.CELL_COMPONENT_64K)
                .unlockedBy("unlock_air_cell_4k", has(AEItems.CELL_COMPONENT_64K))
                .save(recipeOutput, AppliedPneumatics.makeId("air_cell_64k_from_shell"));
        CellDisassemblyRecipeBuilder.cell(APItems.AIR_CELL_64K.get())
                .add(APItems.AIR_CELL_SHELL)
                .add(AEItems.CELL_COMPONENT_64K)
                .save(recipeOutput);

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
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.AIR_CELL_256K.get())
                .requires(APItems.AIR_CELL_SHELL)
                .requires(AEItems.CELL_COMPONENT_256K)
                .unlockedBy("unlock_air_cell_4k", has(AEItems.CELL_COMPONENT_256K))
                .save(recipeOutput, AppliedPneumatics.makeId("air_cell_256k_from_shell"));
        CellDisassemblyRecipeBuilder.cell(APItems.AIR_CELL_256K.get())
                .add(APItems.AIR_CELL_SHELL)
                .add(AEItems.CELL_COMPONENT_256K)
                .save(recipeOutput);

        if(AppliedPneumatics.MEGA_CELL_LOADED)
        {
            // MEGA空气元件外壳
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.MEGA_AIR_CELL_SHELL.get())
                    .pattern("ABA")
                    .pattern("B B")
                    .pattern("DCD")
                    .define('A', AEItems.SKY_DUST)
                    .define('B', AEBlocks.QUARTZ_VIBRANT_GLASS)
                    .define('C', ModItems.PRINTED_CIRCUIT_BOARD)
                    .define('D', ModItems.COMPRESSED_IRON_INGOT)
                    .unlockedBy("unlock_mega_air_cell_shell", has(AEItems.SKY_DUST))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_1M.get())
                    .pattern("ABA")
                    .pattern("BCB")
                    .pattern("DED")
                    .define('A', AEItems.SKY_DUST)
                    .define('B', AEBlocks.QUARTZ_VIBRANT_GLASS)
                    .define('C', MEGAItems.CELL_COMPONENT_1M)
                    .define('D', ModItems.COMPRESSED_IRON_INGOT)
                    .define('E', ModItems.PRINTED_CIRCUIT_BOARD)
                    .unlockedBy("unlock_air_cell_1m", has(MEGAItems.CELL_COMPONENT_1M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.AIR_CELL_1M.get())
                    .requires(APItems.MEGA_AIR_CELL_SHELL)
                    .requires(MEGAItems.CELL_COMPONENT_1M)
                    .unlockedBy("unlock_air_cell_4k", has(MEGAItems.CELL_COMPONENT_1M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)),
                            AppliedPneumatics.makeId("air_cell_1m_from_shell"));
            CellDisassemblyRecipeBuilder.cell(APItems.AIR_CELL_1M.get())
                    .add(APItems.MEGA_AIR_CELL_SHELL)
                    .add(MEGAItems.CELL_COMPONENT_1M)
                    .whenModLoaded(AppliedPneumatics.MEGA_CELL_MODID)
                    .save(recipeOutput);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_4M.get())
                    .pattern("ABA")
                    .pattern("BCB")
                    .pattern("DED")
                    .define('A', AEItems.SKY_DUST)
                    .define('B', AEBlocks.QUARTZ_VIBRANT_GLASS)
                    .define('C', MEGAItems.CELL_COMPONENT_4M)
                    .define('D', ModItems.COMPRESSED_IRON_INGOT)
                    .define('E', ModItems.PRINTED_CIRCUIT_BOARD)
                    .unlockedBy("unlock_air_cell_4m", has(MEGAItems.CELL_COMPONENT_4M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.AIR_CELL_4M.get())
                    .requires(APItems.MEGA_AIR_CELL_SHELL)
                    .requires(MEGAItems.CELL_COMPONENT_4M)
                    .unlockedBy("unlock_air_cell_4k", has(MEGAItems.CELL_COMPONENT_4M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)),
                            AppliedPneumatics.makeId("air_cell_4m_from_shell"));
            CellDisassemblyRecipeBuilder.cell(APItems.AIR_CELL_4M.get())
                    .add(APItems.MEGA_AIR_CELL_SHELL)
                    .add(MEGAItems.CELL_COMPONENT_4M)
                    .whenModLoaded(AppliedPneumatics.MEGA_CELL_MODID)
                    .save(recipeOutput);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_16M.get())
                    .pattern("ABA")
                    .pattern("BCB")
                    .pattern("DED")
                    .define('A', AEItems.SKY_DUST)
                    .define('B', AEBlocks.QUARTZ_VIBRANT_GLASS)
                    .define('C', MEGAItems.CELL_COMPONENT_16M)
                    .define('D', ModItems.COMPRESSED_IRON_INGOT)
                    .define('E', ModItems.PRINTED_CIRCUIT_BOARD)
                    .unlockedBy("unlock_air_cell_16m", has(MEGAItems.CELL_COMPONENT_16M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.AIR_CELL_16M.get())
                    .requires(APItems.MEGA_AIR_CELL_SHELL)
                    .requires(MEGAItems.CELL_COMPONENT_16M)
                    .unlockedBy("unlock_air_cell_4k", has(MEGAItems.CELL_COMPONENT_16M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)),
                            AppliedPneumatics.makeId("air_cell_16m_from_shell"));
            CellDisassemblyRecipeBuilder.cell(APItems.AIR_CELL_16M.get())
                    .add(APItems.MEGA_AIR_CELL_SHELL)
                    .add(MEGAItems.CELL_COMPONENT_16M)
                    .whenModLoaded(AppliedPneumatics.MEGA_CELL_MODID)
                    .save(recipeOutput);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_64M.get())
                    .pattern("ABA")
                    .pattern("BCB")
                    .pattern("DED")
                    .define('A', AEItems.SKY_DUST)
                    .define('B', AEBlocks.QUARTZ_VIBRANT_GLASS)
                    .define('C', MEGAItems.CELL_COMPONENT_64M)
                    .define('D', ModItems.COMPRESSED_IRON_INGOT)
                    .define('E', ModItems.PRINTED_CIRCUIT_BOARD)
                    .unlockedBy("unlock_air_cell_64m", has(MEGAItems.CELL_COMPONENT_64M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.AIR_CELL_64M.get())
                    .requires(APItems.MEGA_AIR_CELL_SHELL)
                    .requires(MEGAItems.CELL_COMPONENT_64M)
                    .unlockedBy("unlock_air_cell_4k", has(MEGAItems.CELL_COMPONENT_64M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)),
                            AppliedPneumatics.makeId("air_cell_64m_from_shell"));
            CellDisassemblyRecipeBuilder.cell(APItems.AIR_CELL_64M.get())
                    .add(APItems.MEGA_AIR_CELL_SHELL)
                    .add(MEGAItems.CELL_COMPONENT_64M)
                    .whenModLoaded(AppliedPneumatics.MEGA_CELL_MODID)
                    .save(recipeOutput);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AIR_CELL_256M.get())
                    .pattern("ABA")
                    .pattern("BCB")
                    .pattern("DED")
                    .define('A', AEItems.SKY_DUST)
                    .define('B', AEBlocks.QUARTZ_VIBRANT_GLASS)
                    .define('C', MEGAItems.CELL_COMPONENT_256M)
                    .define('D', ModItems.COMPRESSED_IRON_INGOT)
                    .define('E', ModItems.PRINTED_CIRCUIT_BOARD)
                    .unlockedBy("unlock_air_cell_256m", has(MEGAItems.CELL_COMPONENT_256M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.AIR_CELL_256M.get())
                    .requires(APItems.MEGA_AIR_CELL_SHELL)
                    .requires(MEGAItems.CELL_COMPONENT_256M)
                    .unlockedBy("unlock_air_cell_4k", has(MEGAItems.CELL_COMPONENT_256M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)),
                            AppliedPneumatics.makeId("air_cell_256m_from_shell"));
            CellDisassemblyRecipeBuilder.cell(APItems.AIR_CELL_256M.get())
                    .add(APItems.MEGA_AIR_CELL_SHELL)
                    .add(MEGAItems.CELL_COMPONENT_256M)
                    .whenModLoaded(AppliedPneumatics.MEGA_CELL_MODID)
                    .save(recipeOutput);
        }

        // 便携1k~256m
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.PORTABLE_AIR_CELL_1K.get())
                .requires(AEItems.CELL_COMPONENT_1K)
                .requires(AEBlocks.ME_CHEST)
                .requires(AEBlocks.ENERGY_CELL)
                .requires(APItems.AIR_CELL_SHELL)
                .unlockedBy("unlock_portable_air_cell_1k", has(AEItems.CELL_COMPONENT_1K))
                .save(recipeOutput);
        CellDisassemblyRecipeBuilder.cell(APItems.PORTABLE_AIR_CELL_1K.get())
                .add(AEItems.CELL_COMPONENT_1K)
                .add(AEBlocks.ME_CHEST)
                .add(AEBlocks.ENERGY_CELL)
                .add(APItems.AIR_CELL_SHELL)
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.PORTABLE_AIR_CELL_4K.get())
                .requires(AEItems.CELL_COMPONENT_4K)
                .requires(AEBlocks.ME_CHEST)
                .requires(AEBlocks.ENERGY_CELL)
                .requires(APItems.AIR_CELL_SHELL)
                .unlockedBy("unlock_portable_air_cell_4k", has(AEItems.CELL_COMPONENT_4K))
                .save(recipeOutput);
        CellDisassemblyRecipeBuilder.cell(APItems.PORTABLE_AIR_CELL_4K.get())
                .add(AEItems.CELL_COMPONENT_4K)
                .add(AEBlocks.ME_CHEST)
                .add(AEBlocks.ENERGY_CELL)
                .add(APItems.AIR_CELL_SHELL)
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.PORTABLE_AIR_CELL_16K.get())
                .requires(AEItems.CELL_COMPONENT_16K)
                .requires(AEBlocks.ME_CHEST)
                .requires(AEBlocks.ENERGY_CELL)
                .requires(APItems.AIR_CELL_SHELL)
                .unlockedBy("unlock_portable_air_cell_16k", has(AEItems.CELL_COMPONENT_16K))
                .save(recipeOutput);
        CellDisassemblyRecipeBuilder.cell(APItems.PORTABLE_AIR_CELL_16K.get())
                .add(AEItems.CELL_COMPONENT_16K)
                .add(AEBlocks.ME_CHEST)
                .add(AEBlocks.ENERGY_CELL)
                .add(APItems.AIR_CELL_SHELL)
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.PORTABLE_AIR_CELL_64K.get())
                .requires(AEItems.CELL_COMPONENT_64K)
                .requires(AEBlocks.ME_CHEST)
                .requires(AEBlocks.ENERGY_CELL)
                .requires(APItems.AIR_CELL_SHELL)
                .unlockedBy("unlock_portable_air_cell_64k", has(AEItems.CELL_COMPONENT_64K))
                .save(recipeOutput);
        CellDisassemblyRecipeBuilder.cell(APItems.PORTABLE_AIR_CELL_64K.get())
                .add(AEItems.CELL_COMPONENT_64K)
                .add(AEBlocks.ME_CHEST)
                .add(AEBlocks.ENERGY_CELL)
                .add(APItems.AIR_CELL_SHELL)
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.PORTABLE_AIR_CELL_256K.get())
                .requires(AEItems.CELL_COMPONENT_256K)
                .requires(AEBlocks.ME_CHEST)
                .requires(AEBlocks.ENERGY_CELL)
                .requires(APItems.AIR_CELL_SHELL)
                .unlockedBy("unlock_portable_air_cell_256k", has(AEItems.CELL_COMPONENT_256K))
                .save(recipeOutput);
        CellDisassemblyRecipeBuilder.cell(APItems.PORTABLE_AIR_CELL_256K.get())
                .add(AEItems.CELL_COMPONENT_256K)
                .add(AEBlocks.ME_CHEST)
                .add(AEBlocks.ENERGY_CELL)
                .add(APItems.AIR_CELL_SHELL)
                .save(recipeOutput);

        if (AppliedPneumatics.MEGA_CELL_LOADED)
        {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.PORTABLE_AIR_CELL_1M.get())
                    .requires(MEGAItems.CELL_COMPONENT_1M)
                    .requires(AEBlocks.ME_CHEST)
                    .requires(AEBlocks.ENERGY_CELL)
                    .requires(APItems.MEGA_AIR_CELL_SHELL)
                    .unlockedBy("unlock_portable_air_cell_1m", has(MEGAItems.CELL_COMPONENT_1M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));
            CellDisassemblyRecipeBuilder.cell(APItems.PORTABLE_AIR_CELL_1M.get())
                    .add(MEGAItems.CELL_COMPONENT_1M)
                    .add(AEBlocks.ME_CHEST)
                    .add(AEBlocks.ENERGY_CELL)
                    .add(APItems.MEGA_AIR_CELL_SHELL)
                    .whenModLoaded(AppliedPneumatics.MEGA_CELL_MODID)
                    .save(recipeOutput);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.PORTABLE_AIR_CELL_4M.get())
                    .requires(MEGAItems.CELL_COMPONENT_4M)
                    .requires(AEBlocks.ME_CHEST)
                    .requires(AEBlocks.ENERGY_CELL)
                    .requires(APItems.MEGA_AIR_CELL_SHELL)
                    .unlockedBy("unlock_portable_air_cell_4m", has(MEGAItems.CELL_COMPONENT_4M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));
            CellDisassemblyRecipeBuilder.cell(APItems.PORTABLE_AIR_CELL_4M.get())
                    .add(MEGAItems.CELL_COMPONENT_4M)
                    .add(AEBlocks.ME_CHEST)
                    .add(AEBlocks.ENERGY_CELL)
                    .add(APItems.MEGA_AIR_CELL_SHELL)
                    .whenModLoaded(AppliedPneumatics.MEGA_CELL_MODID)
                    .save(recipeOutput);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.PORTABLE_AIR_CELL_16M.get())
                    .requires(MEGAItems.CELL_COMPONENT_16M)
                    .requires(AEBlocks.ME_CHEST)
                    .requires(AEBlocks.ENERGY_CELL)
                    .requires(APItems.MEGA_AIR_CELL_SHELL)
                    .unlockedBy("unlock_portable_air_cell_16m", has(MEGAItems.CELL_COMPONENT_16M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));
            CellDisassemblyRecipeBuilder.cell(APItems.PORTABLE_AIR_CELL_16M.get())
                    .add(MEGAItems.CELL_COMPONENT_16M)
                    .add(AEBlocks.ME_CHEST)
                    .add(AEBlocks.ENERGY_CELL)
                    .add(APItems.MEGA_AIR_CELL_SHELL)
                    .whenModLoaded(AppliedPneumatics.MEGA_CELL_MODID)
                    .save(recipeOutput);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.PORTABLE_AIR_CELL_64M.get())
                    .requires(MEGAItems.CELL_COMPONENT_64M)
                    .requires(AEBlocks.ME_CHEST)
                    .requires(AEBlocks.ENERGY_CELL)
                    .requires(APItems.MEGA_AIR_CELL_SHELL)
                    .unlockedBy("unlock_portable_air_cell_64m", has(MEGAItems.CELL_COMPONENT_64M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));
            CellDisassemblyRecipeBuilder.cell(APItems.PORTABLE_AIR_CELL_64M.get())
                    .add(MEGAItems.CELL_COMPONENT_64M)
                    .add(AEBlocks.ME_CHEST)
                    .add(AEBlocks.ENERGY_CELL)
                    .add(APItems.MEGA_AIR_CELL_SHELL)
                    .whenModLoaded(AppliedPneumatics.MEGA_CELL_MODID)
                    .save(recipeOutput);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.PORTABLE_AIR_CELL_256M.get())
                    .requires(MEGAItems.CELL_COMPONENT_256M)
                    .requires(AEBlocks.ME_CHEST)
                    .requires(AEBlocks.ENERGY_CELL)
                    .requires(APItems.MEGA_AIR_CELL_SHELL)
                    .unlockedBy("unlock_portable_air_cell_256m", has(MEGAItems.CELL_COMPONENT_256M))
                    .save(recipeOutput.withConditions(new ModLoadedCondition(AppliedPneumatics.MEGA_CELL_MODID)));
            CellDisassemblyRecipeBuilder.cell(APItems.PORTABLE_AIR_CELL_256M.get())
                    .add(MEGAItems.CELL_COMPONENT_256M)
                    .add(AEBlocks.ME_CHEST)
                    .add(AEBlocks.ENERGY_CELL)
                    .add(APItems.MEGA_AIR_CELL_SHELL)
                    .whenModLoaded(AppliedPneumatics.MEGA_CELL_MODID)
                    .save(recipeOutput);
        }

        // ME气压接口
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APBlocks.ME_PRESSURE_INTERFACE_BLOCK)
                .pattern("ABA")
                .pattern("CED")
                .pattern("ABA")
                .define('A', ModItems.COMPRESSED_IRON_INGOT)
                .define('B', Tags.Items.GLASS_BLOCKS)
                .define('C', AEItems.ANNIHILATION_CORE)
                .define('D', AEItems.FORMATION_CORE)
                .define('E', ModBlocks.AIR_COMPRESSOR)
                .unlockedBy("unlock_me_pressure_interface_block", has(AEItems.CELL_COMPONENT_256K))
                .save(recipeOutput);

        // ME温控接口
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APBlocks.ME_TEMPERATURE_INTERFACE)
                .pattern("ABA")
                .pattern("DCE")
                .pattern("ABA")
                .define('A', ModBlocks.COMPRESSED_IRON_BLOCK)
                .define('B', ModBlocks.VORTEX_TUBE)
                .define('C', ModBlocks.ADVANCED_AIR_COMPRESSOR)
                .define('D', AEItems.ANNIHILATION_CORE)
                .define('E', AEItems.FORMATION_CORE)
                .unlockedBy("unlock_me_temperature_interface_block", has(ModBlocks.ADVANCED_AIR_COMPRESSOR))
                .save(recipeOutput);

        // 亚马龙无线终端
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.AMADRON_WIRELESS_TERMINAL)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', AEItems.WIRELESS_RECEIVER)
                .define('B', ModItems.AMADRON_TABLET)
                .define('C', AEBlocks.DENSE_ENERGY_CELL)
                .unlockedBy("unlock_amadron_wireless_terminal", has(ModItems.AMADRON_TABLET))
                .save(recipeOutput);

        // 亚马龙处理站
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APBlocks.ME_AMADRON_PROCESS_STATION)
                .pattern("EAE")
                .pattern("CBC")
                .pattern("EDE")
                .define('A', ModBlocks.CHARGING_STATION)
                .define('B', AEBlocks.PATTERN_PROVIDER)
                .define('C', AEBlocks.INTERFACE)
                .define('D', ModItems.PRINTED_CIRCUIT_BOARD)
                .define('E', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("unlock_me_amadron_process_station", has(APItems.AMADRON_WIRELESS_TERMINAL))
                .save(recipeOutput);

        // 扩展亚马龙处理站
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APBlocks.ME_AMADRON_EXTENDED_PROCESS_STATION)
                .requires(APBlocks.ME_AMADRON_PROCESS_STATION, 4)
                .unlockedBy("unlock_me_amadron_extended_process_station", has(APBlocks.ME_AMADRON_PROCESS_STATION))
                .save(recipeOutput.withConditions(modLoaded(AppliedPneumatics.EAE_MODID)));

        // 安全卡
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.SECURITY_CARD)
                .requires(AEItems.ADVANCED_CARD)
                .requires(ModUpgrades.SECURITY.get().getItem())
                .unlockedBy("unlock_security_card", has(AEItems.ADVANCED_CARD))
                .save(recipeOutput);

        // 容积卡
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.VOLUME_CARD)
                .requires(AEItems.ADVANCED_CARD)
                .requires(ModUpgrades.VOLUME.get().getItem())
                .unlockedBy("unlock_volume_card", has(AEItems.ADVANCED_CARD))
                .save(recipeOutput);

        // 充气卡
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, APItems.CHARGING_CARD)
                .requires(AEItems.ADVANCED_CARD)
                .requires(ModUpgrades.CHARGING.get().getItem())
                .unlockedBy("unlock_charge_card", has(AEItems.ADVANCED_CARD))
                .save(recipeOutput);

        // 真空卡
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, APItems.VACUUM_CARD)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.PRINTED_CIRCUIT_BOARD)
                .define('B', ModBlocks.VACUUM_PUMP)
                .define('C', AEItems.ADVANCED_CARD)
                .unlockedBy("unlock_vacuum_card", has(AEItems.ADVANCED_CARD))
                .save(recipeOutput);
    }
}
