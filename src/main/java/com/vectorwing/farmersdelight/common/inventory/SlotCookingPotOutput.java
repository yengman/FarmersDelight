package com.vectorwing.farmersdelight.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotCookingPotOutput extends Slot {

    private EntityPlayer entityPlayer;

    private int removeCount;

    public SlotCookingPotOutput(EntityPlayer entityPlayer, IInventory inventory, int p_i1824_2_, int p_i1824_3_,
        int p_i1824_4_) {
        super(inventory, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        this.entityPlayer = entityPlayer;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int p_75209_1_) {
        if (getHasStack()) {
            removeCount += Math.min(p_75209_1_, getStack().stackSize);
        }
        return super.decrStackSize(p_75209_1_);
    }

    @Override
    protected void onCrafting(ItemStack p_75210_1_, int p_75210_2_) {
        removeCount += p_75210_2_;
        super.onCrafting(p_75210_1_, p_75210_2_);
    }

}
