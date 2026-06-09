package com.wintercogs.appliedpneumatics.common.gui;

import ae2.api.inventories.InternalInventory;
import ae2.container.ISubGui;
import ae2.core.gui.locator.ItemGuiHostLocator;
import ae2.helpers.WirelessTerminalGuiHost;
import ae2.items.contents.StackDependentSupplier;
import ae2.items.tools.powered.WirelessTerminalItem;
import ae2.items.tools.powered.WirelessTerminals;
import ae2.util.inv.AppEngInternalInventory;
import ae2.util.inv.InternalInventoryHost;
import ae2.util.inv.SupplierInternalInventory;
import com.wintercogs.appliedpneumatics.common.item.AmadronWirelessTerminalItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.function.BiConsumer;

public class AmadronWirelessTerminalGuiHost extends WirelessTerminalGuiHost<WirelessTerminalItem> {
    private static final String PATTERN_INVENTORY_TAG = "amadron_pattern_inventory";

    private final SupplierInternalInventory<InternalInventory> patternInventory;

    public AmadronWirelessTerminalGuiHost(WirelessTerminalItem stackItem, WirelessTerminalItem terminalItem,
                                          EntityPlayer player, ItemGuiHostLocator locator,
                                          BiConsumer<EntityPlayer, ISubGui> returnToMainContainer) {
        super(stackItem, terminalItem, player, locator, returnToMainContainer);
        this.patternInventory = new SupplierInternalInventory<>(
            new StackDependentSupplier<>(this::getItemStack,
                stack -> createPatternInventory(player, stack, terminalItem)));
    }

    public InternalInventory getPatternInventory() {
        return this.patternInventory;
    }

    private static InternalInventory createPatternInventory(EntityPlayer player, ItemStack stack,
                                                           WirelessTerminalItem terminalItem) {
        AppEngInternalInventory inventory = new AppEngInternalInventory(new InternalInventoryHost() {
            @Override
            public void saveChangedInventory(AppEngInternalInventory inv) {
                inv.writeToNBT(WirelessTerminals.getTerminalData(stack, terminalItem), PATTERN_INVENTORY_TAG);
            }

            @Override
            public boolean isClientSide() {
                return player.world.isRemote;
            }
        }, 2);

        var tag = WirelessTerminals.getExistingTerminalData(stack, terminalItem);
        if (tag != null) {
            inventory.readFromNBT(tag, PATTERN_INVENTORY_TAG);
        }
        return inventory;
    }

    public AmadronWirelessTerminalItem getAmadronTerminalItem() {
        return (AmadronWirelessTerminalItem) getTerminalItem();
    }
}

