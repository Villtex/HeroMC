package betterdungeons;

import betterdungeons.block.BDSpawnerBlock;
import betterdungeons.block.BDSpawnerBlockEntity;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Better Dungeons 1.09 (Chocolate Quest) for NeoForge. Part A (Teil 12): the building engine, the templates of the
 * pack (Chocolate/Building, imported at build time) and the placement of the dungeons in the 1.5.2 world; the
 * template dungeons (castles, snow castles, ships) are built. The generated dungeon types, the mobs, bosses and
 * items follow in part B (Teil 13).
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

    public BetterDungeons(IEventBus modBus) {
        BDWorldGen.register();
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        OldBlockStates.register(BuilderHelper.SPAWNER_BLOCK, () -> SPAWNER.get().defaultBlockState());
        OldModData.registerTile(BDSpawnerTile.class, (tile, blockEntity, registries) -> {
            if (blockEntity instanceof BDSpawnerBlockEntity spawner) {
                spawner.set(tile.mob(), tile.metadata());
            }
        });
        // Until the mobs of the mod are ported (Teil 13), its vanilla spawners make vanilla mobs of a similar kind.
        String[][] placeholders = {{"Pirate", "zombie"}, {"Dwarf", "zombie"}, {"Goblin", "zombie"}, {"SnowGoblin", "zombie"},
                {"ArmoredEnderman", "enderman"}, {"triton", "drowned"}, {"OgreArmored", "zombie"}, {"minotaurArmored", "zombie"},
                {"GremlinArmored", "zombie"}};
        for (String[] mob : placeholders) {
            OldGameIds.registerEntity("BetterDungeons." + mob[0], "minecraft:" + mob[1]);
        }
    }
}
