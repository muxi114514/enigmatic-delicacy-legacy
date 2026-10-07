package net.mx.edelicacy.tool.machete;

import net.minecraft.block.BlockCake;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.eaddons.elplus.BlockEternalCake;
import net.mx.edelicacy.registry.DelicacyItems;

/** 以太砍刀右键神遗拓展的永恒蛋糕（EL+ 的宇宙蛋糕）切下一片宇宙蛋糕切片 */
public class MacheteCakeHandler {

    /** 永恒蛋糕最多切到第 6 口（与 BlockEternalCake.cutSlice 一致） */
    private static final int MAX_CUT_BITES = 5;

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack tool = event.getItemStack();
        if (tool.isEmpty() || !(tool.getItem() instanceof ItemEtheriumMachete)) {
            return;
        }
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != BlockEternalCake.INSTANCE) {
            return;
        }
        int bites = state.getValue(BlockCake.BITES);
        if (bites > MAX_CUT_BITES) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (world.isRemote || !BlockEternalCake.cutSlice(world, pos)) {
            return;
        }
        Item slice = DelicacyItems.COSMIC_CAKE_SLICE;
        if (slice != null) {
            EntityItem drop = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D + bites * 0.1D, new ItemStack(slice));
            drop.motionX = 0.0D;
            drop.motionY = 0.0D;
            drop.motionZ = -0.05D;
            world.spawnEntity(drop);
        }
        world.playSound(null, pos, SoundEvents.BLOCK_CLOTH_BREAK, SoundCategory.PLAYERS, 0.8F, 0.8F);
    }
}
