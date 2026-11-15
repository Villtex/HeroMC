package idiamondhunter.morebows;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * More Bows 1.5.2.2 (FML id "More Bows"): eight bows that draw faster or slower than the vanilla bow and shoot harder
 * (arrow speed x1.2). The Blazing Bow sets its arrows on fire, the Legia Bow shoots two or three arrows, the Ender Bow
 * a volley of six, and the Frost Bow frost arrows that slow what they hit and freeze water and leave snow where they
 * land. Durability, draw time, enchantability and rarity as in the original; recipes are data.
 */
@Mod(MoreBows.MOD_ID)
public final class MoreBows {
    public static final String MOD_ID = "morebows";

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MOD_ID);

    // name, durability, draw ticks, enchantability, kind (rarity where the original had one)
    public static final DeferredItem<MoreBowItem> DIAMOND_BOW = bow("diamond_bow", 1016, 6, 1, MoreBowItem.Kind.PLAIN, p -> p);
    public static final DeferredItem<MoreBowItem> ENDER_BOW = bow("ender_bow", 215, 22, 2, MoreBowItem.Kind.ENDER, p -> p.rarity(Rarity.EPIC));
    public static final DeferredItem<MoreBowItem> FLAME_BOW = bow("flame_bow", 576, 15, 1, MoreBowItem.Kind.FLAME, p -> p);
    public static final DeferredItem<MoreBowItem> GOLD_BOW = bow("gold_bow", 68, 5, 2, MoreBowItem.Kind.PLAIN, p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<MoreBowItem> IRON_BOW = bow("iron_bow", 550, 17, 2, MoreBowItem.Kind.PLAIN, p -> p);
    public static final DeferredItem<MoreBowItem> MULTI_BOW = bow("multi_bow", 550, 13, 1, MoreBowItem.Kind.MULTI, p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<MoreBowItem> STONE_BOW = bow("stone_bow", 484, 17, 1, MoreBowItem.Kind.PLAIN, p -> p);
    public static final DeferredItem<MoreBowItem> FROST_BOW = bow("frost_bow", 550, 26, 1, MoreBowItem.Kind.FROST, p -> p);

    /** Size and tracking of the vanilla arrow. */
    public static final DeferredHolder<EntityType<?>, EntityType<FrostArrow>> FROST_ARROW = ENTITIES.registerEntityType("frost_arrow",
            FrostArrow::new, MobCategory.MISC,
            b -> b.noLootTable().sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20));

    /** Creative order of 1.5.2: by item id, which the pack config gave in alphabetical order of the names. */
    public static final List<DeferredItem<MoreBowItem>> BOWS = List.of(FLAME_BOW, DIAMOND_BOW, ENDER_BOW, FROST_BOW, GOLD_BOW, IRON_BOW,
            MULTI_BOW, STONE_BOW);

    public MoreBows(IEventBus modBus) {
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        modBus.addListener(MoreBows::tab);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            idiamondhunter.morebows.client.MoreBowsClient.register(modBus);
        }
    }

    private static DeferredItem<MoreBowItem> bow(String name, int durability, int drawTicks, int enchantability, MoreBowItem.Kind kind,
                                                 UnaryOperator<Item.Properties> extra) {
        return ITEMS.registerItem(name, p -> new MoreBowItem(p, drawTicks, kind),
                p -> extra.apply(p.durability(durability).enchantable(enchantability)));
    }

    private static void tab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            BOWS.forEach(event::accept);
        }
    }
}
