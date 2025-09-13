package com.wintercogs.appliedpneumatics.client.gui;

import appeng.client.gui.widgets.AE2Button;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEPressureInterfaceBlockEntity;
import com.wintercogs.appliedpneumatics.common.menu.MEPressureInterfaceMenu;
import com.wintercogs.appliedpneumatics.common.network.ExpectedPressureChangePacket;
import com.wintercogs.appliedpneumatics.util.GuiRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class MEPressureInterfaceGUI extends AbstractContainerScreen<MEPressureInterfaceMenu>
{
    public static final ResourceLocation Background = ResourceLocation.tryBuild(AppliedPneumatics.MODID, "textures/gui/me_pressure_interface_menu.png");

    private final MEPressureInterfaceBlockEntity be;
    private AE2Button addExpectedPressureButton;
    private AE2Button addExpectedPressureButtonLess;
    private AE2Button reduceExpectedPressureButton;
    private AE2Button reduceExpectedPressureButtonLess;

    public MEPressureInterfaceGUI(MEPressureInterfaceMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.be = menu.getBlockEntity();
    }

    @Override
    protected void init()
    {
        this.imageWidth = 176;
        this.imageHeight = 207;
        this.leftPos = (this.width - imageWidth)/2;
        this.topPos = (this.height - imageHeight)/2;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 110;

        addExpectedPressureButton = new AE2Button(this.leftPos + 168 - 26,this.topPos + 76,26,20,Component.literal("+1"),button -> {
            PacketDistributor.sendToServer(new ExpectedPressureChangePacket(1f));
        });
        addRenderableWidget(addExpectedPressureButton);

        addExpectedPressureButtonLess = new AE2Button(this.leftPos + 168 - 26 - 34,this.topPos + 76,26,20,Component.literal("+0.1"),button -> {
            PacketDistributor.sendToServer(new ExpectedPressureChangePacket(0.1f));
        });
        addRenderableWidget(addExpectedPressureButtonLess);

        reduceExpectedPressureButtonLess = new AE2Button(this.leftPos + 42,this.topPos + 76,26,20,Component.literal("-0.1"),button -> {
            PacketDistributor.sendToServer(new ExpectedPressureChangePacket(-0.1f));
        });
        addRenderableWidget(reduceExpectedPressureButtonLess);

        reduceExpectedPressureButton = new AE2Button(this.leftPos + 8,this.topPos + 76,26,20,Component.literal("-1"),button -> {
            PacketDistributor.sendToServer(new ExpectedPressureChangePacket(-1f));
        });
        addRenderableWidget(reduceExpectedPressureButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics gui, float v, int x, int y)
    {
        if(Background != null)
            gui.blit(Background, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y)
    {
        super.renderTooltip(guiGraphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        String airStr = String.format(Locale.ROOT, "%.1f", (float) menu.latestAir / 1000f);
        String maxStr = String.format(Locale.ROOT, "%.1f", (float) menu.latestBaseVolume / 1000f);
        String currentPressureStr = String.format(Locale.ROOT, "%.0f", (float)menu.latestAir / (float)menu.latestBaseVolume);
        String pressureStr = String.format(Locale.ROOT, "%.1f", menu.latestExpectedPressure);
        GuiRenderHelper.drawCenteredInRegion(guiGraphics, this.font, Component.translatable("menu.label.appliedpneumatics.me_pressure_interface.air_amount",airStr, maxStr, currentPressureStr), 13, 148, 22, 4210752, false);
        GuiRenderHelper.drawCenteredInRegion(guiGraphics, this.font, Component.translatable("menu.label.appliedpneumatics.me_pressure_interface.max_pressure", menu.latestDangerPressure), 13, 148, 38, 4210752, false);
        GuiRenderHelper.drawCenteredInRegion(guiGraphics, this.font, Component.literal(pressureStr), 13, 168, 82, 4210752, false);
        GuiRenderHelper.drawCenteredInRegion(guiGraphics, this.font, Component.translatable("menu.label.appliedpneumatics.me_pressure_interface.expected_pressure_text"), 13, 168, 60, 4210752, false);

    }

    @Override
    protected void renderSlotHighlight(@NotNull GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick)
    {
        if (slot.isHighlightable()) {
            int x = slot.x;
            int y = slot.y;
            int w = 16, h = 16;

            guiGraphics.hLine(x, x + w, y - 1, 0xFFdaffff);
            guiGraphics.hLine(x - 1, x + w, y + h, 0xFFdaffff);
            guiGraphics.vLine(x - 1, y - 2, y + h, 0xFFdaffff);
            guiGraphics.vLine(x + w, y - 2, y + h, 0xFFdaffff);
            guiGraphics.fillGradient(RenderType.guiOverlay(), x, y, x + w, y + h, 0x669cd3ff, 0x669cd3ff, 0);
        }
    }
}
