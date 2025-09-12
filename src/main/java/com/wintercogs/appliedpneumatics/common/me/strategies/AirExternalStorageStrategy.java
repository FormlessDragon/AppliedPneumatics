package com.wintercogs.appliedpneumatics.common.me.strategies;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.storage.MEStorage;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

// 存储总线等使用的逻辑
public class AirExternalStorageStrategy implements ExternalStorageStrategy
{
    private final ServerLevel serverLevel;
    private final BlockPos blockPos;
    private final Direction side;

    public AirExternalStorageStrategy(ServerLevel serverLevel, BlockPos blockPos, Direction side)
    {
        this.serverLevel = serverLevel;
        this.blockPos = blockPos;
        this.side = side;
    }

    @Override
    public @Nullable MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback)
    {
        IAirHandlerMachine handlerMachine = serverLevel.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE, blockPos, side);
        if (handlerMachine == null) return null;
        return new AirMachineExternalStorageFacade(handlerMachine);
    }
}
