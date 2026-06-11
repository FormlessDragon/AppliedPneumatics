package com.wintercogs.appliedpneumatics.common.block;

import ae2.block.AEBaseTileBlock;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.gui.APGuiIds;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import com.wintercogs.appliedpneumatics.common.item.AmadronProcessUpgradeItem;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronExtendedProcessStationTile;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronProcessStationTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MEAmadronProcessStationBlock extends AEBaseTileBlock<MEAmadronProcessStationTile> {
    public MEAmadronProcessStationBlock() {
        super(Material.IRON);
        setHardness(2.0F);
        setResistance(10.0F);
        setTileEntity(MEAmadronProcessStationTile.class);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ)) {
            return true;
        }

        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty() || held.getItem() != APItems.AMADRON_PROCESS_UPGRADE) {
            if (!world.isRemote) {
                player.openGui(AppliedPneumatics.INSTANCE, APGuiIds.ME_AMADRON_PROCESS_STATION, world,
                    pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }

        MEAmadronProcessStationTile tile = this.getTileEntity(world, pos);
        if (tile == null) {
            return false;
        }

        AmadronProcessUpgradeItem upgradeItem = (AmadronProcessUpgradeItem) held.getItem();
        if (!upgradeItem.canUpgradeStation(tile)) {
            if (!world.isRemote) {
                player.sendStatusMessage(
                    new TextComponentTranslation("tooltip.appliedpneumatics.amadron_upgrade.amadron_process_busy"),
                    true);
            }
            return true;
        }

        if (world.isRemote) {
            return true;
        }

        AmadronProcessUpgradeItem.StationUpgradeResult result = upgradeItem.upgradeStation(tile,
            new AmadronProcessUpgradeItem.StationUpgradeOperation() {
                @Override
                public MEAmadronExtendedProcessStationTile replace() {
                    if (!world.setBlockState(pos, APBlocks.ME_AMADRON_EXTENDED_PROCESS_STATION_BLOCK.getDefaultState(), 3)) {
                        return null;
                    }
                    if (world.getTileEntity(pos) instanceof MEAmadronExtendedProcessStationTile target) {
                        return target;
                    }
                    return null;
                }

                @Override
                public MEAmadronProcessStationTile restore() {
                    if (!world.setBlockState(pos, APBlocks.ME_AMADRON_PROCESS_STATION_BLOCK.getDefaultState(), 3)) {
                        return null;
                    }
                    if (world.getTileEntity(pos) instanceof MEAmadronProcessStationTile restored) {
                        return restored;
                    }
                    return null;
                }
            });
        if (result == AmadronProcessUpgradeItem.StationUpgradeResult.SUCCESS) {
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1);
            }
            return true;
        }
        return result != AmadronProcessUpgradeItem.StationUpgradeResult.FAILED;
    }
}

