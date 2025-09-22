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
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Optional;

public class AmadronOfferPanel extends AbstractWidget
{
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(AppliedPneumatics.MODID, "textures/gui/amadron_wireless_terminal.png");

    private static final Bounds staticOfferUVBounds = new Bounds(95, 282, 8, 11);
    private static final Bounds villagerOfferUVBounds = new Bounds(83, 282, 8, 11);
    private static final Bounds playerOfferUVBounds = new Bounds(107, 283, 8, 8);


    /** 面板尺寸 & 图标区域（16×16） */
    private static final int PANEL_W = 79, PANEL_H = 22;
    private static final int ICON_W = 16, ICON_H = 16;
    private static final int IN_ICON_X = 3, ICON_Y = 3;          // 左侧输入图标的相对偏移
    private static final int OUT_ICON_X = 60, OUT_ICON_Y = 3;    // 右侧输出图标的相对偏移

    /** 文字缩放与置顶的 Z 值（确保压住流体/物品渲染） */
    private static final float TEXT_SCALE = 0.666f;
    private static final float TEXT_Z = 300f;

    private static boolean inBox(int mx, int my, int x, int y, int w, int h)
    {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // 配方主键 & 缓存的显示数据
    private final ResourceLocation offerId;

    private final OfferType offerType;
    private final AmadronTradeResource input;
    private final AmadronTradeResource output;
    private final int maxStock;

    private int wantedStock; // 存储一个数据，标识玩家想购买多少个此交易

    // 点击回调
    @Nullable private final onPress onClicked;

    /** 客户端是否拿到了有效的 offer */
    private final boolean valid;

    // 仅允许通过工厂创建
    private AmadronOfferPanel(int x, int y, ResourceLocation offerId, @Nullable AmadronOffer offer, @Nullable onPress onClicked)
    {
        super(x, y, PANEL_W, PANEL_H, Component.translatable("appliedpneumatics.gui.widget.amadron_offer_panel"));
        this.offerId = offerId;
        this.onClicked = onClicked;

        if (offer != null)
        {
            this.valid = true;
            this.offerType = new OfferType(offer.isStaticOffer(), offer.isVillagerTrade(), offer instanceof AmadronPlayerOffer);
            this.input = offer.getInput();
            this.output = offer.getOutput();
            this.maxStock = offer.getStock();

        }
        else
        {
            this.valid = false;
            this.offerType = new OfferType(false, false, false);
            this.input = AmadronTradeResource.of(ItemStack.EMPTY);
            this.output = AmadronTradeResource.of(ItemStack.EMPTY);
            this.maxStock = -1;
        }
    }

    /** 工厂：用配方 id 构造面板（客户端侧） */
    public static AmadronOfferPanel fromOfferId(int x, int y, ResourceLocation id, onPress runnable)
    {
        var offer = me.desht.pneumaticcraft.common.amadron.AmadronOfferManager.getInstance().getOffer(id);
        return new AmadronOfferPanel(x, y, id, offer, runnable);
    }

    // ---------- Getters ----------

    public int getWantedStock()
    {
        return wantedStock;
    }
    public ResourceLocation getOfferId() { return offerId; }
    public boolean isValidOffer() { return valid; }
    public OfferType getOfferType() { return offerType; }
    public AmadronTradeResource getInput() { return input; }
    public AmadronTradeResource getOutput() { return output; }

    // 由于AE已经覆写点击事件，需要自己放到外界判断
    // （耦合度有点高，不过还能接受）
    public void onClicked(double mouseX, double mouseY, int button, boolean isShiftKeyDown)
    {
        super.onClick(mouseX, mouseY, button);
        if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            if(isShiftKeyDown)
            {
                double wantmulit = button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? 0.5 : 2;
                this.wantedStock = (int)Math.max(0, wantedStock * wantmulit);
                if(maxStock > 0)
                {
                    this.wantedStock = Math.min(maxStock, wantedStock);
                }
                this.wantedStock = Math.min(64, wantedStock);
            }
            else
            {
                int wantedAdd = button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? -1 : 1;
                this.wantedStock = Math.max(0, wantedStock + wantedAdd);
                if(maxStock > 0)
                {
                    this.wantedStock = Math.min(maxStock, wantedStock);
                }
                this.wantedStock = Math.min(64, wantedStock);
            }

        }
        else if(button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
        {
            this.wantedStock = 0;
        }
    }

    // ---------- 渲染 ----------
    @Override
    protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float pt)
    {
        // 背景
        gui.blit(BG, getX(), getY(), 1, 271, PANEL_W, PANEL_H, 512, 512);

        if (!valid) {
            gui.drawString(Minecraft.getInstance().font, "…", getX() + 4, getY() + 7, 0x777777);
            return;
        }

        final var font = Minecraft.getInstance().font;
        final int x0 = getX(), y0 = getY();

        // 绘制成分
        drawAmadronTradeResource(gui, input, font, x0 + IN_ICON_X, y0 + ICON_Y);
        drawAmadronTradeResource(gui, output, font, x0 + OUT_ICON_X, y0 + ICON_Y);

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
        else if(offerType.isPlayer)
        {
            typeU = playerOfferUVBounds.x;
            typeV = playerOfferUVBounds.y;
            typeW = playerOfferUVBounds.width;
            typeH = playerOfferUVBounds.height;
        }
        if(typeU != -1 && typeV != -1 && typeW != -1 && typeH != -1 && !offerType.isPlayer)
        {
            gui.blit(BG, getX() + 35, getY() + 9, 300, typeU, typeV, typeW, typeH, 512, 512);
        }
        else if(typeU != -1 && typeV != -1 && typeW != -1 && typeH != -1)
        {
            gui.blit(BG, getX() + 35, getY() + 10, 300, typeU, typeV, typeW, typeH, 512, 512);
        }

        // 绘制可交易数量
        var pose = gui.pose();
        pose.pushPose();
        pose.translate(x0, y0, TEXT_Z);
        pose.scale(0.5f, 0.5f, 1.0f);

        String wantedText = "";
        String maxStockText = "";
        if(wantedStock > 0)
        {
            wantedText = String.valueOf(wantedStock);
        }
        if(maxStock > 0)
        {
            maxStockText = String.valueOf(maxStock);
        }

        String renderText = !maxStockText.isEmpty() ? String.valueOf(wantedStock) + " / " + maxStockText : wantedText;

        gui.drawCenteredString(font, renderText, 77, 5, 0xFFFFFF);
        pose.popPose();
    }

    private static void drawAmadronTradeResource(GuiGraphics gui, AmadronTradeResource resource, Font font, int x, int y)
    {
        ItemStack mayItem = resource.getItem();
        FluidStack mayFluid = resource.getFluid();
        if (!mayItem.isEmpty())
        {
            gui.renderItem(mayItem, x, y);
            drawCountBottomRight(gui, font, String.valueOf(mayItem.getCount()), x, y);
        }
        else if(!mayFluid.isEmpty())
        {
            IngredientRenderer.darwFluidAs16WHTiledSprite(gui, mayFluid.getFluid(), x, y);
            String inText = String.format("%.1fB", mayFluid.getAmount() / 1000.0);
            drawCountBottomRight(gui, font, inText, x, y);
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
    public void renderStackTooltip(GuiGraphics gui, int mouseX, int mouseY, int offsetX, int offsetY)
    {
        if (!valid) return;

        final int x0 = getX(), y0 = getY();
        final int inX = x0 + IN_ICON_X, inY = y0 + ICON_Y;
        final int outX = x0 + OUT_ICON_X, outY = y0 + OUT_ICON_Y;

        // 输入区域
        if (inBox(mouseX, mouseY, inX, inY, ICON_W, ICON_H))
        {
            drawAmadronTradeResourceTooltip(gui, input, mouseX - offsetX, mouseY - offsetY);
        }

        // 输出区域
        if (inBox(mouseX, mouseY, outX, outY, ICON_W, ICON_H))
        {
            drawAmadronTradeResourceTooltip(gui, output, mouseX - offsetX, mouseY - offsetY);
        }
    }

    private static void drawAmadronTradeResourceTooltip(GuiGraphics gui, AmadronTradeResource resource, int mouseX, int mouseY)
    {
        ItemStack mayItem = resource.getItem();
        FluidStack mayFluid = resource.getFluid();
        if(!mayItem.isEmpty())
        {
            gui.renderTooltip(Minecraft.getInstance().font, mayItem, mouseX, mouseY);
        }
        else if(!mayFluid.isEmpty())
        {
            var font = Minecraft.getInstance().font;
            var lines = new ArrayList<Component>(2);
            lines.add(mayFluid.getHoverName());
            lines.add(Component.literal(mayFluid.getAmount() + " mB").withStyle(net.minecraft.ChatFormatting.GRAY));
            gui.renderTooltip(font, lines, Optional.empty(), mouseX , mouseY );
        }
    }

    // ---------- 无障碍 ----------
    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput out) {}

    /** 交易类型记录 */
    public record OfferType(boolean isStatic, boolean isVillager, boolean isPlayer) {}

    /** 类型图标记录 */
    public record Bounds(int x, int y, int width, int height) {}

    public interface onPress
    {
        void onPress(int button);
    }
}
