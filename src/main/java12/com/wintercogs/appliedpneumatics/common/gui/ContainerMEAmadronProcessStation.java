package com.wintercogs.appliedpneumatics.common.gui;

import ae2.api.inventories.InternalInventory;
import ae2.container.SlotSemantics;
import ae2.container.guisync.GuiSync;
import ae2.container.implementations.UpgradeableContainer;
import ae2.container.slot.AppEngSlot;
import ae2.container.slot.OutputSlot;
import ae2.container.slot.RestrictedInputSlot;
import ae2.helpers.externalstorage.GenericStackInv;
import ae2.util.ConfigGuiInventory;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronProcessStationTile;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

public class ContainerMEAmadronProcessStation extends UpgradeableContainer<MEAmadronProcessStationTile> {
    public static final String ACTION_CANCEL_ALL_JOBS = "cancel_all_jobs";

    @GuiSync(10)
    public int latestJobs;

    public ContainerMEAmadronProcessStation(InventoryPlayer ip, MEAmadronProcessStationTile host) {
        super(ip, host);
        registerClientAction(ACTION_CANCEL_ALL_JOBS, this::onCancelAllJobsAction);
    }

    @Override
    protected void setupInventorySlots() {
        for (int index = 0; index < getHost().getPatternInventory().size(); index++) {
            addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN,
                getHost().getPatternInventory(), index), SlotSemantics.ENCODED_PATTERN);
        }

        ConfigGuiInventory inputInventory = getHost().getInputInventory().createGuiWrapper();
        for (int index = 0; index < inputInventory.size(); index++) {
            addSlot(new AppEngSlot(inputInventory, index), SlotSemantics.MACHINE_INPUT);
        }

        InternalInventory outputInventory = createOutputGuiInventory(getHost().getOutputInventory());
        for (int index = 0; index < outputInventory.size(); index++) {
            addSlot(new OutputSlot(outputInventory, index, 0, 0), SlotSemantics.MACHINE_OUTPUT);
        }
    }

    @Override
    protected int getPlayerInventoryTop() {
        return getHost().getPatternInventory().size() > 9 ? 156 : 138;
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if (isServerSide()) {
            latestJobs = getHost().getJobAmount();
        }
        super.standardDetectAndSendChanges();
    }

    static InternalInventory createOutputGuiInventory(GenericStackInv outputInventory) {
        return new ReadOnlyOutputGuiInventory(outputInventory.createGuiWrapper());
    }

    public void sendCancelAllJobsActionToServer() {
        sendClientAction(ACTION_CANCEL_ALL_JOBS);
    }

    private void onCancelAllJobsAction() {
        getHost().cancelAllJobs(new TextComponentTranslation(
            "amadron.appliedpneumatics.process_fail.order_cancel", getHost().getPos().toString()));
    }

    private static final class ReadOnlyOutputGuiInventory implements InternalInventory {
        private final ConfigGuiInventory displayInventory;

        private ReadOnlyOutputGuiInventory(ConfigGuiInventory displayInventory) {
            this.displayInventory = displayInventory;
        }

        @Override
        public int size() {
            return displayInventory.size();
        }

        @Override
        public int getSlotLimit(int slot) {
            return displayInventory.getSlotLimit(slot);
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            return displayInventory.getStackInSlot(slotIndex);
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return displayInventory.extractItem(slot, amount, simulate);
        }
    }
}

