package com.vectorwing.farmersdelight.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.vectorwing.farmersdelight.common.tile.TileCookingPot;

public class SlotCookingPotOutput extends Slot {

    private final TileCookingPot tileCookingPot;
    private final EntityPlayer entityPlayer;

    private int removeCount;

    public SlotCookingPotOutput(EntityPlayer entityPlayer, TileCookingPot tileCookingPot, int p_i1824_2_,
        int p_i1824_3_, int p_i1824_4_) {
        super(tileCookingPot, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        this.tileCookingPot = tileCookingPot;
        this.entityPlayer = entityPlayer;

    }

    @Override
    public boolean isItemValid(ItemStack itemStack) {
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
    protected void onCrafting(ItemStack itemStack, int p_75210_2_) {
        removeCount += p_75210_2_;
        World world = entityPlayer.worldObj;
        itemStack.onCrafting(world, entityPlayer, removeCount);
        if (!world.isRemote) {
            tileCookingPot.awardExperience(entityPlayer);
        }
        removeCount = 0;
    }

}
