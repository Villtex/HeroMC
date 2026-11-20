package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.TFBlocks;

import java.util.ArrayList;
import java.util.List;

/**
 * The static part of ItemTFOreMagnet (doMagnet, isReplaceable, isNetherReplaceable, findVein, isOre) and
 * TFGenerator.getBresehnamArray. The miner's tree uses it; the ore magnet item (Teil 24f) calls
 * {@link #doMagnet} with the line along the player's look.
 */
public final class OreMagnet {
    private static final TagKey<Block> ORES = TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
            net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ores"));
    private static final TagKey<Block> COAL_ORES = TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
            net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ores/coal"));

    private OreMagnet() {
    }

    /**
     * Walks the line from (useX, useY, useZ) to (destX, destY, destZ): the first dirt/grass/gravel/stone (or
     * netherrack) block on it is the base, the first ore the vein to pull. Up to 24 blocks of the vein move by the
     * offset ore -> base where they land in replaceable blocks (or air); their old places become stone (netherrack).
     * Returns the number of blocks moved.
     */
    public static int doMagnet(Level level, int useX, int useY, int useZ, int destX, int destY, int destZ) {
        int blocksMoved = 0;
        int[] line = getBresenhamArray(useX, useY, useZ, destX, destY, destZ);
        BlockState found = null;
        BlockPos foundPos = null;
        BlockPos base = null;
        boolean isNetherrack = false;
        for (int i = 0; i < line.length; i += 3) {
            BlockPos search = new BlockPos(line[i], line[i + 1], line[i + 2]);
            BlockState state = level.getBlockState(search);
            if (base == null) {
                if (isReplaceable(state)) {
                    base = search;
                } else if (isNetherReplaceable(state)) {
                    isNetherrack = true;
                    base = search;
                }
            }
            if (!state.isAir() && isOre(state)) {
                found = state;
                foundPos = search;
                break;
            }
        }
        if (base != null && found != null) {
            List<BlockPos> vein = new ArrayList<>();
            findVein(level, foundPos, found, vein);
            int offX = base.getX() - foundPos.getX();
            int offY = base.getY() - foundPos.getY();
            int offZ = base.getZ() - foundPos.getZ();
            for (BlockPos coord : vein) {
                BlockPos replace = coord.offset(offX, offY, offZ);
                BlockState there = level.getBlockState(replace);
                if ((isNetherrack ? isNetherReplaceable(there) : isReplaceable(there)) || there.isAir()) {
                    level.setBlock(coord, (isNetherrack ? Blocks.NETHERRACK : Blocks.STONE).defaultBlockState(), Block.UPDATE_CLIENTS);
                    level.setBlock(replace, found, Block.UPDATE_CLIENTS);
                    blocksMoved++;
                }
            }
        }
        return blocksMoved;
    }

    /** Dirt, grass, gravel, or what ores replace in the overworld (isGenMineableReplaceable(stone): stone). */
    public static boolean isReplaceable(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.GRAVEL) || state.is(Blocks.STONE);
    }

    /** Netherrack (isGenMineableReplaceable(netherrack)). */
    public static boolean isNetherReplaceable(BlockState state) {
        return state.is(Blocks.NETHERRACK);
    }

    private static void findVein(Level level, BlockPos pos, BlockState ore, List<BlockPos> vein) {
        if (vein.contains(pos) || vein.size() >= 24 || level.getBlockState(pos) != ore) {
            return;
        }
        vein.add(pos);
        findVein(level, pos.east(), ore, vein);
        findVein(level, pos.west(), ore, vein);
        findVein(level, pos.above(), ore, vein);
        findVein(level, pos.below(), ore, vein);
        findVein(level, pos.south(), ore, vein);
        findVein(level, pos.north(), ore, vein);
    }

    /**
     * isOre: no coal; iron, diamond, emerald, gold, lapis, redstone, quartz ore, the liveroot block, and every block
     * whose name contains "ore" (here: the tag c:ores and ids with the word "ore", so that "mining_core" is none).
     */
    public static boolean isOre(BlockState state) {
        if (state.is(COAL_ORES) || state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) {
            return false;
        }
        if (state.is(TFBlocks.LIVEROOT_BLOCK.get()) || state.is(ORES)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.matches("(.*_)?ores?(_.*)?");
    }

    /** TFGenerator.getBresehnamArray: the blocks of a 3D Bresenham line, both ends included, as x, y, z triples. */
    public static int[] getBresenhamArray(int x1, int y1, int z1, int x2, int y2, int z2) {
        int[] pixel = {x1, y1, z1};
        int dx = x2 - x1;
        int dy = y2 - y1;
        int dz = z2 - z1;
        int xInc = dx < 0 ? -1 : 1;
        int l = Math.abs(dx);
        int yInc = dy < 0 ? -1 : 1;
        int m = Math.abs(dy);
        int zInc = dz < 0 ? -1 : 1;
        int n = Math.abs(dz);
        int dx2 = l << 1;
        int dy2 = m << 1;
        int dz2 = n << 1;
        int[] line;
        if (l >= m && l >= n) {
            int err1 = dy2 - l;
            int err2 = dz2 - l;
            line = new int[3 * l + 3];
            for (int i = 0; i < l * 3; i += 3) {
                line[i] = pixel[0];
                line[i + 1] = pixel[1];
                line[i + 2] = pixel[2];
                if (err1 > 0) {
                    pixel[1] += yInc;
                    err1 -= dx2;
                }
                if (err2 > 0) {
                    pixel[2] += zInc;
                    err2 -= dx2;
                }
                err1 += dy2;
                err2 += dz2;
                pixel[0] += xInc;
            }
        } else if (m >= l && m >= n) {
            int err1 = dx2 - m;
            int err2 = dz2 - m;
            line = new int[3 * m + 3];
            for (int i = 0; i < m * 3; i += 3) {
                line[i] = pixel[0];
                line[i + 1] = pixel[1];
                line[i + 2] = pixel[2];
                if (err1 > 0) {
                    pixel[0] += xInc;
                    err1 -= dy2;
                }
                if (err2 > 0) {
                    pixel[2] += zInc;
                    err2 -= dy2;
                }
                err1 += dx2;
                err2 += dz2;
                pixel[1] += yInc;
            }
        } else {
            int err1 = dy2 - n;
            int err2 = dx2 - n;
            line = new int[3 * n + 3];
            for (int i = 0; i < n * 3; i += 3) {
                line[i] = pixel[0];
                line[i + 1] = pixel[1];
                line[i + 2] = pixel[2];
                if (err1 > 0) {
                    pixel[1] += yInc;
                    err1 -= dz2;
                }
                if (err2 > 0) {
                    pixel[0] += xInc;
                    err2 -= dz2;
                }
                err1 += dy2;
                err2 += dx2;
                pixel[2] += zInc;
            }
        }
        line[line.length - 3] = pixel[0];
        line[line.length - 2] = pixel[1];
        line[line.length - 1] = pixel[2];
        return line;
    }
}
