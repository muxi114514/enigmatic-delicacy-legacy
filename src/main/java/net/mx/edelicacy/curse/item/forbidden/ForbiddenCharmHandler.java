package net.mx.edelicacy.curse.item.forbidden;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 禁忌回响之符：
 * <ul>
 *   <li>负面药水在施加前就被拒绝（无缝免疫），并按等级回复生命</li>
 *   <li>获得任何药水效果时回复生命</li>
 *   <li>攻击时打出累积的伤害加成，剩 40%</li>
 *   <li>进食 / 饮用快 12 tick（至少 8 tick）</li>
 * </ul>
 * 治疗量取 1.21 经禁忌之果 -80% 削减后的实际值（每级 1.6 / 1.0），因为 EL 1.12 不削减大于 1 点的治疗。
 */
public class ForbiddenCharmHandler {

    private static ItemStack charm(EntityLivingBase entity) {
        return entity instanceof EntityPlayer ? EnigmaticBridge.getBauble((EntityPlayer) entity, DelicacyItems.FORBIDDEN_CHARM) : ItemStack.EMPTY;
    }

    /** 食物或吃 / 喝动作的物品 */
    public static boolean isConsumable(ItemStack stack) {
        EnumAction action = stack.getItemUseAction();
        return stack.getItem() instanceof ItemFood || action == EnumAction.EAT || action == EnumAction.DRINK;
    }

    @SubscribeEvent
    public void onApplicable(PotionEvent.PotionApplicableEvent event) {
        PotionEffect effect = event.getPotionEffect();
        if (effect.getPotion().isBadEffect() && !charm(event.getEntityLiving()).isEmpty()) {
            if (!event.getEntityLiving().world.isRemote) {
                event.getEntityLiving().heal((effect.getAmplifier() + 1) * 1.6F);
            }
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onAdded(PotionEvent.PotionAddedEvent event) {
        if (!event.getEntityLiving().world.isRemote && !charm(event.getEntityLiving()).isEmpty()) {
            event.getEntityLiving().heal((event.getPotionEffect().getAmplifier() + 1) * 1.0F);
        }
    }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityLivingBase)) {
            return;
        }
        ItemStack stack = charm((EntityLivingBase) event.getSource().getTrueSource());
        float boost = stack.isEmpty() ? 0.0F : ItemForbiddenCharm.getBoost(stack);
        if (boost > 0.0F) {
            event.setAmount(event.getAmount() + boost);
            ItemForbiddenCharm.setBoost(stack, boost * 0.4F);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (isConsumable(event.getItem()) && !charm(event.getEntityLiving()).isEmpty()) {
            event.setDuration(Math.max(event.getDuration() - 12, 8));
        }
    }
}
