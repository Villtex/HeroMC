package twilightforest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.common.Tags;
import twilightforest.TFMaps;

/**
 * ItemTFMazeMap (maze map 7291 + 256, ore map 7302 + 256) with TFMazeMapData: scale 0, drawn in a circle of 16 blocks
 * around the holder while he is less than 3 blocks above or below the map's layer (yCenter); every pixel is the map
 * colour of the block at that layer (no shading). The ore map looks through stone up to 3 blocks up (light) and down
 * (dark) and colours the ores. Off the layer the holder's marker is the white dot (MapCoord.iconSize 6).
 */
public class MazeMapItem extends TFFilledMapItem {
    /** ItemTFMazeMap.YSEARCH. */
    private static final int YSEARCH = 3;
    private final boolean mapOres;

    public MazeMapItem(Properties properties, boolean mapOres) {
        super(properties);
        this.mapOres = mapOres;
    }

    public static int yCenter(ItemStack stack) {
        return stack.getOrDefault(TFMaps.MAZE_MAP_Y.get(), 0);
    }

    @Override
    protected MapItemSavedData createDefaultData(ServerLevel level, ItemStack stack) {
        BlockPos spawn = level.getRespawnData().pos();
        return TFMaps.newMapData(level, stack, spawn.getX(), spawn.getZ(), 0);
    }

    @Override
    protected void tickMap(ItemStack stack, ServerLevel level, Entity owner, MapItemSavedData data, boolean held) {
        int yCenter = yCenter(stack);
        if (owner instanceof Player player) {
            int yProximity = Mth.floor(player.getY() - yCenter);
            // only a marker on the map changes; a player off the map is left to the vanilla tracking
            float dx = (float) (player.getX() - data.centerX) / (1 << data.scale);
            float dz = (float) (player.getZ() - data.centerZ) / (1 << data.scale);
            if ((yProximity < -YSEARCH || yProximity > YSEARCH) && player.level().dimension() == data.dimension
                    && dx >= -63.0F && dz >= -63.0F && dx <= 63.0F && dz <= 63.0F) {
                data.addDecoration(MapDecorationTypes.PLAYER_OFF_MAP, level, player.getPlainTextName(), player.getX(), player.getZ(),
                        player.getYRot(), null);
            }
        }
        if (held) {
            this.paint(level, owner, data, yCenter);
        }
    }

    @Override
    public void update(Level level, Entity player, MapItemSavedData data) {
        this.paint(level, player, data, 0);
    }

    /** ItemTFMazeMap.updateMapData. */
    private void paint(Level level, Entity entity, MapItemSavedData data, int yCenter) {
        int yDraw = Mth.floor(entity.getY() - yCenter);
        if (level.dimension() != data.dimension || yDraw <= -YSEARCH || yDraw >= YSEARCH || !(entity instanceof Player player)) {
            return;
        }
        int xSize = 128;
        int zSize = 128;
        int xCenter = data.centerX;
        int zCenter = data.centerZ;
        int xDraw = Mth.floor(entity.getX() - xCenter) + xSize / 2;
        int zDraw = Mth.floor(entity.getZ() - zCenter) + zSize / 2;
        int drawSize = 16;
        MapItemSavedData.HoldingPlayer info = data.getHoldingPlayer(player);
        info.step++;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int xStep = xDraw - drawSize + 1; xStep < xDraw + drawSize; xStep++) {
            if ((xStep & 15) != (info.step & 15)) {
                continue;
            }
            for (int zStep = Math.max(zDraw - drawSize - 1, 0); zStep < zDraw + drawSize; zStep++) {
                if (xStep < 0 || xStep >= xSize || zStep >= zSize) {
                    continue;
                }
                int xOffset = xStep - xDraw;
                int zOffset = zStep - zDraw;
                boolean dither = xOffset * xOffset + zOffset * zOffset > (drawSize - 2) * (drawSize - 2);
                if (xOffset * xOffset + zOffset * zOffset >= drawSize * drawSize || dither && (xStep + zStep & 1) == 0) {
                    continue;
                }
                int x = xCenter + xStep - xSize / 2;
                int z = zCenter + zStep - zSize / 2;
                LevelChunk chunk = level.getChunk(x >> 4, z >> 4);
                pos.set(x, yCenter, z);
                BlockState state = chunk.getBlockState(pos);
                int tint = 1;
                if (state.is(Blocks.STONE) && this.mapOres) {
                    for (int i = -YSEARCH; i <= YSEARCH; i++) {
                        pos.setY(yCenter + i);
                        BlockState search = chunk.getBlockState(pos);
                        if (!search.is(Blocks.STONE)) {
                            state = search;
                            if (i > 0) {
                                tint = 2;
                            }
                            if (i < 0) {
                                tint = 0;
                            }
                            break;
                        }
                    }
                    if (state.is(Blocks.STONE)) {
                        pos.setY(yCenter);
                    }
                }
                int colorIndex = state.isAir() ? 0 : state.getMapColor(level, pos).id;
                if (this.mapOres) {
                    colorIndex = oreColor(state, colorIndex);
                }
                data.updateColor(xStep, zStep, (byte) (colorIndex * 4 + tint));
            }
        }
    }

    /** The ore colours of ItemTFMazeMap (MapColor indices of 1.5.2 are those of today's first 14 colours). */
    private static int oreColor(BlockState state, int colorIndex) {
        if (state.is(Tags.Blocks.ORES_COAL)) {
            return MapColor.WOOD.id;
        } else if (state.is(Tags.Blocks.ORES_GOLD)) {
            return MapColor.PLANT.id;
        } else if (state.is(Tags.Blocks.ORES_IRON)) {
            return MapColor.METAL.id;
        } else if (state.is(Tags.Blocks.ORES_LAPIS)) {
            return MapColor.WATER.id;
        } else if (state.is(Tags.Blocks.ORES_REDSTONE)) {
            return MapColor.FIRE.id;
        } else if (state.is(Tags.Blocks.ORES_DIAMOND)) {
            return MapColor.ICE.id;
        } else if (state.is(Tags.Blocks.ORES_EMERALD)) {
            return MapColor.PLANT.id;
        } else if (!state.isAir() && (state.is(Tags.Blocks.ORES) || isOreName(state))) {
            // getUnlocalizedName().contains("ore"): any other ore, also of other mods
            return MapColor.FIRE.id;
        }
        return colorIndex;
    }

    private static boolean isOreName(BlockState state) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.endsWith("_ore") || path.startsWith("ore");
    }
}
