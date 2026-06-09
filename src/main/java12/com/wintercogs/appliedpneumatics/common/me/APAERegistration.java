package com.wintercogs.appliedpneumatics.common.me;

import ae2.api.behaviors.ContainerItemStrategy;
import ae2.api.behaviors.GenericSlotCapacities;
import ae2.api.behaviors.ExternalStorageStrategy;
import ae2.api.behaviors.StackExportStrategy;
import ae2.api.behaviors.StackImportStrategy;
import ae2.api.stacks.AEKeyTypes;
import ae2.api.storage.StorageCells;
import com.wintercogs.appliedpneumatics.common.me.air.AirContainerItemStrategy;
import com.wintercogs.appliedpneumatics.common.me.air.AirExportStrategy;
import com.wintercogs.appliedpneumatics.common.me.air.AirExternalStorageStrategy;
import com.wintercogs.appliedpneumatics.common.me.air.AirImportStrategy;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import com.wintercogs.appliedpneumatics.common.me.storage.AirCellHandler;

public final class APAERegistration {
    public static final long AIR_GENERIC_SLOT_CAPACITY = 64_000L;

    private static boolean keyTypeRegistered;
    private static boolean genericSlotCapacityRegistered;
    private static boolean cellHandlerRegistered;
    private static boolean containerItemStrategyRegistered;
    private static boolean stackWorldStrategiesRegistered;

    private APAERegistration() {
    }

    public static void registerEarly() {
        if (!keyTypeRegistered) {
            AEKeyTypes.register(AirKeyType.INSTANCE);
            keyTypeRegistered = true;
        }
        if (!genericSlotCapacityRegistered) {
            GenericSlotCapacities.register(AirKeyType.INSTANCE, AIR_GENERIC_SLOT_CAPACITY);
            genericSlotCapacityRegistered = true;
        }
    }

    public static void registerCommon() {
        if (!cellHandlerRegistered) {
            StorageCells.addCellHandler(AirCellHandler.INSTANCE);
            cellHandlerRegistered = true;
        }
        if (!containerItemStrategyRegistered) {
            ContainerItemStrategy.register(AirKeyType.INSTANCE, AirKey.class, new AirContainerItemStrategy());
            containerItemStrategyRegistered = true;
        }
        if (!stackWorldStrategiesRegistered) {
            ExternalStorageStrategy.register(AirKeyType.INSTANCE, AirExternalStorageStrategy::new);
            StackImportStrategy.register(AirKeyType.INSTANCE, AirImportStrategy::new);
            StackExportStrategy.register(AirKeyType.INSTANCE, AirExportStrategy::new);
            stackWorldStrategiesRegistered = true;
        }
    }
}

