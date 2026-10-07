package net.mx.edelicacy.machine.pan;

import java.util.function.Supplier;

import keletu.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.machine.MachineConfig;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 把 1.21 饕餮之锅的额外功能并入神秘遗物的 eldritch_pan（不改 EL 本体）：
 * 潜行右键方块面放下锅；主手持锅直接击杀生物积攒禁忌值。
 * <p>非天选之人持锅的交互已被 EL 在 HIGH 优先级取消，这里再判一次以防配置改动。
 */
public class VoraciousPanEvents {

    /** 方块注册事件晚于 preInit，惰性取得 */
    private final Supplier<Block> panBlock;

    public VoraciousPanEvents(Supplier<Block> panBlock) {
        this.panBlock = panBlock;
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() != EnigmaticLegacy.eldritchPan || !player.isSneaking()) {
            return;
        }
        if (!player.capabilities.isCreativeMode && !EnigmaticBridge.isWorthy(player)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (!event.getWorld().isRemote) {
            place(event.getWorld(), event.getPos(), event.getFace(), player, event.getHand(), stack);
        }
    }

    private void place(World world, BlockPos clicked, EnumFacing face, EntityPlayer player, EnumHand hand, ItemStack stack) {
        Block block = panBlock.get();
        if (face == null || block == null) {
            return;
        }
        BlockPos target = world.getBlockState(clicked).getBlock().isReplaceable(world, clicked) ? clicked : clicked.offset(face);
        if (!player.canPlayerEdit(target, face, stack) || !world.mayPlace(block, target, false, face, player)) {
            return;
        }
        IBlockState state = block.getDefaultState().withProperty(BlockVoraciousPan.FACING, player.getHorizontalFacing());
        BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(world, target);
        if (!world.setBlockState(target, state, 11)) {
            return;
        }
        if (ForgeEventFactory.onPlayerBlockPlace(player, snapshot, face, hand).isCanceled()) {
            snapshot.restore(true, false);
            return;
        }
        TileEntity tile = world.getTileEntity(target);
        if (!(tile instanceof TileVoraciousPan)) {
            world.setBlockToAir(target);
            return;
        }
        ((TileVoraciousPan) tile).setPan(stack);
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        SoundType sound = SoundType.METAL;
        world.playSound(null, target, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
    }

    /** 主手持锅、本人直接击杀生物：+⌊最大生命 / 10⌋ 禁忌值 */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof EntityLiving)
                || !(event.getSource().getImmediateSource() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getSource().getImmediateSource();
        ItemStack weapon = player.getHeldItemMainhand();
        if (weapon.isEmpty() || weapon.getItem() != EnigmaticLegacy.eldritchPan) {
            return;
        }
        int gain = (int) Math.floor(event.getEntityLiving().getMaxHealth() / MachineConfig.forbiddenPointDivisor);
        if (gain > 0) {
            ForbiddenPoints.set(weapon, ForbiddenPoints.add(ForbiddenPoints.get(weapon), gain));
        }
    }
}
