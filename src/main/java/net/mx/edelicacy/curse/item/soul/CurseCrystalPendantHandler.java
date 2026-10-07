package net.mx.edelicacy.curse.item.soul;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import keletu.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 咒魂晶坠：
 * <ul>
 *   <li>攻击者生命比例越低，对你的伤害越低（× 2^(比例-1)）</li>
 *   <li>每次受伤后恢复该次伤害的 24%：1.12 没有伤害后事件，记下数值在下一 tick 治疗（伤害前治疗在满血时会被上限吞掉）</li>
 *   <li>所受治疗 ×(1+25%)：1.21 代码是 ×25%，与其中英文提示「提高 25%」矛盾，按提示实现并可配置</li>
 *   <li>受到致死伤害时消耗咒魂值复活（最多恢复 75% 生命上限），并获得生命诅咒 IV 30 秒</li>
 * </ul>
 */
public class CurseCrystalPendantHandler {

    private final Map<UUID, Float> pendingRecovery = new ConcurrentHashMap<>();

    private static boolean wearing(EntityLivingBase entity) {
        return entity instanceof EntityPlayer && EnigmaticBridge.hasBauble((EntityPlayer) entity, DelicacyItems.CURSE_CRYSTAL_PENDANT);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingDamageEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (victim.world.isRemote || !wearing(victim)) {
            return;
        }
        if (event.getSource().getTrueSource() instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
            float ratio = attacker.getHealth() / Math.max(1.0F, attacker.getMaxHealth());
            event.setAmount(event.getAmount() * (float) Math.pow(2, ratio - 1));
        }
        float recovery = event.getAmount() * CurseConfig.pendantRecoveryRate * 0.01F;
        if (recovery > 0) {
            pendingRecovery.merge(victim.getUniqueID(), recovery, Float::sum);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player.world.isRemote || pendingRecovery.isEmpty()) {
            return;
        }
        Float recovery = pendingRecovery.remove(event.player.getUniqueID());
        if (recovery != null && event.player.isEntityAlive()) {
            event.player.heal(recovery);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        pendingRecovery.remove(event.player.getUniqueID());
    }

    @SubscribeEvent
    public void onHeal(LivingHealEvent event) {
        if (wearing(event.getEntityLiving())) {
            event.setAmount(Math.max(0.0F, event.getAmount() * (1.0F + CurseConfig.pendantHealingModifier * 0.01F)));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || !(event.getEntityLiving().world instanceof WorldServer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        ItemStack pendant = EnigmaticBridge.getBauble(player, DelicacyItems.CURSE_CRYSTAL_PENDANT);
        int souls = SoulNBT.getSoulAmount(pendant);
        if (pendant.isEmpty() || souls <= 0) {
            return;
        }
        event.setCanceled(true);
        float restore = Math.min(souls, player.getMaxHealth() * 0.75F);
        // 复活：生命已归零，heal 不生效，只能直接设置（同原版不死图腾）
        player.setHealth(restore);
        SoulNBT.setSoulAmount(pendant, MathHelper.floor(souls - restore));
        pendingRecovery.remove(player.getUniqueID());
        if (CursePotions.HEALTH_CURSE != null) {
            player.addPotionEffect(new PotionEffect(CursePotions.HEALTH_CURSE, 600, 3));
        }
        WorldServer world = (WorldServer) player.world;
        world.playSound(null, player.posX, player.posY, player.posZ, EnigmaticLegacy.DEFLECT_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
        world.spawnParticle(EnumParticleTypes.SPELL_WITCH, player.posX, player.posY + player.height / 2.0D, player.posZ,
                24, player.width / 2.0D, player.height / 2.0D, player.width / 2.0D, 0.0D);
    }
}
