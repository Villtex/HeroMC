package projectzulu;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

/**
 * The items of Project Zulu that the creature drops of Teil 10 need: scrap meat (ItemFoodProjectZulu: 1 food,
 * 1.0 saturation), fur pelt, scale item, ankh and the generic crafting items (ItemGenerics metas) the mob drops
 * reference, and the other ItemGenerics metas (potion ingredients, crafting parts). Blocks, armour, coconut and aloe
 * items come from ZuluPlants/ZuluGear/ZuluWorld/ZuluPotions (Teil 11).
 */
public final class ZuluItems {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectZulu.MOD_ID);

    public static final DeferredItem<Item> SCRAP_MEAT = item("scrap_meat",
            p -> p.food(new FoodProperties.Builder().nutrition(1).saturationModifier(1.0F).build()));
    public static final DeferredItem<Item> FUR_PELT = item("fur_pelt");
    public static final DeferredItem<Item> SCALE_ITEM = item("scale_item");
    public static final DeferredItem<Item> ANKH = item("ankh");

    // ItemGenerics (meta 0..38), one item per meta
    public static final DeferredItem<Item> POISON_DROPLET = item("poison_droplet");
    public static final DeferredItem<Item> TUSK = item("tusk");
    public static final DeferredItem<Item> BLACK_LICHEN = item("black_lichen");
    public static final DeferredItem<Item> ANTENNAE = item("antennae");
    public static final DeferredItem<Item> TALON = item("talon");
    public static final DeferredItem<Item> BARK = item("bark");
    public static final DeferredItem<Item> SMALL_HEART = item("small_heart");
    public static final DeferredItem<Item> LARGE_HEART = item("large_heart");
    public static final DeferredItem<Item> GILL = item("gill");
    public static final DeferredItem<Item> ECTOPLASM = item("ectoplasm");
    public static final DeferredItem<Item> FROG_LEGS = item("frog_legs");
    public static final DeferredItem<Item> RABBITS_FOOT = item("rabbits_foot");
    public static final DeferredItem<Item> RAW_FIBER = item("raw_fiber");
    public static final DeferredItem<Item> PULP = item("pulp");
    public static final DeferredItem<Item> SALT = item("salt");
    public static final DeferredItem<Item> SHINY_BAUBLE = item("shiny_bauble");
    public static final DeferredItem<Item> PLANT_STALK = item("plant_stalk");
    public static final DeferredItem<Item> PRICKLY_POWDER = item("prickly_powder");
    public static final DeferredItem<Item> POWDER_SLUSH = item("powder_slush");
    public static final DeferredItem<Item> GLOWING_GOO = item("glowing_goo");
    public static final DeferredItem<Item> SMALL_UNHEALTHY_HEART = item("small_unhealthy_heart");
    public static final DeferredItem<Item> LARGE_UNHEALTHY_HEART = item("large_unhealthy_heart");

    private ZuluItems() {
    }

    private static DeferredItem<Item> item(String name) {
        return item(name, p -> p);
    }

    private static DeferredItem<Item> item(String name, UnaryOperator<Item.Properties> properties) {
        return ITEMS.registerItem(name, Item::new, properties);
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
