package com.wintercogs.appliedpneumatics.common.network;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.menu.QuickSyncable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record QuickMenuSyncPacket(CompoundTag data) implements CustomPacketPayload
{
    // 定义数据包的类型 注册用
    public static final Type<QuickMenuSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    AppliedPneumatics.MODID, "quick_sync_packet"));

    // 定义数据包的流编码方式 注册用
    public static final StreamCodec<RegistryFriendlyByteBuf, QuickMenuSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG,
                    QuickMenuSyncPacket::data,
                    QuickMenuSyncPacket::new
            );

    @Override //重写type方法，用于返回当前的TYPE
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handleClient(final QuickMenuSyncPacket packet, final IPayloadContext context)
    {
        context.enqueueWork(
                () ->
                {
                    Player player = context.player();
                    if(player.containerMenu instanceof QuickSyncable quickSyncable)
                    {
                        quickSyncable.loadQuickSync(packet.data());
                    }
                }
        );

    }

    public static void handleServer(final QuickMenuSyncPacket packet, final IPayloadContext context)
    {
        context.enqueueWork(
                () -> {
                }
        );
    }
}
