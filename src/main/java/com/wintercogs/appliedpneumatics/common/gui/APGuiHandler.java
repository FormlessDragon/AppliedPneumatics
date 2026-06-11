package com.wintercogs.appliedpneumatics.common.gui;

import ae2.container.AEBaseContainer;
import ae2.core.gui.locator.GuiHostLocator;
import ae2.core.gui.locator.GuiHostLocators;
import ae2.core.gui.locator.ItemGuiHostLocator;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronProcessStationTile;
import com.wintercogs.appliedpneumatics.common.tile.MEPressureInterfaceTile;
import com.wintercogs.appliedpneumatics.common.tile.METemperatureInterfaceTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.jetbrains.annotations.Nullable;

public class APGuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == APGuiIds.AMADRON_WIRELESS_TERMINAL) {
            ItemGuiHostLocator locator = createAmadronWirelessTerminalLocator(x, y, z);
            AmadronWirelessTerminalGuiHost host = locator.locate(player, AmadronWirelessTerminalGuiHost.class);
            if (host != null) {
                return initContainer(new ContainerAmadronWirelessTerminal(player.inventory, host), locator);
            }
            return null;
        }

        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));

        if (ID == APGuiIds.ME_PRESSURE_INTERFACE && tile instanceof MEPressureInterfaceTile) {
            return initTileContainer(new ContainerMEPressureInterface(player.inventory,
                (MEPressureInterfaceTile) tile), tile);
        }
        if (ID == APGuiIds.ME_TEMPERATURE_INTERFACE && tile instanceof METemperatureInterfaceTile) {
            return initTileContainer(new ContainerMETemperatureInterface(player.inventory,
                (METemperatureInterfaceTile) tile), tile);
        }
        if (ID == APGuiIds.ME_AMADRON_PROCESS_STATION && tile instanceof MEAmadronProcessStationTile) {
            return initTileContainer(new ContainerMEAmadronProcessStation(player.inventory,
                (MEAmadronProcessStationTile) tile), tile);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return AppliedPneumatics.proxy().createClientGui(ID, player, world, x, y, z);
    }

    private static <C extends AEBaseContainer> C initTileContainer(C container, TileEntity tile) {
        container.setLocator(GuiHostLocators.forTile(tile));
        return container;
    }

    private static <C extends AEBaseContainer> C initContainer(C container, GuiHostLocator locator) {
        container.setLocator(locator);
        return container;
    }

    private static ItemGuiHostLocator createAmadronWirelessTerminalLocator(int x, int y, int z) {
        if (x < 0 && y == 0 && z == 0) {
            return GuiHostLocators.forBaubleSlot(-x - 1);
        }
        return GuiHostLocators.forInventorySlot(x);
    }
}

