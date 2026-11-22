package hexxit.worldgen152;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.common.Tags;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One 1.5.2 world generator ({@link OldTerrainGen#registerWorldGenerator}, named like {@code "natura:trees"}) as a
 * feature of today, for the world type Hexxit Integrated (vanilla terrain of 26.3). This is the chunk bridge:
 *
 * <ul>
 * <li>The generator runs for the chunk the feature is placed in, on an {@link OldWorld} that shows the 3x3 chunks
 * around it as 1.5.2 ids ({@link OldLiveGen#oldCell}). 1.5.2 populated the area shifted 8 blocks into +x/+z of a
 * chunk; the old world is shifted by those 8 blocks, so the populated area is exactly this chunk and the generator
 * may reach 16 blocks to every side.</li>
 * <li>Heights are the same (sea level 63 in both). The old world is 256 high; {@code min_y}/{@code max_y} narrow
 * the window the generator sees and writes.</li>
 * <li>Biomes are 1.5.2 biomes chosen by tags ({@link OldBiomeTags}), so biomes of other mods get the content
 * that fits them.</li>
 * <li>Only changed blocks are written back. A block of today is replaced only where the bridge knows it: air,
 * replaceable plants, liquids, the stone/dirt/sand tags, logs and leaves, the blocks of the Hexxit mods. Blocks of
 * other mods, ores of today, bedrock and block entities stay.</li>
 * </ul>
 * In the world type Hexxit Legacy (the 1.5.2 chunk generators) the feature does nothing: there the generators run
 * inside the bit-exact population.
 */
public record OldGeneratorFeature(String generator, int minY, int maxY) implements Feature {
    public static final MapCodec<OldGeneratorFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("generator").forGetter(OldGeneratorFeature::generator),
            Codec.intRange(0, 255).optionalFieldOf("min_y", 0).forGetter(OldGeneratorFeature::minY),
            Codec.intRange(0, 255).optionalFieldOf("max_y", 255).forGetter(OldGeneratorFeature::maxY))
            .apply(instance, OldGeneratorFeature::new));

    private static final Logger LOGGER = LogUtils.getLogger();
    /** Old x/z = real x/z + SHIFT. */
    private static final int SHIFT = 8;
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();
    private static final Map<BlockState, Integer> CELLS = new ConcurrentHashMap<>();

    @Override
    public MapCodec<? extends Feature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (chunkGenerator instanceof OldChunkGenerator || chunkGenerator instanceof OldNetherChunkGenerator) {
            return false;
        }
        OldTerrainGen.WorldGenerator gen = OldTerrainGen.generator(this.generator);
        if (gen == null) {
            if (REPORTED.add(this.generator)) {
                LOGGER.warn("Unknown 1.5.2 world generator {} in a feature", this.generator);
            }
            return false;
        }
        ServerLevel serverLevel = level.getLevel();
        boolean nether = serverLevel.dimension() == Level.NETHER;
        if (!nether && serverLevel.dimension() != Level.OVERWORLD) {
            return false;
        }
        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());
        try {
            return new Run(level, chunkX, chunkZ, nether).run(gen);
        } catch (RuntimeException e) {
            if (REPORTED.add(this.generator + "!")) {
                LOGGER.error("1.5.2 world generator {} failed in chunk {} {}", this.generator, chunkX, chunkZ, e);
            }
            return false;
        }
    }

    private static int cell(BlockState state) {
        Integer cell = CELLS.get(state);
        if (cell == null) {
            cell = OldLiveGen.oldCell(state);
            CELLS.put(state, cell);
        }
        return cell;
    }

    /** Whether the bridge may put a block where {@code state} is (see the class comment). */
    static boolean replaceable(BlockState state) {
        if (state.isAir() || state.canBeReplaced()) {
            return true;
        }
        if (state.is(Blocks.BEDROCK) || state.hasBlockEntity()) {
            return false;
        }
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER) || state.is(Tags.Blocks.STONES)
                || state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(Tags.Blocks.GRAVELS)
                || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MYCELIUM) || state.is(Blocks.PODZOL)
                || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(Blocks.FARMLAND)
                || state.is(Blocks.SOUL_SAND) || state.is(Blocks.CACTUS) || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.SNOW) || state.is(Blocks.ICE) || state.is(Blocks.CLAY)
                || OldBlockStates.modCell(state) >= 0;
    }

    /** One run of a generator for one chunk. */
    private final class Run {
        private final WorldGenLevel level;
        private final int chunkX;
        private final int chunkZ;
        private final boolean nether;
        /** Real block area the generator may read and write: the 3x3 chunks around the chunk. */
        private final int minX;
        private final int minZ;
        private final int maxX;
        private final int maxZ;
        private final int lowY;
        private final int highY;
        /** Old chunks handed to the old world, with their cells before the generator ran (0..255 high). */
        private final Map<Long, char[]> before = new HashMap<>();

        Run(WorldGenLevel level, int chunkX, int chunkZ, boolean nether) {
            this.level = level;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.nether = nether;
            this.minX = (chunkX - 1) * 16;
            this.minZ = (chunkZ - 1) * 16;
            this.maxX = (chunkX + 2) * 16 - 1;
            this.maxZ = (chunkZ + 2) * 16 - 1;
            this.lowY = Math.max(OldGeneratorFeature.this.minY, level.getMinY());
            this.highY = Math.min(OldGeneratorFeature.this.maxY, level.getMaxY());
        }

        private boolean inArea(int x, int z) {
            return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
        }

        /** The cells of old chunk ocx/ocz, 256 high, index (x * 16 + z) * 256 + y. */
        private char[] read(int ocx, int ocz) {
            long key = (long) ocx << 32 | ocz & 0xFFFFFFFFL;
            char[] cells = this.before.get(key);
            if (cells != null) {
                return cells;
            }
            cells = new char[256 * 256];
            int baseX = ocx * 16 - SHIFT;
            int baseZ = ocz * 16 - SHIFT;
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    int x = baseX + lx;
                    int z = baseZ + lz;
                    int column = (lx * 16 + lz) * 256;
                    if (!this.inArea(x, z)) {
                        // outside what may be read: plain ground up to below sea level, nothing is written there
                        for (int y = 0; y < 62; y++) {
                            cells[column + y] = (char) (1 << 4);
                        }
                        continue;
                    }
                    ChunkAccess chunk = this.level.getChunk(x >> 4, z >> 4);
                    for (int y = this.lowY; y <= this.highY; y++) {
                        LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
                        if (section.hasOnlyAir()) {
                            y |= 15;
                            continue;
                        }
                        cells[column + y] = (char) cell(section.getBlockState(x & 15, y & 15, z & 15));
                    }
                }
            }
            this.before.put(key, cells);
            return cells;
        }

        private byte[] biomes(int ocx, int ocz) {
            byte[] biomes = new byte[256];
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    int x = Math.clamp(ocx * 16 - SHIFT + lx, this.minX, this.maxX);
                    int z = Math.clamp(ocz * 16 - SHIFT + lz, this.minZ, this.maxZ);
                    int y = this.nether ? 64 : this.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                    biomes[lx + lz * 16] = (byte) OldBiomeTags.oldId(this.level.getNoiseBiome(x >> 2, Math.clamp(y, this.level.getMinY(), this.level.getMaxY()) >> 2, z >> 2), this.nether);
                }
            }
            return biomes;
        }

        boolean run(OldTerrainGen.WorldGenerator gen) {
            OldWorld.TerrainSource terrain = new OldWorld.TerrainSource() {
                @Override
                public OldChunk chunk(int ocx, int ocz) {
                    char[] cells = Run.this.read(ocx, ocz);
                    char[] low = new char[256 * OldChunk.HEIGHT];
                    for (int column = 0; column < 256; column++) {
                        System.arraycopy(cells, column * 256, low, column * OldChunk.HEIGHT, OldChunk.HEIGHT);
                    }
                    return OldChunk.wide(ocx, ocz, low, Run.this.biomes(ocx, ocz));
                }

                @Override
                public char[] upper(int ocx, int ocz) {
                    char[] cells = Run.this.read(ocx, ocz);
                    char[] high = new char[256 * 128];
                    for (int column = 0; column < 256; column++) {
                        System.arraycopy(cells, column * 256 + 128, high, column * 128, 128);
                    }
                    return high;
                }
            };
            long seed = this.level.getSeed();
            OldWorld world = this.nether ? OldWorld.nether(seed, terrain) : new OldWorld(seed, terrain);
            world.beginChunk();
            // the random sequence of GameRegistry.generateWorld, separate for each generator
            long genSeed = seed ^ (long) OldGeneratorFeature.this.generator.hashCode() * 0x9E3779B97F4A7C15L;
            Random random = new Random(genSeed);
            long xMul = random.nextLong() >> 3;
            long zMul = random.nextLong() >> 3;
            random.setSeed(xMul * this.chunkX + zMul * this.chunkZ ^ genSeed);
            gen.generate(random, this.chunkX, this.chunkZ, world);
            return this.write(world);
        }

        private boolean write(OldWorld world) {
            List<BlockPos> changed = new ArrayList<>();
            List<int[]> cells = new ArrayList<>();
            for (Map.Entry<Long, char[]> entry : this.before.entrySet()) {
                int ocx = (int) (entry.getKey() >> 32);
                int ocz = (int) (long) entry.getKey();
                char[] old = entry.getValue();
                for (int lx = 0; lx < 16; lx++) {
                    for (int lz = 0; lz < 16; lz++) {
                        int ox = ocx * 16 + lx;
                        int oz = ocz * 16 + lz;
                        if (!this.inArea(ox - SHIFT, oz - SHIFT)) {
                            continue;
                        }
                        int column = (lx * 16 + lz) * 256;
                        for (int y = this.lowY; y <= this.highY && y < OldWorld.HEIGHT; y++) {
                            int id = world.getBlockId(ox, y, oz);
                            int cell = id << 4 | world.getBlockMetadata(ox, y, oz);
                            if (cell != old[column + y]) {
                                changed.add(new BlockPos(ox - SHIFT, y, oz - SHIFT));
                                cells.add(new int[] {ox, y, oz, cell});
                            }
                        }
                    }
                }
            }
            boolean any = false;
            BlockState air = Blocks.AIR.defaultBlockState();
            for (int i = 0; i < changed.size(); i++) {
                BlockPos at = changed.get(i);
                int[] c = cells.get(i);
                if (!replaceable(this.level.getBlockState(at))) {
                    continue;
                }
                int id = c[3] >> 4;
                BlockState state = id == 0 ? air : OldBlockStates.get(id, c[3] & 15);
                state = this.shape(world, state, c[0], c[1], c[2]);
                this.level.setBlock(at, state, Block.UPDATE_CLIENTS);
                any = true;
                ChunkAccess chunk = this.level.getChunk(at);
                if (id == 8 || id == 10 || id == 9 || id == 11 || OldChunkWriter.connects(state)) {
                    chunk.markPosForPostProcessing(at);
                }
                OldTile tile = world.getTile(c[0], c[1], c[2]);
                if (state.getBlock() instanceof EntityBlock) {
                    BlockEntity blockEntity = this.level.getBlockEntity(at);
                    if (blockEntity != null && tile != null) {
                        OldChunkWriter.fillTile(blockEntity, tile, this.level.registryAccess(), at);
                        blockEntity.setChanged();
                    }
                }
            }
            for (long key : this.before.keySet()) {
                for (OldEntity old : world.entities((int) (key >> 32), (int) key)) {
                    int x = (int) Math.floor(old.x()) - SHIFT;
                    int z = (int) Math.floor(old.z()) - SHIFT;
                    if (!this.inArea(x, z)) {
                        continue;
                    }
                    Entity entity = OldChunkWriter.entity(old, this.level.getLevel(), this.level.registryAccess(), -SHIFT, -SHIFT);
                    if (entity != null) {
                        if (!(entity instanceof HangingEntity)) {
                            entity.snapTo(old.x() - SHIFT, old.y(), old.z() - SHIFT,
                                    old instanceof OldEntity.Mob mob ? mob.yaw() : 0.0F, 0.0F);
                        }
                        this.level.addFreshEntity(entity);
                        any = true;
                    }
                }
            }
            return any;
        }

        /** The states that depend on neighbours, as the chunk writer sets them. */
        private BlockState shape(OldWorld world, BlockState state, int ox, int y, int oz) {
            if (state.getBlock() instanceof LeavesBlock) {
                return state.setValue(LeavesBlock.DISTANCE, leafDistance(world, ox, y, oz));
            }
            if (state.getBlock() instanceof VineBlock) {
                state = state.setValue(BlockStateProperties.UP, y + 1 < 256 && OldBlockInfo.holdsVines(world.getBlockId(ox, y + 1, oz)));
            }
            if (state.getBlock() instanceof DoorBlock) {
                state = OldLiveGen.door(world, state, ox, y, oz);
            }
            if (state.hasProperty(BlockStateProperties.SNOWY)) {
                state = state.setValue(BlockStateProperties.SNOWY, y + 1 < 256 && OldChunkWriter.isSnow(world.getBlockId(ox, y + 1, oz)));
            }
            return state;
        }
    }

    /** Steps through leaves to the nearest log, 7 when none is within 6 (LeavesBlock.DISTANCE). */
    private static int leafDistance(OldWorld world, int x, int y, int z) {
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        java.util.HashSet<Long> seen = new java.util.HashSet<>();
        queue.add(new int[] {x, y, z, 0});
        seen.add(BlockPos.asLong(x, y, z));
        int[][] steps = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        while (!queue.isEmpty()) {
            int[] at = queue.poll();
            if (at[3] >= 6) {
                continue;
            }
            for (int[] step : steps) {
                int nx = at[0] + step[0];
                int ny = at[1] + step[1];
                int nz = at[2] + step[2];
                if (ny < 0 || ny >= OldWorld.HEIGHT || !seen.add(BlockPos.asLong(nx, ny, nz))) {
                    continue;
                }
                int id = world.getBlockId(nx, ny, nz);
                if (OldBlockInfo.wood(id)) {
                    return at[3] + 1;
                }
                if (OldBlockInfo.leaves(id)) {
                    queue.add(new int[] {nx, ny, nz, at[3] + 1});
                }
            }
        }
        return 7;
    }
}
