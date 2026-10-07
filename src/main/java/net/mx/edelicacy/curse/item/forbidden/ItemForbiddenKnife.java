package net.mx.edelicacy.curse.item.forbidden;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.wdcftgg.farmersdelightlegacy.api.knife.ItemKnifeBase;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.curse.item.CurseMaterials;
import net.mx.edelicacy.curse.util.CurseItemEntities;
import net.mx.edelicacy.item.base.DelicacyItem;

/**
 * 禁忌之刃：主手 +0.1 生命窃取（神遗拓展属性）；命中附加禁忌之印、对带印目标增伤、击杀时概率禁忌转化掉落物（见 {@link ForbiddenKnifeHandler}）。
 * 也是混沌派唯一能切的刀。1.21 只在提示里写「仅禁忌之人可用」，代码并不限制，这里同样不限制。
 */
public class ItemForbiddenKnife extends ItemKnifeBase implements IForbiddenItem {

    private static final UUID LIFESTEAL_ID = UUID.fromString("0f3f8a2c-9b1d-4c6e-8a7f-2d5e9c1b4a60");

    public ItemForbiddenKnife() {
        super(CurseMaterials.FORBIDDEN, 6.0D, -2.2D);
        DelicacyItem.setup(this, "forbidden_knife");
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return CurseMaterials.isForbiddenMeat(repair);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> modifiers = super.getItemAttributeModifiers(slot);
        if (slot == EntityEquipmentSlot.MAINHAND) {
            modifiers.put("eaddons.lifesteal", new AttributeModifier(LIFESTEAL_ID, "enigmaticdelicacy:forbidden_knife", 0.1D, 0));
        }
        return modifiers;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (CurseTooltips.shift()) {
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenKnife1");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenKnife2");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenKnife3");
        } else {
            CurseTooltips.holdShift(tooltip);
        }
        CurseTooltips.empty(tooltip);
        CurseTooltips.forbiddenOnly(tooltip);
        CurseTooltips.empty(tooltip);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    @Nullable
    public Entity createEntity(World world, Entity location, ItemStack stack) {
        return CurseItemEntities.fireproof(world, location, stack);
    }
}
