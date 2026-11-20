package secretrooms;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import secretrooms.block.CamoBlockEntity;
import secretrooms.block.CamoButtonBlock;
import secretrooms.block.CamoChestBlock;
import secretrooms.block.CamoChestBlockEntity;
import secretrooms.block.CamoDoorBlock;
import secretrooms.block.CamoDummyBlock;
import secretrooms.block.CamoFullBlock;
import secretrooms.block.CamoGateBlock;
import secretrooms.block.CamoLeverBlock;
import secretrooms.block.CamoLightDetectorBlock;
import secretrooms.block.CamoPlateBlock;
import secretrooms.block.CamoStairBlock;
import secretrooms.block.CamoTrapDoorBlock;
import secretrooms.block.CamoWireBlock;
import secretrooms.block.GhostBlock;
import secretrooms.block.OneWayGlassBlock;
import secretrooms.block.SolidAirBlock;
import secretrooms.block.TorchLeverBlock;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * SecretRooms.load with the ids of the pack's secretroomsmod.cfg. Camo blocks were Material.wood (wood sound, light
 * opacity 255) unless noted; levers, buttons, redstone and plates were Material.circuits.
 */
public final class SRBlocks {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SecretRooms.MOD_ID);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SecretRooms.MOD_ID);

    private static BlockBehaviour.Properties camo(BlockBehaviour.Properties p, float hardness) {
        return p.mapColor(MapColor.WOOD).strength(hardness).sound(SoundType.WOOD);
    }

    private static UnaryOperator<BlockBehaviour.Properties> camo(float hardness) {
        return p -> camo(p, hardness);
    }

    public static final DeferredBlock<TorchLeverBlock.Standing> TORCH_LEVER = BLOCKS.registerBlock("torch_lever", TorchLeverBlock.Standing::new,
            p -> p.noCollision().instabreak().lightLevel(s -> 14).sound(SoundType.WOOD).pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<TorchLeverBlock.Wall> WALL_TORCH_LEVER = BLOCKS.registerBlock("wall_torch_lever", TorchLeverBlock.Wall::new,
            p -> p.noCollision().instabreak().lightLevel(s -> 14).sound(SoundType.WOOD).pushReaction(PushReaction.POPPED)
                    .overrideLootTable(TORCH_LEVER.get().getLootTable()).overrideDescription("block.secretroomsmod.torch_lever"));
    /** BlockOneWay: hardness 1, light passes. */
    public static final DeferredBlock<OneWayGlassBlock> ONE_WAY_GLASS = BLOCKS.registerBlock("one_way_glass", OneWayGlassBlock::new,
            p -> camo(p, 1.0F).noOcclusion().isViewBlocking((s, l, pos, box) -> false).isSuffocating((s, l, pos) -> false));
    public static final DeferredBlock<CamoGateBlock> CAMO_GATE = BLOCKS.registerBlock("camo_gate", CamoGateBlock::new, camo(1.5F));
    public static final DeferredBlock<CamoDummyBlock> CAMO_DUMMY = BLOCKS.registerBlock("camo_dummy", CamoDummyBlock::new,
            p -> camo(p, 2.5F).noLootTable().pushReaction(PushReaction.IMMOVEABLE));
    /** BlockCamoTrapDoor: hardness 3, wood. */
    public static final DeferredBlock<CamoTrapDoorBlock> SECRET_TRAPDOOR = BLOCKS.registerBlock("secret_trapdoor",
            p -> new CamoTrapDoorBlock(BlockSetType.OAK, p), p -> camo(p, 3.0F).noOcclusion());
    public static final DeferredBlock<CamoDoorBlock> SECRET_WOODEN_DOOR = BLOCKS.registerBlock("secret_wooden_door",
            p -> new CamoDoorBlock(BlockSetType.OAK, p), p -> camo(p, 3.0F).noOcclusion().pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<CamoDoorBlock> SECRET_IRON_DOOR = BLOCKS.registerBlock("secret_iron_door",
            p -> new CamoDoorBlock(BlockSetType.IRON, p), p -> p.mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL).noOcclusion()
                    .pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<GhostBlock> GHOST_BLOCK = BLOCKS.registerBlock("ghost_block", GhostBlock::new, camo(1.5F));
    public static final DeferredBlock<CamoLeverBlock> SECRET_LEVER = BLOCKS.registerBlock("secret_lever", CamoLeverBlock::new, camo(1.5F));
    public static final DeferredBlock<CamoWireBlock> SECRET_REDSTONE = BLOCKS.registerBlock("secret_redstone", CamoWireBlock::new, camo(0.0F));
    public static final DeferredBlock<CamoButtonBlock> SECRET_STONE_BUTTON = BLOCKS.registerBlock("secret_stone_button",
            p -> new CamoButtonBlock(p, false), camo(0.0F));
    public static final DeferredBlock<CamoButtonBlock> SECRET_WOOD_BUTTON = BLOCKS.registerBlock("secret_wood_button",
            p -> new CamoButtonBlock(p, true), camo(0.0F));
    public static final DeferredBlock<CamoPlateBlock> SECRET_PRESSURE_PLATE = BLOCKS.registerBlock("secret_pressure_plate",
            p -> new CamoPlateBlock(p, CamoPlateBlock.Sense.ALL, 0), p -> camo(p, 0.5F).pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<CamoPlateBlock> SECRET_PLAYER_PLATE = BLOCKS.registerBlock("secret_player_plate",
            p -> new CamoPlateBlock(p, CamoPlateBlock.Sense.PLAYERS, 0), p -> camo(p, 0.5F).pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<CamoPlateBlock> SECRET_LIGHT_PLATE = BLOCKS.registerBlock("secret_light_plate",
            p -> new CamoPlateBlock(p, CamoPlateBlock.Sense.ITEMS, 64), p -> camo(p, 0.5F).pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<CamoPlateBlock> SECRET_HEAVY_PLATE = BLOCKS.registerBlock("secret_heavy_plate",
            p -> new CamoPlateBlock(p, CamoPlateBlock.Sense.ITEMS, 640), p -> camo(p, 0.5F).pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<CamoStairBlock> SECRET_STAIRS = BLOCKS.registerBlock("secret_stairs", CamoStairBlock::new, camo(1.5F));
    public static final DeferredBlock<CamoChestBlock> SECRET_CHEST = BLOCKS.registerBlock("secret_chest", p -> new CamoChestBlock(p, false), camo(1.5F));
    public static final DeferredBlock<CamoChestBlock> SECRET_TRAPPED_CHEST = BLOCKS.registerBlock("secret_trapped_chest",
            p -> new CamoChestBlock(p, true), camo(1.5F));
    public static final DeferredBlock<CamoLightDetectorBlock> SECRET_LIGHT_DETECTOR = BLOCKS.registerBlock("secret_light_detector",
            CamoLightDetectorBlock::new, camo(0.0F));
    /** BlockSolidAir (MaterialFakeAir): invisible, solid, drops nothing. */
    public static final DeferredBlock<SolidAirBlock> SOLID_AIR = BLOCKS.registerBlock("solid_air", SolidAirBlock::new,
            p -> p.mapColor(MapColor.NONE).noOcclusion().noLootTable().isViewBlocking((s, l, pos, box) -> false).isSuffocating((s, l, pos) -> false)
                    .isRedstoneConductor((s, l, pos) -> true).pushReaction(PushReaction.IMMOVEABLE));

    /** Every block with a camo block entity (the client wraps their models). */
    public static List<DeferredBlock<? extends CamoFullBlock>> fullCamo() {
        return List.of(CAMO_GATE, CAMO_DUMMY, GHOST_BLOCK, SECRET_LEVER, SECRET_REDSTONE, SECRET_STONE_BUTTON, SECRET_WOOD_BUTTON, SECRET_PRESSURE_PLATE,
                SECRET_PLAYER_PLATE, SECRET_LIGHT_PLATE, SECRET_HEAVY_PLATE, SECRET_STAIRS, SECRET_CHEST, SECRET_TRAPPED_CHEST, SECRET_LIGHT_DETECTOR);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CamoBlockEntity>> CAMO_ENTITY = BLOCK_ENTITIES.register("camo",
            () -> new BlockEntityType<>(CamoBlockEntity::new, CAMO_GATE.get(), CAMO_DUMMY.get(), GHOST_BLOCK.get(), SECRET_LEVER.get(),
                    SECRET_REDSTONE.get(), SECRET_STONE_BUTTON.get(), SECRET_WOOD_BUTTON.get(), SECRET_PRESSURE_PLATE.get(), SECRET_PLAYER_PLATE.get(),
                    SECRET_LIGHT_PLATE.get(), SECRET_HEAVY_PLATE.get(), SECRET_STAIRS.get(), ONE_WAY_GLASS.get(), SECRET_WOODEN_DOOR.get(),
                    SECRET_IRON_DOOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CamoChestBlockEntity>> CAMO_CHEST_ENTITY = BLOCK_ENTITIES.register(
            "camo_chest", () -> new BlockEntityType<>(CamoChestBlockEntity::new, SECRET_CHEST.get(), SECRET_TRAPPED_CHEST.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CamoBlockEntity>> CAMO_DETECTOR_ENTITY = BLOCK_ENTITIES.register(
            "camo_detector", () -> new BlockEntityType<>((pos, state) -> new CamoBlockEntity(SRBlocks.CAMO_DETECTOR_ENTITY.get(), pos, state),
                    SECRET_LIGHT_DETECTOR.get()));

    private SRBlocks() {
    }

    static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
