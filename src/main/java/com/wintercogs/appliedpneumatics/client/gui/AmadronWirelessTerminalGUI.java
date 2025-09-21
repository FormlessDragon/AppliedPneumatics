package com.wintercogs.appliedpneumatics.client.gui;

import appeng.api.config.ActionItems;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.Scrollbar;
import com.wintercogs.appliedpneumatics.client.gui.widgets.AmadronOfferPanel;
import com.wintercogs.appliedpneumatics.common.menu.AmadronWirelessTerminalMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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

    private final Scrollbar scrollbar;
    private final AETextField seacher;
    private final AE2Button submitButton;
    private final ActionButton savePatternButton;

    private int totalOffers = 0;
    private int totalRows   = 0; // 所有交易换算为多少“行”
    private int lastTopRow  = -1; // 上一次的滚动位置（行索引）
    private final List<AmadronOfferPanel> pagePanels = new ArrayList<>();

    public AmadronWirelessTerminalGUI(AmadronWirelessTerminalMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/amadron_wireless_terminal.json"));

        this.scrollbar = widgets.addScrollBar("scrollbar", Scrollbar.BIG);
        this.seacher = widgets.addTextField("search");
        this.submitButton = widgets.addButton("submit_button", Component.translatable("menu.appliedpneumatics.button.submit"), button -> {});
        this.savePatternButton = new ActionButton(ActionItems.ENCODE, actionItems -> {});
        widgets.add("save_pattern_button", this.savePatternButton);
    }

    @Override
    protected void init() {
        super.init();

        // 统计条目和总行数
        this.totalOffers = menu.getOfferIdsSnapshot().size();
        this.totalRows   = (int) Math.ceil(this.totalOffers / (double) PANELS_PER_ROW);

        scrollbar.setHeight(91);

        updateScrollbarRange();
        // 强制首帧构建
        this.lastTopRow = -1;
        rebuildVisiblePanels();
    }


    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY)
    {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);

        for (var p : pagePanels) p.renderStackTooltip(guiGraphics, mouseX, mouseY, offsetX, offsetY);
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
            rebuildVisiblePanels();
        }
    }

    private void rebuildVisiblePanels()
    {
        // 清旧
        for (var p : pagePanels) removeWidget(p);
        pagePanels.clear();

        if (totalOffers <= 0) return;

        final int startRow   = Math.min(scrollbar.getCurrentScroll(), Math.max(0, totalRows - VISIBLE_ROWS));
        final int startIndex = startRow * PANELS_PER_ROW;
        final int capacity   = VISIBLE_ROWS * PANELS_PER_ROW;

        final int endIndexExclusive = Math.min(startIndex + capacity, totalOffers);
        final int count = endIndexExclusive - startIndex;

        for (int i = 0; i < count; i++) {
            int row = i / PANELS_PER_ROW;
            int col = i % PANELS_PER_ROW;

            int x = leftPos + GRID_OFFSET_X + col * (PANEL_W + GAP_X);
            int y = topPos  + GRID_OFFSET_Y + row * (PANEL_H + GAP_Y);

            // 取本格对应的 id
            ResourceLocation id = menu.getOfferIdsSnapshot().get(startIndex + i);

            // 用 id 创建带实际数据的 panel
            AmadronOfferPanel panel = AmadronOfferPanel.fromOfferId(x, y, id);

            addRenderableWidget(panel);
            pagePanels.add(panel);
        }
    }

}
