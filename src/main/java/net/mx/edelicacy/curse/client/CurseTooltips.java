package net.mx.edelicacy.curse.client;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.curse.potion.PotionForbiddenImprint;
import net.mx.edelicacy.curse.util.CurseRefs;
import net.mx.edelicacy.util.EnigmaticBridge;
import net.mx.edelicacy.util.TooltipHelper;

/** 本线物品提示的公共写法（客户端）：对应 1.21 的 TooltipHandler 与 BaseForbiddenFood.forbiddenOnly */
@SideOnly(Side.CLIENT)
public final class CurseTooltips {

    private CurseTooltips() {
    }

    public static boolean shift() {
        return TooltipHelper.shiftDown();
    }

    public static void holdShift(List<String> tooltip) {
        TooltipHelper.holdShift(tooltip);
    }

    public static void line(List<String> tooltip, String key, Object... args) {
        TooltipHelper.line(tooltip, key, args);
    }

    public static void empty(List<String> tooltip) {
        TooltipHelper.empty(tooltip);
    }

    @Nullable
    public static EntityPlayer localPlayer() {
        return Minecraft.getMinecraft().player;
    }

    public static boolean localForbidden() {
        return EnigmaticBridge.isForbidden(localPlayer());
    }

    public static boolean localPlayerHasCursedRing() {
        return EnigmaticBridge.hasBauble(localPlayer(), CurseRefs.cursedRing());
    }

    /** 「仅品尝过禁忌之果的人可用」：本人符合时金色，否则暗红 */
    public static void forbiddenOnly(List<String> tooltip) {
        TextFormatting color = localForbidden() ? TextFormatting.GOLD : TextFormatting.DARK_RED;
        tooltip.add(color + I18n.format("tooltip.enigmaticdelicacy.forbiddenCursedOnly1"));
        tooltip.add(color + I18n.format("tooltip.enigmaticdelicacy.forbiddenCursedOnly2"));
    }

    /** 本人身上禁忌之印对治疗的削减系数 */
    public static float imprintHealFactor() {
        EntityPlayer player = localPlayer();
        if (player == null || CursePotions.FORBIDDEN_IMPRINT == null) {
            return 1.0F;
        }
        PotionEffect effect = player.getActivePotionEffect(CursePotions.FORBIDDEN_IMPRINT);
        return effect == null ? 1.0F : 1.0F - PotionForbiddenImprint.reduction(effect.getAmplifier());
    }

    /** 禁忌食物的「食用后：+X 生命值」（只给禁忌之人看） */
    public static void forbiddenHeal(List<String> tooltip, float heal) {
        if (!localForbidden()) {
            return;
        }
        empty(tooltip);
        line(tooltip, "tooltip.enigmaticdelicacy.enigmaticFood");
        line(tooltip, "tooltip.enigmaticdelicacy.forbiddenHeal", String.format("%.1f", heal * imprintHealFactor()));
    }
}
