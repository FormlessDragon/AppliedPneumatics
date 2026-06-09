package com.wintercogs.appliedpneumatics.common.item;

import ae2.api.inventories.InternalInventory;
import ae2.helpers.externalstorage.GenericStackInv;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronExtendedProcessStationTile;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronProcessStationTile;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class AmadronProcessUpgradeItem extends Item {
    public enum StationUpgradeResult {
        SUCCESS,
        BUSY,
        FAILED
    }

    public interface StationUpgradeOperation {
        MEAmadronExtendedProcessStationTile replace();

        default MEAmadronProcessStationTile restore() {
            return null;
        }
    }

    public boolean canUpgrade(Block block) {
        return getUpgradeTarget(block) != null;
    }

    public Block getUpgradeTarget(Block block) {
        if (block == APBlocks.ME_AMADRON_PROCESS_STATION_BLOCK) {
            return APBlocks.ME_AMADRON_EXTENDED_PROCESS_STATION_BLOCK;
        }
        return null;
    }

    public boolean canUpgradeStation(MEAmadronProcessStationTile station) {
        return station != null
            && station.getJobAmount() == 0
            && station.getInputInventory().isEmpty()
            && station.getOutputInventory().isEmpty();
    }

    public StationUpgradeResult upgradeStation(MEAmadronProcessStationTile source,
                                               BooleanSupplier replaceBlock,
                                               Supplier<MEAmadronExtendedProcessStationTile> targetSupplier) {
        return upgradeStation(source, new StationUpgradeOperation() {
            @Override
            public MEAmadronExtendedProcessStationTile replace() {
                if (replaceBlock == null || !replaceBlock.getAsBoolean()) {
                    return null;
                }
                return targetSupplier == null ? null : targetSupplier.get();
            }
        });
    }

    public StationUpgradeResult upgradeStation(MEAmadronProcessStationTile source,
                                               StationUpgradeOperation operation) {
        if (!canUpgradeStation(source)) {
            return StationUpgradeResult.BUSY;
        }

        NBTTagCompound snapshot = new NBTTagCompound();
        source.saveAdditional(snapshot);
        source.clearContent();

        MEAmadronExtendedProcessStationTile target = operation == null ? null : operation.replace();
        if (target == null) {
            MEAmadronProcessStationTile restored = operation == null ? null : operation.restore();
            (restored == null ? source : restored).loadTag(snapshot);
            return StationUpgradeResult.FAILED;
        }

        target.loadTag(snapshot);
        return StationUpgradeResult.SUCCESS;
    }

    public void copyStationData(MEAmadronProcessStationTile source, MEAmadronExtendedProcessStationTile target) {
        copyInventory(source.getPatternInventory(), target.getPatternInventory());
        copyInventory(source.getUpgrades(), target.getUpgrades());
        copyInventory(source.getInputInventory(), target.getInputInventory());
        copyInventory(source.getOutputInventory(), target.getOutputInventory());
        target.setPriority(source.getPriority());
    }

    private static void copyInventory(InternalInventory source, InternalInventory target) {
        int slots = Math.min(source.size(), target.size());
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = source.getStackInSlot(slot);
            target.setItemDirect(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
    }

    private static void copyInventory(GenericStackInv source, GenericStackInv target) {
        int slots = Math.min(source.size(), target.size());
        for (int slot = 0; slot < slots; slot++) {
            target.setStack(slot, source.getStack(slot));
        }
    }
}

