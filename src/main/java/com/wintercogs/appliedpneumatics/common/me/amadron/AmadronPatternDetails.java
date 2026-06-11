package com.wintercogs.appliedpneumatics.common.me.amadron;

import ae2.api.crafting.IPatternDetails;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.GenericStack;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

public final class AmadronPatternDetails implements IPatternDetails {
    private static final String OFFER_TAG = "appliedpneumatics_amadron_offer";

    private final AEItemKey definition;
    private final AmadronOffer offer;
    private final IInput[] inputs;
    private final List<GenericStack> outputs;

    public AmadronPatternDetails(AEItemKey definition) {
        this.definition = definition;
        ItemStack patternStack = definition.toStack();
        this.offer = getEncodedOffer(patternStack);
        if (this.offer == null) {
            throw new IllegalArgumentException("Given item does not encode an Amadron offer: " + definition);
        }

        GenericStack input = toGenericStack(this.offer.getInput());
        GenericStack output = toGenericStack(this.offer.getOutput());
        this.inputs = new IInput[] { new Input(input) };
        this.outputs = Collections.singletonList(output);
    }

    public AmadronOffer getOffer() {
        return this.offer;
    }

    @Override
    public AEItemKey getDefinition() {
        return this.definition;
    }

    @Override
    public IInput[] getInputs() {
        return this.inputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return this.outputs;
    }

    public static void encode(ItemStack stack, AmadronOffer offer) {
        if (stack.isEmpty() || stack.getItem() != APItems.AMADRON_PATTERN) {
            throw new IllegalArgumentException("Amadron offers can only be encoded on Amadron pattern items");
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setTag(OFFER_TAG, Pnc112AmadronBridge.writeOfferKey(offer));
    }

    public static AmadronOffer getEncodedOffer(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != APItems.AMADRON_PATTERN || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(OFFER_TAG, 10)) {
            return null;
        }
        return Pnc112AmadronBridge.readOfferKey(tag.getCompoundTag(OFFER_TAG));
    }

    private static GenericStack toGenericStack(Object resource) {
        if (resource instanceof ItemStack) {
            return GenericStack.fromItemStack((ItemStack) resource);
        }
        if (resource instanceof FluidStack) {
            return GenericStack.fromFluidStack((FluidStack) resource);
        }
        throw new IllegalArgumentException("Amadron resource must be an ItemStack or FluidStack");
    }

    @Override
    public int hashCode() {
        return this.definition.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof AmadronPatternDetails
            && this.definition.equals(((AmadronPatternDetails) obj).definition));
    }

    private static final class Input implements IInput {
        private final GenericStack[] template;
        private final long multiplier;

        private Input(GenericStack stack) {
            this.template = new GenericStack[] { new GenericStack(stack.what(), 1) };
            this.multiplier = stack.amount();
        }

        @Override
        public GenericStack[] possibleInputs() {
            return this.template;
        }

        @Override
        public long getMultiplier() {
            return this.multiplier;
        }

        @Override
        public boolean isValid(AEKey input, World level) {
            return input != null && input.matches(this.template[0]);
        }

        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}

