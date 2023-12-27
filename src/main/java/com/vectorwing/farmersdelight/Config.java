package com.vectorwing.farmersdelight;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Config {

    public static String greeting = "Hello World";

    public static File configDirectory;

    public static void registerConfigs(FMLPreInitializationEvent event) {
        Config.configDirectory = event.getModConfigurationDirectory();
        Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());

        greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "How shall I greet?");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

}
