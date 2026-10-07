package net.mx.edelicacy.curse.potion;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.data.DelicacyData;
import net.mx.edelicacy.item.base.DelicacyFoodItem;

/**
 * 永久生命诅咒（斩断七咒之戒后）：
 * <ul>
 *   <li>阻断层：移除事件（牛奶、/effect clear、其他模组）一律取消；可施加判定强制允许</li>
 *   <li>补全层：死亡重生、被更高等级的临时诅咒顶替后到期等情况，每 10 tick 补回无限时长的诅咒</li>
 * </ul>
 * 1.21 原版用「剩余时长 &lt; 等级」判断是否补回，无限时长返回 -1 导致每 tick 都删了重加，这里改为按等级 / 时长判断。
 */
public class HealthCurseHandler {

    /** 低于这个剩余时长就视为不是永久诅咒，需要补回 */
    private static final int PERMANENT_THRESHOLD = 20 * 60 * 60;

    public static boolean isPermanentlyCursed(EntityPlayer player) {
        return DelicacyData.get(player).isHealthCursed();
    }

    /** 无限时长的永久诅咒效果 */
    public static PotionEffect permanentEffect() {
        return new PotionEffect(CursePotions.HEALTH_CURSE, DelicacyFoodItem.INFINITE, CurseConfig.healthCurseAmplifier, true, true);
    }

    /** 被永久诅咒却没有（或等级不足的）诅咒效果时补上 */
    public static void ensure(EntityPlayer player) {
        Potion curse = CursePotions.HEALTH_CURSE;
        if (curse == null || player.world.isRemote || !isPermanentlyCursed(player)) {
            return;
        }
        int amplifier = CurseConfig.healthCurseAmplifier;
        PotionEffect effect = player.getActivePotionEffect(curse);
        if (effect == null || effect.getAmplifier() < amplifier
                || (effect.getAmplifier() == amplifier && effect.getDuration() < PERMANENT_THRESHOLD)) {
            player.addPotionEffect(permanentEffect());
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.world.isRemote && event.player.ticksExisted % 10 == 0) {
            ensure(event.player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRemove(PotionEvent.PotionRemoveEvent event) {
        if (event.getPotion() == CursePotions.HEALTH_CURSE && event.getEntityLiving() instanceof EntityPlayer
                && isPermanentlyCursed((EntityPlayer) event.getEntityLiving())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onApplicable(PotionEvent.PotionApplicableEvent event) {
        if (event.getPotionEffect().getPotion() == CursePotions.HEALTH_CURSE && event.getEntityLiving() instanceof EntityPlayer
                && isPermanentlyCursed((EntityPlayer) event.getEntityLiving())) {
            event.setResult(Event.Result.ALLOW);
        }
    }
}
