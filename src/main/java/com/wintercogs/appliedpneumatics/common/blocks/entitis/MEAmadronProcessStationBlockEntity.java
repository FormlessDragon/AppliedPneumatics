package com.wintercogs.appliedpneumatics.common.blocks.entitis;

import appeng.api.networking.*;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.MEStorage;
import com.wintercogs.appliedpneumatics.common.init.APBlockEntities;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.me.grid.SimpleBlockNodeListener;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class MEAmadronProcessStationBlockEntity extends BlockEntity implements MenuProvider,
        IInWorldGridNodeHost, IActionHost
{
    // AE部分-------------------------------------------------------------------------------------------------------------------
    // 受管节点
    private final IManagedGridNode node = GridHelper.createManagedNode(this, SimpleBlockNodeListener.INSTANCE)
            .setTagName("me_amadron_process_station_node") // 子标签名
            .setIdlePowerUsage(8.0) // 待机消耗
            .setFlags(GridFlags.REQUIRE_CHANNEL) // 需要频道
            .setExposedOnSides(EnumSet.allOf(Direction.class)) // 可以用于连接的方向
            .setInWorldNode(true) // 是世界内节点
            .setVisualRepresentation(APBlocks.ME_AMADRON_PROCESS_STATION);

    public MEAmadronProcessStationBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(APBlockEntities.ME_AMADRON_PROCESS_STATION_BLOCK_ENTITY.get(), pos, blockState);
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
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        node.saveToNBT(tag);
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

    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("menu.title.beyonddimensions.me_amadron_process_station");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player)
    {
        return null;
    }

    public void dropContent()
    {

    }

    @Override
    public @Nullable IGridNode getActionableNode()
    {
        return node.isReady() ? node.getNode() : null;
    }
}
