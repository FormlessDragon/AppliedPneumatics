package com.wintercogs.appliedpneumatics.common.item;

import ae2.api.implementations.items.AddWirelessTerminalEvent;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.upgrades.UpgradeInventories;
import ae2.container.GuiIds;
import ae2.core.gui.locator.BaublesItemLocator;
import ae2.core.gui.locator.ItemGuiHostLocator;
import ae2.items.tools.powered.WirelessTerminalItem;
import ae2.api.upgrades.Upgrades;
import ae2.util.InteractionUtil;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.gui.APGuiIds;
import com.wintercogs.appliedpneumatics.common.gui.AmadronWirelessTerminalGuiHost;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronProcessStationTile;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class AmadronWirelessTerminalItem extends WirelessTerminalItem {
    private static final String TERMINAL_ID = "appliedpneumatics_amadron_wireless_terminal";
    private static final String AMADRON_PROCESS_POS_TAG = "amadron_process_pos";
    private static final String AMADRON_DIM_TAG = "dim";
    private static final String AMADRON_X_TAG = "x";
    private static final String AMADRON_Y_TAG = "y";
    private static final String AMADRON_Z_TAG = "z";
    private static final double POWER_CAPACITY = 16_000.0D;
    private static final double CHARGE_RATE = 800.0D;
    private static boolean terminalRegistered;

    public AmadronWirelessTerminalItem() {
        super(POWER_CAPACITY, TERMINAL_ID, GuiIds.GuiKey.WIRELESS_TERMINAL, ItemStack::new,
            AmadronWirelessTerminalGuiHost::new, "wireless_amadron_terminal", 2, false);
        registerWirelessTerminal();
    }

    private void registerWirelessTerminal() {
        if (!terminalRegistered) {
            AddWirelessTerminalEvent.register(event -> event.builder(TERMINAL_ID, this,
                                                                AmadronWirelessTerminalItem::openAmadronWirelessGui,
                                                                AmadronWirelessTerminalGuiHost::new,
                                                                ItemStack::new)
                                                            .hotkeyName("wireless_amadron_terminal")
                                                            .upgradeSlots(2)
                                                            .addTerminal());
            terminalRegistered = true;
        }
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return CHARGE_RATE + CHARGE_RATE * Upgrades.getEnergyCardMultiplier(getUpgrades(stack));
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack stack) {
        return UpgradeInventories.forItem(stack, 2, this::onUpgradesChanged);
    }

    public void toggleAmadronStationLink(ItemStack stack, int dimension, BlockPos pos) {
        AmadronStationLink current = getLinkedAmadronStation(stack);
        AmadronStationLink next = new AmadronStationLink(dimension, pos);
        NBTTagCompound tag = openNbtData(stack);
        if (next.equals(current)) {
            tag.removeTag(AMADRON_PROCESS_POS_TAG);
        } else {
            tag.setTag(AMADRON_PROCESS_POS_TAG, next.toTag());
        }
    }

    @Nullable
    public AmadronStationLink getLinkedAmadronStation(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(AMADRON_PROCESS_POS_TAG, 10)) {
            return null;
        }
        return AmadronStationLink.fromTag(tag.getCompoundTag(AMADRON_PROCESS_POS_TAG));
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
                                           float hitY, float hitZ, EnumHand hand) {
        if (!InteractionUtil.isInAlternateUseMode(player)) {
            return EnumActionResult.PASS;
        }

        ItemStack stack = player.getHeldItem(hand);
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof MEAmadronProcessStationTile)) {
            return EnumActionResult.PASS;
        }

        if (!world.isRemote) {
            AmadronStationLink oldLink = getLinkedAmadronStation(stack);
            toggleAmadronStationLink(stack, world.provider.getDimension(), pos);
            boolean linked = !new AmadronStationLink(world.provider.getDimension(), pos).equals(oldLink);
            player.sendStatusMessage(new TextComponentTranslation(linked
                ? "tooltip.appliedpneumatics.item.amadron.linked_status"
                : "tooltip.appliedpneumatics.item.amadron.unlinked_status"), true);
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess level, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void addCheckedInformation(ItemStack stack, World world, List<String> lines,
                                         ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);

        AmadronStationLink link = getLinkedAmadronStation(stack);
        if (link == null) {
            lines.add(new TextComponentTranslation("tooltip.appliedpneumatics.item.amadron.unlink")
                .getFormattedText());
        } else {
            BlockPos pos = link.pos();
            lines.add(new TextComponentTranslation("tooltip.appliedpneumatics.item.amadron.linked",
                link.dimension(), pos.getX(), pos.getY(), pos.getZ()).getFormattedText());
        }
    }

    public record AmadronStationLink(int dimension, BlockPos pos) {
        public AmadronStationLink {
            Objects.requireNonNull(pos, "pos");
        }

        private NBTTagCompound toTag() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger(AMADRON_DIM_TAG, dimension);
            tag.setInteger(AMADRON_X_TAG, pos.getX());
            tag.setInteger(AMADRON_Y_TAG, pos.getY());
            tag.setInteger(AMADRON_Z_TAG, pos.getZ());
            return tag;
        }

        private static AmadronStationLink fromTag(NBTTagCompound tag) {
            return new AmadronStationLink(tag.getInteger(AMADRON_DIM_TAG),
                new BlockPos(tag.getInteger(AMADRON_X_TAG), tag.getInteger(AMADRON_Y_TAG),
                    tag.getInteger(AMADRON_Z_TAG)));
        }
    }

    private void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        setAEMaxPower(stack, POWER_CAPACITY * (1 + Upgrades.getEnergyCardMultiplier(upgrades)));
    }

    private static boolean openAmadronWirelessGui(ae2.api.implementations.items.WirelessTerminalDefinition definition,
                                                  net.minecraft.entity.player.EntityPlayer player,
                                                  ItemGuiHostLocator locator, ItemStack stack,
                                                  boolean returningFromSubmenu) {
        Integer slot = locator.getPlayerInventorySlot();
        if (stack.isEmpty()) {
            return false;
        }
        if (slot != null) {
            player.openGui(AppliedPneumatics.INSTANCE, APGuiIds.AMADRON_WIRELESS_TERMINAL, player.world, slot, 0, 0);
            return true;
        }
        if (locator instanceof BaublesItemLocator baublesLocator) {
            player.openGui(AppliedPneumatics.INSTANCE, APGuiIds.AMADRON_WIRELESS_TERMINAL, player.world,
                -1 - baublesLocator.baubleSlot(), 0, 0);
            return true;
        }
        return false;
    }
}

