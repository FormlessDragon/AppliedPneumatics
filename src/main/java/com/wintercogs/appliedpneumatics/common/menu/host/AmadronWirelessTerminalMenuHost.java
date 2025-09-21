package com.wintercogs.appliedpneumatics.common.menu.host;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.HotkeyAction;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEKey;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.SupplierStorage;
import appeng.api.util.IConfigManager;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.items.contents.StackDependentSupplier;
import appeng.me.helpers.PlayerSource;
import appeng.me.storage.NullInventory;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.SupplierInternalInventory;
import com.wintercogs.appliedpneumatics.common.init.APDataComponents;
import com.wintercogs.appliedpneumatics.common.items.AmadronWirelessTerminalItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class AmadronWirelessTerminalMenuHost extends ItemMenuHost<AmadronWirelessTerminalItem>
    implements IPortableTerminal, IActionHost
{
    /** 从子菜单到主菜单的回调 */
    private final BiConsumer<Player, ISubMenu> returnToMainMenu;
    @Nullable
    private IWirelessAccessPoint currentAccessPoint;
    /** 当前链接的无线接入点的距离 */
    protected double currentDistanceFromGrid = Double.MAX_VALUE;
    /** 我们距离丢失信息还有多少格？ */
    protected double currentRemainingRange = Double.MIN_VALUE;
    private final MEStorage storage;
    private ILinkStatus linkStatus = ILinkStatus.ofDisconnected();

    private final SupplierInternalInventory<InternalInventory> inventory;

    public AmadronWirelessTerminalMenuHost(AmadronWirelessTerminalItem item, Player player, ItemMenuHostLocator locator, BiConsumer<Player, ISubMenu> returnToMainMenu)
    {
        super(item, player, locator);
        this.returnToMainMenu = returnToMainMenu;

        this.storage = new SupplierStorage(new StackDependentSupplier<>(
                this::getItemStack, this::getStorageFromStack));

        this.inventory = new SupplierInternalInventory<>(
                new StackDependentSupplier<>(
                        this::getItemStack,
                        stack -> createPatternInv(player, stack)));

        updateConnectedAccessPoint();
        updateLinkStatus();
    }



    private static InternalInventory createPatternInv(Player player, ItemStack stack)
    {
        AppEngInternalInventory patternGrid = new AppEngInternalInventory(new InternalInventoryHost()
        {
            @Override
            public void saveChangedInventory(AppEngInternalInventory inv)
            {
                stack.set(APDataComponents.COMMON_ITEM_CONTENT, inv.toItemContainerContents());
            }

            @Override
            public boolean isClientSide()
            {
                return player.level().isClientSide();
            }
        }, 1);
        patternGrid.fromItemContainerContents(stack.getOrDefault(APDataComponents.COMMON_ITEM_CONTENT, ItemContainerContents.EMPTY));
        return patternGrid;
    }

    public InternalInventory getPatternInv()
    {
        return inventory;
    }

    /** 获取当前链接状态 */
    @Override
    public ILinkStatus getLinkStatus()
    {
        return linkStatus;
    }

    /** 获取当前物品所对应的AE系统的存储 */
    @Nullable
    private MEStorage getStorageFromStack(ItemStack stack)
    {
        var targetGrid = getLinkedGrid(stack);
        if (targetGrid != null) {
            return targetGrid.getStorageService().getInventory();
        }
        return NullInventory.of();
    }

    /** 获取链接的节点 */
    @Nullable
    private IGrid getLinkedGrid(ItemStack stack)
    {
        return getItem().getLinkedGrid(stack, getPlayer().level(), null);
    }

    /** 获取当前存储，storage给出的是一个代理器，可以安全使用 */
    @Override
    public MEStorage getInventory()
    {
        return this.storage;
    }

    /** 从无线终端扣电 */
    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier)
    {
        final double extracted = Math.min(amt, getItem().getAECurrentPower(getItemStack()));

        if (mode == Actionable.SIMULATE) {
            return extracted;
        }

        return getItem().usePower(getPlayer(), extracted, getItemStack()) ? extracted : 0;
    }

    /** 获取保存后的配置 */
    @Override
    public IConfigManager getConfigManager()
    {
        return getItem().getConfigManager(this::getItemStack);
    }

    /** 返回当前的可操作节点 */
    @Nullable
    @Override
    public IGridNode getActionableNode()
    {
        if (this.currentAccessPoint != null) {
            return this.currentAccessPoint.getActionableNode();
        }
        return null;
    }

    /** 更新当前使用的无线接入点 */
    protected void updateConnectedAccessPoint()
    {
        this.currentAccessPoint = null;
        this.currentDistanceFromGrid = Double.MAX_VALUE;
        this.currentRemainingRange = Double.MIN_VALUE;

        var targetGrid = getLinkedGrid(getItemStack());
        if (targetGrid != null) {
            @Nullable
            IWirelessAccessPoint bestWap = null;
            double bestSqDistance = Double.MAX_VALUE;
            double bestSqRemainingRange = Double.MIN_VALUE;

            // 找到最近的无线接入点
            for (var wap : targetGrid.getMachines(WirelessAccessPointBlockEntity.class)) {
                var signal = getAccessPointSignal(wap);


                if (signal.distanceSquared < bestSqDistance) {
                    bestSqDistance = signal.distanceSquared;
                    bestWap = wap;
                }

                if (signal.remainingRangeSquared > bestSqRemainingRange) {
                    bestSqRemainingRange = signal.remainingRangeSquared;
                }
            }

            this.currentAccessPoint = bestWap;
            this.currentDistanceFromGrid = Math.sqrt(bestSqDistance);
            this.currentRemainingRange = Math.sqrt(bestSqRemainingRange);
        }

    }

    /**
     * @return 如果可以使用WAP，则为到WAP的平方距离；如果不能使用，则为{@link Double#MAX_VALUE}。
     */
    protected AccessPointSignal getAccessPointSignal(IWirelessAccessPoint wap)
    {
        double rangeLimit = wap.getRange();
        rangeLimit *= rangeLimit;

        var dc = wap.getLocation();

        if (dc.getLevel() == this.getPlayer().level())
        {
            var offX = dc.getPos().getX() - this.getPlayer().getX();
            var offY = dc.getPos().getY() - this.getPlayer().getY();
            var offZ = dc.getPos().getZ() - this.getPlayer().getZ();

            double r = offX * offX + offY * offY + offZ * offZ;
            if (r < rangeLimit && wap.isActive())
            {
                return new AccessPointSignal(r, rangeLimit - r);
            }
        }

        return new AccessPointSignal(Double.MAX_VALUE, Double.MIN_VALUE);
    }

    public record AccessPointSignal(double distanceSquared, double remainingRangeSquared)
    {
    }

    /** 每tick维护终端状态：更新无线接入点、消耗能力、更新链接状态...... */
    @Override
    public void tick()
    {
        updateConnectedAccessPoint();
        consumeIdlePower(Actionable.MODULATE);
        updateLinkStatus();
    }

    /**
     * 更新链接状态
     */
    protected void updateLinkStatus()
    {
        if (!consumeIdlePower(Actionable.SIMULATE)) {
            this.linkStatus = ILinkStatus.ofDisconnected(GuiText.OutOfPower.text());
        }
        else if
        (currentAccessPoint != null)
        {
            this.linkStatus = ILinkStatus.ofConnected();
        }
        else
        {
            MutableObject<Component> errorHolder = new MutableObject<>();
            if (getItem().getLinkedGrid(getItemStack(), getPlayer().level(), errorHolder::setValue) == null)
            {
                this.linkStatus = ILinkStatus.ofDisconnected(errorHolder.getValue());
            }
            else
            {
                this.linkStatus = ILinkStatus.ofDisconnected(PlayerMessages.OutOfRange.text());
            }
        }
    }

    @Override
    protected double getPowerDrainPerTick()
    {
        if (currentAccessPoint != null && currentDistanceFromGrid < Double.MAX_VALUE) {
            return AEConfig.instance().wireless_getDrainRate(currentDistanceFromGrid);
        } else {
            return 0.0;
        }
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        returnToMainMenu.accept(player, subMenu);
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return getItemStack();
    }

    public String getCloseHotkey()
    {
        return HotkeyAction.WIRELESS_TERMINAL;
    }

    /** 用于外部将物品插入ME网络的逻辑 */
    @Override
    public long insert(Player player, AEKey what, long amount, Actionable mode)
    {
        // 客户端不允许执行
        if (isClientSide())
        {
            return 0;
        }

        if (getLinkStatus().connected())
        {
            return StorageHelper.poweredInsert(this, getInventory(), what, amount, new PlayerSource(player), mode);
        }
        else
        {
            var statusText = getLinkStatus().statusDescription();
            if (statusText != null && !mode.isSimulate())
            {
                player.displayClientMessage(statusText, false);
            }
            return 0;
        }
    }

    @Override
    public boolean isValid()
    {
        return super.isValid() && getLinkStatus().connected();
    }
}
