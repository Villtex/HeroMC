package twilightforest.gen;

import hexxit.worldgen152.core.populate.OldStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * EnchantmentHelper.addRandomEnchantment of 1.5.2 with the vanilla enchantments of 1.5.2 (TFTreasureItem with a random
 * enchantment level). The table of 1.5.2 also held the enchantments of the other mods of the pack (Fiery Aura 206, Cold
 * Touch 201, ...); they are left out, so a random enchantment can differ from the original where one of them fitted.
 */
public final class OldEnchanting {
    private OldEnchanting() {
    }

    /** EnumEnchantmentType. */
    enum Type { ARMOR, FEET, LEGS, TORSO, HEAD, WEAPON, DIGGER, BOW }

    /** Item.getItemEnchantability and its EnumEnchantmentType class (ItemArmor with slot, ItemSword, ItemTool, ItemBow). */
    record Item(int enchantability, Type kind) {
    }

    private record Ench(int id, int weight, Type type, int maxLevel, int base, int perLevel, int range) {
        int min(int level) {
            return this.base + (level - 1) * this.perLevel;
        }

        int max(int level) {
            return this.min(level) + this.range;
        }

        boolean canEnchant(Item item) {
            return switch (this.type) {
                case ARMOR -> item.kind == Type.FEET || item.kind == Type.LEGS || item.kind == Type.TORSO || item.kind == Type.HEAD;
                default -> this.type == item.kind;
            };
        }
    }

    private static final List<Ench> ENCHANTMENTS = List.of(
            new Ench(0, 10, Type.ARMOR, 4, 1, 11, 20), new Ench(1, 5, Type.ARMOR, 4, 10, 8, 12), new Ench(2, 5, Type.FEET, 4, 5, 6, 10),
            new Ench(3, 2, Type.ARMOR, 4, 5, 8, 12), new Ench(4, 5, Type.ARMOR, 4, 3, 6, 15), new Ench(5, 2, Type.HEAD, 3, 10, 10, 30),
            new Ench(6, 2, Type.HEAD, 1, 1, 0, 40), new Ench(7, 1, Type.TORSO, 3, 10, 20, 50), new Ench(16, 10, Type.WEAPON, 5, 1, 11, 20),
            new Ench(17, 5, Type.WEAPON, 5, 5, 8, 20), new Ench(18, 5, Type.WEAPON, 5, 5, 8, 20), new Ench(19, 5, Type.WEAPON, 2, 5, 20, 50),
            new Ench(20, 2, Type.WEAPON, 2, 10, 20, 50), new Ench(21, 2, Type.WEAPON, 3, 15, 9, 50), new Ench(32, 10, Type.DIGGER, 5, 1, 10, 50),
            new Ench(33, 1, Type.DIGGER, 1, 15, 0, 50), new Ench(34, 5, Type.DIGGER, 3, 5, 8, 50), new Ench(35, 2, Type.DIGGER, 3, 15, 9, 50),
            new Ench(48, 10, Type.BOW, 5, 1, 10, 15), new Ench(49, 2, Type.BOW, 2, 12, 20, 25), new Ench(50, 2, Type.BOW, 1, 20, 0, 30),
            new Ench(51, 1, Type.BOW, 1, 20, 0, 30));

    /** The 1.5.2 items Twilight Forest chests enchant at random, by their item id. */
    static Item item(int itemId) {
        return switch (itemId) {
            case 283, 286, 285 -> new Item(22, itemId == 283 ? Type.WEAPON : Type.DIGGER); // gold sword, axe, pickaxe
            case 317 -> new Item(25, Type.FEET);
            case 314 -> new Item(25, Type.HEAD);
            case 308 -> new Item(9, Type.LEGS); // iron leggings
            case 261 -> new Item(1, Type.BOW);
            case 272 -> new Item(5, Type.WEAPON);
            case 268 -> new Item(15, Type.WEAPON);
            case 267 -> new Item(14, Type.WEAPON);
            case 276 -> new Item(10, Type.WEAPON);
            case TFTreasure.IRONWOOD_SWORD -> new Item(25, Type.WEAPON);
            case TFTreasure.STEELEAF_SWORD -> new Item(9, Type.WEAPON);
            default -> new Item(0, Type.WEAPON);
        };
    }

    private record Data(Ench ench, int level) {
    }

    /** EnchantmentHelper.addRandomEnchantment: draws from {@code rand} exactly as 1.5.2 did. */
    public static OldStack addRandomEnchantment(Random rand, OldStack stack, int level) {
        List<Data> list = buildEnchantmentList(rand, item(stack.itemId()), level);
        if (list != null) {
            for (Data data : list) {
                stack = stack.withEnchantment(data.ench.id, data.level);
            }
        }
        return stack;
    }

    private static List<Data> buildEnchantmentList(Random rand, Item item, int level) {
        int ench = item.enchantability;
        if (ench <= 0) {
            return null;
        }
        ench /= 2;
        ench = 1 + rand.nextInt((ench >> 1) + 1) + rand.nextInt((ench >> 1) + 1);
        int j = ench + level;
        float f = (rand.nextFloat() + rand.nextFloat() - 1.0F) * 0.15F;
        int k = (int) (j * (1.0F + f) + 0.5F);
        if (k < 1) {
            k = 1;
        }
        List<Data> result = null;
        Map<Integer, Data> map = mapEnchantmentData(k, item);
        if (map != null && !map.isEmpty()) {
            Data data = weighted(rand, map.values());
            if (data != null) {
                result = new ArrayList<>();
                result.add(data);
                for (int l = k; rand.nextInt(50) <= l; l >>= 1) {
                    Iterator<Integer> it = map.keySet().iterator();
                    while (it.hasNext()) {
                        Ench candidate = map.get(it.next()).ench;
                        for (Data chosen : result) {
                            if (!canApplyTogether(chosen.ench, candidate)) {
                                it.remove();
                                break;
                            }
                        }
                    }
                    if (!map.isEmpty()) {
                        result.add(weighted(rand, map.values()));
                    }
                }
            }
        }
        return result;
    }

    /**
     * mapEnchantmentData: a HashMap keyed by enchantment id. Its iteration order (capacity 16, never more than 12 entries
     * here) is the id modulo 16, ids of one bucket in the order they were put.
     */
    private static Map<Integer, Data> mapEnchantmentData(int k, Item item) {
        Map<Integer, Data> byId = new LinkedHashMap<>();
        for (Ench e : ENCHANTMENTS) {
            if (e.canEnchant(item)) {
                for (int l = 1; l <= e.maxLevel; l++) {
                    if (k >= e.min(l) && k <= e.max(l)) {
                        byId.put(e.id, new Data(e, l));
                    }
                }
            }
        }
        if (byId.isEmpty()) {
            return null;
        }
        Map<Integer, Data> ordered = new LinkedHashMap<>();
        for (int bucket = 0; bucket < 16; bucket++) {
            for (Map.Entry<Integer, Data> entry : byId.entrySet()) {
                if ((entry.getKey() & 15) == bucket) {
                    ordered.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return ordered;
    }

    /** WeightedRandom.getRandomItem. */
    private static Data weighted(Random rand, Iterable<Data> items) {
        int total = 0;
        for (Data d : items) {
            total += d.ench.weight;
        }
        if (total <= 0) {
            throw new IllegalArgumentException();
        }
        int value = rand.nextInt(total);
        for (Data d : items) {
            value -= d.ench.weight;
            if (value < 0) {
                return d;
            }
        }
        return null;
    }

    /** Enchantment.canApplyTogether of 1.5.2 (protection kinds, damage kinds, silk touch and fortune). */
    private static boolean canApplyTogether(Ench a, Ench b) {
        if (a.id <= 4 && b.id <= 4) {
            return a.id != b.id && (a.id == 2 || b.id == 2);
        }
        if (a.id >= 16 && a.id <= 18 && b.id >= 16 && b.id <= 18) {
            return false;
        }
        if (a.id == 33 && b.id == 35 || a.id == 35 && b.id == 33) {
            return false;
        }
        return a.id != b.id;
    }
}
