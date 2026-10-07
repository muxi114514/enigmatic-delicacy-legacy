package net.mx.edelicacy.curse.item.forbidden;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;

import baubles.api.BaubleType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.item.base.DelicacyBaubleItem;
import net.mx.edelicacy.util.EnigmaticBridge;
import net.mx.edelicacy.util.StackNBT;

/**
 * 禁忌回响之符（护符，仅禁忌之人可佩戴）：+0.1 生命窃取；生命值变化累积伤害加成，下一击打出后保留 40%。
 * <p>1.21 只在佩戴时记录一次生命值，之后每 tick 都按「与佩戴时的差值」累加，加成会无限暴涨；
 * 这里改为每 tick 更新记录，累计变化量每满 1/0.12 点生命换 10 点伤害加成。
 * 其余效果（负面药水转治疗、加速进食）见 {@link ForbiddenCharmHandler}。
 */
public class ItemForbiddenCharm extends DelicacyBaubleItem implements IForbiddenItem {

    public static final String HEALTH_RECORD = "HealthRecord";
    public static final String FORBIDDEN_BOOST = "ForbiddenBoost";
    public static final String HEALTH_CHANGE = "HealthChange";
    private static final UUID LIFESTEAL_ID = UUID.fromString("5d2c7a91-3e4b-4f8d-b6a2-9c0e1f7d3b58");
    /** 每 1/0.12 点生命变化给 10 点伤害加成（1.21 的 floor(变化 × 0.12) × 10） */
    private static final float HEALTH_PER_STEP = 1.0F / 0.12F;

    public ItemForbiddenCharm() {
        super("forbidden_charm", BaubleType.CHARM);
        rarity(EnumRarity.RARE);
    }

    public static float getBoost(ItemStack stack) {
        return StackNBT.getFloat(stack, FORBIDDEN_BOOST, 0.0F);
    }

    public static void setBoost(ItemStack stack, float boost) {
        StackNBT.setFloat(stack, FORBIDDEN_BOOST, boost);
    }

    @Override
    protected void fillModifiers(Multimap<String, AttributeModifier> modifiers, ItemStack stack, EntityLivingBase wearer) {
        modifiers.put("eaddons.lifesteal", new AttributeModifier(LIFESTEAL_ID, "enigmaticdelicacy:forbidden_charm", 0.1D, 0));
    }

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase wearer) {
        return wearer instanceof EntityPlayer && EnigmaticBridge.isForbidden((EntityPlayer) wearer) && super.canEquip(stack, wearer);
    }

    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase wearer) {
        super.onEquipped(stack, wearer);
        StackNBT.setFloat(stack, HEALTH_RECORD, wearer.getHealth());
    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase wearer) {
        super.onUnequipped(stack, wearer);
        StackNBT.remove(stack, HEALTH_RECORD);
        StackNBT.remove(stack, HEALTH_CHANGE);
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase wearer) {
        super.onWornTick(stack, wearer);
        if (wearer.world.isRemote) {
            return;
        }
        float health = wearer.getHealth();
        float record = StackNBT.getFloat(stack, HEALTH_RECORD, health);
        float delta = Math.abs(record - health);
        if (delta > 0.01F) {
            float change = StackNBT.getFloat(stack, HEALTH_CHANGE, 0.0F) + delta;
            int steps = (int) (change / HEALTH_PER_STEP);
            if (steps > 0) {
                setBoost(stack, getBoost(stack) + steps * 10.0F);
                change -= steps * HEALTH_PER_STEP;
            }
            StackNBT.setFloat(stack, HEALTH_CHANGE, change);
            StackNBT.setFloat(stack, HEALTH_RECORD, health);
        } else if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(HEALTH_RECORD)) {
            StackNBT.setFloat(stack, HEALTH_RECORD, health);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        CurseTooltips.empty(tooltip);
        if (CurseTooltips.shift()) {
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenCharm1");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenCharm2");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenCharm3");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenCharm4");
        } else {
            CurseTooltips.holdShift(tooltip);
        }
        CurseTooltips.empty(tooltip);
        CurseTooltips.forbiddenOnly(tooltip);
    }
}
