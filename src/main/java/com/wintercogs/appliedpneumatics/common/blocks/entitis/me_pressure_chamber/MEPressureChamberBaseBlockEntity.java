package com.wintercogs.appliedpneumatics.common.blocks.entitis.me_pressure_chamber;

import appeng.api.networking.*;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.MEStorage;
import com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber.FormedStructure;
import com.wintercogs.appliedpneumatics.common.me.grid.SimpleBlockNodeListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;


public class MEPressureChamberBaseBlockEntity extends BlockEntity implements IInWorldGridNodeHost, IGridMultiblock
{

    // 多方块结构
    protected @Nullable FormedStructure formedStructure = null;

    // AE部分-------------------------------------------------------------------------------------------------------------------
    // 受管节点
    protected final IManagedGridNode node; // 是世界内节点

    public MEPressureChamberBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState,
                                            ItemLike represent, String tagName, GridFlags... flags)
    {
        super(type, pos, blockState);

        node = GridHelper.createManagedNode(this, SimpleBlockNodeListener.INSTANCE)
                .setTagName(tagName) // 子标签名
                .setIdlePowerUsage(8.0) // 待机消耗
                .setFlags(Stream.concat(Arrays.stream(flags), Stream.of(GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL)).toArray(GridFlags[]::new))
                .setExposedOnSides(Collections.emptySet()) // 可以用于连接的方向
                .setInWorldNode(true)
                .setVisualRepresentation(represent)
                .addService(IGridMultiblock.class, this);
    }

    @Override
    public void clearRemoved()
    {
        super.clearRemoved();
        // 回调排队
        GridHelper.onFirstTick(this, MEPressureChamberBaseBlockEntity::onFirstTick);
    }

    private void onFirstTick()
    {
        if (level instanceof ServerLevel server)
        {
            node.create(server, worldPosition);
        }
    }

    public void setFormedStructure(@NotNull FormedStructure formedStructure)
    {
        this.formedStructure = formedStructure;
        node.setExposedOnSides(EnumSet.allOf(Direction.class));
        setChanged();
    }

    public void clearFormedStructure()
    {
        this.formedStructure = null;
        node.setExposedOnSides(Collections.emptySet());
        setChanged();
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

    @Override
    public @Nullable IGridNode getGridNode(Direction dir)
    {
        return node.isReady() ? node.getNode() : null;
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
    public Iterator<IGridNode> getMultiblockNodes()
    {
        if (level == null || formedStructure == null) {
            return Collections.emptyIterator();
        }

        // 收集这整个多方块内，所有真正承载 AE 节点的宿主
        ArrayList<IGridNode> out = new ArrayList<>();

        for (BlockPos p : formedStructure.shell()) {
            var host = GridHelper.getNodeHost(level, p);
            if (host != null) {
                var n = host.getGridNode(Direction.DOWN); // 你的实现已忽略方向，null/任意都行
                if (n != null) {
                    out.add(n);
                }
            }
        }

        return out.stream().distinct().iterator();
    }
}
