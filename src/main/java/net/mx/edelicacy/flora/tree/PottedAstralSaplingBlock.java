package net.mx.edelicacy.flora.tree;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.registry.DelicacyBlocks;

/**
 * 星辰树苗盆栽（1.12 原版花盆放不进模组树苗，由 FlowerPotHandler 把空花盆换成本方块）。
 * 右键取出树苗并还原为空花盆；打掉掉落花盆与树苗。
 */
public class PottedAstralSaplingBlock extends Block {

    private static final AxisAlignedBB POT_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

    public PottedAstralSaplingBlock() {
        super(Material.CIRCUITS);
        setHardness(0.0F);
        setLightLevel(8.0F / 15.0F);
    }

    private static ItemStack sapling() {
        return new ItemStack(DelicacyBlocks.ASTRAL_SAPLING);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            ItemStack stack = sapling();
            if (!player.inventory.addItemStackToInventory(stack)) {
                player.dropItem(stack, false);
            }
            world.setBlockState(pos, Blocks.FLOWER_POT.getDefaultState(), 3);
        }
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(new ItemStack(Items.FLOWER_POT));
        drops.add(sapling());
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return sapling();
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && hasSupport(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!hasSupport(world, pos)) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    /** 与原版花盆相同：下方顶面实心 */
    private static boolean hasSupport(World world, BlockPos pos) {
        BlockPos below = pos.down();
        IBlockState down = world.getBlockState(below);
        return down.isTopSolid() || down.getBlockFaceShape(world, below, EnumFacing.UP) == BlockFaceShape.SOLID;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return POT_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextInt(4) == 0) {
            StarDust.spawn(world, rand, pos.getX() + 0.01D + 0.8D * rand.nextFloat(), pos.getY() + 0.5D + 0.4D * rand.nextFloat(),
                    pos.getZ() + 0.01D + 0.8D * rand.nextFloat());
        }
    }
}
