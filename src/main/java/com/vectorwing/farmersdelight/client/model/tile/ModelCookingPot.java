package com.vectorwing.farmersdelight.client.model.tile;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.tileentity.TileEntity;
// Exported for Minecraft version 1.7 - 1.12
// Paste this class into your mod and generate all required imports

public class ModelCookingPot extends ModelBase {

    private final ModelRenderer cooking_pot;
    private final ModelRenderer cookingpot;
    private final ModelRenderer spoon_r1;

    public ModelCookingPot() {
        textureWidth = 64;
        textureHeight = 64;

        cooking_pot = new ModelRenderer(this);
        cooking_pot.setRotationPoint(0.0F, 16.0F, 0.0F);

        cookingpot = new ModelRenderer(this);
        cookingpot.setRotationPoint(0.0F, 0.0F, 0.0F);
        cooking_pot.addChild(cookingpot);
        cookingpot.cubeList.add(new ModelBox(cookingpot, 0, 0, -6.0F, -2.0F, -6.0F, 12, 10, 12, 0.0F));
        cookingpot.cubeList.add(new ModelBox(cookingpot, 0, 22, -8.0F, -1.0F, -3.0F, 2, 2, 6, 0.0F));
        cookingpot.cubeList.add(new ModelBox(cookingpot, 0, 22, 6.0F, -1.0F, -3.0F, 2, 2, 6, 0.0F));

        spoon_r1 = new ModelRenderer(this);
        spoon_r1.setRotationPoint(0.0F, 5.0F, 0.0F);
        cookingpot.addChild(spoon_r1);
        setRotationAngle(spoon_r1, 0.0F, 0.0F, -0.3927F);
        spoon_r1.cubeList.add(new ModelBox(spoon_r1, 0, 30, -1.0F, -12.0F, -1.0F, 2, 12, 2, 0.0F));
    }

    public void render(TileEntity tileEntity, float f5) {
        cooking_pot.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
