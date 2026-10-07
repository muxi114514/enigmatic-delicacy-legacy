package net.mx.edelicacy.curse.item.gluttony;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.compat.ModCompat;
import net.mx.edelicacy.curse.util.ContainerPlayers;
import net.mx.edelicacy.curse.util.CurseRefs;
import net.mx.edelicacy.util.EnigmaticBridge;
import org.apache.logging.log4j.LogManager;

/**
 * 无尽贪食徽记的合成（1.21 的 CursedShapedRecipe）：形状同原版，但合成者必须佩戴七咒之戒，
 * 自动合成机等找不到玩家时不匹配。材料换成 1.12 对应物：下界合金锭 → 矿辞 ingotNetherite，
 * 绯红菌 → 下界化的绯红菌（未安装则下界疣），扭曲之心 → EL 扭曲核心，炼狱余烬 → 神遗拓展。
 */
public class GluttonyCharmRecipe extends ShapedOreRecipe {

    private GluttonyCharmRecipe(ResourceLocation group, ItemStack result, Object... recipe) {
        super(group, result, recipe);
    }

    /** 材料缺失（如其他模组未注册）时返回 null 并记日志 */
    @Nullable
    public static IRecipe create(Item gluttonyCharm) {
        Item hungryCharm = CurseRefs.HUNGRY_CHARM.get();
        Item cinder = CurseRefs.INFERNAL_CINDER.get();
        Item twisted = CurseRefs.twistedCore();
        if (hungryCharm == null || cinder == null || twisted == null) {
            LogManager.getLogger("enigmaticdelicacy").warn("Gluttony charm recipe skipped: hungry_charm / infernal_cinder / twisted_core missing");
            return null;
        }
        Item fungus = ModCompat.netherized && CurseRefs.CRIMSON_FUNGUS.get() != null ? CurseRefs.CRIMSON_FUNGUS.get() : Items.NETHER_WART;
        ResourceLocation id = new ResourceLocation(EnigmaticDelicacy.MODID, "gluttony_charm");
        GluttonyCharmRecipe recipe = new GluttonyCharmRecipe(id, new ItemStack(gluttonyCharm),
                "CNC", "MXM", "ITI",
                'C', fungus,
                'N', new OreIngredient("ingotNetherite"),
                'M', Items.MAGMA_CREAM,
                'X', hungryCharm,
                'I', cinder,
                'T', twisted);
        recipe.setRegistryName(id);
        return recipe;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        if (!super.matches(inv, world)) {
            return false;
        }
        EntityPlayer player = ContainerPlayers.craftingPlayer(inv);
        return player != null && EnigmaticBridge.isCursed(player);
    }
}
