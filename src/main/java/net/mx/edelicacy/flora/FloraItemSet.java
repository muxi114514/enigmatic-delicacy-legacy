package net.mx.edelicacy.flora;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemSlab;
import net.mx.edelicacy.flora.crop.EnigmaticFruitItem;
import net.mx.edelicacy.flora.crop.EnigmaticSeedItem;
import net.mx.edelicacy.item.base.DelicacyItem;

/**
 * 本模块的全部物品（含方块物品），注册名与 registry/DelicacyItems 一致。
 * 没有物品的方块：神秘灌木、盆栽树苗、悬挂星辰果、双层台阶；门用 ItemDoor 单独注册。
 */
public final class FloraItemSet {

    private final List<Item> all = new ArrayList<>();

    void create(FloraBlockSet b) {
        add(new EnigmaticSeedItem(b.enigmaticBush, b.infinisoilFarmland));
        add(new EnigmaticFruitItem(EnigmaticFruitItem.BASE_NAME, false));
        for (String color : EnigmaticFruitItem.COLORS) {
            add(new EnigmaticFruitItem(EnigmaticFruitItem.BASE_NAME + "_" + color, true));
        }
        block(b.fruitCrate);
        block(b.infinisoil);
        block(b.infinisoilFarmland);
        add(new FloraBlockItem(b.sapling, EnumRarity.UNCOMMON));
        block(b.astralLog);
        block(b.astralWood);
        block(b.leaves);
        block(b.blossomingLeaves);
        block(b.strippedLog);
        block(b.strippedWood);
        block(b.planks);
        block(b.stairs);
        ItemSlab slabItem = new ItemSlab(b.slab, b.slab, b.doubleSlab);
        slabItem.setRegistryName(b.slab.getRegistryName());
        add(slabItem);
        add(DelicacyItem.setup(new ItemDoor(b.door), "astral_door"));
        block(b.trapdoor);
        block(b.pressurePlate);
        block(b.button);
        block(b.cabinet);
        add(new DelicacyItem("astral_leaf"));
        add(new DelicacyItem("astral_petal"));
        add(new DelicacyItem("glistening_bark"));
        add(new DelicacyItem("glistening_powder"));
        block(b.fadingGlass);
    }

    private void block(Block block) {
        add(DelicacyItem.blockItem(block));
    }

    private void add(Item item) {
        all.add(item);
    }

    List<Item> all() {
        return Collections.unmodifiableList(all);
    }
}
