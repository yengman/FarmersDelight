package com.vectorwing.farmersdelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import com.vectorwing.farmersdelight.Config;

public interface IHeatableBlock {

    default boolean isHeated(World world, int x, int y, int z) {
        Block blockBelow = world.getBlock(x, y - 1, z);
        int metaBelow = world.getBlockMetadata(x, y - 1, z);
        int[] allowed1 = Config.HEAT_SOURCES.get(blockBelow);
        if (allowed1 != null) {
            for (int i : allowed1) {
                if (i == -1 || i == metaBelow) {
                    return true;
                }
            }
        }
        if (!requiresDirectHeat()) {
            int[] allowed2 = Config.HEAT_CONDUCTORS.get(blockBelow);
            if (allowed2 != null) {
                for (int i : allowed2) {
                    if (i == -1 || i == metaBelow) {
                        Block blockFurtherBelow = world.getBlock(x, y - 2, z);
                        int metaFurtherBelow = world.getBlockMetadata(x, y - 1, z);
                        int[] allowed3 = Config.HEAT_SOURCES.get(blockFurtherBelow);
                        if (allowed3 != null) {
                            for (int j : allowed3) {
                                if (j == -1 || j == metaFurtherBelow) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    default boolean requiresDirectHeat() {
        return false;
    }
}
