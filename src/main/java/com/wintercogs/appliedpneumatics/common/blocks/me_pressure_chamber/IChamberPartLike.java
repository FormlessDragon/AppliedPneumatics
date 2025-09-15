package com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

public interface IChamberPartLike
{
    void onChamberFormed(ServerLevel level, BlockPos selfPos, FormedStructure fs);
    void onChamberBroken(ServerLevel level, BlockPos selfPos, Set<BlockPos> oldShell);
}
