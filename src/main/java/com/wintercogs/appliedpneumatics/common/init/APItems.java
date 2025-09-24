package com.wintercogs.appliedpneumatics.common.init;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.parts.PartModels;
import appeng.api.upgrades.Upgrades;
import appeng.core.AEConfig;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.items.AirStorageCell;
import com.wintercogs.appliedpneumatics.common.items.AmadronWirelessTerminalItem;
import com.wintercogs.appliedpneumatics.common.me.crafting.AmadronPatternDetails;
import com.wintercogs.appliedpneumatics.common.me.p2p.AirP2PTunnelPart;
import com.wintercogs.appliedpneumatics.common.me.p2p.HeatP2PTunnelPart;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class APItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AppliedPneumatics.MODID);

    // 存储外壳-普通
    public static final DeferredItem<Item> AIR_CELL_SHELL = ITEMS.register("air_cell_shell",
            () -> new Item(new Item.Properties()));

    // 存储外壳-MEGA
    public static final DeferredItem<Item> MEGA_AIR_CELL_SHELL = ITEMS.register("mega_air_cell_shell",
            () -> new Item(new Item.Properties()));

    // 气体元件 1K ~ 256M
    public static final DeferredItem<AirStorageCell> AIR_CELL_1K = ITEMS.register("air_cell_1k",
            () -> new AirStorageCell(new Item.Properties().stacksTo(1),
                    0.5f, // 待机能耗
                    1 // 总字节数量（单位：千）
                    ));

    public static final DeferredItem<AirStorageCell> AIR_CELL_4K = ITEMS.register("air_cell_4k",
            () -> new AirStorageCell(new Item.Properties().stacksTo(1),
                    1.0f,
                    4));

    public static final DeferredItem<AirStorageCell> AIR_CELL_16K = ITEMS.register("air_cell_16k",
            () -> new AirStorageCell(new Item.Properties().stacksTo(1),
                    1.5f,
                    16));

    public static final DeferredItem<AirStorageCell> AIR_CELL_64K = ITEMS.register("air_cell_64k",
            () -> new AirStorageCell(new Item.Properties().stacksTo(1),
                    2.0f,
                    64));

    public static final DeferredItem<AirStorageCell> AIR_CELL_256K = ITEMS.register("air_cell_256k",
            () -> new AirStorageCell(new Item.Properties().stacksTo(1),
                    2.5f,
                    256));

    public static final DeferredItem<AirStorageCell> AIR_CELL_1M = ITEMS.register("air_cell_1m",
            () -> new AirStorageCell(new Item.Properties().stacksTo(1),
                    3.0f,
                    1024));

    public static final DeferredItem<AirStorageCell> AIR_CELL_4M = ITEMS.register("air_cell_4m",
            () -> new AirStorageCell(new Item.Properties().stacksTo(1),
                    3.5f,
                    4096));

    public static final DeferredItem<AirStorageCell> AIR_CELL_16M = ITEMS.register("air_cell_16m",
            () -> new AirStorageCell(new Item.Properties().stacksTo(1),
                    4.0f,
                    16384));

    public static final DeferredItem<AirStorageCell> AIR_CELL_64M = ITEMS.register("air_cell_64m",
            () -> new AirStorageCell(new Item.Properties().stacksTo(1),
                    4.5f,
                    65536));

    public static final DeferredItem<AirStorageCell> AIR_CELL_256M = ITEMS.register("air_cell_256m",
            () -> new AirStorageCell(new Item.Properties().stacksTo(1),
                    5.0f,
                    262144));

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

    // 容量卡 (实际类型为UpgradeCardItem)
    public static final DeferredItem<Item> VOLUME_CARD = ITEMS.register("volume_card",
            () -> Upgrades.createUpgradeCardItem(new Item.Properties()));

    // 安全卡
    public static final DeferredItem<Item> SECURITY_CARD = ITEMS.register("security_card",
            () -> Upgrades.createUpgradeCardItem(new Item.Properties()));

    // 真空卡
    public static final DeferredItem<Item> VACUUM_CARD = ITEMS.register("vacuum_card",
            () -> Upgrades.createUpgradeCardItem(new Item.Properties()));

    // 亚马龙无线终端
    public static final DeferredItem<AmadronWirelessTerminalItem> AMADRON_WIRELESS_TERMINAL = ITEMS.register("amadron_wireless_terminal",
            () -> new AmadronWirelessTerminalItem(AEConfig.instance().getWirelessTerminalBattery(),new Item.Properties().stacksTo(1)));

    // 亚马龙样板
    public static final DeferredItem<Item> AMADRON_PATTERN = ITEMS.register("amadron_pattern",
            () -> PatternDetailsHelper.encodedPatternItemBuilder(AmadronPatternDetails::new).invalidPatternTooltip(AmadronPatternDetails::getInvalidPatternTooltip).build());

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
