package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * BuilderDungeon ("dungeon", undergroundDungeon.prop) with DungeonRoom: rooms grown from one in the middle of a
 * {@code size} x {@code size} plan, joined by doors, walls of {@code wallBlock}, a chest and spawners or mob blocks
 * in each room; then every room furnished by {@link DecoratorRoom}.
 *
 * <p>The original drew the plan (rooms, doors, chests, spawners) from an unseeded {@code new Random()}: here from a
 * random seeded with the world seed and the position.
 */
final class BuilderDungeon implements BDDungeons.Builder {
    private List<DungeonRoom> rooms;
    private final int posY;
    private final int heightVariation;
    private final int size;
    private final int roomSize;
    private final int wallBlock;
    private final int roomHeight = 5;

    BuilderDungeon(int size, int roomSize, int posY, int heightVariation, int wallBlock) {
        this.size = size;
        this.roomSize = roomSize;
        this.wallBlock = wallBlock;
        this.posY = posY;
        this.heightVariation = heightVariation;
    }

    @Override
    public void generate(Random random, OldWorld world, int x, int z, int mob) {
        int y = this.posY;
        if (this.heightVariation > 1) {
            y += random.nextInt(this.heightVariation);
        }
        this.generate(random, world, x, y, z, mob);
    }

    void generate(Random random, OldWorld world, int i, int j, int k, int mob) {
        BuilderHelper b = new BuilderHelper(world);
        b.fillMap(world, random, this.getDungeon(BuilderHelper.positionRandom(world, i, j, k, 1)), i, j, k, this.wallBlock, mob);
        DecoratorRoom roomsDecorator = new DecoratorRoom(mob);
        for (DungeonRoom room : this.rooms) {
            room.decorate(i, j, k, this.roomHeight, roomsDecorator, world, random);
        }
    }

    private int[][][] getDungeon(Random random) {
        int[][] dng = new int[this.size][this.size];
        this.rooms = new ArrayList<>();
        DungeonRoom room = new DungeonRoom(dng.length / 2 - 7, dng.length / 2 - 7, this.roomSize, this.roomSize);
        room.expand(dng);
        this.rooms.add(room);
        for (int i = 0; i < this.rooms.size(); i++) {
            for (int u = 0; u < 4; u++) {
                DungeonRoom troom = this.addRoomsTo(this.rooms.get(i), dng, random, u);
                if (troom != null) {
                    this.rooms.add(troom);
                }
            }
        }
        for (DungeonRoom ro : this.rooms) {
            if (random.nextBoolean()) {
                dng[ro.posX + random.nextInt(ro.sizeX)][ro.posY + random.nextInt(ro.sizeY)] = 54;
            }
            boolean flag = true;
            for (int cont = 0; random.nextBoolean() && cont < 5; cont++) {
                dng[ro.posX + random.nextInt(ro.sizeX)][ro.posY + random.nextInt(ro.sizeY)] = 300;
                flag = false;
            }
            if (flag) {
                dng[ro.posX + random.nextInt(ro.sizeX)][ro.posY + random.nextInt(ro.sizeY)] = 52;
            }
        }
        for (int x = 0; x < dng.length; x++) {
            for (int y = 0; y < dng[0].length; y++) {
                if (checkNeightbours(x, y, dng) <= 0) {
                    dng[x][y] = -1;
                }
            }
        }
        int[][][] map = new int[this.size][this.roomHeight][this.size];
        for (int x = 0; x < dng.length; x++) {
            for (int y = 0; y < dng[0].length; y++) {
                int curr = dng[x][y];
                map[x][0][y] = curr == -1 ? -1 : 1;
                map[x][4][y] = curr == -1 ? -1 : 1;
                if (curr == -1) {
                    for (int h = 1; h < this.roomHeight - 1; h++) {
                        map[x][h][y] = -1;
                    }
                } else if (curr == 0) {
                    for (int h = 1; h < this.roomHeight - 1; h++) {
                        map[x][h][y] = 1;
                    }
                } else if (curr == 1) {
                    for (int h = 1; h < this.roomHeight - 1; h++) {
                        map[x][h][y] = 0;
                    }
                } else if (curr == 3) {
                    map[x][1][y] = 64;
                    map[x][2][y] = -1;
                    for (int h = 3; h < this.roomHeight - 1; h++) {
                        map[x][h][y] = 1;
                    }
                } else {
                    map[x][1][y] = curr;
                    for (int h = 2; h < this.roomHeight - 1; h++) {
                        map[x][h][y] = 0;
                    }
                }
            }
        }
        return map;
    }

    private DungeonRoom addRoomsTo(DungeonRoom room, int[][] map, Random random, int side) {
        int x = room.posX;
        int y = room.posY;
        int sx = random.nextInt(this.roomSize) + 3;
        int sy = random.nextInt(this.roomSize) + 3;
        switch (side) {
            case 0 -> x = room.posX - sx;
            case 1 -> y = room.posY + room.sizeY;
            case 2 -> x = room.posX + room.sizeX;
            case 3 -> y = room.posY - sy;
            default -> {
                return null;
            }
        }
        DungeonRoom newRoom = new DungeonRoom(x, y, sx, sy);
        if (newRoom.expand(map)) {
            createPath(room, newRoom, random, map, side);
            return newRoom;
        }
        return null;
    }

    private static void createPath(DungeonRoom room, DungeonRoom roomdest, Random random, int[][] map, int side) {
        int x = room.posX + random.nextInt(Math.min(roomdest.sizeX, room.sizeX));
        int y = room.posY + random.nextInt(Math.min(roomdest.sizeY, room.sizeY));
        // x1, y1 of the original: drawn, never used
        random.nextInt(Math.min(roomdest.sizeX, room.sizeX));
        random.nextInt(Math.min(roomdest.sizeY, room.sizeY));
        int dx = 0;
        int dy = 0;
        switch (side) {
            case 0 -> {
                x = room.posX - 1;
                dx = -1;
            }
            case 1 -> {
                y = room.posY + room.sizeY;
                dy = 1;
            }
            case 2 -> {
                x = room.posX + room.sizeX;
                dx = 1;
            }
            case 3 -> {
                y = room.posY - 1;
                dy = -1;
            }
            default -> {
                return;
            }
        }
        map[x][y] = 3;
        map[x + dx][y + dy] = 1;
    }

    private static int checkNeightbours(int x, int y, int[][] map) {
        int count = 0;
        count += checkNumAt(x - 1, y, map);
        count += checkNumAt(x + 1, y, map);
        count += checkNumAt(x, y + 1, map);
        count += checkNumAt(x, y - 1, map);
        count += checkNumAt(x + 1, y + 1, map);
        count += checkNumAt(x + 1, y - 1, map);
        count += checkNumAt(x - 1, y + 1, map);
        return count + checkNumAt(x - 1, y - 1, map);
    }

    private static int checkNumAt(int x, int y, int[][] map) {
        if (x <= 0 || y <= 0 || x >= map.length - 1 || y >= map.length - 1) {
            return 0;
        }
        return Math.max(map[x][y], 0);
    }

    /** DungeonRoom: a room of the plan, position and size in plan cells. */
    static final class DungeonRoom {
        int posX;
        int posY;
        int sizeX;
        int sizeY;

        DungeonRoom(int x, int y, int sx, int sy) {
            this.posX = x;
            this.posY = y;
            this.sizeX = sx;
            this.sizeY = sy;
        }

        /** Shrinks the room to the free cells of the plan and marks it there; false (and nothing marked) if it got too small. */
        boolean expand(int[][] map) {
            for (int x = 0; x < this.sizeX; x++) {
                for (int y = 0; y < this.sizeY; y++) {
                    int tx = x + this.posX;
                    int ty = y + this.posY;
                    if (checkNext(tx, ty, map)) {
                        if (map[tx + 1][ty] > 0) {
                            this.sizeX = this.sizeX - (this.sizeX - x);
                        }
                        if (map[tx][ty + 1] > 0) {
                            this.sizeY = this.sizeY - (this.sizeY - y);
                        }
                        if (map[tx - 1][ty] > 0) {
                            this.posX++;
                            this.sizeX--;
                        }
                        if (map[tx][ty - 1] > 0) {
                            this.posY++;
                            this.sizeY--;
                        }
                    } else {
                        if (tx > map.length - 2) {
                            this.sizeX = x - 1;
                        }
                        if (ty > map[0].length - 2) {
                            this.sizeY = y - 1;
                        }
                        if (tx < 1) {
                            this.sizeX = this.sizeX + this.posX;
                            this.posX = 1;
                        }
                        if (ty < 1) {
                            this.posY = 1;
                        }
                    }
                }
            }
            if ((this.sizeX >= 5 || this.sizeY >= 5) && this.sizeX >= 3 && this.sizeY >= 3) {
                for (int x = 0; x < this.sizeX; x++) {
                    for (int y = 0; y < this.sizeY; y++) {
                        if (map[this.posX + x][this.posY + y] == 0) {
                            map[this.posX + x][this.posY + y] = 1;
                        }
                    }
                }
                return true;
            }
            return false;
        }

        private static boolean checkNext(int x, int y, int[][] map) {
            return x <= map.length - 2 && x >= 1 && y <= map[0].length - 2 && y >= 1;
        }

        void decorate(int x, int y, int z, int roomHeight, DecoratorRoom roomsDecorator, OldWorld world, Random random) {
            y++;
            roomHeight -= 2;
            int roomType = roomsDecorator.getRandomRoomType(random);
            int roomCornerX = x + this.posX;
            int roomCornerZ = z + this.posY;
            if (this.sizeX > 3) {
                for (int i = 1; i < this.sizeX - 1; i++) {
                    if (world.getBlockId(roomCornerX + i, y, roomCornerZ - 1) != 64) {
                        roomsDecorator.placeDecorationBlock(random, world, roomCornerX + i, y, roomCornerZ, roomType, roomHeight, 3);
                    }
                    if (world.getBlockId(roomCornerX + i, y, roomCornerZ + this.sizeY) != 64) {
                        roomsDecorator.placeDecorationBlock(random, world, roomCornerX + i, y, roomCornerZ + this.sizeY - 1, roomType, roomHeight, 4);
                    }
                }
            }
            if (this.sizeY > 3) {
                for (int i = 1; i < this.sizeY - 1; i++) {
                    if (world.getBlockId(roomCornerX - 1, y, roomCornerZ + i) != 64) {
                        roomsDecorator.placeDecorationBlock(random, world, roomCornerX, y, roomCornerZ + i, roomType, roomHeight, 1);
                    }
                    if (world.getBlockId(roomCornerX + this.sizeX, y, roomCornerZ + i) != 64) {
                        roomsDecorator.placeDecorationBlock(random, world, roomCornerX + this.sizeX - 1, y, roomCornerZ + i, roomType, roomHeight, 2);
                    }
                }
            }
            if (this.sizeX > 4 && this.sizeY > 4) {
                roomsDecorator.placeDecorationBlock(random, world, roomCornerX + this.sizeX / 2, y, roomCornerZ + this.sizeY / 2, roomType, roomHeight, 5);
            }
        }
    }
}
