package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;

/**
 * BlockCamoPlate / BlockCamoPlateWeighted: a camo block that acts as a pressure plate for what stands on it (the
 * block space above): any entity, players only, or items weighed up to 64 (light) or 640 (heavy). It checks every
 * tick while off and every 20 ticks while on.
 */
public class CamoPlateBlock extends CamoFullBlock {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public enum Sense { ALL, PLAYERS, ITEMS }

    private final Sense sense;
    private final int maxWeight;

    public CamoPlateBlock(Properties properties, Sense sense, int maxWeight) {
        super(properties);
        this.sense = sense;
        this.maxWeight = maxWeight;
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 1);
        }
    }

    private int weight(Level level, BlockPos pos) {
        AABB box = new AABB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
        switch (this.sense) {
            case ITEMS -> {
                int l = 0;
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
                    l += item.getItem().getCount();
                    if (l >= this.maxWeight) {
                        break;
                    }
                }
                return l <= 0 ? 0 : Mth.ceil((float) Math.min(this.maxWeight, l) / this.maxWeight * 15.0F);
            }
            case PLAYERS -> {
                for (Player p : level.getEntitiesOfClass(Player.class, box)) {
                    if (!p.isIgnoringBlockTriggers()) {
                        return 15;
                    }
                }
                return 0;
            }
            default -> {
                for (Entity e : level.getEntities((Entity) null, box)) {
                    if (!e.isIgnoringBlockTriggers()) {
                        return 15;
                    }
                }
                return 0;
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int old = state.getValue(POWER);
        int weight = this.weight(level, pos);
        if (old != weight) {
            BlockState now = state.setValue(POWER, weight);
            level.setBlock(pos, now, 2);
            CamoRedstone.notifyAround(level, pos, now);
        }
        if (weight == 0 && old > 0) {
            CamoRedstone.click(level, pos, 0.1, 0.5F);
        } else if (weight > 0 && old == 0) {
            CamoRedstone.click(level, pos, 0.1, 0.6F);
        }
        level.scheduleTick(pos, this, weight > 0 ? 20 : 1);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        if (state.getValue(POWER) > 0) {
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
            level.updateNeighborsAt(pos.above(), this);
        }
    }
}
