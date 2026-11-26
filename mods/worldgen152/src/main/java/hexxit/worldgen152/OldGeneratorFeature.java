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
public record OldGeneratorFeature(String generator, int minY, int maxY, int yOffset) implements Feature {
    public static final MapCodec<OldGeneratorFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("generator").forGetter(OldGeneratorFeature::generator),
            Codec.intRange(0, 255).optionalFieldOf("min_y", 0).forGetter(OldGeneratorFeature::minY),
            Codec.intRange(0, 255).optionalFieldOf("max_y", 255).forGetter(OldGeneratorFeature::maxY),
            Codec.intRange(-128, 128).optionalFieldOf("y_offset", 0).forGetter(OldGeneratorFeature::yOffset))
            .apply(instance, OldGeneratorFeature::new));

    private static final Logger LOGGER = LogUtils.getLogger();
    /** Old x/z = real x/z + SHIFT. */
    private static final int SHIFT = 8;
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();
    private static final Map<BlockState, Integer> CELLS = new ConcurrentHashMap<>();
    /** See {@link #cell}. */
    private static volatile int[] cellsById = new int[0];

    @Override
    public MapCodec<? extends Feature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (chunkGenerator instanceof OldChunkGenerator || chunkGenerator instanceof OldNetherChunkGenerator) {
            return false;
        }
        if (!IntegratedConfig.enabled(this.generator)) {
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
            return new Run(level, chunkX, chunkZ, nether, SHIFT).run(gen);
        } catch (RuntimeException e) {
            if (REPORTED.add(this.generator + "!")) {
                LOGGER.error("1.5.2 world generator {} failed in chunk {} {}", this.generator, chunkX, chunkZ, e);
            }
            return false;
        }
    }

    /**
     * Runs {@code gen} on the 3x3 chunks around chunkX/chunkZ of the overworld with old and real coordinates the same
     * (pieces of today that a 1.5.2 component builds, like the village pieces of Tinkers' Construct); writes back as
     * the feature does. {@code name} seeds the random.
     */
    static boolean runLive(WorldGenLevel level, int chunkX, int chunkZ, String name, OldTerrainGen.WorldGenerator gen) {
        return new OldGeneratorFeature(name, 0, 255, 0).new Run(level, chunkX, chunkZ, false, 0).run(gen);
    }

    /**
     * {@link OldLiveGen#oldCell} of a block state, cached. The cache is indexed by the id of the state: a map keyed by
     * the state itself would hash it, and the hash of a block state is computed from its properties on every call.
     */
    static int cell(BlockState state) {
        int id = Block.getId(state);
        int[] known = cellsById;
        if (id >= 0 && id < known.length) {
            int cell = known[id];
            if (cell == 0) {
                // cell + 1 is stored, 0 means not known yet; two threads may both compute it, with the same result
                cell = OldLiveGen.oldCell(state) + 1;
                known[id] = cell;
            }
            return cell - 1;
        }
        if (known.length < Block.BLOCK_STATE_REGISTRY.size()) {
            synchronized (OldGeneratorFeature.class) {
                if (cellsById.length < Block.BLOCK_STATE_REGISTRY.size()) {
                    cellsById = java.util.Arrays.copyOf(cellsById, Block.BLOCK_STATE_REGISTRY.size());
                }
            }
            return cell(state);
        }
        // not a registered state
        Integer cell = CELLS.get(state);
        if (cell == null) {
            cell = OldLiveGen.oldCell(state);
            CELLS.put(state, cell);
        }
        return cell;
    }

    private static final Map<Block, BlockState> DEEPSLATE = new ConcurrentHashMap<>();

    /** A vanilla ore put into deepslate or tuff: its deepslate variant (minecraft:diamond_ore → deepslate_diamond_ore). */
    static BlockState deepslate(BlockState state) {
        BlockState variant = DEEPSLATE.computeIfAbsent(state.getBlock(), block -> {
            net.minecraft.resources.Identifier key = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
            if (!key.getNamespace().equals("minecraft") || !key.getPath().endsWith("_ore")) {
                return Blocks.AIR.defaultBlockState();
            }
            return net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    .getOptional(net.minecraft.resources.Identifier.withDefaultNamespace("deepslate_" + key.getPath()))
                    .map(Block::defaultBlockState).orElse(Blocks.AIR.defaultBlockState());
        });
        return variant.isAir() ? state : variant;
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

    private static final char[] EMPTY_SECTION = new char[4096];

    /** Index of the first cell from {@code from} on where the two sections differ, or -1. */
    private static int mismatch(char[] was, char[] now, int from) {
        int at = java.util.Arrays.mismatch(was, from, 4096, now, from, 4096);
        return at < 0 ? -1 : from + at;
    }

    /** The sections of one old chunk as they were read, before the generator ran. */
    private static final class Before {
        final char[][] sections = new char[16][];
    }

    /** One run of a generator for one chunk. */
    private final class Run {
        private final WorldGenLevel level;
        private final int chunkX;
        private final int chunkZ;
        private final boolean nether;
        /** Old x/z = real x/z + shift. */
        private final int shift;
        /** Real block area the generator may read and write: the 3x3 chunks around the chunk. */
        private final int minX;
        private final int minZ;
        private final int maxX;
        private final int maxZ;
        private final int lowY;
        private final int highY;
        /** Real y = old y + off. */
        private final int off;
        /**
         * Old chunks handed to the old world: the sections read so far, each as it was before the generator ran
         * ({@code null} for a section without blocks). Only these can have changed.
         */
        private final Map<Long, Before> before = new HashMap<>();

        Run(WorldGenLevel level, int chunkX, int chunkZ, boolean nether, int shift) {
            this.shift = shift;
            this.level = level;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.nether = nether;
            this.minX = (chunkX - 1) * 16;
            this.minZ = (chunkZ - 1) * 16;
            this.maxX = (chunkX + 2) * 16 - 1;
            this.maxZ = (chunkZ + 2) * 16 - 1;
            // old y = real y - offset; the window is in old heights
            this.off = OldGeneratorFeature.this.yOffset;
            this.lowY = Math.max(OldGeneratorFeature.this.minY, level.getMinY() - this.off);
            this.highY = Math.min(Math.min(OldGeneratorFeature.this.maxY, OldWorld.HEIGHT - 1), level.getMaxY() - this.off);
        }

        private boolean inArea(int x, int z) {
            return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
        }

        /**
         * The cells of old y 16 * index to 16 * index + 15 of old chunk ocx/ocz (index (x * 16 + z) * 16 + (y & 15)), or
         * {@code null} when there is no block; a copy stays in {@link #before}.
         */
        private char[] readSection(int ocx, int ocz, int index) {
            int y0 = index * 16;
            int yLow = Math.max(y0, this.lowY);
            int yHigh = Math.min(y0 + 15, this.highY);
            int baseX = ocx * 16 - this.shift;
            int baseZ = ocz * 16 - this.shift;
            char[] cells = null;
            BlockState lastState = null;
            int lastCell = 0;
            // the old chunk lies over up to 2x2 real chunks (shifted by 8); the area is whole real chunks
            for (int lx0 = 0; lx0 < 16; ) {
                int lx1 = Math.min(16, lx0 + 16 - (baseX + lx0 & 15));
                for (int lz0 = 0; lz0 < 16; ) {
                    int lz1 = Math.min(16, lz0 + 16 - (baseZ + lz0 & 15));
                    int x0 = baseX + lx0;
                    int z0 = baseZ + lz0;
                    if (!this.inArea(x0, z0)) {
                        // outside what may be read: plain ground up to below sea level, nothing is written there
                        for (int y = y0; y < Math.min(y0 + 16, 62); y++) {
                            if (cells == null) {
                                cells = new char[4096];
                            }
                            for (int lx = lx0; lx < lx1; lx++) {
                                for (int lz = lz0; lz < lz1; lz++) {
                                    cells[(lx * 16 + lz) * 16 + (y & 15)] = (char) (1 << 4);
                                }
                            }
                        }
                    } else if (yLow <= yHigh) {
                        ChunkAccess chunk = this.level.getChunk(x0 >> 4, z0 >> 4);
                        for (int y = yLow; y <= yHigh; y++) {
                            int ry = y + this.off;
                            LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(ry));
                            if (section.hasOnlyAir()) {
                                y += 15 - (ry & 15);
                                continue;
                            }
                            int sy = ry & 15;
                            for (int lz = lz0; lz < lz1; lz++) {
                                int sz = baseZ + lz & 15;
                                for (int lx = lx0; lx < lx1; lx++) {
                                    BlockState state = section.getBlockState(baseX + lx & 15, sy, sz);
                                    if (state != lastState) {
                                        lastState = state;
                                        lastCell = cell(state);
                                    }
                                    if (lastCell != 0) {
                                        if (cells == null) {
                                            cells = new char[4096];
                                        }
                                        cells[(lx * 16 + lz) * 16 + (y & 15)] = (char) lastCell;
                                    }
                                }
                            }
                        }
                    }
                    lz0 = lz1;
                }
                lx0 = lx1;
            }
            this.before.get((long) ocx << 32 | ocz & 0xFFFFFFFFL).sections[index] = cells == null ? null : cells.clone();
            return cells;
        }

        private byte[] biomes(int ocx, int ocz) {
            byte[] biomes = new byte[256];
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    int x = Math.clamp(ocx * 16 - this.shift + lx, this.minX, this.maxX);
                    int z = Math.clamp(ocz * 16 - this.shift + lz, this.minZ, this.maxZ);
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
                    throw new IllegalStateException("read by sections");
                }

                @Override
                public OldWorld.Sections sections(int ocx, int ocz) {
                    Run.this.before.put((long) ocx << 32 | ocz & 0xFFFFFFFFL, new Before());
                    return new OldWorld.Sections() {
                        @Override
                        public byte[] biomes() {
                            return Run.this.biomes(ocx, ocz);
                        }

                        @Override
                        public char[] section(int index) {
                            return Run.this.readSection(ocx, ocz, index);
                        }
                    };
                }
            };
            long seed = this.level.getSeed();
            OldWorld world = this.nether ? OldWorld.nether(seed, terrain) : new OldWorld(seed, terrain);
            world.beginChunk();
            world.setRealOffset(-this.shift, this.off, -this.shift);
            world.setWritableArea(this.minX + this.shift, this.minZ + this.shift, this.maxX + this.shift, this.maxZ + this.shift);
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
            for (Map.Entry<Long, Before> entry : this.before.entrySet()) {
                int ocx = (int) (entry.getKey() >> 32);
                int ocz = (int) (long) entry.getKey();
                char[][] old = entry.getValue().sections;
                for (int index = 0; index < 16; index++) {
                    char[] now = world.sectionNow(ocx, ocz, index);
                    if (now == null) {
                        // never read, or read without blocks and still without
                        continue;
                    }
                    char[] was = old[index] != null ? old[index] : EMPTY_SECTION;
                    int at = 0;
                    while ((at = mismatch(was, now, at)) >= 0) {
                        int lx = at >> 8;
                        int lz = at >> 4 & 15;
                        int y = index * 16 + (at & 15);
                        int ox = ocx * 16 + lx;
                        int oz = ocz * 16 + lz;
                        if (y >= this.lowY && y <= this.highY && this.inArea(ox - this.shift, oz - this.shift)) {
                            changed.add(new BlockPos(ox - this.shift, y + this.off, oz - this.shift));
                            cells.add(new int[] {ox, y, oz, now[at]});
                        }
                        at++;
                    }
                }
            }
            boolean any = false;
            BlockState air = Blocks.AIR.defaultBlockState();
            for (int i = 0; i < changed.size(); i++) {
                BlockPos at = changed.get(i);
                int[] c = cells.get(i);
                BlockState real = this.level.getBlockState(at);
                if (!replaceable(real)) {
                    continue;
                }
                int id = c[3] >> 4;
                BlockState state = id == 0 ? air : OldBlockStates.get(id, c[3] & 15);
                if (real.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) {
                    state = deepslate(state);
                }
                state = shape(world, state, c[0], c[1], c[2]);
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
                    int x = (int) Math.floor(old.x()) - this.shift;
                    int z = (int) Math.floor(old.z()) - this.shift;
                    if (!this.inArea(x, z)) {
                        continue;
                    }
                    Entity entity = OldChunkWriter.entity(old, this.level.getLevel(), this.level.registryAccess(), -this.shift, -this.shift);
                    if (entity != null) {
                        if (!(entity instanceof HangingEntity)) {
                            entity.snapTo(old.x() - this.shift, old.y() + this.off, old.z() - this.shift,
                                    old instanceof OldEntity.Mob mob ? mob.yaw() : 0.0F, 0.0F);
                        }
                        this.level.addFreshEntity(entity);
                        any = true;
                    }
                }
            }
            return any;
        }

    }

    /** The states that depend on neighbours, as the chunk writer sets them. */
    static BlockState shape(OldWorld world, BlockState state, int ox, int y, int oz) {
        if (state.getBlock() instanceof LeavesBlock) {
            return state.setValue(LeavesBlock.DISTANCE, leafDistance(world, ox, y, oz));
        }
        if (state.getBlock() instanceof VineBlock) {
            state = state.setValue(BlockStateProperties.UP, y + 1 < 256 && OldBlockInfo.holdsVines(world.getBlockId(ox, y + 1, oz)));
        }
        if (state.getBlock() instanceof DoorBlock) {
            state = OldLiveGen.door(world, state, ox, y, oz);
        }
        state = OldLiveGen.chest(state, ox, y, oz, (cx, cy, cz) -> cy < 0 || cy >= 256 ? null
                : OldBlockStates.get(world.getBlockId(cx, cy, cz), world.getBlockMetadata(cx, cy, cz)));
        if (state.hasProperty(BlockStateProperties.SNOWY)) {
            state = state.setValue(BlockStateProperties.SNOWY, y + 1 < 256 && OldChunkWriter.isSnow(world.getBlockId(ox, y + 1, oz)));
        }
        return state;
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
