package net.mx.edelicacy.machine.pan;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.gui.DelicacyGuiHandler;
import net.mx.edelicacy.item.base.DelicacyItem;

/**
 * 放下的饕餮之锅（没有对应物品：潜行右键神秘遗物的 eldritch_pan 放下，见 {@link VoraciousPanEvents}）。
 * 空手右键打开禁忌转化界面；破坏时掉回整件锅（含剩余禁忌值）与输入槽物品，本身不掉任何东西。
 */
public class BlockVoraciousPan extends Block {

    public static final net.minecraft.block.properties.PropertyDirection FACING = BlockHorizontal.FACING;
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(2 / 16D, 0, 2 / 16D, 14 / 16D, 3 / 16D, 14 / 16D);
    /** 不需要工具、不可燃的金属色材质（原版 IRON 材质要求镐子） */
    private static final Material PAN_MATERIAL = new Material(MapColor.IRON);

    public BlockVoraciousPan() {
        super(PAN_MATERIAL);
        DelicacyItem.setup(this, "voracious_pan");
        setCreativeTab(null);
        setHardness(0.5F);
        setResistance(60.0F);
        setSoundType(SoundType.METAL);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileVoraciousPan();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND || !player.getHeldItem(hand).isEmpty()) {
            return false;
        }
        if (!world.isRemote && world.getTileEntity(pos) instanceof TileVoraciousPan) {
            player.openGui(EnigmaticDelicacy.instance, DelicacyGuiHandler.VORACIOUS_PAN, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    /** 取代 1.21 的战利品函数：掉回存着的锅；方块实体取出后即清空，重复调用不会多掉 */
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileVoraciousPan && !world.isRemote) {
            TileVoraciousPan pan = (TileVoraciousPan) tile;
            ItemStack input = pan.takeInputForDrop();
            // 不走 spawnAsEntity：doTileDrops 关闭时也必须把锅还回来
            if (!input.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), input);
            }
            ItemStack stored = pan.takePanForDrop();
            if (!stored.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stored);
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return ItemStack.EMPTY;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.BLOCK;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
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
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }
}
