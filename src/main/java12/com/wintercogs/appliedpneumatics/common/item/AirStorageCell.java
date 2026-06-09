package com.wintercogs.appliedpneumatics.common.item;

import ae2.api.config.FuzzyMode;
import ae2.api.stacks.AEKeyType;
import ae2.api.storage.StorageCells;
import ae2.api.storage.cells.CellState;
import ae2.api.storage.cells.ICellWorkbenchItem;
import ae2.api.storage.cells.IStackTooltipDataProvider;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.upgrades.UpgradeInventories;
import ae2.items.AEBaseItem;
import ae2.items.storage.StorageCellTooltipComponent;
import ae2.util.ConfigInventory;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AirStorageCell extends AEBaseItem implements ICellWorkbenchItem, IStackTooltipDataProvider {
    private static final String STORAGE_CELL_FUZZY_MODE = "storage_cell_fuzzy_mode";

    private final double idleDrain;
    private final int totalBytes;

    public AirStorageCell(double idleDrain, int kilobytes) {
        this.setMaxStackSize(1);
        this.idleDrain = idleDrain;
        this.totalBytes = kilobytes * 1024;
    }

    public static int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1) {
            var cellInv = StorageCells.getCellInventory(stack, null);
            var cellStatus = cellInv != null ? cellInv.getStatus() : CellState.EMPTY;
            return cellStatus.getStateColor();
        }
        return 0xFFFFFF;
    }

    @Override
    protected void addCheckedInformation(ItemStack stack, World world, List<String> lines,
                                         ITooltipFlag advancedTooltips) {
        addToTooltip(stack, lines);
    }

    public AEKeyType getKeyType() {
        return AirKeyType.INSTANCE;
    }

    public int getBytes(ItemStack cellItem) {
        return this.totalBytes;
    }

    public double getIdleDrain() {
        return this.idleDrain;
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack stack) {
        return UpgradeInventories.forItem(stack, 3);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack stack) {
        return ConfigInventory.emptyTypes();
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(STORAGE_CELL_FUZZY_MODE, 8)) {
            try {
                return FuzzyMode.valueOf(tag.getString(STORAGE_CELL_FUZZY_MODE));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack stack, FuzzyMode fuzzyMode) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setString(STORAGE_CELL_FUZZY_MODE, fuzzyMode.name());
    }

    @Override
    public void addToTooltip(ItemStack stack, List<String> lines) {
        long stored = com.wintercogs.appliedpneumatics.common.me.storage.AirCellInventory.getStoredAir(stack);
        long capacity = (long) getBytes(stack) * AirKeyType.INSTANCE.getAmountPerByte();
        lines.add(stored + " / " + capacity + " L");
    }

    @Override
    public Optional<StorageCellTooltipComponent> getStackTooltipData(ItemStack stack) {
        long stored = com.wintercogs.appliedpneumatics.common.me.storage.AirCellInventory.getStoredAir(stack);
        if (stored <= 0) {
            return Optional.of(new StorageCellTooltipComponent(Collections.emptyList(), Collections.emptyList(), false, true));
        }
        return Optional.of(new StorageCellTooltipComponent(
            Collections.emptyList(),
            List.of(new ae2.api.stacks.GenericStack(com.wintercogs.appliedpneumatics.common.me.keys.AirKey.INSTANCE, stored)),
            false,
            true));
    }
}

