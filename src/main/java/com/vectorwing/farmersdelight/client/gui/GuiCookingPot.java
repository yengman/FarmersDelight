package com.vectorwing.farmersdelight.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.vectorwing.farmersdelight.common.inventory.ContainerCookingPot;
import com.vectorwing.farmersdelight.common.tile.TileCookingPot;

public class GuiCookingPot extends GuiContainer {

    private TileCookingPot tileCookingPot;

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        "farmersdelight",
        "textures/gui/cooking_pot.png");

    public GuiCookingPot(InventoryPlayer inventoryPlayer, TileCookingPot tileCookingPot) {
        super(new ContainerCookingPot(inventoryPlayer, tileCookingPot));
        this.tileCookingPot = tileCookingPot;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(TEXTURE);
        int k = (width - xSize) / 2;
        int l = (height - ySize) / 2;
        drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
        int m = tileCookingPot.getCookProgressScaled();
        drawTexturedModalRect(k + 89, l + 25, 176, 15, m + 1, 17);
    }

}
