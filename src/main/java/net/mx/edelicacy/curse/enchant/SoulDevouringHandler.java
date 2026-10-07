package net.mx.edelicacy.curse.enchant;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.registry.DelicacyEnchantments;

/**
 * 噬魂诅咒的代价：受到的伤害每级 +0.5（护甲前）。
 * <p>等级取装备栏与饰品栏中的最高值：1.21 只看装备栏，但可附魔的晶坠是饰品，只看装备栏会让它的诅咒没有代价。
 */
public class SoulDevouringHandler {

    public static int level(ItemStack stack) {
        Enchantment enchantment = DelicacyEnchantments.SOUL_DEVOURING_CURSE;
        return enchantment == null || stack.isEmpty() ? 0 : EnchantmentHelper.getEnchantmentLevel(enchantment, stack);
    }

    public static int level(EntityLivingBase entity) {
        Enchantment enchantment = DelicacyEnchantments.SOUL_DEVOURING_CURSE;
        if (enchantment == null) {
            return 0;
        }
        int level = EnchantmentHelper.getMaxEnchantmentLevel(enchantment, entity);
        if (entity instanceof EntityPlayer) {
            IBaublesItemHandler baubles = BaublesApi.getBaublesHandler((EntityPlayer) entity);
            for (int i = 0; i < baubles.getSlots(); i++) {
                level = Math.max(level, level(baubles.getStackInSlot(i)));
            }
        }
        return level;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onHurt(LivingHurtEvent event) {
        int level = level(event.getEntityLiving());
        if (level > 0) {
            event.setAmount(event.getAmount() + level * 0.5F);
        }
    }
}
