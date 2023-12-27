package com.vectorwing.farmersdelight.common.crafting;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.vectorwing.farmersdelight.Config;
import com.vectorwing.farmersdelight.Tags;

import cpw.mods.fml.common.registry.GameRegistry;

public class RecipeImporter {

    public static void init() {
        File directory = new File(Config.configDirectory, Tags.MODID + "/recipes/");
        readRecipeDirectory(directory);
    }

    private static void readRecipeDirectory(File directory) {
        File[] files = directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File file, String name) {
                return (name.toLowerCase()
                    .endsWith(".json"));
            }

        });

        for (File file : files) {
            try {
                JsonReader jsonReader = new JsonReader(new FileReader(file));
                try {
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        readJsonRecipe(jsonReader);
                    }
                } finally {
                    jsonReader.close();
                }
            } catch (IOException e) {

            }
        }
    }

    private static void readJsonRecipe(JsonReader jsonReader) throws IOException {
        List<Object> ingredients = null;
        ItemStack output = null;
        ItemStack container = null;
        String outputName = null;
        int cookTime = 200;
        int count = 1;
        float experience = 1.0F;

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "ingredients" -> {
                    if (jsonReader.peek() != JsonToken.NULL) {
                        ingredients = readIngredientsArray(jsonReader);
                        if (ingredients == null || ingredients.size() <= 0) {
                            return;
                        }
                    }
                }

                case "output" -> {
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String name1 = jsonReader.nextName();
                        switch (name1) {
                            case "item" -> outputName = jsonReader.nextString();
                            case "count" -> count = jsonReader.nextInt();
                        }
                    }
                    jsonReader.endObject();
                    output = getItemByName(outputName, count);
                    if (output == null) {
                        return;
                    }
                }
                case "container" -> {
                    jsonReader.beginObject();
                    jsonReader.nextName();
                    container = getItemByName(jsonReader.nextString());
                    jsonReader.endObject();
                }
                case "cookingtime" -> {
                    cookTime = jsonReader.nextInt();
                }
                case "experience" -> {
                    experience = jsonReader.nextLong();
                }
                default -> {
                    jsonReader.skipValue();
                }
            }
        }
        jsonReader.endObject();
        CraftingManager.addCookingPotRecipe(ingredients, output, container, experience, cookTime);
    }

    // Ingredients will be ordered by type (ItemStacks, Lists, Ores) to maximize hits in recipes
    private static List<Object> readIngredientsArray(JsonReader jsonReader) throws IOException {
        List<Object> ingredients = new ArrayList<>();
        List<Object> lists = new ArrayList<>();
        List<Object> ores = new ArrayList<>();
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            Object ingredient = readIngredient(jsonReader);
            if (ingredient == null) {
                return null;
            } else if (ingredient instanceof ItemStack) {
                ingredients.add(ingredient);
            } else if (ingredient instanceof List) {
                lists.add(ingredient);
            } else if (ingredient instanceof String) {
                ores.add(ingredient);
            }
        }
        jsonReader.endArray();
        ingredients.addAll(lists);
        ingredients.addAll(ores);
        return ingredients;
    }

    private static Object readIngredient(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
            jsonReader.beginArray();
            List<Object> ingredients = new ArrayList<>();
            while (jsonReader.hasNext()) {
                Object ingredient = readIngredient(jsonReader);
                if (ingredient == null) {
                    return null;
                }
                ingredients.add(ingredient);
            }
            jsonReader.endArray();
            return ingredients;
        } else {
            jsonReader.beginObject();
            Object object = null;
            String name = jsonReader.nextName();
            switch (name) {
                case "item" -> object = getItemByName(jsonReader.nextString());
                case "ore" -> object = jsonReader.nextString();
            }
            jsonReader.endObject();
            return object;
        }
    }

    // copied from Nutrition
    // TODO error messages
    private static ItemStack getItemByName(String fullName, int count) {
        String modid;
        String name;
        int metadata = 0;
        // Null check input string
        if (fullName == null) {
            return null;
        }
        String[] splitName = fullName.split(":");
        if (splitName.length <= 1) {
            return null;
        }
        modid = splitName[0];
        name = splitName[1];
        if (splitName.length > 2) {
            if (NumberUtils.isNumber(splitName[2])) {
                metadata = Integer.decode(splitName[2]);
            } else {
                return null;
            }
        }

        Item item = GameRegistry.findItem(modid, name);
        if (item == null) {
            return null;
        }
        return new ItemStack(item, count, metadata);
    }

    private static ItemStack getItemByName(String fullName) {
        return getItemByName(fullName, 1);
    }

}
