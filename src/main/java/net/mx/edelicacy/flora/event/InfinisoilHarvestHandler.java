package net.mx.edelicacy.flora.event;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.flora.soil.Infinisoils;

/**
 * 玩家收获种在无尽沃土（耕地）上的成熟作物时，25% 概率再掷一次掉落（1.21 Infinisoil.Event.onCropBreak）。
 */
public class InfinisoilHarvestHandler {

    @SubscribeEvent
    public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        EntityPlayer player = event.getHarvester();
        World world = event.getWorld();
        if (player == null || world.isRemote || player.capabilities.isCreativeMode || event.isSilkTouching()) {
            return;
        }
        IBlockState state = event.getState();
        Block block = state.getBlock();
        if (!(block instanceof BlockCrops) || !((BlockCrops) block).isMaxAge(state) || player.getRNG().nextInt(4) != 0) {
            return;
        }
        if (!Infinisoils.isInfinisoil(world.getBlockState(event.getPos().down()))) {
            return;
        }
        NonNullList<ItemStack> extra = NonNullList.create();
        block.getDrops(extra, world, event.getPos(), state, event.getFortuneLevel());
        event.getDrops().addAll(extra);
    }
}
