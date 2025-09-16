package com.wintercogs.appliedpneumatics.common.me;

import appeng.api.behaviors.*;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.stacks.AEKeyTypes;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.parts.automation.StorageExportStrategy;
import appeng.parts.automation.StorageImportStrategy;
import com.wintercogs.appliedpneumatics.common.items.APItems;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import com.wintercogs.appliedpneumatics.common.me.strategies.AirContainerItemStrategy;
import com.wintercogs.appliedpneumatics.common.me.strategies.AirExternalStorageStrategy;
import com.wintercogs.appliedpneumatics.common.me.strategies.AirHandlerStrategy;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class AEPlugin
{
    /**
     * 必须在早期注册的项目使用init
     */
    public static void init()
    {
        AEKeyTypes.register(AirKeyType.INSTANCE);
        GenericSlotCapacities.register(AirKeyType.INSTANCE, 64000L); // 作为非气压容器的普通me接口，每槽位最多64000ml空气
    }

    /**
     * 在CommonSetup中注册的放在此处
     */
    public static void register()
    {
        // 存储总线
        ExternalStorageStrategy.register(AirKeyType.INSTANCE, AirExternalStorageStrategy::new);
        // 输入总线
        StackWorldBehaviors.registerImportStrategy(AirKeyType.INSTANCE, AEPlugin::createAirImport);
        // 输出总线
        StackWorldBehaviors.registerExportStrategy(AirKeyType.INSTANCE, AEPlugin::createAirExport);
        // PickupStrategy与PlacementStrategy不予注册（空气不可能出现在世界中与破坏或者成型面板交互）

        // UI中与物品交互的逻辑
        ContainerItemStrategy.register(AirKeyType.INSTANCE, AirKey.class, new AirContainerItemStrategy());

        // p2p协调
        P2PTunnelAttunement.registerAttunementApi(APItems.AIR_P2P_TUNEL, PNCCapabilities.AIR_HANDLER_ITEM, Component.translatable("appliedpneumatics.pneumatic"));
    }

    public static StackImportStrategy createAirImport(ServerLevel level, BlockPos fromPos, Direction fromSide)
    {
        return new StorageImportStrategy<>(
            PNCCapabilities.AIR_HANDLER_MACHINE,
            AirHandlerStrategy.INSTANCE,
            level,
            fromPos,
            fromSide
        );
    }

    public static StackExportStrategy createAirExport(ServerLevel level, BlockPos fromPos, Direction fromSide)
    {
        return new StorageExportStrategy<>(
                PNCCapabilities.AIR_HANDLER_MACHINE,
                AirHandlerStrategy.INSTANCE,
                level,
                fromPos,
                fromSide
        );
    }
}
