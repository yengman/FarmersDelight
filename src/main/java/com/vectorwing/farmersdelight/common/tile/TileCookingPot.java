package com.vectorwing.farmersdelight.common.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

import com.vectorwing.farmersdelight.common.crafting.CraftingManager;
import com.vectorwing.farmersdelight.common.crafting.RecipeCookingPot;
import com.vectorwing.farmersdelight.common.utility.ItemUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileCookingPot extends TileEntity implements ISidedInventory {

    public static final int MEAL_DISPLAY_SLOT = 6;
    public static final int CONTAINER_SLOT = 7;
    public static final int OUTPUT_SLOT = 8;
    private static final int INVENTORY_SIZE = OUTPUT_SLOT + 1;
    private static final int MAX_STACK_SIZE = 64;

    private ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];

    private int cookTime;

    private int cookTimeTotal;
    private ItemStack mealContainerStack;

    private String customName;

    private RecipeCookingPot lastRecipe;

    private boolean checkNewRecipe = true;

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        cookTime = compound.getInteger("CookTime");
        cookTimeTotal = compound.getInteger("CookTimeTotal");
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound nbtTagCompound = nbttaglist.getCompoundTagAt(i);
            byte slot = nbtTagCompound.getByte("Slot");
            if (slot >= 0 && slot < inventory.length) inventory[slot] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
        mealContainerStack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("Container"));
        if (compound.hasKey("CustomName", 8)) {
            customName = compound.getString("CustomName");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("CookTime", cookTime);
        compound.setInteger("CookTimeTotal", cookTimeTotal);
        NBTTagList nbtTagList = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                nbtTagCompound.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(nbtTagCompound);
                nbtTagList.appendTag(nbtTagCompound);
            }
        }
        compound.setTag("Items", nbtTagList);
        NBTTagCompound container = new NBTTagCompound();
        if (mealContainerStack != null) {
            mealContainerStack.writeToNBT(container);
        }
        compound.setTag("Container", container);
        if (hasCustomInventoryName()) {
            compound.setString("CustomName", customName);
        }

    }

    @Override
    public void updateEntity() {
        boolean heated = isHeated();
        if (!worldObj.isRemote) {
            boolean didInventoryChange = false;
            if (heated && hasInput()) {
                RecipeCookingPot matchingRecipe = getMatchingRecipe(this);
                if (matchingRecipe != null && canCook(matchingRecipe)) {
                    didInventoryChange = processCooking(matchingRecipe);
                } else {
                    cookTime = 0;
                }
            } else if (cookTime > 0) {
                cookTime = MathHelper.clamp_int(cookTime - 2, 0, cookTimeTotal);
            }
            ItemStack mealStack = getMeal();
            if (mealStack != null) {
                if (!doesMealHaveContainer(mealStack)) {
                    moveMealToOutput();
                    didInventoryChange = true;
                } else if (inventory[CONTAINER_SLOT] != null) {
                    useStoredContainersOnMeal();
                    didInventoryChange = true;
                }
            }
            if (didInventoryChange) {
                markDirty();
            }
            // TODO CLIENT_SIDE EFFECTS
        } else if (heated) {

        }
    }

    private boolean isHeated() {
        // TODO
        return true;
    }

    private boolean hasInput() {
        for (int i = 0; i < MEAL_DISPLAY_SLOT; i++) {
            if (inventory[i] != null) return true;
        }
        return false;
    }

    private RecipeCookingPot getMatchingRecipe(IInventory inventory) {

        if (lastRecipe != null) {
            if (lastRecipe.matches(inventory)) {
                return lastRecipe;
            }
            if (getMeal() != null && lastRecipe.getRecipeOutput()
                .getItem() == getMeal().getItem()) {
                return null;
            }
        }

        if (checkNewRecipe) {
            RecipeCookingPot matchingRecipe = CraftingManager.findMatchingCookingPotRecipe(inventory);
            if (matchingRecipe != null) {
                if (lastRecipe != null && !lastRecipe.equals(matchingRecipe)) {
                    cookTime = 0;
                }
                lastRecipe = matchingRecipe;
                return matchingRecipe;
            }

        }
        checkNewRecipe = false;
        return null;
    }

    private boolean canCook(RecipeCookingPot recipe) {
        if (hasInput()) {
            ItemStack resultStack = recipe.getRecipeOutput();
            if (resultStack == null) {
                return false;
            } else {
                ItemStack storedMealStack = getMeal();
                if (storedMealStack == null) {
                    return true;
                } else if (!storedMealStack.isItemEqual(resultStack)) {
                    return false;
                } else if (storedMealStack.stackSize + resultStack.stackSize <= MAX_STACK_SIZE) {
                    return true;
                } else {
                    return storedMealStack.stackSize + resultStack.stackSize <= resultStack.getMaxStackSize();
                }
            }
        } else {
            return false;
        }
    }

    private boolean processCooking(RecipeCookingPot recipe) {
        ++cookTime;
        cookTimeTotal = recipe.getCookTime();
        if (cookTime < cookTimeTotal) {
            return false;
        }
        cookTime = 0;
        mealContainerStack = recipe.getOutputContainer();
        ItemStack resultStack = recipe.getRecipeOutput();
        ItemStack storedMealStack = getMeal();
        if (storedMealStack == null) {
            inventory[MEAL_DISPLAY_SLOT] = resultStack.copy();
        } else if (storedMealStack.isItemEqual(resultStack)) {
            storedMealStack.stackSize += resultStack.stackSize;
        }

        // TODO setRecipeUsed

        for (int slot = 0; slot < MEAL_DISPLAY_SLOT; slot++) {
            ItemStack itemStack = inventory[slot];
            if (itemStack != null) {
                Item item = itemStack.getItem();
                assert item != null;
                if (item.hasContainerItem(itemStack)) {
                    // EJECT ITEM HERE
                    ejectIngredientRemainder(item.getContainerItem(itemStack));
                } else if (false) {
                    // TODO INGREDIENT_REMAINDER_OVERRIDES

                }
                itemStack.stackSize--;
                if (itemStack.stackSize <= 0) {
                    inventory[slot] = null;
                }
            }

        }
        return true;
    }

    private ItemStack getMeal() {
        return inventory[MEAL_DISPLAY_SLOT];
    }

    // Checks if cooked meal has an available container or is already in one by default
    private boolean doesMealHaveContainer(ItemStack meal) {
        return mealContainerStack != null || meal.getItem()
            .hasContainerItem(meal);
    }

    private void moveMealToOutput() {
        ItemStack mealStack = getMeal();
        ItemStack outputStack = inventory[OUTPUT_SLOT];
        int outputCount = outputStack == null ? 0 : outputStack.stackSize;
        int mealCount = Math.min(mealStack.stackSize, mealStack.getMaxStackSize() - outputCount);
        if (outputCount == 0) {
            inventory[OUTPUT_SLOT] = mealStack.splitStack(mealCount);
        } else if (outputStack.getItem() == mealStack.getItem()) {
            mealStack.stackSize -= mealCount;
            outputStack.stackSize += mealCount;
        }
        if (mealStack.stackSize <= 0) {
            inventory[MEAL_DISPLAY_SLOT] = null;
        }

    }

    private void useStoredContainersOnMeal() {
        ItemStack mealStack = getMeal();
        ItemStack containerInputStack = inventory[CONTAINER_SLOT];
        ItemStack outputStack = inventory[OUTPUT_SLOT];
        if (isContainerValid(containerInputStack)) {
            int smallerStackCount = Math.min(mealStack.stackSize, containerInputStack.stackSize);
            if (outputStack == null) {
                int mealCount = Math.min(smallerStackCount, mealStack.getMaxStackSize());
                containerInputStack.stackSize -= mealCount;
                inventory[OUTPUT_SLOT] = mealStack.splitStack(mealCount);
            } else if (outputStack.getItem() == mealStack.getItem()) {
                int mealCount = Math.min(smallerStackCount, mealStack.getMaxStackSize() - outputStack.stackSize);
                mealStack.stackSize -= mealCount;
                containerInputStack.stackSize -= mealCount;
                outputStack.stackSize += mealCount;
            }

            if (mealStack.stackSize <= 0) {
                inventory[MEAL_DISPLAY_SLOT] = null;
            }

            if (containerInputStack.stackSize <= 0) {
                inventory[CONTAINER_SLOT] = null;
            }
        }
    }

    private boolean isContainerValid(ItemStack containerItem) {
        if (containerItem == null) return false;
        if (mealContainerStack != null) {
            return mealContainerStack.isItemEqual(containerItem);
        } else {
            return getMeal().getItem()
                .getContainerItem() == containerItem.getItem();
        }
    }

    private void ejectIngredientRemainder(ItemStack itemStack) {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        ForgeDirection direction = ForgeDirection.getOrientation(meta);
        double x = xCoord + 0.5D + (direction.offsetX * 0.25D);
        double y = yCoord + 0.7D;
        double z = zCoord + 0.5D + (direction.offsetZ * 0.25D);
        ItemUtils
            .spawnEntityItem(worldObj, itemStack, x, y, z, direction.offsetX * 0.08D, 0.25D, direction.offsetZ * 0.08D);
    }

    public List<ItemStack> getDroppableInventory() {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (i != MEAL_DISPLAY_SLOT) {
                ItemStack itemStack = inventory[i];
                if (itemStack != null) {
                    drops.add(itemStack);
                }
            }
        }
        return drops;
    }

    public NBTTagCompound writeMeal() {
        NBTTagCompound compound = new NBTTagCompound();
        if (getMeal() != null) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            NBTTagList nbtTagList = new NBTTagList();
            nbtTagCompound.setByte("Slot", (byte) MEAL_DISPLAY_SLOT);
            inventory[MEAL_DISPLAY_SLOT].writeToNBT(nbtTagCompound);
            nbtTagList.appendTag(nbtTagCompound);
            compound.setTag("Items", nbtTagList);
        }
        if (mealContainerStack != null) {
            NBTTagCompound container = new NBTTagCompound();
            mealContainerStack.writeToNBT(container);
            compound.setTag("Container", container);
        }
        if (hasCustomInventoryName()) {
            compound.setString("CustomName", customName);
        }
        return compound;
    }

    private void didIngredientsChange(int slot) {
        if (slot >= 0 && slot < MEAL_DISPLAY_SLOT) {
            checkNewRecipe = true;
        }
        markDirty();
    }

    @Override
    public int getSizeInventory() {
        return INVENTORY_SIZE;
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {
        return inventory[slotIn];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack newStack = null;
        if (inventory[index] != null) {
            if (inventory[index].stackSize <= count) {
                ItemStack itemStack = inventory[index];
                inventory[index] = null;
                newStack = itemStack;
            } else {
                ItemStack itemStack = inventory[index].splitStack(count);
                if (inventory[index].stackSize == 0) {
                    inventory[index] = null;
                }
                newStack = itemStack;
            }
        }
        if (index < MEAL_DISPLAY_SLOT) {
            checkNewRecipe = true;
        }
        return newStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        if (inventory[index] != null) {
            ItemStack itemStack = inventory[index];
            inventory[index] = null;
            return itemStack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack itemStack) {
        inventory[index] = itemStack;
        if (itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
        didIngredientsChange(index);
    }

    @Override
    public String getInventoryName() {
        return hasCustomInventoryName() ? customName : "container.cooking_pot";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return customName != null && !customName.isEmpty();
    }

    @Override
    public int getInventoryStackLimit() {
        return MAX_STACK_SIZE;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
        return false;
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
        return false;
    }

    public int getCookTime() {
        return cookTime;
    }

    public int getCookTimeTotal() {
        return cookTimeTotal;
    }

    @SideOnly(Side.CLIENT)
    public void setCookTime(int cookTime) {
        this.cookTime = cookTime;
    }

    @SideOnly(Side.CLIENT)
    public void setCookTimeTotal(int cookTimeTotal) {
        this.cookTimeTotal = cookTimeTotal;
    }

    @SideOnly(Side.CLIENT)
    public int getCookProgressScaled() {
        return cookTime != 0 && cookTimeTotal != 0 ? cookTime * 24 / cookTimeTotal : 0;
    }

}
