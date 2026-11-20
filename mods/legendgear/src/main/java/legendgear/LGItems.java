package legendgear;

import com.mojang.serialization.Codec;
import legendgear.block.ChainedBlock;
import legendgear.block.MysticShrubBlock;
import legendgear.entity.MedallionEntity;
import legendgear.item.AmmoBagItem;
import legendgear.item.AmuletItem;
import legendgear.item.BombItem;
import legendgear.item.BoomerangItem;
import legendgear.item.EmeraldShardItem;
import legendgear.item.HeartItem;
import legendgear.item.HookshotItem;
import legendgear.item.MagicMirrorItem;
import legendgear.item.MagicPowderItem;
import legendgear.item.MedallionItem;
import legendgear.item.MysticSeedItem;
import legendgear.item.ReedPipesItem;
import legendgear.item.SlimeSwordItem;
import legendgear.item.StateBlockItem;
import legendgear.item.TitanBandItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The items of LegendGear.load with the ids of the pack's LegendGear.cfg (+ 256). Every damage value of a 1.5.2 item
 * with sub-items is an item of its own (shard/piece, the keys, stardust/star piece/starstone, the rock candies); the
 * arrows in a quiver and the bombs in a bag, item damage in 1.5.2, are the {@link #AMMO} component. Capture egg and
 * Icarus wings were never registered (allowCaptureEgg is false, icarusWings never made).
 */
public final class LGItems {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LegendGear.MOD_ID);
    static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, LegendGear.MOD_ID);
    static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LegendGear.MOD_ID);

    /** Arrows in a quiver, bombs in a bomb bag. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> AMMO = COMPONENTS.registerComponentType("ammo",
            b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** One entry: the old item id (config + 256), its damage value (-1: any) and the item. */
    public record OldId(int id, int damage, DeferredItem<? extends Item> item) {
    }

    public static final List<OldId> OLD_IDS = new ArrayList<>();

    private static <I extends Item> DeferredItem<I> old(int configId, int damage, DeferredItem<I> item) {
        OLD_IDS.add(new OldId(configId + 256, damage, item));
        return item;
    }

    private static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, LegendGear.id(name));
    }

    private static Map<ArmorType, Integer> noDefense() {
        Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        for (ArmorType type : ArmorType.values()) {
            map.put(type, 0);
        }
        return map;
    }

    private static final TagKey<Item> NO_REPAIR = TagKey.create(Registries.ITEM, LegendGear.id("repairs/none"));
    /** EnumArmorMaterial HEADBAND (0, {0, 0, 0, 0}, 0): no armour points; the items set their own durability. */
    static final ArmorMaterial HEADBAND = new ArmorMaterial(1, noDefense(), 1, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, NO_REPAIR, asset("headband"));
    static final ArmorMaterial WIND_BOOTS_MATERIAL = new ArmorMaterial(1, noDefense(), 1, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, NO_REPAIR,
            asset("wind_boots"));

    // ---------------------------------------------------------------- items in the order of LegendGear.load
    public static final DeferredItem<Item> HEADBAND_OF_VALOR = old(5759, -1, ITEMS.registerSimpleItem("headband",
            p -> p.humanoidArmor(HEADBAND, ArmorType.HELMET).durability(150)));
    public static final DeferredItem<Item> WIND_BOOTS = old(5778, -1, ITEMS.registerSimpleItem("wind_boots",
            p -> p.humanoidArmor(WIND_BOOTS_MATERIAL, ArmorType.BOOTS).durability(6000)));
    public static final DeferredItem<MagicMirrorItem> MAGIC_MIRROR = old(5764, -1, ITEMS.registerItem("magic_mirror", MagicMirrorItem::new,
            p -> p.stacksTo(1).component(DataComponents.BLOCKS_ATTACKS, hexxit.combat152.Combat152.BLOCKING)));
    public static final DeferredItem<EmeraldShardItem> EMERALD_SHARD = old(5755, 0, ITEMS.registerItem("emerald_shard",
            p -> new EmeraldShardItem(p, false)));
    public static final DeferredItem<EmeraldShardItem> EMERALD_PIECE = old(5755, 1, ITEMS.registerItem("emerald_piece",
            p -> new EmeraldShardItem(p, true)));
    public static final DeferredItem<AmmoBagItem> QUIVER = old(5771, -1, ITEMS.registerItem("quiver", p -> new AmmoBagItem(p, false),
            p -> p.stacksTo(1).component(AMMO.get(), 0)));
    public static final DeferredItem<HeartItem> HEART = old(5760, -1, ITEMS.registerItem("heart", HeartItem::new, p -> p.stacksTo(1)));
    public static final DeferredItem<MysticSeedItem> MYSTIC_SEED = old(5767, -1, ITEMS.registerItem("mystic_seed", MysticSeedItem::new));
    public static final DeferredItem<BlockItem> MYSTIC_SHRUB = ITEMS.registerSimpleBlockItem(LGBlocks.MYSTIC_SHRUB);
    public static final DeferredItem<StateBlockItem> ENERGIZED_MYSTIC_SHRUB = ITEMS.registerItem("energized_mystic_shrub",
            p -> new StateBlockItem(LGBlocks.MYSTIC_SHRUB.get(), p, s -> s.setValue(MysticShrubBlock.STAGE, 1), false), p -> p.useBlockDescriptionPrefix());
    public static final DeferredItem<Item> PHOENIX_FEATHER = old(5769, -1, ITEMS.registerSimpleItem("phoenix_feather",
            p -> p.stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));

    private static DeferredItem<MedallionItem> medallion(int id, String name, MedallionEntity.Kind kind) {
        return old(id, -1, ITEMS.registerItem(name, p -> new MedallionItem(p, (level, thrower) -> new MedallionEntity(level, thrower, kind)),
                p -> p.durability(MedallionItem.MAX_DAMAGE).setNoCombineRepair()));
    }

    public static final DeferredItem<MedallionItem> EARTH_MEDALLION = medallion(5754, "earth_medallion", MedallionEntity.Kind.EARTH);
    public static final DeferredItem<MedallionItem> WIND_MEDALLION = medallion(5779, "wind_medallion", MedallionEntity.Kind.WIND);
    public static final DeferredItem<MedallionItem> FIRE_MEDALLION = medallion(5757, "fire_medallion", MedallionEntity.Kind.FIRE);
    public static final DeferredItem<MedallionItem> ENDER_MEDALLION = medallion(5756, "ender_medallion", MedallionEntity.Kind.ENDER);

    private static DeferredItem<AmuletItem> amulet(int id, String name, AmuletItem.Kind kind) {
        return old(id, -1, ITEMS.registerItem(name, p -> new AmuletItem(p, kind),
                p -> p.durability(50).component(DataComponents.BLOCKS_ATTACKS, hexxit.combat152.Combat152.BLOCKING)));
    }

    public static final DeferredItem<AmuletItem> AERO_AMULET = amulet(5751, "aero_amulet", AmuletItem.Kind.AERO);
    public static final DeferredItem<AmuletItem> GEO_AMULET = amulet(5758, "geo_amulet", AmuletItem.Kind.GEO);
    public static final DeferredItem<AmuletItem> PYRO_AMULET = amulet(5770, "pyro_amulet", AmuletItem.Kind.PYRO);
    public static final DeferredItem<TitanBandItem> TITAN_BAND = old(5777, -1, ITEMS.registerItem("titan_band", TitanBandItem::new,
            p -> p.durability(50).component(DataComponents.BLOCKS_ATTACKS, hexxit.combat152.Combat152.BLOCKING)));
    public static final DeferredItem<BoomerangItem> MAGIC_BOOMERANG = old(5763, -1, ITEMS.registerItem("magic_boomerang", BoomerangItem::new,
            p -> p.durability(750)));
    public static final DeferredItem<HookshotItem> HOOKSHOT = old(5761, -1, ITEMS.registerItem("hookshot", HookshotItem::new, p -> p.durability(256)));
    public static final DeferredItem<BombItem> BOMB = old(5776, -1, ITEMS.registerItem("bomb", BombItem::new, p -> p.stacksTo(16)));
    public static final DeferredItem<AmmoBagItem> BOMB_BAG = old(5752, -1, ITEMS.registerItem("bomb_bag", p -> new AmmoBagItem(p, true),
            p -> p.stacksTo(1).component(AMMO.get(), 0)));
    public static final DeferredItem<BlockItem> BOMB_FLOWER = ITEMS.registerSimpleBlockItem(LGBlocks.BOMB_FLOWER);

    /** ItemRockCandy: eaten in 32 ticks, 1 food, speed III for 15 s and the candy's own effect; leaves the stick. */
    private static DeferredItem<Item> candy(int damage, String name, MobEffectInstance effect) {
        List<MobEffectInstance> effects = effect == null ? List.of(new MobEffectInstance(MobEffects.SPEED, 300, 2))
                : List.of(new MobEffectInstance(MobEffects.SPEED, 300, 2), effect);
        Consumable consumable = Consumables.defaultFood().consumeSeconds(1.6F).onConsume(new ApplyStatusEffectsConsumeEffect(effects)).build();
        return old(5772, damage, ITEMS.registerSimpleItem(name, p -> p.stacksTo(1).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.0F)
                .alwaysEdible().build(), consumable).usingConvertsTo(Items.STICK)));
    }

    public static final DeferredItem<Item> REDSTONE_ROCK_CANDY = candy(1, "redstone_rock_candy", new MobEffectInstance(MobEffects.STRENGTH, 300, 0));
    public static final DeferredItem<Item> EMERALD_ROCK_CANDY = candy(3, "emerald_rock_candy", new MobEffectInstance(MobEffects.HASTE, 300, 3));
    public static final DeferredItem<Item> LAPIS_ROCK_CANDY = candy(2, "lapis_rock_candy", new MobEffectInstance(MobEffects.JUMP_BOOST, 300, 1));
    public static final DeferredItem<Item> DIAMOND_ROCK_CANDY = candy(4, "diamond_rock_candy", new MobEffectInstance(MobEffects.RESISTANCE, 300, 3));
    public static final DeferredItem<BlockItem> SUGAR_CUBE = ITEMS.registerSimpleBlockItem(LGBlocks.SUGAR_CUBE);
    /** ItemMilkChocolate: ItemFood(1, 0.6), always edible, cures like milk. */
    public static final DeferredItem<Item> MILK_CHOCOLATE = old(5766, -1, ITEMS.registerSimpleItem("milk_chocolate",
            p -> p.food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.6F).alwaysEdible().build(),
                    Consumables.defaultFood().onConsume(ClearAllStatusEffectsConsumeEffect.INSTANCE).build())));
    public static final DeferredItem<BlockItem> CLAY_JAR = ITEMS.registerSimpleBlockItem(LGBlocks.CLAY_JAR);
    public static final DeferredItem<BlockItem> CALTROPS = ITEMS.registerSimpleBlockItem(LGBlocks.CALTROPS);
    public static final DeferredItem<BlockItem> SWORD_PEDESTAL = ITEMS.registerSimpleBlockItem(LGBlocks.SWORD_PEDESTAL);
    public static final DeferredItem<BlockItem> CHAINED_BLOCK = ITEMS.registerSimpleBlockItem(LGBlocks.CHAINED);

    private static DeferredItem<StateBlockItem> lock(String name, int kind) {
        return ITEMS.registerItem(name, p -> new StateBlockItem(LGBlocks.CHAINED.get(), p, s -> s.setValue(ChainedBlock.KIND, kind), false));
    }

    public static final DeferredItem<StateBlockItem> IRON_LOCK = lock("iron_lock", 2);
    public static final DeferredItem<StateBlockItem> GOLD_LOCK = lock("gold_lock", 3);
    public static final DeferredItem<StateBlockItem> DIAMOND_LOCK = lock("diamond_lock", 4);
    /** ItemKey: iron, gold, diamond (damage 0, 1, 2), each opens its lock once. */
    public static final List<DeferredItem<Item>> KEYS = List.of(
            old(5768, 0, ITEMS.registerSimpleItem("iron_key")),
            old(5768, 1, ITEMS.registerSimpleItem("gold_key")),
            old(5768, 2, ITEMS.registerSimpleItem("diamond_key")));
    /** ItemStardust: stardust, star piece, starstone (damage 0, 1, 2; all glowing). The starstone is the placeable block. */
    public static final DeferredItem<Item> STARDUST = old(5775, 0, ITEMS.registerSimpleItem("stardust",
            p -> p.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
    public static final DeferredItem<Item> STAR_PIECE = old(5775, 1, ITEMS.registerSimpleItem("star_piece",
            p -> p.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
    public static final DeferredItem<BlockItem> STARSTONE = old(5775, 2, ITEMS.registerSimpleBlockItem(LGBlocks.STARSTONE,
            p -> p.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
    public static final DeferredItem<MagicPowderItem> MAGIC_POWDER = old(5765, -1, ITEMS.registerItem("magic_powder", MagicPowderItem::new,
            p -> p.stacksTo(16)));
    public static final DeferredItem<ReedPipesItem> REED_PIPES = old(5773, -1, ITEMS.registerItem("reed_pipes", ReedPipesItem::new, p -> p.stacksTo(1)));
    public static final DeferredItem<BlockItem> SKYBEAM = ITEMS.registerSimpleBlockItem(LGBlocks.SKYBEAM);
    /** ItemSlimeSword: damage 1 in all (the player's own 1, no bonus), 131 uses, enchantability 18, slime balls repair. */
    public static final DeferredItem<SlimeSwordItem> SLIME_SWORD = old(5774, -1, ITEMS.registerItem("slime_sword", SlimeSwordItem::new,
            p -> p.durability(131).enchantable(18).repairable(Items.SLIME_BALL).component(DataComponents.WEAPON, new Weapon(1))
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 0.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND)
                            .build())));
    public static final DeferredItem<StandingAndWallBlockItem> STARBEAM_TORCH = ITEMS.registerItem("starbeam_torch",
            p -> new StandingAndWallBlockItem(LGBlocks.STARBEAM_TORCH.get(), LGBlocks.STARBEAM_WALL_TORCH.get(), Direction.DOWN, p),
            p -> p.useBlockDescriptionPrefix());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("legendgear", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.legendgear"))
            .icon(() -> new ItemStack(MAGIC_BOOMERANG.get()))
            .displayItems((params, output) -> ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());

    private LGItems() {
    }

    static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        COMPONENTS.register(modBus);
        TABS.register(modBus);
    }
}
