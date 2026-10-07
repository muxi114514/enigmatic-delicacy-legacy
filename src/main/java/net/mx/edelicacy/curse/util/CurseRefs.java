package net.mx.edelicacy.curse.util;

import keletu.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.item.Item;

/**
 * 本线用到的外部物品：神秘遗物 1.12 的直接取静态字段，神遗拓展 / 下界化等按注册名惰性查找。
 */
public final class CurseRefs {

    public static final RegistryItem HELL_BLADE_CHARM = new RegistryItem("eaddons:hell_blade_charm");
    public static final RegistryItem INFERNAL_CINDER = new RegistryItem("eaddons:infernal_cinder");
    public static final RegistryItem CRIMSON_FUNGUS = new RegistryItem("netherized:crimson_fungus");
    public static final RegistryItem HUNGRY_CHARM = new RegistryItem("enigmaticdelicacy:hungry_charm");

    private CurseRefs() {
    }

    public static Item cursedRing() {
        return EnigmaticLegacy.cursedRing;
    }

    public static Item abyssalHeart() {
        return EnigmaticLegacy.abyssalHeart;
    }

    public static Item unholyGrail() {
        return EnigmaticLegacy.unholyGrail;
    }

    public static Item enigmaticAmulet() {
        return EnigmaticLegacy.enigmaticAmulet;
    }

    public static Item berserkEmblem() {
        return EnigmaticLegacy.berserkEmblem;
    }

    public static Item twistedCore() {
        return EnigmaticLegacy.twistedCore;
    }

    public static Item forbiddenFruit() {
        return EnigmaticLegacy.forbiddenFruit;
    }
}
