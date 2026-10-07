package net.mx.edelicacy.curse.potion;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.curse.damage.CurseDamageSources;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 深渊腐蚀：无法移除、强制可施加；带着它死亡时，向 4.2 格内非天选之人的生物传染同样的效果并造成死者最大生命 80% 的深渊伤害。
 * <p>传染会连锁触发其他死亡，限制递归深度防止栈溢出（只在服务端主线程运行）。
 */
public class AbyssCorruptionHandler {

    private static final int MAX_CHAIN_DEPTH = 8;
    private static final double SPREAD_RANGE = 4.2D;
    private static int chainDepth;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRemoveHigh(PotionEvent.PotionRemoveEvent event) {
        if (CursePotions.ABYSS_CORRUPTION != null && event.getPotion() == CursePotions.ABYSS_CORRUPTION) {
            event.setCanceled(true);
        }
    }

    /** 再在最低优先级取消一次，防止中间有处理器撤销取消 */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onRemoveLow(PotionEvent.PotionRemoveEvent event) {
        onRemoveHigh(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onApplicable(PotionEvent.PotionApplicableEvent event) {
        if (CursePotions.ABYSS_CORRUPTION != null && event.getPotionEffect().getPotion() == CursePotions.ABYSS_CORRUPTION) {
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (victim.world.isRemote || CursePotions.ABYSS_CORRUPTION == null || chainDepth >= MAX_CHAIN_DEPTH) {
            return;
        }
        PotionEffect effect = victim.getActivePotionEffect(CursePotions.ABYSS_CORRUPTION);
        if (effect == null) {
            return;
        }
        chainDepth++;
        try {
            List<EntityLivingBase> targets = victim.world.getEntitiesWithinAABB(EntityLivingBase.class,
                    victim.getEntityBoundingBox().grow(SPREAD_RANGE));
            for (EntityLivingBase target : targets) {
                if (target == victim || !target.isEntityAlive()) {
                    continue;
                }
                if (target instanceof EntityPlayer && EnigmaticBridge.isWorthy((EntityPlayer) target)) {
                    continue;
                }
                target.addPotionEffect(new PotionEffect(effect));
                target.attackEntityFrom(CurseDamageSources.abyssFrom(victim), victim.getMaxHealth() * 0.8F);
            }
        } finally {
            chainDepth--;
        }
    }
}
