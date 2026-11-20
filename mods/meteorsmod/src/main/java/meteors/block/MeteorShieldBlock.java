package meteors.block;

import meteors.MeteorData;
import meteors.MeteorEvents;
import meteors.MeteorsConfig;
import meteors.MeteorsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * BlockMeteorShield: power 0..5 (metadata). Placed it charges (meteor chips or the next random tick bring it to 1);
 * each Red Meteor Gem adds a level up to 5, and the shield then protects ShieldRadiusMultiplier * power chunks around
 * its own chunk (Manhattan distance) from meteors. At 5 it shines with light 15 and a beam.
 */
public class MeteorShieldBlock extends BaseEntityBlock {
    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 5);

    public MeteorShieldBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MeteorShieldBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, MeteorsMod.METEOR_SHIELD_ENTITY.get(),
                level.isClientSide() ? MeteorShieldBlockEntity::clientTick : MeteorShieldBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer instanceof Player player) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("MeteorShield.charging").withStyle(ChatFormatting.YELLOW));
            }
            if (level.getBlockEntity(pos) instanceof MeteorShieldBlockEntity shield) {
                shield.setOwner(player.getGameProfile().name());
            }
        }
    }

    /** updateTick (random ticks): power 0 becomes 1. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int power = state.getValue(POWER);
        if (power <= 0) {
            level.setBlock(pos, state.setValue(POWER, 1), 2);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        int power = state.getValue(POWER);
        if (stack.is(MeteorsMod.METEOR_CHIPS.get()) && power == 0) {
            if (level.getBlockEntity(pos) instanceof MeteorShieldBlockEntity shield) {
                shield.setOwner(player.getGameProfile().name());
            }
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(POWER, 1), 2);
            }
            level.playSound(player, pos, MeteorsMod.SHIELD_POWERUP.get(), SoundSource.BLOCKS, 1.0F, 0.6F);
            stack.consume(1, player);
            return InteractionResult.SUCCESS;
        }
        if (stack.is(MeteorsMod.RED_METEOR_GEM.get()) && power > 0 && power < 5) {
            if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof MeteorShieldBlockEntity shield) {
                MeteorData data = MeteorData.get(server.getServer());
                data.removeShield(pos);
                data.addShield(pos, shield.shield(power + 1));
                if (MeteorsConfig.SHIELD_RADIUS.get() <= 0) {
                    player.sendSystemMessage(Component.translatable("MeteorShield.noUpgrade"));
                }
            }
            power++;
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(POWER, power), 3);
            }
            level.playSound(player, pos, MeteorsMod.SHIELD_POWERUP.get(), SoundSource.BLOCKS, 1.0F, power / 10.0F + 0.5F);
            stack.consume(1, player);
            if (player instanceof ServerPlayer serverPlayer) {
                player.sendSystemMessage(Component.translatable("MeteorShield.PowerUpgradePercentage").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(" " + 20 * power + "%").withStyle(ChatFormatting.GOLD)));
                if (power >= 5) {
                    MeteorEvents.grant(serverPlayer, "shield_fully_upgraded");
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWER) < 1) {
            MeteorShieldBlockEntity.particles(level, pos, random, 16, -1);
        }
    }
}
