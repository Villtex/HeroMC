package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import twilightforest.TFTower;

/**
 * BlockTFTowerDevice (1370). One block per metadata value of 1.5.2 ({@link TFTower#DEVICES}), so that the tile
 * entities come and go with the block as they did:
 * <ul>
 * <li>0/1 reappearing block, 2/3 vanishing block: right click or redstone activates it (not with a locked block within
 * 2), the active block spreads to its neighbours and vanishes (the reappearing one into a trace that comes back);</li>
 * <li>4 locked / 5 unlocked vanishing block (the tower key unlocks it);</li>
 * <li>6/7/8 carminite builder (redstone starts it, it builds where the nearest player looks, {@link TowerBuilderBlockEntity}),
 * timeout after 60 ticks without building;</li>
 * <li>9 antibuilder ({@link ReverterBlockEntity}), 10/11 ghast trap ({@link GhastTrapBlockEntity}), 12/13 carminite
 * reactor ({@link ReactorBlockEntity}, starts when six redstone blocks surround it).</li>
 * </ul>
 */
public class TowerDeviceBlock extends Block implements EntityBlock {
    public static final int REAPPEARING_INACTIVE = 0;
    public static final int REAPPEARING_ACTIVE = 1;
    public static final int VANISH_INACTIVE = 2;
    public static final int VANISH_ACTIVE = 3;
    public static final int VANISH_LOCKED = 4;
    public static final int VANISH_UNLOCKED = 5;
    public static final int BUILDER_INACTIVE = 6;
    public static final int BUILDER_ACTIVE = 7;
    public static final int BUILDER_TIMEOUT = 8;
    public static final int ANTIBUILDER = 9;
    public static final int GHASTTRAP_INACTIVE = 10;
    public static final int GHASTTRAP_ACTIVE = 11;
    public static final int REACTOR_INACTIVE = 12;
    public static final int REACTOR_ACTIVE = 13;
    /** Block.tickRate of the device and the translucent block. */
    static final int TICK_RATE = 15;

    public final int meta;

    public TowerDeviceBlock(int meta, Properties properties) {
        super(properties);
        this.meta = meta;
    }

    /** Hardness: -1 (unbreakable) for metadata 1..5, else 10. */
    public static float hardness(int meta) {
        return meta >= 1 && meta <= 5 ? -1.0F : 10.0F;
    }

    /** getExplosionResistance: 6000 for the inactive vanishing block, 6000000 for the locked one, else 35 * 0.6. */
    public static float resistance(int meta) {
        return meta == VANISH_INACTIVE ? 6000.0F : meta == VANISH_LOCKED ? 6000000.0F : 21.0F;
    }

    public static int light(int meta) {
        return switch (meta) {
            case 1, 3, 7 -> 4;
            case 9 -> 10;
            case 11, 13 -> 15;
            default -> 0;
        };
    }

    /** The block of a tower device or translucent metadata value. */
    public static BlockState device(int meta) {
        return TFTower.DEVICES.get(meta).get().defaultBlockState();
    }

    public static boolean isDevice(BlockState state, int meta) {
        return state.getBlock() instanceof TowerDeviceBlock device && device.meta == meta;
    }

    static boolean isTranslucent(BlockState state, int meta) {
        return state.getBlock() instanceof TowerTranslucentBlock block && block.meta == meta;
    }

    static void play(Level level, BlockPos pos, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound, SoundSource.BLOCKS, volume, pitch);
    }

    /** "random.click" and "random.pop" of 1.5.2. */
    static void click(Level level, BlockPos pos, float volume, float pitch) {
        play(level, pos, SoundEvents.UI_BUTTON_CLICK.value(), volume, pitch);
    }

    static void pop(Level level, BlockPos pos, float volume, float pitch) {
        play(level, pos, SoundEvents.ITEM_PICKUP, volume, pitch);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (this.meta != VANISH_INACTIVE && this.meta != REAPPEARING_INACTIVE) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            if (areNearbyLockBlocks(level, pos)) {
                click(level, pos, 1.0F, 0.3F);
            } else {
                changeToActiveVanishBlock(level, pos, this.meta == VANISH_INACTIVE ? device(VANISH_ACTIVE) : device(REAPPEARING_ACTIVE));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** A locked vanishing block within 2 blocks in every direction. */
    public static boolean areNearbyLockBlocks(Level level, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            if (isDevice(level.getBlockState(p), VANISH_LOCKED)) {
                return true;
            }
        }
        return false;
    }

    /** ItemTFTowerKey: a locked vanishing block becomes an unlocked one. */
    public static void unlockBlock(Level level, BlockPos pos) {
        if (isDevice(level.getBlockState(pos), VANISH_LOCKED)) {
            changeToBlock(level, pos, device(VANISH_UNLOCKED));
            click(level, pos, 0.3F, 0.6F);
        }
    }

    /** changeToBlockMeta: only a tower device or translucent block changes (setBlock with flag 3, neighbours told). */
    static void changeToBlock(Level level, BlockPos pos, BlockState to) {
        Block there = level.getBlockState(pos).getBlock();
        if (there instanceof TowerDeviceBlock || there instanceof TowerTranslucentBlock) {
            level.setBlock(pos, to, Block.UPDATE_ALL);
            level.updateNeighborsAt(pos, to.getBlock());
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && this.meta == BUILDER_INACTIVE && level.hasNeighborSignal(pos)) {
            changeToBlock(level, pos, device(BUILDER_ACTIVE));
            click(level, pos, 0.3F, 0.6F);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        switch (this.meta) {
            case VANISH_INACTIVE -> {
                if (powered && !areNearbyLockBlocks(level, pos)) {
                    changeToActiveVanishBlock(level, pos, device(VANISH_ACTIVE));
                }
            }
            case REAPPEARING_INACTIVE -> {
                if (powered && !areNearbyLockBlocks(level, pos)) {
                    changeToActiveVanishBlock(level, pos, device(REAPPEARING_ACTIVE));
                }
            }
            case BUILDER_INACTIVE -> {
                if (powered) {
                    changeToBlock(level, pos, device(BUILDER_ACTIVE));
                    click(level, pos, 0.3F, 0.6F);
                    level.scheduleTick(pos, TFTower.DEVICES.get(BUILDER_ACTIVE).get(), 4);
                }
            }
            case BUILDER_ACTIVE -> {
                if (!powered) {
                    changeToBlock(level, pos, device(BUILDER_INACTIVE));
                    click(level, pos, 0.3F, 0.6F);
                    level.scheduleTick(pos, TFTower.DEVICES.get(BUILDER_INACTIVE).get(), 4);
                }
            }
            case BUILDER_TIMEOUT -> {
                if (!powered) {
                    changeToBlock(level, pos, device(BUILDER_INACTIVE));
                }
            }
            case GHASTTRAP_INACTIVE -> {
                if (powered && level.getBlockEntity(pos) instanceof GhastTrapBlockEntity trap && trap.isCharged()) {
                    changeToBlock(level, pos, device(GHASTTRAP_ACTIVE));
                    click(level, pos, 0.3F, 0.6F);
                    level.scheduleTick(pos, TFTower.DEVICES.get(GHASTTRAP_ACTIVE).get(), 4);
                }
            }
            case REACTOR_INACTIVE -> {
                if (isReactorReady(level, pos)) {
                    changeToBlock(level, pos, device(REACTOR_ACTIVE));
                }
            }
            default -> {
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.meta == VANISH_ACTIVE || this.meta == REAPPEARING_ACTIVE) {
            if (this.meta == VANISH_ACTIVE) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            } else {
                BlockState trace = TowerTranslucentBlock.translucent(TowerTranslucentBlock.REAPPEARING_INACTIVE);
                level.setBlock(pos, trace, Block.UPDATE_ALL);
                level.scheduleTick(pos, trace.getBlock(), 80);
            }
            level.updateNeighborsAt(pos, this);
            pop(level, pos, 0.3F, 0.5F);
            activateNeighbours(level, pos);
        }
        if (this.meta == BUILDER_ACTIVE && level.hasNeighborSignal(pos) && level.getBlockEntity(pos) instanceof TowerBuilderBlockEntity builder
                && !builder.makingBlocks) {
            builder.startBuilding();
        }
        if (this.meta == BUILDER_INACTIVE || this.meta == BUILDER_TIMEOUT) {
            activateNeighbours(level, pos);
        }
    }

    static void activateNeighbours(Level level, BlockPos pos) {
        // x - 1, x + 1, y + 1, y - 1, z + 1, z - 1
        checkAndActivateVanishBlock(level, pos.west());
        checkAndActivateVanishBlock(level, pos.east());
        checkAndActivateVanishBlock(level, pos.above());
        checkAndActivateVanishBlock(level, pos.below());
        checkAndActivateVanishBlock(level, pos.south());
        checkAndActivateVanishBlock(level, pos.north());
    }

    private static boolean isReactorReady(Level level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            if (!level.getBlockState(pos.relative(side)).is(Blocks.REDSTONE_BLOCK)) {
                return false;
            }
        }
        return true;
    }

    public static void checkAndActivateVanishBlock(Level level, BlockPos pos) {
        BlockState there = level.getBlockState(pos);
        if ((isDevice(there, VANISH_INACTIVE) || isDevice(there, VANISH_UNLOCKED)) && !areNearbyLockBlocks(level, pos)) {
            changeToActiveVanishBlock(level, pos, device(VANISH_ACTIVE));
        } else if (isDevice(there, REAPPEARING_INACTIVE) && !areNearbyLockBlocks(level, pos)) {
            changeToActiveVanishBlock(level, pos, device(REAPPEARING_ACTIVE));
        } else if (isTranslucent(there, TowerTranslucentBlock.BUILT_INACTIVE)) {
            changeToActiveVanishBlock(level, pos, TowerTranslucentBlock.translucent(TowerTranslucentBlock.BUILT_ACTIVE));
        }
    }

    public static void changeToActiveVanishBlock(Level level, BlockPos pos, BlockState to) {
        changeToBlock(level, pos, to);
        pop(level, pos, 0.3F, 0.6F);
        BlockState now = level.getBlockState(pos);
        int delay;
        if (now.getBlock() instanceof TowerDeviceBlock device && (device.meta == VANISH_ACTIVE || device.meta == REAPPEARING_ACTIVE)) {
            delay = 2 + level.getRandom().nextInt(5);
        } else {
            delay = isTranslucent(now, TowerTranslucentBlock.BUILT_ACTIVE) ? 10 : TICK_RATE;
        }
        level.scheduleTick(pos, now.getBlock(), delay);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (this.meta == VANISH_ACTIVE || this.meta == REAPPEARING_ACTIVE || this.meta == BUILDER_ACTIVE) {
            sparkle(level, pos, random);
        }
    }

    /** sparkle: one "reddust" particle just outside each face that is not covered by an opaque block. */
    public static void sparkle(Level level, BlockPos pos, RandomSource rand) {
        double offset = 0.0625;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (int side = 0; side < 6; side++) {
            double rx = x + rand.nextFloat();
            double ry = y + rand.nextFloat();
            double rz = z + rand.nextFloat();
            if (side == 0 && !opaque(level, pos.above())) {
                ry = y + 1 + offset;
            }
            if (side == 1 && !opaque(level, pos.below())) {
                ry = y - offset;
            }
            if (side == 2 && !opaque(level, pos.south())) {
                rz = z + 1 + offset;
            }
            if (side == 3 && !opaque(level, pos.north())) {
                rz = z - offset;
            }
            if (side == 4 && !opaque(level, pos.east())) {
                rx = x + 1 + offset;
            }
            if (side == 5 && !opaque(level, pos.west())) {
                rx = x - offset;
            }
            if (rx < x || rx > x + 1 || ry < 0.0 || ry > y + 1 || rz < z || rz > z + 1) {
                level.addParticle(new DustParticleOptions(0xFF0000, 1.0F), rx, ry, rz, 0.0, 0.0, 0.0);
            }
        }
    }

    private static boolean opaque(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).isSolidRender();
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        // breakBlock of an active builder: the blocks it built start to vanish
        if (this.meta == BUILDER_ACTIVE && !level.getBlockState(pos).is(this)) {
            activateNeighbours(level, pos);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (this.meta) {
            case BUILDER_ACTIVE -> new TowerBuilderBlockEntity(pos, state);
            case ANTIBUILDER -> new ReverterBlockEntity(pos, state);
            case GHASTTRAP_INACTIVE, GHASTTRAP_ACTIVE -> new GhastTrapBlockEntity(pos, state);
            case REACTOR_ACTIVE -> new ReactorBlockEntity(pos, state);
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == TFTower.BUILDER_ENTITY.get()) {
            return level.isClientSide() ? null : (l, p, s, be) -> ((TowerBuilderBlockEntity) be).tick(l, p);
        } else if (type == TFTower.REVERTER_ENTITY.get()) {
            return (l, p, s, be) -> ((ReverterBlockEntity) be).tick(l, p);
        } else if (type == TFTower.GHAST_TRAP_ENTITY.get()) {
            return (l, p, s, be) -> ((GhastTrapBlockEntity) be).tick(l, p, this.meta == GHASTTRAP_ACTIVE);
        } else if (type == TFTower.REACTOR_ENTITY.get()) {
            return (l, p, s, be) -> ((ReactorBlockEntity) be).tick(l, p);
        }
        return null;
    }
}
