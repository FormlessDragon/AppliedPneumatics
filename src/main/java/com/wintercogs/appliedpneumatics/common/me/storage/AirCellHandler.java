package com.wintercogs.appliedpneumatics.common.me.storage;

import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import com.wintercogs.appliedpneumatics.common.items.AirStorageCell;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AirCellHandler implements ICellHandler
{
    @Override
    public boolean isCell(ItemStack is)
    {
        return is.getItem() instanceof AirStorageCell;
    }

    @Override
    public @Nullable StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host)
    {
        if(is.getItem() instanceof AirStorageCell cell)
        {
            return new AirCellInventory(is, cell, host);
        }
        return null;
    }
}
