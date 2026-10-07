package net.mx.edelicacy.curse.client;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.item.blade.ItemCurseBlade;
import net.mx.edelicacy.curse.item.divine.EntityThrownDivineFruitPie;
import net.mx.edelicacy.registry.DelicacyItems;

/** 本线的客户端注册：物品模型与属性覆盖、投掷物渲染、深渊状态条与禁忌物品提示框 */
@SideOnly(Side.CLIENT)
public final class CurseClient {

    private CurseClient() {
    }

    public static void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityThrownDivineFruitPie.class,
                manager -> new RenderSnowball<>(manager, DelicacyItems.DIVINE_FRUIT_PIE, Minecraft.getMinecraft().getRenderItem()));
        MinecraftForge.EVENT_BUS.register(new AbyssHudRenderer());
        MinecraftForge.EVENT_BUS.register(new ForbiddenTooltipColor());
    }

    /** 模型：每个物品对应 models/item/注册名.json */
    public static void registerModels(List<Item> items) {
        for (Item item : items) {
            if (item.getRegistryName() != null) {
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
            if (item instanceof ItemCurseBlade) {
                registerBladeProperties(item);
            }
        }
    }

    /** 诅咒刃片：using = 正在蓄力，active = 已蓄力 */
    private static void registerBladeProperties(Item blade) {
        blade.addPropertyOverride(new ResourceLocation("using"), new IItemPropertyGetter() {
            @Override
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                return entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
        blade.addPropertyOverride(new ResourceLocation("active"), new IItemPropertyGetter() {
            @Override
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                return ItemCurseBlade.isActive(stack) ? 1.0F : 0.0F;
            }
        });
    }

    /** 18×18 的药水图标 */
    public static void drawPotionIcon(ResourceLocation icon, int x, int y, float alpha) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(icon);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 18, 18, 18, 18);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
