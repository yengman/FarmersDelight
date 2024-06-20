package com.vectorwing.farmersdelight.client.renderer.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;

import com.vectorwing.farmersdelight.client.utils.RenderUtils;
import com.vectorwing.farmersdelight.common.block.BlockCookingPot;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class RenderCookingPot implements ISimpleBlockRenderingHandler {

    public static final int RENDER_ID = RenderingRegistry.getNextAvailableRenderId();

    private static final int INV_META = 3;

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        Tessellator tessellator = Tessellator.instance;
        RenderUtils.renderStandardBlockForInv(tessellator, block, INV_META, renderer);
        BlockCookingPot blockCookingPot = (BlockCookingPot) block;
        renderer.setOverrideBlockTexture(blockCookingPot.iconHandle);
        renderer.uvRotateTop = 1;
        renderer.uvRotateBottom = 1;
        block.setBlockBounds(0.875F, 0.4375F, 0.3125F, 1F, 0.5625F, 0.6875F);
        renderer.setRenderBoundsFromBlock(block);
        RenderUtils.renderStandardBlockForInv(tessellator, block, INV_META, renderer);
        renderer.uvRotateTop = 2;
        renderer.uvRotateBottom = 2;
        block.setBlockBounds(0F, 0.4375F, 0.3125F, 0.125F, 0.5625F, 0.6875F);
        renderer.setRenderBoundsFromBlock(block);
        RenderUtils.renderStandardBlockForInv(tessellator, block, INV_META, renderer);
        renderer.uvRotateTop = 0;
        renderer.uvRotateBottom = 0;
        IIcon iconSpoon = blockCookingPot.iconSpoon;
        Vec3[] vector = new Vec3[8];
        float vecX = 0.0625F;
        float vecY = 0.625F;
        float vecZ = 0.0625F;
        vector[0] = Vec3.createVectorHelper(-vecX, 0.0D, -vecZ);
        vector[1] = Vec3.createVectorHelper(vecX, 0.0D, -vecZ);
        vector[2] = Vec3.createVectorHelper(vecX, 0.0D, vecZ);
        vector[3] = Vec3.createVectorHelper(-vecX, 0.0D, vecZ);
        vector[4] = Vec3.createVectorHelper(-vecX, vecY, -vecZ);
        vector[5] = Vec3.createVectorHelper(vecX, vecY, -vecZ);
        vector[6] = Vec3.createVectorHelper(vecX, vecY, vecZ);
        vector[7] = Vec3.createVectorHelper(-vecX, vecY, vecZ);

        double x, y, z;
        x = y = z = -0.5D;

        for (int vecCount = 0; vecCount < vector.length; vecCount++) {
            vector[vecCount].yCoord -= 0.1875D;
            vector[vecCount].rotateAroundZ(-(float) Math.PI / 8F);
            vector[vecCount].xCoord += -0.125D;
        }

        Vec3 vertex1 = null;
        Vec3 vertex2 = null;
        Vec3 vertex3 = null;
        Vec3 vertex4 = null;

        double uMin = iconSpoon.getMinU();
        double uMax = iconSpoon.getMaxU();
        double vMin = iconSpoon.getMinV();
        double vMax = iconSpoon.getMaxV();

        for (int side = 0; side < 6; side++) {

            if (side == 0) {
                uMin = iconSpoon.getInterpolatedU(7.0D);
                vMin = iconSpoon.getInterpolatedV(2.0D);
                uMax = iconSpoon.getInterpolatedU(9.0D);
                vMax = iconSpoon.getInterpolatedV(4.0D);
            } else if (side == 2) {
                uMin = iconSpoon.getInterpolatedU(7.0D);
                vMin = iconSpoon.getInterpolatedV(4.0D);
                uMax = iconSpoon.getInterpolatedU(9.0D);
                vMax = iconSpoon.getMaxV();
            }

            switch (side) {
                case 0 -> {
                    vertex1 = vector[0];
                    vertex2 = vector[1];
                    vertex3 = vector[2];
                    vertex4 = vector[3];
                }
                case 1 -> {
                    vertex1 = vector[7];
                    vertex2 = vector[6];
                    vertex3 = vector[5];
                    vertex4 = vector[4];
                }
                case 2 -> {
                    tessellator.setColorOpaque_F(0.8F, 0.8F, 0.8F);
                    vertex1 = vector[1];
                    vertex2 = vector[0];
                    vertex3 = vector[4];
                    vertex4 = vector[5];
                }

                case 3 -> {
                    tessellator.setColorOpaque_F(0.6F, 0.6F, 0.6F);
                    vertex1 = vector[2];
                    vertex2 = vector[1];
                    vertex3 = vector[5];
                    vertex4 = vector[6];
                }
                case 4 -> {
                    tessellator.setColorOpaque_F(0.8F, 0.8F, 0.8F);
                    vertex1 = vector[3];
                    vertex2 = vector[2];
                    vertex3 = vector[6];
                    vertex4 = vector[7];
                }
                case 5 -> {
                    tessellator.setColorOpaque_F(0.6F, 0.6F, 0.6F);
                    vertex1 = vector[0];
                    vertex2 = vector[3];
                    vertex3 = vector[7];
                    vertex4 = vector[4];
                }
            }
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord, uMin, vMax);
            tessellator.addVertexWithUV(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord, uMax, vMax);
            tessellator.addVertexWithUV(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord, uMax, vMin);
            tessellator.addVertexWithUV(vertex4.xCoord, vertex4.yCoord, vertex4.zCoord, uMin, vMin);
            tessellator.draw();
        }

        renderer.uvRotateTop = 0;
        renderer.uvRotateBottom = 0;
        renderer.clearOverrideBlockTexture();
        block.setBlockBounds(0.125F, 0F, 0.125F, 0.875F, 0.625F, 0.875F);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        Tessellator tessellator = Tessellator.instance;
        renderer.renderStandardBlock(block, x, y, z);
        int metadata = world.getBlockMetadata(x, y, z);
        renderer.setOverrideBlockTexture(((BlockCookingPot) block).iconHandle);

        switch (metadata) {
            default -> {
                renderer.uvRotateTop = 1;
                renderer.uvRotateBottom = 1;
                block.setBlockBounds(0.875F, 0.4375F, 0.3125F, 1F, 0.5625F, 0.6875F);
                renderer.setRenderBoundsFromBlock(block);
                renderer.renderStandardBlock(block, x, y, z);
                renderer.uvRotateTop = 2;
                renderer.uvRotateBottom = 2;
                block.setBlockBounds(0F, 0.4375F, 0.3125F, 0.125F, 0.5625F, 0.6875F);
                renderer.setRenderBoundsFromBlock(block);
                renderer.renderStandardBlock(block, x, y, z);

            }

            case 4, 5 -> {
                renderer.uvRotateTop = 3;
                renderer.uvRotateBottom = 3;
                block.setBlockBounds(0.3125F, 0.4375F, 0.875F, 0.6875F, 0.5625F, 1F);
                renderer.setRenderBoundsFromBlock(block);
                renderer.renderStandardBlock(block, x, y, z);
                renderer.uvRotateTop = 0;
                renderer.uvRotateBottom = 0;
                block.setBlockBounds(0.3125F, 0.4375F, 0F, 0.6875F, 0.5625F, 0.125F);
                renderer.setRenderBoundsFromBlock(block);
                renderer.renderStandardBlock(block, x, y, z);
            }
        }

        renderer.uvRotateTop = 0;
        renderer.uvRotateBottom = 0;

        IIcon iconSpoon = ((BlockCookingPot) block).iconSpoon;

        Vec3[] vector = new Vec3[8];
        float vecX = 0.0625F;
        float vecY = 0.625F;
        float vecZ = 0.0625F;
        vector[0] = Vec3.createVectorHelper(-vecX, 0.0D, -vecZ);
        vector[1] = Vec3.createVectorHelper(vecX, 0.0D, -vecZ);
        vector[2] = Vec3.createVectorHelper(vecX, 0.0D, vecZ);
        vector[3] = Vec3.createVectorHelper(-vecX, 0.0D, vecZ);
        vector[4] = Vec3.createVectorHelper(-vecX, vecY, -vecZ);
        vector[5] = Vec3.createVectorHelper(vecX, vecY, -vecZ);
        vector[6] = Vec3.createVectorHelper(vecX, vecY, vecZ);
        vector[7] = Vec3.createVectorHelper(-vecX, vecY, vecZ);

        for (int vecCount = 0; vecCount < 8; vecCount++) {
            vector[vecCount].yCoord -= 0.1875D;
            switch (metadata) {
                default -> {
                    vector[vecCount].rotateAroundZ((float) Math.PI / 8F);
                    vector[vecCount].xCoord += x + 0.625D;
                    vector[vecCount].zCoord += z + 0.5D;
                }
                case 3 -> {
                    vector[vecCount].rotateAroundZ(-(float) Math.PI / 8F);
                    vector[vecCount].xCoord += x + 0.375D;
                    vector[vecCount].zCoord += z + 0.5D;

                }
                case 4 -> {
                    vector[vecCount].rotateAroundX((float) Math.PI / 8F);
                    vector[vecCount].xCoord += x + 0.5D;
                    vector[vecCount].zCoord += z + 0.375D;
                }
                case 5 -> {
                    vector[vecCount].rotateAroundX(-(float) Math.PI / 8F);
                    vector[vecCount].xCoord += x + 0.5D;
                    vector[vecCount].zCoord += z + 0.625D;
                }
            }

            vector[vecCount].yCoord += y + 0.5D;
        }

        Vec3 vertex1 = null;
        Vec3 vertex2 = null;
        Vec3 vertex3 = null;
        Vec3 vertex4 = null;

        double uMin = iconSpoon.getMinU();
        double uMax = iconSpoon.getMaxU();
        double vMin = iconSpoon.getMinV();
        double vMax = iconSpoon.getMaxV();

        for (int side = 0; side < 6; side++) {

            if (side == 0) {
                uMin = iconSpoon.getInterpolatedU(7.0D);
                vMin = iconSpoon.getInterpolatedV(2.0D);
                uMax = iconSpoon.getInterpolatedU(9.0D);
                vMax = iconSpoon.getInterpolatedV(4.0D);
            } else if (side == 2) {
                uMin = iconSpoon.getInterpolatedU(7.0D);
                vMin = iconSpoon.getInterpolatedV(4.0D);
                uMax = iconSpoon.getInterpolatedU(9.0D);
                vMax = iconSpoon.getMaxV();
            }

            switch (side) {
                case 0 -> {
                    vertex1 = vector[0];
                    vertex2 = vector[1];
                    vertex3 = vector[2];
                    vertex4 = vector[3];
                }
                case 1 -> {
                    vertex1 = vector[7];
                    vertex2 = vector[6];
                    vertex3 = vector[5];
                    vertex4 = vector[4];
                }
                case 2 -> {
                    tessellator.setColorOpaque_F(0.8F, 0.8F, 0.8F);
                    vertex1 = vector[1];
                    vertex2 = vector[0];
                    vertex3 = vector[4];
                    vertex4 = vector[5];
                }

                case 3 -> {
                    tessellator.setColorOpaque_F(0.6F, 0.6F, 0.6F);
                    vertex1 = vector[2];
                    vertex2 = vector[1];
                    vertex3 = vector[5];
                    vertex4 = vector[6];
                }
                case 4 -> {
                    tessellator.setColorOpaque_F(0.8F, 0.8F, 0.8F);
                    vertex1 = vector[3];
                    vertex2 = vector[2];
                    vertex3 = vector[6];
                    vertex4 = vector[7];
                }
                case 5 -> {
                    tessellator.setColorOpaque_F(0.6F, 0.6F, 0.6F);
                    vertex1 = vector[0];
                    vertex2 = vector[3];
                    vertex3 = vector[7];
                    vertex4 = vector[4];
                }
            }

            tessellator.addVertexWithUV(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord, uMin, vMax);
            tessellator.addVertexWithUV(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord, uMax, vMax);
            tessellator.addVertexWithUV(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord, uMax, vMin);
            tessellator.addVertexWithUV(vertex4.xCoord, vertex4.yCoord, vertex4.zCoord, uMin, vMin);
        }

        renderer.uvRotateTop = 0;
        renderer.uvRotateBottom = 0;
        renderer.clearOverrideBlockTexture();
        block.setBlockBounds(0.125F, 0F, 0.125F, 0.875F, 0.625F, 0.875F);
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return RENDER_ID;
    }

}
