package com.wintercogs.appliedpneumatics.client.me.render;

import appeng.api.client.AEKeyRenderHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wintercogs.appliedpneumatics.common.me.keys.AirKey;
import com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType;
import com.wintercogs.appliedpneumatics.util.IngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;


public class AirKeyRenderHandler implements AEKeyRenderHandler<AirKey>
{
    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics gui, int x, int y, AirKey stack)
    {
        var pose = gui.pose();
        pose.pushPose();

        ResourceLocation still = IClientFluidTypeExtensions.of(Fluids.WATER).getStillTexture();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);

        if (sprite != null && sprite.atlasLocation() != MissingTextureAtlasSprite.getLocation()) {
            int tint = 0x88CFEFFD;
            IngredientRenderer.drawTiledSprite(gui, 16, 16, tint, 16, sprite, x, y);
        }

        pose.popPose();
    }

    @Override
    public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, AirKey what, float scale, int combinedLight, Level level)
    {

    }

    @Override
    public Component getDisplayName(AirKey stack)
    {
        return AirKeyType.NAME;
    }
}
