package net.mx.edelicacy.enigmatic;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.mx.edelicacy.data.DelicacyData;

/**
 * /enigmaticfood clear [玩家]：清除谜之食物的全部永久加成。
 * <p>1.21 原版没有权限检查（任何人都能清别人的加成），这里要求 2 级权限。
 */
public class EnigmaticFoodCommand extends CommandBase {

    @Override
    public String getName() {
        return "enigmaticfood";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.enigmaticdelicacy.enigmaticfood.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || args.length > 2 || !"clear".equals(args[0])) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP target = args.length == 2 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
        // 标脏后在玩家 tick 末尾自动同步；属性来源下次求值时撤掉修饰符
        DelicacyData.get(target).clearFoodAttributes();
        notifyCommandListener(sender, this, "message.enigmaticdelicacy.command.foodEffectClear", target.getDisplayName());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "clear");
        }
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 1;
    }
}
