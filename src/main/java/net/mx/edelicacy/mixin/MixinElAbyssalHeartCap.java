package net.mx.edelicacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import keletu.enigmaticlegacy.event.EnigmaticEvents;

/**
 * 深渊之心上限 +1（1.21 MixinAbyssalHeart.getMaxCount +1）：EL 1.12 在 EnigmaticEvents#onLivingDrops 里
 * 写死「已获得 &lt; 5 颗才登记掉落」（该方法里唯一的 ICONST_5），改成 6，多出的一颗用于深渊乱炖。
 */
@Mixin(value = EnigmaticEvents.class, remap = false)
public abstract class MixinElAbyssalHeartCap {

    @ModifyConstant(method = "onLivingDrops", constant = @Constant(intValue = 5), remap = false)
    private static int enigmaticdelicacy$raiseAbyssalHeartCap(int original) {
        return original + 1;
    }
}
