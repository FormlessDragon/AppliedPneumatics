package com.wintercogs.appliedpneumatics.common.blocks.entitis;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.*;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.MEStorage;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * ME气压接口，作为ME网络与气动工艺的最佳通道使用
 */
public class MEPressureInterfaceBlockEntity extends BlockEntity implements IActionHost, MenuProvider, IAirListener
{

    // AE部分-------------------------------------------------------------------------------------------------------------------
    // 受管节点
    private final IManagedGridNode node = GridHelper.createManagedNode(this, SimpleBlockNodeListener.INSTANCE)
            .setTagName("me_pressure_interface") // 子标签名
            .setIdlePowerUsage(8.0) // 待机消耗
            .setFlags(GridFlags.REQUIRE_CHANNEL) // 需要频道
            .setExposedOnSides(EnumSet.allOf(Direction.class)) // 可以用于连接的方向
            .setInWorldNode(true); // 是世界内节点

    private final IInWorldGridNodeHost nodeHost = new IInWorldGridNodeHost()
    {
        @Override
        public @Nullable IGridNode getGridNode(@Nullable Direction side)
        {
            return node.isReady() ? node.getNode() : null;
        }
    };

    // 气动部分--------------------------------------------------------------------------------
    private float expectedPressure = 2.0f; // 期望气压值
    private static final int baseVolume = 1000; // 基本容量
    private final IAirHandlerMachine airHandler =
            AirHandlerMachineFactory.getInstance().createTierTwoAirHandler(baseVolume); // 实际空气存储
    private int lastAir = 0;

    // 充气槽位--------------------------------------------------------------------------------
    private final ItemStackHandler inventory = new ItemStackHandler(1)
    {
        // 只允许放入带气体物品
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack)
        {
            return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM) != null;
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            setChanged(); // 保存方块实体
        }
    };

    public MEPressureInterfaceBlockEntity(BlockPos pos, BlockState state)
    {
        super(APBlockEntities.ME_PRESSURE_INTERFACE_BLOCK_ENTITY.get(), pos, state);
        airHandler.setConnectableFaces(EnumSet.allOf(Direction.class)); // 设置可散发空气的面
    }

    // 注册AE节点和空气容器能力
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                APBlockEntities.ME_PRESSURE_INTERFACE_BLOCK_ENTITY.get(),
                (be, unused) -> be.asNodeHost()
        );
        event.registerBlockEntity(
                PNCCapabilities.AIR_HANDLER_MACHINE,
                APBlockEntities.ME_PRESSURE_INTERFACE_BLOCK_ENTITY.get(),
                (be, direction) -> be.airHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                APBlockEntities.ME_PRESSURE_INTERFACE_BLOCK_ENTITY.get(),
                (be, direction) -> be.inventory
        );
    }

    public IItemHandler getInventory()
    {
        return inventory;
    }

    public IAirHandlerMachine getAirHandler()
    {
        return airHandler;
    }

    public int getBaseVolume()
    {
        return baseVolume;
    }

    public int getMaxVolume()
    {
        return (int) (baseVolume * (double)airHandler.getDangerPressure());
    }

    public float getExpectedPressure()
    {
        return expectedPressure;
    }

    public void setExpectedPressure(float expectedPressure)
    {
        expectedPressure = Math.min(25f, expectedPressure);
        expectedPressure = Math.max(-1f, expectedPressure);
        this.expectedPressure = expectedPressure;
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
        this.inventory.deserializeNBT(registries,tag.getCompound("inv"));
        this.expectedPressure = tag.getFloat("expected_pressure");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        node.saveToNBT(tag);
        tag.put("air_handler", airHandler.serializeNBT());
        tag.put("inv", inventory.serializeNBT(registries));
        tag.putFloat("expected_pressure", expectedPressure);
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

    // === 5) 卸载/移除：销毁节点，断开连接 ===

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

    // 暴露给 Block Capability 的访问器
    public IInWorldGridNodeHost asNodeHost()
    {
        return nodeHost;
    }

    @Override
    public @Nullable IGridNode getActionableNode()
    {
        return node.isReady() ? node.getNode() : null;
    }

    // 执行ME系统以及空气容器的交互
    public static void serverTick(Level level, BlockPos pos, BlockState state, MEPressureInterfaceBlockEntity be)
    {
        if (level.isClientSide) return;

        // ME交互
        MEStorage storage = be.getNetworkInventory();
        if (storage != null && be.expectedPressure != be.airHandler.getPressure())
        {
            // 用double尽可能确保精度
            double difference = be.expectedPressure - be.airHandler.getPressure();
            int wantedAir = (int) (difference * baseVolume);
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

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("menu.title.beyonddimensions.me_pressure_interface_menu");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player)
    {
        return new MEPressureInterfaceMenu(id, inventory, this);
    }
}
