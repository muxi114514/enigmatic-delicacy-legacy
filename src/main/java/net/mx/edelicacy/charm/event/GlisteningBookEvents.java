package net.mx.edelicacy.charm.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.charm.CharmConfig;
import net.mx.edelicacy.charm.combat.IncomingDamage;
import net.mx.edelicacy.charm.glistening.GlisteningShield;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 闪耀之书（放在背包或古旧书袋）：
 * <ul>
 *   <li>受到的伤害 -10%；护盾按「伤害 ×0.9」抵扣，足够时整次伤害无效（在 LivingAttackEvent 取消，不击退不受击动画）</li>
 *   <li>近战命中回盾：最终伤害 ×10%（佩戴守护护符 ×16%）</li>
 *   <li>主手持书命中：攻击者获得受击无敌（1.21 写成 invulnerableTime = 10，恰好不触发无敌，等于没效果）</li>
 * </ul>
 */
public class GlisteningBookEvents {

    private static final float BOOK_REDUCTION = 0.9F;
    private static final float SHIELD_COST = 0.9F;

    /** 同一次攻击在 First Aid 下可能多次触发 LivingDamageEvent，只回一次盾 */
    private long lastGainKey = Long.MIN_VALUE;

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onAttacked(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        GlisteningShield.Book book = GlisteningShield.locate(player);
        if (book == null || book.shield() <= 0 || IncomingDamage.wouldBeIgnored(player, event.getSource(), event.getAmount())) {
            return;
        }
        int cost = shieldCost(event.getAmount() * BOOK_REDUCTION);
        if (book.shield() >= cost) {
            book.setShield(book.shield() - cost);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote) {
            return;
        }
        GlisteningShield.Book book = GlisteningShield.locate((EntityPlayer) event.getEntityLiving());
        if (book == null) {
            return;
        }
        float amount = event.getAmount() * BOOK_REDUCTION;
        int shield = book.shield();
        if (shield > 0) {
            int cost = shieldCost(amount);
            if (shield >= cost) {
                book.setShield(shield - cost);
                event.setCanceled(true);
                return;
            }
            amount -= shield;
            book.setShield(0);
        }
        event.setAmount(amount);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        Entity direct = source.getImmediateSource();
        if (!(direct instanceof EntityPlayer) || !"player".equals(source.getDamageType())) {
            return;
        }
        EntityPlayer attacker = (EntityPlayer) direct;
        if (DelicacyItems.THE_GLISTENING != null && attacker.getHeldItemMainhand().getItem() == DelicacyItems.THE_GLISTENING) {
            grantHitInvulnerability(attacker);
        }
        if (event.getAmount() >= Float.MAX_VALUE || isDuplicate(event.getEntityLiving(), attacker)) {
            return;
        }
        GlisteningShield.Book book = GlisteningShield.locate(attacker);
        if (book != null) {
            float ratio = EnigmaticBridge.hasBauble(attacker, DelicacyItems.GLISTENING_CHARM) ? 0.16F : 0.1F;
            book.setShield(book.shield() + MathHelper.floor(ratio * event.getAmount()));
        }
    }

    private static int shieldCost(float amount) {
        return MathHelper.ceil(amount * SHIELD_COST);
    }

    /** 按原版受击无敌的规则（只有超过上次伤害的部分能打进来）给攻击者若干 tick 无敌 */
    private static void grantHitInvulnerability(EntityLivingBase attacker) {
        int ticks = CharmConfig.glisteningBookHitInvulnerability;
        if (ticks > 0) {
            attacker.hurtResistantTime = Math.max(attacker.hurtResistantTime, attacker.maxHurtResistantTime / 2 + ticks);
        }
    }

    private boolean isDuplicate(EntityLivingBase victim, EntityPlayer attacker) {
        long key = (victim.world.getTotalWorldTime() << 32) ^ ((long) victim.getEntityId() << 16) ^ attacker.getEntityId();
        if (key == lastGainKey) {
            return true;
        }
        lastGainKey = key;
        return false;
    }
}
