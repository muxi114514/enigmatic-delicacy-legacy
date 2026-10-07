package net.mx.edelicacy.flora.soil;

import java.util.Random;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockMushroomColony;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockSandyShrub;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockWildCrop;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.mx.edelicacy.util.WorldHelper;

/**
 * 无尽沃土对上方植物的催熟（对应 1.21 Infinisoil.tick 的各分支）。
 * <p>读取只涉及沃土正上方同一竖列（与沃土同区块）；催熟本身可能长树，生长前另做区块守卫。
 */
public final class SoilBoost {

    /** 1.21 的 "plantHeight >= 3 不再长高" */
    private static final int MAX_TALL_PLANT_HEIGHT = 3;
    private static final int BONEMEAL_PARTICLES = 2005;
    /** 催熟可能长出树/巨型蘑菇，生长前确认这一范围的区块都已加载 */
    private static final int GROW_GUARD_RADIUS = 10;

    private SoilBoost() {
    }

    /**
     * 按农夫乐事 richSoilBoostChance 的语义（随机数/5 小于概率）给上方植物一次骨粉式生长。
     */
    public static void tryBoostingPlants(World world, BlockPos soilPos, Random rand) {
        double chance = com.wdcftgg.farmersdelightlegacy.common.Configuration.richSoilBoostChance;
        if (chance <= 0.0D || rand.nextFloat() / 5.0F >= chance) {
            return;
        }
        BlockPos above = soilPos.up();
        boostPlant(world, above, world.getBlockState(above), rand);
    }

    /** 1.21 RichSoilBlock.boostPlant：可施肥且未被排除的植物直接生长一次（不掷骨粉成功率） */
    public static boolean boostPlant(World world, BlockPos pos, IBlockState state, Random rand) {
        Block block = state.getBlock();
        if (!(block instanceof IGrowable) || isUnaffected(block)) {
            return false;
        }
        IGrowable growable = (IGrowable) block;
        if (!growable.canGrow(world, pos, state, false) || !WorldHelper.isAreaLoaded(world, pos, GROW_GUARD_RADIUS)
                || !ForgeHooks.onCropsGrowPre(world, pos, state, true)) {
            return false;
        }
        growable.grow(world, rand, pos, state);
        world.playEvent(BONEMEAL_PARTICLES, pos, 0);
        ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
        return true;
    }

    /** 对应农夫乐事 1.21 的 unaffected_by_rich_soil 标签（1.12 里存在的部分） */
    private static boolean isUnaffected(Block block) {
        return block instanceof BlockGrass || block instanceof BlockTallGrass || block instanceof BlockDoublePlant
                || block instanceof BlockMushroomColony || block instanceof BlockSandyShrub || block instanceof BlockWildCrop;
    }

    /** 地狱疣：额外 11 或 22 次随机刻（1.21 写成 1 + nextInt(1) 恒为 11 且循环多一次，这里按本意修正） */
    public static void boostNetherWart(World world, BlockPos pos, Random rand) {
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockNetherWart) || state.getValue(BlockNetherWart.AGE) >= 3) {
            return;
        }
        world.playEvent(BONEMEAL_PARTICLES, pos, 0);
        int cycles = (1 + rand.nextInt(2)) * 11;
        for (int i = 0; i < cycles; i++) {
            // 每次重新读取状态，否则疣只能长一级
            IBlockState current = world.getBlockState(pos);
            if (!(current.getBlock() instanceof BlockNetherWart) || current.getValue(BlockNetherWart.AGE) >= 3) {
                return;
            }
            current.getBlock().randomTick(world, pos, current, rand);
        }
    }

    /** 仙人掌/甘蔗：顶端随机增加 0~19 生长值并补一次随机刻，总高度到 3 后停止 */
    public static void boostTallPlant(World world, BlockPos base, Random rand) {
        Block block = world.getBlockState(base).getBlock();
        PropertyInteger age;
        if (block == Blocks.CACTUS) {
            age = BlockCactus.AGE;
        } else if (block == Blocks.REEDS) {
            age = BlockReed.AGE;
        } else {
            return;
        }
        BlockPos top = base;
        while (top.getY() < world.getHeight() - 1 && world.getBlockState(top.up()).getBlock() == block) {
            top = top.up();
        }
        if (!world.isAirBlock(top.up())) {
            return;
        }
        int height = 1;
        while (world.getBlockState(top.down(height)).getBlock() == block) {
            height++;
        }
        if (height >= MAX_TALL_PLANT_HEIGHT) {
            return;
        }
        IBlockState topState = world.getBlockState(top);
        world.playEvent(BONEMEAL_PARTICLES, base, 0);
        int newAge = Math.min(topState.getValue(age) + rand.nextInt(20), 15);
        world.setBlockState(top, topState.withProperty(age, newAge), 4);
        IBlockState grown = world.getBlockState(top);
        if (grown.getBlock().getTickRandomly()) {
            grown.getBlock().randomTick(world, top, grown, rand);
        }
    }
}
