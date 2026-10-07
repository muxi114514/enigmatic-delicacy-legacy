package net.mx.edelicacy.block.base;

import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * 宴席方块基类（对应 FD 的 FeastBlock，不留残余）：最多 4 份，右键取一份（有容器要求时需手持容器并消耗），
 * 取完最后一份方块消失。满份时被破坏掉落方块本身。
 * <p>状态 = 朝向（2 位）+ 剩余份数 1..4（存 0..3，2 位），正好 16 个 meta。
 * FD Legacy 的 BlockFeast 把份装物品写死在 farmersdelight 命名空间，不能复用。
 */
public class DelicacyFeastBlock extends Block {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    /** 剩余份数减一（0..3） */
    public static final PropertyInteger SERVINGS = PropertyInteger.create("servings", 0, 3);

    private final int maxServings;
    private final Supplier<Item> serving;
    @Nullable
    private final Supplier<Item> requiredContainer;
    private AxisAlignedBB shape = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D);

    /**
     * @param maxServings       1..4
     * @param requiredContainer 取一份需要消耗的容器（如碗），null 表示空手即可
     */
    public DelicacyFeastBlock(Material material, int maxServings, Supplier<Item> serving, @Nullable Supplier<Item> requiredContainer) {
        super(material);
        this.maxServings = Math.max(1, Math.min(4, maxServings));
        this.serving = serving;
        this.requiredContainer = requiredContainer;
        setHardness(0.8F);
        setSoundType(SoundType.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(SERVINGS, this.maxServings - 1));
    }

    public DelicacyFeastBlock shape(AxisAlignedBB shape) {
        this.shape = shape;
        return this;
    }

    public int getMaxServings() {
        return maxServings;
    }

    public int getServings(IBlockState state) {
        return state.getValue(SERVINGS) + 1;
    }

    /** 能否取用（子类可加限制，如不配者触碰即死） */
    protected boolean canTakeServing(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND || !canTakeServing(world, pos, state, player)) {
            return false;
        }
        ItemStack held = player.getHeldItem(hand);
        Item container = requiredContainer == null ? null : requiredContainer.get();
        if (container != null && held.getItem() != container) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        Item item = serving.get();
        if (item == null) {
            return false;
        }
        if (container != null && !player.capabilities.isCreativeMode) {
            held.shrink(1);
        }
        ItemStack result = new ItemStack(item);
        if (!player.inventory.addItemStackToInventory(result)) {
            player.dropItem(result, false);
        }
        int servings = getServings(state);
        if (servings <= 1) {
            world.setBlockToAir(pos);
        } else {
            world.setBlockState(pos, state.withProperty(SERVINGS, servings - 2), 3);
        }
        world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    /** 满份时掉落方块本身，取用过后不掉落 */
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return getServings(state) == maxServings ? Item.getItemFromBlock(this) : Items.AIR;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return getServings(state) == maxServings ? 1 : 0;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && world.getBlockState(pos.down()).getMaterial().isSolid();
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!world.getBlockState(pos.down()).getMaterial().isSolid()) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return shape;
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
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return getServings(state);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING, SERVINGS});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(SERVINGS) << 2);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3))
                .withProperty(SERVINGS, Math.min(maxServings - 1, (meta >> 2) & 3));
    }
}
