package net.mx.edelicacy.mixin;

import keletu.enigmaticlegacy.event.EnigmaticEvents;
import net.minecraft.entity.player.EntityPlayer;
import net.mx.edelicacy.charm.AstralTranquility;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * EL 1.12 的七咒失眠写在 EnigmaticEvents.onPlayerTick 里：
 * {@code if (sleeping && sleepTimer > 90 && hasCursed && enableInsomnia && 没戴泰迪熊) sleepTimer = 90}。
 * 这里改写其中唯一一次读取 sleepTimer：喝过天体安神茶时读到 0，封顶判断不成立，即可正常睡满跳夜。
 * <p>字段名为运行时 SRG 名（无 refmap）。
 */
@Mixin(value = EnigmaticEvents.class, remap = false)
public abstract class MixinElInsomnia {

    @Redirect(method = "onPlayerTick",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;field_71076_b:I",
                    opcode = Opcodes.GETFIELD),
            remap = false)
    private static int edelicacy$tranquilSleepTimer(EntityPlayer player) {
        return AstralTranquility.isTranquilized(player) ? 0 : player.getSleepTimer();
    }
}
