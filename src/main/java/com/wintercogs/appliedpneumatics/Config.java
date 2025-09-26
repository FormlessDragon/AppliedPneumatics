package com.wintercogs.appliedpneumatics;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = AppliedPneumatics.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ALWAYS_SHOW_EXTENDED_CONTENT = BUILDER
            .comment("总是显示联动内容，如1m~256m存储元件以及扩展亚马龙处理站，注意，这不会添加配方，只会使其出现在创造模式菜单。")
            .define("always_show_extended_content", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean alwaysShowExtendedContent;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        alwaysShowExtendedContent = ALWAYS_SHOW_EXTENDED_CONTENT.get();
    }

}
