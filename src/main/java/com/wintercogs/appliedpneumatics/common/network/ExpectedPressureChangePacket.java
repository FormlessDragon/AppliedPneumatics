package com.wintercogs.appliedpneumatics.common.network;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.menu.MEPressureInterfaceMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ExpectedPressureChangePacket(float changeCount) implements CustomPacketPayload
{
    // 定义数据包的类型 注册用
    public static final Type<ExpectedPressureChangePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    AppliedPneumatics.MODID, "expected_pressure_change_packet"));

    // 定义数据包的流编码方式 注册用
    public static final StreamCodec<RegistryFriendlyByteBuf, ExpectedPressureChangePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    ExpectedPressureChangePacket::changeCount,
                    ExpectedPressureChangePacket::new
            );

    @Override //重写type方法，用于返回当前的TYPE
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handleClient(final ExpectedPressureChangePacket packet, final IPayloadContext context)
    {
        context.enqueueWork(
                () -> {}
        );

    }

    public static void handleServer(final ExpectedPressureChangePacket packet, final IPayloadContext context)
    {
        context.enqueueWork(
                () -> {
                    Player player = context.player();
                    if(player.containerMenu instanceof MEPressureInterfaceMenu menu)
                    {
                        // 内部会自动限制大小，直接设定即可
                        float current = menu.getBlockEntity().getExpectedPressure();
                        menu.getBlockEntity().setExpectedPressure(current + packet.changeCount);
                    }
                }
        );
    }
}
