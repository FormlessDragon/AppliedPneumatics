package com.wintercogs.appliedpneumatics.common.init;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEPressureChamberValveBlockEntity;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEPressureInterfaceBlockEntity;
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

    public static void register(IEventBus eventBus)
    {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
