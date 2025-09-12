package com.wintercogs.appliedpneumatics.common.me.strategies;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import com.wintercogs.appliedpneumatics.util.APMath;
import com.wintercogs.appliedpneumatics.util.AirHandlerHelper;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
        return new AirBlockEntityStorage(handlerMachine);
    }

    private record AirBlockEntityStorage(IAirHandlerMachine airHandler) implements MEStorage
    {

        @Override
        public boolean isPreferredStorageFor(AEKey what, IActionSource source)
        {
            return false;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source)
        {
            if(!(what instanceof AirKey)) return 0;
            // 最大余量与意图插入数的最小值
            long wantInsert = Math.min(AirHandlerHelper.getMaxAirInPressure(airHandler) - airHandler.getAir(), amount);
            int maxInsert = APMath.ClampToInt(wantInsert);
            if(!mode.isSimulate())
                airHandler.addAir(maxInsert);
            return maxInsert;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source)
        {
            if(!(what instanceof AirKey)) return 0;

            long wantExtract = Math.min(airHandler.getAir(), amount);
            int maxExtract = APMath.ClampToInt(wantExtract);
            if(!mode.isSimulate())
                airHandler.addAir(-maxExtract);
            return maxExtract;
        }

        @Override
        public void getAvailableStacks(KeyCounter out)
        {
            out.add(AirKey.INSTANCE, airHandler.getAir());
        }

        @Override
        public Component getDescription()
        {
            return Component.translatable("appliedpneumatics.me.externalstorage.air");
        }
    }
}
