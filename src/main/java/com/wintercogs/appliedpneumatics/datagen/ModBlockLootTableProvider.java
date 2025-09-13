package com.wintercogs.appliedpneumatics.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider
{

    protected ModBlockLootTableProvider(HolderLookup.Provider registries)
    {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate()
    {

    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks()
    {
        return new HashSet<Block>(); //给一个空集合，因为暂时没有方块
    }
}
