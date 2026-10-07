package net.mx.edelicacy.tool;

import keletu.enigmaticlegacy.entity.EntityItemIndestructible;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** 1.21 的 fireResistant 物品：掉落时换成 EL 的不灭掉落物（不怕火、岩浆与爆炸），与 EL 以太工具一致 */
public final class IndestructibleDrops {

    private IndestructibleDrops() {
    }

    public static Entity create(World world, Entity location, ItemStack stack) {
        EntityItemIndestructible item = new EntityItemIndestructible(world, location.posX, location.posY, location.posZ, stack);
        item.setDefaultPickupDelay();
        item.motionX = location.motionX;
        item.motionY = location.motionY;
        item.motionZ = location.motionZ;
        if (location instanceof EntityItem) {
            item.setThrower(((EntityItem) location).getThrower());
            item.setOwner(((EntityItem) location).getOwner());
        }
        return item;
    }
}
