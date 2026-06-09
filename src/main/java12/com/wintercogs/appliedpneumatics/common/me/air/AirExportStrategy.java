package com.wintercogs.appliedpneumatics.common.me.air;

import ae2.api.behaviors.StackExportStrategy;
import ae2.api.behaviors.StackTransferContext;
import ae2.api.config.Actionable;
import ae2.api.stacks.AEKey;
import ae2.api.storage.StorageHelper;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

public final class AirExportStrategy implements StackExportStrategy {
    private final WorldServer level;
    private final BlockPos fromPos;
    private final EnumFacing fromSide;

    public AirExportStrategy(WorldServer level, BlockPos fromPos, EnumFacing fromSide) {
        this.level = level;
        this.fromPos = fromPos;
        this.fromSide = fromSide;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long maxAmount) {
        if (!(what instanceof AirKey)) {
            return 0;
        }

        long accepted = push(what, maxAmount, Actionable.SIMULATE);
        if (accepted <= 0) {
            return 0;
        }

        long extracted = StorageHelper.poweredExtraction(
            context.getEnergySource(),
            context.getInternalStorage().getInventory(),
            what,
            accepted,
            context.getActionSource(),
            Actionable.MODULATE);
        long inserted = push(what, extracted, Actionable.MODULATE);
        if (inserted < extracted) {
            context.getInternalStorage().getInventory().insert(
                what, extracted - inserted, Actionable.MODULATE, context.getActionSource());
        }
        return inserted;
    }

    @Override
    public long push(AEKey what, long maxAmount, Actionable mode) {
        if (!(what instanceof AirKey) || maxAmount <= 0) {
            return 0;
        }

        IAirHandler handler = AirExternalStorageStrategy.resolveAirHandler(this.level, this.fromPos, this.fromSide);
        return handler == null ? 0 : AirHandlerStrategy.INSTANCE.insert(handler, what, maxAmount, mode);
    }
}

