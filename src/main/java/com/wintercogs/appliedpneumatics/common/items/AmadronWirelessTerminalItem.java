package com.wintercogs.appliedpneumatics.common.items;

import appeng.api.config.*;
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
import appeng.api.util.IConfigManager;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.localization.Tooltips;
import appeng.items.tools.powered.PoweredContainerItem;
import appeng.items.tools.powered.powersink.PoweredItemCapabilities;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.util.Platform;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEAmadronProcessStationBlockEntity;
import com.wintercogs.appliedpneumatics.common.init.APDataComponents;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import com.wintercogs.appliedpneumatics.common.init.APMenus;
import com.wintercogs.appliedpneumatics.common.menu.host.AmadronWirelessTerminalMenuHost;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * 亚马龙无线终端
 * 能直接从链接的ME网络支付订单
 */
public class AmadronWirelessTerminalItem extends PoweredContainerItem implements
        IUpgradeableItem, IPositionProvider
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
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand)
    {
        ItemStack stack = player.getItemInHand(usedHand);

        if (!level.isClientSide()) {
            var locator = MenuLocators.forHand(player, usedHand);
            MenuOpener.open(APMenus.AMADRON_WIRELESS_TERMINAL_MENU.get(), player, locator);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context)
    {
        super.useOn(context);

        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MEAmadronProcessStationBlockEntity && player != null && player.isShiftKeyDown())
        {
            ItemStack tabletStack = player.getItemInHand(context.getHand());
            GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
            if(!level.isClientSide())
            {
                toggleLinkToAmadronProcess(tabletStack, globalPos);
            }
            else
            {
                player.playSound(ModSounds.CHIRP.get(), 1.0F, 1.5F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
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

        GlobalPos amadronStationPos = getLinkedAmadronPos(stack);
        if(amadronStationPos == null)
        {
            lines.add(Component.translatable("tooltip.appliedpneumatics.item.amadron.unlink")
                    .withStyle(ChatFormatting.RED));
        }
        else
        {
            ResourceKey<Level> dim = amadronStationPos.dimension();
            Component dimName = Component.translatable("dimension." + dim.location().getNamespace() + "." + dim.location().getPath());

            BlockPos pos = amadronStationPos.pos();
            lines.add(Component.translatable("tooltip.appliedpneumatics.item.amadron.linked",
                            dimName, pos.getX(), pos.getY(), pos.getZ())
                    .withStyle(ChatFormatting.GREEN));
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
    public boolean usePower(Player player, double amount, ItemStack is)
    {
        return extractAEPower(is, amount, Actionable.MODULATE) >= amount - 0.5;
    }

    /**
     * gets the power status of the item.
     *
     * @return returns true if there is any power left.
     */
    public boolean hasPower(Player player, double amt, ItemStack is)
    {
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
        final ItemStack stack = locator.locateItem(player);
        if (!(stack.getItem() instanceof AmadronWirelessTerminalItem)) return null;

        // 子菜单返回回调（客户端用 no-op 就行）
        // 服务端暂时也没有额外回调
        BiConsumer<Player, ISubMenu> onReturn = (pl, sub) -> {};

        // 服务端：做校验 + 开启扣电
        if (player instanceof ServerPlayer sp && player.level() instanceof ServerLevel)
        {
            MutableObject<Component> err = new MutableObject<>();
            IGrid grid = getLinkedGrid(stack, player.level(), err::setValue);
            if (grid == null)
            {
                if (err.getValue()!=null)
                    sp.displayClientMessage(err.getValue(), false);
                return null;
            }
            if (!isPlayerInRangeOfAnyWap(sp, grid))
            {
                sp.displayClientMessage(PlayerMessages.OutOfRange.text(), false);
                return null;
            }

            final double OPEN_COST = 8.0;  // 打开耗电
            if (getAECurrentPower(stack) + 1e-6 < OPEN_COST)
            {
                sp.displayClientMessage(GuiText.OutOfPower.text(), false);
                return null;
            }
            usePower(sp, OPEN_COST, stack); // 打开耗电
        }

        // 两端都要返回 host
        return new AmadronWirelessTerminalMenuHost(this, player, locator, onReturn);
    }

    private static boolean isPlayerInRangeOfAnyWap(ServerPlayer sp, IGrid grid)
    {
        for (var wap : grid.getMachines(WirelessAccessPointBlockEntity.class))
        {
            if (!wap.isActive()) continue;
            var loc = wap.getLocation();
            if (loc.getLevel() != sp.level()) continue; // 必须同维度

            double dx = loc.getPos().getX() - sp.getX();
            double dy = loc.getPos().getY() - sp.getY();
            double dz = loc.getPos().getZ() - sp.getZ();
            double r2 = dx*dx + dy*dy + dz*dz;
            double range = wap.getRange();
            if (r2 < range * range) return true;
        }
        return false;
    }

    // 快速绑定、取消绑定方块
    private static void toggleLinkToAmadronProcess(ItemStack stack, GlobalPos pos)
    {
        if(stack.has(APDataComponents.AMADRON_PROCESS_POS) && Objects.equals(stack.get(APDataComponents.AMADRON_PROCESS_POS), pos))
        {
            stack.remove(APDataComponents.AMADRON_PROCESS_POS);
        }
        else
        {
            stack.set(APDataComponents.AMADRON_PROCESS_POS, pos);
        }
    }

    // 用于客户端渲染覆盖层
    public static @Nullable GlobalPos getLinkedAmadronPos(ItemStack stack)
    {
        return stack.get(APDataComponents.AMADRON_PROCESS_POS);
    }

    // 用于服务端获取数据
    public static @Nullable MEAmadronProcessStationBlockEntity getLinkWithAmadronProcess(ItemStack stack, Level level)
    {
        // 客户端不处理，直接返回 null
        if (level.isClientSide) {
            return null;
        }

        // 没有绑定则直接返回 null
        GlobalPos pos = stack.get(APDataComponents.AMADRON_PROCESS_POS);
        if (pos == null) {
            return null;
        }

        // 根据 GlobalPos 找对应的服务器维度
        ServerLevel serverLevel = level.getServer() != null ? level.getServer().getLevel(pos.dimension()) : null;
        if (serverLevel == null) {
            return null;
        }

        BlockEntity be = serverLevel.getBlockEntity(pos.pos());
        if (be instanceof MEAmadronProcessStationBlockEntity station) {
            return station;
        }
        return null;
    }

    @Override
    public @NotNull List<BlockPos> getStoredPositions(UUID player, @NotNull ItemStack itemStack)
    {
        GlobalPos amadronPos = getLinkedAmadronPos(itemStack);
        if(amadronPos != null)
        {
            return List.of(amadronPos.pos());
        }
        return List.of();
    }

    @Override
    public int getRenderColor(int index)
    {
        return 0x9003FF80; // 半透明绿色 - 与亚马龙终端风格一致
    }

    /**
     * 返回此无线终端的配置设置 这里无可用配置
     */
    public IConfigManager getConfigManager(Supplier<ItemStack> target) {
        return IConfigManager.builder(target)
                .registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE)
                .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL)
                .registerSetting(Settings.TERMINAL_STYLE, TerminalStyle.MEDIUM)
                .build();
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
