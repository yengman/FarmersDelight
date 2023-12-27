package com.vectorwing.farmersdelight.common.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.vectorwing.farmersdelight.FarmersDelight;
import com.vectorwing.farmersdelight.Tags;
import com.vectorwing.farmersdelight.client.renderer.block.RenderCookingPot;
import com.vectorwing.farmersdelight.common.registry.ModGuis;
import com.vectorwing.farmersdelight.common.tile.TileCookingPot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCookingPot extends BlockContainer {

    private Random random = new Random();

    public IIcon[] icon = new IIcon[3];

    public IIcon iconSpoon;

    public IIcon iconHandle;

    public BlockCookingPot() {
        super(Material.iron);
        setCreativeTab(CreativeTabs.tabBlock);
        // TODO 0.125F = 1/8
        setBlockBounds(0.125F, 0F, 0.125F, 0.875F, 0.625F, 0.875F);
        setHardness(2.0F);
        setResistance(1.0F);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        int facing = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        switch (facing) {
            case 0 -> worldIn.setBlockMetadataWithNotify(x, y, z, 2, 2);
            case 1 -> worldIn.setBlockMetadataWithNotify(x, y, z, 5, 2);
            case 2 -> worldIn.setBlockMetadataWithNotify(x, y, z, 3, 2);
            case 3 -> worldIn.setBlockMetadataWithNotify(x, y, z, 4, 2);
        }

    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, int x, int y, int z, int meta) {
        player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
        player.addExhaustion(0.025F);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block blockBroken, int meta) {
        if (world.getTileEntity(x, y, z) instanceof TileCookingPot tileCookingPot) {
            List<ItemStack> list = new ArrayList<>(tileCookingPot.getDroppableInventory());

            for (ItemStack itemStack : list) {
                if (itemStack != null) {
                    float f = random.nextFloat() * 0.8F + 0.1F;
                    float f1 = random.nextFloat() * 0.8F + 0.1F;
                    EntityItem entityItem;

                    for (float f2 = random.nextFloat() * 0.8F + 0.1F; itemStack.stackSize > 0; world
                        .spawnEntityInWorld(entityItem)) {
                        int j1 = random.nextInt(21) + 10;

                        if (j1 > itemStack.stackSize) {
                            j1 = itemStack.stackSize;
                        }

                        itemStack.stackSize -= j1;
                        entityItem = new EntityItem(
                            world,
                            (double) ((float) x + f),
                            (double) ((float) y + f1),
                            (double) ((float) z + f2),
                            new ItemStack(itemStack.getItem(), j1, itemStack.getItemDamage()));
                        float f3 = 0.05F;
                        entityItem.motionX = (double) ((float) random.nextGaussian() * f3);
                        entityItem.motionY = (double) ((float) random.nextGaussian() * f3 + 0.2F);
                        entityItem.motionZ = (double) ((float) random.nextGaussian() * f3);

                        if (itemStack.hasTagCompound()) {
                            entityItem.getEntityItem()
                                .setTagCompound(
                                    (NBTTagCompound) itemStack.getTagCompound()
                                        .copy());
                        }
                    }
                }
            }
        }
        super.breakBlock(world, x, y, z, blockBroken, meta);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (!player.capabilities.isCreativeMode && canHarvestBlock(player, world.getBlockMetadata(x, y, z))) {
            List<ItemStack> drops = getDrops(world, player, x, y, z);
            if (!world.setBlockToAir(x, y, z)) {
                return false;
            } else {
                if (!world.isRemote) {
                    for (ItemStack drop : drops) {
                        dropBlockAsItem(world, x, y, z, drop);
                    }
                }
                // TODO this doesn;t work, TE vanishes before meal is read

                return true;
            }
        } else {
            return super.removedByPlayer(world, player, x, y, z, willHarvest);
        }
    }

    @Override
    protected void dropBlockAsItem(World worldIn, int x, int y, int z, ItemStack itemIn) {
        if (!worldIn.isRemote && worldIn.getGameRules().getGameRuleBooleanValue("doTileDrops") && !worldIn.restoringBlockSnapshots){
            if (captureDrops.get()) {
                capturedDrops.get().add(itemIn);
                return;
            }
            float f = 0.7F;
            double d0 = (double)(worldIn.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            double d1 = (double)(worldIn.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            double d2 = (double)(worldIn.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            EntityItem entityitem = new EntityItem(worldIn, (double)x + d0, (double)y + d1, (double)z + d2, itemIn);
            entityitem.delayBeforeCanPickup = 10;
            if (itemIn.hasTagCompound()) {
                entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemIn.getTagCompound().copy());
            }
            worldIn.spawnEntityInWorld(entityitem);
        }
    }

    private List<ItemStack> getDrops(World world, EntityPlayer player, int x, int y, int z) {
        List<ItemStack> list = new ArrayList<>();
        ItemStack itemStack = new ItemStack(this, 1);
        if (world.getTileEntity(x, y, z) instanceof TileCookingPot tileCookingPot) {
            NBTTagCompound compound = tileCookingPot.writeMeal();
            if (!compound.hasNoTags()) {
                itemStack.setTagCompound(compound);
            }
        }
        list.add(itemStack);
        return list;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileCookingPot();
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        if (!worldIn.isRemote) {
            player.openGui(FarmersDelight.INSTANCE, ModGuis.GUI_COOKING_POT, worldIn, x, y, z);
        }
        return true;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return RenderCookingPot.RENDER_ID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        icon[0] = reg.registerIcon(Tags.MODID + ":cooking_pot_bottom");
        icon[1] = reg.registerIcon(Tags.MODID + ":cooking_pot_top");
        icon[2] = reg.registerIcon(Tags.MODID + ":cooking_pot_side");
        iconHandle = reg.registerIcon(Tags.MODID + ":cooking_pot_handle");
        iconSpoon = reg.registerIcon(Tags.MODID + ":cooking_pot_spoon");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        IIcon ret;
        switch (side) {
            case 0 -> ret = icon[0];
            case 1 -> ret = icon[1];
            default -> ret = icon[2];
        }
        return ret;
    }

}
