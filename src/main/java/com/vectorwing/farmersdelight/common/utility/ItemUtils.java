package com.vectorwing.farmersdelight.common.utility;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.vectorwing.farmersdelight.FarmersDelight;

import cpw.mods.fml.common.registry.GameRegistry;

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

    public static Item getItemByName(String fullName) {
        ItemBlockString itemBlockString = MiscUtils.parseItemBlockString(fullName, false, 0);
        if (itemBlockString == null) {
            return null;
        }
        Item item = GameRegistry.findItem(itemBlockString.modid, itemBlockString.name);
        if (item == null) {
            FarmersDelight.LOG.info("Item not found: {}", fullName);
        }
        return item;
    }

    public static ItemStack getItemStackByName(String fullName, int count) {
        ItemBlockString itemBlockString = MiscUtils.parseItemBlockString(fullName, true, 0);
        if (itemBlockString == null) {
            return null;
        }
        Item item = GameRegistry.findItem(itemBlockString.modid, itemBlockString.name);
        if (item == null) {
            FarmersDelight.LOG.info("Item not found: {}", fullName);
            return null;
        }
        return new ItemStack(item, count, itemBlockString.metadata);
    }

    public static ItemStack getItemStackByName(String fullName) {
        return getItemStackByName(fullName, 1);
    }

}
