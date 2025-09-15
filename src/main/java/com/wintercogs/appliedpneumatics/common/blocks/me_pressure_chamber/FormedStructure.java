package com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record FormedStructure(@NotNull Bounds bounds,
                              @NotNull Set<BlockPos> shell,
                              int innerVolume,
                              @NotNull BlockPos controllerPos,
                              int controllerCount)
{

    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("bounds", bounds.toNBT());
        nbt.put("shell", writePosSet(shell));
        nbt.putInt("innerVolume", innerVolume);
        putPos(nbt, "controllerPos", controllerPos);
        nbt.putInt("controllerCount", controllerCount);
        return nbt;
    }

    public static FormedStructure fromNBT(CompoundTag nbt) {
        Bounds b = Bounds.fromNBT(nbt.getCompound("bounds"));
        Set<BlockPos> shell = readPosSet(nbt.getList("shell", Tag.TAG_COMPOUND));
        int inner = nbt.getInt("innerVolume");
        BlockPos ctrl = getPos(nbt, "controllerPos");
        int count = nbt.getInt("controllerCount");
        return new FormedStructure(Objects.requireNonNull(b), shell, inner, ctrl, count);
    }

    // 单个 BlockPos <-> CompoundTag（X/Y/Z）
    static CompoundTag writePos(BlockPos p) {
        CompoundTag t = new CompoundTag();
        t.putInt("X", p.getX());
        t.putInt("Y", p.getY());
        t.putInt("Z", p.getZ());
        return t;
    }

    static BlockPos readPos(CompoundTag t) {
        return new BlockPos(t.getInt("X"), t.getInt("Y"), t.getInt("Z"));
    }

    // Optional（可空）单点：放入根 NBT，并用同键读回
    static void putPos(CompoundTag root, String key, BlockPos posOrNull) {
        if (posOrNull != null) root.put(key, writePos(posOrNull));
    }

    static BlockPos getPos(CompoundTag root, String key) {
        return root.contains(key, Tag.TAG_COMPOUND) ? readPos(root.getCompound(key)) : null;
    }

    // Set<BlockPos> <-> ListTag（元素为 CompoundTag）
    static ListTag writePosSet(Set<BlockPos> set) {
        ListTag list = new ListTag();
        for (BlockPos p : set) list.add(writePos(p));
        return list;
    }

    static Set<BlockPos> readPosSet(ListTag list) {
        Set<BlockPos> out = new HashSet<>(list.size());
        for (Tag tag : list)
        {
            out.add(readPos((CompoundTag) tag));
        }
        return out;
    }


}
