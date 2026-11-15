package invtweaks.client;

import java.nio.file.Path;
import net.neoforged.fml.loading.FMLPaths;

/** InvTweaksConst: version, timings and the files in config/. */
public final class InvTweaksConst {
    public static final String MOD_VERSION = "1.54b";
    /** Version of the converted item tree; a tree file with another treeVersion is backed up and replaced. */
    public static final String TREE_VERSION = "26.3";
    public static final int RULESET_SWAP_DELAY = 1000;
    public static final int AUTO_REFILL_DELAY = 200;
    public static final int CHEST_ALGORITHM_SWAP_MAX_INTERVAL = 2000;
    public static final int TOOLTIP_DELAY = 800;
    public static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get();
    public static final Path CONFIG_PROPS_FILE = CONFIG_DIR.resolve("InvTweaks.cfg");
    public static final Path CONFIG_RULES_FILE = CONFIG_DIR.resolve("InvTweaksRules.txt");
    public static final Path CONFIG_TREE_FILE = CONFIG_DIR.resolve("InvTweaksTree.txt");
    public static final String DEFAULT_CONFIG_FILE = "/invtweaks/DefaultConfig.txt";
    public static final String DEFAULT_CONFIG_TREE_FILE = "/invtweaks/DefaultTree.txt";
    public static final String HELP_URL = "http://modding.kalam-alami.net/invtweaks";
    public static final String INGAME_LOG_PREFIX = "InvTweaks: ";
    public static final int INVENTORY_SIZE = 36;
    public static final int INVENTORY_ROW_SIZE = 9;
    public static final int INVENTORY_HOTBAR_SIZE = 9;

    private InvTweaksConst() {
    }
}
