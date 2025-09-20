package com.wintercogs.appliedpneumatics.common.menu;

import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import com.wintercogs.appliedpneumatics.common.init.APMenus;
import com.wintercogs.appliedpneumatics.common.items.AmadronWirelessTerminalItem;
import com.wintercogs.appliedpneumatics.common.menu.host.AmadronWirelessTerminalMenuHost;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AmadronWirelessTerminalMenu extends UpgradeableMenu<AmadronWirelessTerminalMenuHost>
{
    // 构造：客户端
    public AmadronWirelessTerminalMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, createHostFromBuf(playerInv, buf));
    }

    private static AmadronWirelessTerminalMenuHost createHostFromBuf(Inventory inv, FriendlyByteBuf buf) {
        // 1) 读回 locator（服务端写进去的那个）
        var raw = MenuLocators.readFromPacket(buf);
        if (!(raw instanceof ItemMenuHostLocator locator)) {
            throw new IllegalArgumentException("Expected ItemMenuHostLocator, got " + raw.getClass().getSimpleName());
        }

        // 2) 用 locator 定位“当前那一份” ItemStack（注意不是缓存引用）
        ItemStack stack = locator.locateItem(inv.player);

        // 3) 做一下类型校验再构造 Host（Host 构造器也会二次校验）
        if (!(stack.getItem() instanceof AmadronWirelessTerminalItem item)) {
            throw new IllegalStateException("Slot no longer holds AmadronWirelessTerminalItem");
        }

        // 4) 如有其他打开时参数，按顺序继续读：
        // int defaultTab = buf.readVarInt();

        return new AmadronWirelessTerminalMenuHost(item, inv.player, locator);
    }

    // 构造：服务端
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
        return !getHost().isValid();
    }
}
