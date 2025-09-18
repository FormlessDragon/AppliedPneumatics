package com.wintercogs.appliedpneumatics.common.eventlistner;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 当我们需要模拟驱动器的气压爆炸时，通过此类来延迟一个tick爆炸
 */
@EventBusSubscriber(modid = AppliedPneumatics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class APDelayedBreaker
{
    private static final Set<BlockKey> QUEUE = ConcurrentHashMap.newKeySet();

    /**
     * 去重入队：同一 tick 内同一方块只炸一次
     */
    public static void breakNextTick(ResourceKey<Level> dim, BlockPos pos)
    {
        QUEUE.add(new BlockKey(dim, pos.immutable()));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e)
    {
        if (QUEUE.isEmpty()) return;

        MinecraftServer server = e.getServer();

        var todo = new ArrayList<>(QUEUE);
        QUEUE.clear();

        for (var key : todo)
        {
            var level = server.getLevel(key.dim());
            if (level == null || !level.hasChunkAt(key.pos())) continue;
            // 破坏方块
            level.destroyBlock(key.pos(), true);
        }
    }

    public record BlockKey(ResourceKey<Level> dim, BlockPos pos)
    {
    }
}
