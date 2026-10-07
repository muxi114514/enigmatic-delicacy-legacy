package net.mx.edelicacy.effect.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.effect.PotionAstralDrunkenness;
import net.mx.edelicacy.effect.PotionReflection;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.registry.DelicacyPotions;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 星辉酩酊：
 * <ul>
 *   <li>玩家已有本效果时再次获得：随机升 0~(新等级+1) 级（最高 V），时长取新旧均值；升 0 级或已到 V 则本次无效</li>
 *   <li>造成的近战伤害每级 -5%，V 级 -40%；暴击倍率每级 +0.1</li>
 *   <li>V 级每 10 tick：2 点饥饿伤害、反胃与饥饿 III、清除全部有益效果</li>
 *   <li>佩戴星辉护符免除减伤与 V 级惩罚</li>
 * </ul>
 */
public final class AstralDrunkennessHandler {

    /** 每隔几 tick 重算一次粒子颜色（会同步一个实体元数据，取 4 控制开销） */
    private static final int COLOR_REFRESH_INTERVAL = 4;
    private static final int OVERDOSE_INTERVAL = 10;

    /** 重新施加升级后的效果时防止自己再次拦截；只在服务端主线程读写 */
    private boolean upgrading;

    private static boolean hasCharm(EntityLivingBase entity) {
        return entity instanceof EntityPlayer && EnigmaticBridge.hasBauble((EntityPlayer) entity, DelicacyItems.ASTRAL_CHARM);
    }

    /** 1.21 的 MobEffectEvent.Applicable 改写实例；1.12 的效果字段私有，改为拒绝原效果、再施加合并后的效果 */
    @SubscribeEvent
    public void onApplicable(PotionEvent.PotionApplicableEvent event) {
        Potion drunk = DelicacyPotions.ASTRAL_DRUNKENNESS;
        PotionEffect incoming = event.getPotionEffect();
        EntityLivingBase entity = event.getEntityLiving();
        if (!(entity instanceof EntityPlayerMP) || upgrading || drunk == null || incoming.getPotion() != drunk) {
            return;
        }
        PotionEffect current = entity.getActivePotionEffect(drunk);
        if (current == null) {
            return;
        }
        event.setResult(Event.Result.DENY);
        // 指令给的等级可能溢出成负数，至少取 1 防止 nextInt 抛异常
        int step = entity.getRNG().nextInt(Math.max(1, incoming.getAmplifier() + 2));
        if (step <= 0 || current.getAmplifier() >= PotionAstralDrunkenness.MAX_AMPLIFIER) {
            return;
        }
        int amplifier = Math.min(current.getAmplifier() + step, PotionAstralDrunkenness.MAX_AMPLIFIER);
        int duration = (Math.max(incoming.getDuration(), current.getDuration()) + current.getDuration()) / 2;
        upgrading = true;
        try {
            // 等级更高，combine 会整体替换等级与时长并重挂攻速修饰符
            entity.addPotionEffect(new PotionEffect(drunk, duration, amplifier, incoming.getIsAmbient(), incoming.doesShowParticles()));
        } finally {
            upgrading = false;
        }
    }

    /** 在 updatePotionEffects 之前执行：此时不在遍历效果表，增删效果不会触发并发修改 */
    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        Potion drunk = DelicacyPotions.ASTRAL_DRUNKENNESS;
        if (drunk == null || entity.world.isRemote) {
            return;
        }
        PotionEffect effect = entity.getActivePotionEffect(drunk);
        if (effect == null) {
            return;
        }
        if (entity.ticksExisted % COLOR_REFRESH_INTERVAL == 0) {
            PotionReflection.markPotionsDirty(entity);
        }
        if (effect.getAmplifier() >= PotionAstralDrunkenness.MAX_AMPLIFIER && effect.getDuration() % OVERDOSE_INTERVAL == 0
                && !hasCharm(entity)) {
            overdose(entity);
        }
    }

    private static void overdose(EntityLivingBase entity) {
        entity.attackEntityFrom(DamageSource.STARVE, 2.0F);
        entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 215));
        entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 215, 2));
        List<PotionEffect> effects = new ArrayList<>(entity.getActivePotionEffects());
        for (PotionEffect active : effects) {
            if (PotionReflection.isBeneficial(active.getPotion())) {
                entity.removePotionEffect(active.getPotion());
            }
        }
    }

    /** 只算直接攻击者（与 1.21 的 getDirectEntity 一致，弹射物不受影响） */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onHurt(LivingHurtEvent event) {
        Potion drunk = DelicacyPotions.ASTRAL_DRUNKENNESS;
        Entity direct = event.getSource().getImmediateSource();
        if (drunk == null || !(direct instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase attacker = (EntityLivingBase) direct;
        PotionEffect effect = attacker.getActivePotionEffect(drunk);
        if (effect == null || hasCharm(attacker)) {
            return;
        }
        int level = effect.getAmplifier() + 1;
        float reduction = level < 5 ? 0.05F * level : 0.4F;
        event.setAmount(event.getAmount() * (1.0F - reduction));
    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        Potion drunk = DelicacyPotions.ASTRAL_DRUNKENNESS;
        PotionEffect effect = drunk == null ? null : event.getEntityPlayer().getActivePotionEffect(drunk);
        if (effect != null) {
            event.setDamageModifier(event.getDamageModifier() + (effect.getAmplifier() + 1) * 0.1F);
        }
    }
}
