package com.wintercogs.appliedpneumatics.client.gui;

import ae2.client.gui.implementations.GuiUpgradeable;
import ae2.client.gui.style.GuiStyle;
import com.wintercogs.appliedpneumatics.common.gui.ContainerMETemperatureInterface;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;

@SideOnly(Side.CLIENT)
public class GuiMETemperatureInterface extends GuiUpgradeable<ContainerMETemperatureInterface> {
    public GuiMETemperatureInterface(ContainerMETemperatureInterface container, InventoryPlayer playerInventory,
                                     ITextComponent title, GuiStyle style) {
        super(container, playerInventory, title, style);

        widgets.addButton("add_button_10", new TextComponentString("+10"), () -> {
            double multiplier = isShiftKeyDown() ? 10 : 1;
            container.sendExpectedTemperatureActionToServer(10 * multiplier);
        });
        widgets.addButton("add_button_1", new TextComponentString("+1"), () -> {
            double multiplier = isShiftKeyDown() ? 10 : 1;
            container.sendExpectedTemperatureActionToServer(1 * multiplier);
        });
        widgets.addButton("reduce_button_1", new TextComponentString("-1"), () -> {
            double multiplier = isShiftKeyDown() ? 10 : 1;
            container.sendExpectedTemperatureActionToServer(-1 * multiplier);
        });
        widgets.addButton("reduce_button_10", new TextComponentString("-10"), () -> {
            double multiplier = isShiftKeyDown() ? 10 : 1;
            container.sendExpectedTemperatureActionToServer(-10 * multiplier);
        });
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        ContainerMETemperatureInterface container = getContainer();
        String temperature = String.format(Locale.ROOT, "%.1f", container.latestTemperature - 273) + "\u2103";
        String heatCapacity = String.format(Locale.ROOT, "%.0f", container.latestHeatCapacity);
        String expectedTemperature = String.format(Locale.ROOT, "%.1f", container.latestExpectedTemperature - 273);

        setTextContent("temperature", new TextComponentTranslation(
            "menu.label.appliedpneumatics.me_temperature_interface.temperature", temperature));
        setTextContent("heat_cap", new TextComponentTranslation(
            "menu.label.appliedpneumatics.me_temperature_interface.heat_cap", heatCapacity));
        setTextContent("expected_temperature", new TextComponentString(expectedTemperature));
        setTextContent("expected_temperature_text", new TextComponentTranslation(
            "menu.label.appliedpneumatics.me_temperature_interface.expected_temperature_text"));
        setTextContent("expected_temperature_change_mult_text", new TextComponentTranslation(
            "menu.label.appliedpneumatics.me_temperature_interface.expected_temperature_change_mult_text"));
    }
}

