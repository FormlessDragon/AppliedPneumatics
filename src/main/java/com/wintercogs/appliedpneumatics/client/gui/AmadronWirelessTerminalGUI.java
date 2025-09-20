package com.wintercogs.appliedpneumatics.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import com.wintercogs.appliedpneumatics.common.menu.AmadronWirelessTerminalMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AmadronWirelessTerminalGUI extends UpgradeableScreen<AmadronWirelessTerminalMenu>
{
    public AmadronWirelessTerminalGUI(AmadronWirelessTerminalMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/amadron_wireless_terminal.json"));
    }
}
