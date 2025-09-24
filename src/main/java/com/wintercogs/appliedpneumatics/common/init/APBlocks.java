package com.wintercogs.appliedpneumatics.common.init;

import appeng.block.AEBaseBlock;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.blocks.MEAmadronProcessStation;
import com.wintercogs.appliedpneumatics.common.blocks.MEPressureInterfaceBlock;
import com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber.MEPressureChamberGlass;
import com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber.MEPressureChamberValve;
import com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber.MEPressureChamberVibrantGlass;
import com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber.MEPressureChamberWall;
import me.desht.pneumaticcraft.common.block.IBlockPressureChamber;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class APBlocks
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AppliedPneumatics.MODID);

    public static final DeferredBlock<MEPressureInterfaceBlock> ME_PRESSURE_INTERFACE_BLOCK = registerBlock("me_pressure_interface_block",
            ()-> new MEPressureInterfaceBlock(BlockBehaviour.Properties.of().strength(2f)));

    public static final DeferredBlock<Block> ME_PRESSURE_CHAMBER_VALVE = registerBlock("me_pressure_chamber_valve",
            () -> new MEPressureChamberValve(BlockBehaviour.Properties.of().strength(2f)));

    public static final DeferredBlock<Block> ME_PRESSURE_CHAMBER_WALL = registerBlock("me_pressure_chamber_wall",
            () -> new MEPressureChamberWall(BlockBehaviour.Properties.of().strength(2f)));

    // 注册一个类似AE聚能石英玻璃的方块
    public static final DeferredBlock<Block> ME_PRESSURE_CHAMBER_VIBRANT_GLASS = registerBlock("me_pressure_chamber_vibrant_glass",
            () -> new MEPressureChamberVibrantGlass(AEBaseBlock.glassProps().lightLevel(b -> 15).noOcclusion()
                    .isValidSpawn((blockState, blockGetter, blockPos, entityType) -> false)));

    // 注册一个类似压力室玻璃的方块
    public static final DeferredBlock<Block> ME_PRESSURE_CHAMBER_GLASS = registerBlock("me_pressure_chamber_glass",
            () -> new MEPressureChamberGlass(IBlockPressureChamber.pressureChamberBlockProps().noOcclusion()));

    // ME亚马龙处理站
    public static final DeferredBlock<MEAmadronProcessStation> ME_AMADRON_PROCESS_STATION = registerBlock("me_amadron_process_station",
            () -> new MEAmadronProcessStation(BlockBehaviour.Properties.of().strength(2f)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block)
    {
        DeferredBlock<T> toReturn = BLOCKS.register(name,block);
        registerBlockItem(name,toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block)
    {
        APItems.ITEMS.register(name,() -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus)
    {
        BLOCKS.register(eventBus);
    }
}
