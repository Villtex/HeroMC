package legendgear;

import legendgear.block.BombFlowerBlock;
import legendgear.block.CaltropsBlock;
import legendgear.block.ChainedBlock;
import legendgear.block.ClayJarBlock;
import legendgear.block.JarBlockEntity;
import legendgear.block.MysticShrubBlock;
import legendgear.block.PedestalBlockEntity;
import legendgear.block.SkybeamBlock;
import legendgear.block.SkybeamBlockEntity;
import legendgear.block.StarbeamTorchBlock;
import legendgear.block.StarstoneBlock;
import legendgear.block.StarstoneBlockEntity;
import legendgear.block.SugarCubeBlock;
import legendgear.block.SwordPedestalBlock;
import legendgear.block.SwordPedestalTechnicalBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The blocks of LegendGear.load with the ids of the pack's LegendGear.cfg (1176 .. 1187, MagicIce 1181 was never made). */
public final class LGBlocks {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LegendGear.MOD_ID);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LegendGear.MOD_ID);

    public static final int BOMB_FLOWER_ID = 1176;
    public static final int CALTROPS_ID = 1177;
    public static final int CHAINED_ID = 1178;
    public static final int CLAY_JAR_ID = 1179;
    public static final int GRIND_NODE_ID = 1180;
    public static final int MYSTIC_SHRUB_ID = 1182;
    public static final int SKYBEAM_ID = 1183;
    public static final int STARSTONE_ID = 1184;
    public static final int SUGAR_CUBE_ID = 1185;
    public static final int SWORD_PEDESTAL_ID = 1186;
    public static final int SWORD_PEDESTAL_TECHNICAL_ID = 1187;

    /** MysticShrub(Material.plants), grass sound, light by stage. */
    public static final DeferredBlock<MysticShrubBlock> MYSTIC_SHRUB = BLOCKS.registerBlock("mystic_shrub", MysticShrubBlock::new,
            p -> p.mapColor(MapColor.PLANT).sound(SoundType.GRASS).instabreak().noCollision().noOcclusion().noLootTable()
                    .pushReaction(PushReaction.POPPED).lightLevel(MysticShrubBlock::light).offsetType(BlockBehaviour.OffsetType.NONE));
    /** BlockBombFlower(Material.plants). */
    public static final DeferredBlock<BombFlowerBlock> BOMB_FLOWER = BLOCKS.registerBlock("bomb_flower", BombFlowerBlock::new,
            p -> p.mapColor(MapColor.PLANT).sound(SoundType.GRASS).instabreak().noCollision().noOcclusion().noLootTable()
                    .pushReaction(PushReaction.POPPED));
    /** BlockSugarCube(Material.cake): hardness 0.25, gravel sound, drops 9 sugar. */
    public static final DeferredBlock<SugarCubeBlock> SUGAR_CUBE = BLOCKS.registerBlock("sugar_cube", SugarCubeBlock::new,
            p -> p.mapColor(MapColor.SNOW).strength(0.25F).sound(SoundType.GRAVEL));
    /** BlockJar(Material.glass): hardness 0, glass sound, drops nothing. */
    public static final DeferredBlock<ClayJarBlock> CLAY_JAR = BLOCKS.registerBlock("clay_jar", ClayJarBlock::new,
            p -> p.mapColor(MapColor.TERRACOTTA_ORANGE).instabreak().sound(SoundType.GLASS).noOcclusion().noLootTable());
    /** BlockCaltrops(Material.circuits): hardness 0.25. */
    public static final DeferredBlock<CaltropsBlock> CALTROPS = BLOCKS.registerBlock("caltrops", CaltropsBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(0.25F).sound(SoundType.METAL).noCollision().noOcclusion().pushReaction(PushReaction.POPPED));
    /** BlockSwordPedestal(Material.rock): hardness 4. */
    public static final DeferredBlock<SwordPedestalBlock> SWORD_PEDESTAL = BLOCKS.registerBlock("sword_pedestal", SwordPedestalBlock::new,
            p -> p.mapColor(MapColor.STONE).strength(4.0F).sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion());
    public static final DeferredBlock<SwordPedestalTechnicalBlock> PEDESTAL_TECHNICAL = BLOCKS.registerBlock("sword_pedestal_technical",
            SwordPedestalTechnicalBlock::new, p -> p.mapColor(MapColor.NONE).strength(4.0F).sound(SoundType.METAL).noOcclusion().noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE));
    /** BlockChained(Material.wood): unbreakable, resistance 6000000. */
    public static final DeferredBlock<ChainedBlock> CHAINED = BLOCKS.registerBlock("chained_block", ChainedBlock::new,
            p -> p.mapColor(MapColor.WOOD).strength(-1.0F, 6000000.0F).sound(SoundType.WOOD).noLootTable().pushReaction(PushReaction.IMMOVEABLE));
    /** BlockSkybeam(Material.rock): hardness 50, resistance 2000. */
    public static final DeferredBlock<SkybeamBlock> SKYBEAM = BLOCKS.registerBlock("skybeam", SkybeamBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(50.0F, 2000.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
    /** BlockStarstone(Material.iron): hardness 1.5, light 15. */
    public static final DeferredBlock<StarstoneBlock> STARSTONE = BLOCKS.registerBlock("starstone", StarstoneBlock::new,
            p -> p.mapColor(MapColor.GOLD).strength(1.5F).sound(SoundType.METAL).lightLevel(s -> 15));
    /** BlockGrindNode (a BlockTorch, light 15). */
    public static final DeferredBlock<StarbeamTorchBlock.Standing> STARBEAM_TORCH = BLOCKS.registerBlock("starbeam_torch",
            StarbeamTorchBlock.Standing::new, p -> p.noCollision().instabreak().lightLevel(s -> 15).sound(SoundType.WOOD)
                    .pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<StarbeamTorchBlock.Wall> STARBEAM_WALL_TORCH = BLOCKS.registerBlock("starbeam_wall_torch",
            StarbeamTorchBlock.Wall::new, p -> p.noCollision().instabreak().lightLevel(s -> 15).sound(SoundType.WOOD)
                    .pushReaction(PushReaction.POPPED).overrideLootTable(STARBEAM_TORCH.get().getLootTable()).overrideDescription("block.legendgear.starbeam_torch"));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JarBlockEntity>> JAR_ENTITY = BLOCK_ENTITIES.register("clay_jar",
            () -> new BlockEntityType<>(JarBlockEntity::new, CLAY_JAR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PedestalBlockEntity>> PEDESTAL_ENTITY = BLOCK_ENTITIES.register(
            "sword_pedestal", () -> new BlockEntityType<>(PedestalBlockEntity::new, SWORD_PEDESTAL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SkybeamBlockEntity>> SKYBEAM_ENTITY = BLOCK_ENTITIES.register(
            "skybeam", () -> new BlockEntityType<>(SkybeamBlockEntity::new, SKYBEAM.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StarstoneBlockEntity>> STARSTONE_ENTITY = BLOCK_ENTITIES.register(
            "starstone", () -> new BlockEntityType<>(StarstoneBlockEntity::new, STARSTONE.get()));

    private LGBlocks() {
    }

    static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
