package com.vectorwing.farmersdelight.common.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeCookingPot {

    private static final int INPUT_SLOTS = 6;

    private final List<Object> ingredients;

    private final ItemStack output;

    private final ItemStack container;

    private final float experience;

    private final int cookTime;

    public RecipeCookingPot(List<Object> ingredients, ItemStack output, ItemStack container, float experience,
        int cookTime) {
        this.ingredients = ingredients;
        this.output = output;
        if (container != null) {
            this.container = container;
        } else if (output.getItem()
            .hasContainerItem(output)) {
                this.container = output.getItem()
                    .getContainerItem(output);
            } else {
                this.container = null;
            }
        this.experience = experience;
        this.cookTime = cookTime;
    }

    public boolean matches(IInventory inventory) {
        List<Object> recipe = new ArrayList<>(ingredients);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            ItemStack input = inventory.getStackInSlot(slot);
            if (input != null) {
                boolean inRecipe = false;
                for (Object object : recipe) {
                    if (doesItemMatch(object, input)) {
                        inRecipe = true;
                        recipe.remove(object);
                        break;
                    }
                }
                if (!inRecipe) {
                    return false;
                }
            }
        }
        return recipe.isEmpty();
    }

    private boolean doesItemMatch(Object object, ItemStack input) {
        boolean match = false;
        if (object instanceof ItemStack itemStack) {
            if (input.getItem() == itemStack.getItem() && input.getItemDamage() == itemStack.getItemDamage()) {
                if (input.stackTagCompound != itemStack.stackTagCompound) {
                    if (input.stackTagCompound != null && itemStack.stackTagCompound != null) {
                        match = input.stackTagCompound.equals(itemStack.stackTagCompound);
                    }
                } else {
                    match = true;
                }
            }
        } else if (object instanceof List list) {
            for (Object object1 : list) {
                if (doesItemMatch(object1, input)) {
                    match = true;
                    break;
                }
            }
        } else if (object instanceof String oreName) {
            if (OreDictionary.doesOreNameExist(oreName)) {
                int[] inputOres = OreDictionary.getOreIDs(input);
                int oreID = OreDictionary.getOreID(oreName);
                for (int inputOre : inputOres) {
                    if (inputOre == oreID) {
                        match = true;
                        break;
                    }
                }
            }
        }
        return match;
    }

    public List<Object> getIngredients() {
        return ingredients;
    }

    public ItemStack getRecipeOutput() {
        return output;
    }

    public ItemStack getOutputContainer() {
        return container;
    }

    public float getExperience() {
        return experience;
    }

    public int getCookTime() {
        return cookTime;
    }
}
