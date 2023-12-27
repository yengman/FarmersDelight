package com.vectorwing.farmersdelight.common.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class CraftingManager {

    private static List<RecipeCookingPot> cookingPotRecipes = new ArrayList<>();

    public static RecipeCookingPot findMatchingCookingPotRecipe(IInventory inventory) {
        for (RecipeCookingPot recipe : cookingPotRecipes) {
            if (recipe.matches(inventory)) {
                return recipe;
            }
        }
        return null;
    }

    public static RecipeCookingPot addCookingPotRecipe(List<Object> ingredients, ItemStack output, ItemStack container,
        float experience, int cookTime) {
        RecipeCookingPot recipeCookingPot = new RecipeCookingPot(ingredients, output, container, experience, cookTime);
        cookingPotRecipes.add(recipeCookingPot);
        return recipeCookingPot;
    }

}
