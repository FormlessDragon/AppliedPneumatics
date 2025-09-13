package com.wintercogs.appliedpneumatics.common.init;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.network.ExpectedPressureChangePacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = AppliedPneumatics.MODID, bus = EventBusSubscriber.Bus.MOD)
public class APPackets
{
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event)
    {
        //设置当前网络版本
        final PayloadRegistrar registrar = event.registrar("1");

        // 用于从UI设定期望压力值
        registrar.playBidirectional(
                ExpectedPressureChangePacket.TYPE,
                ExpectedPressureChangePacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ExpectedPressureChangePacket::handleClient,
                        ExpectedPressureChangePacket::handleServer
                )

        );

    }
}


