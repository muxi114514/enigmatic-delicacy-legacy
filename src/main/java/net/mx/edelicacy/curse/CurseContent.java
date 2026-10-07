package net.mx.edelicacy.curse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import net.mx.edelicacy.curse.block.BlockChaoticPie;
import net.mx.edelicacy.curse.block.BlockDivineFruitPie;
import net.mx.edelicacy.curse.block.ItemCursePieBlock;
import net.mx.edelicacy.curse.item.abyss.ItemAbyssalStew;
import net.mx.edelicacy.curse.item.blade.ItemCurseBlade;
import net.mx.edelicacy.curse.item.divine.ItemDivineFruitPie;
import net.mx.edelicacy.curse.item.forbidden.ItemForbiddenCharm;
import net.mx.edelicacy.curse.item.forbidden.ItemForbiddenFood;
import net.mx.edelicacy.curse.item.forbidden.ItemForbiddenFruitSlice;
import net.mx.edelicacy.curse.item.forbidden.ItemForbiddenKnife;
import net.mx.edelicacy.curse.item.gluttony.ItemGluttonyCharm;
import net.mx.edelicacy.curse.item.grail.ItemForbiddenGrail;
import net.mx.edelicacy.curse.item.grail.ItemHarmoniousGrail;
import net.mx.edelicacy.curse.item.soul.ItemCurseCrystalKnife;
import net.mx.edelicacy.curse.item.soul.ItemCurseCrystalPendant;
import net.mx.edelicacy.curse.item.soul.ItemCursePotion;
import net.mx.edelicacy.curse.item.soul.ItemCursedSoulCrystal;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.item.base.DelicacyItem;

/** 本线的方块与物品实例（数值对照 1.21 的 DelicacyItems / EnigmaticFoods） */
final class CurseContent {

    private static Block chaoticPie;
    private static Block divinePie;
    private static final List<Item> ITEMS = new ArrayList<>();

    private CurseContent() {
    }

    static List<Item> items() {
        return Collections.unmodifiableList(ITEMS);
    }

    static void registerBlocks(IForgeRegistry<Block> registry) {
        chaoticPie = DelicacyItem.setup(new BlockChaoticPie(), "chaotic_pie");
        divinePie = DelicacyItem.setup(new BlockDivineFruitPie(), "divine_fruit_pie_block");
        registry.registerAll(chaoticPie, divinePie);
    }

    static void registerItems(IForgeRegistry<Item> registry) {
        ITEMS.clear();
        add(registry, new ItemCurseBlade());
        add(registry, new ItemCursedSoulCrystal("broken_cursed_soul_crystal", 1, true));
        add(registry, new ItemCursedSoulCrystal("cursed_soul_crystal", 1, false));
        add(registry, new ItemCursedSoulCrystal("cursed_soul_fragment", 16, false));
        add(registry, new ItemCursePotion());
        add(registry, new ItemCurseCrystalKnife());
        add(registry, new ItemCurseCrystalPendant());
        add(registry, new ItemForbiddenFruitSlice());
        add(registry, new ItemForbiddenFood("forbidden_meat", 2, 0.1F, 1.8F)
                .effect(() -> MobEffects.HUNGER, 600, 0));
        add(registry, new ItemForbiddenFood("forbidden_meat_on_a_stick", 2, 0.12F, 2.4F)
                .fast().container(Items.STICK)
                .effect(() -> CursePotions.FORBIDDEN_IMPRINT, 600, 1)
                .effect(() -> MobEffects.HUNGER, 600, 0));
        add(registry, new ItemForbiddenFood("chaotic_pie_slice", 3, 0.24F, 3.0F)
                .fast().alwaysEdible().rarity(EnumRarity.RARE)
                .effect(() -> CursePotions.FORBIDDEN_IMPRINT, 1000, 2)
                .effect(() -> MobEffects.STRENGTH, 1000, 1)
                .effect(() -> MobEffects.HUNGER, 1000, 0));
        add(registry, new ItemForbiddenKnife());
        add(registry, new ItemForbiddenCharm());
        add(registry, new ItemForbiddenGrail());
        add(registry, new ItemHarmoniousGrail());
        add(registry, new ItemDivineFruitPie());
        add(registry, new ItemGluttonyCharm());
        add(registry, new ItemAbyssalStew());
        add(registry, new ItemCursePieBlock.Chaotic(chaoticPie));
        add(registry, new ItemCursePieBlock.Divine(divinePie));
    }

    private static void add(IForgeRegistry<Item> registry, Item item) {
        registry.register(item);
        ITEMS.add(item);
    }
}
