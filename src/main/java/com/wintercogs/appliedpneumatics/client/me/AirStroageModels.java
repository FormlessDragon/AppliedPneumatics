package com.wintercogs.appliedpneumatics.client.me;

import appeng.api.client.StorageCellModels;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import com.wintercogs.appliedpneumatics.common.items.AirStorageCell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = AppliedPneumatics.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AirStroageModels
{
    public static final ResourceLocation AIR_CELL_MODEL_1K = ResourceLocation.tryBuild(AppliedPneumatics.MODID, "block/drive/cells/air_cell_1k");
    public static final ResourceLocation AIR_CELL_MODEL_4K = ResourceLocation.tryBuild(AppliedPneumatics.MODID, "block/drive/cells/air_cell_4k");
    public static final ResourceLocation AIR_CELL_MODEL_16K = ResourceLocation.tryBuild(AppliedPneumatics.MODID, "block/drive/cells/air_cell_16k");
    public static final ResourceLocation AIR_CELL_MODEL_64K = ResourceLocation.tryBuild(AppliedPneumatics.MODID, "block/drive/cells/air_cell_64k");
    public static final ResourceLocation AIR_CELL_MODEL_256K = ResourceLocation.tryBuild(AppliedPneumatics.MODID, "block/drive/cells/air_cell_256k");
    public static final ResourceLocation AIR_CELL_MODEL_1M = ResourceLocation.tryBuild(AppliedPneumatics.MODID, "block/drive/cells/air_cell_1m");
    public static final ResourceLocation AIR_CELL_MODEL_4M = ResourceLocation.tryBuild(AppliedPneumatics.MODID, "block/drive/cells/air_cell_4m");
    public static final ResourceLocation AIR_CELL_MODEL_16M = ResourceLocation.tryBuild(AppliedPneumatics.MODID, "block/drive/cells/air_cell_16m");
    public static final ResourceLocation AIR_CELL_MODEL_64M = ResourceLocation.tryBuild(AppliedPneumatics.MODID, "block/drive/cells/air_cell_64m");
    public static final ResourceLocation AIR_CELL_MODEL_256M = ResourceLocation.tryBuild(AppliedPneumatics.MODID, "block/drive/cells/air_cell_256m");

    public static void register()
    {
        StorageCellModels.registerModel(APItems.AIR_CELL_1K, AIR_CELL_MODEL_1K);
        StorageCellModels.registerModel(APItems.AIR_CELL_4K, AIR_CELL_MODEL_4K);
        StorageCellModels.registerModel(APItems.AIR_CELL_16K, AIR_CELL_MODEL_16K);
        StorageCellModels.registerModel(APItems.AIR_CELL_64K, AIR_CELL_MODEL_64K);
        StorageCellModels.registerModel(APItems.AIR_CELL_256K, AIR_CELL_MODEL_256K);
        StorageCellModels.registerModel(APItems.AIR_CELL_1M, AIR_CELL_MODEL_1M);
        StorageCellModels.registerModel(APItems.AIR_CELL_4M, AIR_CELL_MODEL_4M);
        StorageCellModels.registerModel(APItems.AIR_CELL_16M, AIR_CELL_MODEL_16M);
        StorageCellModels.registerModel(APItems.AIR_CELL_64M, AIR_CELL_MODEL_64M);
        StorageCellModels.registerModel(APItems.AIR_CELL_256M, AIR_CELL_MODEL_256M);
    }

    // 注册物品状态的led灯
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event)
    {
        event.register((itemStack, idx) -> FastColor.ARGB32.opaque(AirStorageCell.getColor(itemStack, idx)),
                APItems.AIR_CELL_1K,
                APItems.AIR_CELL_4K,
                APItems.AIR_CELL_16K,
                APItems.AIR_CELL_64K,
                APItems.AIR_CELL_256K,
                APItems.AIR_CELL_1M,
                APItems.AIR_CELL_4M,
                APItems.AIR_CELL_16M,
                APItems.AIR_CELL_64M,
                APItems.AIR_CELL_256M);
    }

}
