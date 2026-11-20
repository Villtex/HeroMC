package xreliquary;

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
import xreliquary.block.AltarBlock;
import xreliquary.block.AltarBlockEntity;
import xreliquary.block.FertileLilypadBlock;
import xreliquary.block.WraithNodeBlock;

/** XRBlocks with the ids of the pack (altarActive 1571, altarIdle 1572, lilypad 1573, wraithNode 1574). */
public final class XRBlocks {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Reliquary.MOD_ID);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Reliquary.MOD_ID);

    public static final int ALTAR_ACTIVE_ID = 1571;
    public static final int ALTAR_IDLE_ID = 1572;
    public static final int LILYPAD_ID = 1573;
    public static final int WRAITH_NODE_ID = 1574;

    /** BlockAltar(Material.rock): hardness 1.5, blockResistance 5 (= 1 in 26.3 terms), light 15 while active. */
    public static final DeferredBlock<AltarBlock> ALTAR = BLOCKS.registerBlock("altar", AltarBlock::new,
            p -> p.mapColor(MapColor.STONE).strength(1.5F, 1.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(AltarBlock.ACTIVE) ? 15 : 0));
    /** BlockFertileLilypad(Material.plants). */
    public static final DeferredBlock<FertileLilypadBlock> FERTILE_LILYPAD = BLOCKS.registerBlock("fertile_lilypad", FertileLilypadBlock::new,
            p -> p.mapColor(MapColor.PLANT).instabreak().sound(SoundType.LILY_PAD).noOcclusion().pushReaction(PushReaction.POPPED));
    /** BlockWraithNode(Material.rock): hardness 1.5. */
    public static final DeferredBlock<WraithNodeBlock> WRAITH_NODE = BLOCKS.registerBlock("wraith_node", WraithNodeBlock::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).strength(1.5F, 1.0F).sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarBlockEntity>> ALTAR_ENTITY = BLOCK_ENTITIES.register("altar",
            () -> new BlockEntityType<>(AltarBlockEntity::new, ALTAR.get()));

    private XRBlocks() {
    }

    static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
