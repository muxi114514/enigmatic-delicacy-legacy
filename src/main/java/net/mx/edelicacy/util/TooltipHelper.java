package net.mx.edelicacy.util;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 物品提示（对应 1.21 的 TooltipHandler）：按住 Shift 展开、逐行本地化 */
@SideOnly(Side.CLIENT)
public final class TooltipHelper {

    private TooltipHelper() {
    }

    public static boolean shiftDown() {
        return GuiScreen.isShiftKeyDown();
    }

    public static boolean altDown() {
        return GuiScreen.isAltKeyDown();
    }

    /** 「按住 Shift 查看详情」，沿用 EL 的语言键 */
    public static void holdShift(List<String> tooltip) {
        tooltip.add(I18n.format("tooltip.enigmaticlegacy.holdShift"));
    }

    public static void line(List<String> tooltip, String key, Object... args) {
        tooltip.add(I18n.format(key, args));
    }

    public static void empty(List<String> tooltip) {
        tooltip.add("");
    }

    /** 0.25 → "25%" */
    public static String percent(double value) {
        double scaled = value * 100;
        return (scaled == Math.rint(scaled) ? String.valueOf((long) scaled) : String.format("%.1f", scaled)) + "%";
    }
}
