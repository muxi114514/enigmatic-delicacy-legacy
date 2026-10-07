package net.mx.edelicacy.fading.client;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.EnigmaticDelicacy;

/**
 * 客户端：记录处于失色的实体编号并跳过它们的渲染（含护甲、手持物、名字与 EL 卷轴环绕）；失色图标绘制；物品模型。
 * <p>编号集合在主线程读写，断线时由网络线程清空，故用并发集合。
 */
@SideOnly(Side.CLIENT)
public final class FadingClient {

    private static final ResourceLocation ICON = new ResourceLocation(EnigmaticDelicacy.MODID, "textures/mob_effect/fading.png");
    /** 图标贴图是 5 帧竖排动画，只画第一帧 */
    private static final int ICON_FRAMES = 5;
    private static final int PRUNE_INTERVAL = 200;

    private static final Set<Integer> FADING = ConcurrentHashMap.newKeySet();

    private FadingClient() {
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    public static void registerModels(List<Item> items) {
        for (Item item : items) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    /** 网络线程调用 */
    public static void schedule(int entityId, boolean active) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (active) {
                FADING.add(entityId);
            } else {
                FADING.remove(entityId);
            }
        });
    }

    public static boolean isFading(EntityLivingBase entity) {
        return FADING.contains(entity.getEntityId());
    }

    public static void drawPotionIcon(int x, int y, float alpha) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(ICON);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 18, 18, 18, 18 * ICON_FRAMES);
    }

    public static final class Events {
        private int ticks;

        @SubscribeEvent(priority = EventPriority.HIGH)
        public void onRenderLiving(RenderLivingEvent.Pre<EntityLivingBase> event) {
            if (!FADING.isEmpty() && isFading(event.getEntity())) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            if (event.getWorld().isRemote) {
                FADING.clear();
            }
        }

        @SubscribeEvent
        public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
            FADING.clear();
        }

        /** 定期清掉已不在世界里的实体，避免长时间游玩累积 */
        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END || FADING.isEmpty() || ++ticks < PRUNE_INTERVAL) {
                return;
            }
            ticks = 0;
            WorldClient world = Minecraft.getMinecraft().world;
            if (world == null) {
                FADING.clear();
            } else {
                FADING.removeIf(id -> world.getEntityByID(id) == null);
            }
        }
    }
}
