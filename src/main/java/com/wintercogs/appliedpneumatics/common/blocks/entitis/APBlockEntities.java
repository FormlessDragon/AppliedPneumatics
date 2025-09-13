package com.wintercogs.appliedpneumatics.common.blocks.entitis;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.blocks.APBlocks;
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

    public static void register(IEventBus eventBus)
    {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
