package net.mx.edelicacy.curse.item.soul;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.StackNBT;

/**
 * 咒魂晶刃重置（替代 1.21 的砂轮）：工作台里只放一把有击杀记录的晶刃，产出清空咒魂值与击杀数的晶刃（附魔、耐久保留），
 * 取出时返还 咒魂值×7 的经验（砂轮的经验）。经验数先写在产物 NBT 里，合成事件里发放后删掉。
 */
public class CrystalKnifeResetRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    /** 产物上的临时经验标记；Shift 取出时合成事件拿到的是副本，残留的标记由晶刃的物品栏 tick 清掉 */
    public static final String RESET_XP = "EnigmaticDelicacyResetXp";

    public CrystalKnifeResetRecipe() {
        setRegistryName(EnigmaticDelicacy.MODID, "curse_crystal_knife_reset");
    }

    private static ItemStack findKnife(InventoryCrafting inv) {
        ItemStack found = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (!found.isEmpty() || stack.getItem() != DelicacyItems.CURSE_CRYSTAL_KNIFE) {
                return ItemStack.EMPTY;
            }
            found = stack;
        }
        return found;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        ItemStack knife = findKnife(inv);
        return !knife.isEmpty() && (SoulNBT.getKillCount(knife) > 0 || SoulNBT.getSoulAmount(knife) > 0);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack knife = findKnife(inv);
        if (knife.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = knife.copy();
        result.setCount(1);
        int xp = SoulNBT.getSoulAmount(knife) * 7;
        StackNBT.remove(result, RESET_XP);
        SoulNBT.clearSouls(result);
        if (xp > 0) {
            StackNBT.setInt(result, RESET_XP, xp);
        }
        return result;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return DelicacyItems.CURSE_CRYSTAL_KNIFE == null ? ItemStack.EMPTY : new ItemStack(DelicacyItems.CURSE_CRYSTAL_KNIFE);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    public static void stripMarker(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(RESET_XP)) {
            stack.getTagCompound().removeTag(RESET_XP);
            if (stack.getTagCompound().hasNoTags()) {
                stack.setTagCompound(null);
            }
        }
    }

    /** 取出产物时发放经验并删掉临时标记 */
    public static final class XpHandler {
        @SubscribeEvent
        public void onCrafted(PlayerEvent.ItemCraftedEvent event) {
            ItemStack crafted = event.crafting;
            int xp = StackNBT.getInt(crafted, RESET_XP, 0);
            if (xp <= 0) {
                return;
            }
            stripMarker(crafted);
            EntityPlayer player = event.player;
            if (!player.world.isRemote) {
                player.world.spawnEntity(new EntityXPOrb(player.world, player.posX, player.posY + 0.5D, player.posZ, xp));
            }
        }
    }
}
