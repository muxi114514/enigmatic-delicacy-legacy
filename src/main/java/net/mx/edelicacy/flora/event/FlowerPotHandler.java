package net.mx.edelicacy.flora.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.registry.DelicacyBlocks;

/**
 * 拿星辰树苗右键空的原版花盆 → 换成星辰树苗盆栽（原版花盆只收原版植物）。
 */
public class FlowerPotHandler {

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() != Item.getItemFromBlock(DelicacyBlocks.ASTRAL_SAPLING)) {
            return;
        }
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        if (world.getBlockState(pos).getBlock() != Blocks.FLOWER_POT) {
            return;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityFlowerPot) || !((TileEntityFlowerPot) tile).getFlowerItemStack().isEmpty()) {
            return;
        }
        EntityPlayer player = event.getEntityPlayer();
        if (!player.canPlayerEdit(pos, event.getFace(), stack)) {
            return;
        }
        if (!world.isRemote) {
            world.setBlockState(pos, DelicacyBlocks.POTTED_ASTRAL_SAPLING.getDefaultState(), 3);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }
}
