package com.wintercogs.appliedpneumatics.util;

import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;

public class AirHandlerHelper
{
    public static long getMaxAirInPressure(IAirHandlerMachine airMachine)
    {
        return (long)(airMachine.getBaseVolume() * (double)airMachine.getDangerPressure()); // 去尾，确保不会出现输入过多的问题
    }
}
