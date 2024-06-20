package com.vectorwing.farmersdelight.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.vectorwing.farmersdelight.Config;
import com.vectorwing.farmersdelight.common.tile.TileCookingPot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerCookingPot extends Container {

    private TileCookingPot tileCookingPot;

    private int lastCookTime;
    private int lastCookTimeTotal;

    public ContainerCookingPot(InventoryPlayer inventoryPlayer, TileCookingPot tileCookingPot) {
        this.tileCookingPot = tileCookingPot;

        // Ingredient Slots - 2 Rows x 3 Columns
        int startX = 8;
        int startY = 18;
        int inputStartX = 30;
        int inputStartY = 17;
        int borderSlotSize = 18;
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                addSlotToContainer(
                    new Slot(
                        tileCookingPot,
                        (row * 3) + column,
                        inputStartX + (column * borderSlotSize),
                        inputStartY + (row * borderSlotSize)));
            }
        }

        // Meal Display
        addSlotToContainer(new SlotUninteractable(tileCookingPot, 6, 124, 26));

        // Bowl Input
        addSlotToContainer(new Slot(tileCookingPot, 7, 92, 55));

        // Bowl Output
        addSlotToContainer(new SlotCookingPotOutput(inventoryPlayer.player, tileCookingPot, 8, 124, 55));

        // Main Player Inventory
        int startPlayerInvY = startY * 4 + 12;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(
                    new Slot(
                        inventoryPlayer,
                        column + row * 9 + 9,
                        startX + column * borderSlotSize,
                        startPlayerInvY + row * borderSlotSize));
            }
        }

        // Hotbar
        for (int column = 0; column < 9; column++) {
            addSlotToContainer(new Slot(inventoryPlayer, column, startX + column * borderSlotSize, 142));
        }

    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        int indexMealDisplay = TileCookingPot.MEAL_DISPLAY_SLOT;
        int indexContainerInput = TileCookingPot.CONTAINER_SLOT;
        int indexOutput = TileCookingPot.OUTPUT_SLOT;
        int startPlayerInv = indexOutput + 1;
        int endPlayerInv = startPlayerInv + 36;
        ItemStack itemStackCopy = null;
        Slot slot = (Slot) inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack = slot.getStack();
            itemStackCopy = itemstack.copy();
            if (index == indexOutput) {
                if (!mergeItemStack(itemstack, startPlayerInv, endPlayerInv, true)) {
                    return null;
                }
                slot.onSlotChange(itemstack, itemStackCopy);
            } else if (index > indexOutput) {
                boolean isValidContainer = Config.CONTAINERS.contains(itemstack.getItem())
                    || (tileCookingPot.getContainer() != null && tileCookingPot.getContainer()
                        .getItem() == itemstack.getItem());
                boolean isListedContainer = Config.CONTAINERS.contains(itemstack.getItem());
                Slot containerSlot = (Slot) inventorySlots.get(indexContainerInput);

                // attempting to fix odd behavior
                if (isValidContainer) {
                    if (containerSlot.getHasStack()) {
                        if (containerSlot.getStack()
                            .getItem() == itemstack.getItem()) {
                            if (!mergeItemStack(itemstack, indexContainerInput, indexContainerInput + 1, false)) {
                                return null;
                            }
                        } else if (!mergeItemStack(itemstack, 0, indexMealDisplay, false)) {
                            return null;
                        }

                    } else if (!mergeItemStack(itemstack, indexContainerInput, indexContainerInput + 1, false)) {
                        return null;
                    }
                } else if (!mergeItemStack(itemstack, 0, indexMealDisplay, false)) {
                    return null;
                }

            } else if (!mergeItemStack(itemstack, startPlayerInv, endPlayerInv, false)) {
                return null;
            }

            if (itemstack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack.stackSize == itemStackCopy.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, itemstack);
        }
        return itemStackCopy;
    }

    @Override
    public void addCraftingToCrafters(ICrafting p_75132_1_) {
        super.addCraftingToCrafters(p_75132_1_);
        p_75132_1_.sendProgressBarUpdate(this, 0, tileCookingPot.getCookTime());
        p_75132_1_.sendProgressBarUpdate(this, 1, tileCookingPot.getCookTimeTotal());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (Object object : crafters) {
            ICrafting crafter = (ICrafting) object;

            if (lastCookTime != tileCookingPot.getCookTime()) {
                crafter.sendProgressBarUpdate(this, 0, tileCookingPot.getCookTime());
            }

            if (lastCookTimeTotal != tileCookingPot.getCookTimeTotal()) {
                crafter.sendProgressBarUpdate(this, 1, tileCookingPot.getCookTimeTotal());
            }

        }
        lastCookTime = tileCookingPot.getCookTime();
        lastCookTimeTotal = tileCookingPot.getCookTimeTotal();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int p_75137_1_, int p_75137_2_) {
        switch (p_75137_1_) {
            case 0 -> tileCookingPot.setCookTime(p_75137_2_);
            case 1 -> tileCookingPot.setCookTimeTotal(p_75137_2_);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tileCookingPot.isUseableByPlayer(player);
    }

}
