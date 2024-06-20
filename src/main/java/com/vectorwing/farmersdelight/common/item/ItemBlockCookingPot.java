package com.vectorwing.farmersdelight.common.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import com.vectorwing.farmersdelight.common.tile.TileCookingPot;

public class ItemBlockCookingPot extends ItemBlock {

    private static final String SINGLE_SERVING = "tooltip.farmersdelight.cooking_pot.single_serving";
    private static final String MANY_SERVINGS = "tooltip.farmersdelight.cooking_pot.many_servings";

    public ItemBlockCookingPot(Block p_i45328_1_) {
        super(p_i45328_1_);
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean p_77624_4_) {
        if (itemStack.hasTagCompound()) {
            ItemStack meal = TileCookingPot.getMealFromItem(itemStack);
            if (meal != null) {
                int stackSize = meal.stackSize;
                String toolTip1 = I18n.format(stackSize == 1 ? SINGLE_SERVING : MANY_SERVINGS);
                info.add(stackSize + toolTip1 + meal.getDisplayName());
            }
        }
    }

}
