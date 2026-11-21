package tconstruct.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jspecify.annotations.Nullable;
import tconstruct.TDeco;
import tconstruct.TPlayer;
import tconstruct.player.PlayerItems;

/**
 * EssenceExtractor: three quarters high, with the book of the enchanting table on top. Right click stores all the
 * player's experience (experienceTotal) and tells the total and the stored levels; left click takes all of it out as an
 * essence crystal, picked up at once. Bookshelves around it send the enchanting particles like the enchanting table.
 */
public class EssenceExtractorBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = net.minecraft.world.level.block.Block.box(0, 0, 0, 16, 12, 16);

    public EssenceExtractorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssenceExtractorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, TDeco.EXTRACTOR_ENTITY.get(), EssenceExtractorBlockEntity::clientTick) : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EssenceExtractorBlockEntity extractor) {
            extractor.essence += player.totalExperience;
            player.giveExperienceLevels(-player.experienceLevel - 1);
            extractor.setChanged();
            player.sendSystemMessage(Component.translatable("message.tconstruct.essence.total", extractor.essence));
            player.sendSystemMessage(Component.translatable("message.tconstruct.essence.levels", PlayerItems.EssenceCrystal.levels(extractor.essence)));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof EssenceExtractorBlockEntity extractor) {
            int amount = extractor.essence;
            extractor.essence = 0;
            extractor.setChanged();
            if (amount > 0) {
                ItemStack crystal = new ItemStack(TPlayer.ESSENCE_CRYSTAL.get());
                crystal.set(TPlayer.ESSENCE.get(), amount);
                ItemEntity item = new ItemEntity(server, player.getX(), player.getY() - 1.0, player.getZ(), crystal);
                server.addFreshEntity(item);
                if (!(player instanceof FakePlayer)) {
                    item.playerTouch(player);
                }
            }
        }
    }

    /** randomDisplayTick: the enchanting particles from bookshelves two blocks away with air between. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int x = pos.getX() - 2; x <= pos.getX() + 2; x++) {
            for (int z = pos.getZ() - 2; z <= pos.getZ() + 2; z++) {
                if (x > pos.getX() - 2 && x < pos.getX() + 2 && z == pos.getZ() - 1) {
                    z = pos.getZ() + 2;
                }
                if (random.nextInt(16) == 0) {
                    for (int y = pos.getY(); y <= pos.getY() + 1; y++) {
                        if (level.getBlockState(new BlockPos(x, y, z)).is(Blocks.BOOKSHELF)) {
                            if (!level.isEmptyBlock(new BlockPos((x - pos.getX()) / 2 + pos.getX(), y, (z - pos.getZ()) / 2 + pos.getZ()))) {
                                break;
                            }
                            level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5,
                                    x - pos.getX() + random.nextFloat() - 0.5, y - pos.getY() - random.nextFloat() - 1.0F,
                                    z - pos.getZ() + random.nextFloat() - 0.5);
                        }
                    }
                }
            }
        }
    }
}
