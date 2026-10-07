package net.mx.edelicacy.curse.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.block.base.DelicacyPieBlock;
import net.mx.edelicacy.curse.damage.CurseDamageSources;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 混沌肉派：亮度 8，只有禁忌之刃能切；只有禁忌之人能直接吃（他们饥饿值被锁满，所以不看饥饿；
 * 1.21 走农夫乐事的派逻辑，反而只有非禁忌之人能吃，且不受禁忌限制）。
 * 非禁忌的生物不潜行踩上去会受到禁忌诅咒伤害，被它踩死时派会长回一块。
 */
public class BlockChaoticPie extends DelicacyPieBlock {

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(1.5D / 16, 0.0D, 1.5D / 16, 14.5D / 16, 6.0D / 16, 14.5D / 16);

    public BlockChaoticPie() {
        super(() -> DelicacyItems.CHAOTIC_PIE_SLICE);
        setLightLevel(8.0F / 15.0F);
    }

    @Override
    protected boolean canCut(World world, BlockPos pos, EntityPlayer player, ItemStack held) {
        return !held.isEmpty() && held.getItem() == DelicacyItems.FORBIDDEN_KNIFE;
    }

    @Override
    protected boolean canEat(World world, BlockPos pos, EntityPlayer player) {
        return EnigmaticBridge.isForbidden(player);
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (world.isRemote || !(entity instanceof EntityLivingBase) || entity.isSneaking()) {
            return;
        }
        EntityLivingBase living = (EntityLivingBase) entity;
        if (living instanceof EntityPlayer && EnigmaticBridge.isForbidden((EntityPlayer) living)) {
            return;
        }
        if (living.attackEntityFrom(CurseDamageSources.FORBIDDEN_CURSE, 2.0F) && living.getHealth() <= 0.0F) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() == this && state.getValue(BITES) > 0) {
                world.setBlockState(pos, state.withProperty(BITES, state.getValue(BITES) - 1), 3);
            }
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
