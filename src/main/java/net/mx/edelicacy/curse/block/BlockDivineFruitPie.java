package net.mx.edelicacy.curse.block;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.block.base.DelicacyFeastBlock;
import net.mx.edelicacy.curse.util.CurseUse;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 神圣果派（宴席，4 份，每份要一个碗）：不配之人（非七咒 / 救赎之人）一碰即死。
 * 1.21 取完最后一份会留下空托盘，B0 的宴席基类不留残余。
 */
public class BlockDivineFruitPie extends DelicacyFeastBlock {

    public BlockDivineFruitPie() {
        super(Material.CAKE, 4, () -> DelicacyItems.DIVINE_FRUIT_PIE, () -> Items.BOWL);
        setHardness(0.5F);
    }

    @Override
    protected boolean canTakeServing(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (CurseUse.canUse(player, new ItemStack(this))) {
            return true;
        }
        if (!world.isRemote) {
            player.onKillCommand();
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(3) == 0) {
            world.spawnParticle(EnumParticleTypes.END_ROD, pos.getX() + 0.05D + 0.9D * random.nextDouble(),
                    pos.getY() + 0.6D + 0.1D * random.nextDouble(), pos.getZ() + 0.05D + 0.9D * random.nextDouble(), 0.0D, 0.0D, 0.0D);
        }
    }
}
