package net.mx.edelicacy.curse.item.forbidden;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.registry.DelicacyItems;
import org.apache.logging.log4j.LogManager;

/**
 * 禁忌之刃：
 * <ul>
 *   <li>玩家近战命中带禁忌之印的目标：伤害 ×(1.08 + 等级×0.08)</li>
 *   <li>命中没有印记的目标：附加 5~10 秒、随机 I~III 级的禁忌之印（下一击才吃增伤）</li>
 *   <li>玩家用它击杀时 25% 概率把所有掉落物做禁忌转化（{@link ForbiddenConversion}）</li>
 * </ul>
 */
public class ForbiddenKnifeHandler {

    private static boolean holdsKnife(EntityLivingBase entity) {
        return entity.getHeldItemMainhand().getItem() == DelicacyItems.FORBIDDEN_KNIFE;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingDamageEvent event) {
        if (!(event.getSource().getImmediateSource() instanceof EntityLivingBase) || CursePotions.FORBIDDEN_IMPRINT == null) {
            return;
        }
        EntityLivingBase attacker = (EntityLivingBase) event.getSource().getImmediateSource();
        if (attacker.world.isRemote || !holdsKnife(attacker)) {
            return;
        }
        EntityLivingBase victim = event.getEntityLiving();
        PotionEffect imprint = victim.getActivePotionEffect(CursePotions.FORBIDDEN_IMPRINT);
        if (imprint != null) {
            if ("player".equals(event.getSource().getDamageType())) {
                event.setAmount(event.getAmount() * (1.08F + imprint.getAmplifier() * 0.08F));
            }
        } else {
            victim.addPotionEffect(new PotionEffect(CursePotions.FORBIDDEN_IMPRINT, 100 + victim.getRNG().nextInt(100),
                    victim.getRNG().nextInt(3)));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDrops(LivingDropsEvent event) {
        if (!(event.getSource().getImmediateSource() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getSource().getImmediateSource();
        if (player.world.isRemote || !holdsKnife(player) || player.getRNG().nextInt(4) != 0) {
            return;
        }
        EntityLivingBase dead = event.getEntityLiving();
        List<EntityItem> converted = new ArrayList<>();
        ListIterator<EntityItem> iterator = event.getDrops().listIterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next().getItem();
            ItemStack result;
            try {
                result = ForbiddenConversion.convert(stack, dead.world);
            } catch (RuntimeException e) {
                LogManager.getLogger("enigmaticdelicacy").warn("Forbidden conversion failed for {}", stack, e);
                continue;
            }
            if (result.isEmpty()) {
                continue;
            }
            iterator.remove();
            int total = stack.getCount() * result.getCount();
            while (total > 0) {
                ItemStack out = result.copy();
                out.setCount(Math.min(out.getMaxStackSize(), total));
                total -= out.getCount();
                EntityItem entity = new EntityItem(dead.world, dead.posX, dead.posY, dead.posZ, out);
                entity.setPickupDelay(10);
                converted.add(entity);
            }
        }
        event.getDrops().addAll(converted);
    }
}
