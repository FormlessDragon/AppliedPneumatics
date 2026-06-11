package com.wintercogs.appliedpneumatics.common.me.air;

import ae2.api.config.Actionable;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.AEKeyType;
import ae2.api.stacks.GenericStack;
import ae2.api.stacks.KeyCounter;
import ae2.me.storage.ExternalStorageFacade;
import com.google.common.primitives.Ints;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class AirExternalStorageFacade extends ExternalStorageFacade {
    private final IAirHandler airHandler;

    public AirExternalStorageFacade(IAirHandler airHandler) {
        this.airHandler = airHandler;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nullable
    @Override
    public GenericStack getStackInSlot(int slot) {
        if (slot != 0) {
            return null;
        }
        int air = Math.max(0, this.airHandler.getAir());
        return air > 0 ? new GenericStack(AirKey.INSTANCE, air) : null;
    }

    @Override
    public AEKeyType getKeyType() {
        return AirKeyType.INSTANCE;
    }

    @Override
    protected int insertExternal(AEKey what, int amount, Actionable mode) {
        if (!(what instanceof AirKey) || amount <= 0) {
            return 0;
        }

        int currentAir = Math.max(0, this.airHandler.getAir());
        int inserted = Ints.saturatedCast(Math.min(Math.min(getRemainingCapacity(currentAir),
            Math.max(0L, (long) Integer.MAX_VALUE - currentAir)), amount));
        if (inserted > 0 && mode == Actionable.MODULATE) {
            this.airHandler.addAir(inserted);
        }
        return inserted;
    }

    @Override
    protected int extractExternal(AEKey what, int amount, Actionable mode) {
        if (!(what instanceof AirKey) || amount <= 0) {
            return 0;
        }

        int extracted = Math.min(Math.max(0, this.airHandler.getAir()), amount);
        if (extracted > 0 && mode == Actionable.MODULATE) {
            this.airHandler.addAir(-extracted);
        }
        return extracted;
    }

    @Override
    public boolean containsAnyFuzzy(Set<AEKey> keys) {
        return keys.contains(AirKey.INSTANCE);
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        long amount = Math.max(0, this.airHandler.getAir());
        if (amount > 0) {
            out.add(AirKey.INSTANCE, amount);
        }
    }

    private long getRemainingCapacity(int currentAir) {
        long maxAir = Math.max(0L, (long) (this.airHandler.getVolume() * (double) this.airHandler.getDangerPressure()));
        return Math.max(0L, maxAir - currentAir);
    }
}

