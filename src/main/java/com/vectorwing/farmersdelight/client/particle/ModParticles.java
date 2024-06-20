package com.vectorwing.farmersdelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;

public class ModParticles {

    public static void spawnBubblePopParticle(World world, double x, double y, double z, double motionX, double motionY,
        double motionZ) {
        EntityFX entityFX = new EntityBubblePopFX(world, x, y, z, motionX, motionY, motionZ);
        Minecraft.getMinecraft().effectRenderer.addEffect(entityFX);
    }

    public static void spawnSteamParticle(World world, double x, double y, double z, double motionX, double motionY,
        double motionZ) {
        EntityFX entityFX = new EntitySteamFX(world, x, y, z, motionX, motionY, motionZ);
        Minecraft.getMinecraft().effectRenderer.addEffect(entityFX);
    }
}
