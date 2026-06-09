package com.wintercogs.appliedpneumatics.common.me.air;

import ae2.api.behaviors.ExternalStorageStrategy;
import ae2.api.storage.MEStorage;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.Nullable;

public final class AirExternalStorageStrategy implements ExternalStorageStrategy {
    private final WorldServer level;
    private final BlockPos fromPos;
    private final EnumFacing fromSide;

    public AirExternalStorageStrategy(WorldServer level, BlockPos fromPos, EnumFacing fromSide) {
        this.level = level;
        this.fromPos = fromPos;
        this.fromSide = fromSide;
    }

    @Nullable
    @Override
    public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        IAirHandler handler = resolveAirHandler(this.level, this.fromPos, this.fromSide);
        if (handler == null) {
            return null;
        }

        AirExternalStorageFacade facade = new AirExternalStorageFacade(handler);
        facade.setExtractableOnly(extractableOnly);
        facade.setChangeListener(injectOrExtractCallback);
        return facade;
    }

    @Nullable
    static IAirHandler resolveAirHandler(@Nullable WorldServer level, @Nullable BlockPos fromPos,
                                         @Nullable EnumFacing fromSide) {
        if (level == null || fromPos == null) {
            return null;
        }

        TileEntity tile = level.getTileEntity(fromPos);
        IPneumaticMachine machine = IPneumaticMachine.getMachine(tile);
        return machine == null ? null : machine.getAirHandler(fromSide);
    }
}

