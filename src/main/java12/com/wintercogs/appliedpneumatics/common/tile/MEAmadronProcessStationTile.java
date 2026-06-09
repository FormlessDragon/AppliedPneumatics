package com.wintercogs.appliedpneumatics.common.tile;

import ae2.api.AECapabilities;
import ae2.api.behaviors.GenericInternalInventory;
import ae2.api.crafting.IPatternDetails;
import ae2.api.implementations.blockentities.PatternContainerGroup;
import ae2.api.inventories.ISegmentedInventory;
import ae2.api.inventories.InternalInventory;
import ae2.api.networking.IGrid;
import ae2.api.networking.GridFlags;
import ae2.api.networking.crafting.ICraftingProvider;
import ae2.api.networking.security.IActionSource;
import ae2.api.networking.storage.IStorageService;
import ae2.api.config.Actionable;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.AEFluidKey;
import ae2.api.stacks.AEKeyType;
import ae2.api.stacks.GenericStack;
import ae2.api.stacks.KeyCounter;
import ae2.api.storage.MEStorage;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.upgrades.IUpgradeableObject;
import ae2.api.upgrades.UpgradeInventories;
import ae2.container.ISubGui;
import ae2.helpers.externalstorage.GenericStackFluidStorage;
import ae2.helpers.externalstorage.GenericStackInv;
import ae2.helpers.externalstorage.GenericStackItemStorage;
import ae2.helpers.IPriorityHost;
import ae2.helpers.patternprovider.PatternContainer;
import ae2.text.TextComponentItemStack;
import ae2.tile.ServerTickingTile;
import ae2.tile.grid.AENetworkedTile;
import ae2.util.inv.AppEngInternalInventory;
import ae2.util.inv.InternalInventoryHost;
import ae2.util.inv.filter.IAEItemFilter;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import com.wintercogs.appliedpneumatics.common.me.amadron.AmadronPatternDetails;
import com.wintercogs.appliedpneumatics.common.me.amadron.Pnc112AmadronBridge;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class MEAmadronProcessStationTile extends AENetworkedTile
    implements ServerTickingTile, IUpgradeableObject, ISegmentedInventory, InternalInventoryHost, IPriorityHost,
    ICraftingProvider, PatternContainer {
    private static final String PATTERN_INVENTORY_TAG = "pattern_inv";
    private static final String UPGRADES_TAG = "upgrade_inv";
    private static final String INPUT_INVENTORY_TAG = "input_inv";
    private static final String OUTPUT_INVENTORY_TAG = "output_inv";
    private static final String PRIORITY_TAG = "priority";
    private static final String JOBS_TAG = "jobs";
    private static final String AP_ITEM_ID_TAG = "ap_item_id";
    private static final int AMADRON_INVENTORY_SLOTS = 9;
    private static final long FLUID_SLOT_CAPACITY = 64_000L;
    private static final int MAX_ITEM_STACKS_PER_DRONE = 36;
    private static final int MAX_FLUID_MB_PER_DRONE = 576_000;

    private final AppEngInternalInventory patternInventory;
    private final IUpgradeInventory upgrades;
    private final GenericStackInv inputInventory = new GenericStackInv(this::saveChanges, AMADRON_INVENTORY_SLOTS);
    private final GenericStackInv outputInventory = new GenericStackInv(this::saveChanges, AMADRON_INVENTORY_SLOTS);
    private final GenericInternalInventory externalInventory = new CombinedAmadronInventory();
    private final GenericStackItemStorage externalItemHandler = new GenericStackItemStorage(this.externalInventory);
    private final GenericStackFluidStorage externalFluidHandler = new GenericStackFluidStorage(this.externalInventory);
    private final List<AmadronJob> jobs = new ArrayList<>();
    private int priority;

    public MEAmadronProcessStationTile() {
        this(9, APBlocks.ME_AMADRON_PROCESS_STATION_ITEM);
    }

    protected MEAmadronProcessStationTile(int patternSlots, Item upgradeHostItem) {
        getMainNode()
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setIdlePowerUsage(8.0D)
            .addService(ICraftingProvider.class, this);
        this.patternInventory = new AppEngInternalInventory(this, patternSlots, 1, new PatternFilter());
        this.upgrades = UpgradeInventories.forMachine(upgradeHostItem, 4, this::saveChanges);
        this.inputInventory.useRegisteredCapacities();
        this.outputInventory.useRegisteredCapacities();
        this.inputInventory.setCapacity(AEKeyType.fluids(), FLUID_SLOT_CAPACITY);
        this.outputInventory.setCapacity(AEKeyType.fluids(), FLUID_SLOT_CAPACITY);
    }

    @Override
    public ItemStack getItemFromTile() {
        return new ItemStack(APBlocks.ME_AMADRON_PROCESS_STATION_ITEM);
    }

    public AppEngInternalInventory getPatternInventory() {
        return this.patternInventory;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    public GenericStackInv getInputInventory() {
        return this.inputInventory;
    }

    public GenericStackInv getOutputInventory() {
        return this.outputInventory;
    }

    public GenericInternalInventory getExternalInventory() {
        return this.externalInventory;
    }

    public IItemHandler getExternalItemHandler() {
        return this.externalItemHandler;
    }

    public IFluidHandler getExternalFluidHandler() {
        return this.externalFluidHandler;
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (ISegmentedInventory.CONFIG.equals(id)) {
            return this.patternInventory;
        }
        if (ISegmentedInventory.UPGRADES.equals(id)) {
            return this.upgrades;
        }
        return null;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(int newValue) {
        this.priority = newValue;
        saveChanges();
        ICraftingProvider.requestUpdate(getMainNode());
    }

    @Override
    public void serverTick() {
        World world = getWorld();
        if (world == null || world.isRemote) {
            return;
        }

        if (this.getMainNode().isActive()) {
            flushOutputToME();
        }
        if (this.jobs.isEmpty()) {
            return;
        }

        int dimensionId = world.provider == null ? 0 : world.provider.getDimension();
        dispatchNextJob(world, getPos(), dimensionId, "AppliedPneumatics",
            () -> new ItemStack(me.desht.pneumaticcraft.common.item.Itemss.AMADRON_TABLET));
    }

    public int getJobAmount() {
        return this.jobs.size();
    }

    public List<AmadronJob> getJobsForTesting() {
        return Collections.unmodifiableList(this.jobs);
    }

    public boolean dispatchNextJobForTesting(World world, BlockPos stationPos, int dimensionId, String buyingPlayer,
                                             Supplier<ItemStack> tabletFactory,
                                             Pnc112AmadronBridge.AmadronOrderDispatcher dispatcher) {
        return dispatchNextJob(world, stationPos, dimensionId, buyingPlayer, tabletFactory, dispatcher);
    }

    @Nullable
    protected MEStorage getNetworkInventory() {
        IGrid grid = getGrid();
        if (grid == null) {
            return null;
        }

        IStorageService storageService = grid.getStorageService();
        return storageService == null ? null : storageService.getInventory();
    }

    boolean flushOutputToME() {
        MEStorage storage = getNetworkInventory();
        if (storage == null) {
            return false;
        }

        boolean moved = false;
        IActionSource source = actionSource();
        for (int slot = 0; slot < this.outputInventory.size(); slot++) {
            GenericStack stack = this.outputInventory.getStack(slot);
            if (stack == null || stack.amount() <= 0) {
                continue;
            }

            long inserted = storage.insert(stack.what(), stack.amount(), Actionable.MODULATE, source);
            if (inserted > 0) {
                this.outputInventory.extract(slot, stack.what(), inserted, Actionable.MODULATE);
                moved = true;
            }
        }

        if (moved) {
            saveChanges();
        }
        return moved;
    }

    private boolean dispatchNextJob(World world, BlockPos stationPos, int dimensionId, String buyingPlayer,
                                    Supplier<ItemStack> tabletFactory) {
        return dispatchNextJob(world, stationPos, dimensionId, buyingPlayer, tabletFactory, null);
    }

    private boolean dispatchNextJob(World world, BlockPos stationPos, int dimensionId, String buyingPlayer,
                                    Supplier<ItemStack> tabletFactory,
                                    Pnc112AmadronBridge.AmadronOrderDispatcher dispatcher) {
        if (this.jobs.isEmpty() || stationPos == null) {
            return false;
        }

        DispatchBatch batch = stageDispatchBatch();
        if (batch.units <= 0 || batch.units > Integer.MAX_VALUE) {
            return false;
        }

        AmadronJob firstJob = batch.firstJob;
        boolean dispatched = dispatcher == null
            ? Pnc112AmadronBridge.dispatchAmadronOrder(firstJob.getOffer(), (int) batch.units, world, stationPos,
                dimensionId, buyingPlayer)
            : Pnc112AmadronBridge.dispatchAmadronOrder(firstJob.getOffer(), (int) batch.units, world, stationPos, dimensionId,
                buyingPlayer, tabletFactory, dispatcher);
        if (dispatched) {
            completeStagedBatch(batch);
            saveChanges();
        } else {
            refundStagedPayments(batch.stagedPayments, world, stationPos);
            completeStagedBatch(batch);
            saveChanges();
        }
        return dispatched;
    }

    private DispatchBatch stageDispatchBatch() {
        AmadronJob firstJob = this.jobs.get(0);
        int maxUnitsPerDispatch = computeMaxUnitsPerDispatch(firstJob.getOffer());
        if (maxUnitsPerDispatch <= 0) {
            return DispatchBatch.empty(firstJob);
        }
        DispatchBatch batch = new DispatchBatch(firstJob);
        long selectedUnits = 0;
        for (int jobIndex = 0; jobIndex < this.jobs.size(); jobIndex++) {
            AmadronJob job = this.jobs.get(jobIndex);
            if (!firstJob.getOffer().equals(job.getOffer())) {
                break;
            }
            long remainingCapacity = maxUnitsPerDispatch - selectedUnits;
            if (remainingCapacity <= 0) {
                break;
            }

            long unitsToStage = Math.min(job.getUnits(), remainingCapacity);
            long nextUnits = selectedUnits + unitsToStage;
            if (nextUnits > Integer.MAX_VALUE
                || !Pnc112AmadronBridge.hasEnoughStock(firstJob.getOffer(), (int) nextUnits)) {
                break;
            }
            GenericStack payment = job.getPayment();
            long paymentToStage = payment.amount() * unitsToStage / job.getUnits();
            if (paymentToStage <= 0) {
                break;
            }
            long inserted = this.inputInventory.insert(payment.what(), paymentToStage, Actionable.SIMULATE, null);
            if (inserted < paymentToStage) {
                break;
            }
            this.inputInventory.insert(payment.what(), paymentToStage, Actionable.MODULATE, null);
            batch.stagedPayments.add(new GenericStack(payment.what(), paymentToStage));
            selectedUnits = nextUnits;
            if (unitsToStage == job.getUnits()) {
                batch.fullJobs++;
            } else {
                batch.splitJob = new AmadronJob(job.getOffer(),
                    new GenericStack(payment.what(), payment.amount() - paymentToStage),
                    job.getUnits() - (int) unitsToStage);
                break;
            }
        }
        batch.units = selectedUnits;
        return batch;
    }

    private static int computeMaxUnitsPerDispatch(AmadronOffer offer) {
        Object output = offer.getOutput();
        if (output instanceof ItemStack) {
            ItemStack stack = (ItemStack) output;
            if (stack.isEmpty()) {
                return 0;
            }
            int amountPerUnit = Math.max(1, stack.getCount());
            int maxStackSize = Math.max(1, stack.getMaxStackSize());
            long maxItems = (long) MAX_ITEM_STACKS_PER_DRONE * maxStackSize;
            return (int) Math.min(maxItems / amountPerUnit, Integer.MAX_VALUE);
        }
        if (output instanceof FluidStack) {
            FluidStack stack = (FluidStack) output;
            if (stack.getFluid() == null || stack.amount <= 0) {
                return 0;
            }
            return MAX_FLUID_MB_PER_DRONE / Math.max(1, stack.amount);
        }
        return 0;
    }

    private void refundStagedPayments(List<GenericStack> stagedPayments, World world, BlockPos stationPos) {
        for (GenericStack payment : stagedPayments) {
            long extracted = this.inputInventory.extract(payment.what(), payment.amount(), Actionable.MODULATE, null);
            refundPayment(payment.what(), extracted, world, stationPos);
        }
    }

    private void completeStagedBatch(DispatchBatch batch) {
        if (batch.fullJobs > 0) {
            this.jobs.subList(0, batch.fullJobs).clear();
        }
        if (batch.splitJob != null) {
            this.jobs.set(0, batch.splitJob);
        }
    }

    private void refundPayment(AEKey what, long amount, World world, BlockPos stationPos) {
        if (amount <= 0) {
            return;
        }

        long remaining = amount;
        MEStorage storage = getNetworkInventory();
        if (storage != null) {
            remaining -= storage.insert(what, remaining, Actionable.MODULATE, actionSource());
        }
        if (remaining <= 0) {
            return;
        }

        remaining -= this.outputInventory.insert(what, remaining, Actionable.MODULATE, null);
        if (remaining <= 0 || !(what instanceof AEItemKey) || world == null || stationPos == null) {
            return;
        }

        AEItemKey itemKey = (AEItemKey) what;
        while (remaining > 0) {
            int dropAmount = (int) Math.min(Math.min(remaining, Integer.MAX_VALUE), itemKey.getMaxStackSize());
            Block.spawnAsEntity(world, stationPos, itemKey.toStack(dropAmount));
            remaining -= dropAmount;
        }
    }

    private long insertOutput(int slot, AEKey what, long amount, Actionable mode) {
        long insertedToNetwork = 0;
        MEStorage storage = getNetworkInventory();
        if (storage != null) {
            insertedToNetwork = storage.insert(what, amount, mode, actionSource());
        }

        long remaining = amount - insertedToNetwork;
        if (remaining <= 0) {
            return insertedToNetwork;
        }
        return insertedToNetwork + this.outputInventory.insert(slot, what, remaining, mode);
    }

    private IActionSource actionSource() {
        return this.getMainNode().isReady() ? IActionSource.ofMachine(this) : IActionSource.empty();
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        List<IPatternDetails> result = new ArrayList<>();
        for (ItemStack stack : this.patternInventory) {
            if (stack.isEmpty() || stack.getItem() != APItems.AMADRON_PATTERN) {
                continue;
            }
            try {
                result.add(new AmadronPatternDetails(AEItemKey.of(stack)));
            } catch (IllegalArgumentException ignored) {
                // Empty pattern slots and malformed patterns are ignored by the terminal listing.
            }
        }
        return result;
    }

    @Override
    public int getPatternPriority() {
        return this.priority;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder, int multiplier) {
        if (isBusy() || !(patternDetails instanceof AmadronPatternDetails) || inputHolder == null || inputHolder.length == 0
            || multiplier <= 0) {
            return false;
        }
        AmadronPatternDetails details = (AmadronPatternDetails) patternDetails;
        IPatternDetails.IInput input = details.getInputs()[0];
        Object2LongMap.Entry<ae2.api.stacks.AEKey> payment = findSinglePositivePayment(inputHolder[0], input);
        if (payment == null) {
            return false;
        }

        long inputMultiplier = input.getMultiplier();
        long paymentAmount = payment.getLongValue();
        if (inputMultiplier <= 0 || paymentAmount % inputMultiplier != 0) {
            return false;
        }

        long units = paymentAmount / inputMultiplier;
        long multipliedUnits = units * (long) multiplier;
        long multipliedPayment = paymentAmount * (long) multiplier;
        return multipliedUnits <= Integer.MAX_VALUE && multipliedPayment > 0
            && enqueuePrepaidOrder(details.getOffer(), new GenericStack(payment.getKey(), multipliedPayment),
                (int) multipliedUnits);
    }

    @Override
    public boolean canMergePatternPush(IPatternDetails patternDetails) {
        return !isBusy() && patternDetails instanceof AmadronPatternDetails;
    }

    @Override
    public int getMaxPatternPushMultiplier(IPatternDetails patternDetails, int maxMultiplier) {
        if (!canMergePatternPush(patternDetails) || maxMultiplier <= 0) {
            return 0;
        }
        return Math.min(maxMultiplier, 512 - this.jobs.size());
    }

    public boolean enqueuePrepaidOrder(AmadronOffer offer, GenericStack payment, int units) {
        if (isBusy() || !isValidPrepaidOrder(offer, payment, units)) {
            return false;
        }

        this.jobs.add(new AmadronJob(offer, payment, units));
        saveChanges();
        return true;
    }

    public boolean enqueuePrepaidOrders(List<PrepaidOrder> orders) {
        if (orders == null || orders.isEmpty() || this.jobs.size() + orders.size() > 512) {
            return false;
        }

        for (PrepaidOrder order : orders) {
            if (order == null || !isValidPrepaidOrder(order.offer(), order.payment(), order.units())) {
                return false;
            }
        }

        for (PrepaidOrder order : orders) {
            this.jobs.add(new AmadronJob(order.offer(), order.payment(), order.units()));
        }
        saveChanges();
        return true;
    }

    private boolean isValidPrepaidOrder(AmadronOffer offer, GenericStack payment, int units) {
        if (offer == null || payment == null || units <= 0 || !Pnc112AmadronBridge.hasEnoughStock(offer, units)) {
            return false;
        }

        GenericStack expectedInput = toGenericStack(offer.getInput());
        if (expectedInput == null || payment.amount() <= 0 || payment.amount() % units != 0
            || payment.amount() / units != expectedInput.amount()
            || !payment.what().equals(expectedInput.what())) {
            return false;
        }

        return true;
    }

    public void cancelAllJobs(ITextComponent message) {
        if (this.jobs.isEmpty()) {
            return;
        }

        World world = getWorld();
        BlockPos stationPos = getPos();
        for (AmadronJob job : new ArrayList<>(this.jobs)) {
            GenericStack payment = job.getPayment();
            refundPayment(payment.what(), payment.amount(), world, stationPos);
        }
        this.jobs.clear();
        saveChanges();
    }

    @Override
    public boolean isBusy() {
        return this.jobs.size() >= 512;
    }

    @Override
    public IGrid getGrid() {
        return this.getMainNode().isReady() ? this.getMainNode().getGrid() : null;
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return this.patternInventory;
    }

    @Override
    public boolean containsPattern(AEItemKey pattern) {
        if (pattern == null) {
            return false;
        }
        for (ItemStack stack : this.patternInventory) {
            if (pattern.equals(AEItemKey.of(stack))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        ItemStack iconStack = new ItemStack(APBlocks.ME_AMADRON_PROCESS_STATION_ITEM);
        return new PatternContainerGroup(AEItemKey.of(iconStack), TextComponentItemStack.of(iconStack),
            Collections.emptyList());
    }

    @Override
    public void loadTag(NBTTagCompound tag) {
        super.loadTag(tag);
        readInventory(tag, PATTERN_INVENTORY_TAG, this.patternInventory);
        readInventory(tag, UPGRADES_TAG, this.upgrades);
        this.inputInventory.readFromChildTag(tag, INPUT_INVENTORY_TAG);
        this.outputInventory.readFromChildTag(tag, OUTPUT_INVENTORY_TAG);
        readJobs(tag);
        this.priority = tag.getInteger(PRIORITY_TAG);
    }

    @Override
    public void saveAdditional(NBTTagCompound tag) {
        super.saveAdditional(tag);
        writeInventory(tag, PATTERN_INVENTORY_TAG, this.patternInventory);
        writeInventory(tag, UPGRADES_TAG, this.upgrades);
        this.inputInventory.writeToChildTag(tag, INPUT_INVENTORY_TAG);
        this.outputInventory.writeToChildTag(tag, OUTPUT_INVENTORY_TAG);
        writeJobs(tag);
        tag.setInteger(PRIORITY_TAG, this.priority);
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops) {
        super.addAdditionalDrops(drops);
        for (ItemStack stack : this.patternInventory) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        for (ItemStack stack : this.upgrades) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        addItemDrops(this.inputInventory, drops);
        addItemDrops(this.outputInventory, drops);
        for (AmadronJob job : this.jobs) {
            GenericStack payment = job.getPayment();
            payment.what().addDrops(payment.amount(), drops, getWorld(), getPos());
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.patternInventory.clear();
        this.upgrades.clear();
        this.inputInventory.clear();
        this.outputInventory.clear();
        this.jobs.clear();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability != null && (capability == AECapabilities.GENERIC_INTERNAL_INV
            || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
            || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability != null && capability == AECapabilities.GENERIC_INTERNAL_INV) {
            return (T) this.externalInventory;
        }
        if (capability != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) this.externalItemHandler;
        }
        if (capability != null && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) this.externalFluidHandler;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        saveChanges();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (inv == this.patternInventory) {
            ICraftingProvider.requestUpdate(getMainNode());
        }
    }

    @Override
    public boolean isClientSide() {
        return this.getWorld() != null && this.getWorld().isRemote;
    }

    @Override
    public void returnToMainContainer(EntityPlayer player, ISubGui subGui) {
        player.closeScreen();
    }

    @Override
    public ItemStack getMainContainerIcon() {
        return getItemFromTile();
    }

    private static final class PatternFilter implements IAEItemFilter {
        @Override
        public boolean allowInsert(InternalInventory inventory, int slot, ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() == APItems.AMADRON_PATTERN;
        }
    }

    private Object2LongMap.Entry<ae2.api.stacks.AEKey> findSinglePositivePayment(
        KeyCounter paymentOptions,
        IPatternDetails.IInput expectedInput) {
        if (paymentOptions == null || expectedInput == null) {
            return null;
        }

        Object2LongMap.Entry<ae2.api.stacks.AEKey> found = null;
        for (Object2LongMap.Entry<ae2.api.stacks.AEKey> entry : paymentOptions) {
            if (entry.getLongValue() <= 0) {
                continue;
            }
            if (found != null || !expectedInput.isValid(entry.getKey(), getWorld())) {
                return null;
            }
            found = entry;
        }
        return found;
    }

    @Nullable
    private static GenericStack toGenericStack(Object resource) {
        if (resource instanceof ItemStack) {
            return GenericStack.fromItemStack((ItemStack) resource);
        }
        if (resource instanceof FluidStack) {
            return GenericStack.fromFluidStack((FluidStack) resource);
        }
        return null;
    }

    private static void writeInventory(NBTTagCompound tag, String name, InternalInventory inventory) {
        if (inventory.isEmpty()) {
            tag.removeTag(name);
            return;
        }

        NBTTagList items = new NBTTagList();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            NBTTagCompound itemTag = new NBTTagCompound();
            itemTag.setInteger("Slot", slot);
            stack.writeToNBT(itemTag);
            ResourceLocation itemId = stack.getItem().getRegistryName();
            if (itemId != null) {
                itemTag.setString(AP_ITEM_ID_TAG, itemId.toString());
            }
            items.appendTag(itemTag);
        }
        tag.setTag(name, items);
    }

    private static void addItemDrops(GenericStackInv inventory, List<ItemStack> drops) {
        for (int slot = 0; slot < inventory.size(); slot++) {
            GenericStack stack = inventory.getStack(slot);
            if (stack != null && stack.what() instanceof AEItemKey) {
                AEItemKey itemKey = (AEItemKey) stack.what();
                long amount = stack.amount();
                while (amount > 0) {
                    int dropAmount = (int) Math.min(Math.min(amount, Integer.MAX_VALUE), itemKey.getMaxStackSize());
                    drops.add(itemKey.toStack(dropAmount));
                    amount -= dropAmount;
                }
            }
        }
    }

    private final class CombinedAmadronInventory implements GenericInternalInventory {
        @Override
        public int size() {
            return inputInventory.size() + outputInventory.size();
        }

        @Nullable
        @Override
        public GenericStack getStack(int slot) {
            SlotRef ref = resolveSlot(slot);
            return ref == null ? null : ref.inventory.getStack(ref.slot);
        }

        @Nullable
        @Override
        public AEKey getKey(int slot) {
            SlotRef ref = resolveSlot(slot);
            return ref == null ? null : ref.inventory.getKey(ref.slot);
        }

        @Override
        public long getAmount(int slot) {
            SlotRef ref = resolveSlot(slot);
            return ref == null ? 0 : ref.inventory.getAmount(ref.slot);
        }

        @Override
        public long getMaxAmount(AEKey key) {
            return Math.max(inputInventory.getMaxAmount(key), outputInventory.getMaxAmount(key));
        }

        @Override
        public long getCapacity(AEKeyType keyType) {
            return Math.max(inputInventory.getCapacity(keyType), outputInventory.getCapacity(keyType));
        }

        @Override
        public boolean canInsert() {
            return true;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public void setStack(int slot, @Nullable GenericStack newStack) {
            SlotRef ref = resolveSlot(slot);
            if (ref != null) {
                ref.inventory.setStack(ref.slot, newStack);
            }
        }

        @Override
        public boolean isSupportedType(AEKeyType type) {
            return inputInventory.isSupportedType(type) || outputInventory.isSupportedType(type);
        }

        @Override
        public boolean isAllowedIn(int slot, AEKey what) {
            SlotRef ref = resolveSlot(slot);
            return ref != null && ref.allowsInsert && ref.inventory.isAllowedIn(ref.slot, what);
        }

        @Override
        public long insert(int slot, AEKey what, long amount, Actionable mode) {
            SlotRef ref = resolveSlot(slot);
            if (ref == null || !ref.allowsInsert) {
                return 0;
            }
            if (ref.inventory == outputInventory) {
                return insertOutput(ref.slot, what, amount, mode);
            }
            return ref.inventory.insert(ref.slot, what, amount, mode);
        }

        @Override
        public long extract(int slot, AEKey what, long amount, Actionable mode) {
            SlotRef ref = resolveSlot(slot);
            if (ref == null || !ref.allowsExtract) {
                return 0;
            }
            return ref.inventory.extract(ref.slot, what, amount, mode);
        }

        @Override
        public void beginBatch() {
            inputInventory.beginBatch();
            outputInventory.beginBatch();
        }

        @Override
        public void endBatch() {
            inputInventory.endBatch();
            outputInventory.endBatch();
        }

        @Override
        public void endBatchSuppressed() {
            inputInventory.endBatchSuppressed();
            outputInventory.endBatchSuppressed();
        }

        @Override
        public void onChange() {
            inputInventory.onChange();
            outputInventory.onChange();
        }

        @Nullable
        private SlotRef resolveSlot(int slot) {
            if (slot < 0) {
                return null;
            }
            if (slot < inputInventory.size()) {
                return new SlotRef(inputInventory, slot, false, true);
            }
            int outputSlot = slot - inputInventory.size();
            if (outputSlot < outputInventory.size()) {
                return new SlotRef(outputInventory, outputSlot, true, false);
            }
            return null;
        }
    }

    private static final class SlotRef {
        private final GenericStackInv inventory;
        private final int slot;
        private final boolean allowsInsert;
        private final boolean allowsExtract;

        private SlotRef(GenericStackInv inventory, int slot, boolean allowsInsert, boolean allowsExtract) {
            this.inventory = inventory;
            this.slot = slot;
            this.allowsInsert = allowsInsert;
            this.allowsExtract = allowsExtract;
        }
    }

    private static void readInventory(NBTTagCompound tag, String name, InternalInventory inventory) {
        inventory.clear();
        if (!tag.hasKey(name, 9)) {
            return;
        }

        NBTTagList items = tag.getTagList(name, 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            int slot = itemTag.getInteger("Slot");
            if (slot < 0 || slot >= inventory.size()) {
                continue;
            }

            ItemStack stack = new ItemStack(itemTag);
            if (stack.isEmpty() && itemTag.hasKey(AP_ITEM_ID_TAG, 8)) {
                Item item = findAppliedPneumaticsItem(itemTag.getString(AP_ITEM_ID_TAG));
                if (item != null) {
                    int count = itemTag.getByte("Count");
                    ItemStack restored = new ItemStack(item, Math.max(1, count), itemTag.getShort("Damage"));
                    if (itemTag.hasKey("tag", 10)) {
                        restored.setTagCompound(itemTag.getCompoundTag("tag"));
                    }
                    stack = restored;
                }
            }
            if (!stack.isEmpty()) {
                inventory.setItemDirect(slot, stack);
            }
        }
    }

    private static Item findAppliedPneumaticsItem(String id) {
        for (Item item : APItems.getAllItems()) {
            ResourceLocation registryName = item.getRegistryName();
            if (registryName != null && registryName.toString().equals(id)) {
                return item;
            }
        }
        if (APBlocks.ME_AMADRON_PROCESS_STATION_ITEM.getRegistryName().toString().equals(id)) {
            return APBlocks.ME_AMADRON_PROCESS_STATION_ITEM;
        }
        return null;
    }

    private void writeJobs(NBTTagCompound tag) {
        if (this.jobs.isEmpty()) {
            tag.removeTag(JOBS_TAG);
            return;
        }

        NBTTagList list = new NBTTagList();
        for (AmadronJob job : this.jobs) {
            list.appendTag(job.writeToTag());
        }
        tag.setTag(JOBS_TAG, list);
    }

    private void readJobs(NBTTagCompound tag) {
        this.jobs.clear();
        if (!tag.hasKey(JOBS_TAG, 9)) {
            return;
        }

        NBTTagList list = tag.getTagList(JOBS_TAG, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            AmadronJob job = AmadronJob.readFromTag(list.getCompoundTagAt(i));
            if (job != null) {
                this.jobs.add(job);
            }
        }
    }

    private static final class DispatchBatch {
        private final AmadronJob firstJob;
        private final List<GenericStack> stagedPayments = new ArrayList<>();
        private long units;
        private int fullJobs;
        private AmadronJob splitJob;

        private DispatchBatch(AmadronJob firstJob) {
            this.firstJob = firstJob;
        }

        private static DispatchBatch empty(AmadronJob firstJob) {
            return new DispatchBatch(firstJob);
        }
    }

    public static final class PrepaidOrder {
        private final AmadronOffer offer;
        private final GenericStack payment;
        private final int units;

        public PrepaidOrder(AmadronOffer offer, GenericStack payment, int units) {
            this.offer = offer;
            this.payment = payment;
            this.units = units;
        }

        public AmadronOffer offer() {
            return this.offer;
        }

        public GenericStack payment() {
            return this.payment;
        }

        public int units() {
            return this.units;
        }
    }

    public static final class AmadronJob {
        private static final String OFFER_TAG = "offer";
        private static final String PAYMENT_TAG = "payment";
        private static final String PAYMENT_AMOUNT_TAG = "amount";
        private static final String UNITS_TAG = "units";
        private static final String PAYMENT_KIND_TAG = "kind";
        private static final String PAYMENT_ITEM_KIND = "item";
        private static final String PAYMENT_FLUID_KIND = "fluid";

        private final AmadronOffer offer;
        private final GenericStack payment;
        private final int units;

        private AmadronJob(AmadronOffer offer, GenericStack payment, int units) {
            this.offer = offer;
            this.payment = payment;
            this.units = units;
        }

        public AmadronOffer getOffer() {
            return this.offer;
        }

        public GenericStack getPayment() {
            return this.payment;
        }

        public int getUnits() {
            return this.units;
        }

        private NBTTagCompound writeToTag() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag(OFFER_TAG, Pnc112AmadronBridge.writeOfferKey(this.offer));
            NBTTagCompound paymentTag = writePaymentKey(this.payment.what());
            paymentTag.setLong(PAYMENT_AMOUNT_TAG, this.payment.amount());
            tag.setTag(PAYMENT_TAG, paymentTag);
            tag.setInteger(UNITS_TAG, this.units);
            return tag;
        }

        private static AmadronJob readFromTag(NBTTagCompound tag) {
            if (!tag.hasKey(OFFER_TAG, 10) || !tag.hasKey(PAYMENT_TAG, 10)) {
                return null;
            }
            NBTTagCompound paymentTag = tag.getCompoundTag(PAYMENT_TAG);
            AEKey paymentKey = readPaymentKey(paymentTag);
            if (paymentKey == null) {
                return null;
            }
            GenericStack payment = new GenericStack(paymentKey, paymentTag.getLong(PAYMENT_AMOUNT_TAG));
            int units = tag.hasKey(UNITS_TAG, 99) ? tag.getInteger(UNITS_TAG) : 1;
            if (units <= 0) {
                return null;
            }
            return new AmadronJob(Pnc112AmadronBridge.readOfferKey(tag.getCompoundTag(OFFER_TAG)), payment, units);
        }

        private static NBTTagCompound writePaymentKey(AEKey key) {
            NBTTagCompound tag = new NBTTagCompound();
            if (key instanceof AEItemKey) {
                tag.setString(PAYMENT_KIND_TAG, PAYMENT_ITEM_KIND);
                ((AEItemKey) key).toStack().writeToNBT(tag);
                return tag;
            }
            if (key instanceof AEFluidKey) {
                tag.setString(PAYMENT_KIND_TAG, PAYMENT_FLUID_KIND);
                ((AEFluidKey) key).toStack(1).writeToNBT(tag);
                return tag;
            }
            throw new IllegalArgumentException("Amadron payment key must be an item or fluid key");
        }

        private static AEKey readPaymentKey(NBTTagCompound tag) {
            String kind = tag.getString(PAYMENT_KIND_TAG);
            if (PAYMENT_ITEM_KIND.equals(kind)) {
                ItemStack stack = new ItemStack(tag);
                return stack.isEmpty() ? null : AEItemKey.of(stack);
            }
            if (PAYMENT_FLUID_KIND.equals(kind)) {
                FluidStack stack = FluidStack.loadFluidStackFromNBT(tag);
                return stack == null ? null : AEFluidKey.of(stack);
            }
            return null;
        }
    }
}

