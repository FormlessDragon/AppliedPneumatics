package com.wintercogs.appliedpneumatics.common.me.air;

import ae2.api.config.Actionable;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.GenericStack;
import ae2.me.storage.ExternalStorageFacade;
import ae2.parts.automation.HandlerStrategy;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import org.jetbrains.annotations.Nullable;

public final class AirHandlerStrategy extends HandlerStrategy<IAirHandler, GenericStack> {
    public static final AirHandlerStrategy INSTANCE = new AirHandlerStrategy();

    private AirHandlerStrategy() {
        super(AirKeyType.INSTANCE);
    }

    @Override
    public boolean isSupported(AEKey what) {
        return what instanceof AirKey;
    }

    @Override
    public ExternalStorageFacade getFacade(IAirHandler handler) {
        return new AirExternalStorageFacade(handler);
    }

    @Nullable
    @Override
    public GenericStack getStack(AEKey what, long amount) {
        return what instanceof AirKey && amount > 0 ? new GenericStack(what, amount) : null;
    }

    @Override
    public long insert(IAirHandler handler, AEKey what, long amount, Actionable mode) {
        if (!(what instanceof AirKey) || amount <= 0) {
            return 0;
        }

        int currentAir = Math.max(0, handler.getAir());
        long maxAir = Math.max(0L, (long) (handler.getVolume() * (double) handler.getDangerPressure()));
        long remainingCapacity = Math.max(0L, maxAir - currentAir);
        long remainingIntCapacity = Math.max(0L, (long) Integer.MAX_VALUE - currentAir);
        long inserted = Math.min(Math.min(remainingCapacity, remainingIntCapacity), amount);
        if (inserted > 0 && mode == Actionable.MODULATE) {
            handler.addAir(com.google.common.primitives.Ints.saturatedCast(inserted));
        }
        return inserted;
    }
}

