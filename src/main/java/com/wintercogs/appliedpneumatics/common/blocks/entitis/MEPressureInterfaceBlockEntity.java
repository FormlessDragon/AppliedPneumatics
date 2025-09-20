package com.wintercogs.appliedpneumatics.common.blocks.entitis;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.*;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import com.wintercogs.appliedpneumatics.common.init.APBlockEntities;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import com.wintercogs.appliedpneumatics.common.me.grid.SimpleBlockNodeListener;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import com.wintercogs.appliedpneumatics.common.menu.MEPressureInterfaceMenu;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.common.pressure.AirHandlerMachineFactory;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * ME气压接口，作为ME网络与气动工艺的最佳通道使用
 */
public class MEPressureInterfaceBlockEntity extends BlockEntity implements MenuProvider, IAirListener,
        IInWorldGridNodeHost, IUpgradeableObject, IActionHost
{

    // AE部分-------------------------------------------------------------------------------------------------------------------
    // 受管节点
    private final IManagedGridNode node = GridHelper.createManagedNode(this, SimpleBlockNodeListener.INSTANCE)
            .setTagName("me_pressure_interface") // 子标签名
            .setIdlePowerUsage(8.0) // 待机消耗
            .setFlags(GridFlags.REQUIRE_CHANNEL) // 需要频道
            .setExposedOnSides(EnumSet.allOf(Direction.class)) // 可以用于连接的方向
            .setInWorldNode(true) // 是世界内节点
            .setVisualRepresentation(APBlocks.ME_PRESSURE_INTERFACE_BLOCK);

    // 升级卡仓 5卡槽 包含四个容量卡和一个真空卡
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(APBlocks.ME_PRESSURE_INTERFACE_BLOCK, 5, this::onUpgradesChanged);

    // 气动部分--------------------------------------------------------------------------------
    private float expectedPressure = 2.0f; // 期望气压值
    private static final int BASE_VOLUME_UNIT = 1000; // 未安装任何升级下的基本容量
    private final IAirHandlerMachine airHandler =
            AirHandlerMachineFactory.getInstance().createTierTwoAirHandler(BASE_VOLUME_UNIT); // 实际空气存储
    private int lastAir = 0;

    // 充气槽位--------------------------------------------------------------------------------
    private final AppEngInternalInventory inventory = new AppEngInternalInventory(1)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    public MEPressureInterfaceBlockEntity(BlockPos pos, BlockState state)
    {
        super(APBlockEntities.ME_PRESSURE_INTERFACE_BLOCK_ENTITY.get(), pos, state);
        airHandler.setConnectableFaces(EnumSet.allOf(Direction.class)); // 设置可散发空气的面
        inventory.setFilter(new IAEItemFilter()
        {
            @Override
            public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack)
            {
                return !stack.isEmpty() && stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM) != null;
            }
        });
    }

    // 注册AE节点和空气容器能力
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                APBlockEntities.ME_PRESSURE_INTERFACE_BLOCK_ENTITY.get(),
                (be, unused) -> be
        );
        event.registerBlockEntity(
                PNCCapabilities.AIR_HANDLER_MACHINE,
                APBlockEntities.ME_PRESSURE_INTERFACE_BLOCK_ENTITY.get(),
                (be, direction) -> be.airHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                APBlockEntities.ME_PRESSURE_INTERFACE_BLOCK_ENTITY.get(),
                (be, direction) -> be.inventory.toItemHandler()
        );
    }

    public AppEngInternalInventory getInventory()
    {
        return inventory;
    }

    public IAirHandlerMachine getAirHandler()
    {
        return airHandler;
    }

    public int getVolume()
    {
        return airHandler.getVolume();
    }

    public int getMaxVolume()
    {
        return (int) (getVolume() * (double)airHandler.getDangerPressure());
    }

    public float getExpectedPressure()
    {
        return expectedPressure;
    }

    public void setExpectedPressure(float expectedPressure)
    {
        expectedPressure = Math.min(20f, expectedPressure);

        float bottomPressure = isUpgradedWith(APItems.VACUUM_CARD) ? -1f : 0f;
        expectedPressure = Math.max(bottomPressure, expectedPressure);
        this.expectedPressure = expectedPressure;
        setChanged();
    }

    @Override
    public void clearRemoved()
    {
        super.clearRemoved();
        // 回调排队
        GridHelper.onFirstTick(this, MEPressureInterfaceBlockEntity::onFirstTick);
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
        airHandler.deserializeNBT(tag.getCompound("air_handler"));
        this.inventory.readFromNBT(tag, "inv", registries);
        this.expectedPressure = tag.getFloat("expected_pressure");
        this.upgrades.readFromNBT(tag, "interface_upgrades", registries);
    }

    // load完成之后，且level被注入后
    @Override
    public void onLoad()
    {
        super.onLoad();
        onUpgradesChanged(); // 加载后刷新一次升级状态
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        node.saveToNBT(tag);
        tag.put("air_handler", airHandler.serializeNBT());
        this.inventory.writeToNBT(tag,"inv", registries);
        tag.putFloat("expected_pressure", expectedPressure);
        this.upgrades.writeToNBT(tag, "interface_upgrades", registries);
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

    /* 对外返回升级仓 */
    @Override
    public IUpgradeInventory getUpgrades()
    {
        return upgrades;
    }

    private void onUpgradesChanged()
    {
        setExpectedPressure(expectedPressure); // 刷新一次期望气压，内部会应用虚空卡检查
        // 容积升级为4的n次方（n为容积卡数量，也就是最大64倍）
        int upgradeVolumeCount = 2 * getInstalledUpgrades(APItems.VOLUME_CARD);
        airHandler.setBaseVolume(BASE_VOLUME_UNIT * (1 << upgradeVolumeCount));
        interactWithMESystem(this.level, worldPosition, getBlockState(), this); // 重设升级卡后立刻与ME系统进行一次交互

        setChanged();
    }

    // 执行ME系统以及空气容器的交互
    public static void serverTick(Level level, BlockPos pos, BlockState state, MEPressureInterfaceBlockEntity be)
    {
        if (level.isClientSide) return;

        // 与ME系统进行一次气体交换
        interactWithMESystem(level, pos, state ,be);

        // 内部物品槽交互
        ItemStack containerItem = be.inventory.getStackInSlot(0);
        if(!containerItem.isEmpty())
        {
            IAirHandler itemAirHandler = containerItem.getCapability(PNCCapabilities.AIR_HANDLER_ITEM);
            if (itemAirHandler != null) {
                float bePressure = be.airHandler.getPressure();
                float itemPressure = itemAirHandler.getPressure();
                float itemVolume = itemAirHandler.getVolume();
                float delta = Math.abs(bePressure - itemPressure) / 2.0F; // 与充电站相同的“半差”限流
                int airInItem = itemAirHandler.getAir();

                // 基础传输率
                int airPerTick = 1000;

                // 充电器几乎为 0 压且差值很小：抽掉物品里的“最后一点点气”
                if (PneumaticCraftUtils.epsilonEquals(bePressure, 0f) && delta < 0.1f)
                {
                    if (airInItem != 0)
                    {
                        itemAirHandler.addAir(-airInItem);
                    }
                }
                // 物品气压更高：物品 -> 接口
                else if (itemPressure > bePressure + 0.01F && itemPressure > 0F)
                {
                    int move = Math.min(Math.min(airPerTick, airInItem),
                            (int) (delta * be.getMaxVolume())); // 还要受接口自身体积*半差限制
                    if (move > 0)
                    {
                        itemAirHandler.addAir(-move);
                        be.airHandler.addAir(move);
                    }
                }
                // 物品气压更低且未达物品最大压：接口 -> 物品
                else if (itemPressure < bePressure - 0.01F && itemPressure < itemAirHandler.maxPressure())
                {
                    int maxAirInItem = (int) (itemAirHandler.maxPressure() * itemVolume);
                    // >15bar 时按充电站逻辑给一点加速
                    float boost = bePressure < 15f ? 1f : 1f + (bePressure - 15f) / 5f;

                    // 取速率、气体量、物品剩余空间的最小值
                    int move = Math.min(Math.min((int) (airPerTick * boost), be.airHandler.getAir()), maxAirInItem - airInItem);
                    // 加上半差限流
                    move = Math.min(move, (int) (delta * itemVolume));

                    if (move > 0)
                    {
                        itemAirHandler.addAir(move);
                        be.airHandler.addAir(-move);
                    }
                }
            }
        }

        // 与其他方块的空气交互
        be.airHandler.tick(be);

        // airHandler的回调很难覆盖所有路径
        // 这里直接使用tick比较，2个int比较极其轻量
        if(be.lastAir != be.airHandler.getAir())
        {
            be.lastAir = be.airHandler.getAir();
            be.setChanged();
        }
    }

    private static void interactWithMESystem(Level level, BlockPos pos, BlockState state, MEPressureInterfaceBlockEntity be)
    {
        // ME交互
        MEStorage storage = be.getNetworkInventory();
        if (storage != null && be.expectedPressure != be.airHandler.getPressure())
        {
            // 用double尽可能确保精度
            double difference = be.expectedPressure - be.airHandler.getPressure();
            int wantedAir = (int) (difference * be.getVolume());
            if (wantedAir > 0)
            {
                int extracted = (int) storage.extract(AirKey.INSTANCE, wantedAir, Actionable.MODULATE, IActionSource.ofMachine(be));
                if(extracted > 0)
                {
                    be.airHandler.addAir(extracted);
                }
            }
            else if(wantedAir < 0)
            {
                wantedAir = -wantedAir; // 反转数值
                int inserted = (int) storage.insert(AirKey.INSTANCE, wantedAir, Actionable.MODULATE, IActionSource.ofMachine(be));
                if(inserted > 0)
                {
                    be.airHandler.addAir(-inserted);
                }
            }
        }

    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("menu.title.appliedpneumatics.me_pressure_interface_menu");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player)
    {
        return new MEPressureInterfaceMenu(id, inventory, this);
    }

    public void dropContent()
    {
        ItemStack dropStack = inventory.getStackInSlot(0);
        if(level != null && !dropStack.isEmpty())
            Block.popResource(level, worldPosition, dropStack);
    }

    @Override
    public @Nullable IGridNode getActionableNode()
    {
        return node.isReady() ? node.getNode() : null;
    }
}
