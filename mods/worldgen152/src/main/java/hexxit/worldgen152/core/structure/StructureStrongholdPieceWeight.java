package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

class StructureStrongholdPieceWeight {
    public Class pieceClass;
    public final int pieceWeight;
    public int instancesSpawned;
    public int instancesLimit;

    public StructureStrongholdPieceWeight(Class var1, int var2, int var3) {
        this.pieceClass = var1;
        this.pieceWeight = var2;
        this.instancesLimit = var3;
    }

    public boolean canSpawnMoreStructuresOfType(int var1) {
        return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
    }

    public boolean canSpawnMoreStructures() {
        return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
    }
}
