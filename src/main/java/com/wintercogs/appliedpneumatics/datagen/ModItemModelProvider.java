package com.wintercogs.appliedpneumatics.datagen;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.items.APItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider
{

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper)
    {
        super(output, AppliedPneumatics.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        basicItem(APItems.AIR_CELL_1K.get());
        basicItem(APItems.AIR_CELL_4K.get());
        basicItem(APItems.AIR_CELL_16K.get());
        basicItem(APItems.AIR_CELL_64K.get());
        basicItem(APItems.AIR_CELL_256K.get());
        basicItem(APItems.AIR_CELL_1M.get());
        basicItem(APItems.AIR_CELL_4M.get());
        basicItem(APItems.AIR_CELL_16M.get());
        basicItem(APItems.AIR_CELL_64M.get());
        basicItem(APItems.AIR_CELL_256M.get());
        basicItem(APItems.VOLUME_CRAD.get());
        basicItem(APItems.SECURITY_CRAD.get());
        basicItem(APItems.VACUUM_CRAD.get());
    }
}
