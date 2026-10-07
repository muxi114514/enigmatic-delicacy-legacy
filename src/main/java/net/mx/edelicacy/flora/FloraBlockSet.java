package net.mx.edelicacy.flora;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.flora.block.FadingGlassBlock;
import net.mx.edelicacy.flora.crop.EnigmaticBushBlock;
import net.mx.edelicacy.flora.soil.InfinisoilBlock;
import net.mx.edelicacy.flora.soil.InfinisoilFarmlandBlock;
import net.mx.edelicacy.flora.tree.AstralFruitBlock;
import net.mx.edelicacy.flora.tree.AstralLeavesBlock;
import net.mx.edelicacy.flora.tree.AstralSaplingBlock;
import net.mx.edelicacy.flora.tree.PottedAstralSaplingBlock;
import net.mx.edelicacy.flora.wood.AstralDoorBlock;
import net.mx.edelicacy.flora.wood.AstralLogBlock;
import net.mx.edelicacy.flora.wood.AstralSlabBlock;
import net.mx.edelicacy.flora.wood.AstralWoodParts;
import net.mx.edelicacy.item.base.DelicacyItem;

/**
 * 本模块的全部方块实例（在方块注册事件里创建，供随后的物品注册与模型注册直接引用）。
 * 注册名与 registry/DelicacyBlocks 一致。
 */
public final class FloraBlockSet {

    private final List<Block> all = new ArrayList<>();

    Block enigmaticBush;
    Block fruitCrate;
    Block infinisoil;
    Block infinisoilFarmland;
    Block astralLog;
    Block astralWood;
    Block strippedLog;
    Block strippedWood;
    Block sapling;
    Block pottedSapling;
    Block planks;
    Block stairs;
    AstralSlabBlock slab;
    AstralSlabBlock doubleSlab;
    Block door;
    Block trapdoor;
    Block pressurePlate;
    Block button;
    Block cabinet;
    Block leaves;
    Block blossomingLeaves;
    Block astralFruit;
    Block fadingGlass;

    void create() {
        enigmaticBush = add(new EnigmaticBushBlock(), "enigmatic_bush");
        fruitCrate = add(new AstralWoodParts.Wood(MapColor.WOOD, 2.0F, 5.0F), "enigmatic_fruit_crate");
        infinisoil = add(new InfinisoilBlock(), "infinisoil");
        infinisoilFarmland = add(new InfinisoilFarmlandBlock(), "infinisoil_farmland");
        sapling = add(new AstralSaplingBlock(), "astral_sapling");
        pottedSapling = add(new PottedAstralSaplingBlock(), "potted_astral_sapling");
        astralLog = add(new AstralLogBlock(), "astral_log");
        astralWood = add(new AstralLogBlock(), "astral_wood");
        leaves = add(new AstralLeavesBlock(false), "astral_leaves");
        blossomingLeaves = add(new AstralLeavesBlock(true), "blossoming_astral_leaves");
        astralFruit = add(new AstralFruitBlock(), "astral_fruit");
        strippedLog = add(new AstralLogBlock(), "stripped_astral_log");
        strippedWood = add(new AstralLogBlock(), "stripped_astral_wood");
        planks = add(new AstralWoodParts.Wood(MapColor.LIGHT_BLUE_STAINED_HARDENED_CLAY, 3.0F, 5.0F * 5.0F / 3.0F), "astral_planks");
        stairs = add(new AstralWoodParts.Stairs(planks.getDefaultState()), "astral_stairs");
        slab = add(new AstralSlabBlock.HalfSlab(), "astral_slab");
        doubleSlab = add(new AstralSlabBlock.DoubleSlab(), "astral_double_slab");
        doubleSlab.setUnlocalizedName(EnigmaticDelicacy.MODID + ".astral_slab");
        door = add(new AstralDoorBlock(), "astral_door");
        trapdoor = add(new AstralWoodParts.Trapdoor(), "astral_trapdoor");
        pressurePlate = add(new AstralWoodParts.PressurePlate(), "astral_pressure_plate");
        button = add(new AstralWoodParts.Button(), "astral_button");
        cabinet = add(new AstralWoodParts.Cabinet(), "astral_cabinet");
        // 1.21 同样不放进创造栏
        fadingGlass = add(new FadingGlassBlock(), "fading_glass");
        fadingGlass.setCreativeTab(null);
    }

    private <T extends Block> T add(T block, String name) {
        DelicacyItem.setup(block, name);
        all.add(block);
        return block;
    }

    List<Block> all() {
        return Collections.unmodifiableList(all);
    }
}
