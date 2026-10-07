package net.mx.edelicacy.flora.tree;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.flora.client.AstralLeafFx;

/**
 * 星辰树叶 / 开花的星辰树叶（开花的亮度 8）。树上自然生成的叶子会在两种之间随机切换，
 * 开花叶子凋谢时在下方结出星辰果；玩家放置的（不可凋落）不变化。凋落沿用原版树叶逻辑。
 */
public class AstralLeavesBlock extends BlockLeaves {

    private static final MapColor[] COLORS = {MapColor.LIGHT_BLUE, MapColor.PINK, MapColor.PURPLE, MapColor.ADOBE};

    private final boolean blossoming;

    public AstralLeavesBlock(boolean blossoming) {
        this.blossoming = blossoming;
        if (blossoming) {
            setLightLevel(8.0F / 15.0F);
        }
        setDefaultState(blockState.getBaseState().withProperty(CHECK_DECAY, false).withProperty(DECAYABLE, true));
    }

    public boolean isBlossoming() {
        return blossoming;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CHECK_DECAY, DECAYABLE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(DECAYABLE, (meta & 4) == 0).withProperty(CHECK_DECAY, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(DECAYABLE) ? 0 : 4) | (state.getValue(CHECK_DECAY) ? 8 : 0);
    }

    /** 玩家放置的树叶永不凋落（1.21 persistent） */
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(DECAYABLE, false).withProperty(CHECK_DECAY, false);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);
        if (world.isRemote) {
            return;
        }
        IBlockState current = world.getBlockState(pos);
        if (current.getBlock() == this && current.getValue(DECAYABLE)) {
            AstralLeafGrowth.tick(world, pos, current, rand, blossoming);
        }
    }

    /** 两种树叶互换时不通知周围树叶检查凋落（换的仍是树叶），其余情况同原版 */
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!(world.getBlockState(pos).getBlock() instanceof AstralLeavesBlock)) {
            super.breakBlock(world, pos, state);
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World) world).rand : RANDOM;
        Item sapling = Item.getItemFromBlock(net.mx.edelicacy.registry.DelicacyBlocks.ASTRAL_SAPLING);
        AstralLeafDrops.roll(drops, rand, fortune, blossoming, sapling);
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(this);
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    public BlockPlanks.EnumType getWoodType(int meta) {
        return BlockPlanks.EnumType.OAK;
    }

    /** 跟随原版树叶的画质设置（原版只给自己的树叶调用 setGraphicsLevel） */
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return Blocks.LEAVES.isOpaqueCube(state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return Blocks.LEAVES.getBlockLayer();
    }

    /**
     * 快速画质：沿用父类（leavesFancy 恒为 false，相邻同种树叶之间的面不画）；
     * 精致画质：按完整方块的默认规则。不在区块渲染线程里写字段。
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (Blocks.LEAVES.isOpaqueCube(state)) {
            return super.shouldSideBeRendered(state, world, pos, side);
        }
        BlockPos neighbor = pos.offset(side);
        return !world.getBlockState(neighbor).doesSideBlockRendering(world, neighbor, side.getOpposite());
    }

    /** 自然树叶向下飘落星辰叶片（下方为实心方块时不生成，1.21 的判断写反了恒为真） */
    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        super.randomDisplayTick(state, world, pos, rand);
        if (rand.nextInt(16) != 0 || !state.getValue(DECAYABLE)) {
            return;
        }
        BlockPos below = pos.down();
        if (!world.getBlockState(below).isSideSolid(world, below, EnumFacing.UP)) {
            AstralLeafFx.spawnFalling(world, rand, pos);
        }
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        int index = MathHelper.floor(pos.getX() * 0.8F + pos.getY() * 0.7F + pos.getZ() * 1.3F) & 3;
        return COLORS[index];
    }

    /** 是否为本模组的星辰树叶（供果实判定支撑） */
    static boolean isAstralLeaves(Block block) {
        return block instanceof AstralLeavesBlock;
    }
}
