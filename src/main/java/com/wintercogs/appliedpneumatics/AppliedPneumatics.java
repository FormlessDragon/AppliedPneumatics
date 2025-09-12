package com.wintercogs.appliedpneumatics;

import com.mojang.logging.LogUtils;
import com.wintercogs.appliedpneumatics.common.me.AEPlugin;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;


@Mod(AppliedPneumatics.MODID)
public class AppliedPneumatics
{
    public static final String MODID = "appliedpneumatics";
    public static final Logger LOGGER = LogUtils.getLogger();


    public AppliedPneumatics(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener((RegisterEvent event) ->{
            if(event.getRegistryKey().equals(Registries.BLOCK))
                AEPlugin.init();
        });


    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        AEPlugin.register();
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("AppliedPneumatics server side setup");
    }
}
