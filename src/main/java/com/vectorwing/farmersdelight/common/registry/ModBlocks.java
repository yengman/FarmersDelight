package com.vectorwing.farmersdelight.common.registry;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

import com.vectorwing.farmersdelight.common.block.BlockCookingPot;
import com.vectorwing.farmersdelight.common.item.ItemBlockCookingPot;
import com.vectorwing.farmersdelight.common.tile.TileCookingPot;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static Block COOKING_POT;

    public static void init() {
        registerBlocks();
        registerTileEntities();
    }

    private static void registerBlocks() {
        COOKING_POT = registerBlock(new BlockCookingPot(), ItemBlockCookingPot.class);
    }

    private static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileCookingPot.class, "TileCookingPot");

    }

    private static Block registerBlock(Block block, Class<? extends ItemBlock> itemClass) {
        return GameRegistry.registerBlock(block, itemClass, block.getUnlocalizedName());
    }

    private static Block registerBlock(Block block) {
        return GameRegistry.registerBlock(block, block.getUnlocalizedName());
    }

}
