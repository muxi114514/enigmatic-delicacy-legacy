package net.mx.edelicacy.recipe;

import static net.mx.edelicacy.recipe.RecipeIngredients.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wdcftgg.farmersdelightlegacy.api.recipe.CuttingBoardRecipeApi;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe.IngredientEntry;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CookingPotRecipeManager;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.registry.DelicacyItems;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 1.21 的 16 条厨锅、11 条砧板配方改用农夫乐事 Legacy 的代码接口注册（1.21 的 JSON 格式不兼容）。
 * 在配方注册事件的默认优先级执行，早于 CraftTweaker 脚本，可用 mods.farmersdelight.* 增删。
 * 灵液蜜酿的配方按用户要求暂时删除（1.12 只有开槽版灵液瓶）。
 */
final class FarmersDelightRecipes {

    private static final Logger LOG = LogManager.getLogger(EnigmaticDelicacy.MODID);

    private FarmersDelightRecipes() {
    }

    static void registerAll() {
        registerCooking();
        registerCutting();
    }

    private static void registerCooking() {
        Item bowl = Items.BOWL;
        Item bottle = Items.GLASS_BOTTLE;
        Item el = null;

        cook("abyssal_stew", DelicacyItems.ABYSSAL_STEW, bowl, 1200, 50,
                itemOf("enigmaticlegacy:abyssal_heart"), itemOf("enigmaticlegacy:twisted_core"),
                itemOf("enigmaticlegacy:evil_essence"), item(Items.SUGAR), item(Items.REDSTONE));
        cook("astral_rice", DelicacyItems.ASTRAL_RICE, bowl, 400, 8,
                itemOf("farmersdelight:rice"), item(DelicacyItems.ASTRAL_PETAL), item(DelicacyItems.ASTRAL_PETAL),
                item(DelicacyItems.ASTRAL_LEAF), item(DelicacyItems.ASTRAL_LEAF), ore("listAllmilk"));
        cook("astral_tea", DelicacyItems.ASTRAL_TEA, bottle, 400, 8, withHoney(
                item(DelicacyItems.ASTRAL_LEAF), item(DelicacyItems.ASTRAL_PETAL), ore("listAllmilk"), item(Items.GHAST_TEAR)));
        cook("celestial_custard", DelicacyItems.CELESTIAL_CUSTARD, bottle, 400, 8,
                item(DelicacyItems.ENIGMATIC_CREAM), item(DelicacyItems.ENIGMATIC_FRUIT_MAGENTA),
                itemOf("enigmaticlegacy:astral_fruit"), item(Items.DRAGON_BREATH), ore("listAllmilk"), item(Items.SUGAR));
        cook("enigmatic_cream", DelicacyItems.ENIGMATIC_CREAM, bowl, 400, 5,
                item(DelicacyItems.ENIGMATIC_FRUIT), item(DelicacyItems.ENIGMATIC_FRUIT), item(Items.SLIME_BALL),
                item(Items.SUGAR), ore("listAllmilk"));
        cook("enigmatic_jam", DelicacyItems.ENIGMATIC_JAM, bottle, 480, 5, withHoney(
                item(DelicacyItems.ENIGMATIC_FRUIT), item(DelicacyItems.ENIGMATIC_FRUIT), item(Items.APPLE), item(Items.SUGAR)));
        cook("etherium_steak", DelicacyItems.ETHERIUM_STEAK, el, 600, 16,
                item(Items.COOKED_BEEF), itemOf("eaddons:etherium_nugget"), itemOf("eaddons:etherium_nugget"),
                itemOf("enigmaticlegacy:etherium_ingot"), itemOf("enigmaticlegacy:astral_dust"), item(Items.GLOWSTONE_DUST));
        cook("glistening_tea", DelicacyItems.GLISTENING_TEA, bottle, 400, 8,
                item(DelicacyItems.GLISTENING_POWDER), item(DelicacyItems.GLISTENING_POWDER), item(Items.GLOWSTONE_DUST),
                item(Items.GHAST_TEAR), item(Items.BLAZE_POWDER));
        cook("harmonious_grail", DelicacyItems.HARMONIOUS_GRAIL, item("enigmaticlegacy:unholy_grail"), 600, 64,
                forbiddenFruit(DelicacyItems.FORBIDDEN_FRUIT_SLICE), itemOf("eaddons:infernal_cinder"),
                itemOf("enigmaticlegacy:astral_dust"), itemOf("eaddons:ichor_droplet"));
        cook("ichoroot_soup", DelicacyItems.ICHOROOT_SOUP, bowl, 400, 8,
                itemOf("eaddons:ichoroot"), itemOf("eaddons:ichoroot"), itemOf("eaddons:ichoroot"), item(Items.GLOWSTONE_DUST));
        // 6 格已满：没有蜂蜜时只用糖代替
        cook("melted_amethyst_cheese", DelicacyItems.MELTED_AMETHYST_CHEESE, Items.BUCKET, 400, 8,
                item(DelicacyItems.ENIGMATIC_CREAM), item(DelicacyItems.ENIGMATIC_FRUIT_VIOLET), amethyst(),
                item(Items.CHORUS_FRUIT), ore("listAllmilk"), honeyAvailable() ? honey() : item(Items.SUGAR));
        cook("passion_fried_rice", DelicacyItems.PASSION_FRIED_RICE, bowl, 400, 8,
                item(DelicacyItems.ENIGMATIC_CREAM), item(DelicacyItems.ENIGMATIC_FRUIT_RED), ore("cropRice"),
                cookedBeef(), itemOf("eaddons:exterminato"), itemOf("farmersdelight:tomato_sauce"));
        cook("rice_cake_in_astral_leaf", DelicacyItems.RICE_CAKE_IN_ASTRAL_LEAF, DelicacyItems.ASTRAL_LEAF, 400, 8,
                itemOf("farmersdelight:wheat_dough"), item(DelicacyItems.ASTRAL_PETAL), item(DelicacyItems.ASTRAL_PETAL),
                itemOf("enigmaticlegacy:astral_dust"));
        cook("sparkling_pastry_plate", DelicacyItems.SPARKLING_PASTRY_PLATE, bowl, 400, 8,
                item(DelicacyItems.ENIGMATIC_CREAM), item(DelicacyItems.ENIGMATIC_FRUIT_AQUA), item(Items.PRISMARINE_CRYSTALS),
                ore("foodDough"), item(Items.SUGAR), item(Items.SNOWBALL));
        cook("redemption_potion", item("enigmaticlegacy:redemption_potion"), el, 600, 16, withHoney(
                forbiddenFruit(DelicacyItems.FORBIDDEN_FRUIT_SLICE), itemOf("eaddons:ichor_droplet"),
                item(Items.GLOWSTONE_DUST), item(Items.FERMENTED_SPIDER_EYE)));
    }

    private static void registerCutting() {
        String machete = "enigmaticdelicacy:etherium_machete";
        String executionAxe = "eaddons:execution_axe";
        cut("astral_fruit", "enigmaticlegacy:astral_fruit", machete, out("enigmaticdelicacy:astral_fruit_slice", 2, 1));
        cut("astral_leaf", "enigmaticdelicacy:astral_leaf", null, out("enigmaticlegacy:astral_dust", 1, 0.5F));
        cut("astral_leaves", "enigmaticdelicacy:astral_leaves", null,
                out("enigmaticdelicacy:astral_leaf", 2, 1), out("enigmaticdelicacy:astral_leaf", 2, 0.5F));
        cut("astral_log", "enigmaticdelicacy:astral_log", "ore:toolAxe",
                out("enigmaticdelicacy:stripped_astral_log", 1, 1), out("enigmaticdelicacy:glistening_bark", 1, 1));
        cut("astral_wood", "enigmaticdelicacy:astral_wood", "ore:toolAxe",
                out("enigmaticdelicacy:stripped_astral_wood", 1, 1), out("enigmaticdelicacy:glistening_bark", 1, 1));
        cut("blossoming_astral_leaves", "enigmaticdelicacy:blossoming_astral_leaves", null,
                out("enigmaticdelicacy:astral_leaf", 2, 1), out("enigmaticdelicacy:astral_petal", 1, 1),
                out("enigmaticdelicacy:astral_petal", 1, 0.6F));
        cut("cursed_soul_crystal", "enigmaticdelicacy:cursed_soul_crystal", executionAxe,
                out("enigmaticdelicacy:cursed_soul_fragment", 3, 1));
        cut("forbidden_fruit", "enigmaticlegacy:forbidden_fruit", "enigmaticdelicacy:forbidden_knife",
                out("enigmaticdelicacy:forbidden_fruit_slice", 2, 1));
        cut("glistening_bark", "enigmaticdelicacy:glistening_bark", null,
                out("enigmaticdelicacy:glistening_powder", 1, 1), out("enigmaticdelicacy:glistening_powder", 1, 0.5F));
        cut("eternal_cake", "eaddons:eternal_cake", machete,
                out("enigmaticlegacy:cosmic_heart", 1, 1), out("enigmaticdelicacy:cosmic_cake_slice", 7, 1));
        // 下界合金碎片只有装了 Netherized 才有
        if (item("netherized:netherite_scrap") != null) {
            cut("hell_blade_charm", "eaddons:hell_blade_charm", executionAxe, out("enigmaticdelicacy:curse_blade", 2, 1),
                    out("netherized:netherite_scrap", 1, 1), out("netherized:netherite_scrap", 1, 0.5F));
        } else {
            cut("hell_blade_charm", "eaddons:hell_blade_charm", executionAxe, out("enigmaticdelicacy:curse_blade", 2, 1));
        }
    }

    // ---------------- 厨锅 ----------------

    /** 原料里含 1.21 #c:drinks/honey 的配方：有蜂蜜用蜂蜜，否则「水瓶 + 糖」 */
    private static IngredientEntry[] withHoney(IngredientEntry... others) {
        List<IngredientEntry> list = new ArrayList<>(Arrays.asList(others));
        if (honeyAvailable()) {
            list.add(honey());
        } else {
            list.add(waterBottle());
            list.add(item(Items.SUGAR));
        }
        return list.toArray(new IngredientEntry[0]);
    }

    private static IngredientEntry itemOf(String id) {
        Item item = item(id);
        return item == null ? null : IngredientEntry.forItem(item);
    }

    /**
     * @param container 出锅需要的容器；null 表示不需要（显式声明，避免农夫乐事默认要碗）
     */
    private static void cook(String name, Item result, Item container, int time, float xp, IngredientEntry... ingredients) {
        if (result == null || Arrays.asList(ingredients).contains(null)) {
            LOG.warn("Skipping cooking pot recipe {}: missing item", name);
            return;
        }
        ItemStack containerStack = container == null ? ItemStack.EMPTY : new ItemStack(container);
        CookingPotRecipeManager.registerScriptRecipe(EnigmaticDelicacy.MODID + ":" + name, Arrays.asList(ingredients),
                new ItemStack(result), containerStack, time, xp, true);
    }

    // ---------------- 砧板 ----------------

    private static final class Output {
        final String id;
        final int count;
        final float chance;

        Output(String id, int count, float chance) {
            this.id = id;
            this.count = count;
            this.chance = chance;
        }
    }

    private static Output out(String id, int count, float chance) {
        return new Output(id, count, chance);
    }

    /** @param tool 工具令牌；null 用默认的刀 */
    private static void cut(String name, String input, String tool, Output... outputs) {
        String[] results = new String[outputs.length];
        int[] counts = new int[outputs.length];
        float[] chances = new float[outputs.length];
        for (int i = 0; i < outputs.length; i++) {
            results[i] = outputs[i].id;
            counts[i] = outputs[i].count;
            chances[i] = outputs[i].chance;
        }
        boolean ok = CuttingBoardRecipeApi.registerRecipe(EnigmaticDelicacy.MODID + ":" + name, new String[]{input},
                tool == null ? null : new String[]{tool}, results, counts, chances);
        if (!ok) {
            LOG.warn("Skipping cutting board recipe {}: invalid item or tool", name);
        }
    }
}
