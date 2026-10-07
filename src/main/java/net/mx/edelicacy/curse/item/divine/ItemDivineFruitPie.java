package net.mx.edelicacy.curse.item.divine;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.eaddons.baubleslot.ElSlotUnlocks;
import net.mx.eaddons.baubleslot.SlotKind;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.curse.util.CurseItemEntities;
import net.mx.edelicacy.item.base.DelicacyFoodItem;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 神圣果派（诅咒 + 祝福物品：七咒 / 救赎之人可食，其他人一碰即死，见 {@link DivineFruitPieHandler}）。
 * 无限时长的抗性 II、再生、力量 III、生命提升 V；吃下获得天体果实的戒指槽与灵液瓶的项链槽（与 EL 共用次数），
 * 并且每人一次额外解锁一个护符槽；没有 BaubleVault / BaublesEX 时改为永久 +4 生命上限。潜行右键投掷。
 */
public class ItemDivineFruitPie extends DelicacyFoodItem {

    public static final String CHARM_FLAG = "EnigmaticDelicacyDivinePieCharm";
    public static final String HEALTH_FLAG = "EnigmaticDelicacyDivinePieHealth";

    public ItemDivineFruitPie() {
        super("divine_fruit_pie", 10, 1.6F);
        alwaysEdible();
        stackSize(16);
        container(Items.BOWL);
        rarity(EnumRarity.EPIC);
        effect(() -> ForgeRegistries.POTIONS.getValue(new ResourceLocation("farmersdelight", "nourishment")), 600, 0);
        effect(() -> MobEffects.FIRE_RESISTANCE, 2400, 0);
        effect(() -> MobEffects.RESISTANCE, INFINITE, 1);
        effect(() -> MobEffects.REGENERATION, INFINITE, 0);
        effect(() -> MobEffects.STRENGTH, INFINITE, 2);
        effect(() -> MobEffects.HEALTH_BOOST, INFINITE, 4);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isSneaking()) {
            return super.onItemRightClick(world, player, hand);
        }
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS,
                0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
        if (!world.isRemote) {
            ItemStack thrown = stack.copy();
            thrown.setCount(1);
            EntityThrownDivineFruitPie pie = new EntityThrownDivineFruitPie(world, player, thrown);
            pie.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 0.5F, 0.0F);
            world.spawnEntity(pie);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    protected void onEaten(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote || !(player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 2.0F);
        if (!ElSlotUnlocks.slotUnlockAvailable()) {
            if (CurseConfig.divinePieFallbackHealth > 0 && !EnigmaticBridge.getPersistentBoolean(player, HEALTH_FLAG)) {
                EnigmaticBridge.setPersistentBoolean(player, HEALTH_FLAG, true);
            }
            return;
        }
        ElSlotUnlocks.grantAstralFruitSlot(serverPlayer);
        ElSlotUnlocks.grantIchorBottleSlot(serverPlayer);
        if (!EnigmaticBridge.getPersistentBoolean(player, CHARM_FLAG)
                && ElSlotUnlocks.unlockExtra(serverPlayer, SlotKind.CHARM, CurseConfig.divinePieCharmSlot)) {
            EnigmaticBridge.setPersistentBoolean(player, CHARM_FLAG, true);
        }
    }

    /** 没有开槽模组时由属性来源给的永久生命上限 */
    public static double fallbackHealth(EntityPlayer player) {
        return EnigmaticBridge.getPersistentBoolean(player, HEALTH_FLAG) ? CurseConfig.divinePieFallbackHealth : 0.0D;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (CurseTooltips.shift()) {
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.divineFruitPie1");
            if (ElSlotUnlocks.slotUnlockAvailable()) {
                CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.divineFruitPie2");
            } else {
                CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.divineFruitPieHealth", CurseConfig.divinePieFallbackHealth);
            }
            CurseTooltips.empty(tooltip);
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.divineFruitPie3");
            super.addInformation(stack, world, tooltip, flag);
        } else {
            CurseTooltips.holdShift(tooltip);
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
