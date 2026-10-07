package net.mx.edelicacy.flora;

import com.wdcftgg.farmersdelightlegacy.api.recipe.DecompositionRecipeApi;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.registry.DelicacyBlocks;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 非合成类的处理登记：熔炉、酿造、农夫乐事堆肥加速。合成/切菜板/厨锅配方由整合方用 JSON / CrT 写。
 */
final class FloraRecipes {

    /** 1.21 compost_activators 标签与农夫乐事沃土相同的加成 */
    private static final float COMPOST_BONUS = 0.02F;

    private FloraRecipes() {
    }

    static void register() {
        // logs_that_burn：星辰原木可烧木炭
        ItemStack charcoal = new ItemStack(Items.COAL, 1, 1);
        for (Block log : new Block[]{DelicacyBlocks.ASTRAL_LOG, DelicacyBlocks.ASTRAL_WOOD,
                DelicacyBlocks.STRIPPED_ASTRAL_LOG, DelicacyBlocks.STRIPPED_ASTRAL_WOOD}) {
            GameRegistry.addSmelting(log, charcoal.copy(), 0.15F);
        }
        // 1.21 DelicacySetupHandler：粗制药水 + 神秘果 → 隐身药水
        PotionHelper.addMix(PotionTypes.AWKWARD, DelicacyItems.ENIGMATIC_FRUIT, PotionTypes.INVISIBILITY);
        accelerator("infinisoil", DelicacyBlocks.INFINISOIL);
        accelerator("infinisoil_farmland", DelicacyBlocks.INFINISOIL_FARMLAND);
    }

    private static void accelerator(String name, Block soil) {
        DecompositionRecipeApi.registerAccelerator(EnigmaticDelicacy.MODID + ":" + name,
                (world, compostPos, testPos, state) -> state.getBlock() == soil ? COMPOST_BONUS : 0.0F,
                new ItemStack(soil));
    }
}
