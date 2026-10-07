package net.mx.edelicacy.curse.item.blade;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.curse.damage.CurseDamageSources;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 蓄力击（护甲后伤害、最后结算）：自身受到当前生命 (等级+1)×5% 的诅咒伤害，目标额外受到其最大生命同比例的伤害并获得生命诅咒，
 * 刃片进入 100 tick 冷却。只认近战直击（1.21 认任何真实来源，持刃射箭也会消耗蓄力）。
 */
public class CurseBladeHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingDamageEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)
                || event.getSource().getImmediateSource() != event.getSource().getTrueSource()) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        EntityLivingBase victim = event.getEntityLiving();
        ItemStack weapon = player.getHeldItemMainhand();
        if (player == victim || player.world.isRemote || weapon.getItem() != DelicacyItems.CURSE_BLADE || !ItemCurseBlade.isActive(weapon)) {
            return;
        }
        ItemCurseBlade.setActive(weapon, false);
        player.getCooldownTracker().setCooldown(weapon.getItem(), 100);
        float percentage = CurseConfig.curseBladeAmplifier * 0.05F + 0.05F;
        player.attackEntityFrom(CurseDamageSources.EVIL_CURSE, player.getHealth() * percentage);
        event.setAmount(event.getAmount() + victim.getMaxHealth() * percentage);
        if (CursePotions.HEALTH_CURSE != null) {
            victim.addPotionEffect(new PotionEffect(CursePotions.HEALTH_CURSE, CurseConfig.curseBladeDuration,
                    CurseConfig.curseBladeAmplifier, true, true));
        }
        if (player.world instanceof WorldServer) {
            ((WorldServer) player.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, victim.posX, victim.posY + victim.height / 2.0D,
                    victim.posZ, 24, victim.width / 2.0D, victim.height / 2.0D, victim.width / 2.0D, 0.0D);
        }
    }
}
