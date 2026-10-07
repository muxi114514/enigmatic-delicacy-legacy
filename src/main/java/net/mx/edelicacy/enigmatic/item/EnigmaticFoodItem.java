package net.mx.edelicacy.enigmatic.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.enigmatic.EnigmaticFoodBonuses;
import net.mx.edelicacy.enigmatic.EnigmaticFoodEffects;
import net.mx.edelicacy.enigmatic.api.EnigmaticColor;
import net.mx.edelicacy.enigmatic.api.IEnigmaticFood;
import net.mx.edelicacy.item.base.DelicacyFoodItem;
import net.mx.edelicacy.util.TooltipHelper;

/** 谜之食物（对应 1.21 BaseEnigmaticFood）：普通食物效果 + 永久记下颜色，提示按住 Shift 展开 */
public class EnigmaticFoodItem extends DelicacyFoodItem implements IEnigmaticFood {

    private final EnigmaticColor color;

    public EnigmaticFoodItem(String name, int hunger, float saturation, EnigmaticColor color) {
        super(name, hunger, saturation);
        this.color = color;
        alwaysEdible();
        rarity(EnumRarity.UNCOMMON);
    }

    @Override
    public EnigmaticColor getEnigmaticColor() {
        return color;
    }

    @Override
    protected void onEaten(ItemStack stack, World world, EntityPlayer player) {
        EnigmaticFoodEffects.markEaten(player, color);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (!TooltipHelper.shiftDown()) {
            TooltipHelper.holdShift(tooltip);
            return;
        }
        addEffectTooltip(tooltip);
        if (!tooltip.isEmpty()) {
            TooltipHelper.empty(tooltip);
        }
        TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.enigmaticFood");
        tooltip.add(I18n.format(EnigmaticFoodBonuses.tooltipKey(color), EnigmaticFoodBonuses.displayValue(color)));
    }
}
