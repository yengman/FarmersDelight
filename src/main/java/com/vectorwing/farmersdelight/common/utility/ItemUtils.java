package com.vectorwing.farmersdelight.common.utility;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemUtils {

    public static void spawnEntityItem(World world, ItemStack itemStack, double x, double y, double z, double motionX,
        double motionY, double motionZ) {
        EntityItem entityItem = new EntityItem(world, x, y, z, itemStack);
        if (itemStack.hasTagCompound()) {
            entityItem.getEntityItem()
                .setTagCompound(
                    (NBTTagCompound) itemStack.getTagCompound()
                        .copy());
        }
        entityItem.motionX = motionX;
        entityItem.motionY = motionY;
        entityItem.motionZ = motionZ;
        world.spawnEntityInWorld(entityItem);
    }

}
