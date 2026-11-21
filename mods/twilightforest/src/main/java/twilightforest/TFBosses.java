package twilightforest;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import twilightforest.block.BossSpawnerBlockEntity;
import twilightforest.block.TrophyBlock;
import twilightforest.block.TrophyBlockEntity;
import twilightforest.entity.boss.Lich;
import twilightforest.entity.boss.LichBolt;
import twilightforest.entity.boss.LichBomb;
import twilightforest.entity.boss.LichMinion;
import twilightforest.entity.boss.Minoshroom;
import twilightforest.entity.boss.Naga;
import twilightforest.entity.boss.UrGhast;
import twilightforest.gen.TFTreasure;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Teil 24f: the bosses (naga, lich with its minions, bolts and bombs, minoshroom, ur-ghast; the hydra is in
 * {@link TFHydra}), their spawn eggs and sounds, the boss spawner block entity and the trophies (BlockTFTrophy 1373,
 * ItemTFTrophy 7274 with damage 0 hydra, 1 naga, 2 lich, 3 ur-ghast).
 */
public final class TFBosses {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(TwilightForest.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TwilightForest.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TwilightForest.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TwilightForest.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TwilightForest.MOD_ID);
    public static final List<DeferredItem<? extends Item>> TAB_ITEMS = new ArrayList<>();
    private static final List<Object[]> OLD = new ArrayList<>();
    private static final List<Object[]> OLD_EGGS = new ArrayList<>();
    public static final int ID_TROPHY_BLOCK = 1373;
    public static final int ID_TROPHY = 7274 + 256;

    private TFBosses() {
    }

    // ---------------------------------------------------------------- entities (TFCreatures.registerTFCreature: 80, 3)
    public static final DeferredHolder<EntityType<?>, EntityType<Naga>> NAGA = mob("naga", 182, "Naga", Naga::new, MobCategory.MONSTER,
            b -> b.sized(1.75F, 3.0F).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<Lich>> LICH = mob("lich", 190, "Twilight Lich", Lich::new, MobCategory.MONSTER,
            b -> b.sized(1.1F, 2.5F).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<LichMinion>> LICH_MINION = mob("lich_minion", 192, "Lich Minion", LichMinion::new,
            MobCategory.MONSTER, b -> b.sized(0.6F, 1.95F).eyeHeight(1.74F));
    public static final DeferredHolder<EntityType<?>, EntityType<Minoshroom>> MINOSHROOM = mob("minoshroom", 205, "Minoshroom", Minoshroom::new,
            MobCategory.MONSTER, b -> b.sized(1.49F, 2.9F));
    public static final DeferredHolder<EntityType<?>, EntityType<UrGhast>> UR_GHAST = mob("ur_ghast", 217, "Tower Boss", UrGhast::new,
            MobCategory.MONSTER, b -> b.sized(14.0F, 18.0F).fireImmune().clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<LichBolt>> LICH_BOLT = TFEntities.projectile("lich_bolt", LichBolt::new, 2);
    public static final DeferredHolder<EntityType<?>, EntityType<LichBomb>> LICH_BOMB = TFEntities.projectile("lich_bomb", LichBomb::new, 3);

    public static final DeferredItem<Item> NAGA_SPAWN_EGG = egg("naga", 182, NAGA);
    public static final DeferredItem<Item> LICH_SPAWN_EGG = egg("lich", 190, LICH);
    public static final DeferredItem<Item> MINOSHROOM_SPAWN_EGG = egg("minoshroom", 205, MINOSHROOM);
    public static final DeferredItem<Item> UR_GHAST_SPAWN_EGG = egg("ur_ghast", 217, UR_GHAST);

    // ---------------------------------------------------------------- sounds (TFSounds: mob.tf.naga.*)
    public static final DeferredHolder<SoundEvent, SoundEvent> NAGA_HISS = sound("naga.hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> NAGA_RATTLE = sound("naga.rattle");
    public static final DeferredHolder<SoundEvent, SoundEvent> NAGA_HURT = sound("naga.hurt");

    // ---------------------------------------------------------------- trophies
    public static final Map<TrophyBlock.Kind, DeferredBlock<TrophyBlock>> TROPHIES = new EnumMap<>(TrophyBlock.Kind.class);
    public static final Map<TrophyBlock.Kind, DeferredBlock<TrophyBlock.Wall>> WALL_TROPHIES = new EnumMap<>(TrophyBlock.Kind.class);
    public static final Map<TrophyBlock.Kind, DeferredItem<Item>> TROPHY_ITEMS = new EnumMap<>(TrophyBlock.Kind.class);

    static {
        for (TrophyBlock.Kind kind : TrophyBlock.Kind.values()) {
            String name = kind.name().toLowerCase() + "_trophy";
            // Material.circuits: breaks at once, pushed out by pistons
            DeferredBlock<TrophyBlock> floor = BLOCKS.registerBlock(name, p -> new TrophyBlock(kind, p),
                    p -> p.strength(1.0F).sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.POPPED));
            DeferredBlock<TrophyBlock.Wall> wall = BLOCKS.registerBlock("wall_" + name, p -> new TrophyBlock.Wall(kind, p),
                    p -> p.strength(1.0F).sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.POPPED));
            DeferredItem<Item> item = ITEMS.registerItem(name, p -> new StandingAndWallBlockItem(floor.get(), wall.get(), Direction.DOWN, p),
                    p -> p.rarity(Rarity.RARE).useBlockDescriptionPrefix());
            TROPHIES.put(kind, floor);
            WALL_TROPHIES.put(kind, wall);
            TROPHY_ITEMS.put(kind, item);
            TAB_ITEMS.add(item);
        }
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrophyBlockEntity>> TROPHY_ENTITY = BLOCK_ENTITIES.register("trophy", () -> {
        Set<net.minecraft.world.level.block.Block> blocks = new HashSet<>();
        TROPHIES.values().forEach(b -> blocks.add(b.get()));
        WALL_TROPHIES.values().forEach(b -> blocks.add(b.get()));
        return new BlockEntityType<>(TrophyBlockEntity::new, blocks);
    });
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BossSpawnerBlockEntity>> BOSS_SPAWNER_ENTITY = BLOCK_ENTITIES.register(
            "boss_spawner", () -> new BlockEntityType<>(BossSpawnerBlockEntity::new,
                    Set.copyOf(TFBlocks.BOSS_SPAWNERS.stream().map(DeferredBlock::get).toList())));

    // ---------------------------------------------------------------- helpers

    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> mob(String name, int oldId, String oldName,
            EntityType.EntityFactory<E> factory, MobCategory category, UnaryOperator<EntityType.Builder<E>> builder) {
        OLD.add(new Object[]{oldId, oldName, TwilightForest.MOD_ID + ":" + name});
        return ENTITIES.registerEntityType(name, factory, category, b -> builder.apply(b.clientTrackingRange(5).updateInterval(3)));
    }

    private static DeferredItem<Item> egg(String name, int oldId, DeferredHolder<EntityType<?>, ? extends EntityType<?>> type) {
        DeferredItem<Item> egg = ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get()));
        TAB_ITEMS.add(egg);
        OLD_EGGS.add(new Object[]{oldId, egg});
        return egg;
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(TwilightForest.id(name)));
    }

    static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(TFBosses::attributes);
        // the ghast trap holds the ur-ghast (Teil 24e hook)
        twilightforest.block.GhastTrapBlockEntity.IS_UR_GHAST = ghast -> ghast instanceof UrGhast;
        twilightforest.block.GhastTrapBlockEntity.UR_GHAST_IN_TRAP = ghast -> ((UrGhast) ghast).stopTantrum();
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(NAGA.get(), Naga.createAttributes().build());
        event.put(LICH.get(), Lich.createAttributes().build());
        event.put(LICH_MINION.get(), Zombie.createAttributes().build());
        event.put(MINOSHROOM.get(), Minoshroom.createAttributes().build());
        event.put(UR_GHAST.get(), UrGhast.createAttributes().build());
    }

    static void registerOld() {
        for (Object[] old : OLD) {
            OldGameIds.registerEntity(TFTreasure.SPAWNER_PREFIX + old[1], (String) old[2]);
        }
        // the minoshroom of the labyrinth's boss room: setHomeArea(x, y, z, 7) where it stands (ComponentTFMazeRoomBoss)
        OldGameIds.registerEntity(twilightforest.gen.structure.ComponentTFMazeRoomBoss.MINOSHROOM, MINOSHROOM.getId().toString(),
                entity -> {
                    if (entity instanceof Minoshroom minoshroom) {
                        minoshroom.setHomeTo(minoshroom.blockPosition(), 7);
                        minoshroom.setPersistenceRequired();
                    }
                });
        OldGameIds.registerEntity(TFTreasure.SPAWNER_PREFIX + "Loyal Zombie", TFEntities.LOYAL_ZOMBIE.getId().toString());
        for (Object[] old : OLD_EGGS) {
            @SuppressWarnings("unchecked")
            DeferredItem<Item> egg = (DeferredItem<Item>) old[1];
            OldGameIds.registerItem(7309 + 256, (Integer) old[0], egg.getId().toString());
        }
        for (TrophyBlock.Kind kind : TrophyBlock.Kind.values()) {
            OldGameIds.registerItem(ID_TROPHY, kind.ordinal(), TROPHY_ITEMS.get(kind).getId().toString());
        }
        OldBlockStates.registerMeta(ID_TROPHY_BLOCK, meta -> (meta >= 2 && meta <= 5
                ? WALL_TROPHIES.get(TrophyBlock.Kind.NAGA).get().defaultBlockState().setValue(TrophyBlock.Wall.FACING, Direction.from3DDataValue(meta))
                : TROPHIES.get(TrophyBlock.Kind.NAGA).get().defaultBlockState()));
    }

    public static List<ItemStack> tabStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        TAB_ITEMS.forEach(item -> stacks.add(new ItemStack(item.get())));
        return stacks;
    }
}
