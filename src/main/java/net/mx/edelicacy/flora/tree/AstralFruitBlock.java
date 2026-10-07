package net.mx.edelicacy.flora.tree;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.util.WorldHelper;

/**
 * 悬挂的星辰果：挂在星辰树叶下方，0~7 阶段，亮度 8。成熟后右键或打掉得到神秘遗物的星辰果；
 * 上方不再是星辰树叶时直接消失（不掉落，同 1.21）。
 */
public class AstralFruitBlock extends Block {

    public static final int MAX_AGE = 7;
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, MAX_AGE);
    private static final ResourceLocation EL_ASTRAL_FRUIT = new ResourceLocation("enigmaticlegacy", "astral_fruit");

    private static final AxisAlignedBB SMALL = box(7.0D, 14.5D, 7.0D, 9.0D, 16.0D, 9.0D);
    private static final AxisAlignedBB MEDIUM = box(6.0D, 10.5D, 6.0D, 10.0D, 14.5D, 10.0D);
    private static final AxisAlignedBB LARGE = box(5.0D, 8.0D, 5.0D, 11.0D, 14.0D, 11.0D);

    public AstralFruitBlock() {
        super(Material.PLANTS);
        setSoundType(SoundType.WOOD);
        setHardness(0.0F);
        setLightLevel(8.0F / 15.0F);
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState().withProperty(AGE, 0));
    }

    private static AxisAlignedBB box(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AxisAlignedBB(x1 / 16.0D, y1 / 16.0D, z1 / 16.0D, x2 / 16.0D, y2 / 16.0D, z2 / 16.0D);
    }

    public static boolean isMature(IBlockState state) {
        return state.getValue(AGE) >= MAX_AGE;
    }

    @Nullable
    private static Item astralFruitItem() {
        return ForgeRegistries.ITEMS.getValue(EL_ASTRAL_FRUIT);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) {
            return;
        }
        if (!canBlockStay(world, pos)) {
            world.setBlockToAir(pos);
            return;
        }
        int age = state.getValue(AGE);
        if (age >= MAX_AGE || !WorldHelper.isAreaLoaded(world, pos, 1) || world.getLight(pos) < 9) {
            return;
        }
        if (ForgeHooks.onCropsGrowPre(world, pos, state, rand.nextInt(15) == 0)) {
            world.setBlockState(pos, state.withProperty(AGE, age + 1), 2);
            ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!isMature(state)) {
            return false;
        }
        if (!world.isRemote) {
            world.destroyBlock(pos, true);
        }
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Item fruit = astralFruitItem();
        if (isMature(state) && fruit != null) {
            drops.add(new ItemStack(fruit));
        }
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        Item fruit = astralFruitItem();
        return fruit == null ? ItemStack.EMPTY : new ItemStack(fruit);
    }

    public boolean canBlockStay(World world, BlockPos pos) {
        return AstralLeavesBlock.isAstralLeaves(world.getBlockState(pos.up()).getBlock());
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && canBlockStay(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!world.isRemote && !canBlockStay(world, pos)) {
            world.setBlockToAir(pos);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        int age = state.getValue(AGE);
        return age < 4 ? SMALL : age < MAX_AGE ? MEDIUM : LARGE;
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
        if (rand.nextInt(10) == 0 && isMature(state)) {
            StarDust.spawn(world, rand, pos.getX() + 0.25D + 0.5D * rand.nextFloat(), pos.getY() + 0.4D + 0.4D * rand.nextFloat(),
                    pos.getZ() + 0.25D + 0.5D * rand.nextFloat());
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(AGE, Math.min(meta, MAX_AGE));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE);
    }
}
