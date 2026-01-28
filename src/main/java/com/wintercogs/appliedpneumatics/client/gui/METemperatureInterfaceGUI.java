package com.wintercogs.appliedpneumatics.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import com.wintercogs.appliedpneumatics.common.menu.METemperatureInterfaceMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class METemperatureInterfaceGUI extends UpgradeableScreen<METemperatureInterfaceMenu>
{
    public METemperatureInterfaceGUI(METemperatureInterfaceMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/me_temperature_interface.json"));

        widgets.addButton("add_button_10", Component.literal("+10"), () -> {
            double mult = hasShiftDown() ? 10 : 1;
            menu.sendExpectedTemperatureActionToServer(10 * mult);
        });
        widgets.addButton("add_button_1", Component.literal("+1"), () -> {
            double mult = hasShiftDown() ? 10 : 1;
            menu.sendExpectedTemperatureActionToServer(1 * mult);
        });
        widgets.addButton("reduce_button_1", Component.literal("-1"), () -> {
            double mult = hasShiftDown() ? 10 : 1;
            menu.sendExpectedTemperatureActionToServer(-1 * mult);
        });
        widgets.addButton("reduce_button_10", Component.literal("-10"), () -> {
            double mult = hasShiftDown() ? 10 : 1;
            menu.sendExpectedTemperatureActionToServer(-10 * mult);
        });
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        String temperatureStr = String.format(Locale.ROOT, "%.1f", menu.latestTemperature - 273) + "℃";
        String heatCapStr = String.format(Locale.ROOT, "%.0f", menu.latestHeatCap);
        String expectedTemperatureStr = String.format(Locale.ROOT, "%.1f", menu.latestExpectedTemperature - 273);

        setTextContent("temperature", Component.translatable("menu.label.appliedpneumatics.me_temperature_interface.temperature", temperatureStr));
        setTextContent("heat_cap", Component.translatable("menu.label.appliedpneumatics.me_temperature_interface.heat_cap", heatCapStr));
        setTextContent("expected_temperature", Component.literal(expectedTemperatureStr));
        setTextContent("expected_temperature_text", Component.translatable("menu.label.appliedpneumatics.me_temperature_interface.expected_temperature_text"));
        setTextContent("expected_temperature_change_mult_text", Component.translatable("menu.label.appliedpneumatics.me_temperature_interface.expected_temperature_change_mult_text"));
    }
}
