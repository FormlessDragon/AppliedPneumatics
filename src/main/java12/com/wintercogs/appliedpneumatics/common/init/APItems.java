package com.wintercogs.appliedpneumatics.common.init;

import ae2.api.parts.IPartItem;
import ae2.api.upgrades.Upgrades;
import ae2.api.parts.IPart;
import ae2.items.parts.PartItem;
import ae2.items.parts.PartModelsHelper;
import ae2.api.parts.PartModels;
import com.wintercogs.appliedpneumatics.common.APCreativeTab;
import com.wintercogs.appliedpneumatics.common.item.AmadronProcessUpgradeItem;
import com.wintercogs.appliedpneumatics.common.item.AmadronWirelessTerminalItem;
import com.wintercogs.appliedpneumatics.common.item.AirStorageCell;
import com.wintercogs.appliedpneumatics.common.item.PortableAirStorageCell;
import com.wintercogs.appliedpneumatics.common.me.p2p.AirP2PTunnelPart;
import com.wintercogs.appliedpneumatics.common.me.p2p.HeatP2PTunnelPart;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = "appliedpneumatics")
public final class APItems {
    private static final List<AirStorageCell> CELLS = new ArrayList<>();
    private static final List<PortableAirStorageCell> PORTABLE_CELLS = new ArrayList<>();
    private static final List<Item> ALL_ITEMS = new ArrayList<>();
    private static boolean initialized;
    private static boolean speedCardUpgradesRegistered;
    private static boolean energyCardUpgradesRegistered;

    public static final Item AIR_CELL_SHELL = simple("air_cell_shell");
    public static final Item MEGA_AIR_CELL_SHELL = simple("mega_air_cell_shell");

    public static final AirStorageCell AIR_CELL_1K = cell("air_cell_1k", 0.5D, 1);
    public static final AirStorageCell AIR_CELL_4K = cell("air_cell_4k", 1.0D, 4);
    public static final AirStorageCell AIR_CELL_16K = cell("air_cell_16k", 1.5D, 16);
    public static final AirStorageCell AIR_CELL_64K = cell("air_cell_64k", 2.0D, 64);
    public static final AirStorageCell AIR_CELL_256K = cell("air_cell_256k", 2.5D, 256);
    public static final AirStorageCell AIR_CELL_1M = cell("air_cell_1m", 3.0D, 1024);
    public static final AirStorageCell AIR_CELL_4M = cell("air_cell_4m", 3.5D, 4096);
    public static final AirStorageCell AIR_CELL_16M = cell("air_cell_16m", 4.0D, 16384);
    public static final AirStorageCell AIR_CELL_64M = cell("air_cell_64m", 4.5D, 65536);
    public static final AirStorageCell AIR_CELL_256M = cell("air_cell_256m", 5.0D, 262144);

    public static final PortableAirStorageCell PORTABLE_AIR_CELL_1K = portableCell("portable_air_cell_1k", 0.5D, 1);
    public static final PortableAirStorageCell PORTABLE_AIR_CELL_4K = portableCell("portable_air_cell_4k", 1.0D, 4);
    public static final PortableAirStorageCell PORTABLE_AIR_CELL_16K = portableCell("portable_air_cell_16k", 1.5D, 16);
    public static final PortableAirStorageCell PORTABLE_AIR_CELL_64K = portableCell("portable_air_cell_64k", 2.0D, 64);
    public static final PortableAirStorageCell PORTABLE_AIR_CELL_256K = portableCell("portable_air_cell_256k", 2.5D, 256);
    public static final PortableAirStorageCell PORTABLE_AIR_CELL_1M = portableCell("portable_air_cell_1m", 3.0D, 1024);
    public static final PortableAirStorageCell PORTABLE_AIR_CELL_4M = portableCell("portable_air_cell_4m", 3.5D, 4096);
    public static final PortableAirStorageCell PORTABLE_AIR_CELL_16M = portableCell("portable_air_cell_16m", 4.0D, 16384);
    public static final PortableAirStorageCell PORTABLE_AIR_CELL_64M = portableCell("portable_air_cell_64m", 4.5D, 65536);
    public static final PortableAirStorageCell PORTABLE_AIR_CELL_256M = portableCell("portable_air_cell_256m", 5.0D, 262144);

    public static final Item VOLUME_CARD = upgrade("volume_card");
    public static final Item SECURITY_CARD = upgrade("security_card");
    public static final Item VACUUM_CARD = upgrade("vacuum_card");
    public static final Item CHARGING_CARD = upgrade("charging_card");

    public static final PartItem<AirP2PTunnelPart> AIR_P2P_TUNEL =
        part("air_p2p_tunel", AirP2PTunnelPart.class, AirP2PTunnelPart::new);
    public static final PartItem<HeatP2PTunnelPart> HEAT_P2P_TUNEL =
        part("heat_p2p_tunel", HeatP2PTunnelPart.class, HeatP2PTunnelPart::new);
    public static final AmadronWirelessTerminalItem AMADRON_WIRELESS_TERMINAL =
        track(new AmadronWirelessTerminalItem(), "amadron_wireless_terminal");
    public static final Item AMADRON_PATTERN = simple("amadron_pattern");
    public static final AmadronProcessUpgradeItem AMADRON_PROCESS_UPGRADE =
        track(new AmadronProcessUpgradeItem(), "amadron_process_upgrade");

    private APItems() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (Item item : ALL_ITEMS) {
            event.getRegistry().register(item);
        }
    }

    public static void init() {
        registerOwnUpgrades();
        registerExternalAe2Upgrades();
    }

    private static void registerOwnUpgrades() {
        if (initialized) {
            return;
        }
        initialized = true;
        for (AirStorageCell cell : CELLS) {
            Upgrades.add(SECURITY_CARD, cell, 1, "appliedpneumatics.group.cell_upgrade");
            Upgrades.add(VACUUM_CARD, cell, 1, "appliedpneumatics.group.cell_upgrade");
        }
        for (PortableAirStorageCell cell : PORTABLE_CELLS) {
            Upgrades.add(CHARGING_CARD, cell, 1, "appliedpneumatics.group.portable_cell_upgrade");
        }
        Upgrades.add(VOLUME_CARD, APBlocks.ME_PRESSURE_INTERFACE_ITEM, 4, "appliedpneumatics.group.pressure_interface_upgrade");
        Upgrades.add(SECURITY_CARD, APBlocks.ME_PRESSURE_INTERFACE_ITEM, 1, "appliedpneumatics.group.pressure_interface_upgrade");
        Upgrades.add(VACUUM_CARD, APBlocks.ME_PRESSURE_INTERFACE_ITEM, 1, "appliedpneumatics.group.pressure_interface_upgrade");
        Upgrades.add(VOLUME_CARD, APBlocks.ME_TEMPERATURE_INTERFACE_ITEM, 4, "appliedpneumatics.group.temperature_interface_upgrade");
    }

    private static void registerExternalAe2Upgrades() {
        Item speedCard = getAe2SpeedCard();
        if (!speedCardUpgradesRegistered && speedCard != null) {
            Upgrades.add(speedCard, APBlocks.ME_TEMPERATURE_INTERFACE_ITEM, 4,
                "appliedpneumatics.group.temperature_interface_upgrade");
            Upgrades.add(speedCard, APBlocks.ME_AMADRON_PROCESS_STATION_ITEM, 4,
                "appliedpneumatics.group.amadron_process_station_upgrade");
            Upgrades.add(speedCard, APBlocks.ME_AMADRON_EXTENDED_PROCESS_STATION_ITEM, 4,
                "appliedpneumatics.group.amadron_process_station_upgrade");
            speedCardUpgradesRegistered = true;
        }
        Item energyCard = getAe2EnergyCard();
        if (!energyCardUpgradesRegistered && energyCard != null) {
            Upgrades.add(energyCard, AMADRON_WIRELESS_TERMINAL, 2);
            energyCardUpgradesRegistered = true;
        }
    }

    public static List<AirStorageCell> getCells() {
        return Collections.unmodifiableList(CELLS);
    }

    public static List<PortableAirStorageCell> getPortableCells() {
        return Collections.unmodifiableList(PORTABLE_CELLS);
    }

    public static List<Item> getAllItems() {
        return Collections.unmodifiableList(ALL_ITEMS);
    }

    private static Item simple(String id) {
        return track(new Item(), id);
    }

    private static Item upgrade(String id) {
        return track(Upgrades.createUpgradeCardItem(), id);
    }

    private static <T extends IPart> PartItem<T> part(String id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return track(new PartItem<>(partClass, factory), id);
    }

    private static AirStorageCell cell(String id, double idleDrain, int kilobytes) {
        AirStorageCell cell = track(new AirStorageCell(idleDrain, kilobytes), id);
        CELLS.add(cell);
        return cell;
    }

    private static PortableAirStorageCell portableCell(String id, double idleDrain, int kilobytes) {
        PortableAirStorageCell cell = track(new PortableAirStorageCell(idleDrain, kilobytes), id);
        CELLS.add(cell);
        PORTABLE_CELLS.add(cell);
        return cell;
    }

    private static <T extends Item> T track(T item, String id) {
        ResourceLocation registryName = AppliedPneumatics.makeId(id);
        item.setRegistryName(registryName);
        item.setTranslationKey(registryName.getNamespace() + "." + registryName.getPath());
        item.setCreativeTab(APCreativeTab.INSTANCE);
        ALL_ITEMS.add(item);
        return item;
    }

    public static Item getAe2SpeedCard() {
        Item item = Item.REGISTRY.getObject(new ResourceLocation("ae2", "speed_card"));
        return item == null || item == Items.AIR ? null : item;
    }

    public static Item getAe2EnergyCard() {
        Item item = Item.REGISTRY.getObject(new ResourceLocation("ae2", "energy_card"));
        return item == null || item == Items.AIR ? null : item;
    }
}

