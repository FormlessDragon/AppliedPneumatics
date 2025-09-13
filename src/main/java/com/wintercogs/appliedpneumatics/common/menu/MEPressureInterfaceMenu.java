package com.wintercogs.appliedpneumatics.common.menu;

import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEPressureInterfaceBlockEntity;
import com.wintercogs.appliedpneumatics.common.init.APMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class MEPressureInterfaceMenu extends AbstractContainerMenu
{

    private final Player player;
    private final MEPressureInterfaceBlockEntity be;

    private int invStartIndex = 0; // 用于for起始
    private int invEndIndex = 0; // 用于for结束判断
    private int beSlotIndex = 0; // 充气槽位 精确匹配槽位

    /**
     * 客户端构造函数
     */
    public MEPressureInterfaceMenu(int id, Inventory playerInventory, FriendlyByteBuf data)
    {
        this(id, playerInventory, (MEPressureInterfaceBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos()));
    }

    /**
     * 服务端构造函数
     */
    public MEPressureInterfaceMenu(int id, Inventory playerInventory, MEPressureInterfaceBlockEntity be)
    {
        super(APMenus.ME_PRESSURE_INTERFACE_MENU.get(), id);
        this.player = playerInventory.player;
        this.be = be;

        invStartIndex = slots.size(); // 用于for起始
        // 背包和快捷栏
        for (int row = 0; row < 3; ++row)
        {
            for (int col = 0; col < 9; ++col)
            {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 123 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col)
        {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 181));
        }
        invEndIndex = slots.size(); // 用于for结束判断

        beSlotIndex = slots.size(); // 精确匹配槽位
        // 充气槽
        addSlot(new SlotItemHandler(be.getInventory(), 0, 152, 26));

    }

    public MEPressureInterfaceBlockEntity getBlockEntity()
    {
        return be;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack ret = stack.copy();

        if (invStartIndex <= slotIndex && slotIndex < invEndIndex) {
            // 从玩家背包 → 方块槽位
            if (!this.moveItemStackTo(stack, beSlotIndex, beSlotIndex + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex == beSlotIndex) {
            // 从方块槽位 → 玩家背包
            if (!this.moveItemStackTo(stack, invStartIndex, invEndIndex, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // 非预期范围，兜底丢回玩家背包
            if (!this.moveItemStackTo(stack, invStartIndex, invEndIndex, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        // 按常规容器实现，触发取出回调
        slot.onTake(player, stack);

        return ret;
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return be != null && !be.isRemoved();
    }
}
