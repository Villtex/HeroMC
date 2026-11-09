package thesbros.rftl;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * RottenFleshToLeather: rotten flesh smelts into leather (0.2 xp) in a furnace.
 *
 * <p>The 1.5.2 original registered the recipe in code; it now lives in
 * {@code data/rftl/recipe/leather.json}. Only the furnace existed in 1.5.2, so there is
 * deliberately no smoker / campfire variant.
 */
@Mod(RFTL.MOD_ID)
public final class RFTL {
    public static final String MOD_ID = "rftl";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RFTL() {
        LOGGER.info("[RottenFleshToLeather] Loaded!");
    }
}
