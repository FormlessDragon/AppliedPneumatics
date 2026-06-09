package com.wintercogs.appliedpneumatics.common.block;

import ae2.block.AEBaseTileBlock;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.gui.APGuiIds;
import com.wintercogs.appliedpneumatics.common.tile.MEPressureInterfaceTile;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MEPressureInterfaceBlock extends AEBaseTileBlock<MEPressureInterfaceTile> {
    public MEPressureInterfaceBlock() {
        super(Material.IRON);
        setHardness(2.0F);
        setResistance(10.0F);
        setTileEntity(MEPressureInterfaceTile.class);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        MEPressureInterfaceTile tile = getTileEntity(world, pos);
        if (tile != null) {
            tile.onNeighborChanged();
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ)) {
            return true;
        }
        if (!world.isRemote) {
            player.openGui(AppliedPneumatics.INSTANCE, APGuiIds.ME_PRESSURE_INTERFACE, world,
                pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }
}

