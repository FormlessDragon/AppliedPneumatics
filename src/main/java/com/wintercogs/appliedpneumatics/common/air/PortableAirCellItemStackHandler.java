package com.wintercogs.appliedpneumatics.common.air;

import com.wintercogs.appliedpneumatics.common.item.AirStorageCell;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import com.wintercogs.appliedpneumatics.common.me.storage.AirCellInventory;
import net.minecraft.item.ItemStack;

public final class PortableAirCellItemStackHandler {
    private static final float MAX_PRESSURE = 20.0F;

    private final ItemStack container;
    private final AirStorageCell storageCell;

    public PortableAirCellItemStackHandler(ItemStack container) {
        if (container.isEmpty() || !(container.getItem() instanceof AirStorageCell)) {
            throw new IllegalArgumentException("itemstack " + container + " must be an AirStorageCell");
        }
        this.container = container;
        this.storageCell = (AirStorageCell) container.getItem();
    }

    public ItemStack getContainer() {
        return this.container;
    }

    public float getPressure() {
        long capacity = getCapacity();
        if (capacity <= 0) {
            return 0.0F;
        }
        long storedAir = Math.min(AirCellInventory.getStoredAir(this.container), capacity);
        return storedAir * MAX_PRESSURE / capacity;
    }

    public int getAir() {
        long storedAir = AirCellInventory.getStoredAir(this.container);
        if (storedAir > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) storedAir;
    }

    public void addAir(int amount) {
        long capacity = getCapacity();
        long storedAir = Math.min(AirCellInventory.getStoredAir(this.container), capacity);
        long actual;
        if (amount >= 0) {
            actual = Math.min(capacity, storedAir + Math.min((long) amount, capacity - storedAir));
        } else {
            actual = Math.max(0, storedAir + amount);
        }
        AirCellInventory.setStoredAir(this.container, actual);
    }

    public int getBaseVolume() {
        return (int) (getCapacity() / maxPressure());
    }

    public void setBaseVolume(int size) {
    }

    public int getVolume() {
        return getBaseVolume();
    }

    public float maxPressure() {
        return MAX_PRESSURE;
    }

    private long getCapacity() {
        return (long) this.storageCell.getBytes(this.container) * AirKeyType.INSTANCE.getAmountPerByte();
    }
}
