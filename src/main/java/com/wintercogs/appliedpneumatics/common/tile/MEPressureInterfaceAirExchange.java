package com.wintercogs.appliedpneumatics.common.tile;

import ae2.api.config.Actionable;
import ae2.api.config.PowerMultiplier;
import ae2.api.networking.energy.IEnergySource;
import ae2.api.networking.security.IActionSource;
import ae2.api.storage.MEStorage;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;

public final class MEPressureInterfaceAirExchange {
    private static final double AE_ENERGY_COST_PER_AIR = 1.25D;

    private MEPressureInterfaceAirExchange() {
    }

    public static int exchange(int interfaceVolume, int interfaceAir, float targetPressure,
                               MEStorage storage, IEnergySource energy, IActionSource source) {
        if (interfaceVolume <= 0 || storage == null || energy == null || source == null) {
            return 0;
        }

        float currentPressure = (float) interfaceAir / interfaceVolume;
        int wantedAir = (int) ((targetPressure - currentPressure) * interfaceVolume);
        int demand = Math.abs(wantedAir);
        if (demand <= 0) {
            return 0;
        }

        double costPerAir = AE_ENERGY_COST_PER_AIR * (wantedAir >= 0 ? 0.1D : 1.0D);
        long availableByStorage = wantedAir > 0
            ? storage.extract(AirKey.INSTANCE, demand, Actionable.SIMULATE, source)
            : storage.insert(AirKey.INSTANCE, demand, Actionable.SIMULATE, source);
        if (availableByStorage <= 0) {
            return 0;
        }

        double requestedPower = Math.min(demand, availableByStorage) * costPerAir;
        double extractedPower = energy.extractAEPower(requestedPower, Actionable.MODULATE, PowerMultiplier.CONFIG);
        int movableByEnergy = (int) Math.floor(extractedPower / costPerAir);
        int move = (int) Math.min(Math.min(demand, availableByStorage), movableByEnergy);
        if (move <= 0) {
            return 0;
        }

        if (wantedAir > 0) {
            int extracted = (int) storage.extract(AirKey.INSTANCE, move, Actionable.MODULATE, source);
            return Math.max(0, extracted);
        }

        int inserted = (int) storage.insert(AirKey.INSTANCE, move, Actionable.MODULATE, source);
        return -Math.max(0, inserted);
    }
}

