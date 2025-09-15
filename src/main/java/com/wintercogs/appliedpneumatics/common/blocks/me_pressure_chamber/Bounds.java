package com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;

import java.util.Collection;

public record Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
{
    public static Bounds of(Collection<BlockPos> ps)
    {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : ps)
        {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }
        return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public int sizeX()
    {
        return maxX - minX + 1;
    }

    public int sizeY()
    {
        return maxY - minY + 1;
    }

    public int sizeZ()
    {
        return maxZ - minZ + 1;
    }

    public AABB aabb()
    {
        return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    public CompoundTag toNBT()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("minX", minX);
        nbt.putInt("minY", minY);
        nbt.putInt("minZ", minZ);
        nbt.putInt("maxX", maxX);
        nbt.putInt("maxY", maxY);
        nbt.putInt("maxZ", maxZ);
        return nbt;
    }

    public static Bounds fromNBT(CompoundTag nbt)
    {
        return new Bounds(
                nbt.getInt("minX"), nbt.getInt("minY"), nbt.getInt("minZ"),
                nbt.getInt("maxX"), nbt.getInt("maxY"), nbt.getInt("maxZ")
        );
    }

}
