package net.mx.edelicacy.curse.item.blade;

import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCuttingBoard;

import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.curse.damage.CurseDamageSources;
import net.mx.edelicacy.curse.item.soul.SoulNBT;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.curse.potion.HealthCurseHandler;
import net.mx.edelicacy.curse.util.CurseItemEntities;
import net.mx.edelicacy.curse.util.CurseRefs;
import net.mx.edelicacy.data.DelicacyData;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 斩断七咒之戒：佩戴七咒之戒、手持已蓄力的诅咒刃片右键空砧板。
 * 戒指脱落到砧板上，其余诅咒饰品散落在周围（发光永久掉落物），刃片损耗 60% 剩余耐久，
 * 砧板上方浮现只有本人能拾取的破碎咒魂晶；此后永久背负生命诅咒（诅咒药水可解），每名玩家只能斩一次（创造模式除外）。
 */
public class CursedRingCutter {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack blade = event.getItemStack();
        if (blade.getItem() != DelicacyItems.CURSE_BLADE || !ItemCurseBlade.isActive(blade)
                || !EnigmaticBridge.hasBauble(player, CurseRefs.cursedRing())) {
            return;
        }
        if (DelicacyData.get(player).isCursedRingCut() && !player.capabilities.isCreativeMode) {
            return;
        }
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityCuttingBoard) || !((TileEntityCuttingBoard) tile).getStoredItem().isEmpty()) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (!world.isRemote) {
            cutRing(world, pos, player, blade);
        }
    }

    private static void cutRing(World world, BlockPos pos, EntityPlayer player, ItemStack blade) {
        Item ring = CurseRefs.cursedRing();
        ItemStack ringStack = EnigmaticBridge.getBauble(player, ring).copy();
        unequip(player, ringStack);
        EnigmaticBridge.destroyBauble(player, ring);
        dropCursedBaubles(world, player);

        int remaining = blade.getMaxDamage() - blade.getItemDamage();
        blade.damageItem((int) Math.floor(remaining * 0.6D), player);
        EntityItem ringDrop = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D, ringStack);
        ringDrop.motionX = 0.0D;
        ringDrop.motionY = 0.06D;
        ringDrop.motionZ = 0.0D;
        ringDrop.setDefaultPickupDelay();
        world.spawnEntity(ringDrop);
        player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 50, 4));
        player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 50));

        if (DelicacyItems.BROKEN_CURSED_SOUL_CRYSTAL != null) {
            ItemStack crystal = new ItemStack(DelicacyItems.BROKEN_CURSED_SOUL_CRYSTAL);
            SoulNBT.setSoulThreshold(crystal, player);
            CurseItemEntities.spawnPermanent(world, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, crystal, player, 120);
        }

        DelicacyData data = DelicacyData.get(player);
        data.setCursedRingCut(true);
        data.setHealthCursed(true);
        if (CursePotions.HEALTH_CURSE != null) {
            player.addPotionEffect(HealthCurseHandler.permanentEffect());
        }
        ItemCurseBlade.setActive(blade, false);
        player.attackEntityFrom(CurseDamageSources.EVIL_CURSE, Math.max(1.0F, player.getHealth() * 0.3F));
    }

    /** 其余诅咒饰品散落在玩家周围 */
    private static void dropCursedBaubles(World world, EntityPlayer player) {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (!EnigmaticBridge.isCursedItem(stack)) {
                continue;
            }
            ItemStack dropped = stack.copy();
            unequip(player, dropped);
            baubles.setStackInSlot(i, ItemStack.EMPTY);
            double x = player.posX + (world.rand.nextDouble() * 2.0D - 1.0D) * player.width * 4.0D;
            double z = player.posZ + (world.rand.nextDouble() * 2.0D - 1.0D) * player.width * 4.0D;
            double y = player.posY + world.rand.nextDouble() * player.height;
            CurseItemEntities.spawnPermanent(world, x, y, z, dropped, null, 10);
        }
    }

    /** 直接改槽位不会触发卸下回调，手动调用以撤掉属性修饰等 */
    private static void unequip(EntityPlayer player, ItemStack stack) {
        if (stack.getItem() instanceof IBauble) {
            try {
                ((IBauble) stack.getItem()).onUnequipped(stack, player);
            } catch (RuntimeException e) {
                org.apache.logging.log4j.LogManager.getLogger("enigmaticdelicacy").warn("onUnequipped failed for {}", stack, e);
            }
        }
    }
}
