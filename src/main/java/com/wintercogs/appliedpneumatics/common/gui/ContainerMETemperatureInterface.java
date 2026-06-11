package com.wintercogs.appliedpneumatics.common.gui;

import ae2.container.guisync.GuiSync;
import ae2.container.implementations.UpgradeableContainer;
import com.wintercogs.appliedpneumatics.common.tile.METemperatureInterfaceTile;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerMETemperatureInterface extends UpgradeableContainer<METemperatureInterfaceTile> {
    public static final String ACTION_CHANGE_EXPECTED_TEMPERATURE = "change_expected_temperature";

    @GuiSync(10)
    public double latestTemperature;
    @GuiSync(11)
    public double latestHeatCapacity;
    @GuiSync(12)
    public double latestExpectedTemperature;

    public ContainerMETemperatureInterface(InventoryPlayer ip, METemperatureInterfaceTile host) {
        super(ip, host);
        registerClientAction(ACTION_CHANGE_EXPECTED_TEMPERATURE, Double.class, this::onClientChangeExpectedTemperature);
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if (isServerSide()) {
            latestTemperature = getHost().getHeatExchangerLogic(null).getTemperature();
            latestHeatCapacity = getHost().getHeatExchangerLogic(null).getThermalCapacity();
            latestExpectedTemperature = getHost().getExpectedTemperature();
        }
        super.standardDetectAndSendChanges();
    }

    public void sendExpectedTemperatureActionToServer(double expectedTemperatureDelta) {
        sendClientAction(ACTION_CHANGE_EXPECTED_TEMPERATURE, expectedTemperatureDelta);
    }

    private void onClientChangeExpectedTemperature(double delta) {
        getHost().setExpectedTemperature(getHost().getExpectedTemperature() + delta);
    }
}

