package net.mx.edelicacy.flora.event;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.flora.soil.ColonyConversion;
import net.mx.edelicacy.registry.DelicacyBlocks;

/**
 * 在无尽沃土顶面种蘑菇/可转换的菌类时直接种成菌落。
 * <p>1.12 的蘑菇要求亮度 < 13，而沃土自身亮度 15，蘑菇根本种不上去（1.21 用标签放行），
 * 下界化的菌类也只认自家土壤；这里直接放置 1.21 里 6~12 tick 后才会转成的菌落。
 */
public class ColonyPlacementHandler {

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || event.getFace() != EnumFacing.UP) {
            return;
        }
        World world = event.getWorld();
        BlockPos soil = event.getPos();
        if (world.getBlockState(soil).getBlock() != DelicacyBlocks.INFINISOIL) {
            return;
        }
        BlockPos above = soil.up();
        IBlockState colony = ColonyConversion.colonyFor(plantOf(stack, world, above));
        EntityPlayer player = event.getEntityPlayer();
        if (colony == null || !world.getBlockState(above).getBlock().isReplaceable(world, above)
                || !player.canPlayerEdit(above, EnumFacing.UP, stack) || !colony.getBlock().canPlaceBlockAt(world, above)) {
            return;
        }
        if (!world.isRemote) {
            world.setBlockState(above, colony, 3);
            SoundType sound = colony.getBlock().getSoundType(colony, world, above, player);
            world.playSound(null, above, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    /** 物品对应的植物方块：方块物品或可种植物品 */
    private static Block plantOf(ItemStack stack, World world, BlockPos pos) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (block == net.minecraft.init.Blocks.AIR && stack.getItem() instanceof IPlantable) {
            block = ((IPlantable) stack.getItem()).getPlant(world, pos).getBlock();
        }
        return block;
    }
}
