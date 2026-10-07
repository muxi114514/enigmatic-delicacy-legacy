package net.mx.edelicacy.core;

import java.util.Map;

import fermiumbooter.FermiumRegistryAPI;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

/**
 * coremod 入口，唯一职责：向 FermiumBooter 注册 mixin 配置。不做 ASM，不得引用 MC 类。
 * <ul>
 *   <li>mixins.enigmaticdelicacy.json：神秘遗物的类（early，必需）</li>
 *   <li>mixins.enigmaticdelicacy.vanilla.json：原版类（early，不生成 refmap，方法名写 SRG，失配只失效不崩）</li>
 *   <li>mixins.enigmaticdelicacy.late.json：农夫乐事、下界乐事的类（late：early 阶段它们的 jar 还没进类加载器）</li>
 * </ul>
 */
@IFMLLoadingPlugin.Name("EnigmaticDelicacyCore")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1002)
public class DelicacyCorePlugin implements IFMLLoadingPlugin {

    public DelicacyCorePlugin() {
        FermiumRegistryAPI.enqueueMixin(false, "mixins.enigmaticdelicacy.json");
        FermiumRegistryAPI.enqueueMixin(false, "mixins.enigmaticdelicacy.vanilla.json");
        FermiumRegistryAPI.enqueueMixin(true, "mixins.enigmaticdelicacy.late.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
