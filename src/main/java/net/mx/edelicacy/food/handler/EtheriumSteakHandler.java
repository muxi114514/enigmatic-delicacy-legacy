package net.mx.edelicacy.food.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.mx.eaddons.attribute.AttributeSources;
import net.mx.eaddons.attribute.EnigmaticAttributes;
import net.mx.edelicacy.data.DelicacyData;

/**
 * 以太牛排的护盾加成：剩余时间 > 0 时以太护盾阈值 +0.08（神遗拓展属性来源，服务端对账）。
 * <p>剩余时间每 20 tick 扣 20 而不是每 tick 扣 1：DelicacyData 每次修改都会整份同步给客户端，
 * 逐 tick 扣会每 tick 发一个包。误差不超过 1 秒。
 */
public final class EtheriumSteakHandler {

    public static final String SOURCE_KEY = "enigmaticdelicacy.etherium_steak";
    private static final double SHIELD_BONUS = 0.08D;
    private static final int STEP = 20;

    public static void registerAttributeSource() {
        AttributeSources.register(SOURCE_KEY, EnigmaticAttributes.ETHERIUM_SHIELD, 0, 1,
                player -> DelicacyData.get(player).getEtheriumSteakTick() > 0 ? SHIELD_BONUS : 0.0D);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote || player.ticksExisted % STEP != 0) {
            return;
        }
        DelicacyData data = DelicacyData.get(player);
        int ticks = data.getEtheriumSteakTick();
        if (ticks > 0) {
            data.setEtheriumSteakTick(ticks - STEP);
        }
    }
}
