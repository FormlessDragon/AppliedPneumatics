package com.wintercogs.appliedpneumatics.common.me;

import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.stacks.AEKeyTypes;
import appeng.parts.automation.StackWorldBehaviors;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import com.wintercogs.appliedpneumatics.common.me.strategies.AirExternalStorageStrategy;

public class AEPlugin
{
    /**
     * 必须在早期注册的项目使用init
     */
    public static void init()
    {
        AEKeyTypes.register(AirKeyType.INSTANCE);
        GenericSlotCapacities.register(AirKeyType.INSTANCE, 64000L); // 作为非气压容器的普通me接口，每槽位最多64000ml空气
    }

    /**
     * 在CommonSetup中注册的放在此处
     */
    public static void register()
    {
        StackWorldBehaviors.registerExternalStorageStrategy(AirKeyType.INSTANCE, AirExternalStorageStrategy::new);
    }
}
