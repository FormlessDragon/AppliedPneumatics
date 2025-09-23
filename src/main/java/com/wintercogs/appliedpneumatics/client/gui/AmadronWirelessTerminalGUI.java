package com.wintercogs.appliedpneumatics.client.gui;

import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.IconButton;
import appeng.client.gui.widgets.Scrollbar;
import com.wintercogs.appliedpneumatics.client.gui.widgets.AE2TinyButton;
import com.wintercogs.appliedpneumatics.client.gui.widgets.AmadronOfferPanel;
import com.wintercogs.appliedpneumatics.common.menu.AmadronWirelessTerminalMenu;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.amadron.MutableBasket;
import me.desht.pneumaticcraft.common.amadron.ShoppingBasket;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AmadronWirelessTerminalGUI extends UpgradeableScreen<AmadronWirelessTerminalMenu>
{
    // 一页显示配置
    private static final int MAX_PANEL_PER_PAGE = 8; // 一页8个交易面板
    private static final int PANELS_PER_ROW = 2;     // 一行两个交易面板
    private static final int VISIBLE_ROWS = MAX_PANEL_PER_PAGE / PANELS_PER_ROW;

    // 面板尺寸与间距
    private static final int PANEL_W = 79;
    private static final int PANEL_H = 22;
    private static final int GAP_X = 1;
    private static final int GAP_Y = 1;

    // 面板区域左上角（相对屏幕左上角）
    private static final int GRID_OFFSET_X = 9;
    private static final int GRID_OFFSET_Y = 19;

    // 隐藏面板停靠点（屏外）
    private static final int HIDE_X = -10000;
    private static final int HIDE_Y = -10000;

    // 组件
    private final Scrollbar scrollbar;
    private final AETextField searchField;
    private final AE2TinyButton submitButton;
    private final IconButton savePatternButton;

    // 状态
    private int totalOffers = 0;  // 过滤后总数
    private int totalRows = 0;    // 过滤后总行数
    private int lastTopRow = -1;  // 上一帧的首行索引
    private final List<AmadronOfferPanel> pagePanels = new ArrayList<>(); // 池：与快照一一对应

    // 搜索
    private String searchQuery = "";
    /** 过滤后：保存“在原始快照中的索引”列表（例如 [0,3,5,...]） */
    private final List<Integer> filteredIndex = new ArrayList<>();

    // 样板制作辅助
    private @Nullable AmadronOfferPanel lastClickedPanel = null;

    public AmadronWirelessTerminalGUI(AmadronWirelessTerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/amadron_wireless_terminal.json"));

        this.scrollbar = widgets.addScrollBar("scrollbar", Scrollbar.BIG);
        this.searchField = widgets.addTextField("search");
        this.submitButton = new AE2TinyButton(Component.translatable("menu.appliedpneumatics.button.submit"), button ->  this.onSubmit());
        widgets.add("submit_button", this.submitButton);

        this.savePatternButton = new IconButton(btn -> {
            if (lastClickedPanel != null) {
                menu.sendSavePatternAction(lastClickedPanel.getOfferId());
            }
        }) {
            @Override
            protected Icon getIcon() {
                return Icon.ARROW_LEFT;
            }
        };
        widgets.add("save_pattern_button", this.savePatternButton);
    }

    @Override
    protected void init() {
        super.init();

        // 搜索监听：内容变化即重算过滤并重排
        this.searchField.setResponder(s -> {
            this.searchQuery = (s == null ? "" : s.trim().toLowerCase(Locale.ROOT));
            rebuildFilter();
            this.lastTopRow = -1;
            relayoutVisiblePanels();
        });

        // 初次构建
        rebuildFilter(); // 计算 filteredIndex + 更新滚动范围
        scrollbar.setHeight(91);

        // 池：覆盖完整快照
        ensurePanelPoolUpToDate();
        reattachPanelPool();

        // 首帧强制重排
        this.lastTopRow = -1;
        relayoutVisiblePanels();
    }

    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        for (var panel : pagePanels) {
            if (panel.isActive() && panel.isMouseOver(xCoord, yCoord)) {
                panel.onClicked(xCoord, yCoord, btn, hasShiftDown());
                updateLastClickedPanel(panel);
                return true;
            }
        }
        return super.mouseClicked(xCoord, yCoord, btn);
    }

    private void updateLastClickedPanel(AmadronOfferPanel clicked) {
        // 只允许对“静态&非玩家”的报价生成样板按钮
        if (clicked.getOfferType().isPlayer() || !clicked.getOfferType().isStatic()) return;

        if (lastClickedPanel != null) {
            removeWidget(lastClickedPanel);
        }
        lastClickedPanel = AmadronOfferPanel.fromOfferId(
                getGuiLeft() + 12, getGuiTop() + 145, clicked.getOfferId(), b -> {}
        );
        addRenderableWidget(lastClickedPanel);
    }

    @Override
    public void drawFG(GuiGraphics gg, int ox, int oy, int mx, int my) {
        super.drawFG(gg, ox, oy, mx, my);
        // 只对可见面板渲染 tooltip
        for (var p : pagePanels) {
            if (p.isActive()) {
                p.renderStackTooltip(gg, mx, my, ox, oy);
            }
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        // 滚动条当前值即“首行”
        int topRow = scrollbar.getCurrentScroll();
        if (topRow != lastTopRow) {
            lastTopRow = topRow;
            relayoutVisiblePanels();
        }
    }

    /** 池：确保数量与“完整快照”一致（新增的直接挂到屏上；多余的隐藏即可） */
    private void ensurePanelPoolUpToDate() {
        var ids = menu.getOfferIdsSnapshot();

        // 扩容：为新增报价创建面板（初始隐藏）
        for (int i = pagePanels.size(); i < ids.size(); i++) {
            ResourceLocation id = ids.get(i);
            AmadronOfferPanel p = AmadronOfferPanel.fromOfferId(HIDE_X, HIDE_Y, id, b -> {});
            p.active = false;
            p.visible = false;
            addRenderableWidget(p);
            pagePanels.add(p);
        }

        // 缩容：旧面板保留但隐藏
        for (int i = ids.size(); i < pagePanels.size(); i++) {
            var p = pagePanels.get(i);
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }
    }

    /** 窗口缩放/重建后，把池里的所有面板重新挂回 Screen */
    private void reattachPanelPool() {
        for (var p : pagePanels) {
            if (!this.renderables.contains(p)) {
                addRenderableWidget(p);
            }
        }
    }

    /** 只做“重排”：根据过滤&滚动把应显示的面板摆到可见区域，其它隐藏 */
    private void relayoutVisiblePanels() {
        // 过滤后数量可能变化（例如 offers 动态变化），先更新滚动范围并保证池齐备
        updateScrollbarRangeBySize(filteredIndex.size());
        ensurePanelPoolUpToDate();

        // 全部隐藏到屏外
        for (var p : pagePanels) {
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }
        if (filteredIndex.isEmpty()) return;

        final int startRow = Math.min(scrollbar.getCurrentScroll(), Math.max(0, totalRows - VISIBLE_ROWS));
        final int startIndex = startRow * PANELS_PER_ROW;
        final int capacity = VISIBLE_ROWS * PANELS_PER_ROW;
        final int endExclusive = Math.min(startIndex + capacity, filteredIndex.size());

        for (int index = startIndex; index < endExclusive; index++) {
            int local = index - startIndex;
            int row = local / PANELS_PER_ROW;
            int col = local % PANELS_PER_ROW;

            int x = leftPos + GRID_OFFSET_X + col * (PANEL_W + GAP_X);
            int y = topPos + GRID_OFFSET_Y + row * (PANEL_H + GAP_Y);

            // 过滤后的索引 -> 原始池子索引
            int idxInSnapshot = filteredIndex.get(index);
            AmadronOfferPanel panel = pagePanels.get(idxInSnapshot);

            panel.setX(x);
            panel.setY(y);
            panel.active = true;
            panel.visible = true;
        }
    }

    /** 重算过滤索引（filteredIndex），并据此更新滚动范围 */
    private void rebuildFilter() {
        filteredIndex.clear();
        var ids = menu.getOfferIdsSnapshot();
        if (ids.isEmpty()) {
            updateScrollbarRangeBySize(0);
            return;
        }

        if (searchQuery.isEmpty()) {
            for (int i = 0; i < ids.size(); i++) filteredIndex.add(i);
        } else {
            for (int i = 0; i < ids.size(); i++) {
                if (offerMatches(ids.get(i), searchQuery)) {
                    filteredIndex.add(i);
                }
            }
        }
        updateScrollbarRangeBySize(filteredIndex.size());
    }

    /** 判定某个 offer 是否命中查询（按 offerId / 输入名 / 输出名 任意命中即通过） */
    private boolean offerMatches(ResourceLocation id, String q) {
        String needle = q.toLowerCase(Locale.ROOT);

        // 1) offerId 命中
        if (id.toString().toLowerCase(Locale.ROOT).contains(needle)) return true;

        // 2) 输入/输出名字命中
        AmadronOffer offer = AmadronOfferManager.getInstance().getOffer(id);
        if (offer == null) return false;

        String inName = safeNameLower(offer, true);
        String outName = safeNameLower(offer, false);
        return (!inName.isEmpty() && inName.contains(needle))
                || (!outName.isEmpty() && outName.contains(needle));
    }

    /** 取输入/输出名字（安全转小写，异常则空串） */
    private static String safeNameLower(AmadronOffer offer, boolean isInput)
    {
        try {
            String name = isInput ? offer.getInput().getName() : offer.getOutput().getName();
            return name == null ? "" : name.toLowerCase(Locale.ROOT);
        } catch (Throwable t) {
            return "";
        }
    }

    /** 基于“过滤后数量”更新总数/总行数与滚动范围 */
    private void updateScrollbarRangeBySize(int filteredSize) {
        this.totalOffers = filteredSize;
        this.totalRows = (int) Math.ceil(filteredSize / (double) PANELS_PER_ROW);

        int maxScroll = Math.max(0, totalRows - VISIBLE_ROWS);   // 能向下滚的最大“盈余行数”
        int pageSize = Math.max(1, VISIBLE_ROWS / 6);            // 小步滚动手感
        scrollbar.setRange(0, maxScroll, pageSize);
        scrollbar.setVisible(maxScroll > 0);
    }

    /** 点击“提交”按钮：收集所有面板的已选数量并打包提交（保留你原语义） */
    private void onSubmit() {
        MutableBasket basket = ShoppingBasket.createMutable();
        for (AmadronOfferPanel panel : pagePanels) {
            if (panel.getWantedStock() > 0) {
                basket.addUnitsToOffer(panel.getOfferId(), panel.getWantedStock());
            }
        }
        menu.sendSubmitOrderAction(basket.toImmutable());
    }

}
