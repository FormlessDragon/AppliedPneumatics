package com.wintercogs.appliedpneumatics.common.item;

import ae2.api.config.AccessRestriction;
import ae2.api.config.Actionable;
import ae2.api.implementations.items.IAEItemPowerStorage;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.upgrades.UpgradeInventories;
import ae2.api.upgrades.Upgrades;
import com.wintercogs.appliedpneumatics.common.air.PortableAirCellItemStackHandler;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class PortableAirStorageCell extends AirStorageCell implements IPressurizable, IAEItemPowerStorage {
    private static final String STORED_ENERGY = "stored_energy";
    private static final String ENERGY_CAPACITY = "energy_capacity";
    private static final String LEGACY_STORED_ENERGY = "internalCurrentPower";
    private static final String LEGACY_ENERGY_CAPACITY = "internalMaxPower";
    private static final double BASE_POWER_CAPACITY = 1600.0D;
    private static final double BASE_CHARGE_RATE = 80.0D;
    private static final double MIN_POWER = 0.0001D;

    public PortableAirStorageCell(double idleDrain, int kilobytes) {
        super(idleDrain, kilobytes);
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack stack) {
        return UpgradeInventories.forItem(stack, 5, this::onUpgradesChanged);
    }

    @Override
    public float getPressure(ItemStack stack) {
        return new PortableAirCellItemStackHandler(stack).getPressure();
    }

    @Override
    public void addAir(ItemStack stack, int amount) {
        new PortableAirCellItemStackHandler(stack).addAir(amount);
    }

    @Override
    public float maxPressure(ItemStack stack) {
        return new PortableAirCellItemStackHandler(stack).maxPressure();
    }

    @Override
    public int getVolume(ItemStack stack) {
        return new PortableAirCellItemStackHandler(stack).getVolume();
    }

    @Override
    public double injectAEPower(ItemStack stack, double amount, Actionable mode) {
        double maxStorage = getAEMaxPower(stack);
        double currentStorage = getAECurrentPower(stack);
        double inserted = Math.min(Math.max(0.0D, amount), Math.max(0.0D, maxStorage - currentStorage));
        if (mode == Actionable.MODULATE && inserted > 0.0D) {
            setAECurrentPower(stack, currentStorage + inserted);
        }
        return Math.max(0.0D, amount - inserted);
    }

    @Override
    public double extractAEPower(ItemStack stack, double amount, Actionable mode) {
        double extracted = Math.min(Math.max(0.0D, amount), getAECurrentPower(stack));
        if (mode == Actionable.MODULATE && extracted > 0.0D) {
            setAECurrentPower(stack, getAECurrentPower(stack) - extracted);
        }
        return extracted;
    }

    @Override
    public double getAEMaxPower(ItemStack stack) {
        return readLegacyCompatibleDouble(stack, ENERGY_CAPACITY, LEGACY_ENERGY_CAPACITY, calculateMaxPower(stack));
    }

    @Override
    public double getAECurrentPower(ItemStack stack) {
        return readLegacyCompatibleDouble(stack, STORED_ENERGY, LEGACY_STORED_ENERGY, 0.0D);
    }

    @Override
    public AccessRestriction getPowerFlow(ItemStack stack) {
        return AccessRestriction.WRITE;
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return BASE_CHARGE_RATE + BASE_CHARGE_RATE * getChargingCardMultiplier(stack);
    }

    private double calculateMaxPower(ItemStack stack) {
        return calculateMaxPower(getUpgrades(stack));
    }

    private double calculateMaxPower(IUpgradeInventory upgrades) {
        return BASE_POWER_CAPACITY * (1.0D + getChargingCardMultiplier(upgrades) * 8.0D);
    }

    private double getChargingCardMultiplier(ItemStack stack) {
        return getChargingCardMultiplier(getUpgrades(stack));
    }

    private double getChargingCardMultiplier(IUpgradeInventory upgrades) {
        return Upgrades.getEnergyCardMultiplier(upgrades) + upgrades.getInstalledUpgrades(APItems.CHARGING_CARD);
    }

    private void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        setAEMaxPower(stack, calculateMaxPower(upgrades));
    }

    private void setAECurrentPower(ItemStack stack, double power) {
        NBTTagCompound data = openNbtData(stack);
        double clampedPower = Math.max(0.0D, Math.min(power, getAEMaxPower(stack)));
        if (clampedPower < MIN_POWER) {
            data.removeTag(STORED_ENERGY);
            data.removeTag(LEGACY_STORED_ENERGY);
        } else {
            data.setDouble(STORED_ENERGY, clampedPower);
            data.removeTag(LEGACY_STORED_ENERGY);
        }
    }

    private void setAEMaxPower(ItemStack stack, double maxPower) {
        NBTTagCompound data = openNbtData(stack);
        if (Math.abs(maxPower - BASE_POWER_CAPACITY) < MIN_POWER) {
            data.removeTag(ENERGY_CAPACITY);
            data.removeTag(LEGACY_ENERGY_CAPACITY);
        } else {
            data.setDouble(ENERGY_CAPACITY, maxPower);
            data.removeTag(LEGACY_ENERGY_CAPACITY);
        }

        if (getAECurrentPower(stack) > maxPower) {
            setAECurrentPower(stack, maxPower);
        }
    }

    private NBTTagCompound openNbtData(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }

    private double readLegacyCompatibleDouble(ItemStack stack, String key, String legacyKey, double defaultValue) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return defaultValue;
        }
        if (tag.hasKey(key, 99)) {
            return tag.getDouble(key);
        }
        if (tag.hasKey(legacyKey, 99)) {
            return tag.getDouble(legacyKey);
        }
        return defaultValue;
    }
}

