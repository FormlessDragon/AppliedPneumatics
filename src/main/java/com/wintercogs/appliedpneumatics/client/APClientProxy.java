package com.wintercogs.appliedpneumatics.client;

import com.wintercogs.appliedpneumatics.client.gui.APClientGuiFactory;
import com.wintercogs.appliedpneumatics.client.me.APClientRegistration;
import com.wintercogs.appliedpneumatics.common.APCommonProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public final class APClientProxy extends APCommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        APClientRegistration.register();
    }

    @Nullable
    @Override
    public Object createClientGui(int id, EntityPlayer player, World world, int x, int y, int z) {
        return APClientGuiFactory.create(id, player, world, x, y, z);
    }
}
