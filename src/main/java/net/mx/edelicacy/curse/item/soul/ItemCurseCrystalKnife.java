package net.mx.edelicacy.curse.item.soul;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.wdcftgg.farmersdelightlegacy.api.knife.ItemKnifeBase;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.curse.item.CurseMaterials;
import net.mx.edelicacy.curse.util.CurseItemEntities;
import net.mx.edelicacy.curse.util.LootDrops;
import net.mx.edelicacy.item.base.DelicacyItem;

/**
 * 咒魂晶刃：攻击力 = 8 + 平均每次击杀的咒魂值（精确到 0.1），攻速随之提高；命中叠加生命诅咒（见 {@link CurseCrystalKnifeHandler}）。
 * 右键成年的可繁殖生物：使其变回幼体并掉落一次战利品（1.21 青蛙变蝌蚪的分支已删除）。
 * 1.21 用砂轮清空咒魂值，这里改为工作台单独放入晶刃的重置配方（{@link CrystalKnifeResetRecipe}）。
 */
public class ItemCurseCrystalKnife extends ItemKnifeBase {

    public ItemCurseCrystalKnife() {
        super(CurseMaterials.CURSE_CRYSTAL, 7.0D, -2.2D);
        DelicacyItem.setup(this, "curse_crystal_knife");
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return CurseMaterials.isNetheriteScrap(repair);
    }

    /** 平均咒魂值带来的额外攻击力；1.21 击杀数为 0 时会除以零 */
    public static float damageBonus(ItemStack stack) {
        int kills = SoulNBT.getKillCount(stack);
        if (kills <= 0) {
            return 0.0F;
        }
        return 0.1F * MathHelper.floor(SoulNBT.getSoulAmount(stack) * 10.0F / kills);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> modifiers = super.getAttributeModifiers(slot, stack);
        if (slot == EntityEquipmentSlot.MAINHAND) {
            float bonus = damageBonus(stack);
            float speed = bonus > 10.0F ? bonus * 0.01F + 0.1F : bonus * 0.02F;
            modifiers.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
            modifiers.removeAll(SharedMonsterAttributes.ATTACK_SPEED.getName());
            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 7.0D + bonus, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.2D + speed, 0));
        }
        return modifiers;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isRemote) {
            CrystalKnifeResetRecipe.stripMarker(stack);
        }
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
        if (!(target instanceof EntityAgeable) || ((EntityAgeable) target).isChild() || player.getCooldownTracker().hasCooldown(this)) {
            return false;
        }
        if (target.world instanceof WorldServer) {
            player.getCooldownTracker().setCooldown(this, CurseConfig.crystalKnifeCooldown);
            ((WorldServer) target.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + target.height / 2.0D,
                    target.posZ, 24, target.width / 2.0D, target.height / 2.0D, target.width / 2.0D, 0.0D);
            ((EntityAgeable) target).setGrowingAge(-24000);
            LootDrops.dropAsPlayerKill(target, player);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (CurseTooltips.shift()) {
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseCrystalKnife1", CurseConfig.crystalKnifeProbability + "%");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseCrystalKnife2");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseCrystalKnife3");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseCrystalKnife4");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseCrystalKnife5");
        } else {
            CurseTooltips.holdShift(tooltip);
        }
        CurseTooltips.empty(tooltip);
        CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseSoulAmount", SoulNBT.getSoulAmount(stack));
        CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseCrystalKnifeCount", SoulNBT.getKillCount(stack));
        if (stack.isItemEnchanted()) {
            CurseTooltips.empty(tooltip);
        }
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
