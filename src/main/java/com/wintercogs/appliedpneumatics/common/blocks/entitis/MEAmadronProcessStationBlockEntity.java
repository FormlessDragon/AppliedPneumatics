package com.wintercogs.appliedpneumatics.common.blocks.entitis;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.inv.AppEngInternalInventory;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.api.GenericInv.CombinedGenericInternalInventory;
import com.wintercogs.appliedpneumatics.api.GenericInv.GenericStackInvWrapper;
import com.wintercogs.appliedpneumatics.common.init.APBlockEntities;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.me.crafting.AmadronPatternDetails;
import com.wintercogs.appliedpneumatics.common.me.grid.SimpleBlockNodeListener;
import com.wintercogs.appliedpneumatics.common.menu.MEAmadronProcessStationMenu;
import com.wintercogs.appliedpneumatics.util.APMath;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.amadron.AmadronUtil;
import me.desht.pneumaticcraft.common.config.subconfig.AmadronPlayerOffers;
import me.desht.pneumaticcraft.common.drone.DroneRegistry;
import me.desht.pneumaticcraft.common.entity.drone.AmadroneEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronStockUpdate;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class MEAmadronProcessStationBlockEntity extends BlockEntity implements MenuProvider,
        IInWorldGridNodeHost, IActionHost, IUpgradeableObject, ICraftingProvider, ICraftingMachine
{
    // AE部分-------------------------------------------------------------------------------------------------------------------
    // 受管节点
    private final IManagedGridNode node = GridHelper.createManagedNode(this, SimpleBlockNodeListener.INSTANCE)
            .setTagName("me_amadron_process_station_node") // 子标签名
            .setIdlePowerUsage(8.0) // 待机消耗
            .setFlags(GridFlags.REQUIRE_CHANNEL) // 需要频道
            .setExposedOnSides(EnumSet.allOf(Direction.class)) // 可以用于连接的方向
            .setInWorldNode(true) // 是世界内节点
            .setVisualRepresentation(APBlocks.ME_AMADRON_PROCESS_STATION)
            .addService(ICraftingProvider.class, this);

    // 样板槽 - 只允许UI存取
    private final AppEngInternalInventory patternInventory = new AppEngInternalInventory(9)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            ICraftingProvider.requestUpdate(node);
            setChanged();
        }
    };

    // 输入槽 - 供无人机拿取，允许能力系统对外输入输出
    private final GenericStackInv inputInv = new GenericStackInv(this::setChanged, 9);
    // 输出槽 - 缓存，一旦收到物品，直接送回AE，允许能力系统输入，不允许能力系统输出
    private final GenericStackInv outputInv = new GenericStackInv(this::setChanged, 9);

    private final List<Job> jobs = new ArrayList<>();
    private boolean isBusy = false;

    public MEAmadronProcessStationBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(APBlockEntities.ME_AMADRON_PROCESS_STATION_BLOCK_ENTITY.get(), pos, blockState);
    }

    // 注册AE节点和空气容器能力
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                APBlockEntities.ME_AMADRON_PROCESS_STATION_BLOCK_ENTITY.get(),
                (be, unused) -> be
        );

        event.registerBlockEntity(
                AECapabilities.CRAFTING_MACHINE,
                APBlockEntities.ME_AMADRON_PROCESS_STATION_BLOCK_ENTITY.get(),
                (be, unused) -> be
        );

        // 对外暴露GENERIC_INTERNAL_INV即可，AE会自动再注册物品、流体的适配器
        event.registerBlockEntity(
                AECapabilities.GENERIC_INTERNAL_INV,
                APBlockEntities.ME_AMADRON_PROCESS_STATION_BLOCK_ENTITY.get(),
                (be, direction) -> {
                    GenericStackInvWrapper inputWrapper = new GenericStackInvWrapper(be.inputInv)
                    {
                        // 防止无人机从外部塞入交易结果
                        @Override
                        public long insert(int slot, AEKey what, long amount, Actionable mode)
                        {
                            return 0;
                        }
                    };
                    GenericStackInvWrapper outputWrapper = new GenericStackInvWrapper(be.outputInv)
                    {
                        @Override
                        public long insert(int slot, AEKey what, long amount, Actionable mode)
                        {
                            // 任何意图塞入的物品，首先会尝试直接塞入ae内，剩余物塞入outputWrapper
                            MEStorage storage = be.getNetworkInventory();
                            long remaining = amount;
                            long firstInsert = 0;
                            if(storage != null)
                            {
                                firstInsert = storage.insert(what, amount, mode, IActionSource.ofMachine(be));
                                remaining = amount - firstInsert;
                                if(remaining <= 0)
                                    return amount;
                            }

                            return firstInsert + super.insert(slot, what, remaining, mode);
                        }

                        // 防止被无人机从中取出内容物
                        @Override
                        public long extract(int slot, AEKey what, long amount, Actionable mode)
                        {
                            return 0;
                        }
                    };

                    return new CombinedGenericInternalInventory(inputWrapper, outputWrapper);
                }
        );
    }

    public AppEngInternalInventory getPatternInventory()
    {
        return patternInventory;
    }

    public GenericStackInv getInputInv()
    {
        return inputInv;
    }

    public GenericStackInv getOutputInv()
    {
        return outputInv;
    }

    @Override
    public void clearRemoved()
    {
        super.clearRemoved();
        // 回调排队
        GridHelper.onFirstTick(this, MEAmadronProcessStationBlockEntity::onFirstTick);
    }

    private void onFirstTick()
    {
        if (level instanceof ServerLevel server)
        {
            node.create(server, worldPosition);
        }
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        ICraftingProvider.requestUpdate(node);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        node.loadFromNBT(tag);
        patternInventory.readFromNBT(tag,"pattern_inv", registries);
        inputInv.readFromChildTag(tag,"input_inv", registries);
        outputInv.readFromChildTag(tag,"output_inv", registries);

        this.jobs.clear();
        if (tag.contains("Jobs", Tag.TAG_LIST)) {
            ListTag jobList = tag.getList("Jobs", Tag.TAG_COMPOUND);
            for (int i = 0; i < jobList.size(); i++) {
                CompoundTag jt = jobList.getCompound(i);
                this.jobs.add(Job.readFromSubTag(jt, registries));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        node.saveToNBT(tag);
        patternInventory.writeToNBT(tag,"pattern_inv", registries);
        inputInv.writeToChildTag(tag,"input_inv", registries);
        outputInv.writeToChildTag(tag,"output_inv", registries);

        ListTag jobList = new ListTag();
        for (Job j : this.jobs) {
            jobList.add(j.writeToSubTag(registries));
        }
        tag.put("Jobs", jobList);
    }

    // 提供基础的网络同步
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries)
    {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag,registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // 卸载/移除：销毁节点，断开连接 ===

    @Override
    public void onChunkUnloaded()
    {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) node.destroy();
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        if (level != null && !level.isClientSide) node.destroy();
    }


    /**
     * 取当前网络的MEStorage
     */
    public @Nullable MEStorage getNetworkInventory()
    {
        IGrid grid = node.getGrid();
        if (grid == null) return null;
        IStorageService ss = grid.getStorageService();
        return ss != null ? ss.getInventory() : null;
    }

    @Override
    public @Nullable IGridNode getGridNode(Direction dir)
    {
        return node.isReady() ? node.getNode() : null;
    }


    // 执行ME系统以及空气容器的交互
    public static void serverTick(Level level, BlockPos pos, BlockState state, MEAmadronProcessStationBlockEntity be)
    {
        if (level.isClientSide) return;

        // 每 tick 把 outputInv 往 ME 塞
        be.flushOutputToME();

        // 每 200 tick 下发订单
        if (level.getGameTime() % 200 == 0) {
            be.doJob();
        }
    }

    private void flushOutputToME()
    {
        MEStorage me = getNetworkInventory();
        if (me == null) return;
        IActionSource src = IActionSource.ofMachine(this);

        boolean moved = false;
        for (int i = 0; i < outputInv.size(); i++)
        {
            GenericStack gs = outputInv.getStack(i);
            if (gs == null) continue;
            long ins = me.insert(gs.what(), gs.amount(), Actionable.MODULATE, src);
            if (ins > 0) {
                outputInv.extract(gs.what(), ins, Actionable.MODULATE, src);
                moved = true;
            }
        }
        if (moved) setChanged();
    }

    // 每隔一定时间触发，通知无人机物流
    private void doJob()
    {
        if (this.level == null || this.level.isClientSide) return;
        if (this.jobs.isEmpty())
        {
            this.isBusy = false;
            return;
        }

        IActionSource src = IActionSource.ofMachine(this);
        List<Job> pending = new ArrayList<>();

        for (Job job : this.jobs)
        {
            ResourceLocation offerId = job.offerId();
            // 先把该 job 的隐藏资源塞进 inputInv；塞不下就跳过到下轮
            GenericStack self = job.selfResource();
            if (self != null)
            {
                long can = inputInv.insert(self.what(), self.amount(), Actionable.SIMULATE, src);
                if (can < self.amount())
                {
                    // 输入库存暂时塞不下，下一轮再试
                    pending.add(job);
                    continue;
                }
                inputInv.insert(self.what(), self.amount(), Actionable.MODULATE, src);
            }

            // 尝试发无人机
            if (AmadronOfferManager.getInstance().isActive(offerId))
            {
                AmadronOffer offer = AmadronOfferManager.getInstance().getOffer(offerId);
                GlobalPos here = new GlobalPos(this.level.dimension(), this.worldPosition);
                AmadroneEntity drone = retrieveOrder(AppliedPneumatics.MODID, offer, 1, here, here);
                if (drone != null)
                {
                    ItemStack tablet = new ItemStack(ModItems.AMADRON_TABLET.get(), 1);
                    tablet.set(ModDataComponents.AMADRON_ITEM_POS, here);
                    tablet.set(ModDataComponents.AMADRON_FLUID_POS, here);
                    drone.setHandlingOffer(offer.getOfferId(), 1, tablet, AppliedPneumatics.MODID,
                            AmadroneEntity.AmadronAction.TAKING_PAYMENT);
                    // 下单成功，不进入 pending
                }
                else if(self != null) // 供货量不足 取回刚刚塞入的内容
                {
                    if(job.player != null)
                    {
                        Player player = level.getServer().getPlayerList().getPlayer(job.player);
                        if(player != null)
                            player.sendSystemMessage(Component.literal(offer.getInput().getName() + "->" + offer.getOutput().getName() + "的交易已无效，资源已返还。"));
                    }

                    inputInv.extract(self.what(), self.amount(), Actionable.MODULATE, src);
                    MEStorage me = getNetworkInventory();
                    long inserted = 0;
                    if(me != null)
                    {
                        inserted = me.insert(self.what(), self.amount(), Actionable.MODULATE, src);
                    }
                    if(inserted < self.amount() && self.what() instanceof AEItemKey itemKey)
                    {
                        Block.popResource(level, worldPosition, itemKey.toStack(APMath.ClampToInt(self.amount() - inserted)));
                    }
                }
            }
        }

        // 清理库存
        MEStorage me = getNetworkInventory();
        if(me != null)
        {
            // 将输出仓内容送回ME
            for (int i = 0; i < outputInv.size(); i++)
            {
                GenericStack gs = outputInv.getStack(i);
                if (gs == null) continue;
                long ins = me.insert(gs.what(), gs.amount(), Actionable.MODULATE, src);
                if (ins > 0) {
                    outputInv.extract(gs.what(), ins, Actionable.MODULATE, src);
                }
            }
        }

        this.jobs.clear();
        this.jobs.addAll(pending);
        this.isBusy = !this.jobs.isEmpty();
        setChanged();
    }

    // 取一个亚马龙无人机
    public static AmadroneEntity retrieveOrder(String playerName, AmadronOffer offer, int units, GlobalPos itemGPos, GlobalPos liquidGPos)
    {
        boolean isAmadronRestock = playerName == null;
        return offer.getInput().apply((itemStack) -> retrieveOrderItems(playerName, offer, units, itemGPos, isAmadronRestock), (fluidStack) -> retrieveOrderFluid(playerName, offer, units, liquidGPos, isAmadronRestock));
    }

    private static AmadroneEntity retrieveOrderFluid(String playerName, AmadronOffer offer, int units, GlobalPos liquidGPos, boolean isAmadronRestock) {
        if (liquidGPos != null && validateStockLevel(playerName, offer, units, isAmadronRestock)) {
            FluidStack queryingFluid = AmadronUtil.buildFluidStack(offer.getInput().getFluid(), units);
            reduceStockLevel(offer, units, isAmadronRestock);
            return (AmadroneEntity)DroneRegistry.getInstance().retrieveFluidAmazonStyle(liquidGPos, queryingFluid);
        } else {
            return null;
        }
    }

    private static AmadroneEntity retrieveOrderItems(String playerName, AmadronOffer offer, int units, GlobalPos itemGPos, boolean isAmadronRestock)
    {
        if (itemGPos != null && validateStockLevel(playerName, offer, units, isAmadronRestock)) {
            ItemStack queryingItems = offer.getInput().getItem();
            ItemStack[] stacks = AmadronUtil.buildStacks(queryingItems, units);
            if (stacks.length == 0) {
                Log.error("retrieveOrderItems: got empty itemstack list for offer {} x {} @ {}", new Object[]{units, queryingItems, itemGPos});
                return null;
            } else {
                reduceStockLevel(offer, units, isAmadronRestock);
                return (AmadroneEntity) DroneRegistry.getInstance().retrieveItemsAmazonStyle(itemGPos, stacks);
            }
        } else {
            return null;
        }
    }

    private static void reduceStockLevel(AmadronOffer offer, int units, boolean isAmadronRestock)
    {
        if (!isAmadronRestock && (offer instanceof AmadronPlayerOffer || offer.getMaxStock() >= 0))
        {
            offer.setStock(offer.getStock() - units);
            if (offer instanceof AmadronPlayerOffer)
            {
                AmadronPlayerOffers.save();
            }

            NetworkHandler.sendNonLocal(new PacketAmadronStockUpdate(offer.getOfferId(), offer.getStock()));
        }
    }

    public static boolean validateStockLevel(String playerName, AmadronOffer offer, int units, boolean isAmadronRestock)
    {
        if (!isAmadronRestock && offer.getStock() >= 0 && units > offer.getStock()) {
            Log.warning("ignoring suspicious order from player [{}] for {} x {} - only {} in stock right now!", new Object[]{playerName, units, offer, offer.getStock()});
            return false;
        } else {
            return true;
        }
    }


    @Override
    public Component getDisplayName()
    {
        return Component.translatable("menu.title.appliedpneumatics.me_amadron_process_station");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player)
    {
        return new MEAmadronProcessStationMenu(id, inventory, this);
    }

    public void dropContent()
    {
        ArrayList<ItemStack> drops = new ArrayList<>();
        // 收集物品，并掉落
        for(int i = 0; i < patternInventory.size(); i++)
        {
            ItemStack slotContent = patternInventory.getStackInSlot(i);
            if(slotContent.isEmpty()) continue;
            drops.add(slotContent.copy());
        }
        for(int i = 0; i < inputInv.size(); i++)
        {
            GenericStack slotContent = inputInv.getStack(i);
            if(slotContent != null && slotContent.what() instanceof AEItemKey itemKey)
            {
                int amount = Math.clamp(slotContent.amount(), 0, Integer.MAX_VALUE);
                if(amount > 0)
                    drops.add(itemKey.toStack(amount));
            }
        }
        for(int i = 0; i < outputInv.size(); i++)
        {
            GenericStack slotContent = outputInv.getStack(i);
            if(slotContent != null && slotContent.what() instanceof AEItemKey itemKey)
            {
                int amount = Math.clamp(slotContent.amount(), 0, Integer.MAX_VALUE);
                if(amount > 0)
                    drops.add(itemKey.toStack(amount));
            }
        }

        if(level != null && !level.isClientSide && !drops.isEmpty())
        {
            for(ItemStack stack : drops)
            {
                Block.popResource(level, worldPosition, stack);
            }
        }
    }

    @Override
    public @Nullable IGridNode getActionableNode()
    {
        return node.isReady() ? node.getNode() : null;
    }

    // ICraftProvider实现---------------------------------------------------------------------------------------
    @Override
    public List<IPatternDetails> getAvailablePatterns()
    {
        List<IPatternDetails> result = new ArrayList<>();
        for(int i = 0; i < patternInventory.size(); i++)
        {
            ItemStack stack = patternInventory.getStackInSlot(i);
            if(stack.isEmpty()) continue;
            IPatternDetails patternDetails = PatternDetailsHelper.decodePattern(stack, level);
            if(patternDetails instanceof AmadronPatternDetails)
                result.add(patternDetails);
        }
        return result;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder)
    {
        if(isBusy) return false;
        if(patternDetails instanceof AmadronPatternDetails details)
        {
            // 必然仅有一个input
            var entry = inputHolder[0].getFirstEntry();
            if(entry != null)
            {
                long inserted = inputInv.insert(entry.getKey(), entry.getLongValue(), Actionable.SIMULATE, IActionSource.ofMachine(this));
                if(inserted == entry.getLongValue()) // 能被完全插入
                {
                    inputInv.insert(entry.getKey(), entry.getLongValue(), Actionable.MODULATE, IActionSource.ofMachine(this));
                    addJob(details.getOfferId(), null);

                    return true;
                }
                else
                {
                    isBusy = true;
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isBusy()
    {
        return isBusy;
    }

    // ICraftMachine实现-----------------------------------------------------------------------------------------------
    @Override
    public PatternContainerGroup getCraftingMachineInfo()
    {
        return new PatternContainerGroup(AEItemKey.of(APBlocks.ME_AMADRON_PROCESS_STATION), Component.translatable("testa"), List.of());
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputs, Direction ejectionDirection)
    {
        if(isBusy) return false;
        if(patternDetails instanceof AmadronPatternDetails details)
        {
            // 必然仅有一个input
            var entry = inputs[0].getFirstEntry();
            if(entry != null)
            {
                long inserted = inputInv.insert(entry.getKey(), entry.getLongValue(), Actionable.SIMULATE, IActionSource.ofMachine(this));
                if(inserted == entry.getLongValue()) // 能被完全插入
                {
                    inputInv.insert(entry.getKey(), entry.getLongValue(), Actionable.MODULATE, IActionSource.ofMachine(this));
                    addJob(details.getOfferId(), null); // 静态任务不可能失败，无需传入onFailure

                    return true;
                }
                else
                {
                    isBusy = true;
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean acceptsPlans()
    {
        return !isBusy;
    }

    public void addJob(ResourceLocation offerId, @Nullable GenericStack selfResource)
    {
        this.jobs.add(new Job(offerId, selfResource, null));
    }

    public void addJob(ResourceLocation offerId, @Nullable GenericStack selfResource, UUID player)
    {
        this.jobs.add(new Job(offerId, selfResource, player));
    }

    /** selfResource表示该Job自己携带了一部分资源，只有这部分资源被插入仓库才执行实际job */
    private record Job(ResourceLocation offerId, @Nullable GenericStack selfResource, @Nullable UUID player)
    {
        private CompoundTag writeToSubTag(HolderLookup.Provider registries)
        {
            CompoundTag tag = new CompoundTag();
            tag.putString("offer", this.offerId.toString());

            if (this.selfResource != null)
                tag.put("resource", GenericStack.writeTag(registries, this.selfResource));

            if(this.player != null)
                tag.putString("player", this.player.toString());

            return tag;
        }

        public static Job readFromSubTag(CompoundTag tag, HolderLookup.Provider registries)
        {
            ResourceLocation offer = ResourceLocation.parse(tag.getString("offer"));

            GenericStack resource = null;
            if (tag.contains("resource"))
                resource = GenericStack.readTag(registries, tag.getCompound("resource"));

            UUID player = null;
            if (tag.contains("player"))
                player = UUID.fromString(tag.getString("player"));

            return new Job(offer, resource, player);
        }
    }
}
