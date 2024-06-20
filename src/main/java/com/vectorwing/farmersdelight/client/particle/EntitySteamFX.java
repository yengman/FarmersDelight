package com.vectorwing.farmersdelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

public class EntitySteamFX extends EntityFX {

    private static final int TEXTURE_COUNT = 12;
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[TEXTURE_COUNT];
    private final ResourceLocation texture;

    protected float entityBrightness;

    static {
        for (int i = 0; i < 12; i++) {
            TEXTURES[i] = new ResourceLocation("farmersdelight", "textures/particles/steam_" + i + ".png");
        }
    }

    public EntitySteamFX(World p_i1219_1_, double p_i1219_2_, double p_i1219_4_, double p_i1219_6_, double p_i1219_8_,
        double p_i1219_10_, double p_i1219_12_) {
        super(p_i1219_1_, p_i1219_2_, p_i1219_4_, p_i1219_6_, p_i1219_8_, p_i1219_10_, p_i1219_12_);
        texture = TEXTURES[rand.nextInt(TEXTURE_COUNT)];
        particleScale = 2.0F;
        setSize(0.25F, 0.25F);
        particleMaxAge = rand.nextInt(50) + 80;
        particleGravity = 3.0E-6F;
        particleAlpha = 0.6F;
        motionX = p_i1219_8_;
        motionY = p_i1219_10_ + (double) (rand.nextFloat() / 500.0F);
        motionZ = p_i1219_12_;
    }

    @Override
    public void renderParticle(Tessellator tessellator, float partialTicks, float rx, float rxz, float rz, float ryz,
        float rxy) {
        int prevTex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(texture);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glPushMatrix();
        float ipx = (float) (prevPosX + (posX - prevPosX) * partialTicks - EntityFX.interpPosX);
        float ipy = (float) (prevPosY + (posY - prevPosY) * partialTicks - EntityFX.interpPosY);
        float ipz = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - EntityFX.interpPosZ);
        double f6;
        double f7;
        double f8;
        double f9;
        double f10 = 0.1D * particleScale;
        f7 = f9 = 1.0D;
        f6 = f8 = 0.0D;
        tessellator.startDrawingQuads();
        tessellator.setBrightness(getBrightnessForRender(entityBrightness));
        tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
        tessellator.addVertexWithUV(ipx - rx * f10 - ryz * f10, ipy - rxz * f10, ipz - rz * f10 - rxy * f10, f7, f9);
        tessellator.addVertexWithUV(ipx - rx * f10 + ryz * f10, ipy + rxz * f10, ipz - rz * f10 + rxy * f10, f7, f8);
        tessellator.addVertexWithUV(ipx + rx * f10 + ryz * f10, ipy + rxz * f10, ipz + rz * f10 + rxy * f10, f6, f8);
        tessellator.addVertexWithUV(ipx + rx * f10 - ryz * f10, ipy - rxz * f10, ipz + rz * f10 - rxy * f10, f6, f9);
        tessellator.draw();
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTex);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        entityBrightness = worldObj.getLightBrightness((int) posX, (int) posY, (int) posZ);
        if (particleAge >= particleMaxAge - 60 && particleAlpha > 0.01F) {
            particleAlpha -= 0.02F;
        }
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

}
