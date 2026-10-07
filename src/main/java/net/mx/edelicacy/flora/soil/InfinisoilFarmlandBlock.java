package net.mx.edelicacy.flora.soil;

import java.util.Random;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockRichSoilFarmland;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.FarmlandWaterManager;
import net.mx.edelicacy.registry.DelicacyBlocks;
import net.mx.edelicacy.util.WorldHelper;

/**
 * 无尽沃土耕地：亮度 15，湿度满时每 6~12 tick 催熟上方作物；随机刻同沃土影响周围。
 * 父类的湿度写入、掉落、选取方块都写死了农夫乐事耕地，这里全部覆写。
 */
public class InfinisoilFarmlandBlock extends BlockRichSoilFarmland {

    private static final int WATER_RANGE = 4;

    public InfinisoilFarmlandBlock() {
        setHardness(1.0F);
        setResistance(50.0F / 3.0F);
        setLightLevel(1.0F);
        setHarvestLevel("shovel", 0);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) {
            return;
        }
        if (isCovered(world, pos)) {
            // 1.21 变回沃土后仍继续往下执行并把耕地写回去，这里直接结束
            turnToInfinisoil(world, pos);
            return;
        }
        world.scheduleUpdate(pos, this, Infinisoils.nextTickDelay(rand));
        int moisture = state.getValue(MOISTURE);
        if (!isNearWater(world, pos) && !world.isRainingAt(pos.up())) {
            if (moisture > 0) {
                world.setBlockState(pos, state.withProperty(MOISTURE, moisture - 1), 2);
            }
        } else if (moisture < 7) {
            world.setBlockState(pos, state.withProperty(MOISTURE, 7), 2);
        } else {
            SoilBoost.tryBoostingPlants(world, pos, rand);
        }
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) {
            return;
        }
        SoilNeighborhood.randomTick(world, pos, rand);
        IBlockState current = world.getBlockState(pos);
        if (current.getBlock() == this) {
            updateTick(world, pos, current, rand);
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) {
            return;
        }
        if (isCovered(world, pos)) {
            turnToInfinisoil(world, pos);
        } else {
            world.scheduleUpdate(pos, this, Infinisoils.nextTickDelay(world.rand));
        }
    }

    /** 上方被实心方块压住时变回沃土（父类为空实现） */
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!world.isRemote && isCovered(world, pos)) {
            turnToInfinisoil(world, pos);
        }
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!world.isRemote && entity instanceof EntityPlayer && world.rand.nextInt(3) == 0) {
            world.scheduleUpdate(pos, this, 5 + world.rand.nextInt(11));
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer) {
        return isCovered(world, pos) ? DelicacyBlocks.INFINISOIL.getDefaultState() : getDefaultState();
    }

    @Override
    public boolean isFertile(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() == this && state.getValue(MOISTURE) > 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(DelicacyBlocks.INFINISOIL);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(this);
    }

    private static boolean isCovered(World world, BlockPos pos) {
        return world.getBlockState(pos.up()).getMaterial().isSolid();
    }

    /** 变回沃土并把站在上面的实体抬到整格高度；相邻区块未加载时改为稍后由计划刻重试 */
    private void turnToInfinisoil(World world, BlockPos pos) {
        if (!WorldHelper.isAreaLoaded(world, pos, 1)) {
            world.scheduleUpdate(pos, this, Infinisoils.nextTickDelay(world.rand));
            return;
        }
        world.setBlockState(pos, DelicacyBlocks.INFINISOIL.getDefaultState());
        AxisAlignedBB box = FULL_BLOCK_AABB.offset(pos);
        for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, box)) {
            if (entity.posY < box.maxY) {
                entity.setPositionAndUpdate(entity.posX, box.maxY, entity.posZ);
            }
        }
    }

    /** 水平 4 格、上方 1 格内有水（只看已加载区块），或有 Forge 的耕地浇水票 */
    private static boolean isNearWater(World world, BlockPos pos) {
        LocalChunkCache chunks = new LocalChunkCache(world);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -WATER_RANGE; dx <= WATER_RANGE; dx++) {
            for (int dz = -WATER_RANGE; dz <= WATER_RANGE; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    cursor.setPos(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    IBlockState state = chunks.getBlockState(cursor);
                    if (state != null && state.getMaterial() == Material.WATER) {
                        return true;
                    }
                }
            }
        }
        return FarmlandWaterManager.hasBlockWaterTicket(world, pos);
    }
}
