package twilightforest.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jspecify.annotations.Nullable;
import twilightforest.TFMaps;
import twilightforest.gen.TFBiomes;
import twilightforest.gen.TFFeature;
import twilightforest.gen.TFLayout;

/**
 * ItemTFMagicMap (item 7289 + 256) with TFMagicMapData: scale 4 (1 pixel = 16 x 16 blocks), drawn in a circle of 32
 * pixels around the holder, every pixel shows the most frequent 1.5.2 biome of its 256 columns (rivers and streams
 * count three times) as {@code biome id + 1}. In the Twilight Forest every feature chunk in the circle puts the icon of
 * its feature (TFFeature.getNearestFeature) on the map.
 */
public class MagicMapItem extends TFFilledMapItem {
    /** ItemTFMagicMap / ItemTFEmptyMagicMap: mapData.scale = 4. */
    public static final int SCALE = 4;

    public MagicMapItem(Properties properties) {
        super(properties);
    }

    /** Puts the marker of a magic map on its stack (and so on its data, also in an item frame). */
    public static void mark(ItemStack stack, MapItemSavedData data) {
        MapDecorations decorations = stack.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY);
        if (!decorations.decorations().containsKey(TFMaps.MARKER_KEY)) {
            stack.set(DataComponents.MAP_DECORATIONS, decorations.withDecoration(TFMaps.MARKER_KEY,
                    new MapDecorations.Entry(TFMaps.MAGIC_MAP_MARKER, data.centerX, data.centerZ, 180.0F)));
        }
    }

    @Override
    protected MapItemSavedData createDefaultData(ServerLevel level, ItemStack stack) {
        var spawn = level.getRespawnData().pos();
        MapItemSavedData data = TFMaps.newMapData(level, stack, spawn.getX(), spawn.getZ(), SCALE);
        mark(stack, data);
        return data;
    }

    @Override
    protected void tickMap(ItemStack stack, ServerLevel level, Entity owner, MapItemSavedData data, boolean held) {
        mark(stack, data);
        data.addDecoration(TFMaps.MAGIC_MAP_MARKER, level, TFMaps.MARKER_KEY, data.centerX, data.centerZ, 180.0, null);
        if (held) {
            this.paint(level, owner, data, stack);
        }
    }

    @Override
    public void update(Level level, Entity player, MapItemSavedData data) {
        this.paint(level, player, data, null);
    }

    /** ItemTFMagicMap.updateMapData. */
    private void paint(Level level, Entity entity, MapItemSavedData data, @Nullable ItemStack stack) {
        if (level.dimension() != data.dimension || !(entity instanceof Player player)) {
            return;
        }
        TFLayout layout = TFMaps.layout(level);
        int xSize = 128;
        int zSize = 128;
        int scaleFactor = 1 << data.scale;
        int xCenter = data.centerX;
        int zCenter = data.centerZ;
        int xDraw = Mth.floor(entity.getX() - xCenter) / scaleFactor + xSize / 2;
        int zDraw = Mth.floor(entity.getZ() - zCenter) / scaleFactor + zSize / 2;
        int drawSize = 512 / scaleFactor;
        MapItemSavedData.HoldingPlayer info = data.getHoldingPlayer(player);
        info.step++;
        int[] biomeFrequencies = new int[256];
        byte[] ids = null;
        int idsX = 0;
        int idsZ = 0;
        for (int xStep = xDraw - drawSize + 1; xStep < xDraw + drawSize; xStep++) {
            if ((xStep & 15) != (info.step & 15)) {
                continue;
            }
            // the row zStep = -1 of the original only fed the (unused) elevation; it drew nothing
            for (int zStep = Math.max(zDraw - drawSize - 1, 0); zStep < zDraw + drawSize; zStep++) {
                if (xStep < 0 || xStep >= xSize || zStep >= zSize) {
                    continue;
                }
                int xOffset = xStep - xDraw;
                int zOffset = zStep - zDraw;
                boolean dither = xOffset * xOffset + zOffset * zOffset > (drawSize - 2) * (drawSize - 2);
                boolean inCircle = xOffset * xOffset + zOffset * zOffset < drawSize * drawSize;
                int xDraw2 = (xCenter / scaleFactor + xStep - xSize / 2) * scaleFactor;
                int zDraw2 = (zCenter / scaleFactor + zStep - zSize / 2) * scaleFactor;
                java.util.Arrays.fill(biomeFrequencies, 0);
                boolean featureAdded = false;
                for (int xStep2 = 0; xStep2 < scaleFactor; xStep2++) {
                    for (int zStep2 = 0; zStep2 < scaleFactor; zStep2++) {
                        int x = xDraw2 + xStep2;
                        int z = zDraw2 + zStep2;
                        int biomeID;
                        if (layout != null) {
                            // the layout's biome ids of the column's chunk, fetched once per chunk
                            if (ids == null || x >> 4 != idsX || z >> 4 != idsZ) {
                                idsX = x >> 4;
                                idsZ = z >> 4;
                                ids = layout.chunkIds(idsX, idsZ);
                            }
                            biomeID = ids[(x & 15) + (z & 15) * 16] & 0xFF;
                        } else {
                            biomeID = TFMaps.biomeIdAt(level, null, x, z);
                        }
                        if (biomeID < 0) {
                            continue;
                        }
                        biomeFrequencies[biomeID]++;
                        if (biomeID == 7 || biomeID == TFBiomes.STREAM.id) {
                            biomeFrequencies[biomeID] += 2;
                        }
                        // TFWorldChunkManager.isInFeatureChunk; addFeatureToMap keeps the first feature of a position
                        if (layout != null && !featureAdded && inCircle && TFFeature.isInFeatureChunk(x >> 4, z >> 4)) {
                            addFeature((ServerLevel) level, data, stack, TFFeature.getNearestFeature(x >> 4, z >> 4, layout), xDraw2, zDraw2);
                            featureAdded = true;
                        }
                    }
                }
                byte biomeIDToShow = 0;
                int highestFrequency = 0;
                for (int i = 0; i < 256; i++) {
                    if (biomeFrequencies[i] > highestFrequency) {
                        biomeIDToShow = (byte) i;
                        highestFrequency = biomeFrequencies[i];
                    }
                }
                if (highestFrequency == 0) {
                    // no column with a 1.5.2 biome (only outside the Twilight Forest): leave the pixel empty
                    continue;
                }
                biomeIDToShow++;
                if (inCircle && (!dither || (xStep + zStep & 1) != 0)) {
                    data.updateColor(xStep, zStep, biomeIDToShow);
                }
            }
        }
    }

    /**
     * TFMagicMapData.addFeatureToMap: the icon at {@code (x - xCenter >> scale) << 1}, rotation 8, one per position.
     * The decoration also goes on the stack so that it survives a restart and shows in an item frame.
     */
    private static void addFeature(ServerLevel level, MapItemSavedData data, @Nullable ItemStack stack, TFFeature feature, int x, int z) {
        int relativeX = x - data.centerX >> data.scale;
        int relativeZ = z - data.centerZ >> data.scale;
        if (relativeX < -64 || relativeZ < -64 || relativeX > 64 || relativeZ > 64 || feature.featureID >= TFMaps.FEATURE_ICONS.length
                || TFMaps.FEATURE_ICONS[feature.featureID] == null) {
            return;
        }
        Holder<MapDecorationType> type = TFMaps.FEATURE_ICONS[feature.featureID];
        String key = "twilightforest_feature_" + (relativeX << 1) + "_" + (relativeZ << 1);
        int scale = 1 << data.scale;
        double px = data.centerX + decorationDelta(relativeX) * scale;
        double pz = data.centerZ + decorationDelta(relativeZ) * scale;
        data.addDecoration(type, level, key, px, pz, 180.0, null);
        if (stack != null) {
            MapDecorations decorations = stack.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY);
            if (!decorations.decorations().containsKey(key)) {
                stack.set(DataComponents.MAP_DECORATIONS, decorations.withDecoration(key, new MapDecorations.Entry(type, px, pz, 180.0F)));
            }
        }
    }

    /**
     * The offset (in pixels) from the centre that today's {@code addDecoration} turns into the map coordinate
     * {@code relative * 2} of 1.5.2: it rounds with {@code (byte) (d * 2 + 0.5)} and clamps at -63 to -128.
     */
    private static double decorationDelta(int relative) {
        return relative >= 0 ? relative : Math.max(relative - 0.25, -63.0);
    }
}
