package com.wintercogs.appliedpneumatics.common.me.amadron;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;
import java.util.function.Supplier;

public final class Pnc112AmadronBridge {
    private Pnc112AmadronBridge() {
    }

    public enum ResourceKind {
        ITEM,
        FLUID
    }

    public static final class ResourceDescriptor {
        private final ResourceKind kind;
        private final String id;
        private final int amount;
        private final int metadata;

        private ResourceDescriptor(ResourceKind kind, String id, int amount, int metadata) {
            this.kind = kind;
            this.id = id;
            this.amount = amount;
            this.metadata = metadata;
        }

        public ResourceKind getKind() {
            return kind;
        }

        public String getId() {
            return id;
        }

        public int getAmount() {
            return amount;
        }

        public int getMetadata() {
            return metadata;
        }
    }

    public interface AmadronOrderDispatcher {
        AmadronOffer getLiveOffer(AmadronOffer offer);

        EntityLiving retrieveOrder(AmadronOffer offer, int units, World world, BlockPos stationPos);

        void setHandlingOffer(EntityLiving drone, AmadronOffer offer, int units, ItemStack tablet, String buyingPlayer);
    }

    private static final AmadronOrderDispatcher VANILLA_DISPATCHER = new AmadronOrderDispatcher() {
        @Override
        public AmadronOffer getLiveOffer(AmadronOffer offer) {
            return AmadronOfferManager.getInstance().get(offer);
        }

        @Override
        public EntityLiving retrieveOrder(AmadronOffer offer, int units, World world, BlockPos stationPos) {
            return ContainerAmadron.retrieveOrderItems(offer, units, world, stationPos, world, stationPos);
        }

        @Override
        public void setHandlingOffer(EntityLiving drone, AmadronOffer offer, int units, ItemStack tablet, String buyingPlayer) {
            ((EntityDrone) drone).setHandlingOffer(offer, units, tablet, buyingPlayer);
        }
    };

    public static NBTTagCompound writeOfferKey(AmadronOffer offer) {
        NBTTagCompound tag = new NBTTagCompound();
        offer.writeToNBT(tag);
        return tag;
    }

    public static AmadronOffer readOfferKey(NBTTagCompound tag) {
        if (tag.hasKey("inStock")) {
            return AmadronOfferCustom.loadFromNBT(tag);
        }
        return AmadronOffer.loadFromNBT(tag);
    }

    public static ResourceDescriptor describeInput(AmadronOffer offer) {
        return describeResource(offer.getInput());
    }

    public static ResourceDescriptor describeOutput(AmadronOffer offer) {
        return describeResource(offer.getOutput());
    }

    public static boolean hasEnoughStock(AmadronOffer offer, int units) {
        if (units <= 0) {
            return false;
        }
        int stock = offer.getStock();
        return stock < 0 || units <= stock;
    }

    public static boolean dispatchAmadronOrder(AmadronOffer offer, int units, World world, BlockPos stationPos,
                                               int dimensionId, String buyingPlayer) {
        return dispatchAmadronOrder(offer, units, world, stationPos, dimensionId, buyingPlayer,
            () -> new ItemStack(Itemss.AMADRON_TABLET), VANILLA_DISPATCHER);
    }

    public static boolean dispatchAmadronOrder(AmadronOffer offer, int units, World world, BlockPos stationPos,
                                               int dimensionId, String buyingPlayer, Supplier<ItemStack> tabletFactory,
                                               AmadronOrderDispatcher dispatcher) {
        if (offer == null || units <= 0 || stationPos == null || tabletFactory == null || dispatcher == null) {
            return false;
        }

        AmadronOffer liveOffer = dispatcher.getLiveOffer(offer);
        if (liveOffer == null || !hasEnoughStock(liveOffer, units)) {
            return false;
        }

        ItemStack tablet = tabletFactory.get();
        if (tablet == null || tablet.isEmpty()) {
            return false;
        }
        setTabletProviders(tablet, stationPos, dimensionId);

        EntityLiving drone = dispatcher.retrieveOrder(liveOffer, units, world, stationPos);
        if (drone == null) {
            return false;
        }

        dispatcher.setHandlingOffer(drone, liveOffer, units, tablet, Objects.toString(buyingPlayer, ""));
        return true;
    }

    public static void setTabletProviders(ItemStack tablet, BlockPos pos, int dimensionId) {
        ItemAmadronTablet.setItemProvidingLocation(tablet, pos, dimensionId);
        ItemAmadronTablet.setLiquidProvidingLocation(tablet, pos, dimensionId);
    }

    private static ResourceDescriptor describeResource(Object resource) {
        if (resource instanceof ItemStack) {
            ItemStack stack = (ItemStack) resource;
            ResourceLocation id = stack.getItem().getRegistryName();
            if (stack.isEmpty() || id == null) {
                throw new IllegalArgumentException("Amadron item resource must be a registered non-empty ItemStack");
            }
            return new ResourceDescriptor(ResourceKind.ITEM, id.toString(), stack.getCount(), stack.getMetadata());
        }
        if (resource instanceof FluidStack) {
            FluidStack stack = (FluidStack) resource;
            if (stack.getFluid() == null || stack.amount <= 0) {
                throw new IllegalArgumentException("Amadron fluid resource must be a non-empty FluidStack");
            }
            return new ResourceDescriptor(ResourceKind.FLUID, stack.getFluid().getName(), stack.amount, 0);
        }
        throw new IllegalArgumentException("Amadron resource must be an ItemStack or FluidStack");
    }
}
