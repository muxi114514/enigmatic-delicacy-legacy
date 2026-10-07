package net.mx.edelicacy.flora.client;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.EnigmaticDelicacy;

/**
 * 星辰叶片粒子：4 种颜色 × 4 帧贴图拼进方块图集，随机取一帧（同 1.21 pickSprite）。
 * 风向用两个静态量缓慢漂移（1.21 AstralLeaves.speedX/Z），只在客户端主线程读写。
 */
@SideOnly(Side.CLIENT)
public final class AstralLeafFx {

    private static final String[] COLORS = {"blue", "orange", "purple", "pink"};
    private static final int FRAMES = 4;
    private static final double MAX_DISTANCE_SQ = 32.0D * 32.0D;

    private static final TextureAtlasSprite[][] SPRITES = new TextureAtlasSprite[COLORS.length][FRAMES];
    private static float windX;
    private static float windZ;

    private AstralLeafFx() {
    }

    /** 由 FloraClient 注册到事件总线 */
    public static final class SpriteRegistrar {
        @SubscribeEvent
        public void onStitch(TextureStitchEvent.Pre event) {
            TextureMap map = event.getMap();
            if (!"textures".equals(map.getBasePath())) {
                return;
            }
            for (int c = 0; c < COLORS.length; c++) {
                for (int f = 0; f < FRAMES; f++) {
                    SPRITES[c][f] = map.registerSprite(new ResourceLocation(EnigmaticDelicacy.MODID,
                            "particle/" + COLORS[c] + "_astral_leaf_" + f));
                }
            }
        }
    }

    /** 从树叶底面飘下一片叶子；遵守粒子设置与 32 格距离限制 */
    public static void spawnFalling(World world, Random rand, BlockPos pos) {
        windX = (windX + rand.nextFloat() - 0.5F) / 1.6F;
        windZ = (windZ + rand.nextFloat() - 0.5F) / 1.6F;
        Minecraft mc = Minecraft.getMinecraft();
        Entity viewer = mc.getRenderViewEntity();
        TextureAtlasSprite sprite = SPRITES[rand.nextInt(COLORS.length)][rand.nextInt(FRAMES)];
        if (viewer == null || sprite == null || !shouldSpawn(mc, rand)) {
            return;
        }
        double x = pos.getX() + rand.nextFloat();
        double y = pos.getY();
        double z = pos.getZ() + rand.nextFloat();
        if (viewer.getDistanceSq(x, y, z) > MAX_DISTANCE_SQ) {
            return;
        }
        mc.effectRenderer.addEffect(new AstralLeafParticle(world, x, y, z, windX / 4.0F, 0.0D, windZ / 4.0F, sprite));
    }

    /** 同原版 RenderGlobal：最少=不生成，减少=1/3 概率不生成 */
    private static boolean shouldSpawn(Minecraft mc, Random rand) {
        int setting = mc.gameSettings.particleSetting;
        if (setting == 1 && rand.nextInt(3) == 0) {
            setting = 2;
        }
        return setting < 2;
    }
}
