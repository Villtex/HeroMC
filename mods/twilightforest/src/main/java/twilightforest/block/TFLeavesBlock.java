package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockTFLeaves and BlockTFMagicLeaves: leaves that decay like vanilla leaves, light opacity 2. The drops (a TF
 * sapling 1 in 40, mangrove 1 in 20; the magic leaves drop vanilla saplings like BlockLeaves) are in the loot tables,
 * the colours (colorMultiplier) on the client.
 */
public class TFLeavesBlock extends TintedParticleLeavesBlock {
    public enum Kind {
        /** Twilight oak, canopy, mangrove, rainbow oak. */
        PLAIN,
        /** Leaves of the tree of time, the miner's tree and the sorting tree. */
        MAGIC,
        /** Leaves of the tree of transformation: runes sparkle on their free faces. */
        RUNES
    }

    private final Kind kind;

    public TFLeavesBlock(Kind kind, Properties properties) {
        super(0.01F, properties);
        this.kind = kind;
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 2;
    }

    /** BlockTFMagicLeaves.sparkleRunes: one rune just outside a random free face (side 0 is never picked). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (this.kind != Kind.RUNES) {
            return;
        }
        double offset = 0.0625;
        int side = random.nextInt(5) + 1;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        double rx = x + random.nextFloat();
        double ry = y + random.nextFloat();
        double rz = z + random.nextFloat();
        if (side == 1 && level.isEmptyBlock(pos.below())) {
            ry = y - offset;
        }
        if (side == 2 && level.isEmptyBlock(pos.relative(Direction.SOUTH))) {
            rz = z + 1 + offset;
        }
        if (side == 3 && level.isEmptyBlock(pos.relative(Direction.NORTH))) {
            rz = z - offset;
        }
        if (side == 4 && level.isEmptyBlock(pos.relative(Direction.EAST))) {
            rx = x + 1 + offset;
        }
        if (side == 5 && level.isEmptyBlock(pos.relative(Direction.WEST))) {
            rx = x - offset;
        }
        if (rx < x || rx > x + 1 || ry < y || ry > y + 1 || rz < z || rz > z + 1) {
            // "leafrune" (EntityTFLeafRuneFX, an enchanting glyph): the vanilla glyph particle
            level.addParticle(ParticleTypes.ENCHANT, rx, ry, rz, 0.0, 0.0, 0.0);
        }
    }
}
