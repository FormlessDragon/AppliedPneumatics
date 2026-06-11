package com.wintercogs.appliedpneumatics.common.tile;

import ae2.api.config.Actionable;
import ae2.api.config.PowerMultiplier;
import ae2.api.inventories.ISegmentedInventory;
import ae2.api.inventories.InternalInventory;
import ae2.api.networking.GridFlags;
import ae2.api.networking.IGrid;
import ae2.api.networking.energy.IEnergyService;
import ae2.api.networking.security.IActionSource;
import ae2.api.networking.storage.IStorageService;
import ae2.api.storage.MEStorage;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.upgrades.IUpgradeableObject;
import ae2.api.upgrades.UpgradeInventories;
import ae2.tile.ServerTickingTile;
import ae2.tile.grid.AENetworkedTile;
import com.wintercogs.appliedpneumatics.common.block.METemperatureInterfaceBlock;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class METemperatureInterfaceTile extends AENetworkedTile
    implements IHeatExchanger, ServerTickingTile, IUpgradeableObject, ISegmentedInventory {
    private static final String UPGRADES_TAG = "upgrades";
    private static final String HEAT_HANDLER_TAG = "heat_handler";
    private static final String THERMAL_CAPACITY_TAG = "thermal_capacity";
    private static final String VOLUME_UPGRADES_TAG = "volume_upgrades";
    private static final String EXPECTED_TEMPERATURE_TAG = "expected_temperature";
    private static final double BASE_HEAT_CAPACITY = 1000.0D;
    private static final double AIR_COST_PER_1000J = 19145.0D;
    private static final double AE_ENERGY_COST_PER_1000J = 24889.0D;
    private static final double DEFAULT_EXPECTED_TEMPERATURE = 300.0D;
    private static final int SOFT_UPDATE_FLAGS = 3;

    private final IHeatExchangerLogic heatHandler =
        HeatExchangerManager.getInstance().getHeatExchangerLogic();
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(APBlocks.ME_TEMPERATURE_INTERFACE_ITEM, 5,
        this::onUpgradesChanged);
    private double expectedTemperature = DEFAULT_EXPECTED_TEMPERATURE;
    private int maxTemperatureChangePerTick = 1;
    private double lastTemperature = 0.0D;

    public METemperatureInterfaceTile() {
        getMainNode()
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setIdlePowerUsage(8.0D);
        this.heatHandler.setThermalCapacity(BASE_HEAT_CAPACITY);
    }

    @Override
    public void onReady() {
        super.onReady();
        if (this.getWorld() != null && this.getPos() != null) {
            this.heatHandler.initializeAsHull(this.getWorld(), this.getPos(), EnumFacing.values());
        }
    }

    @Override
    public ItemStack getItemFromTile() {
        return new ItemStack(APBlocks.ME_TEMPERATURE_INTERFACE_ITEM);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return this.heatHandler;
    }

    @Override
    public void serverTick() {
        if (this.getWorld() == null || this.getWorld().isRemote) {
            return;
        }

        if (this.getMainNode().isActive()) {
            interactWithME();
        }
        this.heatHandler.update();
        updateBlockTemperatureState();
    }

    public double getExpectedTemperature() {
        return this.expectedTemperature;
    }

    public void setExpectedTemperature(double expectedTemperature) {
        this.expectedTemperature = clampExpectedTemperature(expectedTemperature);
        saveChanges();
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (ISegmentedInventory.UPGRADES.equals(id)) {
            return this.upgrades;
        }
        return null;
    }

    @Override
    public void loadTag(NBTTagCompound tag) {
        super.loadTag(tag);
        this.upgrades.readFromNBT(tag, UPGRADES_TAG);
        updateSpeedUpgradeState();
        if (tag.hasKey(HEAT_HANDLER_TAG, 10)) {
            this.heatHandler.readFromNBT(tag.getCompoundTag(HEAT_HANDLER_TAG));
        }
        if (tag.hasKey(THERMAL_CAPACITY_TAG, 99)) {
            this.heatHandler.setThermalCapacity(tag.getDouble(THERMAL_CAPACITY_TAG));
        } else if (tag.hasKey(VOLUME_UPGRADES_TAG, 99)) {
            applyUpgradeCapacity(tag.getInteger(VOLUME_UPGRADES_TAG));
        } else {
            applyUpgradeCapacity();
        }
        if (tag.hasKey(EXPECTED_TEMPERATURE_TAG, 99)) {
            this.expectedTemperature = clampExpectedTemperature(tag.getDouble(EXPECTED_TEMPERATURE_TAG));
        }
    }

    @Override
    public void saveAdditional(NBTTagCompound tag) {
        super.saveAdditional(tag);
        this.upgrades.writeToNBT(tag, UPGRADES_TAG);
        NBTTagCompound heatTag = new NBTTagCompound();
        this.heatHandler.writeToNBT(heatTag);
        tag.setTag(HEAT_HANDLER_TAG, heatTag);
        tag.setDouble(THERMAL_CAPACITY_TAG, this.heatHandler.getThermalCapacity());
        tag.setInteger(VOLUME_UPGRADES_TAG, this.upgrades.getInstalledUpgrades(APItems.VOLUME_CARD));
        tag.setDouble(EXPECTED_TEMPERATURE_TAG, this.expectedTemperature);
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops) {
        super.addAdditionalDrops(drops);
        for (ItemStack stack : this.upgrades) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.upgrades.clear();
    }

    private void onUpgradesChanged() {
        updateSpeedUpgradeState();
        applyUpgradeCapacity();
        interactWithME();
        saveChanges();
    }

    private void updateSpeedUpgradeState() {
        Item speedCard = APItems.getAe2SpeedCard();
        int speedCards = speedCard == null ? 0 : this.upgrades.getInstalledUpgrades(speedCard);
        this.maxTemperatureChangePerTick = speedCards <= 0 ? 1 : 1 << speedCards;
    }

    private void applyUpgradeCapacity() {
        int volumeCards = this.upgrades.getInstalledUpgrades(APItems.VOLUME_CARD);
        applyUpgradeCapacity(volumeCards);
    }

    private void applyUpgradeCapacity(int volumeCards) {
        int heatCapacityMultiplier = volumeCards <= 0 ? 1 : 1 << volumeCards;
        this.heatHandler.setThermalCapacity(BASE_HEAT_CAPACITY * heatCapacityMultiplier);
    }

    private static double clampExpectedTemperature(double expectedTemperature) {
        return Math.max(0.0D, Math.min(2273.0D, expectedTemperature));
    }

    private void interactWithME() {
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null || !this.getMainNode().isActive()) {
            return;
        }

        IStorageService storageService = grid.getStorageService();
        IEnergyService energyService = grid.getEnergyService();
        if (storageService == null || energyService == null) {
            return;
        }

        MEStorage storage = storageService.getInventory();
        if (storage == null) {
            return;
        }

        double currentTemperature = this.heatHandler.getTemperature();
        double temperatureDifference = this.expectedTemperature - currentTemperature;
        if (temperatureDifference == 0.0D) {
            return;
        }

        double capacity = Math.max(0.0D, this.heatHandler.getThermalCapacity());
        if (capacity < 0.1D) {
            return;
        }

        double direction = Math.signum(temperatureDifference);
        double targetDeltaTemperature =
            Math.min(Math.abs(temperatureDifference), this.maxTemperatureChangePerTick);
        double targetHeat = targetDeltaTemperature * capacity;
        IActionSource source = IActionSource.ofMachine(this);

        long availableAir = storage.extract(AirKey.INSTANCE, Long.MAX_VALUE, Actionable.SIMULATE, source);
        double availableEnergy = energyService.getStoredPower();
        double heatByAir = availableAir <= 0 ? 0.0D : availableAir * 1000.0D / AIR_COST_PER_1000J;
        double heatByEnergy = availableEnergy <= 0.0D ? 0.0D : availableEnergy * 1000.0D / AE_ENERGY_COST_PER_1000J;
        double appliedHeat = Math.min(targetHeat, Math.min(heatByAir, heatByEnergy));
        if (appliedHeat <= 0.0D) {
            return;
        }

        long airToUse = (long) Math.min(availableAir, Math.ceil(appliedHeat / 1000.0D * AIR_COST_PER_1000J));
        double energyToUse = Math.min(availableEnergy, appliedHeat / 1000.0D * AE_ENERGY_COST_PER_1000J);
        long airExtracted = storage.extract(AirKey.INSTANCE, airToUse, Actionable.MODULATE, source);
        double energyExtracted = energyService.extractAEPower(energyToUse, Actionable.MODULATE, PowerMultiplier.CONFIG);

        double realHeatByAir = airExtracted <= 0 ? 0.0D : airExtracted * 1000.0D / AIR_COST_PER_1000J;
        double realHeatByEnergy =
            energyExtracted <= 0.0D ? 0.0D : energyExtracted * 1000.0D / AE_ENERGY_COST_PER_1000J;
        double realHeat = Math.min(appliedHeat, Math.min(realHeatByAir, realHeatByEnergy));
        if (realHeat > 0.0D) {
            this.heatHandler.addHeat(direction * realHeat);
            saveChanges();
        }
    }

    private void updateBlockTemperatureState() {
        double currentTemperature = this.heatHandler.getTemperature();
        if (this.lastTemperature == currentTemperature) {
            return;
        }
        this.lastTemperature = currentTemperature;
        saveChanges();

        IBlockState state = getBlockState();
        if (state.getBlock() != APBlocks.ME_TEMPERATURE_INTERFACE_BLOCK) {
            return;
        }

        METemperatureInterfaceBlock.TemperatureState temperatureState =
            METemperatureInterfaceBlock.TemperatureState.fromTemperature(currentTemperature);
        IBlockState updated = state.withProperty(METemperatureInterfaceBlock.TEMPERATURE_STATE, temperatureState);
        if (updated != state) {
            this.getWorld().setBlockState(this.getPos(), updated, SOFT_UPDATE_FLAGS);
        }
    }
}

