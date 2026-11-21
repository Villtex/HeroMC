package hexxit.worldgen152;

import hexxit.worldgen152.core.populate.OldStack;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Today's items, entities and enchantments for the numeric ids and names 1.5.2 generated into chests and
 * spawners. Mods that add dungeon loot or dungeon mobs register their translations here.
 */
public final class OldGameIds {
    private static final Map<Long, Identifier> ITEMS = new ConcurrentHashMap<>();
    private static final Map<String, Identifier> ENTITIES = new ConcurrentHashMap<>();
    private static final Map<String, java.util.function.Consumer<net.minecraft.world.entity.Entity>> ENTITY_INITS = new ConcurrentHashMap<>();
    private static final Map<Integer, ResourceKey<Enchantment>> ENCHANTMENTS = new ConcurrentHashMap<>();
    private static final int ANY_DAMAGE = -1;

    static {
        item(329, Items.SADDLE);
        item(265, Items.IRON_INGOT);
        item(297, Items.BREAD);
        item(296, Items.WHEAT);
        item(289, Items.GUNPOWDER);
        item(287, Items.STRING);
        item(325, Items.BUCKET);
        item(322, Items.GOLDEN_APPLE);
        item(331, Items.REDSTONE);
        item(2256, Items.MUSIC_DISC_13);
        item(2257, Items.MUSIC_DISC_CAT);
        registerItem(351, 3, BuiltInRegistries.ITEM.getKey(Items.COCOA_BEANS).toString());
        // skulls (ItemSkull damage = kind): loot of Wild Caves 3
        registerItem(397, 0, BuiltInRegistries.ITEM.getKey(Items.SKELETON_SKULL).toString());
        registerItem(397, 1, BuiltInRegistries.ITEM.getKey(Items.WITHER_SKELETON_SKULL).toString());
        registerItem(397, 2, BuiltInRegistries.ITEM.getKey(Items.ZOMBIE_HEAD).toString());
        registerItem(397, 3, BuiltInRegistries.ITEM.getKey(Items.PLAYER_HEAD).toString());
        registerItem(397, 4, BuiltInRegistries.ITEM.getKey(Items.CREEPER_HEAD).toString());
        item(403, Items.ENCHANTED_BOOK);
        // loot of structures
        item(264, Items.DIAMOND);
        item(266, Items.GOLD_INGOT);
        item(388, Items.EMERALD);
        item(352, Items.BONE);
        item(367, Items.ROTTEN_FLESH);
        item(262, Items.ARROW);
        item(368, Items.ENDER_PEARL);
        item(260, Items.APPLE);
        item(257, Items.IRON_PICKAXE);
        item(267, Items.IRON_SWORD);
        item(307, Items.IRON_CHESTPLATE);
        item(306, Items.IRON_HELMET);
        item(308, Items.IRON_LEGGINGS);
        item(309, Items.IRON_BOOTS);
        item(340, Items.BOOK);
        item(339, Items.PAPER);
        item(395, Items.MAP);
        item(345, Items.COMPASS);
        item(263, Items.COAL);
        item(49, Items.OBSIDIAN);
        item(6, Items.OAK_SAPLING);
        item(66, Items.RAIL);
        item(362, Items.MELON_SEEDS);
        item(361, Items.PUMPKIN_SEEDS);
        registerItem(351, 4, BuiltInRegistries.ITEM.getKey(Items.LAPIS_LAZULI).toString());
        registerEntity("CaveSpider", "minecraft:cave_spider");
        registerEntity("Silverfish", "minecraft:silverfish");
        registerEntity("Skeleton", "minecraft:skeleton");
        registerEntity("Zombie", "minecraft:zombie");
        registerEntity("Spider", "minecraft:spider");
        registerEntity("Blaze", "minecraft:blaze");
        // the other names of EntityList (entities that templates of structures carry)
        String[][] entities = {{"Creeper", "creeper"}, {"Slime", "slime"}, {"Ghast", "ghast"}, {"PigZombie", "zombified_piglin"},
                {"Enderman", "enderman"}, {"LavaSlime", "magma_cube"}, {"WitherBoss", "wither"}, {"Bat", "bat"}, {"Witch", "witch"},
                {"Pig", "pig"}, {"Sheep", "sheep"}, {"Cow", "cow"}, {"Chicken", "chicken"}, {"Squid", "squid"}, {"Wolf", "wolf"},
                {"MushroomCow", "mooshroom"}, {"SnowMan", "snow_golem"}, {"Ozelot", "ocelot"}, {"VillagerGolem", "iron_golem"},
                {"Villager", "villager"}, {"Minecart", "minecart"}, {"Boat", "oak_boat"}, {"Giant", "giant"}};
        for (String[] entity : entities) {
            registerEntity(entity[0], "minecraft:" + entity[1]);
        }
        Object[][] enchantments = {
                {0, Enchantments.PROTECTION}, {1, Enchantments.FIRE_PROTECTION}, {2, Enchantments.FEATHER_FALLING},
                {3, Enchantments.BLAST_PROTECTION}, {4, Enchantments.PROJECTILE_PROTECTION}, {5, Enchantments.RESPIRATION},
                {6, Enchantments.AQUA_AFFINITY}, {7, Enchantments.THORNS}, {16, Enchantments.SHARPNESS}, {17, Enchantments.SMITE},
                {18, Enchantments.BANE_OF_ARTHROPODS}, {19, Enchantments.KNOCKBACK}, {20, Enchantments.FIRE_ASPECT},
                {21, Enchantments.LOOTING}, {32, Enchantments.EFFICIENCY}, {33, Enchantments.SILK_TOUCH}, {34, Enchantments.UNBREAKING},
                {35, Enchantments.FORTUNE}, {48, Enchantments.POWER}, {49, Enchantments.PUNCH}, {50, Enchantments.FLAME},
                {51, Enchantments.INFINITY}};
        for (Object[] entry : enchantments) {
            @SuppressWarnings("unchecked")
            ResourceKey<Enchantment> key = (ResourceKey<Enchantment>) entry[1];
            ENCHANTMENTS.put((Integer) entry[0], key);
        }
    }

    static {
        registerVanillaItems();
        registerVanillaBlocks();
    }

    /** 1.5.2 {@code Item.java}: item id = 256 + constructor index; every item of the vanilla game. */
    private static void registerVanillaItems() {
        Item[] items = {
                Items.IRON_SHOVEL, Items.IRON_PICKAXE, Items.IRON_AXE, Items.FLINT_AND_STEEL, Items.APPLE, // 256-260
                Items.BOW, Items.ARROW, Items.COAL, Items.DIAMOND, Items.IRON_INGOT, Items.GOLD_INGOT, // 261-266
                Items.IRON_SWORD, Items.WOODEN_SWORD, Items.WOODEN_SHOVEL, Items.WOODEN_PICKAXE, Items.WOODEN_AXE, // 267-271
                Items.STONE_SWORD, Items.STONE_SHOVEL, Items.STONE_PICKAXE, Items.STONE_AXE, // 272-275
                Items.DIAMOND_SWORD, Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, // 276-279
                Items.STICK, Items.BOWL, Items.MUSHROOM_STEW, // 280-282
                Items.GOLDEN_SWORD, Items.GOLDEN_SHOVEL, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, // 283-286
                Items.STRING, Items.FEATHER, Items.GUNPOWDER, // 287-289
                Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.DIAMOND_HOE, Items.GOLDEN_HOE, // 290-294
                Items.WHEAT_SEEDS, Items.WHEAT, Items.BREAD, // 295-297
                Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, // 298-301
                Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS, // 302-305
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, // 306-309
                Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, // 310-313
                Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, // 314-317
                Items.FLINT, Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.PAINTING, Items.GOLDEN_APPLE, // 318-322
                Items.OAK_SIGN, Items.OAK_DOOR, Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET, // 323-327
                Items.MINECART, Items.SADDLE, Items.IRON_DOOR, Items.REDSTONE, Items.SNOWBALL, Items.OAK_BOAT, // 328-333
                Items.LEATHER, Items.MILK_BUCKET, Items.BRICK, Items.CLAY_BALL, Items.SUGAR_CANE, // 334-338
                Items.PAPER, Items.BOOK, Items.SLIME_BALL, Items.CHEST_MINECART, Items.FURNACE_MINECART, // 339-343
                Items.EGG, Items.COMPASS, Items.FISHING_ROD, Items.CLOCK, Items.GLOWSTONE_DUST, // 344-348
                Items.COD, Items.COOKED_COD, Items.INK_SAC /* dye, see below */, Items.BONE, Items.SUGAR, // 349-353
                Items.CAKE, Items.BED.red(), Items.REPEATER, Items.COOKIE, Items.MAP /* filled map: no map data */, // 354-358
                Items.SHEARS, Items.MELON_SLICE, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, // 359-362
                Items.BEEF, Items.COOKED_BEEF, Items.CHICKEN, Items.COOKED_CHICKEN, Items.ROTTEN_FLESH, // 363-367
                Items.ENDER_PEARL, Items.BLAZE_ROD, Items.GHAST_TEAR, Items.GOLD_NUGGET, Items.NETHER_WART, // 368-372
                Items.POTION /* see potion() */, Items.GLASS_BOTTLE, Items.SPIDER_EYE, Items.FERMENTED_SPIDER_EYE, // 373-376
                Items.BLAZE_POWDER, Items.MAGMA_CREAM, Items.BREWING_STAND, Items.CAULDRON, Items.ENDER_EYE, // 377-381
                Items.GLISTERING_MELON_SLICE, Items.CREEPER_SPAWN_EGG /* see spawn eggs */, Items.EXPERIENCE_BOTTLE, // 382-384
                Items.FIRE_CHARGE, Items.WRITABLE_BOOK, Items.WRITABLE_BOOK /* written book without text */, // 385-387
                Items.EMERALD, Items.ITEM_FRAME, Items.FLOWER_POT, Items.CARROT, Items.POTATO, // 388-392
                Items.BAKED_POTATO, Items.POISONOUS_POTATO, Items.MAP /* empty map */, Items.GOLDEN_CARROT, // 393-396
                Items.SKELETON_SKULL, Items.CARROT_ON_A_STICK, Items.NETHER_STAR, Items.PUMPKIN_PIE, // 397-400
                Items.FIREWORK_ROCKET, Items.FIREWORK_STAR, Items.ENCHANTED_BOOK, Items.COMPARATOR, // 401-404
                Items.NETHER_BRICK, Items.QUARTZ, Items.TNT_MINECART, Items.HOPPER_MINECART, // 405-408
        };
        for (int i = 0; i < items.length; i++) {
            item(256 + i, items[i]);
        }
        Item[] records = {Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP,
                Items.MUSIC_DISC_FAR, Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL,
                Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD, Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT};
        for (int i = 0; i < records.length; i++) {
            item(2256 + i, records[i]);
        }
        // dye 351 (damage 0..15): ink, red, green, cocoa, lapis, purple, cyan, light gray, gray, pink, lime, yellow, light blue, magenta, orange, bone meal
        damaged(351, Items.INK_SAC, Items.DYE.red(), Items.DYE.green(), Items.COCOA_BEANS, Items.LAPIS_LAZULI, Items.DYE.purple(),
                Items.DYE.cyan(), Items.DYE.lightGray(), Items.DYE.gray(), Items.DYE.pink(), Items.DYE.lime(), Items.DYE.yellow(),
                Items.DYE.lightBlue(), Items.DYE.magenta(), Items.DYE.orange(), Items.BONE_MEAL);
        exactDamage(263, Items.COAL, Items.CHARCOAL);
        exactDamage(322, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
        // spawn egg 383: damage = 1.5.2 entity id
        Object[][] eggs = {{50, Items.CREEPER_SPAWN_EGG}, {51, Items.SKELETON_SPAWN_EGG}, {52, Items.SPIDER_SPAWN_EGG},
                {54, Items.ZOMBIE_SPAWN_EGG}, {55, Items.SLIME_SPAWN_EGG}, {56, Items.GHAST_SPAWN_EGG},
                {57, Items.ZOMBIFIED_PIGLIN_SPAWN_EGG}, {58, Items.ENDERMAN_SPAWN_EGG}, {59, Items.CAVE_SPIDER_SPAWN_EGG},
                {60, Items.SILVERFISH_SPAWN_EGG}, {61, Items.BLAZE_SPAWN_EGG}, {62, Items.MAGMA_CUBE_SPAWN_EGG},
                {65, Items.BAT_SPAWN_EGG}, {66, Items.WITCH_SPAWN_EGG}, {90, Items.PIG_SPAWN_EGG}, {91, Items.SHEEP_SPAWN_EGG},
                {92, Items.COW_SPAWN_EGG}, {93, Items.CHICKEN_SPAWN_EGG}, {94, Items.SQUID_SPAWN_EGG}, {95, Items.WOLF_SPAWN_EGG},
                {96, Items.MOOSHROOM_SPAWN_EGG}, {98, Items.OCELOT_SPAWN_EGG}, {120, Items.VILLAGER_SPAWN_EGG}};
        for (Object[] egg : eggs) {
            registerItem(383, (Integer) egg[0], BuiltInRegistries.ITEM.getKey((Item) egg[1]).toString());
        }
    }

    /** Block ids 1..158 as items (ItemBlock); damage selects the sub-block where 1.5.2 had metadata items. */
    private static void registerVanillaBlocks() {
        Object[][] simple = {
                {1, Items.STONE}, {2, Items.GRASS_BLOCK}, {3, Items.DIRT}, {4, Items.COBBLESTONE}, {7, Items.BEDROCK},
                {12, Items.SAND}, {13, Items.GRAVEL}, {14, Items.GOLD_ORE}, {15, Items.IRON_ORE}, {16, Items.COAL_ORE},
                {19, Items.SPONGE}, {20, Items.GLASS}, {21, Items.LAPIS_ORE}, {22, Items.LAPIS_BLOCK}, {23, Items.DISPENSER},
                {25, Items.NOTE_BLOCK}, {26, Items.BED.red()}, {27, Items.POWERED_RAIL}, {28, Items.DETECTOR_RAIL},
                {29, Items.STICKY_PISTON}, {30, Items.COBWEB}, {32, Items.DEAD_BUSH}, {33, Items.PISTON},
                {37, Items.DANDELION}, {38, Items.POPPY}, {39, Items.BROWN_MUSHROOM}, {40, Items.RED_MUSHROOM},
                {41, Items.GOLD_BLOCK}, {42, Items.IRON_BLOCK}, {45, Items.BRICKS}, {46, Items.TNT}, {47, Items.BOOKSHELF},
                {48, Items.MOSSY_COBBLESTONE}, {49, Items.OBSIDIAN}, {50, Items.TORCH}, {52, Items.SPAWNER},
                {53, Items.OAK_STAIRS}, {54, Items.CHEST}, {55, Items.REDSTONE}, {56, Items.DIAMOND_ORE},
                {57, Items.DIAMOND_BLOCK}, {58, Items.CRAFTING_TABLE}, {59, Items.WHEAT_SEEDS}, {60, Items.DIRT},
                {61, Items.FURNACE}, {62, Items.FURNACE}, {63, Items.OAK_SIGN}, {64, Items.OAK_DOOR}, {65, Items.LADDER},
                {66, Items.RAIL}, {67, Items.COBBLESTONE_STAIRS}, {68, Items.OAK_SIGN}, {69, Items.LEVER},
                {70, Items.STONE_PRESSURE_PLATE}, {71, Items.IRON_DOOR}, {72, Items.OAK_PRESSURE_PLATE},
                {73, Items.REDSTONE_ORE}, {74, Items.REDSTONE_ORE}, {75, Items.REDSTONE_TORCH}, {76, Items.REDSTONE_TORCH},
                {77, Items.STONE_BUTTON}, {78, Items.SNOW}, {79, Items.ICE}, {80, Items.SNOW_BLOCK}, {81, Items.CACTUS},
                {82, Items.CLAY}, {83, Items.SUGAR_CANE}, {84, Items.JUKEBOX}, {85, Items.OAK_FENCE},
                {86, Items.CARVED_PUMPKIN}, {87, Items.NETHERRACK}, {88, Items.SOUL_SAND}, {89, Items.GLOWSTONE},
                {91, Items.JACK_O_LANTERN}, {92, Items.CAKE}, {93, Items.REPEATER}, {94, Items.REPEATER},
                {95, Items.CHEST}, {96, Items.OAK_TRAPDOOR}, {99, Items.BROWN_MUSHROOM_BLOCK},
                {100, Items.RED_MUSHROOM_BLOCK}, {101, Items.IRON_BARS}, {102, Items.GLASS_PANE}, {103, Items.MELON},
                {104, Items.PUMPKIN_SEEDS}, {105, Items.MELON_SEEDS}, {106, Items.VINE}, {107, Items.OAK_FENCE_GATE},
                {108, Items.BRICK_STAIRS}, {109, Items.STONE_BRICK_STAIRS}, {110, Items.MYCELIUM}, {111, Items.LILY_PAD},
                {112, Items.NETHER_BRICKS}, {113, Items.NETHER_BRICK_FENCE}, {114, Items.NETHER_BRICK_STAIRS},
                {115, Items.NETHER_WART}, {116, Items.ENCHANTING_TABLE}, {117, Items.BREWING_STAND}, {118, Items.CAULDRON},
                {120, Items.END_PORTAL_FRAME}, {121, Items.END_STONE}, {122, Items.DRAGON_EGG}, {123, Items.REDSTONE_LAMP},
                {124, Items.REDSTONE_LAMP}, {127, Items.COCOA_BEANS}, {128, Items.SANDSTONE_STAIRS},
                {129, Items.EMERALD_ORE}, {130, Items.ENDER_CHEST}, {131, Items.TRIPWIRE_HOOK}, {132, Items.STRING},
                {133, Items.EMERALD_BLOCK}, {134, Items.SPRUCE_STAIRS}, {135, Items.BIRCH_STAIRS},
                {136, Items.JUNGLE_STAIRS}, {137, Items.COMMAND_BLOCK}, {138, Items.BEACON}, {140, Items.FLOWER_POT},
                {141, Items.CARROT}, {142, Items.POTATO}, {143, Items.OAK_BUTTON}, {144, Items.SKELETON_SKULL},
                {146, Items.TRAPPED_CHEST}, {147, Items.LIGHT_WEIGHTED_PRESSURE_PLATE},
                {148, Items.HEAVY_WEIGHTED_PRESSURE_PLATE}, {149, Items.COMPARATOR}, {150, Items.COMPARATOR},
                {151, Items.DAYLIGHT_DETECTOR}, {152, Items.REDSTONE_BLOCK}, {153, Items.NETHER_QUARTZ_ORE},
                {154, Items.HOPPER}, {156, Items.QUARTZ_STAIRS}, {157, Items.ACTIVATOR_RAIL}, {158, Items.DROPPER}};
        for (Object[] entry : simple) {
            item((Integer) entry[0], (Item) entry[1]);
        }
        Item[] woods = {Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.BIRCH_PLANKS, Items.JUNGLE_PLANKS};
        masked(5, 3, woods);
        masked(125, 3, woods);
        masked(126, 3, Items.OAK_SLAB, Items.SPRUCE_SLAB, Items.BIRCH_SLAB, Items.JUNGLE_SLAB);
        masked(6, 3, Items.OAK_SAPLING, Items.SPRUCE_SAPLING, Items.BIRCH_SAPLING, Items.JUNGLE_SAPLING);
        masked(17, 3, Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG);
        masked(18, 3, Items.OAK_LEAVES, Items.SPRUCE_LEAVES, Items.BIRCH_LEAVES, Items.JUNGLE_LEAVES);
        masked(24, 3, Items.SANDSTONE, Items.CHISELED_SANDSTONE, Items.CUT_SANDSTONE, Items.SANDSTONE);
        masked(31, 3, Items.DEAD_BUSH, Items.SHORT_GRASS, Items.FERN, Items.SHORT_GRASS);
        // 35 wool: white, orange, magenta, light blue, yellow, lime, pink, gray, light gray, cyan, purple, blue, brown, green, red, black
        masked(35, 15, Items.WOOL.white(), Items.WOOL.orange(), Items.WOOL.magenta(), Items.WOOL.lightBlue(), Items.WOOL.yellow(),
                Items.WOOL.lime(), Items.WOOL.pink(), Items.WOOL.gray(), Items.WOOL.lightGray(), Items.WOOL.cyan(), Items.WOOL.purple(),
                Items.WOOL.blue(), Items.WOOL.brown(), Items.WOOL.green(), Items.WOOL.red(), Items.WOOL.black());
        // 43 double slab: the full block; 44 slab (bit 8 = upper half)
        masked(43, 7, Items.STONE, Items.SANDSTONE, Items.OAK_PLANKS, Items.COBBLESTONE, Items.BRICKS, Items.STONE_BRICKS,
                Items.NETHER_BRICKS, Items.QUARTZ_BLOCK);
        masked(44, 7, Items.STONE_SLAB, Items.SANDSTONE_SLAB, Items.OAK_SLAB, Items.COBBLESTONE_SLAB, Items.BRICK_SLAB,
                Items.STONE_BRICK_SLAB, Items.NETHER_BRICK_SLAB, Items.QUARTZ_SLAB);
        masked(97, 3, Items.INFESTED_STONE, Items.INFESTED_COBBLESTONE, Items.INFESTED_STONE_BRICKS, Items.INFESTED_STONE);
        masked(98, 3, Items.STONE_BRICKS, Items.MOSSY_STONE_BRICKS, Items.CRACKED_STONE_BRICKS, Items.CHISELED_STONE_BRICKS);
        masked(139, 1, Items.COBBLESTONE_WALL, Items.MOSSY_COBBLESTONE_WALL);
        // 145 anvil: bits 2-3 = wear, bits 0-1 = facing
        for (int d = 0; d < 16; d++) {
            Item anvil = switch (d >> 2 & 3) {
                case 1 -> Items.CHIPPED_ANVIL;
                case 2 -> Items.DAMAGED_ANVIL;
                default -> Items.ANVIL;
            };
            registerItem(145, d, BuiltInRegistries.ITEM.getKey(anvil).toString());
        }
        item(145, Items.ANVIL);
        // 155 quartz: 0 block, 1 chiselled, 2-4 pillar
        for (int d = 0; d < 16; d++) {
            Item quartz = d == 0 ? Items.QUARTZ_BLOCK : d == 1 ? Items.CHISELED_QUARTZ_BLOCK : d <= 4 ? Items.QUARTZ_PILLAR : Items.QUARTZ_BLOCK;
            registerItem(155, d, BuiltInRegistries.ITEM.getKey(quartz).toString());
        }
        item(155, Items.QUARTZ_BLOCK);
    }

    /** Damage {@code d} gives {@code items[d & mask]}; the id without a damage value gives item 0. */
    private static void masked(int oldId, int mask, Item... items) {
        for (int d = 0; d < 16; d++) {
            registerItem(oldId, d, BuiltInRegistries.ITEM.getKey(items[(d & mask) % items.length]).toString());
        }
        item(oldId, items[0]);
    }

    /** Damage 0..n-1 gives {@code items[d]}. */
    private static void damaged(int oldId, Item... items) {
        for (int d = 0; d < items.length; d++) {
            registerItem(oldId, d, BuiltInRegistries.ITEM.getKey(items[d]).toString());
        }
        item(oldId, items[0]);
    }

    /** Damage 0 gives {@code base}, damage 1 {@code variant}; the item is not damageable. */
    private static void exactDamage(int oldId, Item base, Item variant) {
        item(oldId, base);
        registerItem(oldId, 1, BuiltInRegistries.ITEM.getKey(variant).toString());
    }

    /**
     * Potion 373: 1.5.2 damage to a potion_contents component. Bits 0-3 = effect (1 regeneration, 2 swiftness, 3 fire
     * resistance, 4 poison, 5 healing, 6 night vision, 8 weakness, 9 strength, 10 slowness, 12 harming), bit 5 (32)
     * = level II, bit 6 (64) = extended, bit 14 (16384) = splash. Without an effect: 0 water, 16 awkward, 32 thick,
     * 8192 and the rest mundane. Combinations 26.3 does not have (strong weakness, long healing) fall back to the
     * base potion.
     */
    private static ItemStack potion(int damage, int count) {
        Item item = (damage & 16384) != 0 ? Items.SPLASH_POTION : Items.POTION;
        boolean strong = (damage & 32) != 0;
        boolean extended = (damage & 64) != 0;
        Holder<Potion> potion;
        switch (damage & 15) {
            case 1 -> potion = strong ? Potions.STRONG_REGENERATION : extended ? Potions.LONG_REGENERATION : Potions.REGENERATION;
            case 2 -> potion = strong ? Potions.STRONG_SWIFTNESS : extended ? Potions.LONG_SWIFTNESS : Potions.SWIFTNESS;
            case 3 -> potion = extended ? Potions.LONG_FIRE_RESISTANCE : Potions.FIRE_RESISTANCE;
            case 4 -> potion = strong ? Potions.STRONG_POISON : extended ? Potions.LONG_POISON : Potions.POISON;
            case 5 -> potion = strong ? Potions.STRONG_HEALING : Potions.HEALING;
            case 6 -> potion = extended ? Potions.LONG_NIGHT_VISION : Potions.NIGHT_VISION;
            case 8 -> potion = extended ? Potions.LONG_WEAKNESS : Potions.WEAKNESS;
            case 9 -> potion = strong ? Potions.STRONG_STRENGTH : extended ? Potions.LONG_STRENGTH : Potions.STRENGTH;
            case 10 -> potion = strong ? Potions.STRONG_SLOWNESS : extended ? Potions.LONG_SLOWNESS : Potions.SLOWNESS;
            case 12 -> potion = strong ? Potions.STRONG_HARMING : Potions.HARMING;
            case 0 -> {
                int bits = damage & 0x7F;
                potion = bits == 16 ? Potions.AWKWARD : bits == 32 ? Potions.THICK
                        : (damage & 8192) != 0 || bits != 0 ? Potions.MUNDANE : Potions.WATER;
            }
            default -> potion = Potions.MUNDANE;
        }
        ItemStack stack = new ItemStack(item, count);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
    }

    private OldGameIds() {
    }

    private static void item(int oldId, Item item) {
        ITEMS.put(key(oldId, ANY_DAMAGE), BuiltInRegistries.ITEM.getKey(item));
    }

    private static long key(int oldId, int damage) {
        return (long) oldId << 32 | damage & 0xFFFFFFFFL;
    }

    /** An old item id, whatever its damage value. */
    public static void registerItem(int oldId, String itemId) {
        ITEMS.put(key(oldId, ANY_DAMAGE), Identifier.parse(itemId));
    }

    /** An old item id with one damage value (sub-items such as dyes). */
    public static void registerItem(int oldId, int damage, String itemId) {
        ITEMS.put(key(oldId, damage), Identifier.parse(itemId));
    }

    /** An entity name of 1.5.2 ("Zombie") and the entity type id it has now. */
    public static void registerEntity(String oldName, String entityId) {
        ENTITIES.put(oldName, Identifier.parse(entityId));
    }

    /**
     * An entity name of 1.5.2 whose entity needs more than its type, such as a variant the original passed to its
     * constructor (the Battle Towers golem of each tower type): {@code init} runs on the new entity before it is added.
     */
    public static void registerEntity(String oldName, String entityId, java.util.function.Consumer<net.minecraft.world.entity.Entity> init) {
        registerEntity(oldName, entityId);
        ENTITY_INITS.put(oldName, init);
    }

    /** Runs the initialiser registered for the 1.5.2 entity name, if any. */
    public static void initEntity(String oldName, net.minecraft.world.entity.Entity entity) {
        java.util.function.Consumer<net.minecraft.world.entity.Entity> init = ENTITY_INITS.get(oldName);
        if (init != null) {
            init.accept(entity);
        }
    }

    public static void registerEnchantment(int oldId, ResourceKey<Enchantment> enchantment) {
        ENCHANTMENTS.put(oldId, enchantment);
    }

    public static EntityType<?> entity(String oldName) {
        Identifier id = ENTITIES.get(oldName);
        if (id == null) {
            throw new IllegalStateException("1.5.2 entity name '" + oldName + "' has no entity type (OldGameIds.registerEntity)");
        }
        return BuiltInRegistries.ENTITY_TYPE.getValue(id);
    }

    private static final Map<Integer, ResourceKey<VillagerProfession>> MOD_PROFESSIONS = new java.util.concurrent.ConcurrentHashMap<>();

    /** A villager type of a mod (VillagerRegistry.registerVillagerType) by its number. */
    public static void registerProfession(int oldProfession, ResourceKey<VillagerProfession> profession) {
        MOD_PROFESSIONS.put(oldProfession, profession);
    }

    /** Villager professions of 1.5.2 by number; the smith, who sold tools, weapons and armour, is a weaponsmith now. */
    public static ResourceKey<VillagerProfession> profession(int oldProfession) {
        ResourceKey<VillagerProfession> mod = MOD_PROFESSIONS.get(oldProfession);
        if (mod != null) {
            return mod;
        }
        return switch (oldProfession) {
            case 1 -> VillagerProfession.LIBRARIAN;
            case 2 -> VillagerProfession.CLERIC;
            case 3 -> VillagerProfession.WEAPONSMITH;
            case 4 -> VillagerProfession.BUTCHER;
            default -> VillagerProfession.FARMER;
        };
    }

    public static ItemStack stack(OldStack old, RegistryAccess registries) {
        if (old.itemId() == 373) {
            return withEnchantment(potion(old.damage(), old.count()), old, registries);
        }
        Identifier id = ITEMS.get(key(old.itemId(), old.damage()));
        boolean exact = id != null;
        if (id == null) {
            id = ITEMS.get(key(old.itemId(), ANY_DAMAGE));
        }
        if (id == null) {
            throw new IllegalStateException("1.5.2 item " + old.itemId() + ":" + old.damage() + " has no item (OldGameIds.registerItem)");
        }
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(id), old.count());
        if (!exact && old.damage() > 0 && stack.isDamageableItem()) {
            // tools and armour: the 1.5.2 damage is the used durability
            stack.setDamageValue(Math.min(old.damage(), stack.getMaxDamage() - 1));
        }
        return withEnchantment(stack, old, registries);
    }

    private static ItemStack withEnchantment(ItemStack stack, OldStack old, RegistryAccess registries) {
        if (old.enchantment() >= 0) {
            ResourceKey<Enchantment> key = ENCHANTMENTS.get(old.enchantment());
            if (key == null) {
                throw new IllegalStateException("1.5.2 enchantment " + old.enchantment() + " is not mapped (OldGameIds.registerEnchantment)");
            }
            Holder<Enchantment> enchantment = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
            ItemEnchantments.Mutable stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            stored.set(enchantment, old.enchantmentLevel());
            stack.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
        }
        if (old.applied().length > 0) {
            // ItemStack.addEnchantment: the same enchantment twice stays twice in 1.5.2; today the later level wins
            ItemEnchantments.Mutable on = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (int i = 0; i + 1 < old.applied().length; i += 2) {
                ResourceKey<Enchantment> key = ENCHANTMENTS.get(old.applied()[i]);
                if (key == null) {
                    throw new IllegalStateException("1.5.2 enchantment " + old.applied()[i] + " is not mapped (OldGameIds.registerEnchantment)");
                }
                on.set(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), old.applied()[i + 1]);
            }
            stack.set(DataComponents.ENCHANTMENTS, on.toImmutable());
        }
        return stack;
    }
}
