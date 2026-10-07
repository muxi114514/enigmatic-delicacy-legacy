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
import net.mx.edelicacy.charm.client.CharmTooltips;
import net.mx.edelicacy.item.base.DelicacyBaubleItem;
import net.mx.edelicacy.util.TooltipHelper;

/**
 * 星花护符（CHARM，幸运 +1）与安宁之戒（RING，幸运 +1.5）：都能延长天体安神茶的效果。
 * <p>各自的特殊效果在 SleepEvents（安宁之戒醒来回血）与 WP2 的星醉效果（星花护符免疫负面）里判定。
 */
public class LuckBaubleItem extends DelicacyBaubleItem {

    private final double luck;
    private final String effectKey;
    private final UUID modifierId;

    public LuckBaubleItem(String name, BaubleType type, double luck, String effectKey) {
        super(name, type);
        this.luck = luck;
        this.effectKey = effectKey;
        this.modifierId = UUID.nameUUIDFromBytes(("enigmaticdelicacy:" + name + "/luck").getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void fillModifiers(Multimap<String, AttributeModifier> modifiers, ItemStack stack, EntityLivingBase wearer) {
        modifiers.put(SharedMonsterAttributes.LUCK.getName(),
                new AttributeModifier(modifierId, "enigmaticdelicacy:" + getRegistryName().getResourcePath(), luck, 0));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        TooltipHelper.empty(tooltip);
        TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.astralTeaBoost");
        TooltipHelper.line(tooltip, effectKey);
        CharmTooltips.header(tooltip);
        CharmTooltips.attribute(tooltip, SharedMonsterAttributes.LUCK, 0, luck);
    }
}
