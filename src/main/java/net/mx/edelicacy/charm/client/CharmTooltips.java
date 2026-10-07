package net.mx.edelicacy.charm.client;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 饰品的属性提示（Baubles 不像 Curios 那样自动列出佩戴属性，这里按原版格式补上） */
@SideOnly(Side.CLIENT)
public final class CharmTooltips {

    private CharmTooltips() {
    }

    /** 「佩戴时：」标题，沿用 EL 的语言键 */
    public static void header(List<String> tooltip) {
        tooltip.add("");
        tooltip.add(I18n.format("tooltip.enigmaticlegacy.simpleRingAttributes"));
    }

    /** 一行属性加成，operation 1/2 按百分比显示 */
    public static void attribute(List<String> tooltip, IAttribute attribute, int operation, double amount) {
        double shown = operation == 0 ? amount : amount * 100.0D;
        tooltip.add(TextFormatting.BLUE + I18n.format("attribute.modifier.plus." + operation,
                ItemStack.DECIMALFORMAT.format(shown), I18n.format("attribute.name." + attribute.getName())));
    }
}
