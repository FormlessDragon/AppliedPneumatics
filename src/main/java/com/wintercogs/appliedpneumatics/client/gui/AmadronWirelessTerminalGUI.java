package com.wintercogs.appliedpneumatics.client.gui;

import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.IconButton;
import appeng.client.gui.widgets.Scrollbar;
import com.wintercogs.appliedpneumatics.client.gui.widgets.AmadronOfferPanel;
import com.wintercogs.appliedpneumatics.common.menu.AmadronWirelessTerminalMenu;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.amadron.MutableBasket;
import me.desht.pneumaticcraft.common.amadron.ShoppingBasket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AmadronWirelessTerminalGUI extends UpgradeableScreen<AmadronWirelessTerminalMenu>
{
    private static final int MAX_PANEL_PER_PAGE = 8; // 一页8个交易面板
    private static final int PANELS_PER_ROW = 2; // 一行两个交易面板
    private static final int VISIBLE_ROWS = MAX_PANEL_PER_PAGE / PANELS_PER_ROW; // 一共几行

    // 面板尺寸与间距
    private static final int PANEL_W = 79;
    private static final int PANEL_H = 22;
    private static final int GAP_X = 1;
    private static final int GAP_Y = 1;

    // 面板区域左上角（相对屏幕左上角）
    private static final int GRID_OFFSET_X = 9;
    private static final int GRID_OFFSET_Y = 19;

    // 将不可见面板“停”到屏幕外，避免重建
    private static final int HIDE_X = -10000;
    private static final int HIDE_Y = -10000;

    private final Scrollbar scrollbar;
    private final AETextField seacher;
    private final AE2Button submitButton;
    private final IconButton savePatternButton;

    private int totalOffers = 0;
    private int totalRows   = 0; // 所有交易换算为多少“行”
    private int lastTopRow  = -1; // 上一次的滚动位置（行索引）
    private final List<AmadronOfferPanel> pagePanels = new ArrayList<>();

    // 用于制作样板
    private @Nullable AmadronOfferPanel lastClickedPanel = null;

    public AmadronWirelessTerminalGUI(AmadronWirelessTerminalMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/amadron_wireless_terminal.json"));

        this.scrollbar = widgets.addScrollBar("scrollbar", Scrollbar.BIG);
        this.seacher = widgets.addTextField("search");
        this.submitButton = widgets.addButton("submit_button", Component.translatable("menu.appliedpneumatics.button.submit"), this::onSubmit);
        this.savePatternButton = new IconButton(button -> {
            if(lastClickedPanel != null)
                menu.sendSavePatternAction(lastClickedPanel.getOfferId());
        })
        {
            @Override
            protected Icon getIcon()
            {
                return Icon.ARROW_LEFT;
            }
        };
        widgets.add("save_pattern_button", this.savePatternButton);
    }

    @Override
    protected void init()
    {
        super.init();

        this.totalOffers = menu.getOfferIdsSnapshot().size();
        this.totalRows   = (int) Math.ceil(this.totalOffers / (double) PANELS_PER_ROW);

        scrollbar.setHeight(91);
        updateScrollbarRange();

        // 1) 先确保池子齐备（缺的补上；新增的会被立即 addRenderableWidget）
        ensurePanelPoolUpToDate();

        // 2) 再把已有面板全部“重新挂回屏”，以应对窗口缩放导致的 renderables 清空
        reattachPanelPool();

        // 强制首帧布局
        this.lastTopRow = -1;
        relayoutVisiblePanels();
    }

    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn)
    {
        for (var panel : pagePanels)
        {
            if (panel.isActive() && panel.isMouseOver(xCoord, yCoord))
            {
                panel.onClicked(xCoord, yCoord, btn, hasShiftDown());
                updateLastClickedPanel(panel);
                return true;
            }
        }
        return super.mouseClicked(xCoord, yCoord, btn);
    }

    private void updateLastClickedPanel(AmadronOfferPanel panelClicked)
    {
        if(panelClicked.getOfferType().isPlayer() || !panelClicked.getOfferType().isStatic()) return;
        if(lastClickedPanel != null)
        {
            removeWidget(lastClickedPanel);
        }
        lastClickedPanel = AmadronOfferPanel.fromOfferId(getGuiLeft() + 12, getGuiTop() + 145, panelClicked.getOfferId(), (button) -> {});
        addRenderableWidget(lastClickedPanel);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY)
    {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);

        // 只对可见/可交互的面板渲染 tooltip，避免隐藏面板干扰
        for (var p : pagePanels)
            if (p.isActive())
                p.renderStackTooltip(guiGraphics, mouseX, mouseY, offsetX, offsetY);
    }

    private void updateScrollbarRange()
    {
        // maxScroll = 能向下滚的“行数盈余”
        int maxScroll = Math.max(0, totalRows - VISIBLE_ROWS);
        // pageSize 设为“滚一小段”手感
        int pageSize  = Math.max(1, VISIBLE_ROWS / 6);
        // 如果之前 currentScroll 超界，Scrollbar.setRange 内会 clamp
        scrollbar.setRange(0, maxScroll, pageSize);
        scrollbar.setVisible(maxScroll > 0);
    }

    @Override
    public void containerTick()
    {
        super.containerTick();

        // 这里不把滚动条当页码，而是当“当前首行”
        int topRow = scrollbar.getCurrentScroll();

        if (topRow != lastTopRow) {
            lastTopRow = topRow;
            relayoutVisiblePanels();
        }
    }

    /** 新建缺少的面板；新建的面板当场加入渲染列表 */
    private void ensurePanelPoolUpToDate() {
        var ids = menu.getOfferIdsSnapshot();

        for (int i = pagePanels.size(); i < ids.size(); i++)
        {
            ResourceLocation id = ids.get(i);
            AmadronOfferPanel panel = AmadronOfferPanel.fromOfferId(HIDE_X, HIDE_Y, id, (button) -> {});
            panel.active = false;
            panel.visible = false;
            // 关键：新建的直接挂到屏上
            addRenderableWidget(panel);
            pagePanels.add(panel);
        }

        // 多出来的旧面板保持隐藏（不移除）
        for (int i = ids.size(); i < pagePanels.size(); i++)
        {
            var p = pagePanels.get(i);
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }
    }

    /** 在 init()/窗口缩放后，把池里的所有面板重新挂回 Screen */
    private void reattachPanelPool() {
        // Screen.renderables 是 protected，可直接用
        for (var p : pagePanels) {
            if (!this.renderables.contains(p)) {
                addRenderableWidget(p);
            }
        }
    }

    /**
     * 只做“重排”：根据滚动位置把应显示的面板放到正确坐标，其他全部停到屏幕外并禁用交互。
     * 不做 remove/add 操作，保留面板内部状态。
     */
    private void relayoutVisiblePanels()
    {
        // 如 offer 数变化，先保证池子齐备
        this.totalOffers = menu.getOfferIdsSnapshot().size();
        this.totalRows = (int) Math.ceil(this.totalOffers / (double) PANELS_PER_ROW);
        updateScrollbarRange();
        ensurePanelPoolUpToDate();

        // 全部先隐藏到屏幕外
        for (var p : pagePanels) {
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }

        if (totalOffers <= 0) return;

        final int startRow   = Math.min(scrollbar.getCurrentScroll(), Math.max(0, totalRows - VISIBLE_ROWS));
        final int startIndex = startRow * PANELS_PER_ROW;
        final int capacity   = VISIBLE_ROWS * PANELS_PER_ROW;
        final int endIndexExclusive = Math.min(startIndex + capacity, totalOffers);

        for (int index = startIndex; index < endIndexExclusive; index++) {
            int local = index - startIndex;
            int row = local / PANELS_PER_ROW;
            int col = local % PANELS_PER_ROW;

            int x = leftPos + GRID_OFFSET_X + col * (PANEL_W + GAP_X);
            int y = topPos  + GRID_OFFSET_Y + row * (PANEL_H + GAP_Y);

            AmadronOfferPanel panel = pagePanels.get(index);
            panel.setX(x);
            panel.setY(y);
            panel.active = true;
            panel.visible = true;
        }
    }

    /** 提交按钮点击所用的回调 */
    private void onSubmit()
    {
        // 首先，根据当前所有panel的数据构建一个购物车 然后，通过动作发送到服务端，剩下的验证、提交给服务端处理
        MutableBasket basket = ShoppingBasket.createMutable();
        for(AmadronOfferPanel panel : pagePanels)
        {
            if(panel.getWantedStock() > 0)
                basket.addUnitsToOffer(panel.getOfferId(), panel.getWantedStock());
        }
        menu.sendSubmitOrderAction(basket.toImmutable());
    }

}
