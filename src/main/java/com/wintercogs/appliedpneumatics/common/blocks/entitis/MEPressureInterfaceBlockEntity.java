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
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.pressure.AirHandlerMachineFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * ME气压接口，作为ME网络与气动工艺的最佳通道使用
 */
public class MEPressureInterfaceBlockEntity extends BlockEntity implements IActionHost
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
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        node.saveToNBT(tag);
        tag.put("air_handler", airHandler.serializeNBT());
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

        // 空气交互
        be.airHandler.tick(be);
    }
}
