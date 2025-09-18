package com.wintercogs.appliedpneumatics.datagen;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
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
        airCellWithOwnBaseAndAeLed(APItems.AIR_CELL_1K.get());
        airCellWithOwnBaseAndAeLed(APItems.AIR_CELL_4K.get());
        airCellWithOwnBaseAndAeLed(APItems.AIR_CELL_16K.get());
        airCellWithOwnBaseAndAeLed(APItems.AIR_CELL_64K.get());
        airCellWithOwnBaseAndAeLed(APItems.AIR_CELL_256K.get());
        airCellWithOwnBaseAndAeLed(APItems.AIR_CELL_1M.get());
        airCellWithOwnBaseAndAeLed(APItems.AIR_CELL_4M.get());
        airCellWithOwnBaseAndAeLed(APItems.AIR_CELL_16M.get());
        airCellWithOwnBaseAndAeLed(APItems.AIR_CELL_64M.get());
        airCellWithOwnBaseAndAeLed(APItems.AIR_CELL_256M.get());
        basicItem(APItems.VOLUME_CARD.get());
        basicItem(APItems.SECURITY_CARD.get());
        basicItem(APItems.VACUUM_CARD.get());
    }

    // 快速注册带led灯状态的存储元件模型
    public ItemModelBuilder airCellWithOwnBaseAndAeLed(Item item)
    {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        var base = modLoc("item/" + id.getPath());

        return withExistingParent(id.getPath(), mcLoc("item/generated"))
                .texture("layer0", base)
                .texture("layer1", ResourceLocation.fromNamespaceAndPath(AppliedPneumatics.MODID, "item/storage_cell_led"));
    }

}
