package myrathi.obsidiplates;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * Obsidian Pressure Plates 1.5.0.13: an obsidian plate that only players press and a mossy cobblestone plate that
 * only mobs press. Both come plain, silent (wool underneath, no click), shrouded (almost invisible) or both.
 * 1.5.2 kept the eight kinds as metadata of one block; here each is a block of its own.
 */
@Mod(ObsidiPlates.MOD_ID)
public final class ObsidiPlates {
    public static final String MOD_ID = "obsidiplates";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    /** Creative order of the original: plain, shrouded, silent, both. */
    public static final List<DeferredItem<BlockItem>> ITEMS_IN_ORDER = new ArrayList<>();

    /** Click of the stone plate ("random.click" in 1.5.2). */
    private static final BlockSetType LOUD = new BlockSetType("obsidiplates_loud", true, true, false,
            BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.STONE, SoundEvents.EMPTY, SoundEvents.EMPTY, SoundEvents.EMPTY,
            SoundEvents.EMPTY, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.EMPTY,
            SoundEvents.EMPTY);
    /** Silent plates (wool under them) make no sound. */
    private static final BlockSetType SILENT = new BlockSetType("obsidiplates_silent", true, true, false,
            BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.STONE, SoundEvents.EMPTY, SoundEvents.EMPTY, SoundEvents.EMPTY,
            SoundEvents.EMPTY, SoundEvents.EMPTY, SoundEvents.EMPTY, SoundEvents.EMPTY, SoundEvents.EMPTY);

    public static final DeferredBlock<PlayerMobPlateBlock> OBSIDIAN = plate("obsidian_pressure_plate", true, false, false);
    public static final DeferredBlock<PlayerMobPlateBlock> OBSIDIAN_SHROUDED = plate("obsidian_pressure_plate_shrouded", true, true, false);
    public static final DeferredBlock<PlayerMobPlateBlock> OBSIDIAN_SILENT = plate("obsidian_pressure_plate_silent", true, false, true);
    public static final DeferredBlock<PlayerMobPlateBlock> OBSIDIAN_BOTH = plate("obsidian_pressure_plate_shrouded_silent", true, true, true);
    public static final DeferredBlock<PlayerMobPlateBlock> MOSSY = plate("mossy_pressure_plate", false, false, false);
    public static final DeferredBlock<PlayerMobPlateBlock> MOSSY_SHROUDED = plate("mossy_pressure_plate_shrouded", false, true, false);
    public static final DeferredBlock<PlayerMobPlateBlock> MOSSY_SILENT = plate("mossy_pressure_plate_silent", false, false, true);
    public static final DeferredBlock<PlayerMobPlateBlock> MOSSY_BOTH = plate("mossy_pressure_plate_shrouded_silent", false, true, true);

    /**
     * Hardness = hardness of the base block x 0.25 (obsidian, 12.5) or x 1.0 (mossy cobblestone, 2.0); explosion
     * resistance of the base block. Material.rock: a pickaxe is needed for a drop. Breaks when pushed by a piston.
     */
    private static DeferredBlock<PlayerMobPlateBlock> plate(String name, boolean players, boolean shrouded, boolean silent) {
        DeferredBlock<PlayerMobPlateBlock> block = BLOCKS.registerBlock(name,
                p -> new PlayerMobPlateBlock(silent ? SILENT : LOUD, p, players),
                p -> p.mapColor(players ? MapColor.COLOR_BLACK : MapColor.STONE).sound(SoundType.STONE).noCollision()
                        .strength(players ? 12.5F : 2.0F, players ? 1200.0F : 6.0F).requiresCorrectToolForDrops()
                        .pushReaction(PushReaction.POPPED).forceSolidOn().noOcclusion());
        ITEMS_IN_ORDER.add(ITEMS.registerSimpleBlockItem(block));
        return block;
    }

    public ObsidiPlates(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(ObsidiPlates::tab);
    }

    private static void tab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            ITEMS_IN_ORDER.forEach(event::accept);
        }
    }

}
