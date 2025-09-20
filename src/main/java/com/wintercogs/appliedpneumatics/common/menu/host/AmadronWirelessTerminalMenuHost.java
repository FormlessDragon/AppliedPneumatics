package com.wintercogs.appliedpneumatics.common.menu.host;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.MEStorage;
import appeng.api.util.IConfigManager;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import com.wintercogs.appliedpneumatics.common.items.AmadronWirelessTerminalItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AmadronWirelessTerminalMenuHost extends ItemMenuHost<AmadronWirelessTerminalItem>
    implements IPortableTerminal, IActionHost
{

    public AmadronWirelessTerminalMenuHost(AmadronWirelessTerminalItem item, Player player, ItemMenuHostLocator locator)
    {
        super(item, player, locator);
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier)
    {
        return 0;
    }

    @Override
    public @Nullable IGridNode getActionableNode()
    {
        return null;
    }

    @Override
    public MEStorage getInventory()
    {
        return null;
    }

    @Override
    public ILinkStatus getLinkStatus()
    {
        return null;
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu)
    {

    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return null;
    }

    @Override
    public IConfigManager getConfigManager()
    {
        return null;
    }
}
