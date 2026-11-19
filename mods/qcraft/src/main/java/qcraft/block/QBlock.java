package qcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import qcraft.QuantumCraft;
import qcraft.item.QBlockItem;
import qcraft.item.QComponents;

/**
 * BlockQBlock "Observer Dependent Block" / "Quantum Block" (and their entangled versions): a block that looks and
 * behaves like one of six impostor blocks (one per side, from the crafting recipe), chosen by the direction it is
 * observed from ({@link QBlockEntity}).
 *
 * <p>The block entity decides on the server which side is observed. The impostor type that results is also kept in
 * the block state ({@link #TYPE}), so that light, light dampening, redstone, collision and the client's prediction
 * follow the impostor as the original's {@code getImpostorBlock} did for every query; the sub type (metadata of the
 * original) is {@link #SUBTYPE}. Swirl and fuzz (unobserved, just changed, goggles, under water) are client-side and
 * come from the block entity's model data ({@code qcraft.client.QBlockModel}).
 *
 * <p>Like the original (a BlockSand), it falls while it is sand or gravel, keeping its data.
 */
public class QBlock extends BaseEntityBlock {
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, Impostors.COUNT - 1);
    public static final IntegerProperty SUBTYPE = IntegerProperty.create("subtype", 0, 1);

    public QBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, 0).setValue(SUBTYPE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, SUBTYPE);
    }

    public static @Nullable Block impostor(BlockState state) {
        return Impostors.block(state.getValue(TYPE));
    }

    public static BlockState impostorState(BlockState state) {
        return Impostors.state(state.getValue(TYPE));
    }

    /** isBlockNormalCube: an impostor that is not the redstone block, ice or glass. */
    public static boolean isNormalCube(BlockState state) {
        int type = state.getValue(TYPE);
        return impostor(state) != null && type != Impostors.REDSTONE_BLOCK && type != Impostors.ICE && type != Impostors.GLASS;
    }

    /** Light value of the impostor (getLightValue). */
    public static int lightOf(BlockState state) {
        return impostorState(state).getLightEmission();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, QuantumCraft.QBLOCK_ENTITY.get(), QBlockEntity::tick);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        QComponents.QBlockData data = context.getItemInHand().get(QComponents.QBLOCK.get());
        BlockState state = this.defaultBlockState();
        if (data != null) {
            // onBlockPlacedBy: metadata = damage (sub type); not yet observed: the type of side 1
            state = state.setValue(SUBTYPE, Math.clamp(data.subType(), 0, 1)).setValue(TYPE, clampType(data.sides().get(1)));
        }
        return state;
    }

    static int clampType(int type) {
        return type >= 0 && type < Impostors.COUNT ? type : 0;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        // ItemQBlock.placeBlockAt
        if (level.getBlockEntity(pos) instanceof QBlockEntity qblock) {
            QComponents.QBlockData data = stack.get(QComponents.QBLOCK.get());
            if (data != null) {
                qblock.setTypes(data.types());
            }
            qblock.setEntanglementFrequency(QBlockItem.getEntanglementFrequency(stack));
        }
    }

    // ---- shape (setBlockBoundsBasedOnState, addCollisionBoxesToList)

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(TYPE) > 0) {
            return Shapes.block();
        }
        // nothing to look like: only selectable while swirling or fuzzy (goggles, under water)
        if (level.getBlockEntity(pos) instanceof QBlockEntity qblock && qblock.getAppearance() != QBlockEntity.Appearance.BLOCK) {
            return Shapes.block();
        }
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return impostor(state) != null ? Shapes.block() : Shapes.empty();
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return impostor(state) == null;
    }

    // ---- properties of the impostor

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        // getBlockHardness = the impostor's; canHarvestBlock is always true and no tool is effective on a qBlock
        Block block = impostor(state);
        float hardness = block == null ? 0.0F : block.defaultDestroyTime();
        if (hardness < 0.0F) {
            return 0.0F;
        }
        if (hardness == 0.0F) {
            return 1.0F;
        }
        return player.getDestroySpeed(state) / hardness / 30.0F;
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        Block block = impostor(state);
        return block != null ? block.getExplosionResistance() : 0.0F;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return impostor(state) != null ? impostorState(state).getFlammability(level, pos, direction) : 0;
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return impostor(state) != null && impostorState(state).isFlammable(level, pos, direction);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return impostor(state) != null ? impostorState(state).getFireSpreadSpeed(level, pos, direction) : 0;
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return impostor(state) != null ? impostorState(state).getLightDampening() : 0;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return impostor(state) == null || impostorState(state).propagatesSkylightDown();
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(TYPE) == Impostors.REDSTONE_BLOCK ? 15 : 0;
    }

    // ---- drops (removeBlockByPlayer; explosions drop nothing, dropBlockAsItemWithChance was empty)

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel server && !player.getAbilities().instabuild) {
            ItemStack tool = player.getMainHandItem();
            boolean silk = EnchantmentHelper.getItemEnchantmentLevel(
                    server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), tool) > 0;
            if (silk) {
                if (level.getBlockEntity(pos) instanceof QBlockEntity qblock) {
                    popResource(level, pos, QBlockItem.create(qblock.getSubType(), qblock.getTypes(), qblock.getEntanglementFrequency(), 1));
                }
            } else if (impostor(state) != null) {
                BlockState impostor = impostorState(state);
                if (player.hasCorrectToolForDrops(impostor)) {
                    for (ItemStack drop : Block.getDrops(impostor, server, pos, null, player, tool)) {
                        popResource(level, pos, drop);
                    }
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        if (level.getBlockEntity(pos) instanceof QBlockEntity qblock) {
            return QBlockItem.create(qblock.getSubType(), qblock.getTypes(), qblock.getEntanglementFrequency(), 1);
        }
        return ItemStack.EMPTY;
    }

    // ---- falling (BlockSand: only while the impostor is sand or gravel)

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, 2);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        ticks.scheduleTick(pos, this, 2);
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(impostor(state) instanceof FallingBlock)) {
            return;
        }
        if (FallingBlock.isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinY()) {
            // onStartFalling: the falling entity carries the block entity and never drops an item
            BlockEntity entity = level.getBlockEntity(pos);
            var data = entity != null ? entity.saveWithoutMetadata(level.registryAccess()) : null;
            FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
            falling.blockData = data;
            falling.dropItem = false;
        }
    }
}
