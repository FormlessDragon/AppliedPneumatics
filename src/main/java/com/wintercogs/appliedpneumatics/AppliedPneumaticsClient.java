package com.wintercogs.appliedpneumatics;

import com.wintercogs.appliedpneumatics.client.me.AEClientPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.AddPackFindersEvent;

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

    @SubscribeEvent
    static void onAddPackFinders(AddPackFindersEvent event)
    {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        // “相对模组 resources”的路径：resources/resourcepacks/optional_textures
        ResourceLocation id = AppliedPneumatics.makeId("resourcepacks/optional_textures");

        Component title = Component.translatable("pack." + AppliedPneumatics.MODID + ".optional_textures.title");

        // 注册：客户端资源、内建注册表、非强制、默认放底部
        event.addPackFinders(
                id,
                PackType.CLIENT_RESOURCES,
                title,
                PackSource.BUILT_IN,
                false,
                Pack.Position.BOTTOM
        );
    }
}
