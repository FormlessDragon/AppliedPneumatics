package com.wintercogs.appliedpneumatics.common.blocks.entitis;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingProvider;
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
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.util.inv.AppEngInternalInventory;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.api.GenericInv.CombinedGenericInternalInventory;
import com.wintercogs.appliedpneumatics.api.GenericInv.GenericStackInvWrapper;
import com.wintercogs.appliedpneumatics.common.init.APBlockEntities;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import com.wintercogs.appliedpneumatics.common.me.crafting.AmadronPatternDetails;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class MEAmadronProcessStationBlockEntity extends AENetworkedBlockEntity implements MenuProvider,
        IUpgradeableObject, ICraftingProvider, PatternContainer, ServerTickingBlockEntity
{

    // 样板槽 - 只允许UI存取
    private final AppEngInternalInventory patternInventory = new AppEngInternalInventory(9)
    {
        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            return super.isItemValid(slot, stack) && stack.getItem() == APItems.AMADRON_PATTERN.get();
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            ICraftingProvider.requestUpdate(getMainNode());
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

    public MEAmadronProcessStationBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(APBlockEntities.ME_AMADRON_PROCESS_STATION_BLOCK_ENTITY.get(), pos, blockState);

        getMainNode().setIdlePowerUsage(8.0) // 待机消耗
                .setFlags(GridFlags.REQUIRE_CHANNEL) // 需要频道
                .setExposedOnSides(EnumSet.allOf(Direction.class)) // 可以用于连接的方向
                .addService(ICraftingProvider.class, this);
    }

    // 注册AE节点和空气容器能力
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
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
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadTag(tag, registries);
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
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
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

    /**
     * 取当前网络的MEStorage
     */
    public @Nullable MEStorage getNetworkInventory()
    {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) return null;
        IStorageService ss = grid.getStorageService();
        return ss != null ? ss.getInventory() : null;
    }

    @Override
    public IUpgradeInventory getUpgrades()
    {
        return upgrades;
    }

    @Override
    public void serverTick()
    {
        if (level == null || level.isClientSide) return;

        if (this.needAmadronRefresh
                && this.getMainNode().isReady()
                && (level.getGameTime() & 20) == 0) // 每 20 tick轻量检查一次
        {
            if (!AmadronOfferManager.getInstance().getActiveOffers().isEmpty()) {
                ICraftingProvider.requestUpdate(this.getMainNode());
                this.needAmadronRefresh = false; // 只刷一次，随后停掉轮询
            }
        }

        if(getMainNode().isActive())
        {
            // 每 tick 把 outputInv 往 ME 塞
            this.flushOutputToME();

            // 每 200 tick 下发订单
            if (level.getGameTime() % 200 == 0)
            {
                int maxDrones = 1 + Math.max(0, this.getInstalledUpgrades(AEItems.SPEED_CARD) - 1);
                int maxUnitsPerDrone = switch (this.getInstalledUpgrades(AEItems.SPEED_CARD))
                {
                    case 1 -> 32;
                    case 2 -> 64;
                    case 3 -> 128;
                    case 4 -> 256;

                    default -> 16;
                };
                this.doJob(maxDrones, maxUnitsPerDrone);
            }
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
            return;
        }

        // 1 将job按offerId汇聚
        Map<ResourceLocation, List<Job>> withSelfByOffer = new HashMap<>();
        for (Job j : this.jobs)
        {
            withSelfByOffer.computeIfAbsent(j.offerId(), k -> new ArrayList<>()).add(j);
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

        // 3 逐 job 插入资源，凑成一批后叫一台；失败则将资源整批送回ME，job不重入队
        for (var entry : withSelfByOffer.entrySet())
        {
            final ResourceLocation offerId = entry.getKey();
            final List<Job> list = entry.getValue();

            // 如果额度用尽，整组入队，等待下一轮，同时不break，确保剩下所有订单都能入队
            if (dispatched >= maxDronesPerTick)
            {
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
                // 报价失效：本轮丢弃
                continue;
            }

            // 逐 job 尝试插入资源；成功的累到 selected
            List<Job> selected = new ArrayList<>();
            Map<AEKey, Long> insertedTotals = new HashMap<>(); // 回滚用：汇总已插入的各 AEKey 数量

            // 记录访问了多少条，用于把“未访问尾部”补回 pending
            int visited = 0;

            for (int i = 0; i < list.size() && selected.size() < maxUnitsPerOfferPerDispatch; i++)
            {
                Job job = list.get(i);
                visited++;

                GenericStack self = job.selfResource();

                long can = inputInv.insert(self.what(), self.amount(), Actionable.SIMULATE, src);
                if (can < self.amount())
                {
                    // 输入仓位不足：该 job 留下轮
                    pending.add(job);
                }
                else
                {
                    inputInv.insert(self.what(), self.amount(), Actionable.MODULATE, src);
                    selected.add(job);
                    insertedTotals.merge(self.what(), self.amount(), Long::sum);
                }
            }

            if (selected.isEmpty())
            {
                // 这一报价本轮没凑成批；visited 可能等于 list.size()，尾部为空
                // 把未访问的尾部补回 pending（如果有的话）
                for (int i = visited; i < list.size(); i++) pending.add(list.get(i));
                continue;
            }

            // 召唤无人机
            int units = selected.size();
            AmadronOffer offer = AmadronOfferManager.getInstance().getOffer(offerId);
            GlobalPos here = new GlobalPos(this.level.dimension(), this.worldPosition);

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

                // 成功派发后，把“未访问的尾部”补回 pending
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
                for (Job job : selected)
                {
                    if (job.player() != null) failByPlayer.merge(job.player(), 1, Integer::sum);
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

                // 失败时，同样把“未访问的尾部”补回 pending；selected 不重入队
                for (int i = visited; i < list.size(); i++) pending.add(list.get(i));
            }
        }

        // 4 刷一遍输出到 ME
        flushOutputs.accept(null);

        // 5 重建队列
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
    public @NotNull Component getDisplayName()
    {
        return Component.translatable("menu.title.appliedpneumatics.me_amadron_process_station");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player)
    {
        return new MEAmadronProcessStationMenu(id, inventory, this);
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops)
    {
        super.addAdditionalDrops(level, pos, drops);

        // 收集掉落物：样板槽、升级槽、两个仓库位
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

        cancelAllJobs(Component.translatable("amadron.appliedpneumatics.process_fail.block_break", worldPosition.toShortString()));
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
        if(isBusy()) return false;
        if(patternDetails instanceof AmadronPatternDetails details)
        {
            // 必然仅有一个input
            var entry = inputHolder[0].getFirstEntry();
            if(entry != null)
            {
                addJob(details.getOfferId(), new GenericStack(entry.getKey(), entry.getLongValue()));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBusy()
    {
        return jobs.size() >= 512;
    }

    public void addJob(ResourceLocation offerId, @NotNull GenericStack selfResource)
    {
        this.jobs.add(new Job(offerId, selfResource, null));
    }

    public void addJob(ResourceLocation offerId, @NotNull GenericStack selfResource, UUID player)
    {
        this.jobs.add(new Job(offerId, selfResource, player));
    }

    @Override
    public @Nullable IGrid getGrid()
    {
        return getMainNode().isReady() ? getMainNode().getGrid() : null;
    }

    @Override
    public InternalInventory getTerminalPatternInventory()
    {
        return patternInventory;
    }

    @Override
    public PatternContainerGroup getTerminalGroup()
    {
        return new PatternContainerGroup(AEItemKey.of(APBlocks.ME_AMADRON_PROCESS_STATION), APBlocks.ME_AMADRON_PROCESS_STATION.get().getName(), List.of());
    }

    /** 将所有job携带的资源送回me网络或掉落，然后给相关玩家发生一次消息 */
    public void cancelAllJobs(Component message)
    {
        // 处理 Job 自带资源：尝试塞回 ME，否则掉落
        MEStorage me = getNetworkInventory();
        IActionSource src = IActionSource.ofMachine(this);

        Set<UUID> involvedPlayers = new HashSet<>();

        List<ItemStack> drops = new ArrayList<>();

        for (Job job : jobs)
        {
            GenericStack res = job.selfResource();

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

        if(level != null && !drops.isEmpty())
        {
            for(ItemStack stack : drops)
            {
                Block.popResource(level, worldPosition, stack);
            }
        }


        // 统一给相关玩家发一次告警
        if (level != null && !involvedPlayers.isEmpty() && level.getServer() != null)
        {
            for (UUID uuid : involvedPlayers)
            {
                var p = level.getServer().getPlayerList().getPlayer(uuid);
                if (p != null) p.sendSystemMessage(message);
            }
        }

        jobs.clear();
        setChanged();
    }

    public int getJobAmount()
    {
        return jobs.size();
    }

    /** selfResource表示该Job自己携带了一部分资源，只有这部分资源被插入仓库才执行实际job */
    private record Job(ResourceLocation offerId, @NotNull GenericStack selfResource, @Nullable UUID player)
    {
        private CompoundTag writeToSubTag(HolderLookup.Provider registries)
        {
            CompoundTag tag = new CompoundTag();
            tag.putString("offer", this.offerId.toString());
            tag.put("resource", GenericStack.writeTag(registries, this.selfResource));

            if(this.player != null)
                tag.putString("player", this.player.toString());

            return tag;
        }

        public static Job readFromSubTag(CompoundTag tag, HolderLookup.Provider registries)
        {
            ResourceLocation offer = ResourceLocation.parse(tag.getString("offer"));
            GenericStack resource = GenericStack.readTag(registries, tag.getCompound("resource"));

            UUID player = null;
            if (tag.contains("player"))
                player = UUID.fromString(tag.getString("player"));

            return new Job(offer, Objects.requireNonNull(resource), player);
        }
    }
}
