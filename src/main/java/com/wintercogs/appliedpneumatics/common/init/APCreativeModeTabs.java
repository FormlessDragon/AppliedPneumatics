package com.wintercogs.appliedpneumatics.common.init;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class APCreativeModeTabs
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AppliedPneumatics.MODID);

    public static final Supplier<CreativeModeTab> AP_CREATIVE_MODE_TAB = CREATIVE_MODE_TAB.register(
            "ap_creative_mode_tab",
            ()->CreativeModeTab.builder()
                    .icon(()->new ItemStack(APBlocks.ME_PRESSURE_INTERFACE_BLOCK.get()))
                    .title(Component.translatable("creativetab.appliedpneumatics.items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(APItems.AIR_CELL_1K.get());
                        output.accept(APItems.AIR_CELL_4K.get());
                        output.accept(APItems.AIR_CELL_16K.get());
                        output.accept(APItems.AIR_CELL_64K.get());
                        output.accept(APItems.AIR_CELL_256K.get());
                        // 大宗存储仅在Mega元件加载时启用
                        if(AppliedPneumatics.MEGA_CELL_LOADED)
                        {
                            output.accept(APItems.AIR_CELL_1M.get());
                            output.accept(APItems.AIR_CELL_4M.get());
                            output.accept(APItems.AIR_CELL_16M.get());
                            output.accept(APItems.AIR_CELL_64M.get());
                            output.accept(APItems.AIR_CELL_256M.get());
                        }
                        output.accept(APBlocks.ME_PRESSURE_INTERFACE_BLOCK.get());
                        output.accept(APBlocks.ME_PRESSURE_CHAMBER_VALVE.get());
                        output.accept(APBlocks.ME_PRESSURE_CHAMBER_WALL.get());
                        output.accept(APBlocks.ME_PRESSURE_CHAMBER_GLASS.get());
                        output.accept(APBlocks.ME_PRESSURE_CHAMBER_VIBRANT_GLASS.get());
                        output.accept(APItems.AIR_P2P_TUNEL.get());
                        output.accept(APItems.HEAT_P2P_TUNEL.get());
                        output.accept(APItems.VOLUME_CARD.get());
                        output.accept(APItems.SECURITY_CARD.get());
                        output.accept(APItems.VACUUM_CARD.get());
                        output.accept(APItems.AMADRON_WIRELESS_TERMINAL.get());
                        output.accept(APBlocks.ME_AMADRON_PROCESS_STATION.get());
                    })
                    .build());


    public static void register(IEventBus eventBus)
    {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
