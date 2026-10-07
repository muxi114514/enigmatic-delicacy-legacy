package net.mx.edelicacy.integration.waila;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.mx.edelicacy.machine.pan.BlockVoraciousPan;
import net.mx.edelicacy.machine.stove.BlockEtheriumStove;

/** Hwyla：准星对着以太炉、放下的饕餮之锅时显示内容（只在装了 Hwyla 时由它加载） */
@WailaPlugin
public class DelicacyWailaPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar registrar) {
        MachineWailaProvider provider = new MachineWailaProvider();
        registrar.registerBodyProvider(provider, BlockEtheriumStove.class);
        registrar.registerNBTProvider(provider, BlockEtheriumStove.class);
        registrar.registerBodyProvider(provider, BlockVoraciousPan.class);
        registrar.registerNBTProvider(provider, BlockVoraciousPan.class);
    }
}
