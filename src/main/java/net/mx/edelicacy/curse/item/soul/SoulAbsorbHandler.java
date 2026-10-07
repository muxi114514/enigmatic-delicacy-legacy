package net.mx.edelicacy.curse.item.soul;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.curse.enchant.SoulDevouringHandler;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 玩家击杀时吸收灵魂（基础点数 = 死者最大生命 / 20，带噬魂诅咒 ×1.25）：
 * <ul>
 *   <li>主手咒魂晶刃：咒魂值增加、击杀数 +1</li>
 *   <li>佩戴咒魂晶坠：咒魂值增加，上限为自身生命上限 × 8</li>
 *   <li>一只手拿破碎的咒魂晶、另一只手拿诅咒刃片：按 死者最大生命 / (自身生命上限×8000)^0.25 灌注晶体，七咒之人 ×1.2</li>
 * </ul>
 */
public class SoulAbsorbHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        EntityLivingBase victim = event.getEntityLiving();
        if (CursePotions.HEALTH_CURSE != null) {
            victim.removePotionEffect(CursePotions.HEALTH_CURSE);
        }
        int point = MathHelper.floor(victim.getMaxHealth() / 20.0F);

        ItemStack weapon = player.getHeldItemMainhand();
        if (weapon.getItem() == DelicacyItems.CURSE_CRYSTAL_KNIFE) {
            SoulNBT.addKillCount(weapon);
            SoulNBT.setSoulAmount(weapon, SoulNBT.getSoulAmount(weapon) + boosted(point, weapon));
        }

        ItemStack pendant = EnigmaticBridge.getBauble(player, DelicacyItems.CURSE_CRYSTAL_PENDANT);
        if (!pendant.isEmpty()) {
            int threshold = (int) (player.getMaxHealth() * 8.0F);
            int amount = SoulNBT.getSoulAmount(pendant);
            if (amount < threshold) {
                SoulNBT.setSoulAmount(pendant, Math.min(amount + boosted(point, pendant), threshold));
            }
        }

        ItemStack crystal = brokenCrystalWithBlade(player);
        if (!crystal.isEmpty()) {
            float base = (float) Math.pow(player.getMaxHealth() * 8000.0F, 0.25D);
            int infused = MathHelper.floor(victim.getMaxHealth() / base * (EnigmaticBridge.isCursed(player) ? 1.2F : 1.0F));
            SoulNBT.setSoulAmount(crystal, SoulNBT.getSoulAmount(crystal) + infused);
        }
    }

    private static int boosted(int point, ItemStack stack) {
        return SoulDevouringHandler.level(stack) > 0 ? MathHelper.floor(point * 1.25F) : point;
    }

    private static ItemStack brokenCrystalWithBlade(EntityPlayer player) {
        ItemStack main = player.getHeldItemMainhand();
        ItemStack off = player.getHeldItemOffhand();
        if (main.getItem() == DelicacyItems.BROKEN_CURSED_SOUL_CRYSTAL && off.getItem() == DelicacyItems.CURSE_BLADE) {
            return main;
        }
        if (off.getItem() == DelicacyItems.BROKEN_CURSED_SOUL_CRYSTAL && main.getItem() == DelicacyItems.CURSE_BLADE) {
            return off;
        }
        return ItemStack.EMPTY;
    }
}
