package net.mx.edelicacy.machine.stove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.mx.edelicacy.api.IStoveRepairable;

/**
 * 以太炉灶的修复规则：实现 {@link IStoveRepairable} 的物品按接口逐单位修复，
 * 配置表里的有耐久物品一次修满（进度 = 损耗 × 10，与 1.21 相同）。
 * <p>{@link IStoveRepairable#getStoveRepairTicks} 的数值按「熔炼进度」计（一次熔炼 = 200），炉子每 tick 推进
 * {@code furnaceProgressPerTick}，与 1.21 的以太牛排常量一致。
 */
public final class StoveRepairRules {

    /** 有耐久物品每点损耗需要的进度 */
    private static final int PROGRESS_PER_DAMAGE = 10;

    private static volatile List<Pattern> patterns = Collections.emptyList();
    /** 物品 → 是否在配置表内（注册名不会变，缓存安全） */
    private static final Map<Item, Boolean> CACHE = new ConcurrentHashMap<>();

    private StoveRepairRules() {
    }

    public static void setPatterns(String[] entries) {
        List<Pattern> list = new ArrayList<>();
        for (String entry : entries) {
            String trimmed = entry == null ? "" : entry.trim();
            if (!trimmed.isEmpty()) {
                list.add(Pattern.compile(Pattern.quote(trimmed).replace("*", "\\E.*\\Q")));
            }
        }
        patterns = Collections.unmodifiableList(list);
        CACHE.clear();
    }

    private static boolean isListed(Item item) {
        return CACHE.computeIfAbsent(item, key -> {
            ResourceLocation id = key.getRegistryName();
            if (id == null) {
                return false;
            }
            String name = id.toString();
            for (Pattern pattern : patterns) {
                if (pattern.matcher(name).matches()) {
                    return true;
                }
            }
            return false;
        });
    }

    /** 当前需要修复（单个物品才修） */
    public static boolean needsRepair(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() != 1) {
            return false;
        }
        if (stack.getItem() instanceof IStoveRepairable) {
            return ((IStoveRepairable) stack.getItem()).getStoveRepairTicks(stack) > 0;
        }
        return stack.isItemStackDamageable() && stack.isItemDamaged() && isListed(stack.getItem());
    }

    /** 本轮修复所需进度 */
    public static int getCycleProgress(ItemStack stack) {
        if (stack.getItem() instanceof IStoveRepairable) {
            return Math.max(1, ((IStoveRepairable) stack.getItem()).getStoveRepairTicks(stack));
        }
        return Math.max(1, (int) Math.min(Integer.MAX_VALUE, (long) stack.getItemDamage() * PROGRESS_PER_DAMAGE));
    }

    /** 完成一轮修复（原地修改） */
    public static void finishCycle(ItemStack stack) {
        if (stack.getItem() instanceof IStoveRepairable) {
            ((IStoveRepairable) stack.getItem()).repairOnStove(stack);
        } else {
            stack.setItemDamage(0);
        }
    }
}
