package com.wintercogs.appliedpneumatics.common.tile;

import ae2.api.networking.GridFlags;
import ae2.api.networking.IGrid;
import ae2.api.networking.security.IActionSource;
import ae2.api.networking.storage.IStorageService;
import ae2.api.inventories.ISegmentedInventory;
import ae2.api.inventories.InternalInventory;
import ae2.api.storage.MEStorage;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.upgrades.IUpgradeableObject;
import ae2.api.upgrades.UpgradeInventories;
import ae2.tile.ServerTickingTile;
import ae2.tile.grid.AENetworkedTile;
import ae2.util.inv.AppEngInternalInventory;
import ae2.util.inv.InternalInventoryHost;
import ae2.util.inv.filter.IAEItemFilter;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.pressure.AirHandler;
import me.desht.pneumaticcraft.common.pressure.AirHandlerSupplier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class MEPressureInterfaceTile extends AENetworkedTile
    implements IPneumaticMachine, ServerTickingTile, InternalInventoryHost, IUpgradeableObject, ISegmentedInventory {
    private static final String AIR_HANDLER_TAG = "air_handler";
    private static final String INVENTORY_TAG = "inv";
    private static final String UPGRADES_TAG = "interface_upgrades";
    private static final String EXPECTED_PRESSURE_TAG = "expected_pressure";
    private static final int BASE_VOLUME = 1000;
    private static final int CONTAINER_AIR_PER_TICK = 1000;
    private static final float PRESSURE_EPSILON = 0.01F;

    private final IAirHandler airHandler = AirHandlerSupplier.getInstance().createTierTwoAirHandler(BASE_VOLUME);
    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 1, 1, new AirContainerFilter());
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(APBlocks.ME_PRESSURE_INTERFACE_ITEM, 5,
        this::onUpgradesChanged);
    private float expectedPressure = 2.0F;

    public MEPressureInterfaceTile() {
        getMainNode()
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setIdlePowerUsage(1.0D);
    }

    @Override
    public void onReady() {
        super.onReady();
        this.airHandler.validate(this);
    }

    @Override
    public ItemStack getItemFromTile() {
        return new ItemStack(APBlocks.ME_PRESSURE_INTERFACE_ITEM);
    }

    @Override
    public IAirHandler getAirHandler(EnumFacing side) {
        return this.airHandler;
    }

    @Override
    public void serverTick() {
        exchangeAirWithNetwork();
        transferAirWithContainedItem();
        if (this.getWorld() != null && !this.getWorld().isRemote) {
            this.airHandler.update();
        }
    }

    public void onNeighborChanged() {
        if (this.getWorld() != null && this.getPos() != null) {
            this.airHandler.onNeighborChange();
        }
    }

    public InternalInventory getInternalInventory() {
        return this.inventory;
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (ISegmentedInventory.STORAGE.equals(id)) {
            return this.inventory;
        }
        if (ISegmentedInventory.UPGRADES.equals(id)) {
            return this.upgrades;
        }
        return null;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    public float getExpectedPressure() {
        return this.expectedPressure;
    }

    public void setExpectedPressure(float expectedPressure) {
        float minimumPressure = isUpgradedWith(APItems.VACUUM_CARD) ? -1.0F : 0.0F;
        this.expectedPressure = Math.max(minimumPressure, Math.min(20.0F, expectedPressure));
        saveChanges();
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inventory) {
        saveChanges();
    }

    @Override
    public boolean isClientSide() {
        return this.getWorld() != null && this.getWorld().isRemote;
    }

    @Override
    public void loadTag(NBTTagCompound tag) {
        super.loadTag(tag);
        if (tag.hasKey(AIR_HANDLER_TAG, 10)) {
            this.airHandler.readFromNBT(tag.getCompoundTag(AIR_HANDLER_TAG));
        }
        this.inventory.readFromNBT(tag, INVENTORY_TAG);
        if (tag.hasKey(EXPECTED_PRESSURE_TAG, 5)) {
            this.expectedPressure = tag.getFloat(EXPECTED_PRESSURE_TAG);
        }
        this.upgrades.readFromNBT(tag, UPGRADES_TAG);
        onUpgradesChanged();
    }

    @Override
    public void saveAdditional(NBTTagCompound tag) {
        super.saveAdditional(tag);
        NBTTagCompound airTag = new NBTTagCompound();
        this.airHandler.writeToNBT(airTag);
        tag.setTag(AIR_HANDLER_TAG, airTag);
        this.inventory.writeToNBT(tag, INVENTORY_TAG);
        tag.setFloat(EXPECTED_PRESSURE_TAG, this.expectedPressure);
        this.upgrades.writeToNBT(tag, UPGRADES_TAG);
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops) {
        super.addAdditionalDrops(drops);
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        for (ItemStack stack : this.upgrades) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.inventory.clear();
        this.upgrades.clear();
    }

    private void onUpgradesChanged() {
        setExpectedPressure(this.expectedPressure);
        int volumeMultiplierShift = 2 * this.upgrades.getInstalledUpgrades(APItems.VOLUME_CARD);
        int volume = BASE_VOLUME * (1 << volumeMultiplierShift);
        this.airHandler.setDefaultVolume(volume);
        if (this.airHandler instanceof AirHandler) {
            ((AirHandler) this.airHandler).setVolume(volume);
        }
    }

    private void exchangeAirWithNetwork() {
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null || !this.getMainNode().isActive()) {
            return;
        }

        IStorageService storageService = grid.getStorageService();
        if (storageService == null || grid.getEnergyService() == null) {
            return;
        }

        MEStorage storage = storageService.getInventory();
        int moved = MEPressureInterfaceAirExchange.exchange(this.airHandler.getVolume(), this.airHandler.getAir(),
            this.expectedPressure, storage, grid.getEnergyService(), IActionSource.ofMachine(this));
        if (moved != 0) {
            this.airHandler.addAir(moved);
            saveChanges();
        }
    }

    private void transferAirWithContainedItem() {
        ItemStack container = this.inventory.getStackInSlot(0);
        if (container.isEmpty()) {
            return;
        }

        IPressurizable pressurizable = IPressurizable.of(container);
        if (pressurizable == null) {
            return;
        }

        float interfacePressure = this.airHandler.getPressure();
        float itemPressure = pressurizable.getPressure(container);
        int itemVolume = pressurizable.getVolume(container);
        if (itemVolume <= 0) {
            return;
        }

        int airInItem = Math.max(0, (int) (itemPressure * itemVolume));
        float delta = Math.abs(interfacePressure - itemPressure) / 2.0F;

        if (epsilonEquals(interfacePressure, 0.0F) && delta < 0.1F) {
            if (airInItem != 0) {
                pressurizable.addAir(container, -airInItem);
                saveChanges();
            }
            return;
        }

        if (itemPressure > interfacePressure + PRESSURE_EPSILON && itemPressure > 0.0F) {
            int move = Math.min(Math.min(CONTAINER_AIR_PER_TICK, airInItem),
                (int) (delta * this.airHandler.getVolume()));
            if (move > 0) {
                pressurizable.addAir(container, -move);
                this.airHandler.addAir(move);
                saveChanges();
            }
            return;
        }

        if (itemPressure < interfacePressure - PRESSURE_EPSILON && itemPressure < pressurizable.maxPressure(container)) {
            int maxAirInItem = (int) (pressurizable.maxPressure(container) * itemVolume);
            int itemSpace = Math.max(0, maxAirInItem - airInItem);
            float boost = interfacePressure < 15.0F ? 1.0F : 1.0F + (interfacePressure - 15.0F) / 5.0F;
            int move = Math.min(Math.min((int) (CONTAINER_AIR_PER_TICK * boost), this.airHandler.getAir()), itemSpace);
            move = Math.min(move, (int) (delta * itemVolume));

            if (move > 0) {
                pressurizable.addAir(container, move);
                this.airHandler.addAir(-move);
                saveChanges();
            }
        }
    }

    private static boolean epsilonEquals(float left, float right) {
        return Math.abs(left - right) < 0.0001F;
    }

    private static final class AirContainerFilter implements IAEItemFilter {
        @Override
        public boolean allowInsert(InternalInventory inventory, int slot, ItemStack stack) {
            return !stack.isEmpty() && IPressurizable.of(stack) != null;
        }
    }
}

