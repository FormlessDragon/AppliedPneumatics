package com.wintercogs.appliedpneumatics.common.init;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.client.gui.AmadronWirelessTerminalGUI;
import com.wintercogs.appliedpneumatics.client.gui.MEAmadronProcessStationGUI;
import com.wintercogs.appliedpneumatics.client.gui.MEPressureInterfaceGUI;
import com.wintercogs.appliedpneumatics.common.menu.AmadronWirelessTerminalMenu;
import com.wintercogs.appliedpneumatics.common.menu.MEAmadronProcessStationMenu;
import com.wintercogs.appliedpneumatics.common.menu.MEPressureInterfaceMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@EventBusSubscriber(modid = AppliedPneumatics.MODID, bus = EventBusSubscriber.Bus.MOD)
public class APMenus
{
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, AppliedPneumatics.MODID);


    public static final Supplier<MenuType<MEPressureInterfaceMenu>> ME_PRESSURE_INTERFACE_MENU = MENU_TYPES.register("me_pressure_interface_menu",
            () -> IMenuTypeExtension.create(MEPressureInterfaceMenu::new));

    public static final Supplier<MenuType<MEAmadronProcessStationMenu>> ME_AMADRON_PROCESS_STATION_MENU = MENU_TYPES.register("me_amadron_process_menu",
            () -> IMenuTypeExtension.create(MEAmadronProcessStationMenu::new));

    public static final Supplier<MenuType<AmadronWirelessTerminalMenu>> AMADRON_WIRELESS_TERMINAL_MENU = MENU_TYPES.register("amadron_wireless_terminal_menu",
            () -> IMenuTypeExtension.create(AmadronWirelessTerminalMenu::new));

    public static void registerMenus(IEventBus eventBus)
    {
        MENU_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(ME_PRESSURE_INTERFACE_MENU.get(), MEPressureInterfaceGUI::new);
        event.register(ME_AMADRON_PROCESS_STATION_MENU.get(), MEAmadronProcessStationGUI::new);
        event.register(AMADRON_WIRELESS_TERMINAL_MENU.get(), AmadronWirelessTerminalGUI::new);
    }
}
