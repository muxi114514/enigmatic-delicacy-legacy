package net.mx.edelicacy.effect.handler;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.mx.edelicacy.registry.DelicacyPotions;

/**
 * 坚毅：受到伤害后获得同等级的伤害吸收 418 tick。
 * <p>1.21 在伤害结算后（LivingDamageEvent.Post）施加。1.12 的 LivingDamageEvent 在扣血之前，
 * 原版随后还会从吸收值里再扣一次本次伤害，当场施加会被这一击吃掉，所以登记下来到本 tick 末尾再施加；
 * 同一 tick 多次受伤（含 First Aid 分部位多次触发）只施加一次。表每 tick 清空，不会积累。
 */
public final class PerseveranceHandler {

    private static final int ABSORPTION_TICKS = 418;

    private final Map<EntityLivingBase, Integer> pending = new ConcurrentHashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingDamageEvent event) {
        Potion perseverance = DelicacyPotions.PERSEVERANCE;
        EntityLivingBase victim = event.getEntityLiving();
        if (perseverance == null || victim.world.isRemote) {
            return;
        }
        PotionEffect effect = victim.getActivePotionEffect(perseverance);
        if (effect != null) {
            pending.merge(victim, effect.getAmplifier(), Math::max);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pending.isEmpty()) {
            return;
        }
        Potion perseverance = DelicacyPotions.PERSEVERANCE;
        Iterator<Map.Entry<EntityLivingBase, Integer>> iterator = pending.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<EntityLivingBase, Integer> entry = iterator.next();
            iterator.remove();
            EntityLivingBase victim = entry.getKey();
            if (perseverance != null && victim.isEntityAlive() && victim.isPotionActive(perseverance)) {
                victim.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, ABSORPTION_TICKS, entry.getValue()));
            }
        }
    }
}
