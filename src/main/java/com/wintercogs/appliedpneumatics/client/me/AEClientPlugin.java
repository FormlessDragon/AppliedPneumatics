package com.wintercogs.appliedpneumatics.client.me;

import appeng.api.client.AEKeyRendering;
import com.wintercogs.appliedpneumatics.client.me.render.AirKeyRenderHandler;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;

public class AEClientPlugin
{
    public static void register()
    {
        AEKeyRendering.register(AirKeyType.INSTANCE, AirKey.class, new AirKeyRenderHandler());
        AirStroageModels.register();
    }
}
