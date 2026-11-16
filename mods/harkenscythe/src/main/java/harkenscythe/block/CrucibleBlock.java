package harkenscythe.block;

import harkenscythe.HSItems;
import harkenscythe.entity.SpectralPets;
import harkenscythe.item.Essence;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockHSSoulCrucible / BlockHSBloodCrucible: a cauldron that stores up to ten portions of souls (5 each, from a
 * Soulkeeper or Soul Vessel) or blood (10 each, from a Bloodkeeper or Blood Vessel); the metadata is {@link #LEVEL}.
 * Breaking a filled crucible frees its content as an ectoplasm or hemoglobin slime (Teil 9, part 3: through
 * {@link SpectralPets#spawnEssenceSlime}).
 */
public class CrucibleBlock extends Block {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 10);
    /** The walls (1/8 thick) and the floor (5/16 high) of addCollisionBoxesToList. */
    private static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Block.box(2, 5, 2, 14, 16, 14), BooleanOp.ONLY_FIRST);
    private final boolean blood;

    public CrucibleBlock(boolean blood, Properties properties) {
        super(properties);
        this.blood = blood;
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    public boolean isBlood() {
        return this.blood;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        int portion = this.blood ? 10 : 5;
        boolean fits = this.blood ? stack.is(HSItems.BLOODKEEPER.get()) || stack.is(HSItems.BLOOD_VESSEL.get())
                : stack.is(HSItems.SOULKEEPER.get()) || stack.is(HSItems.SOUL_VESSEL.get());
        int level0 = state.getValue(LEVEL);
        if (fits && Essence.stored(stack) >= portion && level0 < 10) {
            if (!player.getAbilities().instabuild) {
                ItemStack rest = Essence.drain(stack, portion);
                if (rest != stack) {
                    player.setItemInHand(hand, rest);
                }
            }
            level.setBlock(pos, state.setValue(LEVEL, level0 + 1), Block.UPDATE_CLIENTS);
        }
        return InteractionResult.SUCCESS;
    }

    /** breakBlock: the content leaves as a slime of size 1 (up to 10), 2 (up to 25) or 3; blood counts double. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        int worth = state.getValue(LEVEL) * (this.blood ? 10 : 5);
        if (worth == 0) {
            return;
        }
        int size;
        if (this.blood) {
            size = worth >= 51 ? 3 : worth >= 21 ? 2 : 1;
        } else {
            size = worth >= 26 ? 3 : worth >= 11 ? 2 : 1;
        }
        SpectralPets.spawnEssenceSlime(level, pos.getX(), pos.getY() + 1.0, pos.getZ(), size, worth, this.blood);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LEVEL) > 0 && random.nextFloat() > 0.9F) {
            if (this.blood) {
                level.addParticle(new DustParticleOptions(0xCC0000, 1.0F), pos.getX() + 0.5F, pos.getY() + 0.9F, pos.getZ() + 0.5F, 0.8, 0.0, 0.0);
            } else {
                level.addParticle(ParticleTypes.WITCH, pos.getX() + 0.5F, pos.getY() + 0.7F, pos.getZ() + 0.5F, 0.0, 0.0, 0.0);
            }
        }
    }
}
