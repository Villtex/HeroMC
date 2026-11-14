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

    /** Villager professions of 1.5.2 by number; the smith, who sold tools, weapons and armour, is a weaponsmith now. */
    public static ResourceKey<VillagerProfession> profession(int oldProfession) {
        return switch (oldProfession) {
            case 1 -> VillagerProfession.LIBRARIAN;
            case 2 -> VillagerProfession.CLERIC;
            case 3 -> VillagerProfession.WEAPONSMITH;
            case 4 -> VillagerProfession.BUTCHER;
            default -> VillagerProfession.FARMER;
        };
    }

    public static ItemStack stack(OldStack old, RegistryAccess registries) {
        Identifier id = ITEMS.get(key(old.itemId(), old.damage()));
        if (id == null) {
            id = ITEMS.get(key(old.itemId(), ANY_DAMAGE));
        }
        if (id == null) {
            throw new IllegalStateException("1.5.2 item " + old.itemId() + ":" + old.damage() + " has no item (OldGameIds.registerItem)");
        }
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(id), old.count());
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
        return stack;
    }
}
