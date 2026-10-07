package net.mx.edelicacy.module;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 全部功能模块，顺序即注册与网络包编号顺序（两端必须一致），新模块追加到末尾。
 */
public final class DelicacyModules {

    public static final List<IDelicacyModule> ALL = Collections.unmodifiableList(Arrays.<IDelicacyModule>asList(
            new net.mx.edelicacy.effect.EffectModule(),
            new net.mx.edelicacy.food.FoodModule(),
            new net.mx.edelicacy.flora.FloraModule(),
            new net.mx.edelicacy.enigmatic.EnigmaticFoodModule(),
            new net.mx.edelicacy.charm.CharmModule(),
            new net.mx.edelicacy.fading.FadingModule(),
            new net.mx.edelicacy.curse.CurseModule(),
            new net.mx.edelicacy.machine.MachineModule(),
            new net.mx.edelicacy.tool.ToolModule(),
            new net.mx.edelicacy.recipe.RecipeModule(),
            new net.mx.edelicacy.integration.IntegrationModule()
    ));

    private DelicacyModules() {
    }
}
