package net.mx.edelicacy.flora;

import net.minecraftforge.common.config.Configuration;

/**
 * 植物与土壤模块配置：preInit 读取一次，之后只读。
 */
public final class FloraConfig {

    public static final String CATEGORY_INFINISOIL = "Infinisoil";

    private static final String[] DEFAULT_CONVERT_LIST = {
            "netherized:crimson_fungus->nethers_delight_legacy:crimson_fungus_colony",
            "netherized:warped_fungus->nethers_delight_legacy:warped_fungus_colony"
    };

    private static volatile String[] convertList = DEFAULT_CONVERT_LIST;

    private FloraConfig() {
    }

    public static void load(Configuration config) {
        config.setCategoryComment(CATEGORY_INFINISOIL, "Infini-Soil behaviour.");
        convertList = config.getStringList("convertList", CATEGORY_INFINISOIL, DEFAULT_CONVERT_LIST,
                "Modded mushrooms/fungi that Infini-Soil turns into colonies, format 'plant_block_id->colony_block_id'. "
                        + "Vanilla brown/red mushrooms always become Farmer's Delight colonies. Entries whose blocks are missing are ignored.");
    }

    public static String[] convertList() {
        return convertList.clone();
    }
}
