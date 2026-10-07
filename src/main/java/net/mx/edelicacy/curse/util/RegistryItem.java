package net.mx.edelicacy.curse.util;

import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** 按注册名惰性查找的物品（其他模组的物品，或注册事件里还拿不到的物品）；查到后缓存 */
public final class RegistryItem {

    private final ResourceLocation id;
    @Nullable
    private volatile Item cached;

    public RegistryItem(String id) {
        this.id = new ResourceLocation(id);
    }

    /** 未注册返回 null */
    @Nullable
    public Item get() {
        Item item = cached;
        if (item == null) {
            item = ForgeRegistries.ITEMS.getValue(id);
            if (item == Items.AIR) {
                item = null;
            }
            if (item != null) {
                cached = item;
            }
        }
        return item;
    }

    public ResourceLocation id() {
        return id;
    }
}
