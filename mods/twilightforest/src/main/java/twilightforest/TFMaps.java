package twilightforest;

import com.mojang.serialization.Codec;
import hexxit.worldgen152.OldGameIds;
import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;
import twilightforest.gen.TFBiomes;
import twilightforest.gen.TFFeature;
import twilightforest.gen.TFLayout;
import twilightforest.item.EmptyMagicMapItem;
import twilightforest.item.EmptyMazeMapItem;
import twilightforest.item.MagicMapItem;
import twilightforest.item.MazeMapItem;
import twilightforest.world.TFChunkGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The three maps of 1.5.2 (Teil 24f): ItemTFMagicMap (7289), ItemTFMazeMap (7291, ore map 7302) and the blank maps
 * ItemTFEmptyMagicMap (7260), ItemTFEmptyMazeMap (7261, 7262), item ids of the pack's TwilightForest.cfg + 256.
 *
 * <p>The filled maps keep their picture in vanilla {@link MapItemSavedData} under the {@code minecraft:map_id}
 * component. The magic map stores {@code biome id + 1} per pixel (TFMagicMapData) instead of a map colour; it carries
 * the invisible decoration {@link #MAGIC_MAP_MARKER} so that the client can tell its data apart and paints it with
 * the biome colours of {@link #BIOME_COLORS}. The feature icons (TFMagicMapData.featuresVisibleOnMap) are decorations
 * of the types in {@link #FEATURE_ICONS}, also stored on the stack ({@code minecraft:map_decorations}) so that they
 * survive a restart and show in item frames. The maze map's height layer (TFMazeMapData.yCenter) is the component
 * {@link #MAZE_MAP_Y}.
 */
public final class TFMaps {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TwilightForest.MOD_ID);
    private static final DeferredRegister<MapDecorationType> DECORATIONS = DeferredRegister.create(Registries.MAP_DECORATION_TYPE,
            TwilightForest.MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE,
            TwilightForest.MOD_ID);

    /** Items for the creative tab: the blank maps (the filled ones had no tab in 1.5.2). */
    public static final List<DeferredItem<? extends Item>> TAB_ITEMS = new ArrayList<>();

    /** TFMazeMapData.yCenter: the height layer a maze or ore map shows. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAZE_MAP_Y = COMPONENTS.registerComponentType("maze_map_y",
            b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** "magicMap", 7289: uncommon, stack of 1 in 1.5.2 (see MAPS_NOTES.md). */
    public static final DeferredItem<MagicMapItem> MAGIC_MAP = ITEMS.registerItem("magic_map", MagicMapItem::new, p -> p.rarity(Rarity.UNCOMMON));
    /** "mazeMap", 7291: uncommon. */
    public static final DeferredItem<MazeMapItem> MAZE_MAP = ITEMS.registerItem("maze_map", p -> new MazeMapItem(p, false),
            p -> p.rarity(Rarity.UNCOMMON));
    /** "oreMap", 7302: epic, shows the ores around the layer. */
    public static final DeferredItem<MazeMapItem> ORE_MAP = ITEMS.registerItem("ore_map", p -> new MazeMapItem(p, true),
            p -> p.rarity(Rarity.EPIC));
    /** "emptyMagicMap", 7260. */
    public static final DeferredItem<EmptyMagicMapItem> EMPTY_MAGIC_MAP = ITEMS.registerItem("empty_magic_map", EmptyMagicMapItem::new);
    /** "emptyMazeMap", 7261. */
    public static final DeferredItem<EmptyMazeMapItem> EMPTY_MAZE_MAP = ITEMS.registerItem("empty_maze_map", p -> new EmptyMazeMapItem(p, false));
    /** "emptyOreMap", 7262. */
    public static final DeferredItem<EmptyMazeMapItem> EMPTY_ORE_MAP = ITEMS.registerItem("empty_ore_map", p -> new EmptyMazeMapItem(p, true));

    /** Marks the data of a magic map; drawn by nobody. */
    public static final DeferredHolder<MapDecorationType, MapDecorationType> MAGIC_MAP_MARKER = DECORATIONS.register("magic_map",
            () -> new MapDecorationType(TwilightForest.id("magic_map"), false, false));
    /** The key of {@link #MAGIC_MAP_MARKER} in the decorations of a map. */
    public static final String MARKER_KEY = "twilightforest_magic_map";

    /**
     * One decoration type per TFFeature (index = featureID, the icon of textures/gui/mapicons.png in an 8 x 8 grid),
     * shown in item frames too. The client draws them from mapicons.png ({@code twilightforest.client.TFMapsClient}).
     */
    @SuppressWarnings("unchecked")
    public static final DeferredHolder<MapDecorationType, MapDecorationType>[] FEATURE_ICONS = new DeferredHolder[64];

    static {
        for (int id = 0; id < 64; id++) {
            TFFeature feature = TFFeature.byId(id);
            if (feature.featureID == id) {
                String name = "feature/" + feature.name.toLowerCase(Locale.ROOT).replace(' ', '_');
                FEATURE_ICONS[id] = DECORATIONS.register(name, () -> new MapDecorationType(TwilightForest.id(name), true, false));
            }
        }
        TAB_ITEMS.add(EMPTY_MAGIC_MAP);
        TAB_ITEMS.add(EMPTY_MAZE_MAP);
        TAB_ITEMS.add(EMPTY_ORE_MAP);
    }

    /**
     * BiomeGenBase.color by 1.5.2 biome id: the vanilla biomes 0-22 and the Twilight Forest biomes of TFBiomeBase;
     * {@code -1} for ids without a known colour.
     */
    public static final int[] BIOME_COLORS = new int[256];

    static {
        java.util.Arrays.fill(BIOME_COLORS, -1);
        int[] vanilla = {112, 9286496, 16421912, 6316128, 353825, 747097, 522674, 255, 16711680, 8421631, 9474208, 10526975, 16777215,
                10526880, 16711935, 10486015, 16440917, 13786898, 2250012, 1456435, 7501978, 5470985, 2900485};
        System.arraycopy(vanilla, 0, BIOME_COLORS, 0, vanilla.length);
        BIOME_COLORS[TFBiomes.LAKE.id] = 255;
        BIOME_COLORS[TFBiomes.TWILIGHT_FOREST.id] = 21760;
        BIOME_COLORS[TFBiomes.TWILIGHT_FOREST_2.id] = 21794;
        BIOME_COLORS[TFBiomes.HIGHLANDS.id] = 5596740;
        BIOME_COLORS[TFBiomes.MUSHROOMS.id] = 4482594;
        BIOME_COLORS[TFBiomes.SWAMP.id] = 3359778;
        BIOME_COLORS[TFBiomes.STREAM.id] = 3298231;
        BIOME_COLORS[TFBiomes.SNOW.id] = 13434879;
        BIOME_COLORS[TFBiomes.GLACIER.id] = 7829435;
        BIOME_COLORS[TFBiomes.CLEARING.id] = 3447604;
        BIOME_COLORS[TFBiomes.CLEARING_BORDER.id] = 26112;
        BIOME_COLORS[TFBiomes.LAKE_BORDER.id] = 26163;
        BIOME_COLORS[TFBiomes.DEEP_MUSHROOMS.id] = 6697762;
        BIOME_COLORS[TFBiomes.MAJOR_FEATURE.id] = 16711680;
        BIOME_COLORS[TFBiomes.MINOR_FEATURE.id] = 11184640;
        BIOME_COLORS[TFBiomes.DARK_FOREST.id] = 13073;
        BIOME_COLORS[TFBiomes.ENCHANTED_FOREST.id] = 1135974;
        BIOME_COLORS[TFBiomes.FIRE_SWAMP.id] = 4334362;
    }

    /** The colour of a 1.5.2 biome on the magic map; grey for a known biome without a colour of its own. */
    public static int biomeColor(int oldId) {
        int color = BIOME_COLORS[oldId & 255];
        return color >= 0 ? color : 0x808080;
    }

    private static volatile Map<String, Integer> oldIdsByKey;

    /** 1.5.2 biome id of a biome of today: its worldgen152 / Twilight Forest key, or a vanilla biome alike; -1 if none. */
    public static int oldBiomeId(Holder<Biome> biome) {
        Map<String, Integer> ids = oldIdsByKey;
        if (ids == null) {
            ids = new HashMap<>();
            String[][] vanilla = {{"ocean", "0"}, {"deep_ocean", "0"}, {"plains", "1"}, {"desert", "2"}, {"windswept_hills", "3"},
                    {"forest", "4"}, {"taiga", "5"}, {"swamp", "6"}, {"river", "7"}, {"nether_wastes", "8"}, {"the_end", "9"},
                    {"frozen_ocean", "10"}, {"frozen_river", "11"}, {"snowy_plains", "12"}, {"mushroom_fields", "14"}, {"beach", "16"},
                    {"jungle", "21"}};
            for (String[] entry : vanilla) {
                ids.put("minecraft:" + entry[0], Integer.parseInt(entry[1]));
            }
            for (OldBiome old : OldBiomes.all()) {
                String key = old.key != null ? old.key : TFBiomes.key(old.id);
                if (key != null) {
                    ids.put(key, old.id);
                }
            }
            oldIdsByKey = ids;
        }
        return biome.unwrapKey().map(key -> oldIdsByKey.getOrDefault(key.identifier().toString(), -1)).orElse(-1);
    }

    /** The Twilight Forest layout of a level, or null outside the Twilight Forest (TFWorldChunkManager). */
    public static @Nullable TFLayout layout(Level level) {
        if (level instanceof ServerLevel server && server.getChunkSource().getGenerator() instanceof TFChunkGenerator generator) {
            return generator.world().layout();
        }
        return null;
    }

    /**
     * World.getBiomeGenForCoords(x, z).biomeID: exact from the layout in the Twilight Forest, elsewhere the biome of
     * the 4 x 4 cell at sea level; -1 if it has no 1.5.2 id.
     */
    public static int biomeIdAt(Level level, @Nullable TFLayout layout, int x, int z) {
        if (layout != null) {
            return layout.chunkIds(x >> 4, z >> 4)[(x & 15) + (z & 15) * 16] & 0xFF;
        }
        return oldBiomeId(level.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(level.getSeaLevel()), QuartPos.fromBlock(z)));
    }

    /**
     * A new map data with the exact centre of 1.5.2 (vanilla {@code createFresh} would snap it to its own grid),
     * registered under a free map id that is put on the stack.
     */
    public static MapItemSavedData newMapData(ServerLevel level, ItemStack stack, int centerX, int centerZ, int scale) {
        MapItemSavedData fresh = MapItemSavedData.createFresh(centerX, centerZ, (byte) scale, true, false, level.dimension());
        CompoundTag tag = (CompoundTag) MapItemSavedData.CODEC.encodeStart(NbtOps.INSTANCE, fresh).getOrThrow();
        tag.putInt("xCenter", centerX);
        tag.putInt("zCenter", centerZ);
        MapItemSavedData data = MapItemSavedData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
        MapId id = level.getFreeMapId();
        level.setMapData(id, data);
        stack.set(DataComponents.MAP_ID, id);
        return data;
    }

    private TFMaps() {
    }

    static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        DECORATIONS.register(modBus);
        COMPONENTS.register(modBus);
    }

    /** The item ids of the pack's TwilightForest.cfg (+ 256); the damage of a filled map was its map number. */
    static void registerOld() {
        OldGameIds.registerItem(7289 + 256, MAGIC_MAP.getId().toString());
        OldGameIds.registerItem(7291 + 256, MAZE_MAP.getId().toString());
        OldGameIds.registerItem(7302 + 256, ORE_MAP.getId().toString());
        OldGameIds.registerItem(7260 + 256, EMPTY_MAGIC_MAP.getId().toString());
        OldGameIds.registerItem(7261 + 256, EMPTY_MAZE_MAP.getId().toString());
        OldGameIds.registerItem(7262 + 256, EMPTY_ORE_MAP.getId().toString());
    }

    /** The id the client uses for the texture of mapicons.png. */
    public static final Identifier MAP_ICONS = TwilightForest.id("textures/gui/mapicons.png");
}
