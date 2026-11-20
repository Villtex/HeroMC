package projectzulu;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import hexxit.worldgen152.OldModData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import projectzulu.block.gear.TombstoneBlockEntity;
import projectzulu.entity.HauntedArmorEntity;
import projectzulu.entity.MimicEntity;
import projectzulu.gen.ZuluIds;
import projectzulu.gen.ZuluStructureContent;
import projectzulu.world.LimitedMobSpawnerBlock;
import projectzulu.world.LimitedMobSpawnerBlockEntity;

/**
 * Teil 11, batch World: the limited mob spawner of Project Zulu|Dungeon, and what the structures of
 * {@code projectzulu.gen.ZuluWorldGen} need in the game (the mobs of their spawners and dead ends, the tombstone text).
 */
public final class ZuluWorld {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ProjectZulu.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectZulu.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,
            ProjectZulu.MOD_ID);

    /** BlockLimitedMobSpawner: Material.rock, hardness 0.5, metal sound, not opaque, drops nothing. */
    public static final DeferredBlock<LimitedMobSpawnerBlock> LIMITED_MOB_SPAWNER = BLOCKS.registerBlock("limited_mob_spawner",
            LimitedMobSpawnerBlock::new,
            p -> p.mapColor(MapColor.STONE).strength(0.5F).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.METAL).noLootTable());
    public static final DeferredItem<BlockItem> LIMITED_MOB_SPAWNER_ITEM = ITEMS.registerSimpleBlockItem(LIMITED_MOB_SPAWNER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LimitedMobSpawnerBlockEntity>> LIMITED_MOB_SPAWNER_ENTITY =
            BLOCK_ENTITIES.register("limited_mob_spawner", () -> new BlockEntityType<>(LimitedMobSpawnerBlockEntity::new, LIMITED_MOB_SPAWNER.get()));

    private ZuluWorld() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        OldBlockStates.register(ZuluIds.LIMITED_MOB_SPAWNER, () -> LIMITED_MOB_SPAWNER.get().defaultBlockState());
        OldGameIds.registerItem(ZuluIds.LIMITED_MOB_SPAWNER, ProjectZulu.MOD_ID + ":limited_mob_spawner");
        registerStructureContent();
    }

    private static void registerStructureContent() {
        String ns = ProjectZulu.MOD_ID + ":";
        // spawners of the labyrinth (EntityList names of 1.5.2: mod id + "." + mob name)
        OldGameIds.registerEntity(ZuluStructureContent.SPAWNER_MINOTAUR, ns + "minotaur");
        OldGameIds.registerEntity(ZuluStructureContent.SPAWNER_HAUNTED_ARMOR, ns + "haunted_armor");
        // the mobs the structures put into the world, with what their constructor and the builder did
        OldGameIds.registerEntity(ZuluStructureContent.MIMIC, ns + "mimic", entity -> {
            if (entity instanceof MimicEntity mimic) {
                mimic.setHovering();
            }
        });
        OldGameIds.registerEntity(ZuluStructureContent.HAUNTED_ARMOR, ns + "haunted_armor", entity -> {
            if (entity instanceof HauntedArmorEntity armor) {
                armor.setHovering();
                armor.setRandomArmor(armor.getRandom());
                armor.setPersistenceRequired();
            }
        });
        OldModData.registerTile(ZuluStructureContent.Tombstone.class, (tombstone, blockEntity, registries) -> {
            if (blockEntity instanceof TombstoneBlockEntity entity) {
                for (int line = 0; line < TombstoneBlockEntity.LINES && line < tombstone.lines().length; line++) {
                    entity.setLine(line, tombstone.lines()[line]);
                }
            }
        });
    }
}
