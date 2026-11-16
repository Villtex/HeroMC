package harkenscythe;

import harkenscythe.altar.AltarBlock;
import harkenscythe.altar.AltarBlockEntity;
import harkenscythe.block.BiomassBlock;
import harkenscythe.block.BiomassPlantBlock;
import harkenscythe.block.BloodSplashBlock;
import harkenscythe.block.ClothBlock;
import harkenscythe.block.CreepBlock;
import harkenscythe.block.CrucibleBlock;
import harkenscythe.block.HSSkullBlock;
import harkenscythe.block.HSSkullBlockEntity;
import harkenscythe.block.LivingmetalBlock;
import harkenscythe.block.SoulLightBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The blocks of mod_HarkenScythe. Hardness as in 1.5.2; setResistance(r) of 1.5.2 is
 * {@code 0.6 * r} today. The step sounds "powder" and "metal" of 1.5.2 were the stone sound.
 */
public final class HSBlocks {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HarkenScythe.MOD_ID);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HarkenScythe.MOD_ID);

    /** Material.iron: needs a pickaxe. */
    public static final DeferredBlock<CrucibleBlock> SOUL_CRUCIBLE = BLOCKS.registerBlock("soul_crucible", p -> new CrucibleBlock(false, p),
            p -> p.mapColor(MapColor.METAL).strength(2.0F).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.METAL));
    public static final DeferredBlock<CrucibleBlock> BLOOD_CRUCIBLE = BLOCKS.registerBlock("blood_crucible", p -> new CrucibleBlock(true, p),
            p -> p.mapColor(MapColor.METAL).strength(2.0F).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.METAL));
    /** Material.circuits, light 0.5, no collision, invisible. */
    public static final DeferredBlock<SoulLightBlock> SOUL_LIGHT = BLOCKS.registerBlock("soul_light", SoulLightBlock::new,
            p -> p.strength(0.05F).sound(SoundType.STONE).lightLevel(state -> 7).noCollision().noOcclusion().replaceable().randomTicks()
                    .noLootTable().pushReaction(PushReaction.POPPED));
    /** Material.clay, a layer 0.01 high without collision. */
    public static final DeferredBlock<BloodSplashBlock> BLOOD_SPLASH_RESIDUE = BLOCKS.registerBlock("blood_splash_residue", BloodSplashBlock::new,
            p -> p.mapColor(MapColor.COLOR_RED).strength(0.05F).sound(SoundType.STONE).noCollision().noOcclusion().randomTicks().noLootTable()
                    .pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<HSSkullBlock> SKULL = BLOCKS.registerBlock("skull", HSSkullBlock::new,
            p -> p.strength(1.0F).sound(SoundType.STONE).noOcclusion().instrument(NoteBlockInstrument.SKELETON).pushReaction(PushReaction.POPPED));
    /** Material.cloth; the spectral wool renders translucent. */
    public static final DeferredBlock<ClothBlock> SOULWEAVE_CLOTH = cloth("soulweave_cloth", MapColor.COLOR_LIGHT_BLUE);
    public static final DeferredBlock<ClothBlock> BLOODWEAVE_CLOTH = cloth("bloodweave_cloth", MapColor.COLOR_RED);
    public static final DeferredBlock<ClothBlock> SPECTRAL_WOOL = cloth("spectral_wool", MapColor.SNOW);
    /** Material.iron; a beacon base; heals living things nearby. */
    public static final DeferredBlock<LivingmetalBlock> LIVINGMETAL_BLOCK = BLOCKS.registerBlock("livingmetal_block", LivingmetalBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).randomTicks());
    /** Material.pumpkin; a beacon base; grows blood on top. */
    public static final DeferredBlock<BiomassBlock> BIOMASS_BLOCK = BLOCKS.registerBlock("biomass_block", BiomassBlock::new,
            p -> p.mapColor(MapColor.COLOR_RED).strength(5.0F, 3.0F).sound(SoundType.STONE).randomTicks());
    /** Material.sand: the soul soil of the biomass plant. */
    public static final DeferredBlock<CreepBlock> CREEP_BLOCK = BLOCKS.registerBlock("creep_block", CreepBlock::new,
            p -> p.mapColor(MapColor.COLOR_BROWN).strength(0.6F).sound(SoundType.GRASS).speedFactor(0.4F).randomTicks());
    public static final DeferredBlock<BiomassPlantBlock> BIOMASS_PLANT = BLOCKS.registerBlock("biomass_plant", BiomassPlantBlock::new,
            p -> p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).randomTicks().pushReaction(PushReaction.POPPED));

    /** Material.rock (needs a pickaxe), hardness 5, resistance 10 (6 today), the metal step sound; see {@link AltarBlock}. */
    public static final DeferredBlock<AltarBlock> ALTAR_OF_SOULS = BLOCKS.registerBlock("altar_of_souls", p -> new AltarBlock(false, p),
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<AltarBlock> ALTAR_OF_BLOOD = BLOCKS.registerBlock("altar_of_blood", p -> new AltarBlock(true, p),
            p -> p.mapColor(MapColor.NETHER).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarBlockEntity>> ALTAR_ENTITY = BLOCK_ENTITIES.register("altar",
            () -> new BlockEntityType<>(AltarBlockEntity::new, ALTAR_OF_SOULS.get(), ALTAR_OF_BLOOD.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HSSkullBlockEntity>> SKULL_ENTITY = BLOCK_ENTITIES.register("skull",
            () -> new BlockEntityType<>(HSSkullBlockEntity::new, SKULL.get()));

    private HSBlocks() {
    }

    private static DeferredBlock<ClothBlock> cloth(String name, MapColor color) {
        return BLOCKS.registerBlock(name, ClothBlock::new,
                p -> p.mapColor(color).strength(0.8F).sound(SoundType.WOOL).instrument(NoteBlockInstrument.GUITAR).ignitedByLava().noOcclusion());
    }
}
