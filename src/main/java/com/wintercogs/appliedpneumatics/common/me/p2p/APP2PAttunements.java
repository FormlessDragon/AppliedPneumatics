package com.wintercogs.appliedpneumatics.common.me.p2p;

import ae2.api.features.P2PTunnelAttunement;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public final class APP2PAttunements {
    private static final ResourceLocation[] AIR_TRIGGERS = {
        new ResourceLocation("pneumaticcraft", "air_canister"),
        new ResourceLocation("pneumaticcraft", "pressure_tube")
    };
    private static final ResourceLocation[] HEAT_TRIGGERS = {
        new ResourceLocation("pneumaticcraft", "heat_sink"),
        new ResourceLocation("pneumaticcraft", "vortex_tube")
    };

    private APP2PAttunements() {
    }

    public static void register() {
        registerTagIfReady(APItems.AIR_P2P_TUNEL.getRegistryName(), AIR_TRIGGERS);
        registerTagIfReady(APItems.HEAT_P2P_TUNEL.getRegistryName(), HEAT_TRIGGERS);
    }

    private static void registerTagIfReady(ResourceLocation tunnelItemId, ResourceLocation[] triggerItemIds) {
        if (tunnelItemId == null) {
            return;
        }
        Item item = Item.REGISTRY.getObject(tunnelItemId);
        if (item == null || item == Items.AIR) {
            return;
        }
        P2PTunnelAttunement.registerAttunementTag(tunnelItemId);
        String tagName = P2PTunnelAttunement.getAttunementTag(tunnelItemId);
        for (ResourceLocation triggerItemId : triggerItemIds) {
            registerOreIfReady(tagName, triggerItemId);
        }
    }

    private static void registerOreIfReady(String oreName, ResourceLocation itemId) {
        Item item = Item.REGISTRY.getObject(itemId);
        if (item == null || item == Items.AIR) {
            return;
        }
        OreDictionary.registerOre(oreName, item);
    }
}

