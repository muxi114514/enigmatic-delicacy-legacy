package net.mx.edelicacy.block.base;

import java.util.Random;
import java.util.function.Supplier;

import com.wdcftgg.farmersdelightlegacy.common.item.ItemKnife;

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
import net.minecraft.entity.item.EntityItem;
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
import net.minecraftforge.event.ForgeEventFactory;

/**
 * 派方块基类（对应 FD 的 PieBlock）：4 块，持刀右键切下一块掉落，空手右键直接吃一块（等同吃下切片物品，
 * 切片的效果、容器照常结算）。完整时被破坏掉落整个派，切过后不掉落。
 * <p>FD Legacy 的 BlockPie 把切片物品写死在 farmersdelight 命名空间，不能复用。
 * 子类可覆写 {@link #canCut}、{@link #canEat}（如混沌派只许禁忌之刃切）。
 */
public class DelicacyPieBlock extends Block {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyInteger BITES = PropertyInteger.create("bites", 0, 3);
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.25D, 0.9375D);

    private final Supplier<Item> slice;

    public DelicacyPieBlock(Supplier<Item> slice) {
        super(Material.CAKE);
        this.slice = slice;
        setHardness(0.5F);
        setSoundType(SoundType.CLOTH);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(BITES, 0));
    }

    public Item getSliceItem() {
        return slice.get();
    }

    /** 能否用手中物品切这个派（默认：任意刀） */
    protected boolean canCut(World world, BlockPos pos, EntityPlayer player, ItemStack held) {
        return ItemKnife.isKnife(held);
    }

    /** 能否直接吃（默认：饿了就能吃） */
    protected boolean canEat(World world, BlockPos pos, EntityPlayer player) {
        return player.canEat(false);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if (canCut(world, pos, player, held)) {
            if (!world.isRemote) {
                cutSlice(world, pos, state, player, held);
            }
            return true;
        }
        if (hand != EnumHand.MAIN_HAND || !canEat(world, pos, player)) {
            return false;
        }
        if (!world.isRemote) {
            Item item = getSliceItem();
            if (item != null) {
                // 走切片物品自己的食用流程：饥饿值、效果、返还容器都由它处理；
                // 再补发原版「使用完成」事件（手里吃时由 EntityLivingBase 发），SimpleDifficulty 的口渴/体温靠它结算
                ItemStack slice = new ItemStack(item);
                ItemStack eaten = slice.copy();
                ItemStack result = ForgeEventFactory.onItemUseFinish(player, eaten, 0, item.onItemUseFinish(slice, world, player));
                if (!result.isEmpty() && result.getItem() != item && !player.inventory.addItemStackToInventory(result)) {
                    player.dropItem(result, false);
                }
            }
            removeSlice(world, pos, state);
            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 0.8F, 0.8F);
        }
        return true;
    }

    protected void cutSlice(World world, BlockPos pos, IBlockState state, EntityPlayer player, ItemStack knife) {
        Item item = getSliceItem();
        if (item == null) {
            return;
        }
        removeSlice(world, pos, state);
        EnumFacing dropFacing = player.getHorizontalFacing().getOpposite();
        EntityItem drop = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D, new ItemStack(item));
        drop.motionX = dropFacing.getFrontOffsetX() * 0.15D;
        drop.motionY = 0.05D;
        drop.motionZ = dropFacing.getFrontOffsetZ() * 0.15D;
        drop.setDefaultPickupDelay();
        world.spawnEntity(drop);
        world.playSound(null, pos, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 0.8F, 0.8F);
        if (!knife.isEmpty() && knife.isItemStackDamageable()) {
            knife.damageItem(1, player);
        }
    }

    protected void removeSlice(World world, BlockPos pos, IBlockState state) {
        int bites = state.getValue(BITES);
        if (bites >= 3) {
            world.setBlockToAir(pos);
        } else {
            world.setBlockState(pos, state.withProperty(BITES, bites + 1), 3);
        }
    }

    /** 完整时掉落整个派，切过后不掉落 */
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(BITES) == 0 ? Item.getItemFromBlock(this) : Items.AIR;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return state.getValue(BITES) == 0 ? 1 : 0;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer, EnumHand hand) {
        // 与农夫乐事的派一致取玩家朝向（沿用的 1.21 方块状态按此绘制缺口方向）
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && world.getBlockState(pos.down()).getMaterial().isSolid();
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!world.getBlockState(pos.down()).getMaterial().isSolid()) {
            world.setBlockToAir(pos);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
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
        return 4 - state.getValue(BITES);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING, BITES});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(BITES) << 2);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3)).withProperty(BITES, (meta >> 2) & 3);
    }
}
