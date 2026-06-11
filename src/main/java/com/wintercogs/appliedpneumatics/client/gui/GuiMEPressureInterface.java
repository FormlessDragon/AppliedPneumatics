package com.wintercogs.appliedpneumatics.client.gui;

import ae2.client.gui.implementations.GuiUpgradeable;
import ae2.client.gui.style.GuiStyle;
import com.wintercogs.appliedpneumatics.common.gui.ContainerMEPressureInterface;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;

@SideOnly(Side.CLIENT)
public class GuiMEPressureInterface extends GuiUpgradeable<ContainerMEPressureInterface> {
    public GuiMEPressureInterface(ContainerMEPressureInterface container, InventoryPlayer playerInventory,
                                  ITextComponent title, GuiStyle style) {
        super(container, playerInventory, title, style);

        widgets.addButton("add_button_1", new TextComponentString("+1"), () -> {
            float multiplier = isShiftKeyDown() ? 5 : 1;
            container.sendExpectedPressureActionToServer(1 * multiplier);
        });
        widgets.addButton("add_button_01", new TextComponentString("+0.1"), () -> {
            float multiplier = isShiftKeyDown() ? 5 : 1;
            container.sendExpectedPressureActionToServer(0.1f * multiplier);
        });
        widgets.addButton("reduce_button_01", new TextComponentString("-0.1"), () -> {
            float multiplier = isShiftKeyDown() ? 5 : 1;
            container.sendExpectedPressureActionToServer(-0.1f * multiplier);
        });
        widgets.addButton("reduce_button_1", new TextComponentString("-1"), () -> {
            float multiplier = isShiftKeyDown() ? 5 : 1;
            container.sendExpectedPressureActionToServer(-1 * multiplier);
        });
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        ContainerMEPressureInterface container = getContainer();
        String air = String.format(Locale.ROOT, "%.1f", container.latestAir / 1000.0);
        String volume = String.format(Locale.ROOT, "%.0f", container.latestVolume / 1000.0);
        String currentPressure = container.latestVolume <= 0
            ? "0"
            : String.format(Locale.ROOT, "%.0f", (double) container.latestAir / container.latestVolume);
        String expectedPressure = String.format(Locale.ROOT, "%.1f", container.latestExpectedPressure);

        setTextContent("air_amount", new TextComponentTranslation(
            "menu.label.appliedpneumatics.me_pressure_interface.air_amount", air, volume, currentPressure));
        setTextContent("max_pressure", new TextComponentTranslation(
            "menu.label.appliedpneumatics.me_pressure_interface.max_pressure", container.latestDangerPressure));
        setTextContent("current_pressure", new TextComponentString(currentPressure));
        setTextContent("expected_pressure_text", new TextComponentTranslation(
            "menu.label.appliedpneumatics.me_pressure_interface.expected_pressure_text"));
        setTextContent("expected_pressure_change_mult_text", new TextComponentTranslation(
            "menu.label.appliedpneumatics.me_pressure_interface.expected_pressure_change_mult_text"));
    }
}

