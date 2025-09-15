package com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

public interface IChamberControllerLike extends IChamberPartLike
{
    void onControlFormed(ServerLevel level, BlockPos controllerPos, FormedStructure fs);
    void onControlBroken(ServerLevel level, BlockPos controllerPos, Set<BlockPos> oldShell);
}
