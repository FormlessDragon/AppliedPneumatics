package com.wintercogs.appliedpneumatics.common.me.air;

import ae2.api.behaviors.ContainerItemStrategy;
import ae2.api.config.Actionable;
import ae2.api.stacks.GenericStack;
import com.google.common.primitives.Ints;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import org.jetbrains.annotations.Nullable;

public final class AirContainerItemStrategy implements ContainerItemStrategy<AirKey, AirContainerItemStrategy.Context> {
    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        IPressurizable pressurizable = IPressurizable.of(stack);
        if (pressurizable == null) {
            return null;
        }

        long air = getStoredAir(stack, pressurizable);
        return air > 0 ? new GenericStack(AirKey.INSTANCE, air) : null;
    }

    @Override
    public @Nullable Context findCarriedContext(EntityPlayer player, Container container) {
        return IPressurizable.of(player.inventory.getItemStack()) == null ? null : new CarriedContext(player);
    }

    @Override
    public @Nullable Context findPlayerSlotContext(EntityPlayer player, int slot) {
        return IPressurizable.of(player.inventory.getStackInSlot(slot)) == null ? null : new PlayerInvContext(player, slot);
    }

    @Override
    public long extract(Context context, AirKey what, long amount, Actionable mode) {
        if (amount <= 0) {
            return 0;
        }

        ItemStack stack = context.getStack();
        IPressurizable pressurizable = IPressurizable.of(stack);
        if (pressurizable == null) {
            return 0;
        }

        long extracted = Math.min(amount, getStoredAir(stack, pressurizable));
        if (extracted > 0 && mode == Actionable.MODULATE) {
            ItemStack updated = stack.copy();
            pressurizable.addAir(updated, -Ints.saturatedCast(extracted));
            context.setStack(updated);
        }
        return extracted;
    }

    @Override
    public long insert(Context context, AirKey what, long amount, Actionable mode) {
        if (amount <= 0) {
            return 0;
        }

        ItemStack stack = context.getStack();
        IPressurizable pressurizable = IPressurizable.of(stack);
        if (pressurizable == null) {
            return 0;
        }

        long inserted = Math.min(amount, getRemainingCapacity(stack, pressurizable));
        if (inserted > 0 && mode == Actionable.MODULATE) {
            ItemStack updated = stack.copy();
            pressurizable.addAir(updated, Ints.saturatedCast(inserted));
            context.setStack(updated);
        }
        return inserted;
    }

    @Override
    public void playFillSound(EntityPlayer player, AirKey what) {
        player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
    }

    @Override
    public void playEmptySound(EntityPlayer player, AirKey what) {
        player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1.0F, 1.0F);
    }

    @Override
    public @Nullable GenericStack getExtractableContent(Context context) {
        return getContainedStack(context.getStack());
    }

    private static long getStoredAir(ItemStack stack, IPressurizable pressurizable) {
        return Math.max(0L, Math.round(pressurizable.getPressure(stack) * pressurizable.getVolume(stack)));
    }

    private static long getRemainingCapacity(ItemStack stack, IPressurizable pressurizable) {
        long maxAir = Math.max(0L, Math.round(pressurizable.maxPressure(stack) * pressurizable.getVolume(stack)));
        return Math.max(0L, maxAir - getStoredAir(stack, pressurizable));
    }

    interface Context {
        ItemStack getStack();

        void setStack(ItemStack stack);

        void addOverflow(ItemStack stack);
    }

    private record CarriedContext(EntityPlayer player) implements Context {
        @Override
        public ItemStack getStack() {
            return this.player.inventory.getItemStack();
        }

        @Override
        public void setStack(ItemStack stack) {
            this.player.inventory.setItemStack(stack);
        }

        @Override
        public void addOverflow(ItemStack stack) {
            if (this.player.inventory.getItemStack().isEmpty()) {
                this.player.inventory.setItemStack(stack);
            } else {
                this.player.inventory.placeItemBackInInventory(this.player.world, stack);
            }
        }
    }

    private record PlayerInvContext(EntityPlayer player, int slot) implements Context {
        @Override
        public ItemStack getStack() {
            return this.player.inventory.getStackInSlot(this.slot);
        }

        @Override
        public void setStack(ItemStack stack) {
            this.player.inventory.setInventorySlotContents(this.slot, stack);
        }

        @Override
        public void addOverflow(ItemStack stack) {
            this.player.inventory.placeItemBackInInventory(this.player.world, stack);
        }
    }
}

