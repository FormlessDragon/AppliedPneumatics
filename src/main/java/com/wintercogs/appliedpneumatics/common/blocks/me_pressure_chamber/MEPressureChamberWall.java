package com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber;

import com.wintercogs.appliedpneumatics.common.blocks.entitis.me_pressure_chamber.MEPressureChamberWallBlockEntity;
import com.wintercogs.appliedpneumatics.common.init.APBlockStates;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.Set;


public class MEPressureChamberWall extends MEPressureChamberBase implements EntityBlock, IChamberPartLike
{

    public MEPressureChamberWall(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(APBlockStates.WALL_STATE, APBlockStates.WallState.COMMON));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        super.createBlockStateDefinition(b);
        b.add(APBlockStates.WALL_STATE);
    }

    /* 成型/破碎时计算每面的面态并写回状态 */
    @Override
    public void onChamberFormed(ServerLevel level, BlockPos self, FormedStructure fs)
    {
        super.onChamberFormed(level, self, fs);

        final BlockState s = level.getBlockState(self);
        final Bounds b = fs.bounds();

        final boolean xmin = self.getX() == b.minX();
        final boolean xmax = self.getX() == b.maxX();
        final boolean ymin = self.getY() == b.minY();
        final boolean ymax = self.getY() == b.maxY();
        final boolean zmin = self.getZ() == b.minZ();
        final boolean zmax = self.getZ() == b.maxZ();

        // 命中在边界的“轴”数量：1=面中心，2=棱，3=角
        final int hits = (xmin || xmax ? 1 : 0)
                + (ymin || ymax ? 1 : 0)
                + (zmin || zmax ? 1 : 0);

        APBlockStates.WallState ws = APBlockStates.WallState.COMMON;

        if (hits == 1) {
            // 面中心
            ws = APBlockStates.WallState.CENTER;
        } else if (hits == 2) {
            // 棱：未在边界的轴就是“沿着哪条轴的边”
            if (!xmin && !xmax) {
                ws = APBlockStates.WallState.XEDGE;
            } else if (!ymin && !ymax) {
                ws = APBlockStates.WallState.YEDGE;
            } else {
                ws = APBlockStates.WallState.ZEDGE;
            }
        } else if (hits == 3) {
            // 角：8 个角分别标注
            if (xmin) {
                if (ymin) {
                    ws = zmin ? APBlockStates.WallState.XMIN_YMIN_ZMIN : APBlockStates.WallState.XMIN_YMIN_ZMAX;
                } else { // ymax
                    ws = zmin ? APBlockStates.WallState.XMIN_YMAX_ZMIN : APBlockStates.WallState.XMIN_YMAX_ZMAX;
                }
            } else { // xmax
                if (ymin) {
                    ws = zmin ? APBlockStates.WallState.XMAX_YMIN_ZMIN : APBlockStates.WallState.XMAX_YMIN_ZMAX;
                } else { // ymax
                    ws = zmin ? APBlockStates.WallState.XMAX_YMAX_ZMIN : APBlockStates.WallState.XMAX_YMAX_ZMAX;
                }
            }
        } // hits==0 不应出现，保持 COMMON

        level.setBlock(self, s.setValue(APBlockStates.WALL_STATE, ws), SOFT_FLAGS);
    }

    @Override
    public void onChamberBroken(ServerLevel level, BlockPos self, Set<BlockPos> oldShell)
    {
        super.onChamberBroken(level, self, oldShell);

        final BlockState s = level.getBlockState(self);
        level.setBlock(self, s.setValue(APBlockStates.WALL_STATE, APBlockStates.WallState.COMMON), SOFT_FLAGS);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return new MEPressureChamberWallBlockEntity(blockPos, blockState);
    }
}
