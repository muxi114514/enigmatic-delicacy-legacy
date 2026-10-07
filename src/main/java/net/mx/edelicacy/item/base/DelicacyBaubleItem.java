package net.mx.edelicacy.item.base;

import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * 饰品基类（对应 EL+ 的 BaseCurioItem）：同一件不能重复佩戴；
 * 子类在 {@link #fillModifiers} 里给出属性修饰符，佩戴时挂上、卸下时移除。
 * 1.21 的 scroll 槽对应 Baubles 的 TRINKET（与 EL 的卷轴一致）。
 */
public class DelicacyBaubleItem extends DelicacyItem implements IBauble {

    private final BaubleType type;

    public DelicacyBaubleItem(String name, BaubleType type) {
        super(name);
        this.type = type;
        setMaxStackSize(1);
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return type;
    }

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase wearer) {
        return !(wearer instanceof EntityPlayer) || BaublesApi.isBaubleEquipped((EntityPlayer) wearer, this) == -1;
    }

    /** 佩戴期间的属性修饰符（键为属性名） */
    protected void fillModifiers(Multimap<String, AttributeModifier> modifiers, ItemStack stack, EntityLivingBase wearer) {
    }

    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase wearer) {
        applyModifiers(stack, wearer);
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase wearer) {
        applyModifiers(stack, wearer);
    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase wearer) {
        if (!wearer.world.isRemote) {
            Multimap<String, AttributeModifier> modifiers = HashMultimap.create();
            fillModifiers(modifiers, stack, wearer);
            wearer.getAttributeMap().removeAttributeModifiers(modifiers);
        }
    }

    /** 已挂好的不重复挂，避免每 tick 标脏属性 */
    private void applyModifiers(ItemStack stack, EntityLivingBase wearer) {
        if (wearer.world.isRemote) {
            return;
        }
        Multimap<String, AttributeModifier> modifiers = HashMultimap.create();
        fillModifiers(modifiers, stack, wearer);
        if (modifiers.isEmpty()) {
            return;
        }
        for (Map.Entry<String, AttributeModifier> entry : modifiers.entries()) {
            IAttributeInstance instance = wearer.getAttributeMap().getAttributeInstanceByName(entry.getKey());
            if (instance != null) {
                AttributeModifier current = instance.getModifier(entry.getValue().getID());
                if (current == null || current.getAmount() != entry.getValue().getAmount()
                        || current.getOperation() != entry.getValue().getOperation()) {
                    if (current != null) {
                        instance.removeModifier(current);
                    }
                    instance.applyModifier(entry.getValue());
                }
            }
        }
    }
}
