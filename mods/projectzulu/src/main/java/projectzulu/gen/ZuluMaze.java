package projectzulu.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * The cell maze builder of Project Zulu 1.0.3.8 ({@code projectzulu.common.world}: MazeGenerator, MazeCell,
 * CellType, CellIndexDirection, buildingmanager, architects, blueprints, dataobjects) that builds the labyrinth and
 * the cemetery. Names, loops and random draws as in the original. Every block goes through
 * {@code BuildingManager.HandleBlockPlacement} and {@code World.setBlock(..., 3)} ({@link ZuluUpdates}).
 * No game classes (tools/expect.sh compiles this).
 */
final class ZuluMaze {
    private ZuluMaze() {
    }

    // ---------------------------------------------------------------- CellType, CellIndexDirection, MazeCell

    /** CellType; EnumSet iterates in this order. */
    enum CellType {
        Wall, NorthWall, SoutherWall, EastWall, WestWall, InnerWall, DeadEnd, RandomUnCarved, AirCell
    }

    enum CellIndexDirection {
        Middle, Inner, NorthMiddle, SouthMiddle, WestMiddle, EastMiddle, NorthWestCorner, NorthEastCorner, SouthWestCorner,
        SouthEastCorner, NorthWall, SouthWall, WestWall, EastWall;

        static CellIndexDirection randomCardinalDirection(Random random) {
            return switch (random.nextInt(4)) {
                case 1 -> SouthMiddle;
                case 2 -> WestMiddle;
                case 3 -> EastMiddle;
                default -> NorthMiddle;
            };
        }

        static CellIndexDirection calcDirection(int cellIndex, int cellSize) {
            if (cellSize % 2 == 1) {
                if (cellIndex == cellSize * (cellSize - 1) / 2 + (cellSize - 1) / 2) {
                    return Middle;
                }
                if (cellIndex == (cellSize - 1) / 2) {
                    return WestMiddle;
                }
                if (cellIndex == cellSize * (cellSize - 1) / 2) {
                    return NorthMiddle;
                }
                if (cellIndex == cellSize * (cellSize - 1) + (cellSize - 1) / 2) {
                    return EastMiddle;
                }
                if (cellIndex == cellSize * (cellSize - 1) / 2 + (cellSize - 1)) {
                    return SouthMiddle;
                }
            }
            if (cellIndex == 0) {
                return NorthWestCorner;
            } else if (cellIndex == cellSize - 1) {
                return SouthWestCorner;
            } else if (cellIndex == cellSize * cellSize - 1) {
                return SouthEastCorner;
            } else if (cellIndex == cellSize * cellSize - cellSize) {
                return NorthEastCorner;
            } else if (cellIndex % cellSize == 0) {
                return NorthWall;
            } else if (cellIndex % cellSize == cellSize - 1) {
                return SouthWall;
            } else if (cellIndex >= cellSize * (cellSize - 1) && cellIndex < cellSize * cellSize) {
                return EastWall;
            } else {
                return cellIndex >= 0 && cellIndex < cellSize ? WestWall : Inner;
            }
        }
    }

    static final class MazeCell {
        private final int size;
        /** Block positions x, y, z (Vec3 of whole numbers in the original). */
        private final List<int[]> locations = new ArrayList<>();
        private int cellType;
        CellIndexDirection cellIndexDirection;
        int buildingSchematic = -1;
        private final Set<CellType> cellSubType = EnumSet.noneOf(CellType.class);

        MazeCell(int size) {
            this.size = size;
        }

        int getSize() {
            return this.size;
        }

        void setCellType(int cellType) {
            this.cellType = cellType;
        }

        int getCellType() {
            return this.cellType;
        }

        boolean containedInCellSubType(CellType subType) {
            if (subType == CellType.Wall) {
                return this.cellSubType.contains(CellType.NorthWall) || this.cellSubType.contains(CellType.SoutherWall)
                        || this.cellSubType.contains(CellType.EastWall) || this.cellSubType.contains(CellType.WestWall);
            }
            return this.cellSubType.contains(subType);
        }

        void addCellSubType(CellType subType) {
            this.cellSubType.add(subType);
        }

        void addLocation(int x, int y, int z) {
            this.locations.add(new int[] {x, y, z});
        }

        int[] getLocation(int index) {
            return this.locations.size() > index ? this.locations.get(index) : null;
        }
    }

    // ---------------------------------------------------------------- dataobjects

    /** BlockWithMeta: World.setBlock(x, y, z, id, meta, 3). Id -1 marks the entity pieces. */
    static class Piece {
        final int blockID;
        final int meta;

        Piece(int blockID) {
            this(blockID, 0);
        }

        Piece(int blockID, int meta) {
            this.blockID = blockID;
            this.meta = meta;
        }

        void placeBlock(OldWorld world, int x, int y, int z, Random random) {
            ZuluUpdates.set(world, x, y, z, this.blockID, this.meta);
        }
    }

    /** ChestWithMeta: a chest whose 27 slots each get ChestGenHooks.getOneItem("dungeonChest") with lootChance %. */
    static final class ChestPiece extends Piece {
        private final int lootChance;

        ChestPiece(int meta, int lootChance) {
            super(54, meta);
            this.lootChance = lootChance;
        }

        @Override
        void placeBlock(OldWorld world, int x, int y, int z, Random random) {
            ZuluUpdates.set(world, x, y, z, this.blockID, this.meta);
            OldTile.Chest chest = new OldTile.Chest();
            for (int slot = 0; slot < chest.slots().length; slot++) {
                if (this.lootChance - random.nextInt(100) >= 0) {
                    chest.slots()[slot] = OldLoot.DUNGEON_CHEST.oneItem(random);
                }
            }
            world.setTile(x, y, z, chest);
        }
    }

    /** MobSpawnerWithMeta: a vanilla spawner (id 52) with the given 1.5.2 entity name. */
    static final class SpawnerPiece extends Piece {
        private final String mobName;

        SpawnerPiece(String mobName) {
            super(52);
            this.mobName = mobName;
        }

        @Override
        void placeBlock(OldWorld world, int x, int y, int z, Random random) {
            ZuluUpdates.set(world, x, y, z, this.blockID);
            world.setTile(x, y, z, new OldTile.Spawner(this.mobName));
        }
    }

    /** MimicWithMeta: air, and a hovering mimic in the middle of the block. */
    static final class MimicPiece extends Piece {
        MimicPiece() {
            super(-1);
        }

        @Override
        void placeBlock(OldWorld world, int x, int y, int z, Random random) {
            ZuluUpdates.set(world, x, y, z, 0);
            world.spawnEntity(ZuluStructureContent.mimic(x + 0.5, y, z + 0.5));
        }
    }

    /** HauntedArmorWithMeta: air, and a hovering, persistent haunted armour with a random set. */
    static final class HauntedArmorPiece extends Piece {
        HauntedArmorPiece() {
            super(-1);
        }

        @Override
        void placeBlock(OldWorld world, int x, int y, int z, Random random) {
            ZuluUpdates.set(world, x, y, z, 0);
            world.spawnEntity(ZuluStructureContent.hauntedArmor(x + 0.5, y, z + 0.5));
        }
    }

    /** BlockWithMeta(tombstone): the block creates an empty TileEntityTombstone; its (empty) text goes along as tile data. */
    static final class TombstonePiece extends Piece {
        TombstonePiece() {
            super(ZuluIds.TOMBSTONE);
        }

        @Override
        void placeBlock(OldWorld world, int x, int y, int z, Random random) {
            ZuluUpdates.set(world, x, y, z, this.blockID, this.meta);
            world.setTile(x, y, z, ZuluStructureContent.Tombstone.empty());
        }
    }

    private static final Piece AIR = new Piece(0);

    // ---------------------------------------------------------------- blueprints and architects

    interface Blueprint {
        Piece getBlockFromBlueprint(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
                CellIndexDirection cellIndexDirection);

        default int getWeight() {
            return 1;
        }

        default String getIdentifier() {
            return "generic";
        }
    }

    abstract static class Architect {
        int unCarvedState;
        int carvedState;
        final List<Blueprint> unCarvedBlueprintList = new ArrayList<>();
        final List<Blueprint> carvedBlueprintList = new ArrayList<>();
        CellIndexDirection cellIndexDirection = CellIndexDirection.Inner;

        void randomiseState(Random random) {
            this.unCarvedState = getWeightedState(this.unCarvedBlueprintList, random);
            this.carvedState = getWeightedState(this.carvedBlueprintList, random);
            this.cellIndexDirection = CellIndexDirection.randomCardinalDirection(random);
        }

        Piece getCarvedBlock(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random) {
            return this.getCarvedBlock(cellIndex, cellSize, curHeight, maxHeight, xIndex, zIndex, random, this.cellIndexDirection, this.carvedState);
        }

        Piece getUnCarvedBlock(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random) {
            return this.getUnCarvedBlock(cellIndex, cellSize, curHeight, maxHeight, xIndex, zIndex, random, this.cellIndexDirection,
                    this.unCarvedState);
        }

        abstract Piece getCarvedBlock(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
                CellIndexDirection direction, int buildingIndex);

        abstract Piece getUnCarvedBlock(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
                CellIndexDirection direction, int buildingIndex);

        int searchUncarvedFor(String identifier) {
            for (int i = 0; i < this.unCarvedBlueprintList.size(); i++) {
                if (this.unCarvedBlueprintList.get(i).getIdentifier().equals(identifier)) {
                    return i;
                }
            }
            return -1;
        }

        static int getWeightedState(List<Blueprint> list, Random random) {
            int weightSum = 0;
            for (Blueprint blueprint : list) {
                weightSum += blueprint.getWeight();
            }
            weightSum = weightSum > 0 ? random.nextInt(weightSum) + 1 : 0;
            for (int i = 0; i < list.size(); i++) {
                weightSum -= list.get(i).getWeight();
                if (weightSum <= 0) {
                    return i;
                }
            }
            return 0;
        }
    }

    /** ArchitectLabyrinth: an explicit building index wins over the state. */
    static final class ArchitectLabyrinth extends Architect {
        ArchitectLabyrinth() {
            this.unCarvedBlueprintList.add(new DeadEndChest());
            this.unCarvedBlueprintList.add(ZuluMaze::labyrinthCobweb);
            this.carvedBlueprintList.add(new LabyrinthRandomWall());
            this.carvedBlueprintList.add(ZuluMaze::labyrinthHiddenWall);
        }

        @Override
        Piece getCarvedBlock(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
                CellIndexDirection direction, int buildingIndex) {
            Blueprint blueprint = buildingIndex >= 0 && buildingIndex < this.carvedBlueprintList.size()
                    ? this.carvedBlueprintList.get(buildingIndex) : this.carvedBlueprintList.get(this.carvedState);
            return blueprint.getBlockFromBlueprint(cellIndex, cellSize, curHeight, maxHeight, xIndex, zIndex, random, direction);
        }

        @Override
        Piece getUnCarvedBlock(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
                CellIndexDirection direction, int buildingIndex) {
            Blueprint blueprint = buildingIndex >= 0 && buildingIndex < this.unCarvedBlueprintList.size()
                    ? this.unCarvedBlueprintList.get(buildingIndex) : this.unCarvedBlueprintList.get(this.unCarvedState);
            return blueprint.getBlockFromBlueprint(cellIndex, cellSize, curHeight, maxHeight, xIndex, zIndex, random, direction);
        }
    }

    /** ArchitectCemetary: always the blueprint of the state. */
    static final class ArchitectCemetary extends Architect {
        ArchitectCemetary() {
            this.unCarvedBlueprintList.add(ZuluMaze::scatteredTombstonesAndFlowers);
            this.carvedBlueprintList.add(new Weighted(2, ZuluMaze::cemeteryTomb));
            this.carvedBlueprintList.add(new Weighted(4, ZuluMaze::cemeteryTomb2));
            this.carvedBlueprintList.add(ZuluMaze::cemetaryFountain);
            this.carvedBlueprintList.add(new Weighted(2, ZuluMaze::cemetaryFountain2));
        }

        @Override
        Piece getCarvedBlock(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
                CellIndexDirection direction, int buildingIndex) {
            return this.carvedBlueprintList.get(this.carvedState)
                    .getBlockFromBlueprint(cellIndex, cellSize, curHeight, maxHeight, xIndex, zIndex, random, direction);
        }

        @Override
        Piece getUnCarvedBlock(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
                CellIndexDirection direction, int buildingIndex) {
            return this.unCarvedBlueprintList.get(this.unCarvedState)
                    .getBlockFromBlueprint(cellIndex, cellSize, curHeight, maxHeight, xIndex, zIndex, random, direction);
        }
    }

    private record Weighted(int weight, Blueprint blueprint) implements Blueprint {
        @Override
        public Piece getBlockFromBlueprint(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
                CellIndexDirection direction) {
            return this.blueprint.getBlockFromBlueprint(cellIndex, cellSize, curHeight, maxHeight, xIndex, zIndex, random, direction);
        }

        @Override
        public int getWeight() {
            return this.weight;
        }
    }

    /** BlueprintDeadEndChest: weight 0, found by its identifier "deadend". */
    private static final class DeadEndChest implements Blueprint {
        @Override
        public Piece getBlockFromBlueprint(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
                CellIndexDirection direction) {
            if (curHeight == 0 && random.nextInt(8) == 0) {
                return new ChestPiece(0, 15);
            }
            return curHeight == 0 && random.nextInt(8) == 1 ? new MimicPiece() : new Piece(0);
        }

        @Override
        public int getWeight() {
            return 0;
        }

        @Override
        public String getIdentifier() {
            return "deadend";
        }
    }

    private static Piece labyrinthCobweb(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
            CellIndexDirection direction) {
        return (curHeight == 0 || curHeight == maxHeight - 1) && 10 - random.nextInt(100) >= 0 ? new Piece(30, 0) : new Piece(0);
    }

    /** BlueprintLabyrinthRandomWall, weight 4. */
    private static final class LabyrinthRandomWall implements Blueprint {
        @Override
        public Piece getBlockFromBlueprint(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
                CellIndexDirection direction) {
            if (10 - random.nextInt(100) >= 0) {
                return new Piece(98, 2);
            } else if (10 - random.nextInt(100) >= 0) {
                return new Piece(98, 1);
            } else {
                return 5 - random.nextInt(100) >= 0 ? new Piece(0) : new Piece(98, 0);
            }
        }

        @Override
        public int getWeight() {
            return 4;
        }
    }

    /** BlueprintLabyrinthHiddenWall: ProjectZulu|Mobs is loaded in the pack. */
    private static Piece labyrinthHiddenWall(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
            CellIndexDirection direction) {
        switch (CellIndexDirection.calcDirection(cellIndex, cellSize)) {
            case Inner:
                return new Piece(0);
            case Middle:
                if (curHeight == 0) {
                    if (random.nextInt(2) == 0) {
                        return new Piece(98, 2);
                    } else if (random.nextInt(4) == 0) {
                        return new SpawnerPiece(ZuluStructureContent.SPAWNER_MINOTAUR);
                    } else {
                        return new SpawnerPiece(ZuluStructureContent.SPAWNER_HAUNTED_ARMOR);
                    }
                } else {
                    if (curHeight == 1) {
                        return new ChestPiece(0, 25);
                    }
                    return new Piece(0);
                }
            default:
                return new Piece(98, 2);
        }
    }

    private static Piece cemetaryFountain(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
            CellIndexDirection direction) {
        int var9 = 3;
        CellIndexDirection at = CellIndexDirection.calcDirection(cellIndex, cellSize);
        if (curHeight == 0) {
            return switch (at) {
                case NorthMiddle -> new Piece(109, 2);
                case NorthEastCorner, NorthWestCorner -> new Piece(44, 5);
                case Inner, Middle -> new Piece(9);
                default -> new Piece(98, 0);
            };
        } else if (curHeight < var9 - 2) {
            return switch (at) {
                case NorthMiddle, NorthEastCorner, NorthWestCorner, Inner, Middle -> new Piece(0);
                default -> new Piece(98, 0);
            };
        } else if (curHeight == var9 - 2) {
            return switch (at) {
                case SouthEastCorner -> new Piece(109, 1);
                case SouthWestCorner -> new Piece(109, 0);
                case SouthWall, SouthMiddle, EastWall, EastMiddle, WestWall, WestMiddle -> new Piece(98, 0);
                default -> new Piece(0);
            };
        } else if (curHeight == var9 - 1) {
            return switch (at) {
                case Inner, Middle -> new Piece(44, 5);
                default -> new Piece(0);
            };
        } else {
            return new Piece(0);
        }
    }

    private static Piece cemetaryFountain2(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
            CellIndexDirection direction) {
        if (curHeight == 0) {
            switch (CellIndexDirection.calcDirection(cellIndex, cellSize)) {
                case NorthWall, NorthMiddle:
                    return new Piece(109, 2);
                case SouthWall, SouthMiddle:
                    return new Piece(109, 3);
                case EastWall, NorthEastCorner, SouthEastCorner, EastMiddle:
                    return new Piece(109, 1);
                case WestWall, NorthWestCorner, SouthWestCorner, WestMiddle:
                    return new Piece(109, 0);
                case Inner, Middle:
                    return new Piece(9);
                default:
                    break;
            }
        }
        return new Piece(0);
    }

    /** The door of BlueprintCemeteryTomb and Tomb2 at height 0 or 1 on the side the architect chose, or null. */
    private static Piece tombDoor(int curHeight, CellIndexDirection direction) {
        if (curHeight == 0) {
            return switch (direction) {
                case NorthMiddle -> new Piece(71, 3);
                case SouthMiddle -> new Piece(71, 4);
                case EastMiddle -> new Piece(71, 5);
                case WestMiddle -> new Piece(71, 2);
                default -> null;
            };
        }
        return switch (direction) {
            case NorthMiddle -> new Piece(71, 11);
            case SouthMiddle -> new Piece(71, 12);
            case EastMiddle -> new Piece(71, 13);
            case WestMiddle -> new Piece(71, 10);
            default -> null;
        };
    }

    private static Piece cemeteryTomb(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
            CellIndexDirection direction) {
        CellIndexDirection at = CellIndexDirection.calcDirection(cellIndex, cellSize);
        if (curHeight == 0 && at == CellIndexDirection.Middle) {
            return new ChestPiece(1, 15);
        }
        if (curHeight == 0 && at == direction) {
            Piece door = tombDoor(0, direction);
            if (door != null) {
                return door;
            }
        } else if (curHeight == 1 && at == direction) {
            Piece door = tombDoor(1, direction);
            if (door != null) {
                return door;
            }
        } else if (curHeight < maxHeight) {
            if (curHeight == maxHeight - 1) {
                if (at == CellIndexDirection.Middle || at == CellIndexDirection.Inner) {
                    return new Piece(44, 5);
                }
            } else if (curHeight % 2 == 0) {
                switch (at) {
                    case NorthMiddle, NorthWall:
                        return new Piece(109, 2);
                    case SouthMiddle, SouthWall:
                        return new Piece(109, 3);
                    case EastMiddle, EastWall, NorthEastCorner, SouthEastCorner:
                        return new Piece(109, 1);
                    case WestMiddle, WestWall, NorthWestCorner, SouthWestCorner:
                        return new Piece(109, 0);
                    default:
                        break;
                }
            } else {
                switch (at) {
                    case NorthMiddle, NorthWall:
                        return new Piece(109, 6);
                    case SouthMiddle, SouthWall:
                        return new Piece(109, 7);
                    case EastMiddle, EastWall, NorthEastCorner, SouthEastCorner:
                        return new Piece(109, 5);
                    case WestMiddle, WestWall, NorthWestCorner, SouthWestCorner:
                        return new Piece(109, 4);
                    default:
                        break;
                }
            }
        }
        return new Piece(0);
    }

    private static Piece cemeteryTomb2(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex, Random random,
            CellIndexDirection direction) {
        CellIndexDirection at = CellIndexDirection.calcDirection(cellIndex, cellSize);
        if (curHeight == 0 && at == CellIndexDirection.Middle) {
            return new ChestPiece(1, 15);
        }
        if (curHeight == 0 && at == direction) {
            Piece door = tombDoor(0, direction);
            if (door != null) {
                return door;
            }
        } else if (curHeight == 1 && at == direction) {
            Piece door = tombDoor(1, direction);
            if (door != null) {
                return door;
            }
        } else if (curHeight < maxHeight) {
            if (curHeight == maxHeight - 1) {
                if (at == CellIndexDirection.Middle || at == CellIndexDirection.Inner) {
                    return new Piece(44, 5);
                }
            } else if (curHeight == maxHeight - 2) {
                switch (at) {
                    case NorthMiddle, NorthWall:
                        return new Piece(109, 2);
                    case SouthMiddle, SouthWall:
                        return new Piece(109, 3);
                    case EastMiddle, EastWall, NorthEastCorner, SouthEastCorner:
                        return new Piece(109, 1);
                    case WestMiddle, WestWall, NorthWestCorner, SouthWestCorner:
                        return new Piece(109, 0);
                    default:
                        break;
                }
            } else if (curHeight % 2 == 0) {
                switch (at) {
                    case NorthMiddle, NorthWall:
                        return new Piece(109, 2);
                    case SouthMiddle, SouthWall:
                        return new Piece(109, 3);
                    case EastMiddle, EastWall:
                        return new Piece(109, 1);
                    case WestMiddle, WestWall:
                        return new Piece(109, 0);
                    case NorthEastCorner, SouthEastCorner, NorthWestCorner, SouthWestCorner:
                        return new Piece(98, 0);
                    default:
                        break;
                }
            } else {
                switch (at) {
                    case NorthMiddle, NorthWall:
                        return new Piece(109, 6);
                    case SouthMiddle, SouthWall:
                        return new Piece(109, 7);
                    case EastMiddle, EastWall:
                        return new Piece(109, 5);
                    case WestMiddle, WestWall:
                        return new Piece(109, 4);
                    case NorthEastCorner, SouthEastCorner, NorthWestCorner, SouthWestCorner:
                        return new Piece(98, 0);
                    default:
                        break;
                }
            }
        }
        return new Piece(0);
    }

    /** BlueprintScatteredTombstonesAndFlowers (Project Zulu's tombstone block is present in the pack). */
    private static Piece scatteredTombstonesAndFlowers(int cellIndex, int cellSize, int curHeight, int maxHeight, int xIndex, int zIndex,
            Random random, CellIndexDirection direction) {
        if (curHeight != 0) {
            return new Piece(0);
        }
        if (random.nextInt(90) == 0) {
            return new HauntedArmorPiece();
        }
        if (xIndex % 2 == 0 && zIndex % 2 == 0 || xIndex % 2 == 1 && zIndex % 2 == 1) {
            if (20 - random.nextInt(100) < 0 || cellIndex % 2 * cellSize != cellSize && cellIndex % 2 * cellSize != 2 * cellSize - 1) {
                return new Piece(0);
            }
            return new TombstonePiece();
        } else if (20 - random.nextInt(100) >= 0 && cellIndex % 2 * cellSize == 0 || cellIndex % 2 * cellSize == cellSize - 1) {
            return new TombstonePiece();
        } else if (5 - random.nextInt(100) >= 0) {
            return new Piece(38, 0);
        } else if (5 - random.nextInt(100) >= 0) {
            return new Piece(37, 0);
        } else {
            return 40 - random.nextInt(100) >= 0 ? new Piece(31, 1) : new Piece(0);
        }
    }

    // ---------------------------------------------------------------- building managers

    abstract static class BuildingManager {
        final OldWorld world;
        final Architect architect;

        BuildingManager(OldWorld world, Architect architect) {
            this.world = world;
            this.architect = architect;
        }

        abstract void createFloor(int sx, int sy, int sz, int width, int floorHeight, int floorNumber, int cellSize);

        abstract boolean evaluateCarvedCells(MazeCell[][] cells, int xIndex, int zIndex, int numCellsX, int numCellsZ, Random random);

        abstract boolean evaluateUnCarvedCells(MazeCell[][] cells, int xIndex, int zIndex, int numCellsX, int numCellsZ, Random random);

        abstract void createBuilding(MazeCell[][] cells, int xIndex, int zIndex, int floorHeight, Random random);

        abstract void createSpecial(int sx, int sy, int sz, int width, int floorHeight, int floorNumber, int cellSize);

        /**
         * HandleBlockPlacement: a block with a tile entity (World.getBlockTileEntity creates one for any such block)
         * loses its tile entity and stays; anything else is replaced. World.blockExists is taken as true for every
         * chunk (in 1.5.2 it was false for chunks not loaded at that moment).
         */
        void handleBlockPlacement(Piece piece, int x, int y, int z, Random random) {
            if (y < 0 || y >= OldWorld.HEIGHT) {
                return;
            }
            if (this.world.getTile(x, y, z) != null || hasTileEntity(this.world.getBlockId(x, y, z))) {
                this.world.removeTile(x, y, z);
            } else {
                piece.placeBlock(this.world, x, y, z, random);
            }
        }

        /**
         * Block.hasTileEntity of 1.5.2 for the blocks that can stand here: dispenser, note block, moving piston,
         * spawner, chest, furnaces, signs, jukebox, enchanting table, brewing stand, end portal, ender chest, command
         * block, beacon, skull, trapped chest, comparators, daylight sensor, hopper, dropper, and Project Zulu's
         * BlockContainers. Blocks of other mods with tile data have it in the world already ({@code getTile}).
         */
        static boolean hasTileEntity(int id) {
            return switch (id) {
                case 23, 25, 36, 52, 54, 61, 62, 63, 68, 84, 116, 117, 119, 130, 137, 138, 144, 146, 149, 150, 151, 154, 158,
                        ZuluIds.LIMITED_MOB_SPAWNER, ZuluIds.MOB_SKULLS, ZuluIds.TOMBSTONE, ZuluIds.UNIVERSAL_FLOWER_POT -> true;
                default -> false;
            };
        }

        void placeRandomBlock(int x, int y, int z, Random random, int probability, int probabilityOutOf, Piece... pieces) {
            for (int i = 0; i < pieces.length; i++) {
                if (probability - random.nextInt(probabilityOutOf) >= 0 && i + 1 < pieces.length) {
                    this.handleBlockPlacement(new Piece(pieces[i].blockID, pieces[i].meta), x, y, z, random);
                    break;
                }
                this.handleBlockPlacement(new Piece(pieces[i].blockID, pieces[i].meta), x, y, z, random);
            }
        }

        static boolean isDeadEnd(MazeCell[][] cells, int selectedX, int selectedZ, int maxCellsX, int maxCellsZ) {
            int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            int wallsNearby = 0;
            for (int[] d : directions) {
                int tempX = selectedX + d[0];
                int tempZ = selectedZ + d[1];
                if (tempX >= 0 && tempX <= maxCellsX - 1 && tempZ >= 0 && tempZ <= maxCellsZ - 1 && cells[tempX][tempZ].getCellType() == 1) {
                    wallsNearby++;
                }
            }
            return wallsNearby == 3;
        }
    }

    static final class BuildingManagerLabyrinth extends BuildingManager {
        BuildingManagerLabyrinth(OldWorld world) {
            super(world, new ArchitectLabyrinth());
        }

        @Override
        void createFloor(int sx, int sy, int sz, int width, int floorHeight, int floorNumber, int cellSize) {
            for (int i = -(width - cellSize); i < width - cellSize; i++) {
                for (int k = -(width - 3); k < width - 3; k++) {
                    this.handleBlockPlacement(new Piece(98, 0), sx + i, sy + floorNumber * (floorHeight + 1), sz + k, null);
                }
            }
        }

        @Override
        boolean evaluateCarvedCells(MazeCell[][] cells, int xIndex, int zIndex, int numCellsX, int numCellsZ, Random random) {
            if (!cells[xIndex][zIndex].containedInCellSubType(CellType.Wall)) {
                cells[xIndex][zIndex].addCellSubType(CellType.InnerWall);
                return true;
            }
            return false;
        }

        @Override
        boolean evaluateUnCarvedCells(MazeCell[][] cells, int xIndex, int zIndex, int numCellsX, int numCellsZ, Random random) {
            if (cells[xIndex][zIndex].containedInCellSubType(CellType.Wall)) {
                return false;
            }
            if (isDeadEnd(cells, xIndex, zIndex, numCellsX, numCellsZ) && cells[xIndex][zIndex].getCellType() == 0) {
                cells[xIndex][zIndex].addCellSubType(CellType.DeadEnd);
                cells[xIndex][zIndex].buildingSchematic = this.architect.searchUncarvedFor("deadend");
            } else {
                cells[xIndex][zIndex].addCellSubType(CellType.RandomUnCarved);
            }
            return true;
        }

        @Override
        void createBuilding(MazeCell[][] cells, int xIndex, int zIndex, int floorHeight, Random random) {
            MazeCell cell = cells[xIndex][zIndex];
            int cellSize = cell.getSize();
            this.architect.randomiseState(random);
            for (int cellIndex = 0; cellIndex < cellSize * cellSize; cellIndex++) {
                int[] p = cell.getLocation(cellIndex);
                for (CellType cellType : cell.cellSubType) {
                    switch (cellType) {
                        case NorthWall, SoutherWall, EastWall, WestWall -> {
                            for (int j = 0; j < floorHeight; j++) {
                                this.handleBlockPlacement(new Piece(98, 0), p[0], p[1] + j, p[2], random);
                            }
                        }
                        case InnerWall -> {
                            for (int j = 0; j < floorHeight; j++) {
                                this.handleBlockPlacement(this.architect.getCarvedBlock(cellIndex, cellSize, j, floorHeight, xIndex, zIndex, random),
                                        p[0], p[1] + j, p[2], random);
                            }
                        }
                        case DeadEnd -> {
                            for (int j = 0; j < floorHeight; j++) {
                                this.handleBlockPlacement(this.architect.getUnCarvedBlock(cellIndex, cellSize, j, floorHeight, xIndex, zIndex, random,
                                        cell.cellIndexDirection, cell.buildingSchematic), p[0], p[1] + j, p[2], random);
                            }
                        }
                        case RandomUnCarved -> {
                            for (int j = 0; j < floorHeight; j++) {
                                this.handleBlockPlacement(this.architect.getUnCarvedBlock(cellIndex, cellSize, j, floorHeight, xIndex, zIndex, random),
                                        p[0], p[1] + j, p[2], random);
                            }
                        }
                        case AirCell -> {
                            for (int j = 0; j < floorHeight; j++) {
                                this.handleBlockPlacement(AIR, p[0], p[1] + j, p[2], random);
                            }
                        }
                        default -> {
                        }
                    }
                }
            }
        }

        @Override
        void createSpecial(int sx, int sy, int sz, int width, int floorHeight, int floorNumber, int cellSize) {
            for (int k = 0; k < cellSize * 2; k++) {
                for (int i = 0; i < cellSize; i++) {
                    for (int j = 1; j <= floorHeight; j++) {
                        this.handleBlockPlacement(AIR, sx + i, sy + j, sz - k + (width - cellSize), null);
                    }
                }
            }
            this.createFloor(sx, sy, sz, width, floorHeight, floorNumber + 1, cellSize);
            for (int i = 0; i < cellSize; i++) {
                this.handleBlockPlacement(AIR, sx + i, sy + floorHeight, sz + (width - cellSize), null);
            }
            int yHeight = 1;
            for (int k = 0; k < 10; k++) {
                for (int j = yHeight; j <= yHeight + 5; j++) {
                    for (int i = -1; i <= cellSize; i++) {
                        Piece piece;
                        if (i >= 0 && i < cellSize && j == yHeight) {
                            piece = new Piece(109, 2);
                        } else if (i >= 0 && i < cellSize && j == yHeight + 5) {
                            piece = new Piece(98, 0);
                        } else if (i >= 0 && i < cellSize && j > yHeight && j < yHeight + 5) {
                            piece = AIR;
                        } else {
                            piece = new Piece(98, 0);
                        }
                        this.handleBlockPlacement(piece, sx + i, sy + j, sz + k + (width - cellSize), null);
                    }
                }
                yHeight++;
            }
        }
    }

    static final class BuildingManagerCemetary extends BuildingManager {
        BuildingManagerCemetary(OldWorld world) {
            super(world, new ArchitectCemetary());
        }

        @Override
        void createFloor(int sx, int sy, int sz, int width, int floorHeight, int floorNumber, int cellSize) {
            if (floorNumber % floorHeight == 0) {
                for (int i = -(width - 3); i < width - 3; i++) {
                    for (int k = -(width - 3); k < width - 3; k++) {
                        this.handleBlockPlacement(new Piece(2), sx + i, sy + floorNumber * floorHeight, sz + k, null);
                    }
                }
            }
        }

        @Override
        void createSpecial(int sx, int sy, int sz, int width, int floorHeight, int floorNumber, int cellSize) {
        }

        @Override
        boolean evaluateCarvedCells(MazeCell[][] cells, int xIndex, int zIndex, int numCellsX, int numCellsZ, Random random) {
            if (cells[xIndex][zIndex].containedInCellSubType(CellType.Wall)) {
                return false;
            }
            if (cells[xIndex + 1][zIndex].containedInCellSubType(CellType.InnerWall)
                    || cells[xIndex - 1][zIndex].containedInCellSubType(CellType.InnerWall)
                    || cells[xIndex][zIndex + 1].containedInCellSubType(CellType.InnerWall)
                    || cells[xIndex][zIndex - 1].containedInCellSubType(CellType.InnerWall)) {
                cells[xIndex][zIndex].addCellSubType(CellType.RandomUnCarved);
            } else if (random.nextInt(4) < 3) {
                cells[xIndex][zIndex].addCellSubType(CellType.RandomUnCarved);
            } else {
                cells[xIndex][zIndex].addCellSubType(CellType.InnerWall);
            }
            return true;
        }

        @Override
        boolean evaluateUnCarvedCells(MazeCell[][] cells, int xIndex, int zIndex, int numCellsX, int numCellsZ, Random random) {
            if (!cells[xIndex][zIndex].containedInCellSubType(CellType.Wall)) {
                cells[xIndex][zIndex].addCellSubType(CellType.RandomUnCarved);
                return true;
            }
            return false;
        }

        /** The fence of one side: at the outer row a cobblestone wall (1 in 4: a gap), inside 5 % graves. */
        private void wallSide(boolean outer, int x, int y, int z, int floorHeight, Random random) {
            for (int j = 0; j < floorHeight; j++) {
                if (j == 0) {
                    if (outer) {
                        if (random.nextInt(4) == 0) {
                            this.handleBlockPlacement(new Piece(0), x, y + j, z, random);
                        } else {
                            this.placeRandomBlock(x, y + j, z, random, 50, 100, new Piece(139, 1), new Piece(139, 0));
                        }
                    } else if (5 - random.nextInt(100) >= 0) {
                        this.handleBlockPlacement(new TombstonePiece(), x, y + j, z, random);
                    } else {
                        this.handleBlockPlacement(new Piece(0), x, y + j, z, random);
                    }
                } else {
                    this.handleBlockPlacement(new Piece(0), x, y + j, z, random);
                }
            }
        }

        @Override
        void createBuilding(MazeCell[][] cells, int xIndex, int zIndex, int floorHeight, Random random) {
            MazeCell cell = cells[xIndex][zIndex];
            int cellSize = cell.getSize();
            this.architect.randomiseState(random);
            for (int cellIndex = 0; cellIndex < cellSize * cellSize; cellIndex++) {
                int[] p = cell.getLocation(cellIndex);
                for (CellType cellType : cell.cellSubType) {
                    switch (cellType) {
                        case NorthWall -> this.wallSide(cellIndex % cellSize == 0, p[0], p[1], p[2], floorHeight, random);
                        case SoutherWall -> this.wallSide(cellIndex % cellSize == cellSize - 1, p[0], p[1], p[2], floorHeight, random);
                        case EastWall -> this.wallSide(cellIndex >= cellSize * (cellSize - 1) && cellIndex < cellSize * cellSize, p[0], p[1], p[2],
                                floorHeight, random);
                        case WestWall -> this.wallSide(cellIndex >= 0 && cellIndex < cellSize, p[0], p[1], p[2], floorHeight, random);
                        case InnerWall -> {
                            for (int j = 0; j < floorHeight; j++) {
                                this.handleBlockPlacement(this.architect.getCarvedBlock(cellIndex, cellSize, j, floorHeight, xIndex, zIndex, random),
                                        p[0], p[1] + j, p[2], random);
                            }
                        }
                        case RandomUnCarved -> {
                            for (int j = 0; j < floorHeight; j++) {
                                this.handleBlockPlacement(this.architect.getUnCarvedBlock(cellIndex, cellSize, j, floorHeight, xIndex, zIndex, random),
                                        p[0], p[1] + j, p[2], random);
                            }
                        }
                        case AirCell -> {
                            for (int j = 0; j < floorHeight; j++) {
                                this.handleBlockPlacement(AIR, p[0], p[1] + j, p[2], random);
                            }
                        }
                        default -> {
                        }
                    }
                }
            }
        }

        /** BuildingManagerCemetary.HandleBlockPlacement: snow is not cleared, logs and leaves are left alone. */
        @Override
        void handleBlockPlacement(Piece piece, int x, int y, int z, Random random) {
            if (piece.blockID == 0 && this.world.getBlockId(x, y, z) == 78) {
                return;
            }
            Material material = this.world.material(x, y, z);
            if (material != Material.WOOD && material != Material.LEAVES) {
                super.handleBlockPlacement(piece, x, y, z, random);
            }
        }
    }

    // ---------------------------------------------------------------- MazeGenerator

    /** MazeGenerator(buildingManager, numberOfFloors, floorHeight, cellSize, width, chancePerChunk, chanceOutOf). */
    static boolean generate(BuildingManager buildingManager, int numberOfFloors, int floorHeight, int cellSize, int width, int chancePerChunk,
            int chanceOutOf, OldWorld world, Random random, int x, int y, int z) {
        if (chancePerChunk - random.nextInt(chanceOutOf) < 0) {
            return false;
        }
        for (int j = 0; j < numberOfFloors; j++) {
            if (j == 0) {
                buildingManager.createFloor(x, y, z, width, floorHeight, j, cellSize);
                generateMaze(x, y, z, j, floorHeight, cellSize, -(width - 3), width - 3, -(width - 3), width - 3, buildingManager, random);
                buildingManager.createSpecial(x, y, z, width, floorHeight, j, cellSize);
            }
        }
        return true;
    }

    private static void generateMaze(int sx, int sy, int sz, int floorYcoord, int floorHeight, int cellWidth, int lowerX, int upperX, int lowerZ,
            int upperZ, BuildingManager buildingManager, Random random) {
        // MathHelper.ceiling_double_int of an int division
        int numCellsX = (Math.abs(lowerX - upperX) + 1) / cellWidth;
        int numCellsZ = (Math.abs(lowerZ - upperZ) + 1) / cellWidth;
        int mazeStartX = cellWidth * 2;
        int mazeStartZ = cellWidth * 2;
        int mazeFinishX = numCellsX - cellWidth * 2;
        int mazeFinishZ = numCellsZ - cellWidth * 2;
        MazeCell[][] allCells = new MazeCell[numCellsX][numCellsZ];
        for (int i = 0; i < numCellsX; i++) {
            for (int k = 0; k < numCellsZ; k++) {
                allCells[i][k] = new MazeCell(cellWidth);
            }
        }
        int xCell = 0;
        int xCounter = 1;
        for (int i = lowerX; i < upperX; i++) {
            int zCell = 0;
            int zCounter = 1;
            for (int k = lowerZ; k < upperZ; k++) {
                if (xCell < numCellsX && zCell < numCellsZ) {
                    allCells[xCell][zCell].addLocation(sx + i, sy + floorYcoord + 1, sz + k);
                }
                if (zCounter % cellWidth == 0) {
                    zCell++;
                }
                zCounter++;
            }
            if (xCounter % cellWidth == 0) {
                xCell++;
            }
            xCounter++;
        }
        for (int i = 0; i < numCellsZ; i++) {
            allCells[i][0].setCellType(1);
            allCells[i][0].addCellSubType(CellType.NorthWall);
            allCells[i][numCellsZ - 1].setCellType(1);
            allCells[i][numCellsZ - 1].addCellSubType(CellType.SoutherWall);
        }
        for (int k = 0; k < numCellsX; k++) {
            allCells[0][k].setCellType(1);
            allCells[0][k].addCellSubType(CellType.WestWall);
            allCells[numCellsX - 1][k].setCellType(1);
            allCells[numCellsX - 1][k].addCellSubType(CellType.EastWall);
        }
        int attemptsSinceLastPlaced = 0;
        int[] directionsX = {0, 1, 0, -1};
        int[] directionsZ = {1, 0, -1, 0};
        while (attemptsSinceLastPlaced < 800) {
            int randLength = random.nextInt(3) + 1;
            int randDirection = random.nextInt(4);
            int selectedX = random.nextInt(numCellsX);
            int selectedZ;
            for (selectedZ = random.nextInt(numCellsZ); allCells[selectedX][selectedZ].getCellType() != 1; selectedZ = random.nextInt(numCellsZ)) {
                selectedX = random.nextInt(numCellsX);
            }
            if (!isCellTouching(allCells, selectedX, selectedZ, randDirection, randLength, numCellsX, numCellsZ, mazeStartX, mazeStartZ,
                    mazeFinishX, mazeFinishZ)) {
                for (int i = 1; i <= randLength; i++) {
                    int tempX = selectedX + directionsX[randDirection] * i;
                    int tempZ = selectedZ + directionsZ[randDirection] * i;
                    allCells[tempX][tempZ].setCellType(1);
                    buildingManager.evaluateCarvedCells(allCells, tempX, tempZ, numCellsX, numCellsZ, random);
                }
                attemptsSinceLastPlaced = 0;
            } else {
                attemptsSinceLastPlaced++;
            }
        }
        for (int i = 0; i < numCellsX; i++) {
            for (int k = 0; k < numCellsZ; k++) {
                if (allCells[i][k].getCellType() == 0 && buildingManager.evaluateUnCarvedCells(allCells, i, k, numCellsX, numCellsZ, random)) {
                    allCells[i][k].setCellType(2);
                }
            }
        }
        for (int i = 0; i < numCellsX; i++) {
            for (int k = 0; k < numCellsZ; k++) {
                if (allCells[i][k].getCellType() == 0) {
                    allCells[i][k].setCellType(3);
                    allCells[i][k].addCellSubType(CellType.AirCell);
                }
            }
        }
        for (int i = 0; i < numCellsX; i++) {
            for (int k = 0; k < numCellsZ; k++) {
                buildingManager.createBuilding(allCells, i, k, floorHeight, random);
            }
        }
    }

    /** MazeGenerator.isCellTouching (also WorldGenPyramid's copy). */
    static boolean isCellTouching(MazeCell[][] allCells, int selectedX, int selectedZ, int randDirection, int randLength, int maxCellsX,
            int maxCellsZ, int mazeStartX, int mazeStartZ, int mazeFinishX, int mazeFinishZ) {
        int[] directionsX = {0, 1, 0, -1, -1, -1, 1, 1};
        int[] directionsZ = {1, 0, -1, 0, -1, 1, -1, 1};
        for (int i = 1; i <= randLength; i++) {
            int tempX = selectedX + directionsX[randDirection] * i;
            int tempZ = selectedZ + directionsZ[randDirection] * i;
            if (tempX < 0 || tempX > maxCellsX - 1 || tempZ < 0 || tempZ > maxCellsZ - 1) {
                return true;
            }
            if (allCells[tempX][tempZ].getCellType() == 1) {
                return true;
            }
            for (int j = 0; j < 8; j++) {
                int nx = tempX + directionsX[j];
                int nz = tempZ + directionsZ[j];
                if (nx >= 0 && nx <= maxCellsX - 1 && nz >= 0 && nz <= maxCellsZ - 1
                        && (selectedX != nx || selectedZ != nz && selectedZ != nz - 1 && selectedZ != nz + 1)
                        && (selectedZ != nz || selectedX != nx && selectedX != nx - 1 && selectedX != nx + 1)) {
                    if (wouldMazeStartBeSurrounded(allCells, nx, nz, mazeStartX, mazeStartZ, maxCellsX, maxCellsZ)) {
                        return true;
                    }
                    if (wouldMazeStartBeSurrounded(allCells, nx, nz, mazeFinishX, mazeFinishZ, maxCellsX, maxCellsZ)) {
                        return true;
                    }
                    if (allCells[nx][nz].getCellType() == 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean wouldMazeStartBeSurrounded(MazeCell[][] allCells, int currentX, int currentZ, int mazeStartX, int mazeStartZ,
            int maxCellsX, int maxCellsZ) {
        int[] directionsX = {0, 1, 0, -1};
        int[] directionsZ = {1, 0, -1, 0};
        int numberOfSidesOccupied = 0;
        if (mazeStartX != currentX || mazeStartZ != currentZ) {
            if (Math.abs(mazeStartX - currentX) > 1 || Math.abs(mazeStartZ - currentZ) > 1) {
                return false;
            }
            if (allCells[currentX][currentZ].getCellType() == 0) {
                numberOfSidesOccupied++;
            }
        }
        for (int j = 0; j < 4; j++) {
            int tempX = mazeStartX + directionsX[j];
            int tempZ = mazeStartZ + directionsZ[j];
            if (tempX + directionsX[j] >= 0 && tempX + directionsX[j] <= maxCellsX - 1 && tempZ + directionsZ[j] >= 0
                    && tempZ + directionsZ[j] <= maxCellsZ - 1 && allCells[tempX + directionsX[j]][tempZ + directionsZ[j]].getCellType() == 1) {
                numberOfSidesOccupied++;
            }
        }
        return numberOfSidesOccupied >= 4;
    }
}
