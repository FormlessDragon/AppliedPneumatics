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
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.definitions.AEItems;
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

import java.util.*;
import java.util.function.Consumer;

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

    /** 升级卡仓，最多四个加速卡 */
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(APBlocks.ME_AMADRON_PROCESS_STATION, 4, () -> {});

    // 输入槽 - 供无人机拿取，允许能力系统对外输入输出
    private final GenericStackInv inputInv = new GenericStackInv(this::setChanged, 9);
    // 输出槽 - 缓存，一旦收到物品，直接送回AE，允许能力系统输入，不允许能力系统输出
    private final GenericStackInv outputInv = new GenericStackInv(this::setChanged, 9);

    // 用来判断亚马龙样版是否准备就绪
    private boolean needAmadronRefresh = true;

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
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        node.loadFromNBT(tag);
        patternInventory.readFromNBT(tag,"pattern_inv", registries);
        inputInv.readFromChildTag(tag,"input_inv", registries);
        outputInv.readFromChildTag(tag,"output_inv", registries);
        upgrades.readFromNBT(tag,"upgrade_inv", registries);

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
        upgrades.writeToNBT(tag,"upgrade_inv", registries);

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

    @Override
    public IUpgradeInventory getUpgrades()
    {
        return upgrades;
    }

    // 执行ME系统以及空气容器的交互
    public static void serverTick(Level level, BlockPos pos, BlockState state, MEAmadronProcessStationBlockEntity be)
    {
        if (level.isClientSide) return;

        if (be.needAmadronRefresh
                && be.node.isReady()
                && (level.getGameTime() & 20) == 0) // 每 20 tick轻量检查一次
        {
            if (!AmadronOfferManager.getInstance().getActiveOffers().isEmpty()) {
                ICraftingProvider.requestUpdate(be.node);
                be.needAmadronRefresh = false; // 只刷一次，随后停掉轮询
            }
        }

        // 每 tick 把 outputInv 往 ME 塞
        be.flushOutputToME();

        // 每 200 tick 下发订单
        if (level.getGameTime() % 200 == 0)
        {
            int maxDrones = 1 + Math.max(0, be.getInstalledUpgrades(AEItems.SPEED_CARD) - 1);
            int maxUnitsPerDrone = Integer.MAX_VALUE;
            be.doJob(maxDrones, maxUnitsPerDrone);
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
    private void doJob(int maxDronesPerTick, int maxUnitsPerOfferPerDispatch)
    {
        if (this.level == null || this.level.isClientSide) return;

        IActionSource src = IActionSource.ofMachine(this);
        if (this.jobs.isEmpty())
        {
            this.isBusy = false;
            return;
        }

        // 1 将不带资源和带资源的部分分成两组，再按offerId汇聚
        Map<ResourceLocation, List<Job>> noSelfByOffer = new HashMap<>();
        Map<ResourceLocation, List<Job>> withSelfByOffer = new HashMap<>();
        for (Job j : this.jobs)
        {
            if (j.selfResource() == null)
            {
                noSelfByOffer.computeIfAbsent(j.offerId(), k -> new ArrayList<>()).add(j);
            }
            else
            {
                withSelfByOffer.computeIfAbsent(j.offerId(), k -> new ArrayList<>()).add(j);
            }
        }

        List<Job> pending = new ArrayList<>(); // 本轮无法完成的暂存在这里，等处理结束后下一轮继续
        Set<ResourceLocation> usedThisRound = new HashSet<>(); // 本轮已经处理过的offerId放在这，用于限制召唤的无人机总数
        int dispatched = 0;

        // 2 工具：把 outputInv 刷回 ME（本轮结束也会再刷一次）
        Consumer<Void> flushOutputs = v -> {
            MEStorage me = getNetworkInventory();
            if (me == null) return;
            for (int i = 0; i < outputInv.size(); i++)
            {
                GenericStack gs = outputInv.getStack(i);
                if (gs == null) continue;
                long ins = me.insert(gs.what(), gs.amount(), Actionable.MODULATE, src);
                if (ins > 0)
                {
                    outputInv.extract(gs.what(), ins, Actionable.MODULATE, src);
                }
            }
        };

        // 3 无自带资源：优先处理，不同 offerId 最多一批；一批一次叫一台无人机，单位数做合并
        for (var e : noSelfByOffer.entrySet())
        {
            final ResourceLocation offerId = e.getKey();
            final List<Job> list = e.getValue();

            // FIX: 不再 break；额度用尽时把整组丢回 pending
            if (dispatched >= maxDronesPerTick) {
                pending.addAll(list);
                continue;
            }

            if (list.isEmpty() || usedThisRound.contains(offerId))
            {
                pending.addAll(list);
                continue;
            }

            if (!AmadronOfferManager.getInstance().isActive(offerId))
            {
                // 报价失效：整批丢弃（按你的原意保留此行为）
                continue;
            }

            int units = Math.min(list.size(), maxUnitsPerOfferPerDispatch);
            var offer = AmadronOfferManager.getInstance().getOffer(offerId);
            var here = new GlobalPos(this.level.dimension(), this.worldPosition);

            AmadroneEntity drone = retrieveOrder(AppliedPneumatics.MODID, offer, units, here, here);
            if (drone != null)
            {
                ItemStack tablet = new ItemStack(ModItems.AMADRON_TABLET.get(), 1);
                tablet.set(ModDataComponents.AMADRON_ITEM_POS, here);
                tablet.set(ModDataComponents.AMADRON_FLUID_POS, here);
                drone.setHandlingOffer(offer.getOfferId(), units, tablet, AppliedPneumatics.MODID,
                        AmadroneEntity.AmadronAction.TAKING_PAYMENT);

                // 成功：消费前 units 个 job；剩余入队
                for (int i = units; i < list.size(); i++) pending.add(list.get(i));
                usedThisRound.add(offerId);
                dispatched++;
            }
            else
            {
                // 无人机/库存暂不可用：整批入队重试
                pending.addAll(list);
            }
        }

        // 4 携带资源：按 offerId 合并，逐 job 插入资源，凑成一批后叫一台；失败则整批回滚且不重入队
        if (dispatched < maxDronesPerTick)
        {
            for (var e : withSelfByOffer.entrySet())
            {
                final ResourceLocation offerId = e.getKey();
                final List<Job> list = e.getValue();

                // FIX: 同上，不再因为额度用尽 break；而是整组入 pending
                if (dispatched >= maxDronesPerTick) {
                    pending.addAll(list);
                    continue;
                }

                if (list.isEmpty() || usedThisRound.contains(offerId))
                {
                    pending.addAll(list);
                    continue;
                }

                if (!AmadronOfferManager.getInstance().isActive(offerId))
                {
                    // 报价失效：本轮丢弃（保持原注释行为）
                    continue;
                }

                // 逐 job 尝试插入资源；成功的累到 selected
                List<Job> selected = new ArrayList<>();
                Map<AEKey, Long> insertedTotals = new HashMap<>(); // 回滚用：汇总已插入的各 AEKey 数量

                // FIX: 记录访问了多少条，用于把“未访问尾部”补回 pending
                int visited = 0;

                for (int i = 0; i < list.size() && selected.size() < maxUnitsPerOfferPerDispatch; i++)
                {
                    Job j = list.get(i);
                    visited++;

                    GenericStack self = j.selfResource();
                    if (self == null) { // 容错：当成无自带资源 job 留到下一轮
                        pending.add(j);
                        continue;
                    }

                    long can = inputInv.insert(self.what(), self.amount(), Actionable.SIMULATE, src);
                    if (can < self.amount())
                    {
                        // 输入仓位不足：该 job 留下轮
                        pending.add(j);
                    }
                    else
                    {
                        inputInv.insert(self.what(), self.amount(), Actionable.MODULATE, src);
                        selected.add(j);
                        insertedTotals.merge(self.what(), self.amount(), Long::sum);
                    }
                }

                if (selected.isEmpty())
                {
                    // 这一报价本轮没凑成批；visited 可能等于 list.size()，尾部为空
                    // FIX: 把未访问的尾部补回 pending（如果有的话）
                    for (int i = visited; i < list.size(); i++) pending.add(list.get(i));
                    continue;
                }

                int units = selected.size();
                var offer = AmadronOfferManager.getInstance().getOffer(offerId);
                var here = new GlobalPos(this.level.dimension(), this.worldPosition);

                AmadroneEntity drone = retrieveOrder(AppliedPneumatics.MODID, offer, units, here, here);
                if (drone != null)
                {
                    ItemStack tablet = new ItemStack(ModItems.AMADRON_TABLET.get(), 1);
                    tablet.set(ModDataComponents.AMADRON_ITEM_POS, here);
                    tablet.set(ModDataComponents.AMADRON_FLUID_POS, here);
                    drone.setHandlingOffer(offer.getOfferId(), units, tablet, AppliedPneumatics.MODID,
                            AmadroneEntity.AmadronAction.TAKING_PAYMENT);

                    usedThisRound.add(offerId);
                    dispatched++;

                    // FIX: 成功派发后，把“未访问的尾部”补回 pending
                    for (int i = visited; i < list.size(); i++) pending.add(list.get(i));
                }
                else
                {
                    // 无人机失败：整批回滚 + 合并消息（按玩家统计失败数量），不重入队
                    // 1) 把已插入 inputInv 的资源抽回
                    for (var it : insertedTotals.entrySet())
                    {
                        inputInv.extract(it.getKey(), it.getValue(), Actionable.MODULATE, src);
                    }

                    // 2) 再尽量塞回 ME，不行的物品才掉落
                    MEStorage me = getNetworkInventory();
                    for (var it : insertedTotals.entrySet())
                    {
                        long back = (me != null) ? me.insert(it.getKey(), it.getValue(), Actionable.MODULATE, src) : 0;
                        long remain = it.getValue() - back;
                        if (remain > 0 && it.getKey() instanceof AEItemKey itemKey)
                        {
                            int drop = (int) Math.min(Integer.MAX_VALUE, remain);
                            Block.popResource(level, worldPosition, itemKey.toStack(drop));
                        }
                    }

                    // 3) 合并失败消息：A -> B 交易失败，资源已返还，失败数量 x
                    Map<UUID, Integer> failByPlayer = new HashMap<>();
                    for (Job j : selected)
                    {
                        if (j.player() != null) failByPlayer.merge(j.player(), 1, Integer::sum);
                    }
                    if (level.getServer() != null)
                    {
                        for (var ent : failByPlayer.entrySet())
                        {
                            var p = level.getServer().getPlayerList().getPlayer(ent.getKey());
                            if (p != null)
                            {
                                p.sendSystemMessage(
                                        Component.translatable("amadron.appliedpneumatics.process_fail",
                                                offer.getInput().getName(), offer.getOutput().getName(), ent.getValue()));
                            }
                        }
                    }

                    // FIX: 失败时，同样把“未访问的尾部”补回 pending；selected 不重入队
                    for (int i = visited; i < list.size(); i++) pending.add(list.get(i));
                }
            }
        }

        // 5) 刷一遍输出到 ME
        flushOutputs.accept(null);

        // 6) 重建队列
        this.jobs.clear();
        this.jobs.addAll(pending);
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
        if (level == null || level.isClientSide) return;

        // 收集掉落物：样板槽、升级槽、两个仓库位
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < patternInventory.size(); i++) {
            ItemStack s = patternInventory.getStackInSlot(i);
            if (!s.isEmpty()) drops.add(s.copy());
        }
        for (int i = 0; i < upgrades.size(); i++) {
            ItemStack slotContent = upgrades.getStackInSlot(i);
            if (!slotContent.isEmpty()) drops.add(slotContent.copy());
        }
        Consumer<GenericStack> toDrops = (gs) -> {
            if (gs == null) return;
            if (gs.what() instanceof AEItemKey itemKey) {
                int amt = Math.clamp(gs.amount(), 0, Integer.MAX_VALUE);
                if (amt > 0) drops.add(itemKey.toStack(amt));
            }
        };
        for (int i = 0; i < inputInv.size(); i++) toDrops.accept(inputInv.getStack(i));
        for (int i = 0; i < outputInv.size(); i++) toDrops.accept(outputInv.getStack(i));

        // 处理 Job 自带资源：优先尝试塞回 ME，否则掉落
        MEStorage me = getNetworkInventory();
        IActionSource src = IActionSource.ofMachine(this);

        Set<UUID> involvedPlayers = new HashSet<>();

        for (Job job : jobs)
        {
            GenericStack res = job.selfResource();
            if (res == null) continue;

            long remain = res.amount();
            if (me != null)
            {
                long inserted = me.insert(res.what(), remain, Actionable.MODULATE, src);
                remain -= inserted;
            }

            if (remain > 0 && res.what() instanceof AEItemKey itemKey)
            {
                int drop = (int) Math.min(remain, Integer.MAX_VALUE);
                drops.add(itemKey.toStack(drop));
            }

            if (job.player() != null)
            {
                involvedPlayers.add(job.player());
            }
        }

        // 掉落所有剩余物品
        for (ItemStack s : drops)
        {
            Block.popResource(level, worldPosition, s);
        }

        // 统一给相关玩家发一次告警
        if (!involvedPlayers.isEmpty() && level.getServer() != null)
        {

            Component msg = Component.translatable("amadron.appliedpneumatics.process_fail.block_break", worldPosition.toShortString());
            for (UUID uuid : involvedPlayers)
            {
                var p = level.getServer().getPlayerList().getPlayer(uuid);
                if (p != null) p.sendSystemMessage(msg);
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
