package tconstruct.landmine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import tconstruct.TDeco;

import java.util.List;

/**
 * BlockLandmine: a plate of one pixel on any solid face (ATTACH = the side it lies on). It goes off when something
 * that may trigger it is in its block (trigger type 0 everything, 1 living things, 2 players, 3 nothing) or when it is
 * powered, checked on entity contact, neighbour changes and random ticks, and only with items in its trigger slots
 * ({@link LandmineLogic}). It drops itself and its contents when broken (not while it goes off); right click opens it.
 */
public class LandmineBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> ATTACH = BlockStateProperties.FACING;
    private final int trigger;

    public LandmineBlock(int trigger, Properties properties) {
        super(properties);
        this.trigger = trigger;
        this.registerDefaultState(this.stateDefinition.any().setValue(ATTACH, Direction.DOWN).setValue(TDeco.LIGHT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ATTACH, TDeco.LIGHT);
    }

    /** setBlockBoundsBasedOnState: 14 x 14 pixels, one thick, against the attached side. */
    public static VoxelShape plate(Direction attach) {
        return switch (attach) {
            case DOWN -> Block.box(1, 0, 1, 15, 1, 15);
            case UP -> Block.box(1, 15, 1, 15, 16, 15);
            case NORTH -> Block.box(1, 1, 0, 15, 15, 1);
            case SOUTH -> Block.box(1, 1, 15, 15, 15, 16);
            case WEST -> Block.box(0, 1, 1, 1, 15, 15);
            case EAST -> Block.box(15, 1, 1, 16, 15, 15);
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return plate(state.getValue(ATTACH));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return Shapes.block();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction attach = context.getClickedFace().getOpposite();
        BlockState state = this.defaultBlockState().setValue(ATTACH, attach);
        if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
            return state;
        }
        for (Direction d : context.getNearestLookingDirections()) {
            state = state.setValue(ATTACH, d);
            if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
                return state;
            }
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction attach = state.getValue(ATTACH);
        BlockPos support = pos.relative(attach);
        return level.getBlockState(support).isFaceSturdy(level, support, attach.getOpposite());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return direction == state.getValue(ATTACH) && !state.canSurvive(level, pos) ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : state;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LandmineBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, TDeco.LANDMINE_ENTITY.get(), LandmineBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof LandmineBlockEntity mine) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) -> new LandmineMenu(id, inv, mine), Component.translatable("container.tconstruct.landmine")));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean moving) {
        if (level instanceof ServerLevel server) {
            this.checkExplosion(server, pos, false);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.checkExplosion(level, pos, false);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (level instanceof ServerLevel server) {
            this.checkExplosion(server, pos, false);
        }
    }

    public void checkExplosion(ServerLevel level, BlockPos pos, boolean skipCheck) {
        if (!(level.getBlockEntity(pos) instanceof LandmineBlockEntity mine) || mine.isExploding) {
            return;
        }
        if ((skipCheck || this.triggerer(level, pos) != null || level.hasNeighborSignal(pos)) && hasItems(mine)) {
            if (mine.soundCooldown <= 0) {
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3F, 0.6F);
                mine.soundCooldown = 35;
            }
            Entity triggerer = this.triggerer(level, pos);
            if (triggerer == null) {
                // getMineTriggerer: powered without anyone on it, the mine goes off for a primed TNT that is not in the world
                triggerer = new PrimedTnt(EntityTypes.TNT, level);
                triggerer.setPos(pos.getX(), pos.getY(), pos.getZ());
            }
            new LandmineLogic(level, pos, mine, triggerer).explode();
        }
    }

    private static boolean hasItems(LandmineBlockEntity mine) {
        return !mine.getItem(0).isEmpty() || !mine.getItem(1).isEmpty() || !mine.getItem(2).isEmpty();
    }

    /** getMineState / getMineTriggerer: the first entity in the block that may trigger this mine. */
    private @Nullable Entity triggerer(ServerLevel level, BlockPos pos) {
        AABB box = new AABB(pos);
        List<? extends Entity> list = switch (this.trigger) {
            case 0 -> level.getEntities((Entity) null, box);
            case 1 -> level.getEntitiesOfClass(LivingEntity.class, box);
            case 2 -> level.getEntitiesOfClass(Player.class, box);
            default -> List.of();
        };
        for (Entity entity : list) {
            if (!entity.isIgnoringBlockTriggers()) {
                return entity;
            }
        }
        return null;
    }
}
