package chococraft;

import chococraft.block.GysahlGreenBlock;
import chococraft.block.GysahlStemBlock;
import chococraft.block.StrawBlock;
import chococraft.entity.AbstractChocobo;
import chococraft.entity.Chicobo;
import chococraft.entity.Chocobo;
import chococraft.entity.ChocoboColor;
import chococraft.entity.ChocoboSpawner;
import chococraft.entity.PurpleChocoboEgg;
import chococraft.gen.ChocoWorldGen;
import chococraft.item.ChocoboFeatherItem;
import chococraft.item.ChocopediaItem;
import chococraft.item.PurpleChocoboEggItem;
import chococraft.network.ChocoNetwork;
import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Torojimas ChocoCraft 2.8.8 (FML id "chococraft"): chocobos in nine colours with chicobos, the gysahl plants, straw,
 * the Chocopedia, saddle and bags, the chocodisguise. Item and block ids of the pack's chococraft.cfg for chests and
 * old worlds (OldGameIds / OldBlockStates), world generation in {@link ChocoWorldGen}.
 */
@Mod(ChocoCraft.MOD_ID)
public final class ChocoCraft {
    public static final String MOD_ID = "chococraft";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);

    // ---- blocks (hardness 0, grass step sound) ----
    public static final DeferredBlock<GysahlGreenBlock> GYSAHL_GREEN = BLOCKS.registerBlock("gysahl_green", GysahlGreenBlock::new,
            p -> p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).randomTicks().pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<GysahlStemBlock> GYSAHL_STEM = BLOCKS.registerBlock("gysahl_stem", GysahlStemBlock::new,
            p -> p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).randomTicks().pushReaction(PushReaction.POPPED));
    /** Material.circuits: no collision, pushed out by pistons. */
    public static final DeferredBlock<StrawBlock> STRAW = BLOCKS.registerBlock("straw", StrawBlock::new,
            p -> p.mapColor(MapColor.GRASS).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion().pushReaction(PushReaction.POPPED));

    // ---- entities: setSize(1.3, 1.9) / (0.5, 0.5), registerModEntity(..., 128, 1, true) ----
    private static final Map<ChocoboColor, DeferredHolder<EntityType<?>, EntityType<Chocobo>>> CHOCOBOS = new EnumMap<>(ChocoboColor.class);
    private static final Map<EntityType<?>, ChocoboColor> COLORS = new IdentityHashMap<>();

    static {
        for (ChocoboColor color : ChocoboColor.values()) {
            CHOCOBOS.put(color, ENTITIES.registerEntityType(color.id(), Chocobo::new, MobCategory.CREATURE, b -> {
                // the rider's feet 0.9 above the chocobo (mountedYOffset 1.4 + the player's yOffset 1.12 - 1.62)
                b.sized(1.3F, 1.9F).passengerAttachments(1.5F).clientTrackingRange(8).updateInterval(1);
                return color.fireImmune ? b.fireImmune() : b;
            }));
        }
    }

    public static final DeferredHolder<EntityType<?>, EntityType<Chicobo>> CHICOBO = ENTITIES.registerEntityType("chicobo", Chicobo::new,
            MobCategory.CREATURE, b -> b.sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1));
    public static final DeferredHolder<EntityType<?>, EntityType<PurpleChocoboEgg>> PURPLE_CHOCOBO_EGG_ENTITY = ENTITIES.registerEntityType(
            "purple_chocobo_egg", PurpleChocoboEgg::new, MobCategory.MISC, b -> b.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));

    // ---- items ----
    private static final TagKey<Item> REPAIRS_DISGUISE = TagKey.create(Registries.ITEM, id("repairs/choco_disguise"));
    private static final ResourceKey<EquipmentAsset> DISGUISE_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, id("choco_disguise"));
    /** ChocoboArmourMaterial.CHOCOFEATHER: no protection, durability 1, on the GOLD armour of 1.5.2 (its equip sound). */
    private static final ArmorMaterial CHOCOFEATHER = new ArmorMaterial(1, defense(), 1, SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F,
            REPAIRS_DISGUISE, DISGUISE_ASSET);

    public static final DeferredItem<ChocopediaItem> CHOCOPEDIA = ITEMS.registerItem("chocopedia", ChocopediaItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<ChocoboFeatherItem> CHOCOBO_FEATHER = ITEMS.registerItem("chocobo_feather", ChocoboFeatherItem::new);
    public static final DeferredItem<Item> CHOCOBO_SADDLE = ITEMS.registerSimpleItem("chocobo_saddle", p -> p.stacksTo(5));
    public static final DeferredItem<Item> CHOCOBO_SADDLE_BAGS = ITEMS.registerSimpleItem("chocobo_saddle_bags", p -> p.stacksTo(8));
    public static final DeferredItem<Item> CHOCOBO_PACK_BAGS = ITEMS.registerSimpleItem("chocobo_pack_bags", p -> p.stacksTo(8));
    public static final DeferredItem<BlockItem> GYSAHL_SEEDS = ITEMS.registerItem("gysahl_seeds", p -> new BlockItem(GYSAHL_STEM.get(), p),
            Item.Properties::useItemDescriptionPrefix);
    public static final DeferredItem<Item> GYSAHL_LOVERLY = ITEMS.registerSimpleItem("gysahl_loverly");
    public static final DeferredItem<Item> GYSAHL_GOLDEN = ITEMS.registerSimpleItem("gysahl_golden");
    public static final DeferredItem<Item> GYSAHL_PINK = ITEMS.registerSimpleItem("gysahl_pink");
    public static final DeferredItem<Item> GYSAHL_RED = ITEMS.registerSimpleItem("gysahl_red");
    public static final DeferredItem<Item> GYSAHL_CAKE = ITEMS.registerSimpleItem("gysahl_cake", p -> p.stacksTo(8));
    public static final DeferredItem<Item> CHOCOBO_WHISTLE = ITEMS.registerSimpleItem("chocobo_whistle");
    public static final DeferredItem<PurpleChocoboEggItem> PURPLE_CHOCOBO_EGG = ITEMS.registerItem("purple_chocobo_egg", PurpleChocoboEggItem::new);
    /** ItemFood(heal, wolf favourite): saturation modifier 0.6 of ItemFood(int, boolean). */
    public static final DeferredItem<Item> CHOCOBO_LEG_RAW = ITEMS.registerSimpleItem("chocobo_leg_raw", p -> p.food(food(4)));
    public static final DeferredItem<Item> CHOCOBO_LEG_COOKED = ITEMS.registerSimpleItem("chocobo_leg_cooked", p -> p.food(food(8)));
    public static final DeferredItem<Item> GYSAHL_RAW_PICKLES = ITEMS.registerSimpleItem("gysahl_raw_pickles");
    public static final DeferredItem<Item> GYSAHL_PICKLES = ITEMS.registerSimpleItem("gysahl_pickles", p -> p.food(food(2)));
    public static final DeferredItem<Item> CHOCO_DISGUISE_HELMET = disguise("choco_disguise_helmet", ArmorType.HELMET);
    public static final DeferredItem<Item> CHOCO_DISGUISE_CHESTPLATE = disguise("choco_disguise_chestplate", ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> CHOCO_DISGUISE_LEGGINGS = disguise("choco_disguise_leggings", ArmorType.LEGGINGS);
    public static final DeferredItem<Item> CHOCO_DISGUISE_BOOTS = disguise("choco_disguise_boots", ArmorType.BOOTS);
    public static final DeferredItem<BlockItem> GYSAHL_GREEN_ITEM = ITEMS.registerSimpleBlockItem(GYSAHL_GREEN);
    public static final DeferredItem<BlockItem> STRAW_ITEM = ITEMS.registerSimpleBlockItem(STRAW);

    /** EntityEggInfo colours of registerChocoboEntityClass. */
    private static final Map<String, DeferredItem<Item>> EGGS = new java.util.LinkedHashMap<>();

    static {
        EGGS.put("chicobo", ITEMS.registerItem("chicobo_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(CHICOBO.get())));
        for (ChocoboColor color : ChocoboColor.values()) {
            EGGS.put(color.id(), ITEMS.registerItem(color.id() + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(CHOCOBOS.get(color).get())));
        }
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> KWEH = SOUNDS.register("kweh",
            () -> SoundEvent.createVariableRangeEvent(id("kweh")));

    /** Client hook: opens the Chocopedia (the chocobo's page, or the book for null). */
    public static Consumer<@Nullable AbstractChocobo> chocopediaOpener = chocobo -> {
    };

    public ChocoCraft(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, ChocoConfig.SPEC);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(ChocoCraft::attributes);
        modBus.addListener(ChocoCraft::tabs);
        modBus.addListener(ChocoNetwork::register);
        NeoForge.EVENT_BUS.addListener(ChocoCraft::purpleRiderFire);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, ChocoboSpawner::tick);
        ChocoWorldGen.register();
        registerOldIds();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            chococraft.client.ChocoCraftClient.register(modBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static EntityType<Chocobo> chocoboType(ChocoboColor color) {
        return CHOCOBOS.get(color).get();
    }

    public static ChocoboColor colorOf(EntityType<?> type) {
        if (COLORS.isEmpty()) {
            for (ChocoboColor color : ChocoboColor.values()) {
                COLORS.put(CHOCOBOS.get(color).get(), color);
            }
        }
        return COLORS.getOrDefault(type, ChocoboColor.YELLOW);
    }

    public static Map<String, DeferredItem<Item>> spawnEggs() {
        return EGGS;
    }

    public static void openChocopedia(@Nullable AbstractChocobo chocobo) {
        chocopediaOpener.accept(chocobo);
    }

    private static FoodProperties food(int nutrition) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(0.6F).build();
    }

    private static Map<ArmorType, Integer> defense() {
        Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        for (ArmorType type : ArmorType.values()) {
            map.put(type, 0);
        }
        return map;
    }

    private static DeferredItem<Item> disguise(String name, ArmorType type) {
        return ITEMS.registerSimpleItem(name, p -> p.humanoidArmor(CHOCOFEATHER, type).durability(1));
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        for (ChocoboColor color : ChocoboColor.values()) {
            event.put(CHOCOBOS.get(color).get(), Chocobo.createAttributes(color).build());
        }
        event.put(CHICOBO.get(), Chicobo.createAttributes().build());
    }

    private static void tabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CHOCOPEDIA.get());
            event.accept(CHOCOBO_WHISTLE.get());
            event.accept(CHOCOBO_SADDLE.get());
            event.accept(CHOCOBO_SADDLE_BAGS.get());
            event.accept(CHOCOBO_PACK_BAGS.get());
        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(CHOCO_DISGUISE_HELMET.get());
            event.accept(CHOCO_DISGUISE_CHESTPLATE.get());
            event.accept(CHOCO_DISGUISE_LEGGINGS.get());
            event.accept(CHOCO_DISGUISE_BOOTS.get());
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(CHOCOBO_FEATHER.get());
            event.accept(GYSAHL_LOVERLY.get());
            event.accept(GYSAHL_GOLDEN.get());
            event.accept(GYSAHL_PINK.get());
            event.accept(GYSAHL_RED.get());
            event.accept(GYSAHL_RAW_PICKLES.get());
            event.accept(PURPLE_CHOCOBO_EGG.get());
        } else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(CHOCOBO_LEG_RAW.get());
            event.accept(CHOCOBO_LEG_COOKED.get());
            event.accept(GYSAHL_PICKLES.get());
            event.accept(GYSAHL_CAKE.get());
        } else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(GYSAHL_GREEN_ITEM.get());
            event.accept(GYSAHL_SEEDS.get());
            event.accept(STRAW_ITEM.get());
        } else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            EGGS.values().forEach(egg -> event.accept(egg.get()));
        }
    }

    /** onBurnDamage: no lava or fire damage for the rider of a purple chocobo. */
    private static void purpleRiderFire(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && player.getVehicle() instanceof Chocobo chocobo
                && chocobo.getColor() == ChocoboColor.PURPLE
                && (event.getSource().is(DamageTypes.LAVA) || event.getSource().is(DamageTypes.IN_FIRE) || event.getSource().is(DamageTypes.ON_FIRE))) {
            event.setCanceled(true);
        }
    }

    /** The ids of chococraft.cfg: blocks as configured, items + 256 (Item constructor). */
    private static void registerOldIds() {
        OldBlockStates.register(ChocoWorldGen.GYSAHL_GREEN, () -> GYSAHL_GREEN.get().defaultBlockState());
        OldBlockStates.registerMeta(ChocoWorldGen.GYSAHL_STEM, meta -> GYSAHL_STEM.get().defaultBlockState()
                .setValue(GysahlStemBlock.AGE, Math.min(meta, 4)));
        OldBlockStates.register(ChocoWorldGen.STRAW, () -> STRAW.get().defaultBlockState());
        Object[][] items = {
                {5476, CHOCO_DISGUISE_BOOTS}, {5477, CHOCO_DISGUISE_HELMET}, {5478, CHOCO_DISGUISE_LEGGINGS},
                {5479, CHOCO_DISGUISE_CHESTPLATE}, {5480, CHOCOBO_FEATHER}, {5481, CHOCOBO_LEG_COOKED}, {5482, CHOCOBO_LEG_RAW},
                {5483, CHOCOBO_PACK_BAGS}, {5484, CHOCOBO_SADDLE_BAGS}, {5485, CHOCOBO_SADDLE}, {5486, CHOCOBO_WHISTLE},
                {5487, CHOCOPEDIA}, {5488, GYSAHL_CAKE}, {5489, GYSAHL_GOLDEN}, {5490, GYSAHL_LOVERLY}, {5491, GYSAHL_PICKLES},
                {5492, GYSAHL_RAW_PICKLES}, {5493, GYSAHL_PINK}, {5494, GYSAHL_RED}, {5495, GYSAHL_SEEDS}, {5496, PURPLE_CHOCOBO_EGG}};
        for (Object[] entry : items) {
            OldGameIds.registerItem((Integer) entry[0] + 256, MOD_ID + ":" + ((DeferredItem<?>) entry[1]).getId().getPath());
        }
        OldGameIds.registerItem(ChocoWorldGen.GYSAHL_GREEN, MOD_ID + ":gysahl_green");
        OldGameIds.registerItem(ChocoWorldGen.STRAW, MOD_ID + ":straw");
        // registerModEntity names of 1.5.2, for spawners and saved worlds of other mods
        OldGameIds.registerEntity("chococraft.Chicobo", MOD_ID + ":chicobo");
        for (ChocoboColor color : ChocoboColor.values()) {
            String name = color.name().charAt(0) + color.name().substring(1).toLowerCase(java.util.Locale.ROOT);
            OldGameIds.registerEntity("chococraft.Chocobo" + name, MOD_ID + ":" + color.id());
        }
    }
}
