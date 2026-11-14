package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.Random;

public class ComponentVillageStartPiece extends ComponentVillageWell {
    public final OldStructures worldChunkMngr;
    public final boolean inDesert;
    public final OldBiome biome;
    public final int terrainType;
    public StructureVillagePieceWeight structVillagePieceWeight;
    public ArrayList<StructureVillagePieceWeight> structureVillageWeightedPieceList;
    public ArrayList field_74932_i = new ArrayList();
    public ArrayList field_74930_j = new ArrayList();

    public ComponentVillageStartPiece(
        OldStructures par1WorldChunkManager, int par2, Random par3Random, int par4, int par5, ArrayList par6ArrayList, int par7
    ) {
        super((ComponentVillageStartPiece)null, 0, par3Random, par4, par5);
        this.worldChunkMngr = par1WorldChunkManager;
        this.structureVillageWeightedPieceList = par6ArrayList;
        this.terrainType = par7;
        OldBiome biomegenbase = par1WorldChunkManager.getBiomeGenAt(par4, par5);
        this.inDesert = biomegenbase == OldBiomes.DESERT || biomegenbase == OldBiomes.DESERT_HILLS;
        this.biome = biomegenbase;
        this.startPiece = this;
    }

    public OldStructures getWorldChunkManager() {
        return this.worldChunkMngr;
    }
}
