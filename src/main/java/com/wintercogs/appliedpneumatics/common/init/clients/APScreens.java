package com.wintercogs.appliedpneumatics.common.init.clients;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.init.client.InitScreens;
import appeng.menu.me.common.MEStorageMenu;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.client.gui.AmadronWirelessTerminalGUI;
import com.wintercogs.appliedpneumatics.client.gui.MEAmadronProcessStationGUI;
import com.wintercogs.appliedpneumatics.client.gui.MEPressureInterfaceGUI;
import com.wintercogs.appliedpneumatics.client.gui.METemperatureInterfaceGUI;
import com.wintercogs.appliedpneumatics.common.init.APMenus;
import com.wintercogs.appliedpneumatics.common.menu.MEAmadronProcessStationMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = AppliedPneumatics.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class APScreens
{
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(APMenus.ME_PRESSURE_INTERFACE_MENU.get(), MEPressureInterfaceGUI::new);
        event.<MEAmadronProcessStationMenu, MEAmadronProcessStationGUI>register(APMenus.ME_AMADRON_PROCESS_STATION_MENU.get(), (menu, inv, title) -> new MEAmadronProcessStationGUI(menu, inv, title, menu.getScreenStyle()));
        event.register(APMenus.AMADRON_WIRELESS_TERMINAL_MENU.get(), AmadronWirelessTerminalGUI::new);
        event.register(APMenus.ME_TEMPERATURE_INTERFACE_MENU.get(), METemperatureInterfaceGUI::new);
        InitScreens.<MEStorageMenu, MEStorageScreen<MEStorageMenu>>register(event,
                APMenus.PORTABLE_AIR_CELL_TYPE,
                MEStorageScreen::new,
                "/screens/terminals/portable_air_cell.json"
        );
    }
}
