package com.wintercogs.appliedpneumatics.common.menu;

import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.RestrictedInputSlot;
import com.wintercogs.appliedpneumatics.common.amadron.ImmutableBasketArg;
import com.wintercogs.appliedpneumatics.common.init.APMenus;
import com.wintercogs.appliedpneumatics.common.menu.host.AmadronWirelessTerminalMenuHost;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.amadron.ImmutableBasket;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AmadronWirelessTerminalMenu extends UpgradeableMenu<AmadronWirelessTerminalMenuHost>
{
    private static String submitOrderAction = "submit_amadron_order_action";

    // 打开UI时所有可用的交易快照对应的Id（直到UI关闭前都不更新）
    // 无需同步，双端均有气动自行处理
    private final List<ResourceLocation> offerIdsSnapshot = AmadronOfferManager.getInstance()
            .getActiveOffers().stream()
            .map(AmadronOffer::getOfferId)
            .toList();

    // 构造：双端通用，由AE自行传递信息
    public AmadronWirelessTerminalMenu(int id, Inventory playerInv, @NotNull AmadronWirelessTerminalMenuHost host)
    {
        super(APMenus.AMADRON_WIRELESS_TERMINAL_MENU.get(), id, playerInv, host);

        registerClientAction(submitOrderAction, ImmutableBasketArg.class, immutableBasketArg -> onSubmitOrder(immutableBasketArg.basket()));
    }

    private void onSubmitOrder(ImmutableBasket basket)
    {

    }

    public void sendSubmitOrderAction(ImmutableBasket basket)
    {
        sendClientAction(submitOrderAction, new ImmutableBasketArg(basket));
    }

    public List<ResourceLocation> getOfferIdsSnapshot()
    {
        return offerIdsSnapshot;
    }

    // 放除了升级槽之外的其他真实库存
    // 注：玩家槽位已经由UpgradeableMenu处理，不必再写
    @Override
    protected void setupInventorySlots()
    {
        RestrictedInputSlot slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.BLANK_PATTERN ,getHost().getPatternInv(), 0);
        addSlot(slot, SlotSemantics.BLANK_PATTERN);
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return getHost().isValid();
    }
}
