package com.wintercogs.appliedpneumatics.client.gui;

import ae2.client.gui.style.GuiStyleManager;
import ae2.core.gui.locator.GuiHostLocators;
import ae2.core.gui.locator.ItemGuiHostLocator;
import com.wintercogs.appliedpneumatics.common.gui.APGuiIds;
import com.wintercogs.appliedpneumatics.common.gui.AmadronWirelessTerminalGuiHost;
import com.wintercogs.appliedpneumatics.common.gui.ContainerAmadronWirelessTerminal;
import com.wintercogs.appliedpneumatics.common.gui.ContainerMEAmadronProcessStation;
import com.wintercogs.appliedpneumatics.common.gui.ContainerMEPressureInterface;
import com.wintercogs.appliedpneumatics.common.gui.ContainerMETemperatureInterface;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronExtendedProcessStationTile;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronProcessStationTile;
import com.wintercogs.appliedpneumatics.common.tile.MEPressureInterfaceTile;
import com.wintercogs.appliedpneumatics.common.tile.METemperatureInterfaceTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public final class APClientGuiFactory {
    private APClientGuiFactory() {
    }

    @Nullable
    public static Object create(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == APGuiIds.AMADRON_WIRELESS_TERMINAL) {
            ItemGuiHostLocator locator = createAmadronWirelessTerminalLocator(x, y, z);
            AmadronWirelessTerminalGuiHost host = locator.locate(player, AmadronWirelessTerminalGuiHost.class);
            if (host != null) {
                ContainerAmadronWirelessTerminal container = new ContainerAmadronWirelessTerminal(player.inventory,
                    host);
                container.setLocator(locator);
                return new GuiAmadronWirelessTerminal(container, player.inventory, null,
                    GuiStyleManager.loadStyleDoc("/screens/amadron_wireless_terminal.json"));
            }
            return null;
        }

        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));

        if (id == APGuiIds.ME_PRESSURE_INTERFACE && tile instanceof MEPressureInterfaceTile) {
            ContainerMEPressureInterface container = new ContainerMEPressureInterface(player.inventory,
                (MEPressureInterfaceTile) tile);
            return new GuiMEPressureInterface(container, player.inventory, null,
                GuiStyleManager.loadStyleDoc("/screens/appliedpneumatics_pressure_interface.json"));
        }
        if (id == APGuiIds.ME_TEMPERATURE_INTERFACE && tile instanceof METemperatureInterfaceTile) {
            ContainerMETemperatureInterface container = new ContainerMETemperatureInterface(player.inventory,
                (METemperatureInterfaceTile) tile);
            return new GuiMETemperatureInterface(container, player.inventory, null,
                GuiStyleManager.loadStyleDoc("/screens/appliedpneumatics_temperature_interface.json"));
        }
        if (id == APGuiIds.ME_AMADRON_PROCESS_STATION && tile instanceof MEAmadronProcessStationTile) {
            ContainerMEAmadronProcessStation container = new ContainerMEAmadronProcessStation(player.inventory,
                (MEAmadronProcessStationTile) tile);
            String stylePath = tile instanceof MEAmadronExtendedProcessStationTile
                ? "/screens/appliedpneumatics_amadron_extended_process_station.json"
                : "/screens/appliedpneumatics_amadron_process_station.json";
            return new GuiMEAmadronProcessStation(container, player.inventory, null,
                GuiStyleManager.loadStyleDoc(stylePath));
        }
        return null;
    }

    private static ItemGuiHostLocator createAmadronWirelessTerminalLocator(int x, int y, int z) {
        if (x < 0 && y == 0 && z == 0) {
            return GuiHostLocators.forBaubleSlot(-x - 1);
        }
        return GuiHostLocators.forInventorySlot(x);
    }
}

