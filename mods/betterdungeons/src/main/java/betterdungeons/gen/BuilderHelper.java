package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The building engine of Better Dungeons (chocolate.mods.BD.builder.BuilderHelper) on the 1.5.2 world of worldgen152:
 * puts templates (NBT and the older text maps) and generated block maps into the world, with chests, spawners,
 * mob blocks, bosses, doors, furnaces, dispensers and the entities of a template.
 *
 * <p>Where the original drew from an unseeded {@code new Random()} (the contents of the mod's own chests, the
 * dispensers and furnaces of copySpecialBlocks), a random seeded from the world seed and the position is used, so
 * that the world only depends on the seed.
 */
public final class BuilderHelper {
    /** The mod's spawner block: blockID + 1 of the pack's Chocolate/config.prop (blockID = 2001). */
    public static final int SPAWNER_BLOCK = 2002;
    public static final int MOB_SPAWNER_BLOCK = 300;
    public static final int CUSTOM_CHEST_BLOCK = 301;
    public static final int BOSS_BLOCK = 302;

    private record BlockData(int x, int y, int z, int blockID, int blockMetadata) {
    }

    public final OldWorld world;
    private List<BlockData> specialBlocks = new ArrayList<>();

    public BuilderHelper(OldWorld world) {
        this.world = world;
    }

    // ---- block maps of the builders ----

    public void fillMap(OldWorld world, Random random, int[][][] map, int i, int height, int k, int idWalls) {
        this.fillMap(world, random, map, i, height, k, idWalls, 0);
    }

    public void fillMap(OldWorld world, Random random, int[][][] map, int i, int height, int k, int idWalls, int idMob) {
        for (int y = 0; y < map[0].length; y++) {
            for (int x = 0; x < map.length; x++) {
                for (int z = 0; z < map[0][0].length; z++) {
                    int id = world.getBlockId(i + x, height + y, k + z);
                    int value = map[x][y][z];
                    if (value != -1 && id != 54) {
                        if (value == 54) {
                            int r = random.nextInt(10);
                            if (r < 3) {
                                addChest(random, world, i + x, height + y, k + z);
                            } else if (r < 5) {
                                addCustomBlock(i + x, height + y, k + z, CUSTOM_CHEST_BLOCK, 1, world, 1);
                            } else if (r < 7) {
                                addCustomBlock(i + x, height + y, k + z, CUSTOM_CHEST_BLOCK, 1, world, 2);
                            } else if (r < 9) {
                                addCustomBlock(i + x, height + y, k + z, CUSTOM_CHEST_BLOCK, 1, world, 3);
                            } else {
                                addCustomBlock(i + x, height + y, k + z, CUSTOM_CHEST_BLOCK, 1, world, 0);
                            }
                        } else if (value == 52) {
                            addSpawner(random, world, x + i, y + height, z + k, idMob);
                        } else if (value == 64) {
                            world.placeDoor(x + i, y + height, z + k, map[x][y][z + 1] == 0 ? 1 : 0, random.nextInt(100) != 0 ? 64 : 71);
                        } else if (value == 1) {
                            if (idWalls != -1) {
                                world.setBlock(i + x, height + y, k + z, idWalls, 0);
                            }
                        } else if (value == MOB_SPAWNER_BLOCK) {
                            addCustomBlock(i + x, height + y, k + z, MOB_SPAWNER_BLOCK, 1, world, 7);
                        } else if (value == BOSS_BLOCK) {
                            addCustomBlock(i + x, height + y, k + z, BOSS_BLOCK, 2, world, 0);
                        } else {
                            world.setBlock(i + x, height + y, k + z, value, 0);
                        }
                    }
                }
            }
        }
    }

    /** The text templates: "id.meta" per block, -1 keeps what is there. */
    public void fillMap(OldWorld world, Random random, float[][][] map, int i, int height, int k, int idMob) {
        this.fillFloatMap(world, random, map, i, height, k, idMob, -1);
    }

    /** Like {@link #fillMap(OldWorld, Random, float[][][], int, int, int, int)}, spawners with the dungeon mob {@code idSpawner}. */
    public void fillMap(OldWorld world, Random random, float[][][] map, int i, int height, int k, int idMob, int idSpawner) {
        this.fillFloatMap(world, random, map, i, height, k, idMob, idSpawner);
    }

    private void fillFloatMap(OldWorld world, Random random, float[][][] map, int i, int height, int k, int idMob, int idSpawner) {
        this.specialBlocks = new ArrayList<>();
        for (int y = 0; y < map[0].length; y++) {
            for (int x = 0; x < map.length; x++) {
                for (int z = 0; z < map[0][0].length; z++) {
                    int id = world.getBlockId(i + x, height + y, k + z);
                    float value = map[x][y][z];
                    if (id != 54 && value != -1.0F) {
                        int metadata = (int) (value * 100.0 - (int) value * 100.0 + 0.01);
                        if (this.addSpecialBlock(i + x, height + y, k + z, (int) value, metadata) || !(value < 300.0F)) {
                            addCustomBlock(x + i, y + height, z + k, (int) value, metadata, world, idMob);
                        } else if (value == 52.0F) {
                            if (idSpawner < 0) {
                                addRandomSpawner(random, world, x + i, y + height, z + k);
                            } else {
                                addSpawner(random, world, x + i, y + height, z + k, idSpawner);
                            }
                        } else if ((int) value == 54) {
                            addChest(random, world, x + i, y + height, z + k);
                        } else {
                            world.setBlock(x + i, y + height, z + k, (int) value, metadata);
                        }
                    }
                }
            }
        }
        this.copySpecialBlocks(world);
    }

    // ---- NBT templates ----

    public void fillMap(OldWorld world, Random random, BDTemplate nbt, int i, int j, int k, int idMob) {
        this.fillMap(world, random, nbt, i, j, k, idMob, true);
    }

    public void fillMap(OldWorld world, Random random, BDTemplate nbt, int i, int j, int k, int idMob, boolean replaceChestAndSpawners) {
        int sx = nbt.sx;
        int sy = nbt.sy;
        int sz = nbt.sz;
        int cont = 0;
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    int currentID = world.getBlockId(i + x, j + y, k + z);
                    if (currentID == 54) {
                        world.setBlock(i + x, j + y, k + z, 0, 0);
                    }
                    int index = cont++;
                    int id = nbt.id(index);
                    boolean flagNBTSpecialBlock = false;
                    if (replaceChestAndSpawners) {
                        flagNBTSpecialBlock = this.addNBTSpecialBlock(world, i + x, j + y, k + z, id, idMob, nbt, index, random);
                    }
                    if (id == 8000) {
                        addCustomBlock(i + x, j + y, k + z, MOB_SPAWNER_BLOCK, nbt.metadata(index), world, idMob);
                    } else if (id == 8001) {
                        addCustomBlock(i + x, j + y, k + z, CUSTOM_CHEST_BLOCK, nbt.metadata(index), world, idMob);
                    } else if (id == 8002) {
                        addCustomBlock(i + x, j + y, k + z, BOSS_BLOCK, nbt.metadata(index), world, idMob);
                    } else if (!flagNBTSpecialBlock && checkIfPlacedOnFirstPass(id) && id != 8003) {
                        if (id != 0) {
                            world.setBlockToAir(i + x, j + y, k + z);
                            int md = nbt.metadata(index);
                            if (id != 71 && id != 64) {
                                world.setBlock(i + x, j + y, k + z, id, md);
                            } else {
                                world.placeDoor(i + x, j + y, k + z, md, id);
                            }
                            if (isContainer(id)) {
                                world.setMetadata(i + x, j + y, k + z, md);
                                OldTile tile = tileFromNbt(id, nbt.nbt(index));
                                if (tile != null) {
                                    world.setTile(i + x, j + y, k + z, tile);
                                }
                                world.setMetadata(i + x, j + y, k + z, md);
                            }
                        } else {
                            world.setBlockToAir(i + x, j + y, k + z);
                        }
                    }
                }
            }
        }
        this.recopy(nbt, world, i, j, k);
        for (Object entry : nbt.entities) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entity = (Map<String, Object>) entry;
            OldEntity e = entityFromNbt(entity, i, j, k);
            if (e != null) {
                world.spawnEntity(e);
            }
        }
    }

    /** The blocks that need a block they stand on or hang at: placed in the second pass. */
    public static boolean checkIfPlacedOnFirstPass(int id) {
        return id != 55 && id != 76 && id != 75 && id != 77 && id != 143 && id != 69 && id != 50 && id != 65 && id != 26;
    }

    public void recopy(BDTemplate nbt, OldWorld world, int i, int j, int k) {
        int cont = 0;
        this.specialBlocks = new ArrayList<>();
        for (int x = 0; x < nbt.sx; x++) {
            for (int y = 0; y < nbt.sy; y++) {
                for (int z = 0; z < nbt.sz; z++) {
                    int index = cont++;
                    int id = nbt.id(index);
                    if (!checkIfPlacedOnFirstPass(id)) {
                        if (id != 76 && id != 75 && id != 69) {
                            this.world.setBlock(i + x, j + y, k + z, id, nbt.metadata(index));
                        } else {
                            this.addSpecialBlock(i + x, j + y, k + z, id, nbt.metadata(index));
                        }
                    }
                }
            }
        }
        this.copySpecialBlocks(world);
    }

    /** Chests, pig spawners, furnaces and dispensers of a template get their content here. */
    public boolean addNBTSpecialBlock(OldWorld world, int x, int y, int z, int id, int mobId, BDTemplate template, int index, Random random) {
        if (id == 52) {
            if (BDNbt.getString(template.nbt(index), "EntityId").equals("Pig")) {
                addSpawner(random, world, x, y, z, mobId);
                return true;
            }
            return false;
        }
        if (id == 54) {
            addChest(random, world, x, y, z);
            return true;
        }
        if (id == 61 || id == 62) {
            Map<String, Object> nbt = template.nbt(index);
            int md = BDNbt.getInt(nbt, "metadata");
            world.setBlock(x, y, z, id, md);
            OldTile.Furnace furnace = new OldTile.Furnace(slots(nbt, 3), BDNbt.getInt(nbt, "BurnTime"), BDNbt.getInt(nbt, "CookTime"));
            world.setTile(x, y, z, furnace);
            world.setMetadata(x, y, z, md);
            if (random.nextInt(15) == 0) {
                furnace.slots()[0] = new OldStack(266, 0, random.nextInt(3) + 1);
            }
            if (random.nextInt(3) == 0) {
                furnace.slots()[1] = new OldStack(263, 0, random.nextInt(45) + 1);
            }
            return true;
        }
        if (id != 23) {
            return false;
        }
        Map<String, Object> nbt = template.nbt(index);
        int md = BDNbt.getInt(nbt, "metadata");
        world.setBlock(x, y, z, id, md);
        OldTile.Dispenser dispenser = new OldTile.Dispenser(slots(nbt, 9));
        world.setTile(x, y, z, dispenser);
        world.setMetadata(x, y, z, md);
        boolean hasItems = false;
        for (OldStack stack : dispenser.slots()) {
            if (stack != null) {
                hasItems = true;
                break;
            }
        }
        if (!hasItems) {
            fillDispenser(dispenser, random);
        }
        return true;
    }

    private static void fillDispenser(OldTile.Dispenser dispenser, Random random) {
        for (int v = 0; v < 9; v++) {
            if (random.nextInt(20) == 0) {
                if (random.nextInt(50) == 0) {
                    dispenser.slots()[v] = new OldStack(384, 0, random.nextInt(64) + 1);
                }
                dispenser.slots()[v] = new OldStack(385, 0, random.nextInt(64) + 1);
            } else {
                dispenser.slots()[v] = new OldStack(262, 0, random.nextInt(64) + 1);
            }
        }
    }

    public boolean addSpecialBlock(int x, int y, int z, int id, int blockMetadata) {
        if (id != 50 && id != 64 && id != 65 && id != 69 && id != 71 && id != 75 && id != 76 && id != 77 && id != 143 && id != 23
                && id != 62 && id != 61) {
            return false;
        }
        this.specialBlocks.add(new BlockData(x, y, z, id, blockMetadata));
        return true;
    }

    public void copySpecialBlocks(OldWorld world) {
        if (this.specialBlocks.isEmpty()) {
            return;
        }
        for (BlockData b : this.specialBlocks) {
            int x = b.x;
            int y = b.y;
            int z = b.z;
            if (b.blockID == 69) {
                world.setBlock(x, y, z, b.blockID, 0);
                world.setMetadata(x, y, z, b.blockMetadata);
            } else if (b.blockID != 64) {
                // (the original tested the wooden door twice, so iron doors go the plain way below)
                if (b.blockID != 61 && b.blockID != 62) {
                    if (b.blockID == 23) {
                        world.setBlock(x, y, z, b.blockID, b.blockMetadata);
                        OldTile.Dispenser dispenser = new OldTile.Dispenser();
                        world.setTile(x, y, z, dispenser);
                        fillDispenser(dispenser, positionRandom(world, x, y, z));
                    } else if (b.blockID == 303) {
                        // paintings of the block maps (never reached: 303 is not a special block)
                        world.setBlock(x, y, z, 0, 0);
                    } else {
                        world.setBlock(x, y, z, b.blockID, b.blockMetadata);
                    }
                } else {
                    world.setBlock(x, y, z, b.blockID, b.blockMetadata);
                    OldTile.Furnace furnace = new OldTile.Furnace();
                    world.setTile(x, y, z, furnace);
                    Random rand = positionRandom(world, x, y, z);
                    if (rand.nextInt(15) == 0) {
                        furnace.slots()[0] = new OldStack(266, 0, rand.nextInt(3) + 1);
                    }
                    if (rand.nextInt(3) == 0) {
                        furnace.slots()[1] = new OldStack(263, 0, rand.nextInt(45) + 1);
                    }
                }
            } else if (b.blockMetadata < 8) {
                world.placeDoor(x, y, z, b.blockMetadata, b.blockID);
            }
        }
        this.specialBlocks.clear();
    }

    // ---- spawners, chests, mob blocks, bosses ----

    public static void addSpawner(Random random, OldWorld world, int x, int y, int z, int idMob) {
        world.setBlock(x, y, z, 52, 0);
        String mob = BDMobs.LIST.get(idMob).spawner();
        world.setTile(x, y, z, new OldTile.Spawner(mob != null ? mob : OldLoot.randomDungeonMob(random)));
    }

    public static void addRandomSpawner(Random random, OldWorld world, int x, int y, int z) {
        world.setBlock(x, y, z, 52, 0);
        world.setTile(x, y, z, new OldTile.Spawner(OldLoot.randomDungeonMob(random)));
    }

    /** 300 the mod's spawner, 301 a chest of one kind (metadata 0 treasure, 1 food, 2 minerals, 3 weapons), 302 a boss, 303 a painting place. */
    public static void addCustomBlock(int x, int y, int z, int block, int metadata, OldWorld world, int mob) {
        OldEntity boss = null;
        if (block == MOB_SPAWNER_BLOCK) {
            world.setBlock(x, y, z, SPAWNER_BLOCK, 0);
            world.setTile(x, y, z, new BDSpawnerTile(mob, metadata));
        }
        if (block == CUSTOM_CHEST_BLOCK) {
            Random random = positionRandom(world, x, y, z);
            if (metadata == 0) {
                addTreasure(random, world, x, y, z);
            } else if (metadata == 1) {
                addFoodChest(random, world, x, y, z);
            } else if (metadata == 2) {
                addMineralChest(random, world, x, y, z);
            } else if (metadata == 3) {
                addWeaponChest(random, world, x, y, z);
            }
        }
        if (block == BOSS_BLOCK) {
            String name = switch (metadata) {
                case 0 -> "BetterDungeons.SpiderBoss";
                case 1 -> "BetterDungeons.SlimeBoss";
                case 2 -> "BetterDungeons.Monking";
                case 3 -> "BetterDungeons.TurtleBoss";
                case 4 -> "BetterDungeons.IceBull";
                default -> null;
            };
            if (name != null) {
                world.setBlock(x, y, z, 0, 0);
                boss = new OldEntity.Mob(name, x + 0.5, y + 0.5, z + 0.5, 0.0F, null);
            }
        }
        if (block == 303) {
            world.setBlock(x, y, z, 0, 0);
        }
        if (boss != null) {
            world.spawnEntity(boss);
            world.setBlock(x, y, z, 0, 0);
        }
    }

    /** Replaces the {@code new Random()} of the original: the same position of the same world always gives the same chest. */
    static Random positionRandom(OldWorld world, int x, int y, int z) {
        return new Random(world.seed() ^ x * 341873128712L ^ z * 132897987541L ^ y * 42317861L ^ 0x5DEECE66DL);
    }

    private static OldTile.Chest chest(OldWorld world, int x, int y, int z) {
        world.setBlock(x, y, z, 54, 0);
        OldTile.Chest chest = new OldTile.Chest();
        world.setTile(x, y, z, chest);
        return chest;
    }

    public static boolean addTreasure(Random random, OldWorld world, int x, int y, int z) {
        OldTile.Chest chest = chest(world, x, y, z);
        int itemsCount = random.nextInt(4) + 2;
        for (int f = 1; f < itemsCount + 1; f++) {
            chest.slots()[f] = BDLoot.random(BDLoot.TREASURE, random).stack();
        }
        if (random.nextInt(2) == 0) {
            chest.slots()[0] = new OldStack(2256 + random.nextInt(12), 0, 1);
        }
        return true;
    }

    public static boolean addFoodChest(Random random, OldWorld world, int x, int y, int z) {
        OldTile.Chest chest = chest(world, x, y, z);
        int itemsCount = random.nextInt(6) + 2;
        for (int f = 0; f < itemsCount; f++) {
            chest.slots()[f] = BDLoot.random(BDLoot.FOOD, random).stack();
        }
        return true;
    }

    public static boolean addMineralChest(Random random, OldWorld world, int x, int y, int z) {
        OldTile.Chest chest = chest(world, x, y, z);
        int itemsCount = random.nextInt(6) + 2;
        for (int f = 0; f < itemsCount; f++) {
            chest.slots()[f] = BDLoot.random(BDLoot.MINERALS, random).stack();
        }
        return true;
    }

    // Enchantment ids of 1.5.2
    private static final int PROTECTION = 0;
    private static final int FIRE_PROTECTION = 1;
    private static final int FEATHER_FALLING = 2;
    private static final int BLAST_PROTECTION = 3;
    private static final int PROJECTILE_PROTECTION = 4;
    private static final int RESPIRATION = 5;
    private static final int AQUA_AFFINITY = 6;
    private static final int THORNS = 7;
    private static final int SHARPNESS = 16;
    private static final int SMITE = 17;
    private static final int BANE_OF_ARTHROPODS = 18;
    private static final int KNOCKBACK = 19;
    private static final int FIRE_ASPECT = 20;

    public static boolean addWeaponChest(Random random, OldWorld world, int x, int y, int z) {
        OldTile.Chest chest = chest(world, x, y, z);
        int itemsCount = random.nextInt(6) + 1;
        for (int f = 0; f < itemsCount; f++) {
            BDLoot.Entry entry = BDLoot.random(BDLoot.WEAPONS, random);
            OldStack stack = entry.stack();
            if (entry.armorType() >= 0) {
                if (entry.armorType() == 0) {
                    int cant = 7;
                    stack = maybe(stack, random, cant, PROTECTION, 4);
                    stack = maybe(stack, random, cant, FIRE_PROTECTION, 4);
                    stack = maybe(stack, random, cant, BLAST_PROTECTION, 4);
                    stack = maybe(stack, random, cant, PROJECTILE_PROTECTION, 4);
                    stack = maybe(stack, random, cant, RESPIRATION, 3);
                    if (random.nextInt(cant) == 0) {
                        stack = stack.withEnchantment(AQUA_AFFINITY, 1);
                    }
                } else if (entry.armorType() == 3) {
                    int cant = 5;
                    stack = maybe(stack, random, cant, PROTECTION, 4);
                    stack = maybe(stack, random, cant, FIRE_PROTECTION, 4);
                    stack = maybe(stack, random, cant, BLAST_PROTECTION, 4);
                    stack = maybe(stack, random, cant, PROJECTILE_PROTECTION, 4);
                    stack = maybe(stack, random, cant, FEATHER_FALLING, 4);
                } else {
                    int cant = 6;
                    stack = maybe(stack, random, cant, PROTECTION, 4);
                    stack = maybe(stack, random, cant, FIRE_PROTECTION, 4);
                    stack = maybe(stack, random, cant, BLAST_PROTECTION, 4);
                    stack = maybe(stack, random, cant, PROJECTILE_PROTECTION, 4);
                    stack = maybe(stack, random, cant, THORNS, 3);
                }
            } else if (entry.sword()) {
                int cant = 7;
                stack = maybe(stack, random, cant, SHARPNESS, 2);
                stack = maybe(stack, random, cant, FIRE_ASPECT, 2);
                stack = maybe(stack, random, cant, SMITE, 5);
                stack = maybe(stack, random, cant, BANE_OF_ARTHROPODS, 5);
                if (random.nextInt(cant) == 0) {
                    stack = stack.withEnchantment(KNOCKBACK, 1);
                }
            }
            chest.slots()[f] = stack;
        }
        return true;
    }

    /** "if (random.nextInt(cant) == 0) itemstack.addEnchantment(e, random.nextInt(levels) + 1)". */
    private static OldStack maybe(OldStack stack, Random random, int cant, int enchantment, int levels) {
        return random.nextInt(cant) == 0 ? stack.withEnchantment(enchantment, random.nextInt(levels) + 1) : stack;
    }

    public static boolean addChest(Random random, OldWorld world, int x, int y, int z) {
        OldTile.Chest chest = chest(world, x, y, z);
        int itemsCount = random.nextInt(8) + 1;
        int f = 0;
        if (random.nextInt(150) == 0) {
            chest.slots()[f] = new OldStack(2256 + random.nextInt(2), 0, 1);
            f++;
            itemsCount++;
        }
        while (f < itemsCount) {
            chest.slots()[f] = BDLoot.random(BDLoot.CHEST, random).stack();
            f++;
        }
        return true;
    }

    // ---- tile entities and entities of a template ----

    /** Blocks that were a BlockContainer in 1.5.2: their tile entity is read from the template. */
    static boolean isContainer(int id) {
        return switch (id) {
            case 23, 25, 52, 54, 61, 62, 63, 68, 84, 116, 117, 119, 130, 137, 138, 144, 146, 149, 150, 151, 154, 158 -> true;
            default -> false;
        };
    }

    private static OldTile tileFromNbt(int id, Map<String, Object> nbt) {
        return switch (id) {
            case 52 -> new OldTile.Spawner(BDNbt.getString(nbt, "EntityId"));
            case 63, 68 -> new OldTile.Sign(new String[] {BDNbt.getString(nbt, "Text1"), BDNbt.getString(nbt, "Text2"),
                    BDNbt.getString(nbt, "Text3"), BDNbt.getString(nbt, "Text4")});
            case 84 -> {
                // TileEntityRecordPlayer: "RecordItem", or an older plain "Record" id
                Map<String, Object> item = BDNbt.getCompound(nbt, "RecordItem");
                OldStack record = !item.isEmpty() ? stack(item) : BDNbt.getInt(nbt, "Record") > 0 ? new OldStack(BDNbt.getInt(nbt, "Record"), 0, 1) : null;
                yield new OldTile.Jukebox(record);
            }
            case 117 -> new OldTile.BrewingStand(slots(nbt, 4), BDNbt.getInt(nbt, "BrewTime"));
            case 144 -> new OldTile.Skull(BDNbt.getInt(nbt, "SkullType"), BDNbt.getInt(nbt, "Rot"));
            case 54, 146 -> new OldTile.Chest(slots(nbt, 27));
            case 23, 158 -> new OldTile.Dispenser(slots(nbt, 9));
            default -> null;
        };
    }

    /** An "Items" list ({Slot, id, Damage, Count}) as slots. */
    private static OldStack[] slots(Map<String, Object> nbt, int size) {
        OldStack[] slots = new OldStack[size];
        for (Object entry : BDNbt.getList(nbt, "Items")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> item = (Map<String, Object>) entry;
            int slot = BDNbt.getInt(item, "Slot") & 255;
            if (slot < size) {
                slots[slot] = stack(item);
            }
        }
        return slots;
    }

    private static OldStack stack(Map<String, Object> item) {
        if (BDNbt.getInt(item, "id") <= 0 || BDNbt.getInt(item, "Count") <= 0) {
            return null;
        }
        return new OldStack(BDNbt.getInt(item, "id"), BDNbt.getInt(item, "Damage"), BDNbt.getInt(item, "Count"));
    }

    /** EntityList.createEntityFromNBT, moved by the template position like fillMap did. */
    private static OldEntity entityFromNbt(Map<String, Object> nbt, int i, int j, int k) {
        String id = BDNbt.getString(nbt, "id");
        List<Object> pos = BDNbt.getList(nbt, "Pos");
        List<Object> rotation = BDNbt.getList(nbt, "Rotation");
        if (id.equals("Painting") || id.equals("ItemFrame")) {
            // EntityHanging.readEntityFromNBT: "Direction", else the older "Dir"
            int direction;
            if (nbt.containsKey("Direction")) {
                direction = BDNbt.getInt(nbt, "Direction");
            } else {
                direction = switch (BDNbt.getInt(nbt, "Dir")) {
                    case 0 -> 2;
                    case 1 -> 1;
                    case 2 -> 0;
                    default -> 3;
                };
            }
            int x = BDNbt.getInt(nbt, "TileX") + i;
            int y = BDNbt.getInt(nbt, "TileY") + j;
            int z = BDNbt.getInt(nbt, "TileZ") + k;
            if (id.equals("Painting")) {
                return new OldEntity.Painting(x, y, z, direction, BDNbt.getString(nbt, "Motive"));
            }
            Map<String, Object> item = BDNbt.getCompound(nbt, "Item");
            return new OldEntity.ItemFrame(x, y, z, direction, item.isEmpty() ? null : stack(item), BDNbt.getInt(nbt, "ItemRotation"));
        }
        if (pos.size() < 3) {
            return null;
        }
        double x = ((Number) pos.get(0)).doubleValue() + i;
        double y = ((Number) pos.get(1)).doubleValue() + j;
        double z = ((Number) pos.get(2)).doubleValue() + k;
        if (id.equals("Item")) {
            OldStack stack = stack(BDNbt.getCompound(nbt, "Item"));
            return stack == null ? null : new OldEntity.Item(x, y, z, stack);
        }
        float yaw = rotation.isEmpty() ? 0.0F : ((Number) rotation.get(0)).floatValue();
        List<Object> equipment = BDNbt.getList(nbt, "Equipment");
        OldStack[] worn = new OldStack[equipment.size()];
        for (int s = 0; s < worn.length; s++) {
            @SuppressWarnings("unchecked")
            Map<String, Object> item = (Map<String, Object>) equipment.get(s);
            worn[s] = stack(item);
        }
        return new OldEntity.Mob(id, x, y, z, yaw, worn);
    }
}
