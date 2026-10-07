package net.mx.edelicacy.mixin;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.mx.edelicacy.charm.HungerSprint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 客户端疾跑的饱食度门槛：onLivingUpdate（func_70636_d）里唯一的 6.0F 常量（foodLevel > 6.0F）。
 * 佩戴饥饿 / 暴食护符时改为 0，即有饱食度就能疾跑。服务端不检查饱食度，无需改动。
 */
@Mixin(value = EntityPlayerSP.class, remap = false)
public abstract class MixinPlayerSPSprint {

    @ModifyConstant(method = "func_70636_d", constant = @Constant(floatValue = 6.0F), remap = false)
    private float edelicacy$hungrySprintThreshold(float original) {
        return HungerSprint.allowsLowFoodSprint((EntityPlayer) (Object) this) ? 0.0F : original;
    }
}
