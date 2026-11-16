package natura.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldWorld;

/**
 * What the Natura generators share: the 1.5.2 {@code WorldGenerator} helpers and the questions they asked
 * blocks, answered from {@link OldBlockInfo}.
 */
public abstract class NaturaGen extends OldGen {
    static final int GRASS = 2;
    static final int DIRT = 3;
    static final int BEDROCK = 7;
    static final int WATER_MOVING = 8;
    static final int WATER_STILL = 9;
    static final int SAND = 12;
    static final int NETHERRACK = 87;
    static final int SLOW_SAND = 88;
    static final int MYCELIUM = 110;

    /**
     * WorldGenerator.doBlockNotify: the original set blocks with neighbour updates (flag 3) when true, as the
     * saplings do. The 1.5.2 world of the core has no neighbour updates; the flag is kept for the record.
     */
    protected final boolean doBlockNotify;

    protected NaturaGen() {
        this(false);
    }

    protected NaturaGen(boolean notify) {
        this.doBlockNotify = notify;
    }

    /** WorldGenerator.setBlockAndMetadata. */
    protected void setBlockAndMetadata(OldWorld world, int x, int y, int z, int id, int meta) {
        world.setBlock(x, y, z, id, meta);
    }

    /** WorldGenerator.setBlock. */
    protected void setBlock(OldWorld world, int x, int y, int z, int id) {
        this.setBlockAndMetadata(world, x, y, z, id, 0);
    }

    /**
     * {@code Block.opaqueCubeLookup}. In 1.5.2 it was true for leaves: {@code BlockLeaves.isOpaqueCube()}
     * returned {@code !graphicsLevel}, and the lookup was filled in the Block constructor, before that field
     * was set (the same holds for the leaves of ExtrabiomesXL and Natura, which ask the vanilla leaves or
     * extend them). {@link OldBlockInfo} lists leaves as see-through, so they are added here.
     */
    static boolean opaqueCube(int id) {
        return OldBlockInfo.opaqueCube(id) || OldBlockInfo.leaves(id);
    }

    /** {@code block != null && block.isLeaves(...)}. */
    static boolean isLeaves(int id) {
        return id != 0 && OldBlockInfo.leaves(id);
    }

    /** {@code block != null && block.isWood(...)}. */
    static boolean isWood(int id) {
        return id != 0 && OldBlockInfo.wood(id);
    }

    /** {@code block == null || block.canBeReplacedByLeaves(...)}, which is {@code !opaqueCubeLookup[id]}. */
    static boolean replaceable(int id) {
        return id == 0 || !opaqueCube(id);
    }

    /** Netherrack, soul sand or tainted soil: the ground of the Nether trees. */
    static boolean netherSoil(int id) {
        return id == NETHERRACK || id == SLOW_SAND || id == NaturaIds.TAINTED_SOIL;
    }

    /**
     * {@code soil != null && soil.canSustainPlant(..., sapling)} for the two Natura saplings: both name grass,
     * dirt, soul sand, netherrack and tainted soil in {@code canThisPlantGrowOnThisBlockID}; their plant types
     * (Plains or Nether) add nothing to that list.
     */
    static boolean sustainsSapling(int id) {
        return id == GRASS || id == DIRT || netherSoil(id);
    }
}
