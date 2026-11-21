package twilightforest.gen.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.structure.StructureComponent;
import hexxit.worldgen152.core.structure.StructureStart;
import twilightforest.gen.TFBiomes;
import twilightforest.gen.TFFeature;
import twilightforest.gen.TFLayout;
import twilightforest.gen.TFTerrain;

import java.util.Random;

/**
 * StructureTFMajorFeatureStart: the first piece of the feature of a feature chunk, and everything it builds on. The
 * pieces do not look at the world while they are laid out, so a start needs only the biome layout.
 *
 * <p>Teil 24d: hollow hills, hedge maze, naga courtyard, quest grove, lich tower, labyrinth. The hydra lair, the dark
 * tower and the knight stronghold (Teil 24e) have no first piece yet: their starts stay empty.
 */
public class StructureTFMajorFeatureStart extends StructureStart {
    public final TFFeature feature;

    public StructureTFMajorFeatureStart(TFLayout layout, Random rand, int chunkX, int chunkZ) {
        // StructureStrongholdPieces.prepareStructurePieces: only resets the weights of the vanilla stronghold
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        int y = TFTerrain.SEALEVEL + 1;
        this.feature = TFFeature.getFeatureDirectlyAt(chunkX, chunkZ, layout);
        StructureComponent firstComponent = this.makeFirstComponent(rand, this.feature, x, y, z);
        if (firstComponent != null) {
            this.components.add(firstComponent);
            firstComponent.buildComponent(firstComponent, this.components, rand);
        }

        this.updateBoundingBox();
        // ComponentStrongholdStairs2 (the vanilla stronghold as first piece) is never made by makeFirstComponent

        if (firstComponent instanceof ComponentTFTowerMain) {
            this.moveToAvgGroundLevel(layout, x, z);
        }
    }

    public StructureComponent makeFirstComponent(Random rand, TFFeature feature, int x, int y, int z) {
        if (feature == TFFeature.NAGA_LAIR) {
            return new ComponentTFNagaCourtyard(null, rand, 0, x, y, z);
        } else if (feature == TFFeature.HEDGE_MAZE) {
            return new ComponentTFHedgeMaze(null, rand, 0, x, y, z);
        } else if (feature == TFFeature.HILL1) {
            return new ComponentTFHollowHill(null, rand, 0, 1, x, y, z);
        } else if (feature == TFFeature.HILL2) {
            return new ComponentTFHollowHill(null, rand, 0, 2, x, y, z);
        } else if (feature == TFFeature.HILL3) {
            return new ComponentTFHollowHill(null, rand, 0, 3, x, y, z);
        } else if (feature == TFFeature.WIZARD_TOWER) {
            return new ComponentTFTowerMain(null, rand, 0, x, y, z);
        } else if (feature == TFFeature.QUEST_GROVE) {
            return new ComponentTFQuestGrove(null, rand, 0, x, y, z);
        } else if (feature == TFFeature.LABYRINTH) {
            return new ComponentTFMazeRuins(null, rand, 0, x, y, z);
        }
        // hydraLair, darkTower, tfStronghold: Teil 24e
        return null;
    }

    @Override
    public boolean isSizeableStructure() {
        return this.feature.structureEnabled;
    }

    protected void moveToAvgGroundLevel(TFLayout layout, int x, int z) {
        OldBiome biomeAt = layout.biomeAt(x, z);
        int offY = (int) ((biomeAt.minHeight + biomeAt.maxHeight) * 8.0F);
        if (biomeAt == TFBiomes.DARK_FOREST) {
            offY += 4;
        }

        if (offY > 0) {
            this.boundingBox.offset(0, offY, 0);

            for (StructureComponent com : this.getComponents()) {
                com.getBoundingBox().offset(0, offY, 0);
            }
        }
    }
}
