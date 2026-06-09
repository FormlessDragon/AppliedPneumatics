package com.wintercogs.appliedpneumatics.common.gui;

import ae2.container.SlotSemantics;
import ae2.container.guisync.GuiSync;
import ae2.container.implementations.UpgradeableContainer;
import ae2.container.slot.AppEngSlot;
import com.wintercogs.appliedpneumatics.common.tile.MEPressureInterfaceTile;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerMEPressureInterface extends UpgradeableContainer<MEPressureInterfaceTile> {
    public static final String ACTION_CHANGE_EXPECTED_PRESSURE = "change_expected_pressure";

    @GuiSync(10)
    public int latestAir;
    @GuiSync(11)
    public int latestVolume;
    @GuiSync(12)
    public double latestExpectedPressure;
    @GuiSync(13)
    public double latestDangerPressure;

    public ContainerMEPressureInterface(InventoryPlayer ip, MEPressureInterfaceTile host) {
        super(ip, host);
        registerClientAction(ACTION_CHANGE_EXPECTED_PRESSURE, Float.class, this::onClientChangeExpectedPressure);
    }

    @Override
    protected void setupInventorySlots() {
        addSlot(new AppEngSlot(getHost().getInternalInventory(), 0), SlotSemantics.MACHINE_INPUT);
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if (isServerSide()) {
            latestAir = getHost().getAirHandler(null).getAir();
            latestVolume = getHost().getAirHandler(null).getVolume();
            latestExpectedPressure = getHost().getExpectedPressure();
            latestDangerPressure = getHost().getAirHandler(null).getDangerPressure();
        }
        super.standardDetectAndSendChanges();
    }

    public void sendExpectedPressureActionToServer(float expectedPressureDelta) {
        sendClientAction(ACTION_CHANGE_EXPECTED_PRESSURE, expectedPressureDelta);
    }

    private void onClientChangeExpectedPressure(float delta) {
        getHost().setExpectedPressure(getHost().getExpectedPressure() + delta);
    }
}

