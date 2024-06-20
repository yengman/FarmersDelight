package com.vectorwing.farmersdelight;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;

import com.vectorwing.farmersdelight.common.utility.ItemBlockString;
import com.vectorwing.farmersdelight.common.utility.ItemUtils;
import com.vectorwing.farmersdelight.common.utility.MiscUtils;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class Config {

    public static File configDirectory;

    public static HashMap<Block, int[]> HEAT_SOURCES = new HashMap<>();
    public static HashMap<Block, int[]> HEAT_CONDUCTORS = new HashMap<>();

    public static HashMap<Item, Item> INGREDIENT_REMAINDER_OVERRIDES = new HashMap<>();

    public static HashSet<Item> CONTAINERS = new HashSet<>();

    public static void registerConfigs(FMLPreInitializationEvent event) {
        Config.configDirectory = event.getModConfigurationDirectory();
        Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());

        String[] heatSources = configuration
            .getString(
                "heat_sources",
                Configuration.CATEGORY_GENERAL,
                "",
                "comma-separated list of heat source blocks, used for the cooking pot")
            .split(",");
        loadBlocksFromConfig(HEAT_SOURCES, heatSources);

        String[] heatConductors = configuration
            .getString(
                "heat_conductors",
                Configuration.CATEGORY_GENERAL,
                "",
                "comma-separated list of heat-conducting blocks, used for the cooking pot")
            .split(",");
        loadBlocksFromConfig(HEAT_CONDUCTORS, heatConductors);

        String[] containers = configuration
            .getString(
                "containers",
                Configuration.CATEGORY_GENERAL,
                "",
                "comma-separated list of containers, used for shift-clicking in the cooking pot")
            .split(",");

        for (String s : containers) {
            Item item = ItemUtils.getItemByName(s);
            if (item != null) {
                CONTAINERS.add(item);
            }
        }

        // maybe make this configurable?
        INGREDIENT_REMAINDER_OVERRIDES.put(Items.mushroom_stew, Items.bowl);
        INGREDIENT_REMAINDER_OVERRIDES.put(Items.potionitem, Items.glass_bottle);
        INGREDIENT_REMAINDER_OVERRIDES.put(Items.experience_bottle, Items.glass_bottle);
        INGREDIENT_REMAINDER_OVERRIDES.put(Items.milk_bucket, Items.bucket);

        if (configuration.hasChanged()) {
            configuration.save();
        }

    }

    private static void loadBlocksFromConfig(HashMap<Block, int[]> map, String[] prop) {
        main_loop: for (String s : prop) {
            // -1 meta = any metadata ok
            ItemBlockString itemBlockString = MiscUtils.parseItemBlockString(s, true, -1);
            if (itemBlockString == null) {
                continue;
            }
            Block block = GameRegistry.findBlock(itemBlockString.modid, itemBlockString.name);
            if (block == null) {
                FarmersDelight.LOG.info("Block not found for config: {}", s);
                continue;
            }
            int metadata = itemBlockString.metadata;
            int[] allowedMeta = map.get(block);
            if (allowedMeta == null || metadata == -1) {
                map.put(block, new int[] { metadata });
            } else {
                for (int md : allowedMeta) {
                    if (md == metadata || md == -1) {
                        continue main_loop;
                    }
                }
                int[] newAllowedMeta = Arrays.copyOf(allowedMeta, allowedMeta.length + 1);
                newAllowedMeta[newAllowedMeta.length - 1] = metadata;
                map.put(block, newAllowedMeta);
            }
        }
    }

}
