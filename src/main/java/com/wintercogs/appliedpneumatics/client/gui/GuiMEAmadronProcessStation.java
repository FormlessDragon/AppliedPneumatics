package com.wintercogs.appliedpneumatics.client.gui;

import ae2.client.gui.implementations.GuiUpgradeable;
import ae2.client.gui.style.GuiStyle;
import ae2.client.gui.widgets.AE2Button;
import com.wintercogs.appliedpneumatics.common.gui.ContainerMEAmadronProcessStation;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMEAmadronProcessStation extends GuiUpgradeable<ContainerMEAmadronProcessStation> {
    public GuiMEAmadronProcessStation(ContainerMEAmadronProcessStation container, InventoryPlayer playerInventory,
                                      ITextComponent title, GuiStyle style) {
        super(container, playerInventory, title, style);
        AE2Button cancelJobsButton = widgets.addButton("cancel_jobs_button",
            new TextComponentTranslation("menu.appliedpneumatics.button.cancel_jobs"),
            container::sendCancelAllJobsActionToServer);
        widgets.addOpenPriorityButton();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        setTextContent("job_amount", new TextComponentTranslation(
            "menu.label.appliedpneumatics.me_amadron_process_station.job_amount", getContainer().latestJobs));
    }
}

