package net.mx.edelicacy.flora.tree;

import java.util.Random;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.registry.DelicacyBlocks;
import net.mx.edelicacy.util.WorldHelper;

/**
 * 星辰树苗：只能种在无尽沃土或农夫乐事沃土上，亮度 12，不吃骨粉（1.21 未实现可施肥接口）。
 * 两阶段生长；沃土上生长更慢且长成小树，2×2 树苗长成大型星辰树。
 */
public class AstralSaplingBlock extends BlockBush {

    public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 1);
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.75D, 0.875D);

    public AstralSaplingBlock() {
        super(Material.PLANTS);
        setSoundType(SoundType.PLANT);
        setHardness(0.0F);
        setLightLevel(12.0F / 15.0F);
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState().withProperty(STAGE, 0));
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) {
            return;
        }
        super.updateTick(world, pos, state, rand);
        if (world.getBlockState(pos).getBlock() != this) {
            return;
        }
        if (isOnRichSoil(world, pos) && rand.nextInt(3) != 0) {
            return;
        }
        if (WorldHelper.isAreaLoaded(world, pos, 1) && world.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(7) == 0) {
            advanceTree(world, pos, state, rand);
        }
    }

    public void advanceTree(World world, BlockPos pos, IBlockState state, Random rand) {
        if (state.getValue(STAGE) == 0) {
            world.setBlockState(pos, state.withProperty(STAGE, 1), 4);
        } else if (isOnRichSoil(world, pos)) {
            AstralTreeGrower.growSmall(world, pos, state, rand);
        } else {
            AstralTreeGrower.grow(world, pos, state, rand);
        }
    }

    private static boolean isOnRichSoil(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).getBlock() == ModBlocks.RICH_SOIL;
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        Block block = state.getBlock();
        return block == DelicacyBlocks.INFINISOIL || block == ModBlocks.RICH_SOIL;
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return canSustainBush(world.getBlockState(pos.down()));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos) && canSustainBush(world.getBlockState(pos.down()));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextInt(4) == 0) {
            StarDust.spawn(world, rand, pos.getX() + 0.05D + 0.9D * rand.nextFloat(), pos.getY() + 0.15D + 0.8D * rand.nextFloat(),
                    pos.getZ() + 0.05D + 0.9D * rand.nextFloat());
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(STAGE, meta & 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(STAGE);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, STAGE);
    }
}
