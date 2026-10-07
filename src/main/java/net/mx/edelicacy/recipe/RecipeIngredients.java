package net.mx.edelicacy.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe.IngredientEntry;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

/**
 * 厨锅配方用到的「可替换原料」：1.21 的 tag 与 1.12 不存在的原版物品，按整合包里实际装了什么来选。
 * 规则：只依赖农夫乐事与原版；装了潘马斯等再额外认它们的矿辞。
 */
final class RecipeIngredients {

    private RecipeIngredients() {
    }

    @Nullable
    static Item item(String id) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return item == null || item == Items.AIR ? null : item;
    }

    /** 物品未注册（null）时返回 null，由调用方跳过该配方 */
    @Nullable
    static IngredientEntry item(@Nullable Item item) {
        return item == null ? null : IngredientEntry.forItem(item);
    }

    static IngredientEntry ore(String name) {
        return IngredientEntry.forOreDict(name);
    }

    static boolean oreHasItems(String name) {
        return OreDictionary.doesOreNameExist(name) && !OreDictionary.getOres(name, false).isEmpty();
    }

    /** 若干个具体物品 / 矿辞任选其一 */
    static IngredientEntry anyOf(List<ItemStack> stacks, String... ores) {
        List<ItemStack> display = new ArrayList<>(stacks);
        List<String> usedOres = new ArrayList<>();
        for (String ore : ores) {
            if (oreHasItems(ore)) {
                usedOres.add(ore);
                display.addAll(OreDictionary.getOres(ore, false));
            }
        }
        return IngredientEntry.forCustom(stack -> {
            for (ItemStack candidate : stacks) {
                if (OreDictionary.itemMatches(candidate, stack, false)) {
                    return true;
                }
            }
            for (String ore : usedOres) {
                for (ItemStack candidate : OreDictionary.getOres(ore, false)) {
                    if (OreDictionary.itemMatches(candidate, stack, false)) {
                        return true;
                    }
                }
            }
            return false;
        }, null, display);
    }

    /** 1.21 #c:foods/forbidden_fruit：EL 禁忌之果或禁忌果切片 */
    static IngredientEntry forbiddenFruit(Item slice) {
        List<ItemStack> stacks = new ArrayList<>();
        Item fruit = item("enigmaticlegacy:forbidden_fruit");
        if (fruit != null) {
            stacks.add(new ItemStack(fruit));
        }
        if (slice != null) {
            stacks.add(new ItemStack(slice));
        }
        return anyOf(stacks);
    }

    /** 1.21 #c:foods/cooked_beef：原版熟牛排，装了潘马斯再认 listAllbeefcooked */
    static IngredientEntry cookedBeef() {
        return anyOf(Collections.singletonList(new ItemStack(Items.COOKED_BEEF)), "listAllbeefcooked");
    }

    /** 1.13 紫水晶碎片：Deeper Depths 的 material:1，否则矿辞 gemAmethyst，都没有用石英 */
    static IngredientEntry amethyst() {
        Item deeper = item("deeperdepths:material");
        if (deeper != null) {
            return IngredientEntry.forItem(deeper, 1);
        }
        return oreHasItems("gemAmethyst") ? ore("gemAmethyst") : item(Items.QUARTZ);
    }

    /** 当前是否有蜂蜜可用（潘马斯 dropHoney 或 FutureMC 蜂蜜瓶） */
    static boolean honeyAvailable() {
        return oreHasItems("dropHoney") || item("futuremc:honey_bottle") != null;
    }

    static IngredientEntry honey() {
        List<ItemStack> stacks = new ArrayList<>();
        Item bottle = item("futuremc:honey_bottle");
        if (bottle != null) {
            stacks.add(new ItemStack(bottle));
        }
        return anyOf(stacks, "dropHoney");
    }

    /** 没有蜂蜜时照农夫乐事的做法用「水瓶 + 糖」代替；水瓶用完返还玻璃瓶 */
    static IngredientEntry waterBottle() {
        ItemStack display = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER);
        return IngredientEntry.forCustom(
                stack -> stack.getItem() == Items.POTIONITEM && PotionUtils.getPotionFromItem(stack) == PotionTypes.WATER,
                stack -> new ItemStack(Items.GLASS_BOTTLE),
                Collections.singletonList(display));
    }
}
