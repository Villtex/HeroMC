package xreliquary.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import xreliquary.XRBlocks;
import xreliquary.XRFx;

/**
 * BlockAltar (altarIdle and altarActive of 1.5.2 as one block with {@link #ACTIVE}): three redstone dusts start it;
 * active it glows (light 15) and, during the day under the open sky, counts down 24000 ± 6000 ticks, then turns the
 * block above into glowstone and goes idle again.
 */
public class AltarBlock extends BaseEntityBlock {
    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;

    public AltarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, XRBlocks.ALTAR_ENTITY.get(), AltarBlockEntity::tick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE) && level.getOverworldClockTime() % 24000L < 12000L && level.canSeeSky(pos.above()) && random.nextInt(3) == 0) {
            level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.9F, 0.9F, 0.0F), pos.getX() + 0.5 + random.nextGaussian() / 8.0,
                    pos.getY() + 1.1, pos.getZ() + 0.5 + random.nextGaussian() / 8.0, 0.9, 0.9, 0.0);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (state.getValue(ACTIVE) || !(level.getBlockEntity(pos) instanceof AltarBlockEntity altar)) {
            return InteractionResult.SUCCESS;
        }
        if (!stack.is(Items.REDSTONE)) {
            return InteractionResult.SUCCESS;
        }
        int slot = XRFx.findMain(player, Items.REDSTONE);
        if (slot < 0) {
            return InteractionResult.SUCCESS;
        }
        int count = altar.redstoneCount();
        XRFx.sound(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.FIRE_EXTINGUISH, 0.3F,
                0.5F + 0.5F * count + (float) (level.getRandom().nextGaussian() / 8.0));
        for (int i = level.getRandom().nextInt(3); i < 3 + count * 4 + count; i++) {
            XRFx.particle(level, DustParticleOptions.REDSTONE, pos.getX() + 0.5 + level.getRandom().nextGaussian() / 5.0, pos.getY() + 1.2,
                    pos.getZ() + 0.5 + level.getRandom().nextGaussian() / 5.0, 1.0, 0.0, 0.0);
        }
        if (!level.isClientSide()) {
            player.getInventory().removeItem(slot, 1);
            altar.addRedstone();
        }
        return InteractionResult.SUCCESS;
    }
}
