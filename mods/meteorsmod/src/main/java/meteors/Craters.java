package meteors;

import meteors.block.MeteorBlock;
import meteors.entity.AlienCreeper;
import meteors.entity.CometKitty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * The crash generators (crash/CrashMeteorite and its subclasses): what a meteor leaves behind once it hit the ground
 * and exploded. The random numbers come from the meteor entity, in the order of the original.
 */
public final class Craters {
    private Craters() {
    }

    /**
     * EntityMeteor.getWorldGen + generate + afterCrashCompleted for a meteor of that type and size (clamped to 1..3) at
     * the block i, j, k, after its explosion destroyed {@code exploded}.
     */
    public static void crash(ServerLevel level, RandomSource random, MeteorType type, int size, List<BlockPos> exploded, int i, int j, int k) {
        int crashSize = Mth.clamp(size, 1, 3);
        switch (type) {
            case UNKNOWN -> unknown(level, random, crashSize, i, j, k);
            case KITTY -> {
                // CrashKitty: no crater, size² comet kitties
                int kitties = (int) Math.pow(crashSize, 2.0);
                for (int c = 0; c < kitties; c++) {
                    CometKitty kitty = MeteorsMod.COMET_KITTY.get().create(level, EntitySpawnReason.EVENT);
                    if (kitty != null) {
                        kitty.setPos(i, j, k);
                        level.addFreshEntity(kitty);
                    }
                }
            }
            default -> {
                crater(level, random, type, crashSize, i, j, k);
                switch (type) {
                    case FREZARITE -> snow(level, random, exploded);
                    case KREKNORITE -> {
                        if (crashSize >= 2) {
                            portal(level, i, j, k, random.nextBoolean());
                        }
                        spawn(level, random, exploded, random.nextInt(3), false);
                        if (crashSize >= 2) {
                            level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("Meteor.netherPortalCreation"), false);
                        }
                    }
                    default -> spawn(level, random, exploded, random.nextInt(5), true);
                }
            }
        }
    }

    /**
     * CrashMeteorite.generate: within 4 * size of the impact 3 of 10 solid blocks become the meteor's material (1 of
     * 45 the rare one), hot (meta 1..4); the two layers at the bottom become it completely. Bedrock, water and the
     * meteor's own blocks stay. (meteorsAboveAndBelow was always 0 by operator precedence and is left out.)
     */
    static void crater(ServerLevel level, RandomSource random, MeteorType type, int crashSize, int i, int j, int k) {
        int r = 4 * crashSize;
        for (int y = j + r; y >= j - r; y--) {
            for (int x = i + r; x >= i - r; x--) {
                for (int z = k + r; z >= k - r; z--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.getBlockState(pos).isAir() && random.nextInt(10) + 1 > 7 && replaceable(level, type, pos)) {
                        place(level, random, type, pos);
                    }
                }
            }
        }
        for (int y = j - r; y >= j - (r + 1); y--) {
            for (int x = i + r; x >= i - r; x--) {
                for (int z = k + r; z >= k - r; z--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.getBlockState(pos).isAir() && replaceable(level, type, pos)) {
                        place(level, random, type, pos);
                    }
                }
            }
        }
    }

    private static void place(ServerLevel level, RandomSource random, MeteorType type, BlockPos pos) {
        Block block = random.nextInt(45) == 25 ? type.rareMaterial() : type.material();
        if (block != Blocks.ICE && block != Blocks.LAVA) {
            level.setBlock(pos, MeteorBlock.hot(block.defaultBlockState(), random.nextInt(4) + 1), 3);
        } else {
            level.setBlock(pos, block.defaultBlockState(), 2);
        }
    }

    /** checkBlockIDs. */
    private static boolean replaceable(ServerLevel level, MeteorType type, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.is(Blocks.BEDROCK) && !state.is(type.material()) && !state.is(type.rareMaterial()) && !state.is(Blocks.WATER);
    }

    /** afterCraterFormed of CrashMeteorite (alien creepers, up to 4) and CrashKreknorite (blazes, up to 2). */
    private static void spawn(ServerLevel level, RandomSource random, List<BlockPos> exploded, int count, boolean creepers) {
        for (int n = exploded.size() - 1; n >= 0 && count > 0; n--) {
            BlockPos pos = exploded.get(n);
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolidRender() && random.nextInt(10) > 4) {
                if (creepers) {
                    AlienCreeper creeper = MeteorsMod.ALIEN_CREEPER.get().create(level, EntitySpawnReason.EVENT);
                    if (creeper != null) {
                        creeper.snapTo(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
                        level.addFreshEntity(creeper);
                    }
                } else {
                    Blaze blaze = EntityTypes.BLAZE.create(level, EntitySpawnReason.EVENT);
                    if (blaze != null) {
                        blaze.snapTo(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
                        level.addFreshEntity(blaze);
                    }
                }
                count--;
            }
        }
    }

    /** CrashFrezarite.afterCraterFormed: snow on half of the destroyed places above solid ground. */
    private static void snow(ServerLevel level, RandomSource random, List<BlockPos> exploded) {
        for (int n = exploded.size() - 1; n >= 0; n--) {
            BlockPos pos = exploded.get(n);
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolidRender() && random.nextInt(2) == 0) {
                level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 2);
            }
        }
    }

    // ---- SBAPI ----

    /** SBAPI.generateCuboid: every block of the box (hollow: only the edge of a one block thick wall). */
    private static void cuboid(ServerLevel level, int x1, int y1, int z1, int x2, int y2, int z2, BlockState state, boolean hollow) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean edge = y == minY || y == maxY || (minX == maxX ? z == minZ || z == maxZ : x == minX || x == maxX);
                    if (!hollow || edge) {
                        level.setBlock(new BlockPos(x, y, z), state, 3);
                    }
                }
            }
        }
    }

    private static void cuboid(ServerLevel level, int x1, int y1, int z1, int x2, int y2, int z2, Block block) {
        cuboid(level, x1, y1, z1, x2, y2, z2, block.defaultBlockState(), false);
    }

    /** CrashKreknorite.createPortal: a nether brick platform with a lit obsidian portal frame and a glowstone roof. */
    private static void portal(ServerLevel level, int i, int j, int k, boolean directionFlag) {
        Block brick = Blocks.NETHER_BRICKS;
        Block air = Blocks.AIR;
        if (directionFlag) {
            cuboid(level, i + 1, j + 1, k, i, j + 1, k, brick);
            cuboid(level, i + 2, j + 2, k + 1, i - 1, j + 2, k - 1, brick);
            cuboid(level, i + 3, j + 3, k + 2, i - 2, j + 3, k - 2, brick);
            cuboid(level, i + 2, j + 3, k, i - 1, j + 7, k, Blocks.OBSIDIAN.defaultBlockState(), true);
            cuboid(level, i + 3, j + 4, k + 2, i + 3, j + 8, k + 2, brick);
            cuboid(level, i + 3, j + 4, k - 2, i + 3, j + 8, k - 2, brick);
            cuboid(level, i - 2, j + 4, k - 2, i - 2, j + 8, k - 2, brick);
            cuboid(level, i - 2, j + 4, k + 2, i - 2, j + 8, k + 2, brick);
            cuboid(level, i + 3, j + 8, k + 3, i - 2, j + 8, k + 3, brick);
            cuboid(level, i + 3, j + 8, k - 3, i - 2, j + 8, k - 3, brick);
            cuboid(level, i + 4, j + 8, k + 2, i + 4, j + 8, k - 2, brick);
            cuboid(level, i - 3, j + 8, k + 2, i - 3, j + 8, k - 2, brick);
            cuboid(level, i + 3, j + 9, k + 2, i - 2, j + 9, k - 2, brick);
            cuboid(level, i + 3, j + 9, k + 2, i + 3, j + 9, k + 2, air);
            cuboid(level, i + 3, j + 9, k - 2, i + 3, j + 9, k - 2, air);
            cuboid(level, i - 2, j + 9, k - 2, i - 2, j + 9, k - 2, air);
            cuboid(level, i - 2, j + 9, k + 2, i - 2, j + 9, k + 2, air);
            cuboid(level, i + 2, j + 9, k + 1, i - 1, j + 9, k - 1, Blocks.GLOWSTONE);
            cuboid(level, i + 2, j + 10, k + 1, i - 1, j + 10, k - 1, brick);
        } else {
            cuboid(level, i, j + 1, k + 1, i, j + 1, k, brick);
            cuboid(level, i + 1, j + 2, k + 2, i - 1, j + 2, k - 1, brick);
            cuboid(level, i + 2, j + 3, k + 3, i - 2, j + 3, k - 2, brick);
            cuboid(level, i, j + 3, k + 2, i, j + 7, k - 1, Blocks.OBSIDIAN.defaultBlockState(), true);
            cuboid(level, i - 2, j + 4, k + 3, i - 2, j + 8, k + 3, brick);
            cuboid(level, i - 2, j + 4, k - 2, i - 2, j + 8, k - 2, brick);
            cuboid(level, i + 2, j + 4, k - 2, i + 2, j + 8, k - 2, brick);
            cuboid(level, i + 2, j + 4, k + 3, i + 2, j + 8, k + 3, brick);
            cuboid(level, i + 3, j + 8, k + 3, i + 3, j + 8, k - 2, brick);
            cuboid(level, i - 3, j + 8, k + 3, i - 3, j + 8, k - 2, brick);
            cuboid(level, i + 2, j + 8, k + 4, i - 2, j + 8, k + 4, brick);
            cuboid(level, i + 2, j + 8, k - 3, i - 2, j + 8, k - 3, brick);
            cuboid(level, i + 2, j + 9, k + 3, i - 2, j + 9, k - 2, brick);
            cuboid(level, i - 2, j + 9, k + 3, i - 2, j + 9, k + 3, air);
            cuboid(level, i - 2, j + 9, k - 2, i - 2, j + 9, k - 2, air);
            cuboid(level, i + 2, j + 9, k - 2, i + 2, j + 9, k - 2, air);
            cuboid(level, i + 2, j + 9, k + 3, i + 2, j + 9, k + 3, air);
            cuboid(level, i + 1, j + 9, k + 2, i - 1, j + 9, k - 1, Blocks.GLOWSTONE);
            cuboid(level, i + 1, j + 10, k + 2, i - 1, j + 10, k - 1, brick);
        }
        level.setBlock(new BlockPos(i, j + 4, k), Blocks.FIRE.defaultBlockState(), 3);
    }

    // ---- CrashUnknown ----

    /**
     * CrashUnknown.generate: a 3x3x3 fallen meteor with Frezarite and Kreknorite in it, glowstone on the six sides
     * and a chest in the middle with eight draws of its loot table.
     */
    static void unknown(ServerLevel level, RandomSource random, int crashSize, int i, int j, int k) {
        int y = j - crashSize * 4;
        int yEnd = j + crashSize * 4;
        while (!level.getBlockState(new BlockPos(i, y + 1, k)).isAir() && y <= yEnd) {
            y++;
        }
        int j1 = y + 1;
        cuboid(level, i + 1, j1 + 1, k + 1, i - 1, j1 - 1, k - 1,
                MeteorBlock.hot(MeteorsMod.FALLEN_METEOR.get().defaultBlockState(), random.nextInt(4) + 1), false);
        int end = random.nextInt(5) + 4;
        for (int r = 0; r < end; r++) {
            Block block = random.nextBoolean() ? MeteorsMod.FREZARITE.get() : MeteorsMod.KREKNORITE.get();
            BlockPos pos = new BlockPos(i + random.nextInt(3) - 1, j1 + random.nextInt(3) - 1, k + random.nextInt(3) - 1);
            level.setBlock(pos, MeteorBlock.hot(block.defaultBlockState(), random.nextInt(4) + 1), 3);
        }
        BlockState glowstone = Blocks.GLOWSTONE.defaultBlockState();
        level.setBlock(new BlockPos(i, j1 + 2, k), glowstone, 3);
        level.setBlock(new BlockPos(i, j1 - 2, k), glowstone, 3);
        level.setBlock(new BlockPos(i + 2, j1, k), glowstone, 3);
        level.setBlock(new BlockPos(i - 2, j1, k), glowstone, 3);
        level.setBlock(new BlockPos(i, j1, k + 2), glowstone, 3);
        level.setBlock(new BlockPos(i, j1, k - 2), glowstone, 3);
        BlockPos chestPos = new BlockPos(i, j1, k);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            for (int n = 0; n < 8; n++) {
                ItemStack item = randomLoot(level, random);
                if (!item.isEmpty()) {
                    chest.setItem(random.nextInt(chest.getContainerSize()), item);
                }
            }
        }
    }

    private static ItemStack enchanted(ServerLevel level, net.minecraft.world.item.Item item, ResourceKey<Enchantment> key, int lvl) {
        ItemStack stack = new ItemStack(item);
        level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).ifPresent(h -> stack.enchant((Holder<Enchantment>) h, lvl));
        return stack;
    }

    /** CrashUnknown.getRandomLoot: one of 60, nine of them empty. */
    static ItemStack randomLoot(ServerLevel level, RandomSource random) {
        int r = random.nextInt(60);
        return switch (r) {
            case 0, 1, 2 -> new ItemStack(MeteorsMod.METEOR_CHIPS.get(), random.nextInt(8) + 1);
            case 3, 4, 5 -> new ItemStack(MeteorsMod.FREZARITE_CRYSTAL.get(), random.nextInt(8) + 1);
            case 6, 7, 8 -> new ItemStack(MeteorsMod.KREKNORITE_CHIP.get(), random.nextInt(8) + 1);
            case 9, 10, 11 -> enchanted(level, MeteorsMod.METEORITE_CHESTPLATE.get(), MeteorsMod.MAGNETIZATION, 1);
            case 15, 16 -> enchanted(level, MeteorsMod.KREKNORITE_SWORD.get(), Enchantments.FIRE_ASPECT, 2);
            case 19, 20, 21, 22 -> new ItemStack(MeteorsMod.RED_METEOR_GEM.get(), random.nextInt(16) + 1);
            case 23, 24, 25 -> enchanted(level, MeteorsMod.KREKNORITE_CHESTPLATE.get(), Enchantments.FIRE_PROTECTION, 4);
            case 26, 27, 28, 29 -> new ItemStack(Items.DIAMOND, random.nextInt(2) + 1);
            case 30, 31 -> new ItemStack(Items.GOLD_INGOT, random.nextInt(4) + 1);
            case 32, 33, 34 -> new ItemStack(MeteorsMod.FREZARITE_PICKAXE.get());
            case 35, 36, 37 -> new ItemStack(MeteorsMod.CHOCOLATE_ICE_CREAM.get(), random.nextInt(6) + 1);
            case 38 -> new ItemStack(MeteorsMod.FREZARITE_SHOVEL.get());
            case 39, 40 -> enchanted(level, MeteorsMod.KREKNORITE_HELMET.get(), Enchantments.FIRE_PROTECTION, 4);
            case 41, 42 -> enchanted(level, MeteorsMod.FREZARITE_HELMET.get(), Enchantments.RESPIRATION, 3);
            case 43, 44 -> new ItemStack(Items.REDSTONE, random.nextInt(16) + 1);
            case 48, 49 -> new ItemStack(Items.IRON_BLOCK, random.nextInt(5) + 1);
            // forModpack is false: a summoner of any kind
            case 50, 51, 52 -> new ItemStack(MeteorsMod.SUMMONERS.get(random.nextInt(6)).get());
            default -> ItemStack.EMPTY;
        };
    }
}
