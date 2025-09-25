package com.wintercogs.appliedpneumatics.common.init;


import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEAmadronProcessStationBlockEntity;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEPressureInterfaceBlockEntity;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.METemperatureInterfaceBlockEntity;
import com.wintercogs.appliedpneumatics.common.items.AmadronWirelessTerminalItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = AppliedPneumatics.MODID, bus = EventBusSubscriber.Bus.MOD)
public class APCapabilities
{
    @SubscribeEvent
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        MEPressureInterfaceBlockEntity.onRegisterCaps(event);
        AmadronWirelessTerminalItem.onRegisterCaps(event);
        MEAmadronProcessStationBlockEntity.onRegisterCaps(event);
        METemperatureInterfaceBlockEntity.onRegisterCaps(event);
    }
}
