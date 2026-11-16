package hexxit.worldgen152.core.structure;

class StructureNetherBridgePieceWeight {
    public Class weightClass;
    public final int weight;
    public int instancesSpawned;
    public int instancesLimit;
    /** Whether the piece may follow a piece of its own kind. */
    public boolean allowInRow;

    public StructureNetherBridgePieceWeight(Class var1, int var2, int var3, boolean var4) {
        this.weightClass = var1;
        this.weight = var2;
        this.instancesLimit = var3;
        this.allowInRow = var4;
    }

    public StructureNetherBridgePieceWeight(Class var1, int var2, int var3) {
        this(var1, var2, var3, false);
    }

    public boolean canSpawnMoreStructuresOfType(int var1) {
        return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
    }

    public boolean canSpawnMoreStructures() {
        return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
    }
}
