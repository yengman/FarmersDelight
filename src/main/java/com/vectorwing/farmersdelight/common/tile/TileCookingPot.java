package com.vectorwing.farmersdelight.common.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.vectorwing.farmersdelight.Config;
import com.vectorwing.farmersdelight.client.particle.ModParticles;
import com.vectorwing.farmersdelight.common.block.IHeatableBlock;
import com.vectorwing.farmersdelight.common.crafting.CraftingManager;
import com.vectorwing.farmersdelight.common.crafting.RecipeCookingPot;
import com.vectorwing.farmersdelight.common.item.ItemBlockCookingPot;
import com.vectorwing.farmersdelight.common.utility.ItemUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileCookingPot extends TileEntity implements ISidedInventory, IHeatableBlock {

    public static final int MEAL_DISPLAY_SLOT = 6;
    public static final int CONTAINER_SLOT = 7;
    public static final int OUTPUT_SLOT = 8;
    private static final int INVENTORY_SIZE = OUTPUT_SLOT + 1;
    private static final int MAX_STACK_SIZE = 64;

    private static final String DEFAULT_INVENTORY_NAME = "container.farmersdelight.cooking_pot";

    private static final int[] TOP_SLOT_ACCESS = IntStream.range(0, MEAL_DISPLAY_SLOT - 1)
        .toArray();
    private static final int[] OTHER_SLOT_ACCESS = new int[] { CONTAINER_SLOT, OUTPUT_SLOT };

    private ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];

    private int cookTime;

    private int cookTimeTotal;
    private ItemStack mealContainerStack;

    private String customName;

    private RecipeCookingPot lastRecipe;

    private boolean checkNewRecipe = true;

    // The vanilla furnace stores exp but does not give it on break, only on inventory retrieval (SlotFurnace)
    private float storedExperience;

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

        } else if (heated) {
            Random random = worldObj.rand;
            if (random.nextFloat() < 0.2F) {
                double x = (double) xCoord + 0.5D + (random.nextDouble() * 0.6D - 0.3D);
                double y = (double) yCoord + 0.7D;
                double z = (double) zCoord + 0.5D + (random.nextDouble() * 0.6D - 0.3D);
                ModParticles.spawnBubblePopParticle(worldObj, x, y, z, 0.0D, 0.0D, 0.0D);
            }
            if (random.nextFloat() < 0.05F) {
                double x = (double) xCoord + 0.5D + (random.nextDouble() * 0.4D - 0.2D);
                double y = (double) yCoord + 0.5D + 0.3D;
                double z = (double) zCoord + 0.5D + (random.nextDouble() * 0.4D - 0.2D);
                double motionY = random.nextBoolean() ? 0.015D : 0.005D;
                ModParticles.spawnSteamParticle(worldObj, x, y, z, 0.0D, motionY, 0.0D);
            }
        }
    }

    public boolean isHeated() {
        return isHeated(worldObj, xCoord, yCoord, zCoord);
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

        storedExperience += recipe.getExperience();

        for (int slot = 0; slot < MEAL_DISPLAY_SLOT; slot++) {
            ItemStack itemStack = inventory[slot];
            if (itemStack != null) {
                Item item = itemStack.getItem();
                assert item != null;
                if (item.hasContainerItem(itemStack)) {
                    ejectIngredientRemainder(item.getContainerItem(itemStack));
                } else if (Config.INGREDIENT_REMAINDER_OVERRIDES.containsKey(item)) {
                    ejectIngredientRemainder(new ItemStack(Config.INGREDIENT_REMAINDER_OVERRIDES.get(item)));

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
        } else if (getMeal() != null) {
            return getMeal().getItem()
                .getContainerItem() == containerItem.getItem();
        }
        return false;
    }

    private void ejectIngredientRemainder(ItemStack itemStack) {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        ForgeDirection rotation = ForgeDirection.getOrientation(meta)
            .getRotation(ForgeDirection.DOWN);
        double x = xCoord + 0.5D;
        double y = yCoord + 0.7D;
        double z = zCoord + 0.5D;
        ItemUtils
            .spawnEntityItem(worldObj, itemStack, x, y, z, rotation.offsetX * 0.08D, 0.25D, rotation.offsetZ * 0.08D);
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

    public void setMealContainerStack(ItemStack itemStack) {
        mealContainerStack = itemStack;
    }

    public NBTTagCompound writeMeal() {
        NBTTagCompound compound = new NBTTagCompound();
        ItemStack meal = getMeal();
        if (meal != null && meal.stackSize > 0) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            NBTTagList nbtTagList = new NBTTagList();
            nbtTagCompound.setByte("Slot", (byte) MEAL_DISPLAY_SLOT);
            inventory[MEAL_DISPLAY_SLOT].writeToNBT(nbtTagCompound);
            nbtTagList.appendTag(nbtTagCompound);
            compound.setTag("Items", nbtTagList);
            if (mealContainerStack != null) {
                NBTTagCompound container = new NBTTagCompound();
                mealContainerStack.writeToNBT(container);
                compound.setTag("Container", container);
            }
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

    public void awardExperience(EntityPlayer entityPlayer) {
        int expTotal = MathHelper.floor_float(storedExperience);
        float expFraction = storedExperience - (float) expTotal;
        if (expFraction != 0.0F && Math.random() < (double) expFraction) {
            expTotal++;
        }
        World world = entityPlayer.worldObj;
        worldObj.spawnEntityInWorld(
            new EntityXPOrb(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ + 0.5D, expTotal));
    }

    public ItemStack getContainer() {
        ItemStack mealStack = getMeal();
        if (mealStack != null) {
            return mealContainerStack;
        }
        return null;
    }

    public ItemStack useHeldItemOnMeal(ItemStack container) {
        if (isContainerValid(container)) {
            ItemStack mealStack = getMeal();
            if (mealStack != null) {
                container.stackSize--;
                ItemStack portion = mealStack.splitStack(1);
                if (mealStack.stackSize <= 0) {
                    inventory[MEAL_DISPLAY_SLOT] = null;
                }
                markDirty();
                return portion;
            }
        }
        return null;
    }

    public static ItemStack getMealFromItem(ItemStack itemStack) {
        if (itemStack != null) {
            Item item = itemStack.getItem();
            if (item instanceof ItemBlockCookingPot) {
                NBTTagCompound compound = itemStack.getTagCompound();
                if (compound.hasKey("Items")) {
                    NBTTagList items = compound.getTagList("Items", 10);
                    for (int i = 0; i < items.tagCount(); i++) {
                        NBTTagCompound nbtTagCompound = items.getCompoundTagAt(i);
                        byte slot = nbtTagCompound.getByte("Slot");
                        if (slot == MEAL_DISPLAY_SLOT) {
                            return ItemStack.loadItemStackFromNBT(nbtTagCompound);
                        }
                    }
                }
            }
        }
        return null;
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
        return hasCustomInventoryName() ? customName : DEFAULT_INVENTORY_NAME;
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
        return (index < MEAL_DISPLAY_SLOT || index == CONTAINER_SLOT);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return side == 1 ? TOP_SLOT_ACCESS : OTHER_SLOT_ACCESS;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack p_102007_2_, int side) {
        return side == 1 ? slot < MEAL_DISPLAY_SLOT : slot == CONTAINER_SLOT;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack p_102008_2_, int side) {
        return side == 1 ? slot < MEAL_DISPLAY_SLOT : slot == OUTPUT_SLOT;
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
