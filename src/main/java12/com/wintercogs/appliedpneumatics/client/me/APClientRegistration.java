package com.wintercogs.appliedpneumatics.client.me;

import ae2.api.client.AEKeyRendering;
import ae2.api.client.StorageCellModels;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.Reference;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import com.wintercogs.appliedpneumatics.common.item.AirStorageCell;
import com.wintercogs.appliedpneumatics.common.item.PortableAirStorageCell;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Side.CLIENT)
public final class APClientRegistration {
    private static boolean registered;

    private APClientRegistration() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        AEKeyRendering.register(AirKeyType.INSTANCE, AirKey.class, new AirKeyRenderHandler());
        registerCellModels();
        registered = true;
    }

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event) {
        register();
        event.getItemColors().registerItemColorHandler((IItemColor) AirStorageCell::getColor,
            airCellColorItems().toArray(new Item[0]));
        event.getItemColors().registerItemColorHandler((IItemColor) PortableAirStorageCell::getColor,
            portableAirCellColorItems().toArray(new Item[0]));
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (Item item : inventoryModelItems()) {
            ModelLoader.setCustomModelResourceLocation(item, 0, inventoryModelLocation(item));
        }
    }

    static Set<Item> airCellColorItems() {
        return APItems.getCells().stream()
            .filter(cell -> !(cell instanceof PortableAirStorageCell))
            .collect(Collectors.toUnmodifiableSet());
    }

    static Set<Item> portableAirCellColorItems() {
        return APItems.getPortableCells().stream()
            .map(cell -> (Item) cell)
            .collect(Collectors.toUnmodifiableSet());
    }

    static Set<Item> inventoryModelItems() {
        Set<Item> items = new HashSet<>(APItems.getAllItems());
        items.add(APBlocks.ME_PRESSURE_INTERFACE_ITEM);
        items.add(APBlocks.ME_TEMPERATURE_INTERFACE_ITEM);
        items.add(APBlocks.ME_AMADRON_PROCESS_STATION_ITEM);
        items.add(APBlocks.ME_AMADRON_EXTENDED_PROCESS_STATION_ITEM);
        return Set.copyOf(items);
    }

    static ModelResourceLocation inventoryModelLocation(Item item) {
        return new ModelResourceLocation(item.getRegistryName(), "inventory");
    }

    private static void registerCellModels() {
        for (AirStorageCell cell : APItems.getCells()) {
            StorageCellModels.registerModel(cell, driveCellModel(cell));
        }
    }

    private static ResourceLocation driveCellModel(AirStorageCell cell) {
        String modelPath = cell.getRegistryName().getPath().replace("portable_", "");
        return AppliedPneumatics.makeId("block/drive/cells/" + modelPath);
    }
}

