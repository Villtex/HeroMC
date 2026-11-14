package ichun.backtools;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * BackTools 1.5.2v1: the tool a player held last (sword, shears, bow, hoe, pickaxe, axe, shovel) is shown on their
 * back while they hold something else. Only seen by others or in third person; hidden under a cape. Client only
 * in effect; the server just loads the mod.
 */
@Mod(BackTools.MOD_ID)
public final class BackTools {
    public static final String MOD_ID = "mod_backtools";

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    /** itemSupport of mod_BackTools.cfg (empty in the pack), with item names instead of numeric ids. */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_SUPPORT = BUILDER
            .comment("More items for the back: \"<item id>:<orientation 0-3>:<flip 0/1>\", e.g. \"minecraft:fishing_rod:3:0\"")
            .defineListAllowEmpty("item_support", List.of(), () -> "", o -> o instanceof String);
    private static final ModConfigSpec SPEC = BUILDER.build();

    public BackTools(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ichun.backtools.client.BackToolsClient.register(modBus);
        }
    }
}
