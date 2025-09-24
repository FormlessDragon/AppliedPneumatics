package com.wintercogs.appliedpneumatics.common.menu;

import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.util.ConfigMenuInventory;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEAmadronProcessStationBlockEntity;
import com.wintercogs.appliedpneumatics.common.init.APMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MEAmadronProcessStationMenu extends UpgradeableMenu<MEAmadronProcessStationBlockEntity>
{
    private static String cancelAllJobsAction = "cancel_all_jobs";

    @GuiSync(10) public int latestJobs = 0;

    // 构造：客户端
    public MEAmadronProcessStationMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, (MEAmadronProcessStationBlockEntity)
                playerInv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    // 构造：服务端
    public MEAmadronProcessStationMenu(int id, Inventory playerInv, @NotNull MEAmadronProcessStationBlockEntity host)
    {
        super(APMenus.ME_AMADRON_PROCESS_STATION_MENU.get(), id, playerInv, host);

        registerClientAction(cancelAllJobsAction, this::onJobCancel);
    }

    private void onJobCancel()
    {
        if(getBlockEntity() != null)
            getBlockEntity().cancelAllJobs();
    }

    public void senCancelJobAction()
    {
        sendClientAction(cancelAllJobsAction);
    }

    // 放除了升级槽之外的其他真实库存
    // 注：玩家槽位已经由UpgradeableMenu处理，不必再写
    @Override
    protected void setupInventorySlots()
    {
        for(int i = 0; i<getBlockEntity().getPatternInventory().size(); i++)
        {
            AppEngSlot slot = new AppEngSlot(getHost().getPatternInventory(), i);
            this.addSlot(slot, SlotSemantics.ENCODED_PATTERN);
        }
        ConfigMenuInventory inputWrapper = getBlockEntity().getInputInv().createMenuWrapper();
        for(int i = 0; i<inputWrapper.size(); i++)
        {
            AppEngSlot slot = new AppEngSlot(inputWrapper, i);
            this.addSlot(slot, SlotSemantics.MACHINE_INPUT);
        }
        ConfigMenuInventory outputWrapper = getBlockEntity().getOutputInv().createMenuWrapper();
        for(int i = 0; i<outputWrapper.size(); i++)
        {
            AppEngSlot slot = new AppEngSlot(outputWrapper, i)
            {
                @Override
                public boolean mayPlace(ItemStack stack)
                {
                    return false;
                }
            };
            this.addSlot(slot, SlotSemantics.MACHINE_OUTPUT);
        }
    }

    @Override
    public void broadcastChanges()
    {
        latestJobs = getBlockEntity() == null ? 0 : getBlockEntity().getJobAmount();
        super.broadcastChanges();
    }

    public @Nullable MEAmadronProcessStationBlockEntity getBlockEntity()
    {
        return getHost();
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return !getHost().isRemoved();
    }
}
