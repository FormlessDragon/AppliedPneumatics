package com.wintercogs.appliedpneumatics.common.me.keys;

import ae2.api.stacks.AEKey;
import ae2.api.stacks.AEKeyType;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public final class AirKey extends AEKey {
    public static final AirKey INSTANCE = new AirKey();

    private static final ResourceLocation ID = AppliedPneumatics.makeId("air_key");

    private AirKey() {
    }

    @Override
    public AEKeyType getType() {
        return AirKeyType.INSTANCE;
    }

    @Override
    public AEKey dropSecondary() {
        return this;
    }

    @Override
    public NBTTagCompound toTag() {
        return new NBTTagCompound();
    }

    @Override
    public Object getPrimaryKey() {
        return this;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void writeToPacket(PacketBuffer data) {
    }

    @Nullable
    @Override
    public Object getReadOnlyStack() {
        return null;
    }

    @Override
    protected ITextComponent computeDisplayName() {
        return new TextComponentTranslation("appliedpneumatics.me.key.air");
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, World level, BlockPos pos) {
    }

    @Override
    public boolean isTagged(String tag) {
        return false;
    }

    @Nullable
    @Override
    public NBTBase get(String componentId) {
        return null;
    }

    @Override
    public boolean hasComponents() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AirKey;
    }

    @Override
    public int hashCode() {
        return AirKey.class.hashCode();
    }
}
