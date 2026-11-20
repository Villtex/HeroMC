package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;
import twilightforest.TFBlocks;

/**
 * BlockTFNagastone: the stone serpents of the naga courtyard. 1.5.2 metadata & 12 = type (0 head, 4 body bending
 * down, 8 body bending up, 12 straight body / turn), & 3 = orientation. The head (metadata 0..3) and the body
 * (4..15) are two blocks here, each with {@link #META} = the old metadata. Placing one and every neighbour change
 * fit it to the stones around it (onBlockPlacedBy, placePerfectFitHead/Body), as in the original.
 */
public class NagastoneBlock extends Block {
    public static final IntegerProperty HEAD_META = IntegerProperty.create("meta", 0, 3);
    public static final IntegerProperty BODY_META = IntegerProperty.create("meta", 4, 15);
    private static final ThreadLocal<Boolean> CONSTRUCTING = new ThreadLocal<>();

    private final boolean head;
    public final IntegerProperty META;

    public NagastoneBlock(boolean head, Properties properties) {
        super(remember(head, properties));
        CONSTRUCTING.remove();
        this.head = head;
        this.META = head ? HEAD_META : BODY_META;
        // the creative items of the original: head metadata 1, body 13
        this.registerDefaultState(this.stateDefinition.any().setValue(this.META, head ? 1 : 13));
    }

    private static Properties remember(boolean head, Properties properties) {
        CONSTRUCTING.set(head);
        return properties;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(Boolean.TRUE.equals(CONSTRUCTING.get()) ? HEAD_META : BODY_META);
    }

    public static BlockState fromMeta(int meta) {
        return meta < 4 ? TFBlocks.NAGASTONE_HEAD.get().defaultBlockState().setValue(HEAD_META, meta)
                : TFBlocks.NAGASTONE_BODY.get().defaultBlockState().setValue(BODY_META, meta);
    }

    private static void setMeta(Level level, BlockPos pos, int meta) {
        level.setBlock(pos, fromMeta(meta), Block.UPDATE_ALL);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide() || placer == null) {
            return;
        }
        int metadata = this.head ? 0 : 12;
        int direction = 0;
        if (metadata > 0 && placePerfectFitBody(level, pos) || metadata == 0 && placePerfectFitHead(level, pos)) {
            return;
        }
        int pistonOrient = pistonOrientation(pos, placer);
        switch (pistonOrient) {
            case 0, 1 -> direction = 2;
            case 2, 3 -> direction = 1;
            default -> direction = 0;
        }
        if (metadata > 0 && (direction == 0 || direction == 1) && isNagastone(level, pos.below())) {
            metadata = 4;
        }
        if (metadata > 0 && (direction == 0 || direction == 1) && isNagastone(level, pos.above())) {
            metadata = 8;
        }
        if (metadata == 0 || metadata == 4 || metadata == 8) {
            direction = determineOrientation(placer);
        }
        if (metadata > 0 && direction == 2) {
            int connect = getOnlyNSEWConnection(level, pos);
            if (connect != -1) {
                metadata = pistonOrient == 0 ? 4 : 8;
                direction = connect;
            }
        }
        if (metadata == 12) {
            int connect = getOnlyConnection(level, pos);
            if (connect != -1) {
                direction = switch (connect) {
                    case 0, 1 -> 1;
                    case 2, 3 -> 0;
                    default -> 2;
                };
            }
        }
        setMeta(level, pos, metadata | direction);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }
        if (this.head) {
            placePerfectFitHead(level, pos);
        } else {
            placePerfectFitBody(level, pos);
        }
    }

    private static boolean placePerfectFitBody(Level level, BlockPos pos) {
        int first = getPossibleConnection(level, pos, -1, -1);
        int second = getPossibleConnection(level, pos, first, -1);
        if (first == -1 || second == -1) {
            return false;
        }
        if (getPossibleConnection(level, pos, first, second) != -1) {
            return false;
        }
        if (first == 2 && second == 3 || first == 3 && second == 2) {
            setMeta(level, pos, 12);
        } else if (first + second == 1) {
            setMeta(level, pos, 13);
        } else if (first + second == 9) {
            setMeta(level, pos, 14);
        } else if (first == 4) {
            setMeta(level, pos, 4 | second);
        } else if (first == 5) {
            setMeta(level, pos, 8 | second);
        } else if (second == 4) {
            setMeta(level, pos, 4 | first);
        } else if (second == 5) {
            setMeta(level, pos, 8 | first);
        } else {
            setMeta(level, pos, 15);
        }
        return true;
    }

    private static boolean placePerfectFitHead(Level level, BlockPos pos) {
        int connect = getOnlyNSEWConnection(level, pos);
        int meta = switch (connect) {
            case 0 -> 1;
            case 1 -> 0;
            case 2 -> 3;
            case 3 -> 2;
            default -> -1;
        };
        if (meta < 0) {
            return false;
        }
        setMeta(level, pos, meta);
        return true;
    }

    private static int getOnlyNSEWConnection(Level level, BlockPos pos) {
        int first = getPossibleConnection(level, pos, -1, -1);
        int second = getPossibleConnection(level, pos, first, -1);
        return first <= 0 || first >= 4 || second >= 0 && second <= 3 ? -1 : first;
    }

    private static int getOnlyConnection(Level level, BlockPos pos) {
        int first = getPossibleConnection(level, pos, -1, -1);
        int second = getPossibleConnection(level, pos, first, -1);
        return first > 0 && first < 6 && second == -1 ? first : -1;
    }

    private static int getPossibleConnection(Level level, BlockPos pos, int exclude1, int exclude2) {
        for (int i = 0; i < 6; i++) {
            if (i != exclude1 && i != exclude2 && isNagastone(level, neighbour(pos, i))) {
                return i;
            }
        }
        return -1;
    }

    /** Directions of isNagaStoneInDirection: 0 z-1, 1 z+1, 2 x-1, 3 x+1, 4 y-1, 5 y+1. */
    private static BlockPos neighbour(BlockPos pos, int direction) {
        return switch (direction) {
            case 0 -> pos.north();
            case 1 -> pos.south();
            case 2 -> pos.west();
            case 3 -> pos.east();
            case 4 -> pos.below();
            default -> pos.above();
        };
    }

    private static boolean isNagastone(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof NagastoneBlock;
    }

    /** BlockTFNagastone.determineOrientation: the player's yaw to 0, 3, 1, 2. */
    private static int determineOrientation(LivingEntity placer) {
        int rot = Mth.floor(placer.getYRot() * 4.0F / 360.0F + 0.5) & 3;
        return switch (rot) {
            case 1 -> 3;
            case 2 -> 1;
            case 3 -> 2;
            default -> 0;
        };
    }

    /** BlockPistonBase.determineOrientation: 1 when placed from far above, 0 from below, else the horizontal side. */
    private static int pistonOrientation(BlockPos pos, LivingEntity placer) {
        if (Math.abs(placer.getX() - (pos.getX() + 0.5F)) < 2.0F && Math.abs(placer.getZ() - (pos.getZ() + 0.5F)) < 2.0F) {
            double eye = placer.getEyeY();
            if (eye - pos.getY() > 2.0) {
                return 1;
            }
            if (pos.getY() - eye > 0.0) {
                return 0;
            }
        }
        int l = Mth.floor(placer.getYRot() * 4.0F / 360.0F + 0.5) & 3;
        return switch (l) {
            case 0 -> 2;
            case 1 -> 5;
            case 2 -> 3;
            default -> 4;
        };
    }
}
