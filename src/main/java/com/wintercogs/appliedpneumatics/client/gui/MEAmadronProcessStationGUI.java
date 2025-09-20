package com.wintercogs.appliedpneumatics.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import com.wintercogs.appliedpneumatics.common.menu.MEAmadronProcessStationMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MEAmadronProcessStationGUI extends UpgradeableScreen<MEAmadronProcessStationMenu>
{
    // 将使用样式 JSON，背景由样式管理
    public MEAmadronProcessStationGUI(MEAmadronProcessStationMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, StyleManager.loadStyleDoc("/screens/me_amadron_process_station.json"));
    }

}
