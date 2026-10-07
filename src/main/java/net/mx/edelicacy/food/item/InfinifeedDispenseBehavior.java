package net.mx.edelicacy.food.item;

import java.util.List;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** 发射器使用野性滋养精华：对正前方一格里的第一只生物生效，失败时发出失败音效，物品不射出也不消耗 */
public class InfinifeedDispenseBehavior extends Bootstrap.BehaviorDispenseOptional {

    @Override
    protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
        World world = source.getWorld();
        BlockPos pos = source.getBlockPos().offset(source.getBlockState().getValue(BlockDispenser.FACING));
        List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos));
        successful = !list.isEmpty() && ItemInfinifeed.tryApply(world, list.get(0), null);
        return stack;
    }
}
