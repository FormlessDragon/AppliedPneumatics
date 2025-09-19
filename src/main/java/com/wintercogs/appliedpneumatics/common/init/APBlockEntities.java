package com.wintercogs.appliedpneumatics.common.init;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEAmadronProcessStationBlockEntity;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEPressureInterfaceBlockEntity;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.me_pressure_chamber.MEPressureChamberGlassBlockEntity;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.me_pressure_chamber.MEPressureChamberValveBlockEntity;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.me_pressure_chamber.MEPressureChamberVibrantGlassBlockEntity;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.me_pressure_chamber.MEPressureChamberWallBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class APBlockEntities
{

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AppliedPneumatics.MODID);

    public static final Supplier<BlockEntityType<MEPressureInterfaceBlockEntity>> ME_PRESSURE_INTERFACE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "me_pressure_interface_block_entity",
            () -> BlockEntityType.Builder.of(
                            MEPressureInterfaceBlockEntity::new,
                            APBlocks.ME_PRESSURE_INTERFACE_BLOCK.get()
                    )
                    .build(null)
    );

    public static final Supplier<BlockEntityType<MEPressureChamberValveBlockEntity>> ME_PRESSURE_CHAMBER_VALVE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "me_pressure_chamber_valve_block_entity",
            () -> BlockEntityType.Builder.of(
                            MEPressureChamberValveBlockEntity::new,
                            APBlocks.ME_PRESSURE_CHAMBER_VALVE.get()
                    )
                    .build(null)
    );

    // 压力室墙壁
    public static final Supplier<BlockEntityType<MEPressureChamberWallBlockEntity>> ME_PRESSURE_CHAMBER_WALL_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "me_pressure_chamber_wall_block_entity",
            () -> BlockEntityType.Builder.of(
                            MEPressureChamberWallBlockEntity::new,
                            APBlocks.ME_PRESSURE_CHAMBER_WALL.get()
                    )
                    .build(null)
    );

    // 压力室玻璃
    public static final Supplier<BlockEntityType<MEPressureChamberGlassBlockEntity>> ME_PRESSURE_CHAMBER_GLASS_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "me_pressure_chamber_glass_block_entity",
            () -> BlockEntityType.Builder.of(
                            MEPressureChamberGlassBlockEntity::new,
                            APBlocks.ME_PRESSURE_CHAMBER_GLASS.get()
                    )
                    .build(null)
    );

    // 压力室聚能玻璃
    public static final Supplier<BlockEntityType<MEPressureChamberVibrantGlassBlockEntity>> ME_PRESSURE_CHAMBER_VIBRANT_GLASS_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "me_pressure_chamber_vibrant_glass_block_entity",
            () -> BlockEntityType.Builder.of(
                            MEPressureChamberVibrantGlassBlockEntity::new,
                            APBlocks.ME_PRESSURE_CHAMBER_VIBRANT_GLASS.get()
                    )
                    .build(null)
    );

    // ME亚马龙处理站
    public static final Supplier<BlockEntityType<MEAmadronProcessStationBlockEntity>> ME_AMADRON_PROCESS_STATION_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "me_amadron_process_station_block_entity",
            () -> BlockEntityType.Builder.of(
                    MEAmadronProcessStationBlockEntity::new,
                    APBlocks.ME_AMADRON_PROCESS_STATION.get()
            ).build(null)
    );

    public static void register(IEventBus eventBus)
    {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
