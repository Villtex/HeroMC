package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StructureStrongholdPieces {
    private static final StructureStrongholdPieceWeight[] pieceWeightArray = new StructureStrongholdPieceWeight[]{
        new StructureStrongholdPieceWeight(ComponentStrongholdStraight.class, 40, 0),
        new StructureStrongholdPieceWeight(ComponentStrongholdPrison.class, 5, 5),
        new StructureStrongholdPieceWeight(ComponentStrongholdLeftTurn.class, 20, 0),
        new StructureStrongholdPieceWeight(ComponentStrongholdRightTurn.class, 20, 0),
        new StructureStrongholdPieceWeight(ComponentStrongholdRoomCrossing.class, 10, 6),
        new StructureStrongholdPieceWeight(ComponentStrongholdStairsStraight.class, 5, 5),
        new StructureStrongholdPieceWeight(ComponentStrongholdStairs.class, 5, 5),
        new StructureStrongholdPieceWeight(ComponentStrongholdCrossing.class, 5, 4),
        new StructureStrongholdPieceWeight(ComponentStrongholdChestCorridor.class, 5, 4),
        new StructureStrongholdPieceWeight(ComponentStrongholdLibrary.class, 10, 2) {
            @Override
            public boolean canSpawnMoreStructuresOfType(int var1) {
                return super.canSpawnMoreStructuresOfType(var1) && var1 > 4;
            }
        },
        new StructureStrongholdPieceWeight(ComponentStrongholdPortalRoom.class, 20, 1) {
            @Override
            public boolean canSpawnMoreStructuresOfType(int var1) {
                return super.canSpawnMoreStructuresOfType(var1) && var1 > 5;
            }
        }
    };
    private static List<StructureStrongholdPieceWeight> structurePieceList;
    private static Class strongComponentType;
    static int totalWeight = 0;
    private static final StructureStrongholdStones strongholdStones = new StructureStrongholdStones();

    static void setComponentType(Class var0) {
        strongComponentType = var0;
    }

    static StructureStrongholdStones getStrongholdStones() {
        return strongholdStones;
    }

    public static void prepareStructurePieces() {
        structurePieceList = new ArrayList();

        for (StructureStrongholdPieceWeight var3 : pieceWeightArray) {
            var3.instancesSpawned = 0;
            structurePieceList.add(var3);
        }

        strongComponentType = null;
    }

    private static boolean canAddStructurePieces() {
        boolean var0 = false;
        totalWeight = 0;

        for (StructureStrongholdPieceWeight var2 : structurePieceList) {
            if (var2.instancesLimit > 0 && var2.instancesSpawned < var2.instancesLimit) {
                var0 = true;
            }

            totalWeight = totalWeight + var2.pieceWeight;
        }

        return var0;
    }

    private static ComponentStronghold getStrongholdComponentFromWeightedPiece(Class var0, List var1, Random var2, int var3, int var4, int var5, int var6, int var7) {
        ComponentStronghold var8 = null;
        if (var0 == ComponentStrongholdStraight.class) {
            var8 = ComponentStrongholdStraight.findValidPlacement(var1, var2, var3, var4, var5, var6, var7);
        } else if (var0 == ComponentStrongholdPrison.class) {
            var8 = ComponentStrongholdPrison.findValidPlacement(var1, var2, var3, var4, var5, var6, var7);
        } else if (var0 == ComponentStrongholdLeftTurn.class) {
            var8 = ComponentStrongholdLeftTurn.findValidPlacement(var1, var2, var3, var4, var5, var6, var7);
        } else if (var0 == ComponentStrongholdRightTurn.class) {
            var8 = ComponentStrongholdRightTurn.findValidPlacement(var1, var2, var3, var4, var5, var6, var7);
        } else if (var0 == ComponentStrongholdRoomCrossing.class) {
            var8 = ComponentStrongholdRoomCrossing.findValidPlacement(var1, var2, var3, var4, var5, var6, var7);
        } else if (var0 == ComponentStrongholdStairsStraight.class) {
            var8 = ComponentStrongholdStairsStraight.findValidPlacement(var1, var2, var3, var4, var5, var6, var7);
        } else if (var0 == ComponentStrongholdStairs.class) {
            var8 = ComponentStrongholdStairs.getStrongholdStairsComponent(var1, var2, var3, var4, var5, var6, var7);
        } else if (var0 == ComponentStrongholdCrossing.class) {
            var8 = ComponentStrongholdCrossing.findValidPlacement(var1, var2, var3, var4, var5, var6, var7);
        } else if (var0 == ComponentStrongholdChestCorridor.class) {
            var8 = ComponentStrongholdChestCorridor.findValidPlacement(var1, var2, var3, var4, var5, var6, var7);
        } else if (var0 == ComponentStrongholdLibrary.class) {
            var8 = ComponentStrongholdLibrary.findValidPlacement(var1, var2, var3, var4, var5, var6, var7);
        } else if (var0 == ComponentStrongholdPortalRoom.class) {
            var8 = ComponentStrongholdPortalRoom.findValidPlacement(var1, var2, var3, var4, var5, var6, var7);
        }

        return var8;
    }

    private static ComponentStronghold getNextComponent(ComponentStrongholdStairs2 var0, List var1, Random var2, int var3, int var4, int var5, int var6, int var7) {
        if (!canAddStructurePieces()) {
            return null;
        }

        if (strongComponentType != null) {
            ComponentStronghold var8 = getStrongholdComponentFromWeightedPiece(strongComponentType, var1, var2, var3, var4, var5, var6, var7);
            strongComponentType = null;
            if (var8 != null) {
                return var8;
            }
        }

        int var13 = 0;

        while (var13 < 5) {
            var13++;
            int var9 = var2.nextInt(totalWeight);

            for (StructureStrongholdPieceWeight var11 : structurePieceList) {
                var9 -= var11.pieceWeight;
                if (var9 < 0) {
                    if (!var11.canSpawnMoreStructuresOfType(var7) || var11 == var0.strongholdPieceWeight) {
                        break;
                    }

                    ComponentStronghold var12 = getStrongholdComponentFromWeightedPiece(var11.pieceClass, var1, var2, var3, var4, var5, var6, var7);
                    if (var12 != null) {
                        var11.instancesSpawned++;
                        var0.strongholdPieceWeight = var11;
                        if (!var11.canSpawnMoreStructures()) {
                            structurePieceList.remove(var11);
                        }

                        return var12;
                    }
                }
            }
        }

        StructureBoundingBox var14 = ComponentStrongholdCorridor.func_74992_a(var1, var2, var3, var4, var5, var6);
        return var14 != null && var14.minY > 1 ? new ComponentStrongholdCorridor(var7, var2, var14, var6) : null;
    }

    static StructureComponent getNextValidComponent(ComponentStrongholdStairs2 var0, List var1, Random var2, int var3, int var4, int var5, int var6, int var7) {
        if (var7 > 50) {
            return null;
        }

        if (Math.abs(var3 - var0.getBoundingBox().minX) <= 112 && Math.abs(var5 - var0.getBoundingBox().minZ) <= 112) {
            ComponentStronghold var8 = getNextComponent(var0, var1, var2, var3, var4, var5, var6, var7 + 1);
            if (var8 != null) {
                var1.add(var8);
                var0.field_75026_c.add(var8);
            }

            return var8;
        } else {
            return null;
        }
    }
}
