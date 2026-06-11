package com.wintercogs.appliedpneumatics.common.me.storage;

import ae2.api.config.Actionable;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.KeyCounter;
import ae2.api.storage.cells.CellState;
import ae2.api.storage.cells.ISaveProvider;
import ae2.api.storage.cells.StorageCell;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.text.TextComponentItemStack;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import com.wintercogs.appliedpneumatics.common.item.AirStorageCell;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public final class AirCellInventory implements StorageCell {
    public static final String STORED_AIR_TAG = "appliedpneumatics_air";

    private final ItemStack stack;
    private final AirStorageCell cellType;
    @Nullable
    private final ISaveProvider saveProvider;
    private final IUpgradeInventory upgrades;
    private long storedAir;
    private boolean persisted = true;

    AirCellInventory(ItemStack stack, AirStorageCell cellType, @Nullable ISaveProvider saveProvider) {
        this(stack, cellType, saveProvider, cellType.getUpgrades(stack));
    }

    AirCellInventory(ItemStack stack, AirStorageCell cellType, @Nullable ISaveProvider saveProvider,
                     IUpgradeInventory upgrades) {
        this.stack = stack;
        this.cellType = cellType;
        this.saveProvider = saveProvider;
        this.upgrades = upgrades;
        this.storedAir = getStoredAir(stack);
    }

    public static boolean hasStoredAir(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(STORED_AIR_TAG, 4) && tag.getLong(STORED_AIR_TAG) > 0;
    }

    public static long getStoredAir(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(STORED_AIR_TAG, 4)) {
            return 0;
        }
        return Math.max(0, tag.getLong(STORED_AIR_TAG));
    }

    public static void setStoredAir(ItemStack stack, long amount) {
        if (amount <= 0) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null) {
                tag.removeTag(STORED_AIR_TAG);
                if (tag.isEmpty()) {
                    stack.setTagCompound(null);
                }
            }
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setLong(STORED_AIR_TAG, amount);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0 || what != AirKey.INSTANCE) {
            return 0;
        }

        long inserted = Math.min(amount, getRemainingAmount());
        if (mode == Actionable.MODULATE && inserted > 0) {
            this.storedAir += inserted;
            saveChanges();
        }
        if (amount > inserted && hasVacuumUpgrade()) {
            return amount;
        }
        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0 || what != AirKey.INSTANCE || this.storedAir <= 0) {
            return 0;
        }

        long extracted = Math.min(amount, this.storedAir);
        if (mode == Actionable.MODULATE && extracted > 0) {
            this.storedAir -= extracted;
            saveChanges();
        }
        return extracted;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (this.storedAir > 0) {
            out.add(AirKey.INSTANCE, this.storedAir);
        }
    }

    @Override
    public ITextComponent getDescription() {
        return TextComponentItemStack.of(this.stack);
    }

    @Override
    public CellState getStatus() {
        if (this.storedAir <= 0) {
            return CellState.EMPTY;
        }
        return getRemainingAmount() > 0 ? CellState.NOT_EMPTY : CellState.FULL;
    }

    @Override
    public double getIdleDrain() {
        return this.cellType.getIdleDrain();
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return what == AirKey.INSTANCE;
    }

    @Override
    public boolean canFitInsideCell() {
        return this.storedAir <= 0;
    }

    @Override
    public void persist() {
        if (this.persisted) {
            return;
        }

        setStoredAir(this.stack, this.storedAir);
        this.persisted = true;
    }

    public long getTotalBytes() {
        return this.cellType.getBytes(this.stack);
    }

    public long getUsedBytes() {
        long amountPerByte = AirKeyType.INSTANCE.getAmountPerByte();
        return (this.storedAir + amountPerByte - 1) / amountPerByte;
    }

    public long getRemainingAmount() {
        long capacity = getTotalBytes() * (long) AirKeyType.INSTANCE.getAmountPerByte();
        return Math.max(0, capacity - this.storedAir);
    }

    private void saveChanges() {
        this.storedAir = Math.max(0, this.storedAir);
        this.persisted = false;
        if (this.saveProvider != null) {
            this.saveProvider.saveChanges();
        } else {
            persist();
        }
    }

    private boolean hasVacuumUpgrade() {
        return this.upgrades.isInstalled(APItems.VACUUM_CARD);
    }
}
