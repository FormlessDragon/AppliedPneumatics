package com.wintercogs.appliedpneumatics.common.me;

import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.stacks.AEKeyTypes;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;

public class AEPlugin
{
    /**
     * 注册有关AE部分的组件
     */
    public static void register()
    {
        AEKeyTypes.register(AirKeyType.INSTANCE);
        GenericSlotCapacities.register(AirKeyType.INSTANCE, 64000L); // 作为非气压容器的普通me接口，每槽位最多64000ml空气
    }
}
