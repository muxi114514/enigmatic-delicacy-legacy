package net.mx.edelicacy.curse.item.abyss;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import keletu.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.curse.util.CurseRefs;
import net.mx.edelicacy.curse.util.CurseUse;
import net.mx.edelicacy.data.DelicacyData;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 深渊状态（深渊乱炖）：
 * <ul>
 *   <li>受到伤害 -25%、造成伤害 +30%，每件穿戴 / 手持 / 佩戴的深渊物品再各 ±5%</li>
 *   <li>造成伤害时给目标深渊腐蚀 II 30 秒；击杀 +3 秒</li>
 *   <li>结束（或不再是天选之人）时返还一颗深渊之心</li>
 * </ul>
 * 修正 1.21：失去资格时每 tick 返还一颗深渊之心（计时被改回 tick-1 继续运行）；攻击加成误用了受害者的深渊物品数。
 * 计时每秒结算一次，客户端条形图自行插值。
 */
public class AbyssalStewHandler {

    public static boolean isActive(EntityLivingBase entity) {
        return entity instanceof EntityPlayer && DelicacyData.get((EntityPlayer) entity).getAbyssStewTick() > 0;
    }

    /** 装备栏、双手与饰品栏里的深渊物品件数 */
    public static int eldritchCount(EntityLivingBase entity) {
        int count = 0;
        for (ItemStack stack : entity.getEquipmentAndArmor()) {
            if (EnigmaticBridge.isEldritchItem(stack)) {
                count++;
            }
        }
        if (entity instanceof EntityPlayer) {
            IBaublesItemHandler baubles = BaublesApi.getBaublesHandler((EntityPlayer) entity);
            for (int i = 0; i < baubles.getSlots(); i++) {
                if (EnigmaticBridge.isEldritchItem(baubles.getStackInSlot(i))) {
                    count++;
                }
            }
        }
        return count;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote || player.ticksExisted % 20 != 0) {
            return;
        }
        DelicacyData data = DelicacyData.get(player);
        int tick = data.getAbyssStewTick();
        if (tick <= 0) {
            return;
        }
        boolean worthy = DelicacyItems.ABYSSAL_STEW == null || CurseUse.canUse(player, new ItemStack(DelicacyItems.ABYSSAL_STEW));
        if (!worthy || tick <= 20) {
            data.setAbyssStewTick(0);
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(CurseRefs.abyssalHeart()));
            player.world.playSound(null, player.posX, player.posY, player.posZ, EnigmaticLegacy.CHARGED_OFF_SOUND, SoundCategory.PLAYERS, 1.0F, 0.25F);
        } else {
            data.setAbyssStewTick(tick - 20);
        }
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (!isActive(victim)) {
            return;
        }
        float amount = event.getAmount() * (1.0F - 0.01F * CurseConfig.abyssalStewDamageResistance);
        amount *= Math.max(0.0F, 1.0F - 0.01F * CurseConfig.abyssBoostRate * eldritchCount(victim));
        event.setAmount(amount);
    }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
        if (attacker.world.isRemote || !isActive(attacker) || attacker == event.getEntityLiving()) {
            return;
        }
        float amount = event.getAmount() * (1.0F + 0.01F * CurseConfig.abyssalStewDamageBoost);
        amount *= 1.0F + 0.01F * CurseConfig.abyssBoostRate * eldritchCount(attacker);
        event.setAmount(amount);
        if (CursePotions.ABYSS_CORRUPTION != null && amount > 0) {
            event.getEntityLiving().addPotionEffect(new PotionEffect(CursePotions.ABYSS_CORRUPTION, 600, 1));
        }
    }

    @SubscribeEvent
    public void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        DelicacyData data = DelicacyData.get(player);
        if (!player.world.isRemote && data.getAbyssStewTick() > 0) {
            data.setAbyssStewTick(data.getAbyssStewTick() + 60);
        }
    }
}
