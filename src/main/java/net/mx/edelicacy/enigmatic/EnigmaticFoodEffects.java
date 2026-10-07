package net.mx.edelicacy.enigmatic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.mx.edelicacy.data.DelicacyData;
import net.mx.edelicacy.enigmatic.api.EnigmaticColor;

/** 谜之食物的食用结算：永久记下颜色（加成由 {@link EnigmaticFoodBonuses} 按标记生效） */
public final class EnigmaticFoodEffects {

    private EnigmaticFoodEffects() {
    }

    /** 仅服务端生效；客户端调用直接忽略 */
    public static void markEaten(EntityPlayer player, EnigmaticColor color) {
        if (player == null || player.world.isRemote || color == null) {
            return;
        }
        DelicacyData.get(player).setFoodAttribute(color.dataKey(), true);
        // 1.12 没有信标激活音效，换成升级音效
        player.world.playSound(null, player.posX, player.posY, player.posZ,
                SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.75F, 1.0F);
    }
}
