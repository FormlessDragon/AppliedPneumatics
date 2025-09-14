package com.wintercogs.appliedpneumatics.common.tags;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class APBlockTags
{
    public static final TagKey<Block> CHAMBER_FRAME_TAG =
            TagKey.create(Registries.BLOCK, ResourceLocation.tryBuild(AppliedPneumatics.MODID, "me_pressure_chamber_frame"));
    public static final TagKey<Block> CHAMBER_EDGE_TAG =
            TagKey.create(Registries.BLOCK, ResourceLocation.tryBuild(AppliedPneumatics.MODID, "me_pressure_chamber_edge"));
}
