package com.wintercogs.appliedpneumatics.common.menu;

import appeng.menu.implementations.UpgradeableMenu;
import com.wintercogs.appliedpneumatics.common.init.APMenus;
import com.wintercogs.appliedpneumatics.common.menu.host.AmadronWirelessTerminalMenuHost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class AmadronWirelessTerminalMenu extends UpgradeableMenu<AmadronWirelessTerminalMenuHost>
{
    // 构造：双端通用，由AE自行传递信息
    public AmadronWirelessTerminalMenu(int id, Inventory playerInv, @NotNull AmadronWirelessTerminalMenuHost host)
    {
        super(APMenus.AMADRON_WIRELESS_TERMINAL_MENU.get(), id, playerInv, host);
    }

    // 放除了升级槽之外的其他真实库存
    // 注：玩家槽位已经由UpgradeableMenu处理，不必再写
    @Override
    protected void setupInventorySlots()
    {
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return getHost().isValid();
    }
}
