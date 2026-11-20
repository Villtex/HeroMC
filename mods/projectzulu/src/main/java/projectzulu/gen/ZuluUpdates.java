package projectzulu.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldWorld;

/**
 * {@code World.setBlock} with flag 3, as Project Zulu's generators call it ({@code setBlock(x, y, z, id)} and
 * {@code setBlock(..., 3)}): after a change the six neighbours get {@code onNeighborBlockChange}. Modelled are the
 * reactions that change blocks while the chunk is populated:
 * <ul>
 * <li>still water and lava turn into flowing (BlockStationary.setNotStationary), lava next to water hardens first;</li>
 * <li>flowers, grass, saplings, mushrooms, crops, lily pads and Project Zulu's plants that lost their soil or light
 * drop (BlockFlower.checkFlowerChange, setBlockToAir with flag 3);</li>
 * <li>snow layers without a block to lie on, cactus touching a solid block or without sand, sugar cane without
 * soil and water melt or break (flag 3, which notifies on);</li>
 * <li>door halves without the other half or the floor break, torches without their wall or floor drop.</li>
 * </ul>
 * Not modelled: the items these drop, sand and gravel that fall a tick later (the game lets them fall once the
 * chunk is live only if something updates them), redstone torches, rails, redstone and the plants of other mods.
 * No game classes (tools/expect.sh compiles this).
 */
final class ZuluUpdates {
    private ZuluUpdates() {
    }

    /**
     * World.setBlock(x, y, z, id, meta, 3). The neighbours learn the id of the block that was there before
     * (World.notifyBlockChange passes the old id on).
     */
    static boolean set(OldWorld world, int x, int y, int z, int id, int meta) {
        int oldId = world.getBlockId(x, y, z);
        if (!world.setBlock(x, y, z, id, meta)) {
            return false;
        }
        notifyNeighbors(world, x, y, z, oldId);
        return true;
    }

    /** World.setBlock(x, y, z, id): metadata 0, flag 3. */
    static boolean set(OldWorld world, int x, int y, int z, int id) {
        return set(world, x, y, z, id, 0);
    }

    /** World.notifyBlocksOfNeighborChange, in the order of 1.5.2. */
    static void notifyNeighbors(OldWorld world, int x, int y, int z, int changedId) {
        neighborChanged(world, x - 1, y, z, changedId);
        neighborChanged(world, x + 1, y, z, changedId);
        neighborChanged(world, x, y - 1, z, changedId);
        neighborChanged(world, x, y + 1, z, changedId);
        neighborChanged(world, x, y, z - 1, changedId);
        neighborChanged(world, x, y, z + 1, changedId);
    }

    private static void neighborChanged(OldWorld world, int x, int y, int z, int changedId) {
        if (y < 0 || y >= OldWorld.HEIGHT) {
            return;
        }
        int id = world.getBlockId(x, y, z);
        switch (id) {
            case 8 -> {
            }
            case 9 -> world.setBlock(x, y, z, 8, world.getBlockMetadata(x, y, z));
            case 10, 11 -> lava(world, x, y, z, id);
            case 78 -> {
                if (!world.snowCanStay(x, y, z)) {
                    set(world, x, y, z, 0);
                }
            }
            case 81 -> {
                if (!cactusCanStay(world, x, y, z)) {
                    set(world, x, y, z, 0);
                }
            }
            case 83 -> {
                if (!reedCanStay(world, x, y, z)) {
                    set(world, x, y, z, 0);
                }
            }
            case 64, 71 -> door(world, x, y, z, id, changedId);
            case 50 -> torch(world, x, y, z);
            default -> {
                if (isFlower(id) && !flowerCanStay(world, x, y, z, id)) {
                    // checkFlowerChange: setBlockToAir, which is flag 3 as well
                    set(world, x, y, z, 0);
                }
            }
        }
    }

    /** BlockFluid.checkForHarden, then BlockStationary.setNotStationary (flag 2) if the lava is still there. */
    private static void lava(OldWorld world, int x, int y, int z, int id) {
        if (world.material(x, y, z - 1) == Material.WATER || world.material(x, y, z + 1) == Material.WATER
                || world.material(x - 1, y, z) == Material.WATER || world.material(x + 1, y, z) == Material.WATER
                || world.material(x, y + 1, z) == Material.WATER) {
            int meta = world.getBlockMetadata(x, y, z);
            if (meta == 0) {
                set(world, x, y, z, 49);
            } else if (meta <= 4) {
                set(world, x, y, z, 4);
            }
        }
        if (id == 11 && world.getBlockId(x, y, z) == 11) {
            world.setBlock(x, y, z, 10, world.getBlockMetadata(x, y, z));
        }
    }

    /**
     * BlockDoor.onNeighborBlockChange of the door with id {@code id} for the block at x, y, z, without the redstone
     * part (no power source is generated next to the doors). The upper half hands the call on to the block below
     * whatever it is, as the original did ({@code this.onNeighborBlockChange(world, x, y - 1, z, par5)}).
     */
    private static void door(OldWorld world, int x, int y, int z, int id, int changedId) {
        int meta = world.getBlockMetadata(x, y, z);
        if ((meta & 8) == 0) {
            if (world.getBlockId(x, y + 1, z) != id) {
                set(world, x, y, z, 0);
            }
            if (!world.hasSolidTop(x, y - 1, z)) {
                set(world, x, y, z, 0);
                if (world.getBlockId(x, y + 1, z) == id) {
                    set(world, x, y + 1, z, 0);
                }
            }
        } else {
            if (world.getBlockId(x, y - 1, z) != id) {
                set(world, x, y, z, 0);
            }
            if (changedId > 0 && changedId != id) {
                door(world, x, y - 1, z, id, changedId);
            }
        }
    }

    /** BlockTorch.onNeighborBlockChange: a torch nothing holds, or whose own wall or floor went, drops. */
    private static void torch(OldWorld world, int x, int y, int z) {
        boolean west = world.isSolidOnSide(x - 1, y, z, 5);
        boolean east = world.isSolidOnSide(x + 1, y, z, 4);
        boolean north = world.isSolidOnSide(x, y, z - 1, 3);
        boolean south = world.isSolidOnSide(x, y, z + 1, 2);
        boolean floor = canPlaceTorchOn(world, x, y - 1, z);
        if (!west && !east && !north && !south && !floor) {
            set(world, x, y, z, 0);
            return;
        }
        int meta = world.getBlockMetadata(x, y, z);
        if (!west && meta == 1 || !east && meta == 2 || !north && meta == 3 || !south && meta == 4 || !floor && meta == 5) {
            set(world, x, y, z, 0);
        }
    }

    /** World.canPlaceTorchOn: a solid top, or a fence, glass or a wall (Block.canPlaceTorchOnTop). */
    private static boolean canPlaceTorchOn(OldWorld world, int x, int y, int z) {
        if (world.hasSolidTop(x, y, z)) {
            return true;
        }
        int id = world.getBlockId(x, y, z);
        return id == 85 || id == 113 || id == 20 || id == 139;
    }

    /** BlockCactus.canBlockStay. */
    private static boolean cactusCanStay(OldWorld world, int x, int y, int z) {
        if (world.material(x - 1, y, z).solid || world.material(x + 1, y, z).solid || world.material(x, y, z - 1).solid
                || world.material(x, y, z + 1).solid) {
            return false;
        }
        int below = world.getBlockId(x, y - 1, z);
        return below == 81 || below == 12;
    }

    /** BlockReed.canBlockStay with Forge's canSustainPlant (beach plant: grass, dirt or sand next to water). */
    private static boolean reedCanStay(OldWorld world, int x, int y, int z) {
        int below = world.getBlockId(x, y - 1, z);
        if (below == 83) {
            return true;
        }
        if (below != 2 && below != 3 && below != 12) {
            return false;
        }
        return world.material(x - 1, y - 1, z) == Material.WATER || world.material(x + 1, y - 1, z) == Material.WATER
                || world.material(x, y - 1, z - 1) == Material.WATER || world.material(x, y - 1, z + 1) == Material.WATER;
    }

    /** BlockFlower and its subclasses whose canBlockStay is modelled. */
    private static boolean isFlower(int id) {
        return switch (id) {
            case 6, 31, 32, 37, 38, 39, 40, 59, 104, 105, 111, 141, 142, ZuluIds.ALOE_VERA, ZuluIds.CREEPER_BLOSSOM, ZuluIds.NIGHT_BLOOM,
                    ZuluIds.PALM_TREE_SAPLING -> true;
            default -> false;
        };
    }

    /**
     * BlockFlower.canBlockStay of Forge 7.8: light 8 or open sky, and soil that canSustainPlant (the plant's own
     * canThisPlantGrowOnThisBlockID, or grass and dirt for plains plants). Mushrooms: dark and an opaque or
     * solid-topped block, or mycelium.
     */
    private static boolean flowerCanStay(OldWorld world, int x, int y, int z, int id) {
        int soil = world.getBlockId(x, y - 1, z);
        if (id == 111) {
            // BlockLilyPad.canBlockStay: still water source below, no light check
            return world.material(x, y - 1, z) == Material.WATER && world.getBlockMetadata(x, y - 1, z) == 0;
        }
        if (id == 39 || id == 40) {
            if (soil == 110) {
                return true;
            }
            return world.skyLight(x, y, z) < 13 && (OldBlockInfo.opaqueCube(soil) || world.hasSolidTop(x, y - 1, z));
        }
        if (world.skyLight(x, y, z) < 8 && !world.canBlockSeeTheSky(x, y, z)) {
            return false;
        }
        return switch (id) {
            case 32 -> soil == 12;
            case 59, 104, 105, 141, 142 -> soil == 60;
            case ZuluIds.ALOE_VERA -> soil == 2 || soil == 3 || soil == 60 || soil == 12 || soil == ZuluIds.WATERED_DIRT
                    || soil == ZuluIds.ALOE_VERA;
            case ZuluIds.NIGHT_BLOOM -> soil == 2 || soil == 3 || soil == 60 || soil == ZuluIds.WATERED_DIRT;
            case ZuluIds.CREEPER_BLOSSOM -> soil == 2 || soil == 3 || soil == 60 || soil == 17;
            case ZuluIds.PALM_TREE_SAPLING -> soil == 2 || soil == 3 || soil == 12;
            default -> soil == 2 || soil == 3 || soil == 60;
        };
    }
}
