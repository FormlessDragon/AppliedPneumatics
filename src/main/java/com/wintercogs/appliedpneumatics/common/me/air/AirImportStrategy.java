package com.wintercogs.appliedpneumatics.common.me.air;

import ae2.api.behaviors.StackImportStrategy;
import ae2.api.behaviors.StackTransferContext;
import ae2.api.config.Actionable;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.Nullable;

public final class AirImportStrategy implements StackImportStrategy {
    private final WorldServer level;
    private final BlockPos fromPos;
    private final EnumFacing fromSide;

    public AirImportStrategy(WorldServer level, BlockPos fromPos, EnumFacing fromSide) {
        this.level = level;
        this.fromPos = fromPos;
        this.fromSide = fromSide;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(AirKeyType.INSTANCE)
            || context.isInFilter(AirKey.INSTANCE) == context.isInverted()) {
            return false;
        }

        IAirHandler handler = AirExternalStorageStrategy.resolveAirHandler(this.level, this.fromPos, this.fromSide);
        return transferFromHandler(handler, context);
    }

    static boolean transferFromHandler(@Nullable IAirHandler handler, StackTransferContext context) {
        if (handler == null || handler.getAir() <= 0) {
            return false;
        }

        long maxTransfer = context.getOperationsRemaining() * (long) AirKeyType.INSTANCE.getAmountPerOperation();
        long insertable = context.getInternalStorage().getInventory().insert(
            AirKey.INSTANCE, Math.min(maxTransfer, handler.getAir()), Actionable.SIMULATE, context.getActionSource());
        long extracted = Math.min(Math.max(0, handler.getAir()), insertable);
        if (extracted <= 0) {
            return false;
        }

        handler.addAir(-com.google.common.primitives.Ints.saturatedCast(extracted));
        long inserted = context.getInternalStorage().getInventory().insert(
            AirKey.INSTANCE, extracted, Actionable.MODULATE, context.getActionSource());
        if (inserted < extracted) {
            handler.addAir(com.google.common.primitives.Ints.saturatedCast(extracted - inserted));
        }
        if (inserted > 0) {
            context.reduceOperationsRemaining(Math.max(1, inserted / AirKeyType.INSTANCE.getAmountPerOperation()));
        }
        return false;
    }
}

