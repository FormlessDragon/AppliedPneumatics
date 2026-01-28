package com.wintercogs.appliedpneumatics.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import com.wintercogs.appliedpneumatics.client.gui.widgets.AE2TinyButton;
import com.wintercogs.appliedpneumatics.common.menu.MEAmadronProcessStationMenu;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MEAmadronProcessStationGUI extends UpgradeableScreen<MEAmadronProcessStationMenu>
{
    public static final String COMMON = "/screens/me_amadron_process_station.json";
    public static final String EXTENDED = "/screens/me_amadron_extended_process_station.json";

    private final AE2TinyButton cancelButton;

    public MEAmadronProcessStationGUI(MEAmadronProcessStationMenu menu, Inventory inv, Component title, String stylePath)
    {
        super(menu, inv, title, StyleManager.loadStyleDoc(stylePath));
        cancelButton = new AE2TinyButton(Component.translatable("menu.appliedpneumatics.button.cancel_jobs"), button -> menu.senCancelJobAction());
        cancelButton.setTooltip(Tooltip.create(Component.translatable("menu.appliedpneumatics.button.cancel_jobs.tooltip")));
        widgets.add("cancel_jobs_button", this.cancelButton);
        widgets.addOpenPriorityButton();
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();
        setTextContent("job_amount", Component.translatable("menu.label.appliedpneumatics.me_amadron_process_station.job_amount", menu.latestJobs));
    }
}
