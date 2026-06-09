package com.wintercogs.appliedpneumatics.common.block;

import ae2.block.AEBaseTileBlock;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.gui.APGuiIds;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronExtendedProcessStationTile;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MEAmadronExtendedProcessStationBlock extends AEBaseTileBlock<MEAmadronExtendedProcessStationTile> {
    public MEAmadronExtendedProcessStationBlock() {
        super(Material.IRON);
        setHardness(2.0F);
        setResistance(10.0F);
        setTileEntity(MEAmadronExtendedProcessStationTile.class);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ)) {
            return true;
        }
        if (!world.isRemote) {
            player.openGui(AppliedPneumatics.INSTANCE, APGuiIds.ME_AMADRON_PROCESS_STATION, world,
                pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }
}

