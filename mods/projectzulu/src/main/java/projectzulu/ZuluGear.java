package projectzulu;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.advancements.predicates.entity.EntityFlagsPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import projectzulu.block.gear.CampfireBlock;
import projectzulu.block.gear.JasperBlock;
import projectzulu.block.gear.MobHead;
import projectzulu.block.gear.MobHeadBlock;
import projectzulu.block.gear.MobHeadBlockEntity;
import projectzulu.block.gear.SpikesBlock;
import projectzulu.block.gear.TombstoneBlock;
import projectzulu.block.gear.TombstoneBlockEntity;
import projectzulu.gen.ZuluIds;
import projectzulu.item.gear.CampfireItem;
import projectzulu.item.gear.MobHeadItem;
import projectzulu.network.gear.OpenTombstonePayload;
import projectzulu.network.gear.TombstoneTextPayload;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Teil 11, batch Gear: the ten armour sets (ItemZuluArmor, temperature off), jasper, ivory spikes, campfire, mob
 * heads, tombstone, the ankh's fireball, the event hooks of EventHookContainerClass that belong to these items
 * (tombstone on death, cactus armour, desert sun) and the rare head drops of the creatures (dropRareDrop).
 */
public final class ZuluGear {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ProjectZulu.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectZulu.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ProjectZulu.MOD_ID);
    private static final DeferredRegister<GameRule<?>> GAME_RULES = DeferredRegister.create(Registries.GAME_RULE, ProjectZulu.MOD_ID);

    // ---------------------------------------------------------------- armour

    /** EnumHelper materials have no crafting material in 1.5.2: not repairable with an item (empty tag). */
    public static final TagKey<Item> NO_REPAIR = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "zulu_armor_repair"));

    private static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, name));
    }

    /** EnumHelper.addArmorMaterial(name, durability, {helmet, chest, legs, boots}, enchantability) with the set's texture. */
    private static ArmorMaterial material(int durability, int[] reduction, int enchantability, String asset) {
        Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.HELMET, reduction[0]);
        defense.put(ArmorType.CHESTPLATE, reduction[1]);
        defense.put(ArmorType.LEGGINGS, reduction[2]);
        defense.put(ArmorType.BOOTS, reduction[3]);
        defense.put(ArmorType.BODY, reduction[1]);
        return new ArmorMaterial(durability, defense, enchantability, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, NO_REPAIR, asset(asset));
    }

    private static final int[] DESERT_CLOTH = {1, 2, 1, 1};
    private static final int[] SCALE = {1, 3, 2, 1};

    /** One armour set: registry prefix, material and the 1.5.2 item ids (config id) of helmet, chest, legs, boots. */
    public record ArmorSet(String name, ArmorMaterial material, int[] oldIds, List<DeferredItem<Item>> items) {
        public DeferredItem<Item> helmet() {
            return this.items.get(0);
        }
    }

    public static final List<ArmorSet> ARMOR_SETS = new ArrayList<>();
    public static final ArmorSet SCALE_ARMOR = armorSet("scale", material(5, SCALE, 15, "scale"), 5093, 5092, 5095, 5091);
    public static final ArmorSet GOLD_SCALE_ARMOR = armorSet("gold_scale", material(7, new int[] {2, 5, 3, 1}, 25, "gold_scale"), 5077, 5076, 5078, 5075);
    public static final ArmorSet IRON_SCALE_ARMOR = armorSet("iron_scale", material(15, new int[] {2, 6, 5, 2}, 9, "iron_scale"), 5085, 5084, 5086, 5083);
    public static final ArmorSet DIAMOND_SCALE_ARMOR = armorSet("diamond_scale", material(33, new int[] {3, 8, 6, 3}, 10, "diamond_scale"),
            5067, 5066, 5068, 5065);
    public static final ArmorSet WHITE_CLOTH_ARMOR = armorSet("white_cloth", material(2, DESERT_CLOTH, 15, "white_desert_cloth"), 5101, 5100, 5102, 5099);
    public static final ArmorSet RED_CLOTH_ARMOR = armorSet("red_cloth", material(2, DESERT_CLOTH, 15, "red_desert_cloth"), 5089, 5088, 5090, 5087);
    public static final ArmorSet GREEN_CLOTH_ARMOR = armorSet("green_cloth", material(2, DESERT_CLOTH, 15, "green_desert_cloth"), 5081, 5080, 5082, 5079);
    public static final ArmorSet BLUE_CLOTH_ARMOR = armorSet("blue_cloth", material(2, DESERT_CLOTH, 15, "blue_desert_cloth"), 5055, 5054, 5056, 5053);
    /** Cactus and fur armour used scaleMaterial (furMaterial was never used). */
    public static final ArmorSet CACTUS_ARMOR = armorSet("cactus", material(5, SCALE, 15, "cactus"), 5059, 5058, 5060, 5057);
    public static final ArmorSet FUR_ARMOR = armorSet("fur", material(5, SCALE, 15, "mammoth_fur"), 5071, 5070, 5072, 5069);

    private static ArmorSet armorSet(String name, ArmorMaterial material, int... oldIds) {
        String[] parts = {"helmet", "chestplate", "leggings", "boots"};
        ArmorType[] types = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};
        List<DeferredItem<Item>> items = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ArmorType type = types[i];
            items.add(ITEMS.registerItem(name + "_" + parts[i], Item::new, p -> p.humanoidArmor(material, type)));
        }
        ArmorSet set = new ArmorSet(name, material, oldIds, items);
        ARMOR_SETS.add(set);
        return set;
    }

    // ---------------------------------------------------------------- blocks

    /** BlockJasper: Material.rock, hardness 1, resistance 1 (= 0.6 now). */
    public static final DeferredBlock<JasperBlock> JASPER = BLOCKS.registerBlock("jasper", JasperBlock::new,
            p -> p.mapColor(MapColor.COLOR_RED).strength(1.0F, 0.6F).requiresCorrectToolForDrops().randomTicks().sound(SoundType.STONE));
    /** BlockSpikes: Material.iron, hardness 0.5, metal sound. */
    public static final DeferredBlock<SpikesBlock> SPIKES = BLOCKS.registerBlock("spikes", SpikesBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(0.5F).requiresCorrectToolForDrops().noCollision().noOcclusion().sound(SoundType.METAL)
                    .dynamicShape());
    /** BlockCampfire: Material.wood, hardness 0.5, stone sound, light 15 when lit. */
    public static final DeferredBlock<CampfireBlock> CAMPFIRE = BLOCKS.registerBlock("campfire", CampfireBlock::new,
            p -> p.mapColor(MapColor.WOOD).strength(0.5F).noOcclusion().randomTicks().sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(CampfireBlock.LIT) ? 15 : 0));
    /** BlockMobHeads: Material.circuits, hardness 1, stone sound. */
    public static final DeferredBlock<MobHeadBlock> MOB_HEAD = BLOCKS.registerBlock("mob_head", MobHeadBlock::new,
            p -> p.strength(1.0F).noOcclusion().sound(SoundType.STONE).pushReaction(PushReaction.POPPED));
    /** BlockTombstone: Material.rock, hardness 0.5, metal sound, no collision. */
    public static final DeferredBlock<TombstoneBlock> TOMBSTONE = BLOCKS.registerBlock("tombstone", TombstoneBlock::new,
            p -> p.mapColor(MapColor.STONE).strength(0.5F).requiresCorrectToolForDrops().noCollision().noOcclusion().sound(SoundType.METAL));

    public static final DeferredItem<BlockItem> JASPER_ITEM = ITEMS.registerSimpleBlockItem(JASPER);
    public static final DeferredItem<BlockItem> SPIKES_ITEM = ITEMS.registerSimpleBlockItem(SPIKES);
    public static final DeferredItem<BlockItem> TOMBSTONE_ITEM = ITEMS.registerSimpleBlockItem(TOMBSTONE);
    /** ItemCampFire metadata 0-3. */
    public static final DeferredItem<CampfireItem> CAMPFIRE_ITEM = campfire("campfire", CampfireBlock.Kind.WOOD, false);
    public static final DeferredItem<CampfireItem> STONE_CAMPFIRE_ITEM = campfire("stone_campfire", CampfireBlock.Kind.STONE, false);
    public static final DeferredItem<CampfireItem> LIT_CAMPFIRE_ITEM = campfire("lit_campfire", CampfireBlock.Kind.WOOD, true);
    public static final DeferredItem<CampfireItem> LIT_STONE_CAMPFIRE_ITEM = campfire("lit_stone_campfire", CampfireBlock.Kind.STONE, true);

    /** ItemMobHeads metadata 0-17, stacks to 8. */
    public static final Map<MobHead, DeferredItem<MobHeadItem>> MOB_HEADS = new EnumMap<>(MobHead.class);

    static {
        for (MobHead head : MobHead.values()) {
            MOB_HEADS.put(head, ITEMS.registerItem("mob_head_" + head.id, p -> new MobHeadItem(head, p), p -> p.stacksTo(8)));
        }
    }

    private static DeferredItem<CampfireItem> campfire(String name, CampfireBlock.Kind kind, boolean lit) {
        return ITEMS.registerItem(name, p -> new CampfireItem(CAMPFIRE.get(), kind, lit, p), Item.Properties::useItemDescriptionPrefix);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MobHeadBlockEntity>> MOB_HEAD_ENTITY = BLOCK_ENTITIES.register("mob_head",
            () -> new BlockEntityType<>(MobHeadBlockEntity::new, MOB_HEAD.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TombstoneBlockEntity>> TOMBSTONE_ENTITY = BLOCK_ENTITIES.register("tombstone",
            () -> new BlockEntityType<>(TombstoneBlockEntity::new, TOMBSTONE.get()));

    /** The game rule doesCampfireBurn (added at server start, default false). */
    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> DOES_CAMPFIRE_BURN = GAME_RULES.register("does_campfire_burn",
            () -> new GameRule<>(GameRuleCategory.UPDATES, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL,
                    value -> value ? 1 : 0, false, FeatureFlagSet.of()));

    /** Properties.tombstoneOnDeath, "Drop Tombstone On Death" of the pack configuration. */
    public static final boolean TOMBSTONE_ON_DEATH = true;

    private ZuluGear() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        GAME_RULES.register(modBus);
        modBus.addListener(ZuluGear::registerPayloads);
        modBus.addListener(ZuluGear::modifyComponents);
        NeoForge.EVENT_BUS.addListener(ZuluGear::playerDeath);
        NeoForge.EVENT_BUS.addListener(ZuluGear::cactusArmorDamage);
        NeoForge.EVENT_BUS.addListener(ZuluGear::useAnkh);
        NeoForge.EVENT_BUS.addListener(ZuluGear::addHeadDrops);
        NeoForge.EVENT_BUS.addListener(ZuluGear::desertSun);
        registerOldIds();
    }

    // ---------------------------------------------------------------- 1.5.2 ids (world generation, chest loot)

    private static void registerOldIds() {
        OldBlockStates.register(ZuluIds.JASPER, () -> JASPER.get().defaultBlockState());
        OldBlockStates.registerMeta(ZuluIds.CAMPFIRE, meta -> CAMPFIRE.get().byMeta(meta));
        OldBlockStates.registerMeta(ZuluIds.MOB_SKULLS, meta -> MOB_HEAD.get().byMeta(meta));
        OldBlockStates.registerMeta(ZuluIds.SPIKES, meta -> SPIKES.get().byMeta(meta));
        OldBlockStates.registerMeta(ZuluIds.TOMBSTONE, meta -> TOMBSTONE.get().defaultBlockState().setValue(TombstoneBlock.ROTATION, meta & 7));

        String ns = ProjectZulu.MOD_ID + ":";
        OldGameIds.registerItem(ZuluIds.JASPER, ns + "jasper");
        OldGameIds.registerItem(ZuluIds.SPIKES, ns + "spikes");
        OldGameIds.registerItem(ZuluIds.TOMBSTONE, ns + "tombstone");
        String[] campfires = {"campfire", "stone_campfire", "lit_campfire", "lit_stone_campfire"};
        for (int meta = 0; meta < campfires.length; meta++) {
            OldGameIds.registerItem(ZuluIds.CAMPFIRE, meta, ns + campfires[meta]);
        }
        for (MobHead head : MobHead.values()) {
            OldGameIds.registerItem(ZuluIds.MOB_SKULLS, head.meta(), ns + "mob_head_" + head.id);
        }
        for (ArmorSet set : ARMOR_SETS) {
            for (int i = 0; i < 4; i++) {
                OldGameIds.registerItem(set.oldIds()[i] + 256, ns + set.items().get(i).getId().getPath());
            }
        }
        OldGameIds.registerItem(ZuluIds.ANKH, ns + "ankh");
        // ItemGenerics (the shiny bauble is dungeon chest loot), metadata -> item of ZuluItems
        Object[][] generics = {{0, "poison_droplet"}, {1, "tusk"}, {2, "raw_fiber"}, {20, "black_lichen"}, {21, "pulp"}, {22, "salt"},
                {23, "antennae"}, {24, "shiny_bauble"}, {25, "talon"}, {26, "plant_stalk"}, {27, "bark"}, {28, "small_heart"},
                {29, "large_heart"}, {30, "gill"}, {31, "ectoplasm"}, {32, "frog_legs"}, {33, "rabbits_foot"}, {34, "prickly_powder"},
                {35, "powder_slush"}, {36, "glowing_goo"}, {37, "small_unhealthy_heart"}, {38, "large_unhealthy_heart"}};
        for (Object[] generic : generics) {
            OldGameIds.registerItem(ZuluIds.GENERIC_CRAFTING_ITEMS, (Integer) generic[0], ns + generic[1]);
        }
    }

    // ---------------------------------------------------------------- events

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(TombstoneTextPayload.TYPE, TombstoneTextPayload.CODEC, TombstoneTextPayload::handle);
        // handled by the client (projectzulu.client.GearClient)
        registrar.playToClient(OpenTombstonePayload.TYPE, OpenTombstonePayload.CODEC);
    }

    /** ItemAnkh: maxStackSize 1 (the ankh item itself is registered in ZuluItems). */
    private static void modifyComponents(ModifyDefaultComponentsEvent event) {
        event.modify(ZuluItems.ANKH.get(), (components, context, item) -> components.set(DataComponents.MAX_STACK_SIZE, 1));
    }

    /** PlayerDeathTest: a tombstone with the death message where the player died, if that block is air. */
    private static void playerDeath(LivingDeathEvent event) {
        if (!TOMBSTONE_ON_DEATH || !(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        if (level.isEmptyBlock(pos) && level.isInWorldBounds(pos)) {
            level.setBlockAndUpdate(pos, TOMBSTONE.get().defaultBlockState());
            if (level.getBlockEntity(pos) instanceof TombstoneBlockEntity tombstone) {
                tombstone.setSignString(event.getSource().getLocalizedDeathMessage(player).getString());
            }
        }
    }

    /** cactusArmorDamage: a mob hitting a player takes ceil(0.5 per cactus armour piece) damage back. */
    private static void cactusArmorDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel level)
                || !(source.getDirectEntity() instanceof LivingEntity attacker) || !source.is(DamageTypes.MOB_ATTACK)) {
            return;
        }
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        double cactusDamage = 0.0;
        for (int i = 0; i < 4; i++) {
            if (player.getItemBySlot(slots[i]).is(CACTUS_ARMOR.items().get(i).get())) {
                cactusDamage += 0.5;
            }
        }
        if (cactusDamage > 0.0) {
            attacker.hurtServer(level, level.damageSources().mobAttack(player), Mth.ceil(cactusDamage));
        }
    }

    /** ItemAnkh.onItemRightClick: for half a heart a large fireball falls from the sky onto the player's position. */
    private static void useAnkh(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ZuluItems.ANKH.get())) {
            return;
        }
        Player player = event.getEntity();
        if (player.getHealth() > 1 && player.level() instanceof ServerLevel level) {
            shootFireball(level, player);
            player.setHealth(player.getHealth() - 1);
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void shootFireball(Level level, Player player) {
        RandomSource random = player.getRandom();
        double sourceX = player.getX() + (random.nextInt(10) - 5);
        double sourceY = level.getMaxY() + 1 - 15; // getActualHeight() - 15
        double sourceZ = player.getZ() + (random.nextInt(10) - 5);
        double dx = player.getX() - sourceX;
        double dy = player.getBoundingBox().minY + player.getBbHeight() / 2.0F - (sourceY + player.getBbHeight() / 2.0F);
        double dz = player.getZ() - sourceZ;
        level.levelEvent(null, LevelEvent.SOUND_GHAST_FIREBALL, player.blockPosition(), 0);
        LargeFireball fireball = new LargeFireball(level, player, new Vec3(dx, dy, dz), 1);
        Vec3 look = player.getViewVector(1.0F);
        fireball.setPos(sourceX + look.x, sourceY + player.getBbHeight() / 2.0F + 0.5, sourceZ + look.z);
        level.addFreshEntity(fireball);
    }

    /** dropRareDrop of the creatures: their head, (5 + looting) / 200 when killed by a player, not as a baby. */
    private static final Map<String, MobHead> HEAD_DROPS = Map.ofEntries(
            Map.entry("red_finch", MobHead.RED_FINCH), Map.entry("green_finch", MobHead.RED_FINCH), Map.entry("blue_finch", MobHead.RED_FINCH),
            Map.entry("alligator", MobHead.ALLIGATOR), Map.entry("armadillo", MobHead.ARMADILLO), Map.entry("black_bear", MobHead.BLACK_BEAR),
            Map.entry("brown_bear", MobHead.BROWN_BEAR), Map.entry("polar_bear", MobHead.POLAR_BEAR), Map.entry("beaver", MobHead.BEAVER),
            Map.entry("boar", MobHead.BOAR), Map.entry("giraffe", MobHead.GIRAFFE), Map.entry("gorilla", MobHead.GORILLA),
            Map.entry("lizard", MobHead.LIZARD), Map.entry("mammoth", MobHead.MAMMOTH), Map.entry("ostrich", MobHead.OSTRICH),
            Map.entry("penguin", MobHead.PENGUIN), Map.entry("rhino", MobHead.RHINO), Map.entry("tree_ent", MobHead.TREE_ENT),
            Map.entry("vulture", MobHead.VULTURE), Map.entry("elephant", MobHead.ELEPHANT));

    private static void addHeadDrops(LootTableLoadEvent event) {
        Identifier name = event.getName();
        if (!name.getNamespace().equals(ProjectZulu.MOD_ID) || !name.getPath().startsWith("entities/")) {
            return;
        }
        MobHead head = HEAD_DROPS.get(name.getPath().substring("entities/".length()));
        if (head == null) {
            return;
        }
        event.getTable().addPool(LootPool.lootPool()
                .name(ProjectZulu.MOD_ID + ":mob_head")
                .add(LootItem.lootTableItem(MOB_HEADS.get(head).get()))
                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setIsBaby(false))))
                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(
                        event.getRegistries().lookupOrThrow(Registries.ENCHANTMENT), 0.025F, 0.005F))
                .build());
    }

    /**
     * onPlayerUpdateStarve: in the desert under open sky by day a player loses saturation (by difficulty), a little
     * less without full armour. (OnPlayerUpdateFreeze is not ported: EntityPlayer.onLivingUpdate set
     * landMovementFactor again right after the event, so it never had an effect.)
     */
    private static void desertSun(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level) || player.isCreative() || player.isSpectator()) {
            return;
        }
        int x = Mth.floor(player.getX());
        int y = Mth.floor(player.getBoundingBox().minY);
        int z = Mth.floor(player.getZ());
        BlockPos pos = new BlockPos(x, y, z);
        Holder<Biome> biome = level.getBiome(pos);
        boolean desert = biome.is(Identifier.parse("worldgen152:desert")) || biome.is(Identifier.parse("worldgen152:desert_hills"))
                || biome.is(Identifier.parse("minecraft:desert"));
        if (!desert || !level.canSeeSky(pos) || !level.isBrightOutside()) {
            return;
        }
        float exhaustion = switch (level.getDifficulty().getId()) {
            case 0 -> 0.0F;
            case 2 -> 0.0064F;
            case 3 -> 0.0095999995F;
            default -> 0.0032F;
        };
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            if (player.getItemBySlot(slot).isEmpty()) {
                exhaustion = (float) (exhaustion - (exhaustion - exhaustion * 0.4) / 4.0);
                break;
            }
        }
        player.causeFoodExhaustion(Math.max(exhaustion, 0.0F));
    }
}
