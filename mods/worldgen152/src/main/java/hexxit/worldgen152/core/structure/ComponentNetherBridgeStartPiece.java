package hexxit.worldgen152.core.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ComponentNetherBridgeStartPiece extends ComponentNetherBridgeCrossing3 {
    /** The weight of the piece placed last (a piece without allowInRow may not follow itself). */
    public StructureNetherBridgePieceWeight theNetherBridgePieceWeight;
    public List<StructureNetherBridgePieceWeight> primaryWeights;
    public List<StructureNetherBridgePieceWeight> secondaryWeights;
    /** Pieces whose own children are still to be added. */
    public ArrayList field_74967_d = new ArrayList();

    public ComponentNetherBridgeStartPiece(Random var1, int var2, int var3) {
        super(var1, var2, var3);
        this.primaryWeights = new ArrayList<>();

        for (StructureNetherBridgePieceWeight var7 : StructureNetherBridgePieces.getPrimaryComponents()) {
            var7.instancesSpawned = 0;
            this.primaryWeights.add(var7);
        }

        this.secondaryWeights = new ArrayList<>();

        for (StructureNetherBridgePieceWeight var11 : StructureNetherBridgePieces.getSecondaryComponents()) {
            var11.instancesSpawned = 0;
            this.secondaryWeights.add(var11);
        }
    }
}
