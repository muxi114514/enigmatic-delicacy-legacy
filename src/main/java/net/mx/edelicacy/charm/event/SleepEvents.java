package net.mx.edelicacy.charm.event;

import keletu.enigmaticlegacy.EnigmaticConfigs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.mx.edelicacy.charm.AstralTranquility;
import net.mx.edelicacy.charm.SleepTracker;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;
import net.mx.edelicacy.util.PlayerSleepAccess;

/**
 * 睡眠相关：天体安神茶的入睡提示与醒来后标记的保留（星花护符 / 安宁之戒），安宁之戒醒来回满血。
 * <p>1.21 的安宁之戒回血漏了「佩戴」判断，所有玩家醒来都回满；这里只给佩戴者。
 */
public class SleepEvents {

    /** 1.21 在入睡第 30 tick 提示 */
    private static final int MESSAGE_TICK = 30;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote) {
            return;
        }
        SleepTracker.track(player);
        if (!player.isPlayerSleeping() || PlayerSleepAccess.getSleepTimer(player) != MESSAGE_TICK) {
            return;
        }
        if (EnigmaticBridge.isCursed(player) && AstralTranquility.isTranquilized(player)) {
            TextComponentTranslation message = new TextComponentTranslation("message.enigmaticdelicacy.cursed_sleep_with_tea");
            message.getStyle().setColor(TextFormatting.GOLD);
            player.sendMessage(message);
        }
    }

    /** 只处理真睡了一觉（夜晚被跳过）；点「离开床」、被打醒都不算 */
    @SubscribeEvent
    public void onWakeUp(PlayerWakeUpEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) {
            return;
        }
        boolean sleptThrough = SleepTracker.sleptThrough(player, !event.updateWorld());
        if (event.wakeImmediately() || !sleptThrough) {
            return;
        }
        boolean ring = EnigmaticBridge.hasBauble(player, DelicacyItems.PETAL_RING);
        boolean charm = EnigmaticBridge.hasBauble(player, DelicacyItems.ASTRAL_CHARM);
        if (ring) {
            player.heal(player.getMaxHealth());
        }
        if (!EnigmaticBridge.isCursed(player) || !EnigmaticConfigs.enableInsomnia || !AstralTranquility.isTranquilized(player)) {
            return;
        }
        // 两件都戴必定保留，戴一件 80% 保留
        int boosts = (ring ? 1 : 0) + (charm ? 1 : 0);
        if (boosts > 1 || (boosts == 1 && player.getRNG().nextInt(10) < 8)) {
            return;
        }
        AstralTranquility.clear(player);
    }

    @SubscribeEvent
    public void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SleepTracker.forget(event.player);
    }
}
