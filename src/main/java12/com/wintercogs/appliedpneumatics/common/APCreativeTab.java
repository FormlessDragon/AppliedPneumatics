package com.wintercogs.appliedpneumatics.common;

import com.wintercogs.appliedpneumatics.Reference;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public final class APCreativeTab extends CreativeTabs {
    public static final APCreativeTab INSTANCE = new APCreativeTab();

    private APCreativeTab() {
        super(Reference.MOD_ID);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(APItems.AIR_CELL_1K);
    }
}
