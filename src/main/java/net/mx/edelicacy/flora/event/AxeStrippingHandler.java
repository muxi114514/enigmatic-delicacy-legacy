package net.mx.edelicacy.flora.event;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.registry.DelicacyBlocks;

/**
 * 斧头右键给星辰原木/木头去皮（1.21 BlockToolModificationEvent 的 AXE_STRIP，1.12 无此机制）。
 * 两端都取消事件以挥手，实际改方块只在服务端。
 */
public class AxeStrippingHandler {

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !stack.getItem().getToolClasses(stack).contains("axe")) {
            return;
        }
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        Block stripped = strippedOf(state.getBlock());
        EntityPlayer player = event.getEntityPlayer();
        if (stripped == null || !player.canPlayerEdit(pos, event.getFace(), stack)) {
            return;
        }
        if (!world.isRemote) {
            world.setBlockState(pos, stripped.getDefaultState().withProperty(BlockLog.LOG_AXIS, state.getValue(BlockLog.LOG_AXIS)), 11);
            world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            stack.damageItem(1, player);
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    private static Block strippedOf(Block block) {
        if (block == DelicacyBlocks.ASTRAL_LOG) {
            return DelicacyBlocks.STRIPPED_ASTRAL_LOG;
        }
        if (block == DelicacyBlocks.ASTRAL_WOOD) {
            return DelicacyBlocks.STRIPPED_ASTRAL_WOOD;
        }
        return null;
    }
}
