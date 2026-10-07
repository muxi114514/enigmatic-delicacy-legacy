package net.mx.edelicacy.food.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.food.entity.EntityThrownBlazingMeatRoll;
import net.mx.edelicacy.food.item.ItemRiceCake;
import net.mx.edelicacy.registry.DelicacyItems;

/** 日常食物模块的客户端：物品模型、星叶粽蜂蜜模型切换、肉卷投掷物渲染、闪冰酥闪光粒子 */
@SideOnly(Side.CLIENT)
public final class FoodClient {

    /** FD Legacy 自带但没有用上的星星粒子贴图（与 1.21 FD 的 sparkle 粒子同图） */
    private static final ResourceLocation STAR = new ResourceLocation("farmersdelight", "particle/star");
    private static TextureAtlasSprite starSprite;

    private FoodClient() {
    }

    public static void preInit() {
        MinecraftForge.EVENT_BUS.register(new FoodClient.Events());
        // 工厂在渲染管理器创建时才调用，此时物品已注册
        RenderingRegistry.registerEntityRenderingHandler(EntityThrownBlazingMeatRoll.class,
                manager -> new RenderSnowball<>(manager, DelicacyItems.BLAZING_MEAT_ROLL, Minecraft.getMinecraft().getRenderItem()));
    }

    public static void registerModels(List<Item> items) {
        for (Item item : items) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
        Item riceCake = DelicacyItems.RICE_CAKE_IN_ASTRAL_LEAF;
        if (riceCake != null) {
            riceCake.addPropertyOverride(new ResourceLocation("honey"),
                    (stack, world, entity) -> ItemRiceCake.hasHoney(stack) ? 1.0F : 0.0F);
        }
    }

    public static void spawnSparkle(World world, double x, double y, double z) {
        if (starSprite != null) {
            Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSparkle(world, x, y, z, starSprite));
        }
    }

    public static final class Events {
        @SubscribeEvent
        public void onTextureStitch(TextureStitchEvent.Pre event) {
            if (event.getMap() == Minecraft.getMinecraft().getTextureMapBlocks()) {
                starSprite = event.getMap().registerSprite(STAR);
            }
        }
    }
}
