package com.wintercogs.appliedpneumatics.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import com.wintercogs.appliedpneumatics.client.gui.widgets.AE2TinyButton;
import com.wintercogs.appliedpneumatics.common.menu.MEAmadronProcessStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MEAmadronProcessStationGUI extends UpgradeableScreen<MEAmadronProcessStationMenu>
{
    private final AE2TinyButton cancelButton;

    public MEAmadronProcessStationGUI(MEAmadronProcessStationMenu menu, Inventory inv, Component title)
    {
        super(menu, inv, title, StyleManager.loadStyleDoc("/screens/me_amadron_process_station.json"));
        cancelButton = new AE2TinyButton(Component.translatable("menu.appliedpneumatics.button.cancel_jobs"), button -> menu.senCancelJobAction());
        cancelButton.setTooltip(Tooltip.create(Component.translatable("menu.appliedpneumatics.button.cancel_jobs.tooltip")));
        widgets.add("cancel_jobs_button", this.cancelButton);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY)
    {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);
        guiGraphics.drawString(font,Component.translatable("menu.label.appliedpneumatics.me_amadron_process_station.job_amount",menu.latestJobs), 8,39,4210752,false);
    }
}
