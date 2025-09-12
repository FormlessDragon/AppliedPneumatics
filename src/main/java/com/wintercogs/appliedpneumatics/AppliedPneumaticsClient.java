package com.wintercogs.appliedpneumatics;

import com.wintercogs.appliedpneumatics.client.me.AEClientPlugin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = AppliedPneumatics.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = AppliedPneumatics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class AppliedPneumaticsClient
{
    public AppliedPneumaticsClient(ModContainer container)
    {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event)
    {
        AEClientPlugin.register();
        AppliedPneumatics.LOGGER.info("AppliedPneumatics client side setup");
    }
}
