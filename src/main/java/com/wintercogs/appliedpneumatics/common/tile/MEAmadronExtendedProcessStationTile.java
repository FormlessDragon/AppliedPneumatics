package com.wintercogs.appliedpneumatics.common.tile;

import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import net.minecraft.item.ItemStack;

public class MEAmadronExtendedProcessStationTile extends MEAmadronProcessStationTile {
    public MEAmadronExtendedProcessStationTile() {
        super(27, APBlocks.ME_AMADRON_EXTENDED_PROCESS_STATION_ITEM);
    }

    @Override
    public ItemStack getItemFromTile() {
        return new ItemStack(APBlocks.ME_AMADRON_EXTENDED_PROCESS_STATION_ITEM);
    }
}
