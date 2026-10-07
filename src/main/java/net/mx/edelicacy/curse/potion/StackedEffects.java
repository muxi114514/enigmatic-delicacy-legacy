package net.mx.edelicacy.curse.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * 1.21 的「隐藏效果」：同一药水先给长时低级、再给短时高级时，高级结束后低级接着生效。1.12 同种药水只能有一个实例，
 * 这里把被顶掉的那个记进玩家实体数据，高级到期后补回剩余时长；该药水被移除（牛奶等）时一并清掉记录。
 * <p>只对玩家记录（只有圣杯类物品用到）；其他生物照常 addPotionEffect。
 */
public class StackedEffects {

    private static final String KEY = "EnigmaticDelicacyHiddenEffects";

    /** 按 1.21 的叠加语义施加效果 */
    public static void add(EntityLivingBase entity, PotionEffect effect) {
        if (entity.world.isRemote) {
            return;
        }
        PotionEffect existing = entity.getActivePotionEffect(effect.getPotion());
        if (entity instanceof EntityPlayer && existing != null) {
            if (effect.getAmplifier() > existing.getAmplifier() && existing.getDuration() > effect.getDuration()) {
                remember(entity, existing);
            } else if (effect.getAmplifier() < existing.getAmplifier() && effect.getDuration() > existing.getDuration()) {
                remember(entity, effect);
                return;
            }
        }
        entity.addPotionEffect(effect);
    }

    private static void remember(EntityLivingBase entity, PotionEffect effect) {
        if (effect.getPotion().getRegistryName() == null) {
            return;
        }
        NBTTagCompound data = entity.getEntityData();
        NBTTagList list = data.getTagList(KEY, Constants.NBT.TAG_COMPOUND);
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("Id", effect.getPotion().getRegistryName().toString());
        entry.setInteger("Amp", effect.getAmplifier());
        entry.setLong("End", entity.world.getTotalWorldTime() + effect.getDuration());
        entry.setBoolean("Ambient", effect.getIsAmbient());
        entry.setBoolean("Particles", effect.doesShowParticles());
        list.appendTag(entry);
        data.setTag(KEY, list);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote || !player.getEntityData().hasKey(KEY)) {
            return;
        }
        NBTTagList list = player.getEntityData().getTagList(KEY, Constants.NBT.TAG_COMPOUND);
        long now = player.world.getTotalWorldTime();
        NBTTagList kept = new NBTTagList();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            Potion potion = Potion.getPotionFromResourceLocation(entry.getString("Id"));
            long remaining = entry.getLong("End") - now;
            if (potion == null || remaining <= 0) {
                continue;
            }
            if (player.getActivePotionEffect(potion) == null) {
                player.addPotionEffect(new PotionEffect(potion, (int) Math.min(Integer.MAX_VALUE, remaining),
                        entry.getInteger("Amp"), entry.getBoolean("Ambient"), entry.getBoolean("Particles")));
            } else {
                kept.appendTag(entry);
            }
        }
        if (kept.hasNoTags()) {
            player.getEntityData().removeTag(KEY);
        } else if (kept.tagCount() != list.tagCount()) {
            player.getEntityData().setTag(KEY, kept);
        }
    }

    /** 药水真的被移除了（没被别的处理器取消），被它压住的隐藏效果也一并作废 */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRemove(PotionEvent.PotionRemoveEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote || event.getPotion() == null || event.getPotion().getRegistryName() == null
                || !entity.getEntityData().hasKey(KEY)) {
            return;
        }
        String id = event.getPotion().getRegistryName().toString();
        NBTTagList list = entity.getEntityData().getTagList(KEY, Constants.NBT.TAG_COMPOUND);
        NBTTagList kept = new NBTTagList();
        for (int i = 0; i < list.tagCount(); i++) {
            if (!id.equals(list.getCompoundTagAt(i).getString("Id"))) {
                kept.appendTag(list.getCompoundTagAt(i));
            }
        }
        if (kept.hasNoTags()) {
            entity.getEntityData().removeTag(KEY);
        } else {
            entity.getEntityData().setTag(KEY, kept);
        }
    }
}
