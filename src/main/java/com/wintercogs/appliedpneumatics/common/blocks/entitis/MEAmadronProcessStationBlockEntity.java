package com.wintercogs.appliedpneumatics.common.blocks.entitis;

import appeng.api.AECapabilities;
import appeng.api.networking.*;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.inv.AppEngInternalInventory;
import com.wintercogs.appliedpneumatics.api.GenericInv.CombinedGenericInternalInventory;
import com.wintercogs.appliedpneumatics.api.GenericInv.GenericStackInvWrapper;
import com.wintercogs.appliedpneumatics.common.init.APBlockEntities;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.me.grid.SimpleBlockNodeListener;
import com.wintercogs.appliedpneumatics.common.menu.MEAmadronProcessStationMenu;
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
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;

public class MEAmadronProcessStationBlockEntity extends BlockEntity implements MenuProvider,
        IInWorldGridNodeHost, IActionHost, IUpgradeableObject
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

    // 样板槽 - 只允许UI存取
    private final AppEngInternalInventory patternInventory = new AppEngInternalInventory(9)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            setChanged();
        }
    };
    // 输入槽 - 供无人机拿取，允许能力系统对外输入输出
    private final GenericStackInv inputInv = new GenericStackInv(this::setChanged, 9);
    // 输出槽 - 缓存，一旦收到物品，直接送回AE，允许能力系统输入，不允许能力系统输出
    private final GenericStackInv outputInv = new GenericStackInv(this::setChanged, 9);

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

        // 对外暴露GENERIC_INTERNAL_INV即可，AE会自动再注册物品、流体的适配器
        event.registerBlockEntity(
                AECapabilities.GENERIC_INTERNAL_INV,
                APBlockEntities.ME_AMADRON_PROCESS_STATION_BLOCK_ENTITY.get(),
                (be, direction) -> {
                    GenericStackInvWrapper inputWrapper = new GenericStackInvWrapper(be.inputInv);
                    GenericStackInvWrapper outputWrapper = new GenericStackInvWrapper(be.outputInv)
                    {
                        // 防止被无人机从中取出内容物
                        @Override
                        public boolean canExtract()
                        {
                            return false;
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
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        node.saveToNBT(tag);
        patternInventory.writeToNBT(tag,"pattern_inv", registries);
        inputInv.writeToChildTag(tag,"input_inv", registries);
        outputInv.writeToChildTag(tag,"output_inv", registries);
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
}
