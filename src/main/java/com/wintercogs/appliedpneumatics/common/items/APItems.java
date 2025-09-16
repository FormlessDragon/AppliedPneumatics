package com.wintercogs.appliedpneumatics.common.items;

import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.items.storage.BasicStorageCell;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import com.wintercogs.appliedpneumatics.common.me.p2p.AirP2PTunnelPart;
import com.wintercogs.appliedpneumatics.common.me.p2p.HeatP2PTunnelPart;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class APItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AppliedPneumatics.MODID);

    // 气体元件 1K ~ 256M
    public static final DeferredItem<BasicStorageCell> AIR_CELL_1K = ITEMS.register("air_cell_1k",
            () -> new BasicStorageCell(new Item.Properties().stacksTo(1),
                    0.5f, // 待机能耗
                    1, // 总字节数量（单位：千）
                    8, // 每个种类占用多少字节（其实对气体来说没什么意义，但我还是要加）
                    1, // 最多能容纳的种类数
                    AirKeyType.INSTANCE));

    public static final DeferredItem<BasicStorageCell> AIR_CELL_4K = ITEMS.register("air_cell_4k",
            () -> new BasicStorageCell(new Item.Properties().stacksTo(1),
                    1.0f,
                    4,
                    32,
                    1,
                    AirKeyType.INSTANCE));

    public static final DeferredItem<BasicStorageCell> AIR_CELL_16K = ITEMS.register("air_cell_16k",
            () -> new BasicStorageCell(new Item.Properties().stacksTo(1),
                    1.5f,
                    16,
                    128,
                    1,
                    AirKeyType.INSTANCE));

    public static final DeferredItem<BasicStorageCell> AIR_CELL_64K = ITEMS.register("air_cell_64k",
            () -> new BasicStorageCell(new Item.Properties().stacksTo(1),
                    2.0f,
                    64,
                    512,
                    1,
                    AirKeyType.INSTANCE));

    public static final DeferredItem<BasicStorageCell> AIR_CELL_256K = ITEMS.register("air_cell_256k",
            () -> new BasicStorageCell(new Item.Properties().stacksTo(1),
                    2.5f,
                    256,
                    2048,
                    1,
                    AirKeyType.INSTANCE));

    public static final DeferredItem<BasicStorageCell> AIR_CELL_1M = ITEMS.register("air_cell_1m",
            () -> new BasicStorageCell(new Item.Properties().stacksTo(1),
                    3.0f,
                    1024,
                    8192,
                    1,
                    AirKeyType.INSTANCE));

    public static final DeferredItem<BasicStorageCell> AIR_CELL_4M = ITEMS.register("air_cell_4m",
            () -> new BasicStorageCell(new Item.Properties().stacksTo(1),
                    3.5f,
                    4096,
                    32768,
                    1,
                    AirKeyType.INSTANCE));

    public static final DeferredItem<BasicStorageCell> AIR_CELL_16M = ITEMS.register("air_cell_16m",
            () -> new BasicStorageCell(new Item.Properties().stacksTo(1),
                    4.0f,
                    16384,
                    131072,
                    1,
                    AirKeyType.INSTANCE));

    public static final DeferredItem<BasicStorageCell> AIR_CELL_64M = ITEMS.register("air_cell_64m",
            () -> new BasicStorageCell(new Item.Properties().stacksTo(1),
                    4.5f,
                    65536,
                    524288,
                    1,
                    AirKeyType.INSTANCE));

    public static final DeferredItem<BasicStorageCell> AIR_CELL_256M = ITEMS.register("air_cell_256m",
            () -> new BasicStorageCell(new Item.Properties().stacksTo(1),
                    5.0f,
                    262144,
                    2097152,
                    1,
                    AirKeyType.INSTANCE));

    // 空气p2p通道
    public static final DeferredItem<PartItem<AirP2PTunnelPart>> AIR_P2P_TUNEL = ITEMS.register("air_p2p_tunel",
            () -> {
                // 注册模型
                PartModels.registerModels(PartModelsHelper.createModels(AirP2PTunnelPart.class));
                return new PartItem<>(new Item.Properties(), AirP2PTunnelPart.class, AirP2PTunnelPart::new);
            });

    // 热量p2p通道
    public static final DeferredItem<PartItem<HeatP2PTunnelPart>> HEAT_P2P_TUNEL = ITEMS.register("heat_p2p_tunel",
            () -> {
                // 注册模型
                PartModels.registerModels(PartModelsHelper.createModels(HeatP2PTunnelPart.class));
                return new PartItem<>(new Item.Properties(), HeatP2PTunnelPart.class, HeatP2PTunnelPart::new);
            });


    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
