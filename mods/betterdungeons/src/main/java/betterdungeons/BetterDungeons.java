package betterdungeons;

import betterdungeons.api.BDEntityIds;
import betterdungeons.block.BDSpawnerBlock;
import betterdungeons.block.BDSpawnerBlockEntity;
import betterdungeons.block.CapsuleStationBlock;
import betterdungeons.block.CapsuleStationBlockEntity;
import betterdungeons.gen.BDSpawnerTile;
import betterdungeons.gen.BDWorldGen;
import betterdungeons.gen.BuilderHelper;
import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import hexxit.worldgen152.OldModData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Better Dungeons 1.09 (Chocolate Quest) for NeoForge. Teil 12: the building engine, the templates of the pack
 * (Chocolate/Building, imported at build time) and the placement of the dungeons in the 1.5.2 world. Teil 13: the
 * items ({@link BDItems}), projectiles ({@link BDEntities}), sounds, the spawner's behaviour and the capsule stations;
 * the mobs and bosses come in their own classes and plug in through {@link betterdungeons.api}.
 */
@Mod(BetterDungeons.MOD_ID)
public final class BetterDungeons {
    public static final String MOD_ID = "betterdungeons";

    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

    /** BlockMobSpawner of the mod (blockID + 1 = 2002 in the pack): Material.rock, no hardness set, drops nothing. */
    public static final DeferredBlock<BDSpawnerBlock> SPAWNER = BLOCKS.registerBlock("spawner", BDSpawnerBlock::new,
            p -> p.mapColor(MapColor.STONE).strength(0.0F).sound(SoundType.STONE).noOcclusion().noLootTable());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BDSpawnerBlockEntity>> SPAWNER_ENTITY =
            BLOCK_ENTITIES.register("spawner", () -> new BlockEntityType<>(BDSpawnerBlockEntity::new, SPAWNER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CapsuleStationBlockEntity>> CAPSULE_STATION_ENTITY =
            BLOCK_ENTITIES.register("capsule_station", () -> new BlockEntityType<>(CapsuleStationBlockEntity::new,
                    BDItems.STATION_BLOCKS.stream().map(b -> (CapsuleStationBlock) b.get()).toArray(CapsuleStationBlock[]::new)));

    public BetterDungeons(IEventBus modBus) {
        BDWorldGen.register();
        BDItems.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        BDEntities.register(modBus);
        BDSounds.register(modBus);
        NeoForge.EVENT_BUS.register(BDEvents.class);
        OldBlockStates.register(BuilderHelper.SPAWNER_BLOCK, () -> SPAWNER.get().defaultBlockState());
        OldModData.registerTile(BDSpawnerTile.class, (tile, blockEntity, registries) -> {
            if (blockEntity instanceof BDSpawnerBlockEntity spawner) {
                spawner.set(tile.mob(), tile.metadata());
            }
        });
        // Until the mobs of the mod are ported, its vanilla spawners make vanilla mobs of a similar kind; at common setup
        // every name whose type is registered by then points to it (BDEntityIds.registerOldNames).
        String[][] placeholders = {{"Pirate", "zombie"}, {"Dwarf", "zombie"}, {"Goblin", "zombie"}, {"SnowGoblin", "zombie"},
                {"ArmoredEnderman", "enderman"}, {"triton", "drowned"}, {"OgreArmored", "zombie"}, {"minotaurArmored", "zombie"},
                {"GremlinArmored", "zombie"}};
        for (String[] mob : placeholders) {
            OldGameIds.registerEntity("BetterDungeons." + mob[0], "minecraft:" + mob[1]);
        }
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(BDEntityIds::registerOldNames));
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            betterdungeons.client.BDClient.register(modBus);
        }
    }
}
