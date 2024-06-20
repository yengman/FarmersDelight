package com.vectorwing.farmersdelight;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.vectorwing.farmersdelight.client.gui.GuiCookingPot;
import com.vectorwing.farmersdelight.common.inventory.ContainerCookingPot;
import com.vectorwing.farmersdelight.common.registry.ModGuis;
import com.vectorwing.farmersdelight.common.tile.TileCookingPot;

import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        switch (ID) {
            case ModGuis.GUI_COOKING_POT -> {
                if (tileEntity instanceof TileCookingPot tileCookingPot) {
                    return new ContainerCookingPot(player.inventory, tileCookingPot);
                }
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        switch (ID) {
            case ModGuis.GUI_COOKING_POT -> {
                if (tileEntity instanceof TileCookingPot tileCookingPot) {
                    return new GuiCookingPot(player.inventory, tileCookingPot);
                }
            }
        }
        return null;
    }
}
