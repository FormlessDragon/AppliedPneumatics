package com.wintercogs.appliedpneumatics.common.me;

import appeng.api.parts.RegisterPartCapabilitiesEvent;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.me.p2p.AirP2PTunnelPart;
import com.wintercogs.appliedpneumatics.common.me.p2p.HeatP2PTunnelPart;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AppliedPneumatics.MODID, bus = EventBusSubscriber.Bus.MOD)
public class APPartCapabilities
{
    @SubscribeEvent
    public static void registerPartCaps(RegisterPartCapabilitiesEvent event) {
        event.register(
                PNCCapabilities.AIR_HANDLER_MACHINE,
                (part, side) -> part.getExposedApi(),
                AirP2PTunnelPart.class
        );

        event.register(
                PNCCapabilities.HEAT_EXCHANGER_BLOCK,
                (part, direction) -> part.getExposedApi(),
                HeatP2PTunnelPart.class
        );
    }
}
