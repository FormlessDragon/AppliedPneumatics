package com.wintercogs.appliedpneumatics.common.me.keys.types;

import ae2.api.stacks.AEKey;
import ae2.api.stacks.AEKeyType;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

public final class AirKeyType extends AEKeyType {
    public static final AirKeyType INSTANCE = new AirKeyType();

    private AirKeyType() {
        super(AppliedPneumatics.makeId("air_type"), AirKey.class,
            new TextComponentTranslation("appliedpneumatics.me.key.air"));
    }

    @Nullable
    @Override
    public AEKey readFromPacket(PacketBuffer input) {
        return AirKey.INSTANCE;
    }

    @Nullable
    @Override
    public AEKey loadKeyFromTag(NBTTagCompound tag) {
        return AirKey.INSTANCE;
    }

    @Override
    public int getAmountPerOperation() {
        return 1000;
    }

    @Override
    public int getAmountPerByte() {
        return 250;
    }

    @Override
    public int getAmountPerUnit() {
        return 1000;
    }

    @Nullable
    @Override
    public String getUnitSymbol() {
        return "L";
    }
}
