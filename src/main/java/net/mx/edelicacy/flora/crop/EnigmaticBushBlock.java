package net.mx.edelicacy.flora.crop;

import java.util.Random;

import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.mx.edelicacy.registry.DelicacyBlocks;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 神秘灌木：只能种在无尽沃土耕地上，不吃骨粉。成熟后右键摘 1~2 个神秘果并退回 4 阶段；
 * 成熟时打掉有 2.5% 额外掉一个彩色神秘果。
 */
public class EnigmaticBushBlock extends BlockCrops {

    private static final int HARVEST_RESET_AGE = 4;
    private static final float COLORED_FRUIT_CHANCE = 0.025F;
    /** 时运二项分布（1.21 ApplyBonusCount: extra 1, p 0.3） */
    private static final float FORTUNE_PROBABILITY = 0.3F;

    private static final AxisAlignedBB[] SHAPES = {
            box(5, 7, 11), box(4, 9, 12), box(3, 13, 13), box(2, 15, 14),
            box(0, 16, 16), box(0, 16, 16), box(0, 16, 16), box(0, 16, 16)
    };

    private static AxisAlignedBB box(int min, int height, int max) {
        return new AxisAlignedBB(min / 16.0D, 0.0D, min / 16.0D, max / 16.0D, height / 16.0D, max / 16.0D);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPES[getAge(state)];
    }

    @Override
    protected Item getSeed() {
        return DelicacyItems.ENIGMATIC_SEED;
    }

    @Override
    protected Item getCrop() {
        return DelicacyItems.ENIGMATIC_FRUIT;
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return state.getBlock() == DelicacyBlocks.INFINISOIL_FARMLAND;
    }

    /** 父类只问土壤能否承载作物，农夫乐事耕地也会答应，这里额外限定无尽沃土耕地 */
    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return canSustainBush(world.getBlockState(pos.down())) && super.canBlockStay(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!isMaxAge(state)) {
            return false;
        }
        if (!world.isRemote) {
            spawnAsEntity(world, pos, new ItemStack(getCrop(), 1 + world.rand.nextInt(2)));
            world.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F,
                    0.8F + world.rand.nextFloat() * 0.4F);
            world.setBlockState(pos, withAge(HARVEST_RESET_AGE), 2);
        }
        return true;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (!world.isRemote && willHarvest && isMaxAge(state) && player.getRNG().nextFloat() < COLORED_FRUIT_CHANCE
                && world.getBlockState(pos.down()).getBlock() == DelicacyBlocks.INFINISOIL_FARMLAND) {
            Item colored = EnigmaticFruitItem.randomColored(player.getRNG());
            if (colored != null) {
                spawnAsEntity(world, pos, new ItemStack(colored));
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    /** 未成熟掉种子；成熟掉 1 + 二项(时运+1, 0.3) 个神秘果（1.21 战利品表，不掉种子） */
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World) world).rand : RANDOM;
        if (!isMaxAge(state)) {
            drops.add(new ItemStack(getSeed()));
            return;
        }
        int count = 1;
        for (int i = 0; i < fortune + 1; i++) {
            if (rand.nextFloat() < FORTUNE_PROBABILITY) {
                count++;
            }
        }
        drops.add(new ItemStack(getCrop(), count));
    }

    /** 原版作物把时运强制置 0，这里按 Block 的默认实现把时运传下去 */
    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (world.isRemote || world.restoringBlockSnapshots) {
            return;
        }
        NonNullList<ItemStack> drops = NonNullList.create();
        getDrops(drops, world, pos, state, fortune);
        float finalChance = ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, fortune, chance, false, harvesters.get());
        for (ItemStack drop : drops) {
            if (world.rand.nextFloat() <= finalChance) {
                spawnAsEntity(world, pos, drop);
            }
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return false;
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return false;
    }
}
