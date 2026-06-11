package com.wintercogs.appliedpneumatics.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class APCommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
    }

    @Nullable
    public Object createClientGui(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }
}
