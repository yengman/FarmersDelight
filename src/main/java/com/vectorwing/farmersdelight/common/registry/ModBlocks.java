package com.vectorwing.farmersdelight.common.registry;

import net.minecraft.block.Block;

import com.vectorwing.farmersdelight.common.block.BlockCookingPot;
import com.vectorwing.farmersdelight.common.tile.TileCookingPot;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static Block COOKING_POT;

    public static void init() {
        registerBlocks();
        registerTileEntities();
    }

    private static void registerBlocks() {
        COOKING_POT = GameRegistry.registerBlock(new BlockCookingPot(), "cooking_pot");
    }

    private static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileCookingPot.class, "TileCookingPot");

    }

}
