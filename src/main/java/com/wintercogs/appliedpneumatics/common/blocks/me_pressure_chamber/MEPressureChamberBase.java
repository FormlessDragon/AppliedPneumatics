package com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber;

import com.wintercogs.appliedpneumatics.common.tags.APBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * ME压力室方块的基类（重写版）
 * 规则：
 * - 壳体由 FRAME_TAG 标记的方块组成（一层厚度），内部必须是空气；
 * - 控制器通过 Block 是否实现 IChamberControllerLike 来识别，可放多个，但仅选“第一个”（坐标最小）作为控制器专用回调对象；
 * - 分发：所有实现 IChamberPartLike 的部件都会收到 onChamberFormed/onChamberBroken；
 *         若存在控制器，则它还会额外收到 onControlFormed/onControlBroken；
 * - 结构成型必须至少有 1 个控制器，且控制器总数 <= maxControllers。
 */
public class MEPressureChamberBase extends Block {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    private final TagKey<Block> FRAME_TAG = APBlockTags.CHAMBER_FRAME_TAG;
    private final TagKey<Block> EDGE_TAG = APBlockTags.CHAMBER_EDGE_TAG;
    private final int minEdge = 3;
    private final int maxEdge = 7;

    /** 允许的最大控制器数量（含被选中的控制器）*/
    private final int maxControllers = Integer.MAX_VALUE;

    private static final int MAX_SCAN_BLOCKS = 8192;
    protected static final int SOFT_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    /** 需要传入最大控制器数量（>=1） */
    public MEPressureChamberBase(Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false));
    }

    // 处理方块状态---------------------------------------------------------------------------------------
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FORMED);
    }

    @Override public BlockState rotate(BlockState state, Rotation rot) { return state; }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return state; }



    /** 是否为外壳方块：仅看标签。方便添加其他模组的方块 */
    private boolean isFrame(LevelReader level, BlockPos pos, BlockState state)
    {
        return state.is(FRAME_TAG) || state.is(EDGE_TAG);
    }


    /** 是否为控制器方块：仅看 Block 是否实现接口。 */
    private boolean isController(LevelReader level, BlockPos pos)
    {
        Block blk = level.getBlockState(pos).getBlock();
        return blk instanceof IChamberControllerLike;
    }


    // 扫描入口点----------------------------------------------------------------------------------------------------
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston)
    {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel && oldState.getBlock() != state.getBlock()) {
            tryScan(level, pos);
        }
    }

    @Override
    public void onRemove(@NotNull BlockState oldState, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moved)
    {
        super.onRemove(oldState, level, pos, newState, moved);
        // 仅当“方块类型改变”时扫描；属性（formed）切换不扫描，避免回环
        if (level instanceof ServerLevel && oldState.getBlock() != newState.getBlock()) {
            tryScan(level, pos);
        }
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos fromPos, boolean moving)
    {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, moving);
        tryScan(level, pos);
    }

    /* 扫描总入口 */
    protected final void tryScan(Level level, BlockPos trigger)
    {
        if (!(level instanceof ServerLevel server)) return;

        BlockPos seed = findAnyFrameSeed(server, trigger);
        if (seed == null) return;

        FormedStructure fs = detectStructure(server, seed);
        if (fs != null) {
            applyFormed(server, fs);
            dispatchFormed(server, fs);
        } else {
            Set<BlockPos> cleared = clearFormedAround(server, seed);
            if (!cleared.isEmpty()) {
                dispatchBroken(server, cleared);
            }
        }
    }

    private BlockPos findAnyFrameSeed(ServerLevel level, BlockPos start)
    {
        BlockState st = level.getBlockState(start);
        if (isFrame(level, start, st)) return start;
        for (Direction d : Direction.values()) {
            BlockPos p = start.relative(d);
            BlockState s = level.getBlockState(p);
            if (isFrame(level, p, s)) return p;
        }
        return null;
    }

    /* 结构判定（轴对齐长方体；壳体一层；内部全空气；控制器数量约束）*/
    private @Nullable FormedStructure detectStructure(ServerLevel level, BlockPos seed)
    {
        // BFS 收集连通外壳
        Set<BlockPos> shell = floodCollectFrames(level, seed, MAX_SCAN_BLOCKS);
        if (shell.isEmpty()) return null;

        // 包围盒与尺寸限制
        Bounds bounds = Bounds.of(shell);
        int sx = bounds.sizeX(), sy = bounds.sizeY(), sz = bounds.sizeZ();
        if (sx < minEdge || sy < minEdge || sz < minEdge) return null;
        if (sx > maxEdge || sy > maxEdge || sz > maxEdge) return null;

        // 校验“一层外壳完整性”
        Set<BlockPos> expectedShell = new HashSet<>();
        for (int x = bounds.minX; x <= bounds.maxX; x++) {
            for (int y = bounds.minY; y <= bounds.maxY; y++) {
                for (int z = bounds.minZ; z <= bounds.maxZ; z++) {
                    boolean onShell = (x == bounds.minX || x == bounds.maxX) ||
                            (y == bounds.minY || y == bounds.maxY) ||
                            (z == bounds.minZ || z == bounds.maxZ);
                    if (onShell) expectedShell.add(new BlockPos(x, y, z));
                }
            }
        }
        if (!shell.equals(expectedShell)) return null;

        // 逐个外壳方块做“棱/面”标签精确校验
        for (BlockPos p : expectedShell) {
            BlockState bs = level.getBlockState(p);
            boolean edge = isEdgePos(bounds, p.getX(), p.getY(), p.getZ());
            if (edge) {
                if (!bs.is(EDGE_TAG)) return null;    // 棱：必须命中 EDGE_TAG
            } else {
                if (!bs.is(FRAME_TAG)) return null;   // 面：必须命中 FRAME_TAG
            }
        }

        // 内部必须为空气
        int inner = 0;
        for (int x = bounds.minX + 1; x <= bounds.maxX - 1; x++) {
            for (int y = bounds.minY + 1; y <= bounds.maxY - 1; y++) {
                for (int z = bounds.minZ + 1; z <= bounds.maxZ - 1; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!level.isEmptyBlock(p)) return null;
                    inner++;
                }
            }
        }

        // 统计控制器，必须 ≥1 且 ≤ maxControllers；同时挑选第一个控制器送入结构体数据
        int controllerCount = 0;
        BlockPos controller = null;
        for (BlockPos p : expectedShell) {
            if (isController(level, p)) {
                controllerCount++;
                if (controller == null || p.asLong() < controller.asLong()) controller = p;
                if (controllerCount > maxControllers) return null; // 超出上限，判失败
            }
        }
        if (controllerCount == 0) return null; // 至少需要 1 个控制器

        return new FormedStructure(bounds, expectedShell, inner, controller, controllerCount);
    }

    private Set<BlockPos> floodCollectFrames(ServerLevel level, BlockPos seed, int limit)
    {
        Set<BlockPos> vis = new HashSet<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        q.add(seed); vis.add(seed);

        while (!q.isEmpty()) {
            BlockPos p = q.poll();
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (vis.contains(n)) continue;
                if (vis.size() >= limit) return Collections.emptySet();
                BlockState s = level.getBlockState(n);
                if (isFrame(level, n, s)) {
                    vis.add(n);
                    q.add(n);
                }
            }
        }
        return vis;
    }

    /* ================= 状态写入 ================= */

    private void applyFormed(ServerLevel level, FormedStructure fs)
    {
        for (BlockPos p : fs.shell()) {
            BlockState s = level.getBlockState(p);
            if (s.hasProperty(FORMED) && !s.getValue(FORMED)) {
                level.setBlock(p, s.setValue(FORMED, true), SOFT_FLAGS);
            }
        }
    }

    /** 清除与 seedOrNear 相邻的“同一团”已成型外壳，返回被清除的坐标集。 */
    private Set<BlockPos> clearFormedAround(ServerLevel level, BlockPos seedOrNear)
    {
        Set<BlockPos> vis = new HashSet<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();

        BlockState s0 = level.getBlockState(seedOrNear);
        if (s0.hasProperty(FORMED) && s0.getValue(FORMED)) {
            q.add(seedOrNear); vis.add(seedOrNear);
        } else {
            for (Direction d : Direction.values()) {
                BlockPos p = seedOrNear.relative(d);
                BlockState s = level.getBlockState(p);
                if (s.hasProperty(FORMED) && s.getValue(FORMED)) {
                    q.add(p); vis.add(p);
                }
            }
        }

        while (!q.isEmpty() && vis.size() < MAX_SCAN_BLOCKS) {
            BlockPos p = q.poll();
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (vis.contains(n)) continue;
                BlockState s = level.getBlockState(n);
                if (s.hasProperty(FORMED) && s.getValue(FORMED)) {
                    vis.add(n); q.add(n);
                }
            }
        }

        for (BlockPos p : vis) {
            BlockState s = level.getBlockState(p);
            if (s.hasProperty(FORMED) && s.getValue(FORMED)) {
                level.setBlock(p, s.setValue(FORMED, false), SOFT_FLAGS);
            }
        }
        return vis;
    }

    // 回调触发
    private void dispatchFormed(ServerLevel level, FormedStructure fs)
    {
        // 广播到所有“部件”方块
        for (BlockPos p : fs.shell()) {
            Block blk = level.getBlockState(p).getBlock();
            if (blk instanceof IChamberPartLike part) {
                part.onChamberFormed(level, p, fs);
            }
        }
        // 额外：通知选定控制器
        if (fs.controllerPos() != null) {
            Block blk = level.getBlockState(fs.controllerPos()).getBlock();
            if (blk instanceof IChamberControllerLike ctrl) {
                ctrl.onControlFormed(level, fs.controllerPos(), fs);
            }
        }
    }

    private void dispatchBroken(ServerLevel level, Set<BlockPos> oldShell)
    {
        // 广播到所有“部件”方块
        for (BlockPos p : oldShell) {
            Block blk = level.getBlockState(p).getBlock();
            if (blk instanceof IChamberPartLike part) {
                part.onChamberBroken(level, p, oldShell);
            }
        }
        // 额外：尽力找到一个控制器（破碎后仍可能存在于 oldShell）
        BlockPos ctrl = null;
        for (BlockPos p : oldShell) {
            if (isController(level, p)) {
                if (ctrl == null || p.asLong() < ctrl.asLong()) ctrl = p;
            }
        }
        if (ctrl != null) {
            Block blk = level.getBlockState(ctrl).getBlock();
            if (blk instanceof IChamberControllerLike c) {
                c.onControlBroken(level, ctrl, oldShell);
            }
        }
    }

    // 判断某外壳坐标是否位于"棱"
    private static boolean isEdgePos(Bounds b, int x, int y, int z) {
        int c = 0;
        if (x == b.minX || x == b.maxX) c++;
        if (y == b.minY || y == b.maxY) c++;
        if (z == b.minZ || z == b.maxZ) c++;
        return c >= 2; // 2=边, 3=角
    }



    // 数据存储
    public record Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        public static Bounds of(Collection<BlockPos> ps) {
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (BlockPos p : ps) {
                minX = Math.min(minX, p.getX());
                minY = Math.min(minY, p.getY());
                minZ = Math.min(minZ, p.getZ());
                maxX = Math.max(maxX, p.getX());
                maxY = Math.max(maxY, p.getY());
                maxZ = Math.max(maxZ, p.getZ());
            }
            return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
        }
        public int sizeX() { return maxX - minX + 1; }
        public int sizeY() { return maxY - minY + 1; }
        public int sizeZ() { return maxZ - minZ + 1; }
        public AABB aabb() { return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1); }
    }

    /** 结构成型信息（包含控制器位置与控制器数量） */
    public record FormedStructure(Bounds bounds,
                                  Set<BlockPos> shell,
                                  int innerVolume,
                                  BlockPos controllerPos,
                                  int controllerCount) {}

    /** 任意参与多方块的“普通部件”方块可实现（接收广播回调）。 */
    public interface IChamberPartLike
    {
        void onChamberFormed(ServerLevel level, BlockPos selfPos, MEPressureChamberBase.FormedStructure fs);
        void onChamberBroken(ServerLevel level, BlockPos selfPos, Set<BlockPos> oldShell);
    }

    /** 控制器方块可实现（除广播外，额外接收控制器专用回调）。 */
    public interface IChamberControllerLike extends IChamberPartLike
    {
        void onControlFormed(ServerLevel level, BlockPos controllerPos, MEPressureChamberBase.FormedStructure fs);
        void onControlBroken(ServerLevel level, BlockPos controllerPos, Set<BlockPos> oldShell);
    }
}
