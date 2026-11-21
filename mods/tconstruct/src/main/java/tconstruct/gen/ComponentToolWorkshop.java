package tconstruct.gen;

import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.structure.ComponentVillage;
import hexxit.worldgen152.core.structure.ComponentVillageStartPiece;
import hexxit.worldgen152.core.structure.OldStructures;
import hexxit.worldgen152.core.structure.StructureBoundingBox;
import hexxit.worldgen152.core.structure.StructureComponent;
import hexxit.worldgen152.core.structure.StructureVillagePieceWeight;

import java.util.List;
import java.util.Random;

/**
 * ComponentToolWorkshop and VillageToolStationHandler of Tinkers' Construct 1.4.3: a tinker's house with tool station,
 * part builder, stencil table, pattern chest, chest and one villager of profession 78943.
 */
public class ComponentToolWorkshop extends ComponentVillage {
    /** The villager profession Tinkers' Construct registered (VillagerRegistry.registerVillagerType). */
    public static final int VILLAGER = 78943;
    private int averageGroundLevel = -1;

    /** PatternChestLogic: 30 slots, empty slots are {@code null}. */
    public record PatternChest(OldStack[] slots) implements OldTile.Mod {
        public PatternChest() {
            this(new OldStack[30]);
        }
    }

    public ComponentToolWorkshop(ComponentVillageStartPiece par1ComponentVillageStartPiece, int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, int par5) {
        super(par1ComponentVillageStartPiece, par2);
        this.coordBaseMode = par5;
        this.boundingBox = par4StructureBoundingBox;
    }

    public static ComponentToolWorkshop buildComponent(ComponentVillageStartPiece villagePiece, List<StructureComponent> pieces, Random random, int p1, int p2, int p3, int p4, int p5) {
        StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p1, p2, p3, 0, 0, 0, 7, 6, 7, p4);
        return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(pieces, structureboundingbox) == null
                ? new ComponentToolWorkshop(villagePiece, p5, random, structureboundingbox, p4)
                : null;
    }

    @Override
    public boolean addComponentParts(OldWorld world, Random random, StructureBoundingBox sbb) {
        if (this.averageGroundLevel < 0) {
            this.averageGroundLevel = this.getAverageGroundLevel(world, sbb);
            if (this.averageGroundLevel < 0) {
                return true;
            }
            this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 4, 0);
        }

        this.fillWithBlocks(world, sbb, 0, 0, 0, 6, 0, 6, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(world, sbb, 0, 5, 0, 6, 5, 6, 85 /* fence */, 85 /* fence */, false);
        this.fillWithBlocks(world, sbb, 1, 0, 1, 5, 0, 5, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(world, sbb, 2, 0, 2, 4, 0, 4, 35 /* cloth */, 35 /* cloth */, false);
        this.fillWithBlocks(world, sbb, 0, 1, 0, 0, 4, 0, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(world, sbb, 0, 1, 6, 0, 4, 6, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(world, sbb, 6, 1, 0, 6, 4, 0, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(world, sbb, 6, 1, 6, 6, 4, 6, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(world, sbb, 0, 1, 1, 0, 1, 5, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(world, sbb, 1, 1, 0, 5, 1, 0, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(world, sbb, 6, 1, 1, 6, 1, 5, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(world, sbb, 1, 1, 6, 5, 1, 6, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(world, sbb, 0, 3, 1, 0, 3, 5, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(world, sbb, 1, 3, 0, 5, 3, 0, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(world, sbb, 6, 3, 1, 6, 3, 5, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(world, sbb, 1, 3, 6, 5, 3, 6, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(world, sbb, 0, 4, 1, 0, 4, 5, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(world, sbb, 1, 4, 0, 5, 4, 0, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(world, sbb, 6, 4, 1, 6, 4, 5, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(world, sbb, 1, 4, 6, 5, 4, 6, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(world, sbb, 1, 1, 1, 5, 5, 5, 0, 0, false);
        this.fillWithBlocks(world, sbb, 1, 4, 1, 5, 4, 5, 5 /* planks */, 5 /* planks */, false);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 1, 2, 0, sbb);
        this.placeBlockAtCurrentPosition(world, 5 /* planks */, 0, 2, 2, 0, sbb);
        this.placeDoorAtCurrentPosition(world, sbb, random, 3, 1, 0, this.getMetadataWithOffset(64 /* doorWood */, 1));
        this.placeBlockAtCurrentPosition(world, 5 /* planks */, 0, 4, 2, 0, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 5, 2, 0, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 1, 2, 6, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 2, 2, 6, sbb);
        this.placeBlockAtCurrentPosition(world, 5 /* planks */, 0, 3, 2, 6, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 4, 2, 6, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 5, 2, 6, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 0, 2, 1, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 0, 2, 2, sbb);
        this.placeBlockAtCurrentPosition(world, 5 /* planks */, 0, 0, 2, 3, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 0, 2, 4, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 0, 2, 5, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 6, 2, 1, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 6, 2, 2, sbb);
        this.placeBlockAtCurrentPosition(world, 5 /* planks */, 0, 6, 2, 3, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 6, 2, 4, sbb);
        this.placeBlockAtCurrentPosition(world, 102 /* thinGlass */, 0, 6, 2, 5, sbb);
        int i = this.getMetadataWithOffset(65 /* ladder */, 3);
        this.placeBlockAtCurrentPosition(world, 65 /* ladder */, i, 3, 1, 5, sbb);
        this.placeBlockAtCurrentPosition(world, 65 /* ladder */, i, 3, 2, 5, sbb);
        this.placeBlockAtCurrentPosition(world, 65 /* ladder */, i, 3, 3, 5, sbb);
        this.placeBlockAtCurrentPosition(world, 65 /* ladder */, i, 3, 4, 5, sbb);
        this.placeBlockAtCurrentPosition(world, TCWorldGen.TOOL_STATION_WOOD, 0, 1, 1, 1, sbb);
        this.generateStructurePatternChestContents(world, sbb, random, 1, 1, 2, TCWorldGen.TINKER_HOUSE_PATTERNS.items(random),
                TCWorldGen.TINKER_HOUSE_PATTERNS.count(random));
        this.placeBlockAtCurrentPosition(world, TCWorldGen.TOOL_STATION_WOOD, 1, 1, 1, 3, sbb);
        this.placeBlockAtCurrentPosition(world, 58 /* workbench */, 0, 1, 1, 4, sbb);
        this.placeBlockAtCurrentPosition(world, TCWorldGen.TOOL_STATION_WOOD, 10, 1, 1, 5, sbb);
        this.generateStructureChestContents(world, sbb, random, 4, 1, 5, TCWorldGen.TINKER_HOUSE_CHEST.items(random), TCWorldGen.TINKER_HOUSE_CHEST.count(random));
        i = this.getMetadataWithOffset(33 /* pistonBase */, 3);
        this.placeBlockAtCurrentPosition(world, 33 /* pistonBase */, i, 5, 1, 5, sbb);

        for (int l = 0; l < 6; l++) {
            for (int i1 = 0; i1 < 9; i1++) {
                this.clearCurrentPositionBlocksUpwards(world, i1, 9, l, sbb);
                this.fillCurrentPositionBlocksDownwards(world, 4 /* cobblestone */, 0, i1, -1, l, sbb);
            }
        }

        this.spawnVillagers(world, sbb, 3, 1, 3, 1);
        return true;
    }

    /** Puts a pattern chest (tool station metadata 5) there and fills its 30 slots. */
    protected boolean generateStructurePatternChestContents(OldWorld world, StructureBoundingBox sbb, Random random, int x, int y, int z,
                                                            OldLoot.Entry[] content, int count) {
        int posX = this.getXWithOffset(x, z);
        int posY = this.getYWithOffset(y);
        int posZ = this.getZWithOffset(x, z);
        if (sbb.isVecInside(posX, posY, posZ) && world.getBlockId(posX, posY, posZ) != 54 /* chest */) {
            world.setBlock(posX, posY, posZ, TCWorldGen.TOOL_STATION_WOOD, 5);
            PatternChest chest = new PatternChest();
            world.setTile(posX, posY, posZ, chest);
            OldLoot.generate(random, content, chest.slots(), count);
            return true;
        }
        return false;
    }

    @Override
    protected int getVillagerType(int par1) {
        return VILLAGER;
    }

    /** VillageToolStationHandler: weight 30, up to {@code i + random.nextInt(4)} per village. */
    static final class Handler implements OldStructures.VillageHandler {
        @Override
        public StructureVillagePieceWeight getVillagePieceWeight(Random random, int i) {
            return new StructureVillagePieceWeight(ComponentToolWorkshop.class, 30, i + random.nextInt(4));
        }

        @Override
        public Class<?> getComponentClass() {
            return ComponentToolWorkshop.class;
        }

        @Override
        public ComponentVillage buildComponent(StructureVillagePieceWeight weight, ComponentVillageStartPiece start, List<StructureComponent> pieces,
                                               Random random, int x, int y, int z, int direction, int type) {
            return ComponentToolWorkshop.buildComponent(start, pieces, random, x, y, z, direction, type);
        }
    }
}
