package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * RegisterChestItem with what ConfigHelper.addChests read from Chocolate/ChestConfig (in the order Properties.elements()
 * gave in Java 6 to 8) and what BetterDungeons.registerItems added after it (registerModItemsToChests = true).
 */
public final class BDLoot {
    /** A weighted stack; for the weapon chests also whether it is armour (and which piece) or a sword. */
    public record Entry(OldStack stack, int weight, int armorType, boolean sword) {
    }

    public static final List<Entry> CHEST = new ArrayList<>();
    public static final List<Entry> WEAPONS = new ArrayList<>();
    public static final List<Entry> MINERALS = new ArrayList<>();
    public static final List<Entry> FOOD = new ArrayList<>();
    public static final List<Entry> TREASURE = new ArrayList<>();
    /** First item id of the mod (itemID = 7351 in the pack's Chocolate/config.prop; Item(id) has itemID 256 + id). */
    public static final int ITEM_ID = 7351 + 256;
    private static boolean loaded;

    private BDLoot() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        read("ChestConfig/chests.prop", CHEST);
        read("ChestConfig/treasures.prop", TREASURE);
        read("ChestConfig/weapons.prop", WEAPONS);
        read("ChestConfig/ores.prop", MINERALS);
        read("ChestConfig/food.prop", FOOD);
        // BetterDungeons.registerItems
        for (int i = 0; i < 8; i++) {
            add(CHEST, ITEM_ID + 3, 5, i, 5);           // essence (totem)
        }
        add(CHEST, ITEM_ID + 28, 1, 0, 5);              // scroll of escape
        for (int i = 0; i < 8; i++) {
            add(WEAPONS, ITEM_ID + 6, 5, i, 5);         // magic arrows
        }
        for (int i = 0; i < 8; i++) {
            add(WEAPONS, ITEM_ID + 4, 5, i, 5);         // grenades
        }
        add(WEAPONS, ITEM_ID + 5, 1, 0, 20);            // long bow
        add(WEAPONS, ITEM_ID + 7, 1, 0, 20);            // revolver
        add(WEAPONS, ITEM_ID + 8, 10, 0, 5);            // bullets
        add(WEAPONS, ITEM_ID + 18, 1, 0, 20);           // materials
        WEAPONS.add(new Entry(new OldStack(ITEM_ID + 41, 0, 1), 15, -1, true));   // iron spear
        WEAPONS.add(new Entry(new OldStack(ITEM_ID + 42, 0, 1), 5, -1, true));    // diamond spear
        WEAPONS.add(new Entry(new OldStack(ITEM_ID + 43, 0, 1), 15, -1, true));   // iron big sword
        WEAPONS.add(new Entry(new OldStack(ITEM_ID + 44, 0, 1), 5, -1, true));    // diamond big sword
        WEAPONS.add(new Entry(new OldStack(ITEM_ID + 39, 0, 1), 15, -1, true));   // iron dagger
        WEAPONS.add(new Entry(new OldStack(ITEM_ID + 40, 0, 1), 5, -1, true));    // diamond dagger
        add(WEAPONS, ITEM_ID + 2, 1, 0, 5);             // quiver
        for (int staff : new int[] {11, 12, 15, 16, 14, 13, 10, 17}) {
            add(TREASURE, ITEM_ID + staff, 1, 0, 5);    // staffs: water, poison, dark, electric, fire, ice, life, wind
        }
        add(TREASURE, ITEM_ID + 23, 1, 0, 15);          // golden feather
        TREASURE.add(new Entry(new OldStack(ITEM_ID + 30, 0, 1), 5, 3, false));   // cloud boots
        add(TREASURE, ITEM_ID + 25, 1, 0, 20);          // hookshot
    }

    private static void add(List<Entry> list, int id, int count, int damage, int weight) {
        list.add(new Entry(new OldStack(id, damage, count), weight, -1, false));
    }

    /** ConfigHelper.getItems: "name = id, num_items, item_damage, item_weight". */
    private static void read(String file, List<Entry> list) {
        BDFiles.Ordered prop = BDFiles.properties(file);
        // the names only matter for the weapon chests: they tell armour pieces and swords of other mods apart
        List<String> values = prop.hashtableValues();
        for (String value : values) {
            String key = null;
            for (String k : prop.keys) {
                if (prop.getProperty(k) == value) {
                    key = k;
                    break;
                }
            }
            StringTokenizer stkn = new StringTokenizer(value, ",");
            int id = Integer.parseInt(stkn.nextToken().trim());
            int count = Integer.parseInt(stkn.nextToken().trim());
            int damage = Integer.parseInt(stkn.nextToken().trim());
            int weight = Integer.parseInt(stkn.nextToken().trim());
            list.add(new Entry(new OldStack(id, damage, count), weight, armorType(id, key), sword(id, key)));
        }
    }

    /** ItemArmor.armorType (0 helmet, 1 plate, 2 legs, 3 boots) of a chest item, -1 for no armour. */
    private static int armorType(int id, String name) {
        if (id >= 298 && id <= 317) {
            return (id - 298) % 4;
        }
        if (id < 4096 || name == null) {
            return -1;
        }
        String n = name.toLowerCase();
        if (n.endsWith("helmet") || n.endsWith("head")) {
            return 0;
        }
        if (n.endsWith("chest") || n.endsWith("chestplate")) {
            return 1;
        }
        if (n.endsWith("legs") || n.endsWith("leggins")) {
            return 2;
        }
        return n.endsWith("boots") ? 3 : -1;
    }

    /** ItemSword (vanilla swords; swords, daggers, spears and scythes of other mods by their name in weapons.prop). */
    private static boolean sword(int id, String name) {
        if (id == 267 || id == 268 || id == 272 || id == 276 || id == 283) {
            return true;
        }
        if (id < 4096 || name == null) {
            return false;
        }
        String n = name.toLowerCase();
        return n.endsWith("sword") || n.endsWith("dagger") || n.endsWith("spear") || n.endsWith("glaive") || n.endsWith("scythe")
                || n.endsWith("reaper");
    }

    /** RegisterChestItem.getRandomItemStack: one draw over the weights. */
    public static Entry random(List<Entry> list, Random random) {
        int maxNum = 0;
        for (Entry e : list) {
            maxNum += e.weight;
        }
        int randomNum = random.nextInt(maxNum);
        int index = 0;
        int weightSum = list.get(0).weight;
        while (weightSum <= randomNum) {
            weightSum += list.get(++index).weight;
        }
        return list.get(index);
    }
}
