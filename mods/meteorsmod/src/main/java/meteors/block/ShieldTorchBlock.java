package meteors.block;

import meteors.MeteorData;
import meteors.MeteorsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * BlockMeteorShieldTorch, the protected land tester: lit (id 1208) while its chunk is protected by a meteor shield,
 * unlit (1209) otherwise, checked on placing, on random ticks and on a right click; a right click also lists the
 * owners of the protecting shields. It drops nothing (quantityDropped 0), as in the original.
 */
public class ShieldTorchBlock extends TorchBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public ShieldTorchBlock(Properties properties) {
        super(ParticleTypes.FLAME, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        checkArea(state, level, pos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        checkArea(state, level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return use(state, level, pos, player);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        particles(state, level, pos, random, null);
    }

    static void checkArea(BlockState state, Level level, BlockPos pos) {
        if (level instanceof ServerLevel server) {
            boolean safe = MeteorData.get(server.getServer()).isProtected(pos.getX() >> 4, pos.getZ() >> 4);
            if (state.getValue(ACTIVE) != safe) {
                level.setBlock(pos, state.setValue(ACTIVE, safe), 3);
            }
        }
    }

    static InteractionResult use(BlockState state, Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel server) {
            checkArea(state, level, pos);
            MeteorData data = MeteorData.get(server.getServer());
            int cx = pos.getX() >> 4;
            int cz = pos.getZ() >> 4;
            if (data.isProtected(cx, cz)) {
                player.sendSystemMessage(Component.translatable("ProtectionTorch.landOwnership"));
                for (String owner : data.owners(cx, cz)) {
                    player.sendSystemMessage(Component.literal("    - " + owner));
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** randomDisplayTick of the lit tester: one meteor dust particle at the flame. */
    static void particles(BlockState state, Level level, BlockPos pos, RandomSource random, Direction wall) {
        if (!state.getValue(ACTIVE)) {
            return;
        }
        double d = pos.getX() + 0.5F + (random.nextFloat() - 0.5F) * 0.2;
        double d1 = pos.getY() + 0.7F + (random.nextFloat() - 0.5F) * 0.2;
        double d2 = pos.getZ() + 0.5F + (random.nextFloat() - 0.5F) * 0.2;
        double d3 = 0.22F;
        double d4 = 0.27F;
        if (wall == null) {
            level.addParticle(MeteorsMod.METEOR_DUST.get(), d, d1, d2, 0.0, 0.0, 0.0);
        } else {
            Direction opposite = wall.getOpposite();
            level.addParticle(MeteorsMod.METEOR_DUST.get(), d + d4 * opposite.getStepX(), d1 + d3, d2 + d4 * opposite.getStepZ(), 0.0, 0.0, 0.0);
        }
    }
}
