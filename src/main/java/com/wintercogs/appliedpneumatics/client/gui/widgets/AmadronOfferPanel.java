package com.wintercogs.appliedpneumatics.client.gui.widgets;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.util.IngredientRenderer;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class AmadronOfferPanel extends AbstractWidget
{
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(AppliedPneumatics.MODID, "textures/gui/amadron_wireless_terminal.png");

    private static final Bounds staticOfferUVBounds = new Bounds(95, 244, 8, 11);
    private static final Bounds villagerOfferUVBounds = new Bounds(83, 244, 8, 11);


    /** 面板尺寸 & 图标区域（16×16） */
    private static final int PANEL_W = 79, PANEL_H = 22;
    private static final int ICON_W = 16, ICON_H = 16;
    private static final int IN_ICON_X = 3, ICON_Y = 3;          // 左侧输入图标的相对偏移
    private static final int OUT_ICON_X = 60, OUT_ICON_Y = 3;    // 右侧输出图标的相对偏移

    /** 文字缩放与置顶的 Z 值（确保压住流体/物品渲染） */
    private static final float TEXT_SCALE = 0.666f;
    private static final float TEXT_Z = 300f;

    private static boolean inBox(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ---- 关键：配方主键 & 缓存的显示数据 ----
    private final ResourceLocation offerId;

    private final OfferType offerType;
    private final AmadronTradeResource input;
    private final AmadronTradeResource output;
    private final int tradeLevel;
    private final int maxStock;
    private final int initialStock;

    // 预拆解缓存，仅用于渲染
    @Nullable private final ItemStack inputItem, outputItem;
    @Nullable private final FluidStack inputFluid, outputFluid;
    private final boolean inputIsItem, outputIsItem;

    /** 客户端是否拿到了有效的 offer */
    private final boolean valid;

    // 仅允许通过工厂创建
    private AmadronOfferPanel(int x, int y, ResourceLocation offerId, @Nullable AmadronOffer offer) {
        super(x, y, PANEL_W, PANEL_H, Component.translatable("appliedpneumatics.gui.widget.amadron_offer_panel"));
        this.offerId = offerId;

        if (offer != null) {
            this.valid        = true;
            this.offerType    = new OfferType(offer.isStaticOffer(), offer.isVillagerTrade(), offer instanceof AmadronPlayerOffer);
            this.input        = offer.getInput();
            this.output       = offer.getOutput();
            this.tradeLevel   = offer.getTradeLevel();
            this.maxStock     = offer.getMaxStock();
            this.initialStock = offer.getStock();

            this.inputItem    = input.getItem();
            this.outputItem   = output.getItem();
            this.inputFluid   = input.getFluid();
            this.outputFluid  = output.getFluid();
            this.inputIsItem  = !inputItem.isEmpty();
            this.outputIsItem = !outputItem.isEmpty();
        } else {
            this.valid        = false;
            this.offerType    = new OfferType(false, false, false);
            this.input        = AmadronTradeResource.of(ItemStack.EMPTY);
            this.output       = AmadronTradeResource.of(ItemStack.EMPTY);
            this.tradeLevel   = 1;
            this.maxStock     = -1;
            this.initialStock = -1;

            this.inputItem = this.outputItem = ItemStack.EMPTY;
            this.inputFluid = this.outputFluid = FluidStack.EMPTY;
            this.inputIsItem = this.outputIsItem = false;
        }
    }

    /** 工厂：用配方 id 构造面板（客户端侧） */
    public static AmadronOfferPanel fromOfferId(int x, int y, ResourceLocation id) {
        var offer = me.desht.pneumaticcraft.common.amadron.AmadronOfferManager.getInstance().getOffer(id);
        return new AmadronOfferPanel(x, y, id, offer);
    }

    // ---------- Getters ----------
    public ResourceLocation getOfferId() { return offerId; }
    public boolean isValidOffer() { return valid; }
    public OfferType getOfferType() { return offerType; }
    public AmadronTradeResource getInput() { return input; }
    public AmadronTradeResource getOutput() { return output; }

    // ---------- 渲染 ----------
    @Override
    protected void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float pt)
    {
        // 背景
        g.blit(BG, getX(), getY(), 1, 233, PANEL_W, PANEL_H, 256, 256);

        if (!valid) {
            g.drawString(Minecraft.getInstance().font, "…", getX() + 4, getY() + 7, 0x777777);
            return;
        }

        final var font = Minecraft.getInstance().font;
        final int x0 = getX(), y0 = getY();

        // 左侧：输入
        if (inputIsItem) {
            g.renderItem(inputItem, x0 + IN_ICON_X, y0 + ICON_Y);
            drawCountBottomRight(g, font, String.valueOf(inputItem.getCount()),
                    x0 + IN_ICON_X, y0 + ICON_Y);
        } else {
            IngredientRenderer.darwFluidAs16WHTiledSprite(g, input.getFluid(), x0 + IN_ICON_X, y0 + ICON_Y);
            String inText = String.format("%.1fB", input.getAmount() / 1000.0);
            drawCountBottomRight(g, font, inText, x0 + IN_ICON_X, y0 + ICON_Y);
        }

        // 右侧：输出
        if (outputIsItem) {
            g.renderItem(outputItem, x0 + OUT_ICON_X, y0 + OUT_ICON_Y);
            drawCountBottomRight(g, font, String.valueOf(outputItem.getCount()),
                    x0 + OUT_ICON_X, y0 + OUT_ICON_Y);
        } else {
            IngredientRenderer.darwFluidAs16WHTiledSprite(g, output.getFluid(), x0 + OUT_ICON_X, y0 + ICON_Y);
            String outText = String.format("%.1fB", output.getAmount() / 1000.0);
            drawCountBottomRight(g, font, outText, x0 + OUT_ICON_X, y0 + OUT_ICON_Y);
        }

        int typeU = -1, typeV = -1, typeW = -1, typeH = -1;
        // 绘制类型图标
        if(offerType.isStatic && !offerType.isPlayer)
        {
            typeU = staticOfferUVBounds.x;
            typeV = staticOfferUVBounds.y;
            typeW = staticOfferUVBounds.width;
            typeH = staticOfferUVBounds.height;
        }
        else if(offerType.isVillager)
        {
            typeU = villagerOfferUVBounds.x;
            typeV = villagerOfferUVBounds.y;
            typeW = villagerOfferUVBounds.width;
            typeH = villagerOfferUVBounds.height;
        }
        if(typeU != -1 && typeV != -1 && typeW != -1 && typeH != -1)
        {
            g.blit(BG, getX() + 35, getY() + 9, 300, typeU, typeV, typeW, typeH, 256, 256);
        }
        if(offerType.isPlayer)
        {

        }
    }

    /** 在指定 16×16 图标区域的右下角绘制缩放文字，并抬高 Z，保证文字压住图标/流体 */
    private static void drawCountBottomRight(GuiGraphics g, Font font, String text, int iconX, int iconY)
    {
        float s = TEXT_SCALE;

        var pose = g.pose();
        pose.pushPose();
        pose.translate(0, 0, TEXT_Z);
        pose.scale(s, s, s);

        int w = font.width(text);
        final int X = (int) ((iconX + -1 + 16.0f + 2.0f - w * s) / s);
        final int Y = (int) ((iconY + -1 + 16.0f - 5.0f * s) / s);

        g.drawString(font, text, X, Y, 0xFFFFFF);
        pose.popPose();
    }

    // 由外部UI自行调用
    public void renderStackTooltip(GuiGraphics g, int mouseX, int mouseY, int offsetX, int offsetY)
    {
        if (!valid) return;

        final int x0 = getX(), y0 = getY();
        final int inX = x0 + IN_ICON_X, inY = y0 + ICON_Y;
        final int outX = x0 + OUT_ICON_X, outY = y0 + OUT_ICON_Y;

        // 输入区域
        if (inBox(mouseX, mouseY, inX, inY, ICON_W, ICON_H))
        {
            if (inputIsItem && inputItem != null && !inputItem.isEmpty())
            {
                g.renderTooltip(Minecraft.getInstance().font, inputItem, mouseX - offsetX, mouseY - offsetY);
                return;
            }
            else if (inputFluid != null && !inputFluid.isEmpty())
            {
                var font = Minecraft.getInstance().font;
                var lines = new ArrayList<Component>(2);
                lines.add(inputFluid.getHoverName());
                lines.add(Component.literal(inputFluid.getAmount() + " mB")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
                g.renderTooltip(font, lines, Optional.empty(), mouseX - offsetX, mouseY - offsetY);
                return;
            }
        }

        // 输出区域
        if (inBox(mouseX, mouseY, outX, outY, ICON_W, ICON_H))
        {
            if (outputIsItem && outputItem != null && !outputItem.isEmpty())
            {
                g.renderTooltip(Minecraft.getInstance().font, outputItem, mouseX - offsetX, mouseY - offsetY);
            }
            else if (outputFluid != null && !outputFluid.isEmpty())
            {
                var font = Minecraft.getInstance().font;
                var lines = new ArrayList<Component>(2);
                lines.add(outputFluid.getHoverName());
                lines.add(Component.literal(outputFluid.getAmount() + " mB")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
                g.renderTooltip(font, lines, Optional.empty(), mouseX - offsetX, mouseY - offsetY);
            }
        }
    }

    // ---------- 无障碍 ----------
    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput out) {}

    /** 交易类型记录 */
    public record OfferType(boolean isStatic, boolean isVillager, boolean isPlayer) {}

    /** 类型图标记录 */
    public record Bounds(int x, int y, int width, int height) {}
}
