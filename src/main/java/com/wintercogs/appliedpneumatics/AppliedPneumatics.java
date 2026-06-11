package com.wintercogs.appliedpneumatics;

import com.wintercogs.appliedpneumatics.common.APCommonProxy;
import com.wintercogs.appliedpneumatics.common.gui.APGuiHandler;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import com.wintercogs.appliedpneumatics.common.me.APAERegistration;
import com.wintercogs.appliedpneumatics.common.me.p2p.APP2PAttunements;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = Reference.MOD_ID,
    name = Reference.MOD_NAME,
    version = Reference.VERSION,
    acceptedMinecraftVersions = "[1.12.2]",
    dependencies = "required-after:ae2;required-after:pneumaticcraft"
)
public final class AppliedPneumatics {
    private static final String CLIENT_PROXY = "com.wintercogs.appliedpneumatics.client.APClientProxy";
    private static final String COMMON_PROXY = "com.wintercogs.appliedpneumatics.common.APCommonProxy";
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    @Mod.Instance(Reference.MOD_ID)
    public static AppliedPneumatics INSTANCE;

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    private static APCommonProxy proxy;

    public static APCommonProxy proxy() {
        return proxy;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        APAERegistration.registerEarly();
        APBlocks.preInit();
        proxy.preInit(event);
        LOGGER.info("{} loading for Cleanroom 1.12.2", Reference.MOD_NAME);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        APAERegistration.registerCommon();
        APItems.init();
        APP2PAttunements.register();
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new APGuiHandler());
        proxy.init(event);
        LOGGER.info("{} common setup complete", Reference.MOD_NAME);
    }

    public static ResourceLocation makeId(String path) {
        return new ResourceLocation(Reference.MOD_ID, path);
    }
}
