package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import twilightforest.TwilightForest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BlockTFMagicLogSpecial: the heart of a magic tree (clock of the tree of time, heart of transformation, miner's
 * core, sorting engine). Light 15, drops the magic log of its tree. Right click switches it on and off (1.5.2
 * metadata & 12 == 12 = off); while on it ticks every 20 ticks and does its tree's effect.
 */
public class MagicLogSpecialBlock extends Block {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final int TICK_RATE = 20;
    public static final ResourceKey<Biome> ENCHANTED_FOREST = ResourceKey.create(Registries.BIOME, TwilightForest.id("enchanted_forest"));

    public enum Effect {
        TIME, TRANSFORMATION, MINING, SORTING
    }

    private final Effect effect;

    public MagicLogSpecialBlock(Effect effect, Properties properties) {
        super(properties.randomTicks());
        this.effect = effect;
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, TICK_RATE);
    }

    /** A core placed by the world generation has no tick yet (onBlockAdded did that): a random tick starts it. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, TICK_RATE);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        boolean active = state.getValue(ACTIVE);
        level.setBlock(pos, state.setValue(ACTIVE, !active), Block.UPDATE_ALL);
        if (!active) {
            level.scheduleTick(pos, this, TICK_RATE);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) {
            return;
        }
        switch (this.effect) {
            case TIME -> {
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1F, 0.5F);
                doTreeOfTimeEffect(level, pos, random);
            }
            case TRANSFORMATION -> doTreeOfTransformationEffect(level, pos, random);
            case MINING -> doMinersTreeEffect(level, pos, random);
            case SORTING -> doSortingTreeEffect(level, pos, random);
        }
        level.scheduleTick(pos, this, TICK_RATE);
    }

    /** 24 * 20 random ticks for blocks within +-16 that tick randomly. */
    private static void doTreeOfTimeEffect(ServerLevel level, BlockPos pos, RandomSource rand) {
        int numticks = 24 * TICK_RATE;
        for (int i = 0; i < numticks; i++) {
            BlockPos there = pos.offset(rand.nextInt(32) - 16, rand.nextInt(32) - 16, rand.nextInt(32) - 16);
            BlockState state = level.getBlockState(there);
            if (!state.isAir() && state.isRandomlyTicking()) {
                state.randomTick(level, there, rand);
            }
        }
    }

    /**
     * One column within radius 16 per tick becomes enchanted forest. Biomes are kept per 4 x 4 x 4 blocks today, so
     * the whole 4 x 4 column cell (all heights) changes, and the chunk's biomes are sent to the players again.
     */
    private static void doTreeOfTransformationEffect(ServerLevel level, BlockPos pos, RandomSource rand) {
        int dx = rand.nextInt(32) - 16;
        int dz = rand.nextInt(32) - 16;
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.NOTE_BLOCK_HARP, SoundSource.BLOCKS, 0.1F,
                rand.nextFloat() * 2.0F);
        if (Math.sqrt(dx * dx + dz * dz) >= 16.0) {
            return;
        }
        BlockPos target = pos.offset(dx, 0, dz);
        Holder<Biome> enchanted = level.registryAccess().lookupOrThrow(Registries.BIOME).get(ENCHANTED_FOREST).orElse(null);
        if (enchanted == null || level.getBiome(target).is(ENCHANTED_FOREST) || !level.hasChunkAt(target)) {
            return;
        }
        LevelChunk chunk = level.getChunkAt(target);
        int qx = QuartPos.fromBlock(target.getX());
        int qz = QuartPos.fromBlock(target.getZ());
        chunk.fillBiomesFromNoise((x, y, z) -> x == qx && z == qz ? enchanted : chunk.getNoiseBiome(x, y, z));
        chunk.markUnsaved();
        level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
    }

    /** ItemTFOreMagnet.doMagnet from the core towards a random point within 32 blocks. */
    private static void doMinersTreeEffect(ServerLevel level, BlockPos pos, RandomSource rand) {
        int dx = rand.nextInt(64) - 32;
        int dy = rand.nextInt(64) - 32;
        int dz = rand.nextInt(64) - 32;
        int moved = OreMagnet.doMagnet(level, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
        if (moved > 0) {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.1F, 1.0F);
        }
    }

    /**
     * Chests (plain chests, as 1.5.2 Block.chest) in the 32-block cube: one random item moves to the chest that has
     * the most items of its creative tab, then stacks of the same item merge into it. The tab grouping uses the
     * creative tabs of 26.3 (the first category tab showing the item); items in no tab group by their mod.
     */
    private static void doSortingTreeEffect(ServerLevel level, BlockPos pos, RandomSource rand) {
        int search = 16;
        List<Container> chests = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        int itemCount = 0;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-search, -search, -search), pos.offset(search - 1, search - 1, search - 1))) {
            BlockState state = level.getBlockState(p);
            if (!state.is(Blocks.CHEST) || seen.contains(p)) {
                continue;
            }
            BlockPos here = p.immutable();
            seen.add(here);
            if (state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                seen.add(ChestBlock.getConnectedBlockPos(here, state));
            }
            Container chest = ChestBlock.getContainer((ChestBlock) state.getBlock(), state, level, here, true);
            if (chest == null) {
                continue;
            }
            int inChest = 0;
            for (int i = 0; i < chest.getContainerSize(); i++) {
                if (!chest.getItem(i).isEmpty()) {
                    inChest++;
                    itemCount++;
                }
            }
            if (inChest > 0) {
                chests.add(chest);
            }
        }
        if (itemCount == 0) {
            return;
        }
        ItemStack beingSorted = ItemStack.EMPTY;
        int sortedChestNum = -1;
        int sortedSlotNum = -1;
        int itemNumber = rand.nextInt(itemCount);
        int currentNumber = 0;
        for (int i = 0; i < chests.size(); i++) {
            Container chest = chests.get(i);
            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                ItemStack current = chest.getItem(slot);
                if (!current.isEmpty() && currentNumber++ == itemNumber) {
                    beingSorted = current;
                    sortedChestNum = i;
                    sortedSlotNum = slot;
                }
            }
        }
        if (beingSorted.isEmpty()) {
            return;
        }
        int matchChestNum = -1;
        int matchCount = 0;
        for (int chestNum = 0; chestNum < chests.size(); chestNum++) {
            Container chest = chests.get(chestNum);
            int matches = 0;
            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                ItemStack current = chest.getItem(slot);
                if (!current.isEmpty() && group(level, beingSorted.getItem()).equals(group(level, current.getItem()))) {
                    matches += current.getCount();
                }
            }
            if (matches > matchCount) {
                matchCount = matches;
                matchChestNum = chestNum;
            }
        }
        if (matchChestNum >= 0 && matchChestNum != sortedChestNum) {
            Container moveChest = chests.get(matchChestNum);
            Container oldChest = chests.get(sortedChestNum);
            int moveSlot = -1;
            for (int i = 0; i < moveChest.getContainerSize(); i++) {
                if (moveChest.getItem(i).isEmpty()) {
                    moveSlot = i;
                    break;
                }
            }
            if (moveSlot >= 0) {
                oldChest.setItem(sortedSlotNum, ItemStack.EMPTY);
                moveChest.setItem(moveSlot, beingSorted);
            }
        }
        if (beingSorted.getCount() < beingSorted.getMaxStackSize()) {
            for (Container chest : chests) {
                for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                    ItemStack current = chest.getItem(slot);
                    if (!current.isEmpty() && current != beingSorted && ItemStack.isSameItemSameComponents(beingSorted, current)
                            && current.getCount() <= beingSorted.getMaxStackSize() - beingSorted.getCount()) {
                        chest.setItem(slot, ItemStack.EMPTY);
                        beingSorted.grow(current.getCount());
                        current.setCount(0);
                    }
                }
                chest.setChanged();
            }
        }
    }

    private static Map<Item, Object> groups;

    /** The creative tab of an item (Item.tabToDisplayOn of 1.5.2), or its mod when no tab shows it. */
    private static synchronized Object group(ServerLevel level, Item item) {
        if (groups == null) {
            groups = new HashMap<>();
            try {
                CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(), false, level.registryAccess());
                for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
                    if (tab.getType() != CreativeModeTab.Type.CATEGORY) {
                        continue;
                    }
                    for (ItemStack stack : tab.getDisplayItems()) {
                        groups.putIfAbsent(stack.getItem(), tab);
                    }
                }
            } catch (RuntimeException e) {
                TwilightForest.LOGGER.warn("Sorting tree: creative tabs not available, grouping items by mod", e);
            }
        }
        Object group = groups.get(item);
        if (group != null) {
            return group;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return id.getNamespace();
    }
}
