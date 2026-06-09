package com.wintercogs.appliedpneumatics.common.me.storage;

import ae2.api.storage.cells.ICellHandler;
import ae2.api.storage.cells.ISaveProvider;
import ae2.api.storage.cells.StorageCell;
import com.wintercogs.appliedpneumatics.common.item.AirStorageCell;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public final class AirCellHandler implements ICellHandler {
    public static final AirCellHandler INSTANCE = new AirCellHandler();

    private AirCellHandler() {
    }

    @Override
    public boolean isCell(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof AirStorageCell;
    }

    @Nullable
    @Override
    public StorageCell getCellInventory(ItemStack stack, @Nullable ISaveProvider host) {
        if (!(stack.getItem() instanceof AirStorageCell cellType)) {
            return null;
        }
        return new AirCellInventory(stack, cellType, host);
    }
}

