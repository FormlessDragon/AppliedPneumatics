package com.wintercogs.appliedpneumatics.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import com.wintercogs.appliedpneumatics.common.menu.MEPressureInterfaceMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;


public class MEPressureInterfaceGUI extends UpgradeableScreen<MEPressureInterfaceMenu>
{
    // 将使用样式 JSON，背景由样式管理
    public MEPressureInterfaceGUI(MEPressureInterfaceMenu menu, Inventory inv, Component title)
    {
        super(menu, inv, title, StyleManager.loadStyleDoc("/screens/me_pressure_interface.json"));

        widgets.addButton("add_button_1", Component.literal("+1"), () -> {
            float mult = hasShiftDown() ? 5 : 1;
            menu.sendExpectedPressureActionToServer(1 * mult);
        });
        widgets.addButton("add_button_01", Component.literal("+0.1"), () -> {
            float mult = hasShiftDown() ? 5 : 1;
            menu.sendExpectedPressureActionToServer(0.1f * mult);
        });
        widgets.addButton("reduce_button_01", Component.literal("-0.1"), () -> {
            float mult = hasShiftDown() ? 5 : 1;
            menu.sendExpectedPressureActionToServer(-0.1f * mult);
        });
        widgets.addButton("reduce_button_1", Component.literal("-1"), () -> {
            float mult = hasShiftDown() ? 5 : 1;
            menu.sendExpectedPressureActionToServer(-1 * mult);
        });
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        String airStr = String.format(Locale.ROOT, "%.1f", (float) menu.latestAir / 1000f);
        String maxStr = String.format(Locale.ROOT, "%.0f", (float) menu.latestVolume / 1000f);
        String currentPressureStr = String.format(Locale.ROOT, "%.0f", (float) menu.latestAir / (float) menu.latestVolume);
        String pressureStr = String.format(Locale.ROOT, "%.1f", menu.latestExpectedPressure);

        setTextContent("air_amount", Component.translatable("menu.label.appliedpneumatics.me_pressure_interface.air_amount", airStr, maxStr, currentPressureStr));
        setTextContent("max_pressure", Component.translatable("menu.label.appliedpneumatics.me_pressure_interface.max_pressure", menu.latestDangerPressure));
        setTextContent("current_pressure", Component.literal(pressureStr));
        setTextContent("expected_pressure_text", Component.translatable("menu.label.appliedpneumatics.me_pressure_interface.expected_pressure_text"));
        setTextContent("expected_pressure_change_mult_text", Component.translatable("menu.label.appliedpneumatics.me_pressure_interface.expected_pressure_change_mult_text"));
    }
}
