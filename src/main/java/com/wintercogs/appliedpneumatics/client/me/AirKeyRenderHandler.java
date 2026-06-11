package com.wintercogs.appliedpneumatics.client.me;

import ae2.api.client.AEKeyRenderHandler;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class AirKeyRenderHandler implements AEKeyRenderHandler<AirKey> {
    private static final ResourceLocation AIR_PARTICLE_TEXTURE =
        new ResourceLocation("pneumaticcraft", "textures/particle/air_particle.png");

    @Override
    public void drawInGui(Minecraft minecraft, int x, int y, AirKey stack) {
        minecraft.getTextureManager().bindTexture(AIR_PARTICLE_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.ingameGUI.drawTexturedModalRect(x, y, 0, 0, 16, 16);
    }

    @Override
    public void drawOnBlockFace(AirKey what, float scale, int combinedLight, World level) {
    }

    @Override
    public ITextComponent getDisplayName(AirKey stack) {
        return stack.getDisplayName();
    }
}

