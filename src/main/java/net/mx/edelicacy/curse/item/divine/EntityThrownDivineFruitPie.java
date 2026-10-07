package net.mx.edelicacy.curse.item.divine;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 被投出的神圣果派：命中非七咒 / 救赎之人的生物直接将其杀死（派随之消失），命中其他目标或方块则掉落成物品
 * （投掷者为创造模式时不掉落）。
 */
public class EntityThrownDivineFruitPie extends EntityThrowable {

    private static final byte BURST_EVENT = 3;
    private ItemStack pie = ItemStack.EMPTY;

    public EntityThrownDivineFruitPie(World world) {
        super(world);
        setSize(0.4F, 0.4F);
        isImmuneToFire = true;
    }

    public EntityThrownDivineFruitPie(World world, EntityLivingBase thrower, ItemStack pie) {
        super(world, thrower);
        setSize(0.4F, 0.4F);
        isImmuneToFire = true;
        this.pie = pie;
    }

    private ItemStack pieStack() {
        if (pie.isEmpty() && DelicacyItems.DIVINE_FRUIT_PIE != null) {
            pie = new ItemStack(DelicacyItems.DIVINE_FRUIT_PIE);
        }
        return pie;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote && ticksExisted % 2 == 0) {
            world.spawnParticle(EnumParticleTypes.END_ROD, posX, posY, posZ,
                    (rand.nextFloat() - 0.5F) * 0.1D, (rand.nextFloat() - 0.5F) * 0.1D, (rand.nextFloat() - 0.5F) * 0.1D);
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (world.isRemote) {
            return;
        }
        if (result.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) result.entityHit;
            if (!(target instanceof EntityPlayer) || !EnigmaticBridge.isTheOne((EntityPlayer) target)) {
                world.setEntityState(this, BURST_EVENT);
                target.onKillCommand();
                setDead();
                return;
            }
        }
        dropPie();
        setDead();
    }

    private void dropPie() {
        world.setEntityState(this, BURST_EVENT);
        EntityLivingBase thrower = getThrower();
        if (thrower instanceof EntityPlayer && ((EntityPlayer) thrower).capabilities.isCreativeMode) {
            return;
        }
        ItemStack stack = pieStack();
        if (!stack.isEmpty()) {
            EntityItem item = new EntityItem(world, posX, posY, posZ, stack.copy());
            item.setDefaultPickupDelay();
            world.spawnEntity(item);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id != BURST_EVENT) {
            super.handleStatusUpdate(id);
            return;
        }
        for (int i = 0; i < 8; i++) {
            world.spawnParticle(EnumParticleTypes.END_ROD, posX, posY, posZ,
                    (rand.nextFloat() - 0.5F) * 0.2D, (rand.nextFloat() - 0.5F) * 0.2D, (rand.nextFloat() - 0.5F) * 0.2D);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (!pie.isEmpty()) {
            compound.setTag("Item", pie.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        pie = compound.hasKey("Item") ? new ItemStack(compound.getCompoundTag("Item")) : ItemStack.EMPTY;
    }
}
