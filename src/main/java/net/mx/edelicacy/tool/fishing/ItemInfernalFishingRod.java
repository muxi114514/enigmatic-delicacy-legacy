package net.mx.edelicacy.tool.fishing;

import javax.annotation.Nullable;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.tool.IndestructibleDrops;

/**
 * 熔岩钓竿：在熔岩里钓鱼。继承原版钓竿以接受饵钓 / 海之眷顾附魔，但不用玩家的 fishEntity：
 * 服务端浮漂 UUID 记在 DelicacyData，客户端按浮漂实体自带的主人判断「已抛竿」模型（其他玩家手里的钓竿也正确）。
 */
public class ItemInfernalFishingRod extends ItemFishingRod {

    public ItemInfernalFishingRod() {
        DelicacyItem.setup(this, "infernal_fishing_rod");
        setMaxDamage(972);
        // 覆盖原版的 cast 属性：原版看 fishEntity，这里看熔岩浮漂
        addPropertyOverride(new ResourceLocation("cast"), new IItemPropertyGetter() {
            @Override
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                if (!(entity instanceof EntityPlayer)) {
                    return 0.0F;
                }
                boolean main = entity.getHeldItemMainhand() == stack;
                boolean off = entity.getHeldItemOffhand() == stack && !(entity.getHeldItemMainhand().getItem() instanceof ItemInfernalFishingRod);
                return (main || off) && EntityInfernalHook.clientHasHook((EntityPlayer) entity) ? 1.0F : 0.0F;
            }
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            EntityInfernalHook hook = EntityInfernalHook.getActiveHook(player);
            if (hook != null) {
                int damage = hook.retrieve();
                stack.damageItem(damage, player);
                world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_BOBBER_RETRIEVE, SoundCategory.NEUTRAL,
                        1.0F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            } else {
                world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL,
                        0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                int lure = EnchantmentHelper.getFishingSpeedBonus(stack);
                int luck = EnchantmentHelper.getFishingLuckBonus(stack);
                world.spawnEntity(new EntityInfernalHook(world, player, luck, lure));
                player.addStat(StatList.getObjectUseStats(this));
            }
        }
        player.swingArm(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public int getItemEnchantability() {
        return 12;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(World world, Entity location, ItemStack stack) {
        return IndestructibleDrops.create(world, location, stack);
    }
}
