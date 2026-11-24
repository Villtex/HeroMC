package hexxit.worldgen152.client;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Hexxit Legacy is the world type a new world starts with (the game opens the screen with "Default", our Integrated).
 * Only on the first opening of a fresh screen, and only while it still shows Default: a re-created world keeps its type.
 */
public final class DefaultWorldType {
    private static final ResourceKey<WorldPreset> LEGACY = ResourceKey.create(Registries.WORLD_PRESET, Identifier.fromNamespaceAndPath("hexxit", "legacy"));
    private static final Set<CreateWorldScreen> SEEN = Collections.newSetFromMap(new WeakHashMap<>());

    private DefaultWorldType() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, event -> {
            if (!(event.getScreen() instanceof CreateWorldScreen screen) || !SEEN.add(screen)) {
                return;
            }
            WorldCreationUiState state = screen.getUiState();
            var current = state.getWorldType().preset();
            if (current == null || !current.is(WorldPresets.NORMAL)) {
                return;
            }
            for (WorldCreationUiState.WorldTypeEntry entry : state.getNormalPresetList()) {
                if (entry.preset() != null && entry.preset().is(LEGACY)) {
                    state.setWorldType(entry);
                    return;
                }
            }
        });
    }
}
