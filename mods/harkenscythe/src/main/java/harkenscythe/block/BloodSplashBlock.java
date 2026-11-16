package harkenscythe.block;

import harkenscythe.HSBlocks;
import harkenscythe.HSItems;
import harkenscythe.item.SpecialTierAbilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockHSBloodSplash: a splash of blood on the ground. Metadata 1-4 (red), 5-8 (green) and 9-12 (purple) are
 * {@link #COLOR} and {@link #STAGE}; stage 4 is dried blood. A random tick dries fresh blood and removes dried blood,
 * except on a biomass block. Fresh blood can be taken up into a Bloodkeeper or Blood Vessel (one blood each; it repairs
 * biomass gear by 2, 4 or 6). It slows down whatever walks through it.
 */
public class BloodSplashBlock extends Block {
    public enum BloodColor implements StringRepresentable {
        RED, GREEN, PURPLE;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static final EnumProperty<BloodColor> COLOR = EnumProperty.create("color", BloodColor.class);
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 1, 4);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 0.16, 16);

    public BloodSplashBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(COLOR, BloodColor.RED).setValue(STAGE, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR, STAGE);
    }

    /** The block state of the 1.5.2 metadata 1-12. */
    public BlockState fromMeta(int meta) {
        int m = Math.max(1, Math.min(12, meta)) - 1;
        return this.defaultBlockState().setValue(COLOR, BloodColor.values()[m / 4]).setValue(STAGE, m % 4 + 1);
    }

    public static boolean isDried(BlockState state) {
        return state.is(HSBlocks.BLOOD_SPLASH_RESIDUE.get()) && state.getValue(STAGE) == 4;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return SoulLightBlock.canStayAbove(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockState(pos.below()).is(HSBlocks.BIOMASS_BLOCK.get())) {
            return;
        }
        if (state.getValue(STAGE) < 4) {
            level.setBlock(pos, state.setValue(STAGE, 4), Block.UPDATE_CLIENTS);
        } else {
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (entity instanceof LivingEntity && entity.isAlive()) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.9, 1.0, 0.9));
        }
    }

    /** getBiomassRepairAmount: red 2, green 4, purple 6 (by metadata 1-3, 5-7, 9-11). */
    private static int biomassRepair(BlockState state) {
        return switch (state.getValue(COLOR)) {
            case RED -> 2;
            case GREEN -> 4;
            case PURPLE -> 6;
        };
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (state.getValue(STAGE) == 4) {
            return InteractionResult.SUCCESS;
        }
        ItemStack result = null;
        if ((stack.is(HSItems.BLOOD_VESSEL.get()) || stack.is(HSItems.BLOODKEEPER.get())) && stack.getDamageValue() != 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
            result = stack;
        } else if (stack.is(HSItems.ESSENCE_VESSEL.get()) || stack.is(HSItems.ESSENCE_KEEPER.get())) {
            ItemStack filled = new ItemStack(stack.is(HSItems.ESSENCE_VESSEL.get()) ? HSItems.BLOOD_VESSEL.get() : HSItems.BLOODKEEPER.get());
            filled.setDamageValue(filled.getMaxDamage() - 1);
            result = filled;
        }
        if (result == null) {
            return InteractionResult.SUCCESS;
        }
        SpecialTierAbilities.biomass(player, biomassRepair(state));
        if (result != stack) {
            player.setItemInHand(hand, result);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SLIME_ATTACK, SoundSource.PLAYERS, 0.3F, 0.5F);
        level.removeBlock(pos, false);
        return InteractionResult.SUCCESS;
    }
}
