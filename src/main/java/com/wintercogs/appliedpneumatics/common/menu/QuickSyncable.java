package com.wintercogs.appliedpneumatics.common.menu;

import net.minecraft.nbt.CompoundTag;

public interface QuickSyncable
{
    CompoundTag getQuickSyncTag();

    void loadQuickSync(CompoundTag tag);
}
