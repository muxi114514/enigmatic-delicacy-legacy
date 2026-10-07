package net.mx.edelicacy.fading;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.network.DelicacyNetwork;
import net.mx.edelicacy.registry.DelicacyPotions;

/**
 * 服务端：跟踪处于失色的实体并通知客户端（对应 1.21 的 FadingData 附加数据）。
 * <p>每 2 tick 对比「是否有失色」与已知状态，变化才发包给追踪者和本人；
 * 新开始追踪、登录、换维度时补发（1.21 只在状态变化时发，后进入视野的玩家看不到失色）。
 * 状态只存内存，重启后由对比自动恢复。
 */
public class FadingSync {

    private static final Set<UUID> ACTIVE = ConcurrentHashMap.newKeySet();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote || entity.ticksExisted % 2 != 0) {
            return;
        }
        Potion fading = DelicacyPotions.FADING;
        boolean has = fading != null && entity.isPotionActive(fading);
        UUID id = entity.getUniqueID();
        if (has == ACTIVE.contains(id)) {
            return;
        }
        if (has) {
            ACTIVE.add(id);
        } else {
            ACTIVE.remove(id);
        }
        broadcast(entity, has);
    }

    /** 死亡后清掉（玩家重生沿用同一实体编号，不清会让重生后的玩家继续隐形） */
    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.world.isRemote && ACTIVE.remove(entity.getUniqueID())) {
            broadcast(entity, false);
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        if (target instanceof EntityLivingBase && event.getEntityPlayer() instanceof EntityPlayerMP
                && ACTIVE.contains(target.getUniqueID())) {
            DelicacyNetwork.CHANNEL.sendTo(new FadingSyncMessage(target.getEntityId(), true), (EntityPlayerMP) event.getEntityPlayer());
        }
    }

    @SubscribeEvent
    public void onLogin(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        syncSelf(event.player);
    }

    @SubscribeEvent
    public void onChangeDimension(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
        syncSelf(event.player);
    }

    @SubscribeEvent
    public void onLogout(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.player.getUniqueID());
    }

    /** 客户端换世界会清空本地记录，本人的状态需要补发；同时发给此刻已在追踪的玩家（可能早于本事件开始追踪） */
    private static void syncSelf(EntityPlayer player) {
        Potion fading = DelicacyPotions.FADING;
        if (player instanceof EntityPlayerMP && fading != null && player.isPotionActive(fading)) {
            ACTIVE.add(player.getUniqueID());
            broadcast(player, true);
        }
    }

    private static void broadcast(EntityLivingBase entity, boolean active) {
        FadingSyncMessage message = new FadingSyncMessage(entity.getEntityId(), active);
        DelicacyNetwork.CHANNEL.sendToAllTracking(message, entity);
        if (entity instanceof EntityPlayerMP) {
            DelicacyNetwork.CHANNEL.sendTo(message, (EntityPlayerMP) entity);
        }
    }
}
