package com.wintercogs.appliedpneumatics.common.init;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.Reference;
import com.wintercogs.appliedpneumatics.common.APCreativeTab;
import com.wintercogs.appliedpneumatics.common.block.MEAmadronExtendedProcessStationBlock;
import com.wintercogs.appliedpneumatics.common.block.MEAmadronProcessStationBlock;
import com.wintercogs.appliedpneumatics.common.block.MEPressureInterfaceBlock;
import com.wintercogs.appliedpneumatics.common.block.METemperatureInterfaceBlock;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronExtendedProcessStationTile;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronProcessStationTile;
import com.wintercogs.appliedpneumatics.common.tile.MEPressureInterfaceTile;
import com.wintercogs.appliedpneumatics.common.tile.METemperatureInterfaceTile;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class APBlocks {
    private static final List<Block> ALL_BLOCKS = new ArrayList<>();

    public static final MEPressureInterfaceBlock ME_PRESSURE_INTERFACE_BLOCK =
        track(new MEPressureInterfaceBlock(), "me_pressure_interface_block");
    public static final ItemBlock ME_PRESSURE_INTERFACE_ITEM =
        (ItemBlock) new ItemBlock(ME_PRESSURE_INTERFACE_BLOCK)
            .setRegistryName(ME_PRESSURE_INTERFACE_BLOCK.getRegistryName())
            .setTranslationKey(ME_PRESSURE_INTERFACE_BLOCK.getTranslationKey())
            .setCreativeTab(APCreativeTab.INSTANCE);
    public static final METemperatureInterfaceBlock ME_TEMPERATURE_INTERFACE_BLOCK =
        track(new METemperatureInterfaceBlock(), "me_temperature_interface");
    public static final ItemBlock ME_TEMPERATURE_INTERFACE_ITEM =
        (ItemBlock) new ItemBlock(ME_TEMPERATURE_INTERFACE_BLOCK)
            .setRegistryName(ME_TEMPERATURE_INTERFACE_BLOCK.getRegistryName())
            .setTranslationKey(ME_TEMPERATURE_INTERFACE_BLOCK.getTranslationKey())
            .setCreativeTab(APCreativeTab.INSTANCE);
    public static final MEAmadronProcessStationBlock ME_AMADRON_PROCESS_STATION_BLOCK =
        track(new MEAmadronProcessStationBlock(), "me_amadron_process_station");
    public static final ItemBlock ME_AMADRON_PROCESS_STATION_ITEM =
        (ItemBlock) new ItemBlock(ME_AMADRON_PROCESS_STATION_BLOCK)
            .setRegistryName(ME_AMADRON_PROCESS_STATION_BLOCK.getRegistryName())
            .setTranslationKey(ME_AMADRON_PROCESS_STATION_BLOCK.getTranslationKey())
            .setCreativeTab(APCreativeTab.INSTANCE);
    public static final MEAmadronExtendedProcessStationBlock ME_AMADRON_EXTENDED_PROCESS_STATION_BLOCK =
        track(new MEAmadronExtendedProcessStationBlock(), "me_amadron_extended_process_station");
    public static final ItemBlock ME_AMADRON_EXTENDED_PROCESS_STATION_ITEM =
        (ItemBlock) new ItemBlock(ME_AMADRON_EXTENDED_PROCESS_STATION_BLOCK)
            .setRegistryName(ME_AMADRON_EXTENDED_PROCESS_STATION_BLOCK.getRegistryName())
            .setTranslationKey(ME_AMADRON_EXTENDED_PROCESS_STATION_BLOCK.getTranslationKey())
            .setCreativeTab(APCreativeTab.INSTANCE);

    private APBlocks() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(ME_PRESSURE_INTERFACE_BLOCK);
        event.getRegistry().register(ME_TEMPERATURE_INTERFACE_BLOCK);
        event.getRegistry().register(ME_AMADRON_PROCESS_STATION_BLOCK);
        event.getRegistry().register(ME_AMADRON_EXTENDED_PROCESS_STATION_BLOCK);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ME_PRESSURE_INTERFACE_ITEM);
        event.getRegistry().register(ME_TEMPERATURE_INTERFACE_ITEM);
        event.getRegistry().register(ME_AMADRON_PROCESS_STATION_ITEM);
        event.getRegistry().register(ME_AMADRON_EXTENDED_PROCESS_STATION_ITEM);
    }

    public static void preInit() {
        GameRegistry.registerTileEntity(MEPressureInterfaceTile.class,
            AppliedPneumatics.makeId("me_pressure_interface_block"));
        GameRegistry.registerTileEntity(METemperatureInterfaceTile.class,
            AppliedPneumatics.makeId("me_temperature_interface"));
        GameRegistry.registerTileEntity(MEAmadronProcessStationTile.class,
            AppliedPneumatics.makeId("me_amadron_process_station"));
        GameRegistry.registerTileEntity(MEAmadronExtendedProcessStationTile.class,
            AppliedPneumatics.makeId("me_amadron_extended_process_station"));
    }

    public static List<Block> getAllBlocks() {
        return Collections.unmodifiableList(ALL_BLOCKS);
    }

    private static <T extends Block> T track(T block, String id) {
        ResourceLocation registryName = AppliedPneumatics.makeId(id);
        block.setRegistryName(registryName);
        block.setTranslationKey(registryName.getNamespace() + "." + registryName.getPath());
        block.setCreativeTab(APCreativeTab.INSTANCE);
        ALL_BLOCKS.add(block);
        return block;
    }
}
