package net.mx.edelicacy.curse.item.gluttony;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;

import baubles.api.BaubleType;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.item.base.DelicacyBaubleItem;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 无尽贪食徽记（诅咒护符，与禁忌之人互斥）：按缺失饥饿比例提供攻击、暴击伤害、移速与减伤，免疫饥饿伤害，
 * 佩戴期间持续消耗饱食度。移速走饰品属性修饰，其余见 {@link GluttonyCharmHandler}。
 */
public class ItemGluttonyCharm extends DelicacyBaubleItem {

    private static final UUID SPEED_ID = UUID.fromString("3c9d5e1a-8b74-4e2f-9a61-7f0b2c4d8e93");

    public ItemGluttonyCharm() {
        super("gluttony_charm", BaubleType.CHARM);
        rarity(EnumRarity.RARE);
    }

    @Override
    protected void fillModifiers(Multimap<String, AttributeModifier> modifiers, ItemStack stack, EntityLivingBase wearer) {
        double speed = GluttonyLogic.missingFoodProperty(wearer) * CurseConfig.gluttonyMovementSpeed;
        modifiers.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
                new AttributeModifier(SPEED_ID, "enigmaticdelicacy:gluttony_charm", speed, 1));
    }

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase wearer) {
        return super.canEquip(stack, wearer) && !(wearer instanceof EntityPlayer && EnigmaticBridge.isForbidden((EntityPlayer) wearer));
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase wearer) {
        super.onWornTick(stack, wearer);
        if (!wearer.world.isRemote && wearer instanceof EntityPlayer && wearer.ticksExisted % 10 == 0) {
            ((EntityPlayer) wearer).addExhaustion(0.1F);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        CurseTooltips.empty(tooltip);
        if (CurseTooltips.shift()) {
            trait(tooltip, percent(CurseConfig.gluttonyAttackDamage), "tooltip.enigmaticlegacy.berserk_emblem1");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.gluttonyCharm2", percent(CurseConfig.gluttonyCritModifier));
            trait(tooltip, percent(CurseConfig.gluttonyMovementSpeed), "tooltip.enigmaticlegacy.berserk_emblem3");
            trait(tooltip, percent(CurseConfig.gluttonyDamageResistance), "tooltip.enigmaticlegacy.berserk_emblem4");
            CurseTooltips.empty(tooltip);
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.gluttonyCharm5");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.gluttonyCharm6");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.gluttonyCharm7");
        } else {
            CurseTooltips.holdShift(tooltip);
        }
        EntityPlayer player = CurseTooltips.localPlayer();
        if (player != null && EnigmaticBridge.hasBauble(player, this)) {
            int percentage = (int) (GluttonyLogic.missingFoodProperty(player) * 100.0F);
            boolean hellBlade = GluttonyLogic.hasHellBladeCharm(player);
            CurseTooltips.empty(tooltip);
            CurseTooltips.line(tooltip, "tooltip.enigmaticlegacy.berserk_emblem7");
            trait(tooltip, String.format("%.1f%%", percentage * CurseConfig.gluttonyAttackDamage * (hellBlade ? 1.2 : 1.0)),
                    "tooltip.enigmaticlegacy.berserk_emblem8");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.gluttonyCharm2", String.format("%.1f%%", percentage * CurseConfig.gluttonyCritModifier));
            trait(tooltip, String.format("%.1f%%", percentage * CurseConfig.gluttonyMovementSpeed), "tooltip.enigmaticlegacy.berserk_emblem10");
            trait(tooltip, String.format("%.1f%%", percentage * CurseConfig.gluttonyDamageResistance * (hellBlade ? 0.6 : 1.0)),
                    "tooltip.enigmaticlegacy.berserk_emblem11");
        }
    }

    /** 配置存的是比例（0.4 即 40%），提示按百分数显示 */
    private static String percent(double ratio) {
        double value = ratio * 100.0D;
        return (value == Math.rint(value) ? String.valueOf((long) value) : String.format("%.1f", value)) + "%";
    }

    /** EL 1.12 染血勇气徽记的提示写法：§6+数值 后接属性名 */
    @SideOnly(Side.CLIENT)
    private static void trait(List<String> tooltip, String value, String nameKey) {
        tooltip.add(TextFormatting.GOLD + I18n.format("tooltip.enigmaticlegacy.add") + value + I18n.format(nameKey));
    }
}
