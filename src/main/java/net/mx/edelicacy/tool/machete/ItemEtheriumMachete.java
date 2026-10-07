package net.mx.edelicacy.tool.machete;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.wdcftgg.farmersdelightlegacy.api.knife.ItemKnifeBase;
import keletu.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.eaddons.attribute.EnigmaticAttributes;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.tool.IndestructibleDrops;
import net.mx.edelicacy.tool.ToolConfig;

/**
 * 以太砍刀：农夫乐事刀具（矿辞 toolKnife），以太材质，主手 +4% 以太护盾阈值。
 * <p>1.12 没有 EL+ 的「以太共鸣」附魔，改为始终可以右键投掷灵体砍刀（有冷却），伤害取投掷时玩家的攻击力
 * （修正 1.21 写成「攻击力 − 攻击力 = 0」的问题）。
 */
public class ItemEtheriumMachete extends ItemKnifeBase {

    /** 0.5 + 以太材质 5.0（与 1.21 一致，总伤害 6.5） */
    private static final double ATTACK_DAMAGE = 5.5D;
    private static final double ATTACK_SPEED = -2.2D;
    private static final double SHIELD_THRESHOLD = 0.04D;
    private static final UUID SHIELD_UUID = UUID.fromString("5d2c9c51-0a6e-4d37-9f0e-6b1e2d7a3c41");

    public ItemEtheriumMachete() {
        super(EnigmaticLegacy.ETHERIUM, ATTACK_DAMAGE, ATTACK_SPEED);
        DelicacyItem.setup(this, "etherium_machete");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ItemStack offhand = player.getHeldItemOffhand();
        if (hand != EnumHand.MAIN_HAND || (!offhand.isEmpty() && offhand.getItemUseAction() != EnumAction.NONE)
                || player.getCooldownTracker().hasCooldown(this)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (!world.isRemote) {
            ThrownEtheriumMachete thrown = new ThrownEtheriumMachete(world, player, stack);
            IAttributeInstance attack = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
            thrown.setDamage(attack == null ? ATTACK_DAMAGE + 1.0D : attack.getAttributeValue());
            thrown.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 2.5F, 1.0F);
            world.spawnEntity(thrown);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                    SoundCategory.PLAYERS, 1.0F, 2.0F);
        }
        player.getCooldownTracker().setCooldown(this, ToolConfig.macheteCooldown);
        stack.damageItem(2, player);
        player.addStat(StatList.getObjectUseStats(this));
        player.swingArm(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> map = HashMultimap.create(super.getItemAttributeModifiers(slot));
        if (slot == EntityEquipmentSlot.MAINHAND) {
            map.put(EnigmaticAttributes.ETHERIUM_SHIELD.getName(), new AttributeModifier(SHIELD_UUID, "Etherium machete", SHIELD_THRESHOLD, 0));
        }
        return map;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == EnigmaticLegacy.etheriumIngot || super.getIsRepairable(toRepair, repair);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    /** 掉落物不怕火与岩浆（1.21 的 fireResistant），沿用 EL 以太工具的不灭掉落物 */
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(World world, Entity location, ItemStack stack) {
        return IndestructibleDrops.create(world, location, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("tooltip.enigmaticdelicacy.etheriumMacheteBuff"));
    }
}
