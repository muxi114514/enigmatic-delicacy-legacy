package net.mx.edelicacy.tool.machete;

import io.netty.buffer.ByteBuf;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

/**
 * 投出的灵体以太砍刀：只伤害一次，不可拾取，落地两 tick 后消散（与 1.21 相同）。
 * 伤害 = 投掷时玩家攻击力 + 刀上锋利等附魔对目标的加成；火焰附加、击退附魔照常生效。
 */
public class ThrownEtheriumMachete extends EntityArrow implements IEntityAdditionalSpawnData {

    /** 飞行最长存活时间，防止一直飞不落地 */
    private static final int MAX_LIFE = 600;

    private ItemStack weapon = ItemStack.EMPTY;
    private boolean dealtDamage;

    public ThrownEtheriumMachete(World world) {
        super(world);
        this.pickupStatus = PickupStatus.DISALLOWED;
        this.isImmuneToFire = true;
        setSize(0.4F, 0.4F);
    }

    public ThrownEtheriumMachete(World world, EntityLivingBase thrower, ItemStack weapon) {
        super(world, thrower);
        this.weapon = weapon.copy();
        this.pickupStatus = PickupStatus.DISALLOWED;
        this.isImmuneToFire = true;
        setSize(0.4F, 0.4F);
    }

    /** 渲染用 */
    public ItemStack getWeapon() {
        return weapon;
    }

    @Override
    protected ItemStack getArrowStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public void onUpdate() {
        if (timeInGround > 4 || ticksExisted > MAX_LIFE) {
            dealtDamage = true;
        }
        if (inGround) {
            if (world.isRemote) {
                for (int i = 0; i < 5; i++) {
                    double theta = rand.nextDouble() * 2.0D * Math.PI;
                    double phi = (rand.nextDouble() - 0.5D) * Math.PI;
                    world.spawnParticle(EnumParticleTypes.END_ROD, posX, posY, posZ,
                            Math.cos(theta) * Math.cos(phi) * 0.1D, Math.sin(phi) * 0.1D, Math.sin(theta) * Math.cos(phi) * 0.1D);
                }
            }
            if (timeInGround > 1) {
                setDead();
                return;
            }
        } else if (ticksExisted > MAX_LIFE) {
            setDead();
            return;
        }
        // 1.21 的飞行手感：上升时减速更快，下落时略微加速
        motionX *= 0.95D;
        motionY = motionY > 0 ? motionY * 0.9D - 0.01D : motionY * 1.05D + 0.02D;
        motionZ *= 0.95D;
        super.onUpdate();
    }

    @Override
    protected Entity findEntityOnPath(Vec3d start, Vec3d end) {
        return dealtDamage ? null : super.findEntityOnPath(start, end);
    }

    @Override
    protected void onHit(RayTraceResult result) {
        Entity target = result.entityHit;
        if (target == null) {
            super.onHit(result);
            return;
        }
        dealtDamage = true;
        if (!world.isRemote) {
            hitEntity(target);
        }
        motionX *= -0.01D;
        motionY *= -0.1D;
        motionZ *= -0.01D;
        playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.0F);
    }

    private void hitEntity(Entity target) {
        float damage = (float) getDamage();
        if (target instanceof EntityLivingBase) {
            damage += EnchantmentHelper.getModifierForCreature(weapon, ((EntityLivingBase) target).getCreatureAttribute());
        }
        Entity owner = shootingEntity;
        DamageSource source = DamageSource.causeThrownDamage(this, owner == null ? this : owner);
        ((WorldServer) world).spawnParticle(EnumParticleTypes.END_ROD, posX, posY, posZ, 4, 0.0D, 0.0D, 0.0D, 0.04D);
        int fire = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, weapon);
        if (fire > 0 && !(target instanceof EntityEnderman)) {
            target.setFire(fire * 4);
        }
        if (!target.attackEntityFrom(source, damage) || target instanceof EntityEnderman || !(target instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase living = (EntityLivingBase) target;
        int knockback = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, weapon);
        float horizontal = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        if (knockback > 0 && horizontal > 0.0F) {
            living.addVelocity(motionX * knockback * 0.6D / horizontal, 0.1D, motionZ * knockback * 0.6D / horizontal);
        }
        if (owner instanceof EntityLivingBase) {
            EnchantmentHelper.applyThornEnchantments(living, owner);
            EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) owner, living);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("DealtDamage", dealtDamage);
        compound.setTag("Machete", weapon.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        dealtDamage = compound.getBoolean("DealtDamage");
        weapon = compound.hasKey("Machete", Constants.NBT.TAG_COMPOUND) ? new ItemStack(compound.getCompoundTag("Machete")) : ItemStack.EMPTY;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeItemStack(buffer, weapon);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        weapon = ByteBufUtils.readItemStack(buffer);
    }
}
