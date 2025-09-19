package com.wintercogs.appliedpneumatics.common.items;

import appeng.api.config.Actionable;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGrid;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.localization.Tooltips;
import appeng.items.tools.powered.PoweredContainerItem;
import appeng.items.tools.powered.powersink.PoweredItemCapabilities;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.Platform;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * 亚马龙无线终端
 * 能直接从链接的ME网络支付订单
 */
public class AmadronWirelessTerminalItem extends PoweredContainerItem implements IUpgradeableItem
{
    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    public AmadronWirelessTerminalItem(DoubleSupplier powerCapacity, Properties props)
    {
        super(powerCapacity, props);
    }

    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        IAEItemPowerStorage powerStorage = APItems.AMADRON_WIRELESS_TERMINAL.get();
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (o, unused) -> new PoweredItemCapabilities(o, powerStorage),
                APItems.AMADRON_WIRELESS_TERMINAL);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines,
                                TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, context, lines, advancedTooltips);

        if (getLinkedPosition(stack) == null) {
            lines.add(Tooltips.of(GuiText.Unlinked, Tooltips.RED));
        } else {
            lines.add(Tooltips.of(GuiText.Linked, Tooltips.GREEN));
        }
    }

    /**
     * 获取链接的地点的位置
     */
    @Nullable
    public GlobalPos getLinkedPosition(ItemStack item)
    {
        return item.get(AEComponents.WIRELESS_LINK_TARGET);
    }

    @Nullable
    public IGrid getLinkedGrid(ItemStack item, Level level, @Nullable Consumer<Component> errorConsumer)
    {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        var linkedPos = getLinkedPosition(item);
        if (linkedPos == null) {
            if (errorConsumer != null) {
                errorConsumer.accept(PlayerMessages.DeviceNotLinked.text());
            }
            return null;
        }

        var linkedLevel = serverLevel.getServer().getLevel(linkedPos.dimension());
        if (linkedLevel == null) {
            if (errorConsumer != null) {
                errorConsumer.accept(PlayerMessages.LinkedNetworkNotFound.text());
            }
            return null;
        }

        var be = Platform.getTickingBlockEntity(linkedLevel, linkedPos.pos());
        if (!(be instanceof IWirelessAccessPoint accessPoint)) {
            if (errorConsumer != null) {
                errorConsumer.accept(PlayerMessages.LinkedNetworkNotFound.text());
            }
            return null;
        }

        var grid = accessPoint.getGrid();
        if (grid == null) {
            if (errorConsumer != null) {
                errorConsumer.accept(PlayerMessages.LinkedNetworkNotFound.text());
            }
        }
        return grid;
    }

    /**
     * use an amount of power, in AE units
     *
     * @param amount is in AE units ( 5 per MJ ), if you return false, the item should be dead and return false for
     *               hasPower
     * @return true if wireless terminal uses power
     */
    public boolean usePower(Player player, double amount, ItemStack is) {
        return extractAEPower(is, amount, Actionable.MODULATE) >= amount - 0.5;
    }

    /**
     * gets the power status of the item.
     *
     * @return returns true if there is any power left.
     */
    public boolean hasPower(Player player, double amt, ItemStack is) {
        return getAECurrentPower(is) >= amt;
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack stack)
    {
        return UpgradeInventories.forItem(stack, 2, this::onUpgradesChanged);
    }

    @Override
    public double getChargeRate(ItemStack stack)
    {
        return 800d + 800d * Upgrades.getEnergyCardMultiplier(getUpgrades(stack));
    }

    private void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades)
    {
        setAEMaxPowerMultiplier(stack, 1 + Upgrades.getEnergyCardMultiplier(upgrades));
    }

    @Override
    public @Nullable ItemMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult)
    {
        return null;
    }

    private static class LinkableHandler implements IGridLinkableHandler
    {
        @Override
        public boolean canLink(ItemStack stack)
        {
            return stack.getItem() instanceof AmadronWirelessTerminalItem;
        }

        @Override
        public void link(ItemStack itemStack, GlobalPos pos)
        {
            itemStack.set(AEComponents.WIRELESS_LINK_TARGET, pos);
        }

        @Override
        public void unlink(ItemStack itemStack)
        {
            itemStack.remove(AEComponents.WIRELESS_LINK_TARGET);
        }
    }
}
