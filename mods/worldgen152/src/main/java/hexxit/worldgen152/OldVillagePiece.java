package hexxit.worldgen152;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.structure.ComponentVillage;
import hexxit.worldgen152.core.structure.ComponentVillageStartPiece;
import hexxit.worldgen152.core.structure.OldStructures;
import hexxit.worldgen152.core.structure.StructureBoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.core.Vec3i;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A 1.5.2 village component ({@link OldStructures#VILLAGE_PIECES}) as a house of the villages of today: a jigsaw
 * pool element with one entrance in the middle of its door side, which the streets of a village attach to like their
 * own houses. When a chunk of it is decorated, the component builds the part inside that chunk (its own bounding box
 * logic), on the real blocks around it ({@link OldGeneratorFeature#runLive}).
 *
 * <p>At server start every piece is added to the house pools of the five vanilla villages
 * ({@code minecraft:village/<type>/houses}) with its share of the pool's weight. Pools of other mods are left alone.
 */
public final class OldVillagePiece extends StructurePoolElement {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<OldVillagePiece> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("piece").forGetter(p -> p.name),
            projectionCodec())
            .apply(instance, OldVillagePiece::new));

    static Supplier<StructurePoolElementType<OldVillagePiece>> TYPE;

    private static final Identifier ENTRANCE = Identifier.withDefaultNamespace("building_entrance");
    private static final ResourceKey<StructureTemplatePool> EMPTY = ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.withDefaultNamespace("empty"));
    private static final List<String> VILLAGES = List.of("plains", "desert", "savanna", "snowy", "taiga");

    private final String name;

    OldVillagePiece(String name, StructureTemplatePool.Projection projection) {
        super(projection);
        this.name = name;
    }

    private OldStructures.VillagePiece piece() {
        OldStructures.VillagePiece piece = OldStructures.VILLAGE_PIECES.get(this.name);
        if (piece == null) {
            throw new IllegalStateException("Unknown 1.5.2 village piece " + this.name);
        }
        return piece;
    }

    /** Template x/z (orientation 0) to the offset from the piece's origin, as StructureTemplate.transform with pivot 0. */
    private static BlockPos rotate(int x, int y, int z, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> new BlockPos(-z, y, x);
            case CLOCKWISE_180 -> new BlockPos(-x, y, -z);
            case COUNTERCLOCKWISE_90 -> new BlockPos(z, y, -x);
            default -> new BlockPos(x, y, z);
        };
    }

    @Override
    public Vec3i getSize(StructureTemplateManager manager, Rotation rotation) {
        OldStructures.VillagePiece p = this.piece();
        int height = p.height() + p.floor();
        return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90
                ? new Vec3i(p.depth(), height, p.width()) : new Vec3i(p.width(), height, p.depth());
    }

    @Override
    public List<StructureTemplate.JigsawBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager manager, BlockPos pos, Rotation rotation,
                                                                          RandomSource random) {
        OldStructures.VillagePiece p = this.piece();
        BlockPos at = pos.offset(rotate(p.width() / 2, 0, 0, rotation));
        Direction front = rotation.rotate(Direction.NORTH);
        return List.of(new StructureTemplate.JigsawBlockInfo(at,
                Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, FrontAndTop.fromFrontAndTop(front, Direction.UP)),
                JigsawBlockEntity.JointType.ALIGNED, ENTRANCE, EMPTY, ENTRANCE, 0, 0));
    }

    @Override
    public BoundingBox getBoundingBox(StructureTemplateManager manager, BlockPos pos, Rotation rotation) {
        OldStructures.VillagePiece p = this.piece();
        BlockPos a = pos.offset(rotate(0, 0, 0, rotation));
        BlockPos b = pos.offset(rotate(p.width() - 1, p.height() + p.floor() - 1, p.depth() - 1, rotation));
        return BoundingBox.fromCorners(a, b);
    }

    @Override
    public boolean place(StructureTemplateManager manager, WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                         BlockPos pos, BlockPos referencePos, Rotation rotation, BoundingBox chunkBox, RandomSource random,
                         LiquidSettings liquidSettings, boolean keepJigsaws) {
        OldStructures.VillagePiece p = this.piece();
        BoundingBox box = this.getBoundingBox(manager, pos, rotation);
        int mode = switch (rotation) {
            case CLOCKWISE_90 -> 1;
            case CLOCKWISE_180 -> 2;
            case COUNTERCLOCKWISE_90 -> 3;
            default -> 0;
        };
        int minY = box.minY() + p.floor();
        StructureBoundingBox oldBox = new StructureBoundingBox(box.minX(), minY, box.minZ(), box.maxX(), minY + p.height() - 1, box.maxZ());
        BlockPos center = box.getCenter();
        int biome = OldBiomeTags.oldId(level.getNoiseBiome(QuartPos.fromBlock(center.getX()), QuartPos.fromBlock(box.minY()),
                QuartPos.fromBlock(center.getZ())), false);
        ComponentVillage component = p.factory().create(new ComponentVillageStartPiece(OldBiomes.byId(biome)), oldBox, mode);
        StructureBoundingBox sbb = new StructureBoundingBox(chunkBox.minX(), chunkBox.minY(), chunkBox.minZ(), chunkBox.maxX(), chunkBox.maxY(),
                chunkBox.maxZ());
        int chunkX = chunkBox.minX() >> 4;
        int chunkZ = chunkBox.minZ() >> 4;
        OldGeneratorFeature.runLive(level, chunkX, chunkZ, this.name + "@" + box.minX() + "," + box.minZ(),
                (r, cx, cz, world) -> component.addComponentParts(world, r, sbb));
        return true;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return TYPE.get();
    }

    @Override
    public String toString() {
        return "OldVillagePiece[" + this.name + "]";
    }

    /** Adds every village piece to the house pools of the vanilla villages (once per server). */
    @SuppressWarnings("unchecked")
    static void addToVillages(MinecraftServer server) {
        if (OldStructures.VILLAGE_PIECES.isEmpty()) {
            return;
        }
        Registry<StructureTemplatePool> pools = server.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        try {
            java.lang.reflect.Field raw = StructureTemplatePool.class.getDeclaredField("rawTemplates");
            java.lang.reflect.Field templates = StructureTemplatePool.class.getDeclaredField("templates");
            raw.setAccessible(true);
            templates.setAccessible(true);
            for (String village : VILLAGES) {
                StructureTemplatePool pool = pools.getValue(Identifier.withDefaultNamespace("village/" + village + "/houses"));
                if (pool == null) {
                    continue;
                }
                List<Pair<StructurePoolElement, Integer>> entries = new ArrayList<>((List<Pair<StructurePoolElement, Integer>>) raw.get(pool));
                if (entries.stream().anyMatch(e -> e.getFirst() instanceof OldVillagePiece)) {
                    continue;
                }
                int total = entries.stream().mapToInt(Pair::getSecond).sum();
                it.unimi.dsi.fastutil.objects.ObjectArrayList<StructurePoolElement> list =
                        (it.unimi.dsi.fastutil.objects.ObjectArrayList<StructurePoolElement>) templates.get(pool);
                for (Map.Entry<String, OldStructures.VillagePiece> piece : OldStructures.VILLAGE_PIECES.entrySet()) {
                    int weight = Math.max(1, total * piece.getValue().weightPercent() / 100);
                    OldVillagePiece element = new OldVillagePiece(piece.getKey(), StructureTemplatePool.Projection.RIGID);
                    entries.add(Pair.of(element, weight));
                    for (int i = 0; i < weight; i++) {
                        list.add(element);
                    }
                }
                raw.set(pool, List.copyOf(entries));
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Could not add the 1.5.2 village pieces to the village pools", e);
        }
    }
}
