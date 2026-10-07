package net.mx.edelicacy.flora.soil;

import java.util.Random;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockRichSoil;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.mx.edelicacy.registry.DelicacyBlocks;
import net.mx.edelicacy.util.WorldHelper;

/**
 * 无尽沃土：亮度 15，每 6~12 tick 自我计划刻催熟上方植物、把蘑菇转成菌落；
 * 随机刻加速周围堆肥并同化农夫乐事沃土。继承农夫乐事沃土，但锄地、掉落等写死的方块引用全部改成本模组的。
 */
public class InfinisoilBlock extends BlockRichSoil {

    private static final int NEIGHBOR_GUARD = 2;

    public InfinisoilBlock() {
        setHardness(1.0F);
        setResistance(50.0F / 3.0F);
        setLightLevel(1.0F);
        setSoundType(SoundType.GROUND);
        setHarvestLevel("shovel", 0);
    }

    /** 锄地变成无尽沃土耕地（父类写死成农夫乐事耕地，必须整体覆写） */
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty() || !(held.getItem() instanceof ItemHoe || held.getItem().getToolClasses(held).contains("hoe"))) {
            return false;
        }
        if (facing == EnumFacing.DOWN || !world.isAirBlock(pos.up())) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        IBlockState farmland = DelicacyBlocks.INFINISOIL_FARMLAND.getDefaultState().withProperty(BlockFarmland.MOISTURE, 0);
        world.setBlockState(pos, farmland, 11);
        SoundType sound = farmland.getBlock().getSoundType(farmland, world, pos, player);
        world.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) * 0.5F, sound.getPitch() * 0.8F);
        held.damageItem(1, player);
        return true;
    }

    /** 计划刻（1.21 tick） */
    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) {
            return;
        }
        world.scheduleUpdate(pos, this, Infinisoils.nextTickDelay(rand));
        // 转换/催熟会通知相邻方块，边缘区块未加载时本次跳过（循环已续上，下次再试）
        if (!WorldHelper.isAreaLoaded(world, pos, NEIGHBOR_GUARD)) {
            return;
        }
        BlockPos above = pos.up();
        if (ColonyConversion.convert(world, above, world.getBlockState(above))) {
            return;
        }
        SoilBoost.tryBoostingPlants(world, pos, rand);
        SoilBoost.boostNetherWart(world, above, rand);
        SoilBoost.boostTallPlant(world, above, rand);
    }

    /** 随机刻：先影响周围，再执行一次计划刻逻辑（同时把计划刻循环续上） */
    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) {
            return;
        }
        SoilNeighborhood.randomTick(world, pos, rand);
        updateTick(world, pos, state, rand);
    }

    /** 放下即启动计划刻循环（1.21 要等第一次随机刻或玩家踩踏才启动） */
    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            world.scheduleUpdate(pos, this, Infinisoils.nextTickDelay(world.rand));
        }
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!world.isRemote && entity instanceof EntityPlayer && world.rand.nextInt(3) == 0) {
            world.scheduleUpdate(pos, this, 5 + world.rand.nextInt(11));
        }
    }

    /**
     * 与泥土相当（1.21 属于 dirt 标签）：庄稼/水生植物除外都能种；甘蔗同泥土要求旁边有水；
     * 另外放行地狱植物，1.21 的地狱疣催熟分支才有意义。
     */
    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
        if (direction != EnumFacing.UP) {
            return false;
        }
        EnumPlantType type = plantable.getPlantType(world, pos.up());
        if (type == EnumPlantType.Nether) {
            return true;
        }
        if (type == EnumPlantType.Beach) {
            for (EnumFacing side : EnumFacing.HORIZONTALS) {
                if (world.getBlockState(pos.offset(side)).getMaterial() == Material.WATER) {
                    return true;
                }
            }
            return false;
        }
        return super.canSustainPlant(state, world, pos, direction, plantable);
    }
}
