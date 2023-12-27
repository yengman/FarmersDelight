package com.vectorwing.farmersdelight.client.renderer.tile;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.vectorwing.farmersdelight.client.model.tile.ModelCookingPot;

public class RenderTileCookingPot extends TileEntitySpecialRenderer {

    private static final ModelCookingPot MODEL = new ModelCookingPot();

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        "farmersdelight",
        "textures/block/cooking_pot.png");

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double posX, double posY, double posZ, float partialTick) {
        bindTexture(TEXTURE);
        GL11.glPushMatrix();
        GL11.glTranslated(posX + 0.5D, posY + 1.5D, posZ + 0.5D);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        switch (tileEntity.getBlockMetadata()) {
            case 2 -> GL11.glRotatef(0F, 0F, 1F, 0F);
            case 3 -> GL11.glRotatef(180F, 0F, 1F, 0F);
            case 4 -> GL11.glRotatef(90F, 0F, 1F, 0F);
            case 5 -> GL11.glRotatef(270F, 0F, 1F, 0F);
        }
        MODEL.render(tileEntity, 0.0625F);
        GL11.glPopMatrix();

    }
}
