package projectzulu.block.gear;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import projectzulu.ZuluMonsters;
import projectzulu.entity.MummyPharaohEntity;

/**
 * BlockJasper: the summoning stone of the mummy pharaoh. Placed with a torch cross around it (the eight neighbours
 * and one complete arm of five torches, under open sky) it whispers, ticks every game tick, raises a sandstone altar
 * from second 5 to 35 (one block per second) and after 800 ticks roars and spawns the pharaoh three blocks above.
 * Without the torches it pops off as an item. A jasper that is not preparing a summon removes itself on the next
 * (random) tick.
 *
 * <p>Like the original the summon state lives in the block object, not per position: one ritual at a time, and a
 * second jasper placed meanwhile resets the counter.
 */
public class JasperBlock extends Block {
    /** alterBlockLocations of the original, built in this order (one per second). */
    private static final int[][] ALTAR = {
            {2, 0, -1}, {2, 0, 0}, {2, 0, 1}, {1, 0, -2}, {1, 0, -1}, {1, 0, 0}, {1, 0, 1}, {1, 0, 2},
            {0, 0, -2}, {0, 0, -1}, {0, 0, 1}, {0, 0, 2}, {-1, 0, -2}, {-1, 0, -1}, {-1, 0, 0}, {-1, 0, 1}, {-1, 0, 2},
            {-2, 0, -1}, {-2, 0, 0}, {-2, 0, 1}, {1, 1, -1}, {1, 1, 0}, {1, 1, 1}, {0, 1, -1}, {0, 1, 0}, {0, 1, 1},
            {-1, 1, -1}, {-1, 1, 0}, {-1, 1, 1}, {0, 2, 0}};

    private boolean prepareToSummonBoss;
    private int counter;

    public JasperBlock(Properties properties) {
        super(properties);
    }

    private static boolean torch(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH);
    }

    /** The four checks of onBlockAdded: the eight neighbours plus one arm each, the count resets below 13. */
    private static boolean torchCross(Level level, int x, int y, int z) {
        int[][] ring = {{x + 1, z}, {x + 1, z + 1}, {x + 1, z - 1}, {x, z - 1}, {x, z + 1}, {x - 1, z}, {x - 1, z + 1}, {x - 1, z - 1}};
        int[][][] arms = {
                {{x - 2, z}, {x - 3, z}, {x - 3, z + 1}, {x - 3, z - 1}, {x - 4, z}},
                {{x + 2, z}, {x + 3, z}, {x + 3, z + 1}, {x + 3, z - 1}, {x + 4, z}},
                {{x, z + 2}, {x, z + 3}, {x + 1, z + 3}, {x - 1, z + 3}, {x, z + 4}},
                {{x, z - 2}, {x, z - 3}, {x + 1, z - 3}, {x - 1, z - 3}, {x, z - 4}}};
        int count = 0;
        for (int[][] arm : arms) {
            for (int[] p : ring) {
                if (torch(level, p[0], y, p[1])) {
                    count++;
                }
            }
            for (int[] p : arm) {
                if (torch(level, p[0], y, p[1])) {
                    count++;
                }
            }
            if (count < 13) {
                count = 0;
            }
        }
        return count >= 13;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide() || oldState.is(this)) {
            return;
        }
        this.counter = 0;
        if (torchCross(level, pos.getX(), pos.getY(), pos.getZ()) && level.canSeeSky(pos.above())) {
            // playRecord("streaming.summonwhispers") of the original (only heard in single player)
            level.playSound(null, pos, ZuluMonsters.SUMMON_WHISPERS.get(), SoundSource.RECORDS, 4.0F, 1.0F);
            level.scheduleTick(pos, this, 1);
            this.counter++;
            this.prepareToSummonBoss = true;
        } else {
            popResource(level, pos, new ItemStack(this));
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // setTickRandomly(true): random ticks run updateTick as well
        this.tick(state, level, pos, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.prepareToSummonBoss) {
            level.removeBlock(pos, false);
        }
        int seconds = Mth.floor(this.counter / 20);
        if (seconds >= 5 && seconds < 35) {
            int[] offset = ALTAR[seconds - 5];
            level.setBlockAndUpdate(pos.offset(offset[0], offset[1], offset[2]), Blocks.SANDSTONE.defaultBlockState());
        }
        if (this.counter < 800) {
            level.scheduleTick(pos, this, 1);
            this.counter++;
        } else {
            level.playSound(null, pos, ZuluMonsters.MUMMY_ROAR.get(), SoundSource.HOSTILE, 10.0F, 1.0F);
            MummyPharaohEntity pharaoh = ZuluMonsters.MUMMY_PHARAOH.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (pharaoh != null) {
                pharaoh.setSummoned();
                pharaoh.snapTo(pos.getX(), pos.getY() + 3, pos.getZ(), 1.0F, 1.0F);
                pharaoh.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.MOB_SUMMONED, null);
                level.addFreshEntity(pharaoh);
            }
            level.removeBlock(pos, false);
        }
    }
}
