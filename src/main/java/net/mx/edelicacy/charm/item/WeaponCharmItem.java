package net.mx.edelicacy.charm.item;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;

import baubles.api.BaubleType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.charm.CharmConfig;
import net.mx.edelicacy.charm.client.CharmTooltips;
import net.mx.edelicacy.item.base.DelicacyBaubleItem;
import net.mx.edelicacy.util.TooltipHelper;

/**
 * 武器大师之荣（CHARM）：攻击 +8%（配置，乘总值）；
 * 慢武器（攻速 < 1.5）暴击额外 +50%，快武器（攻速 > 1.75）近战命中附加虚弱（WeaponCharmEvents）。
 */
public class WeaponCharmItem extends DelicacyBaubleItem {

    private static final UUID DAMAGE_ID = UUID.nameUUIDFromBytes("enigmaticdelicacy:weapon_charm/damage".getBytes(StandardCharsets.UTF_8));

    public WeaponCharmItem() {
        super("weapon_charm", BaubleType.CHARM);
    }

    private static double bonus() {
        return CharmConfig.weaponCharmDamageModifier * 0.01D;
    }

    @Override
    protected void fillModifiers(Multimap<String, AttributeModifier> modifiers, ItemStack stack, EntityLivingBase wearer) {
        modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                new AttributeModifier(DAMAGE_ID, "enigmaticdelicacy:weapon_charm", bonus(), 2));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        TooltipHelper.empty(tooltip);
        if (TooltipHelper.shiftDown()) {
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.weaponCharm");
        } else {
            TooltipHelper.holdShift(tooltip);
        }
        CharmTooltips.header(tooltip);
        CharmTooltips.attribute(tooltip, SharedMonsterAttributes.ATTACK_DAMAGE, 2, bonus());
    }
}
