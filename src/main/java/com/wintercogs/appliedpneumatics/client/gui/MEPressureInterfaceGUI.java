package com.wintercogs.appliedpneumatics.client.gui;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEPressureInterfaceBlockEntity;
import com.wintercogs.appliedpneumatics.common.menu.MEPressureInterfaceMenu;
import com.wintercogs.appliedpneumatics.util.GuiRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class MEPressureInterfaceGUI extends AbstractContainerScreen<MEPressureInterfaceMenu>
{
    public static final ResourceLocation Background = ResourceLocation.tryBuild(AppliedPneumatics.MODID, "textures/gui/me_pressure_interface_menu.png");

    private final MEPressureInterfaceBlockEntity be;

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


    }

    @Override
    protected void renderBg(@NotNull GuiGraphics gui, float v, int i, int i1)
    {
        if(Background != null)
            gui.blit(Background, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight);

    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        float airL = (float) be.getAirHandler().getAir() / 1000f;
        float maxL = (float) be.getMaxVolume() / 1000f;
        String airStr = String.format(Locale.ROOT, "%.1f", airL);
        String maxStr = String.format(Locale.ROOT, "%.1f", maxL);
        GuiRenderHelper.drawCenteredInRegion(guiGraphics, this.font, Component.translatable("menu.label.appliedpneumatics.me_pressure_interface.air_amount",airStr, maxStr), 13, 148, 22, 4210752, false );
        GuiRenderHelper.drawCenteredInRegion(guiGraphics, this.font,  Component.translatable("menu.label.appliedpneumatics.me_pressure_interface.max_pressure",be.getAirHandler().getDangerPressure()), 13, 148, 38, 4210752, false );
    }
}
