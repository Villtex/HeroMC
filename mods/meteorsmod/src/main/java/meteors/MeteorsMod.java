package meteors;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import meteors.block.FrezariteBlock;
import meteors.block.KreknoriteBlock;
import meteors.block.MeteorBlock;
import meteors.block.MeteorShieldBlock;
import meteors.block.MeteorShieldBlockEntity;
import meteors.block.MeteorTimerBlock;
import meteors.block.MeteorTimerBlockEntity;
import meteors.block.ShieldTorchBlock;
import meteors.block.ShieldWallTorchBlock;
import meteors.entity.AlienCreeper;
import meteors.entity.CometKitty;
import meteors.entity.FallingMeteor;
import meteors.entity.MeteorSummonerEntity;
import meteors.gen.MeteorsWorldGen;
import meteors.item.EnchItem;
import meteors.item.FrezariteHoeItem;
import meteors.item.FrezariteSwordItem;
import meteors.item.SummonerItem;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Falling Meteors 2.10.2 (FML id "Meteors"): meteors that fall at night near a random player (see {@link MeteorData}
 * and {@link MeteorEvents}), their craters ({@link Craters}), the meteor shield, the protected land tester, the
 * meteor timer, detectors, summoners, tools, armour, ice cream, the alien creeper and the comet kitty. Ids of the
 * pack's Meteors.cfg for chests and old worlds (OldGameIds / OldBlockStates), world generation in
 * {@link MeteorsWorldGen}.
 */
@Mod(MeteorsMod.MOD_ID)
public final class MeteorsMod {
    public static final String MOD_ID = "meteors";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);
    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final ResourceKey<Enchantment> MAGNETIZATION = ResourceKey.create(Registries.ENCHANTMENT, id("magnetization"));
    public static final ResourceKey<Enchantment> COLD_TOUCH = ResourceKey.create(Registries.ENCHANTMENT, id("cold_touch"));

    // ---- blocks (setHardness / setResistance of 1.5.2: explosion resistance = resistance * 3 / 5) ----
    public static final DeferredBlock<MeteorBlock> FALLEN_METEOR = BLOCKS.registerBlock("fallen_meteor", MeteorBlock::new,
            p -> MeteorBlock.properties(p, 7).mapColor(MapColor.COLOR_BLACK));
    public static final DeferredBlock<Block> METEOR_ORE = BLOCKS.registerSimpleBlock("meteor_ore",
            p -> p.mapColor(MapColor.METAL).strength(10.0F, 120.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
    public static final DeferredBlock<MeteorBlock> RARE_FALLEN_METEOR = BLOCKS.registerBlock("rare_fallen_meteor", MeteorBlock::new,
            p -> MeteorBlock.properties(p, 7).mapColor(MapColor.COLOR_RED));
    public static final DeferredBlock<MeteorShieldBlock> METEOR_SHIELD = BLOCKS.registerBlock("meteor_shield", MeteorShieldBlock::new,
            p -> p.mapColor(MapColor.STONE).strength(2.5F).sound(SoundType.STONE).requiresCorrectToolForDrops().randomTicks()
                    .lightLevel(s -> s.getValue(MeteorShieldBlock.POWER) == 5 ? 15 : 7));
    public static final DeferredBlock<FrezariteBlock> FREZARITE = BLOCKS.registerBlock("frezarite", FrezariteBlock::new,
            p -> p.mapColor(MapColor.ICE).strength(8.5F, 90.0F).sound(SoundType.GLASS).friction(0.98F).lightLevel(s -> 3)
                    .requiresCorrectToolForDrops().randomTicks());
    public static final DeferredBlock<KreknoriteBlock> KREKNORITE = BLOCKS.registerBlock("kreknorite", KreknoriteBlock::new,
            p -> MeteorBlock.properties(p, 10).mapColor(MapColor.FIRE).strength(11.0F, 210.0F));
    /** The protected land tester: one block for the lit (1208) and the unlit (1209) torch. */
    public static final DeferredBlock<ShieldTorchBlock> PROTECTED_LAND_TESTER = BLOCKS.registerBlock("protected_land_tester",
            p -> new ShieldTorchBlock(p), MeteorsMod::torch);
    public static final DeferredBlock<ShieldWallTorchBlock> WALL_PROTECTED_LAND_TESTER = BLOCKS.registerBlock("wall_protected_land_tester",
            p -> new ShieldWallTorchBlock(p), MeteorsMod::torch);
    public static final DeferredBlock<MeteorTimerBlock> METEOR_TIMER = BLOCKS.registerBlock("meteor_timer", MeteorTimerBlock::new,
            p -> p.mapColor(MapColor.STONE).instabreak().sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<Block> RED_METEOR_GEM_BLOCK = BLOCKS.registerSimpleBlock("red_meteor_gem_block",
            p -> p.mapColor(MapColor.COLOR_RED).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MeteorShieldBlockEntity>> METEOR_SHIELD_ENTITY = BLOCK_ENTITIES.register(
            "meteor_shield", () -> new BlockEntityType<>(MeteorShieldBlockEntity::new, METEOR_SHIELD.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MeteorTimerBlockEntity>> METEOR_TIMER_ENTITY = BLOCK_ENTITIES.register(
            "meteor_timer", () -> new BlockEntityType<>(MeteorTimerBlockEntity::new, METEOR_TIMER.get()));

    // ---- entities: registerModEntity(..., 64, 8, true) / (..., 80, 3, true) ----
    public static final DeferredHolder<EntityType<?>, EntityType<FallingMeteor>> FALLING_METEOR = ENTITIES.registerEntityType("falling_meteor",
            FallingMeteor::new, MobCategory.MISC, b -> b.sized(0.98F, 0.98F).clientTrackingRange(4).updateInterval(8).noLootTable());
    public static final DeferredHolder<EntityType<?>, EntityType<MeteorSummonerEntity>> METEOR_SUMMONER_ENTITY = ENTITIES.registerEntityType(
            "meteor_summoner", MeteorSummonerEntity::new, MobCategory.MISC, b -> b.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(8));
    public static final DeferredHolder<EntityType<?>, EntityType<AlienCreeper>> ALIEN_CREEPER = ENTITIES.registerEntityType("alien_creeper",
            AlienCreeper::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.7F).clientTrackingRange(5).updateInterval(3).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<CometKitty>> COMET_KITTY = ENTITIES.registerEntityType("comet_kitty",
            CometKitty::new, MobCategory.CREATURE, b -> b.sized(0.6F, 0.7F).eyeHeight(0.35F).passengerAttachments(0.5125F)
                    .clientTrackingRange(5).updateInterval(3).fireImmune());

    // ---- materials ----
    private static final TagKey<Item> NO_REPAIR = TagKey.create(Registries.ITEM, id("no_repair"));
    /** EnumHelper.addToolMaterial("METEORITE", 3, 900, 10.0F, 4, 10) and ("FREZARITE", 2, 225, 7.0F, 2, 14); no repair material. */
    public static final ToolMaterial METEORITE_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 900, 10.0F, 4.0F, 10, NO_REPAIR);
    public static final ToolMaterial FREZARITE_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 225, 7.0F, 2.0F, 14, NO_REPAIR);
    /** EnumHelper.addArmorMaterial: durability factor, reductions helmet/chest/legs/boots, enchantability. */
    public static final ArmorMaterial METEORITE_ARMOR = armor("meteorite", 36, 2, 7, 5, 2, 15);
    public static final ArmorMaterial FREZARITE_ARMOR = armor("frezarite", 7, 2, 5, 3, 1, 25);
    public static final ArmorMaterial KREKNORITE_ARMOR = armor("kreknorite", 40, 3, 8, 6, 3, 10);

    // ---- items ----
    public static final DeferredItem<Item> METEOR_CHIPS = ITEMS.registerSimpleItem("meteor_chips");
    public static final DeferredItem<Item> RED_METEOR_GEM = ITEMS.registerSimpleItem("red_meteor_gem");
    public static final DeferredItem<Item> FREZARITE_CRYSTAL = ITEMS.registerSimpleItem("frezarite_crystal");
    public static final DeferredItem<Item> KREKNORITE_CHIP = ITEMS.registerSimpleItem("kreknorite_chip");
    /** ItemFoodMeteorsMod(id, 2 / 3, false): saturation modifier 0.6. */
    public static final DeferredItem<Item> VANILLA_ICE_CREAM = ITEMS.registerSimpleItem("vanilla_ice_cream", p -> p.food(food(2)));
    public static final DeferredItem<Item> CHOCOLATE_ICE_CREAM = ITEMS.registerSimpleItem("chocolate_ice_cream", p -> p.food(food(3)));
    public static final DeferredItem<Item> METEOR_PROXIMITY_DETECTOR = ITEMS.registerSimpleItem("meteor_proximity_detector");
    public static final DeferredItem<Item> METEOR_TIME_DETECTOR = ITEMS.registerSimpleItem("meteor_time_detector");
    public static final DeferredItem<Item> METEOR_CRASH_DETECTOR = ITEMS.registerSimpleItem("meteor_crash_detector");
    /** ItemSummoner with damage 0..5: random, meteorite, frezarite, kreknorite, unknown, kitty. */
    public static final List<DeferredItem<SummonerItem>> SUMMONERS = new ArrayList<>();

    static {
        String[] names = {"meteor_summoner", "meteorite_meteor_summoner", "frezarite_meteor_summoner", "kreknorite_meteor_summoner",
                "unknown_meteor_summoner", "kitty_comet_summoner"};
        for (int i = 0; i < names.length; i++) {
            int type = i - 1;
            SUMMONERS.add(ITEMS.registerItem(names[i], p -> new SummonerItem(p, type), p -> p.stacksTo(16)));
        }
    }

    public static final DeferredItem<Item> METEORITE_HELMET = armorItem("meteorite_helmet", METEORITE_ARMOR, ArmorType.HELMET, MAGNETIZATION, 1);
    public static final DeferredItem<Item> METEORITE_CHESTPLATE = armorItem("meteorite_chestplate", METEORITE_ARMOR, ArmorType.CHESTPLATE, MAGNETIZATION, 1);
    public static final DeferredItem<Item> METEORITE_LEGGINGS = armorItem("meteorite_leggings", METEORITE_ARMOR, ArmorType.LEGGINGS, MAGNETIZATION, 1);
    public static final DeferredItem<Item> METEORITE_BOOTS = armorItem("meteorite_boots", METEORITE_ARMOR, ArmorType.BOOTS, MAGNETIZATION, 1);
    // ItemTool damage of 1.5.2: sword 4, axe 3, pickaxe 2, spade 1 + material damage; hoe without
    public static final DeferredItem<Item> METEORITE_AXE = ench("meteorite_axe", MAGNETIZATION, 1, p -> p.axe(METEORITE_TOOL, 2.0F, -3.0F));
    public static final DeferredItem<Item> METEORITE_SHOVEL = ench("meteorite_shovel", MAGNETIZATION, 1, p -> p.shovel(METEORITE_TOOL, 0.0F, -3.0F));
    public static final DeferredItem<Item> METEORITE_SWORD = ench("meteorite_sword", MAGNETIZATION, 1, p -> p.sword(METEORITE_TOOL, 3.0F, -2.4F));
    public static final DeferredItem<Item> METEORITE_PICKAXE = ench("meteorite_pickaxe", MAGNETIZATION, 1, p -> p.pickaxe(METEORITE_TOOL, 1.0F, -2.8F));
    public static final DeferredItem<Item> METEORITE_HOE = ench("meteorite_hoe", MAGNETIZATION, 1, p -> p.hoe(METEORITE_TOOL, -4.0F, 0.0F));
    public static final DeferredItem<Item> FREZARITE_HELMET = armorItem("frezarite_helmet", FREZARITE_ARMOR, ArmorType.HELMET, Enchantments.RESPIRATION, 3);
    public static final DeferredItem<Item> FREZARITE_CHESTPLATE = armorItem("frezarite_chestplate", FREZARITE_ARMOR, ArmorType.CHESTPLATE, Enchantments.AQUA_AFFINITY, 1);
    public static final DeferredItem<Item> FREZARITE_LEGGINGS = armorItem("frezarite_leggings", FREZARITE_ARMOR, ArmorType.LEGGINGS, COLD_TOUCH, 1);
    public static final DeferredItem<Item> FREZARITE_BOOTS = armorItem("frezarite_boots", FREZARITE_ARMOR, ArmorType.BOOTS, COLD_TOUCH, 1);
    public static final DeferredItem<Item> FREZARITE_PICKAXE = frezarite("frezarite_pickaxe", "frezPickaxe", Item::new, p -> p.pickaxe(FREZARITE_TOOL, 1.0F, -2.8F));
    public static final DeferredItem<Item> FREZARITE_SHOVEL = frezarite("frezarite_shovel", "frezSpade", Item::new, p -> p.shovel(FREZARITE_TOOL, 0.0F, -3.0F));
    public static final DeferredItem<Item> FREZARITE_SWORD = frezarite("frezarite_sword", "frezSword", FrezariteSwordItem::new, p -> p.sword(FREZARITE_TOOL, 3.0F, -2.4F));
    public static final DeferredItem<Item> FREZARITE_AXE = frezarite("frezarite_axe", "frezAxe", Item::new, p -> p.axe(FREZARITE_TOOL, 2.0F, -3.0F));
    public static final DeferredItem<Item> FREZARITE_HOE = frezarite("frezarite_hoe", "frezHoe", FrezariteHoeItem::new, p -> p.hoe(FREZARITE_TOOL, -2.0F, 0.0F));
    public static final DeferredItem<Item> KREKNORITE_HELMET = armorItem("kreknorite_helmet", KREKNORITE_ARMOR, ArmorType.HELMET, Enchantments.FIRE_PROTECTION, 4);
    public static final DeferredItem<Item> KREKNORITE_CHESTPLATE = armorItem("kreknorite_chestplate", KREKNORITE_ARMOR, ArmorType.CHESTPLATE, Enchantments.FIRE_PROTECTION, 4);
    public static final DeferredItem<Item> KREKNORITE_LEGGINGS = armorItem("kreknorite_leggings", KREKNORITE_ARMOR, ArmorType.LEGGINGS, Enchantments.FIRE_PROTECTION, 4);
    public static final DeferredItem<Item> KREKNORITE_BOOTS = armorItem("kreknorite_boots", KREKNORITE_ARMOR, ArmorType.BOOTS, Enchantments.FIRE_PROTECTION, 4);
    /** ItemKreknoSword: the meteorite material, getDamageVsEntity 8 (= 4 + 4 of METEORITE), Fire Aspect II. */
    public static final DeferredItem<Item> KREKNORITE_SWORD = ench("kreknorite_sword", Enchantments.FIRE_ASPECT, 2, p -> p.sword(METEORITE_TOOL, 3.0F, -2.4F));

    public static final DeferredItem<BlockItem> FALLEN_METEOR_ITEM = ITEMS.registerSimpleBlockItem(FALLEN_METEOR);
    public static final DeferredItem<BlockItem> METEOR_ORE_ITEM = ITEMS.registerSimpleBlockItem(METEOR_ORE);
    public static final DeferredItem<BlockItem> RARE_FALLEN_METEOR_ITEM = ITEMS.registerSimpleBlockItem(RARE_FALLEN_METEOR);
    public static final DeferredItem<BlockItem> METEOR_SHIELD_ITEM = ITEMS.registerSimpleBlockItem(METEOR_SHIELD);
    public static final DeferredItem<BlockItem> FREZARITE_ITEM = ITEMS.registerSimpleBlockItem(FREZARITE);
    public static final DeferredItem<BlockItem> KREKNORITE_ITEM = ITEMS.registerSimpleBlockItem(KREKNORITE);
    public static final DeferredItem<BlockItem> PROTECTED_LAND_TESTER_ITEM = ITEMS.registerItem("protected_land_tester",
            p -> new StandingAndWallBlockItem(PROTECTED_LAND_TESTER.get(), WALL_PROTECTED_LAND_TESTER.get(), Direction.DOWN, p),
            Item.Properties::useBlockDescriptionPrefix);
    public static final DeferredItem<BlockItem> METEOR_TIMER_ITEM = ITEMS.registerSimpleBlockItem(METEOR_TIMER);
    public static final DeferredItem<BlockItem> RED_METEOR_GEM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(RED_METEOR_GEM_BLOCK);

    /** EntityRegistry.registerGlobalEntityID(..., 7864485, 16732697) / (2239283, 884535): spawn eggs. */
    public static final DeferredItem<Item> ALIEN_CREEPER_SPAWN_EGG = ITEMS.registerItem("alien_creeper_spawn_egg", SpawnEggItem::new,
            p -> p.spawnEgg(ALIEN_CREEPER.get()));
    public static final DeferredItem<Item> COMET_KITTY_SPAWN_EGG = ITEMS.registerItem("comet_kitty_spawn_egg", SpawnEggItem::new,
            p -> p.spawnEgg(COMET_KITTY.get()));

    // ---- sounds and particles ----
    public static final DeferredHolder<SoundEvent, SoundEvent> METEOR_CRASH = SOUNDS.register("meteor.crash",
            () -> SoundEvent.createVariableRangeEvent(id("meteor.crash")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_POWERUP = SOUNDS.register("shield.powerup",
            () -> SoundEvent.createVariableRangeEvent(id("shield.powerup")));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> METEOR_DUST = PARTICLES.register("meteor_dust",
            () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FREZA_DUST = PARTICLES.register("freza_dust",
            () -> new SimpleParticleType(false));
    /** EntityMeteorShieldParticleFX: the colour row of particles.png as particle type (row 0..4 = meteor type, 5 = mixed). */
    public static final List<DeferredHolder<ParticleType<?>, SimpleParticleType>> SHIELD_PARTICLES = new ArrayList<>();

    static {
        for (int i = 0; i <= 5; i++) {
            SHIELD_PARTICLES.add(PARTICLES.register("meteor_shield_" + i, () -> new SimpleParticleType(false)));
        }
    }

    /** CreativeTabMeteor "Meteor": icon a fallen meteor. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("meteor", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.meteors.meteor"))
            .icon(() -> new ItemStack(FALLEN_METEOR_ITEM.get()))
            .displayItems((params, output) -> {
                ITEMS.getEntries().stream().map(DeferredHolder::get).filter(item -> !(item instanceof SpawnEggItem)).forEach(output::accept);
                // CreativeTabMeteor: the two enchanted books at their maximum level (1)
                params.holders().lookup(Registries.ENCHANTMENT).ifPresent(lookup -> {
                    for (ResourceKey<Enchantment> key : List.of(COLD_TOUCH, MAGNETIZATION)) {
                        lookup.get(key).ifPresent(e -> output.accept(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(
                                new net.minecraft.world.item.enchantment.EnchantmentInstance(e, 1))));
                    }
                });
            }).build());

    public MeteorsMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, MeteorsConfig.SPEC);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        SOUNDS.register(modBus);
        PARTICLES.register(modBus);
        TABS.register(modBus);
        modBus.addListener(MeteorsMod::attributes);
        modBus.addListener(MeteorsMod::tabs);
        modBus.addListener(MeteorNetwork::register);
        MeteorEvents.register();
        MeteorsWorldGen.register();
        registerOldIds();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            meteors.client.MeteorsClient.register(modBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static FoodProperties food(int nutrition) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(0.6F).build();
    }

    private static Block.Properties torch(Block.Properties p) {
        return p.noCollision().instabreak().sound(SoundType.WOOD).lightLevel(s -> 7).randomTicks().pushReaction(PushReaction.POPPED);
    }

    private static ArmorMaterial armor(String name, int durability, int helmet, int chest, int legs, int boots, int enchantability) {
        Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.HELMET, helmet);
        defense.put(ArmorType.CHESTPLATE, chest);
        defense.put(ArmorType.LEGGINGS, legs);
        defense.put(ArmorType.BOOTS, boots);
        defense.put(ArmorType.BODY, chest);
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, id(name));
        return new ArmorMaterial(durability, defense, enchantability, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, NO_REPAIR, asset);
    }

    private static DeferredItem<Item> armorItem(String name, ArmorMaterial material, ArmorType type, ResourceKey<Enchantment> ench, int level) {
        return ITEMS.registerItem(name, p -> new EnchItem(p, ench, level), p -> p.humanoidArmor(material, type));
    }

    private static DeferredItem<Item> ench(String name, ResourceKey<Enchantment> ench, int level, Function<Item.Properties, Item.Properties> props) {
        return ITEMS.registerItem(name, p -> new EnchItem(p, ench, level), props::apply);
    }

    /** The Frezarite tools: always glinting (hasEffect), not enchantable (getItemEnchantability 0), two tooltip lines. */
    private static DeferredItem<Item> frezarite(String name, String tooltip, Function<Item.Properties, ? extends Item> factory,
            Function<Item.Properties, Item.Properties> props) {
        return ITEMS.registerItem(name, p -> {
            Item.Properties q = props.apply(p).component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(net.minecraft.core.component.DataComponents.ENCHANTABLE, null)
                    .component(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(List.of(
                            tooltipLine("enchantment.meteors." + tooltip + ".one"), tooltipLine("enchantment.meteors." + tooltip + ".two"))));
            return factory.apply(q);
        });
    }

    private static Component tooltipLine(String key) {
        return Component.translatable(key).withStyle(s -> s.withColor(net.minecraft.ChatFormatting.DARK_AQUA).withItalic(false));
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(ALIEN_CREEPER.get(), Creeper.createAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 20.0).build());
        event.put(COMET_KITTY.get(), Cat.createAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 16.0).build());
    }

    private static void tabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ALIEN_CREEPER_SPAWN_EGG.get());
            event.accept(COMET_KITTY_SPAWN_EGG.get());
        }
    }

    /** The ids of Meteors.cfg: blocks as configured, items + 256 (Item constructor); enchantments 201 / 202. */
    private static void registerOldIds() {
        OldBlockStates.register(MeteorsWorldGen.METEOR_ORE, () -> METEOR_ORE.get().defaultBlockState());
        Object[][] items = {
                {5801, CHOCOLATE_ICE_CREAM}, {5802, FREZARITE_AXE}, {5803, FREZARITE_CHESTPLATE}, {5804, FREZARITE_BOOTS},
                {5805, FREZARITE_CRYSTAL}, {5806, FREZARITE_HELMET}, {5807, FREZARITE_HOE}, {5808, FREZARITE_LEGGINGS},
                {5809, FREZARITE_PICKAXE}, {5810, FREZARITE_SHOVEL}, {5811, FREZARITE_SWORD}, {5812, KREKNORITE_CHESTPLATE},
                {5813, KREKNORITE_BOOTS}, {5814, KREKNORITE_CHIP}, {5815, KREKNORITE_HELMET}, {5816, KREKNORITE_LEGGINGS},
                {5817, KREKNORITE_SWORD}, {5818, METEORITE_AXE}, {5819, METEORITE_CHESTPLATE}, {5820, METEORITE_BOOTS},
                {5821, METEOR_CHIPS}, {5822, METEOR_CRASH_DETECTOR}, {5823, METEORITE_HELMET}, {5824, METEORITE_HOE},
                {5825, METEORITE_LEGGINGS}, {5826, METEORITE_PICKAXE}, {5827, METEOR_PROXIMITY_DETECTOR}, {5828, METEORITE_SHOVEL},
                {5830, METEORITE_SWORD}, {5831, METEOR_TIME_DETECTOR}, {5832, RED_METEOR_GEM}, {5833, VANILLA_ICE_CREAM}};
        for (Object[] entry : items) {
            OldGameIds.registerItem((Integer) entry[0] + 256, MOD_ID + ":" + ((DeferredItem<?>) entry[1]).getId().getPath());
        }
        for (int damage = 0; damage < SUMMONERS.size(); damage++) {
            OldGameIds.registerItem(5829 + 256, damage, MOD_ID + ":" + SUMMONERS.get(damage).getId().getPath());
        }
        Object[][] blocks = {
                {MeteorsWorldGen.RED_METEOR_GEM_BLOCK, RED_METEOR_GEM_BLOCK}, {MeteorsWorldGen.FALLEN_METEOR, FALLEN_METEOR},
                {MeteorsWorldGen.FREZARITE, FREZARITE}, {MeteorsWorldGen.KREKNORITE, KREKNORITE}, {MeteorsWorldGen.METEOR_SHIELD, METEOR_SHIELD},
                {MeteorsWorldGen.METEOR_TIMER, METEOR_TIMER}, {MeteorsWorldGen.RARE_FALLEN_METEOR, RARE_FALLEN_METEOR},
                {MeteorsWorldGen.TORCH_LIT, PROTECTED_LAND_TESTER}, {MeteorsWorldGen.METEOR_ORE, METEOR_ORE}};
        for (Object[] entry : blocks) {
            OldGameIds.registerItem((Integer) entry[0], MOD_ID + ":" + ((DeferredBlock<?>) entry[1]).getId().getPath());
        }
        OldGameIds.registerEnchantment(201, COLD_TOUCH);
        OldGameIds.registerEnchantment(202, MAGNETIZATION);
        // registerModEntity names of 1.5.2
        OldGameIds.registerEntity("Meteors.FallingMeteor", MOD_ID + ":falling_meteor");
        OldGameIds.registerEntity("Meteors.AlienCreeper", MOD_ID + ":alien_creeper");
        OldGameIds.registerEntity("AlienCreeper", MOD_ID + ":alien_creeper");
        OldGameIds.registerEntity("Meteors.CometKitty", MOD_ID + ":comet_kitty");
        OldGameIds.registerEntity("CometKitty", MOD_ID + ":comet_kitty");
    }
}
