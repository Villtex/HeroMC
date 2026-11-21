package tconstruct.deco;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Punji (trap.punji): one to five sharpened sticks (meta 0..4; right click with another stick adds one). Any living thing
 * in the block takes cactus damage, meta / 2 + 1, plus fallDistance * 1.5 + 2 when it falls in, and Slowness II for a
 * second. Breaking drops all sticks.
 */
public class PunjiBlock extends Block {
    public static final IntegerProperty STICKS = IntegerProperty.create("sticks", 1, 5);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 6, 14);

    public PunjiBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STICKS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STICKS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** onEntityCollidedWithBlock ran for every entity in the cell, also those standing on the sticks. */
    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return Shapes.block();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (level instanceof ServerLevel server && entity instanceof LivingEntity living) {
            int damage = (state.getValue(STICKS) - 1) / 2 + 1;
            if (entity.fallDistance > 0.0) {
                damage = (int) (damage + (entity.fallDistance * 1.5 + 2.0));
            }
            living.hurtServer(server, server.damageSources().cactus(), damage);
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 1));
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        int sticks = state.getValue(STICKS);
        if (sticks < 5 && stack.is(this.asItem())) {
            level.setBlock(pos, state.setValue(STICKS, sticks + 1), 3);
            SoundType sound = state.getSoundType();
            level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            stack.consume(1, player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }
}
