package natura.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldTerrainGen;

/**
 * The world generation of Natura 2.1.5.1 in the 1.5.2 core, without game classes so that tools/expect.sh can run
 * it. The mod class calls {@link #register()} once; the block states for the ids come from the mod class.
 */
public final class NaturaWorldGen {
    /** FML mod id of the original, for the generator order ({@code OldTerrainGen.LOAD_ORDER}). */
    public static final String FML_ID = "Natura";

    private NaturaWorldGen() {
    }

    public static void register() {
        // ---- what the generators set (and the saplings and the small glowshroom they ask about) ----
        // BerryBush / NetherBerryBush: BlockLeavesBase with Material.leaves, isOpaqueCube false, not isLeaves
        OldBlockInfo.register(NaturaIds.BERRY_BUSH, Material.LEAVES, false, 0, false);
        OldBlockInfo.register(NaturaIds.NETHER_BERRY_BUSH, Material.LEAVES, false, 0, false);
        // logs: BlockLog (TreeBlock, WillowBlock, OverworldTreeBlock), SimpleLog (redwood), DarkTreeBlock and
        // LogTwoxTwo (bloodwood): Material.wood, opaque cubes, isWood
        OldBlockInfo.registerLog(NaturaIds.TREE);
        OldBlockInfo.registerLog(NaturaIds.REDWOOD);
        OldBlockInfo.registerLog(NaturaIds.WILLOW);
        OldBlockInfo.registerLog(NaturaIds.RARE_TREE);
        OldBlockInfo.registerLog(NaturaIds.DARK_TREE);
        OldBlockInfo.registerLog(NaturaIds.BLOODWOOD);
        // NLeaves and its subclasses: BlockLeaves, light opacity 1
        OldBlockInfo.registerLeaves(NaturaIds.FLORA_LEAVES);
        OldBlockInfo.registerLeaves(NaturaIds.FLORA_LEAVES_NO_COLOR);
        OldBlockInfo.registerLeaves(NaturaIds.RARE_LEAVES);
        OldBlockInfo.registerLeaves(NaturaIds.DARK_LEAVES);
        // BlockCrops, BlockFlower, BlockSapling, BlockMushroom: Material.plants
        OldBlockInfo.register(NaturaIds.CROPS, Material.PLANTS, false, 0, false);
        OldBlockInfo.register(NaturaIds.BLUEBELLS, Material.PLANTS, false, 0, false);
        OldBlockInfo.register(NaturaIds.FLORA_SAPLING, Material.PLANTS, false, 0, false);
        OldBlockInfo.register(NaturaIds.RARE_SAPLING, Material.PLANTS, false, 0, false);
        OldBlockInfo.register(NaturaIds.GLOWSHROOM, Material.PLANTS, false, 0, false);
        // SaguaroBlock: Material.cactus, not an opaque cube
        OldBlockInfo.register(NaturaIds.SAGUARO, Material.CACTUS, false, 0, false);
        // CloudBlock: CloudMaterial (not solid, does not block movement, not liquid, not replaceable), which
        // has the properties of Material.plants for the generators
        OldBlockInfo.register(NaturaIds.CLOUD, Material.PLANTS, false, 0, false);
        // ThornVines: BlockVine
        OldBlockInfo.register(NaturaIds.THORN_VINES, Material.VINE, false, 0, false);
        // LargeGlowshroom: Material.wood, not an opaque cube
        OldBlockInfo.register(NaturaIds.GLOWSHROOM_BLUE, Material.WOOD, false, 0, false);
        OldBlockInfo.register(NaturaIds.GLOWSHROOM_GREEN, Material.WOOD, false, 0, false);
        OldBlockInfo.register(NaturaIds.GLOWSHROOM_PURPLE, Material.WOOD, false, 0, false);
        // NPlanks (Material.wood), HeatSand (Material.sand), TaintedSoil (Material.ground): plain cubes
        OldBlockInfo.registerCube(NaturaIds.PLANKS, Material.WOOD);
        OldBlockInfo.registerCube(NaturaIds.HEAT_SAND, Material.SAND);
        OldBlockInfo.registerCube(NaturaIds.TAINTED_SOIL, Material.GROUND);

        // ---- GameRegistry.registerWorldGenerator, in the order of Natura.init ----
        OldTerrainGen.registerWorldGenerator(FML_ID, "natura:crops", BaseCropWorldgen::generate);
        OldTerrainGen.registerWorldGenerator(FML_ID, "natura:clouds", BaseCloudWorldgen::generate);
        OldTerrainGen.registerWorldGenerator(FML_ID, "natura:trees", BaseTreeWorldgen::generate);
    }
}
