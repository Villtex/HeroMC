package bibliocraft.display;

import bibliocraft.BiblioCraft;
import bibliocraft.common.BiblioBlock;
import bibliocraft.common.BiblioMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockWeaponCase ("Glass Case", metadata = wood * 4 + cloth): a glass-lidded case for one stack. Angles 0..3 lie on
 * the floor, 4..7 hang on the wall (placed while looking roughly level). Sneak-click opens or closes the lid (sounds
 * copen/cclose). With the lid open: a held item goes in, an empty hand opens the GUI if the case is empty, and a full
 * case drops its item in front. A block of redstone inside powers the neighbours with 15 (not behind the back).
 * The light is that of the shown block.
 */
public class WeaponCaseBlock extends BiblioBlock {
    /** TileEntityWeaponCase.caseAngle: 0..3 like the other blocks, + 4 for the wall-mounted case. */
    public static final IntegerProperty ANGLE = IntegerProperty.create("angle", 0, 7);
    /** setBlockBoundsBasedOnState. */
    private static final VoxelShape[] SHAPES = {
            box(0.96, 0, 0, 15.04, 8, 16), box(0, 0, 0.96, 16, 8, 15.04), box(0.96, 0, 0, 15.04, 8, 16), box(0, 0, 0.96, 16, 8, 15.04),
            box(8, 0, 0.96, 16, 16, 15.04), box(0.96, 0, 8, 15.04, 16, 16), box(0, 0, 0.96, 8, 16, 15.04), box(0.96, 0, 0, 15.04, 16, 8)};
    /** Wood 0..3 (case texture weaponcase1..4) and cloth 0..3 (red, blue, green, black: weaponcaseinner1..4). */
    public final int wood;
    public final int cloth;

    public WeaponCaseBlock(int wood, int cloth, Properties properties) {
        super(false, properties);
        this.wood = wood;
        this.cloth = cloth;
        this.registerDefaultState(this.stateDefinition.any().setValue(ANGLE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ANGLE);
    }

    public static int caseAngle(BlockState state) {
        return state.hasProperty(ANGLE) ? state.getValue(ANGLE) : 0;
    }

    /**
     * onBlockPlacedBy: the angle from the yaw as usual; {@code ((floor(pitch * 2 / 180 + 0.5) & 3) + 1) % 4 == 1}
     * (pitch -45..45, looking level) hangs the case on the wall (angle + 4).
     */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        int angle = 0;
        if (player != null) {
            angle = BiblioBlock.angle(player);
            int angle2 = Mth.floor(player.getXRot() * 2.0F / 180.0F + 0.5) & 3;
            if ((angle2 + 1) % 4 == 1) {
                angle += 4;
            }
        }
        return this.defaultBlockState().setValue(ANGLE, angle);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WeaponCaseBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[caseAngle(state)];
    }

    @Override
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    /** getLightValue: Block.lightValue of the block in the case. */
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return DisplayLight.get(level, pos);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WeaponCaseBlockEntity be) {
            this.activate(level, pos, state, player, held, be);
        }
        return InteractionResult.SUCCESS;
    }

    /** onBlockActivated (server). Sneaking with an item in hand never gets here (as in 1.5.2). */
    private void activate(Level level, BlockPos pos, BlockState state, Player player, ItemStack held, WeaponCaseBlockEntity be) {
        if (!player.isShiftKeyDown() && be.isLidOpen()) {
            if (!held.isEmpty() && !be.isSlotFull() && be.setCaseSlot(held)) {
                player.getInventory().setItem(player.getInventory().getSelectedSlot(), ItemStack.EMPTY);
                return;
            }
            if (held.isEmpty() && !be.isSlotFull()) {
                if (player instanceof ServerPlayer sp) {
                    BiblioMenu.open(sp, be, Component.literal("Glass Case"), WeaponCaseMenu::new);
                }
                return;
            }
            if (be.isSlotFull()) {
                this.dropCaseItem(level, pos, state, be);
                be.setCaseSlot(ItemStack.EMPTY);
                return;
            }
        }
        if (player.isShiftKeyDown()) {
            // world.playSoundAtEntity(player, ...): heard by everyone near the player
            boolean open = !be.isLidOpen();
            be.setLidOpen(open);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), open ? BiblioCraft.CASE_OPEN.get() : BiblioCraft.CASE_CLOSE.get(),
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    /** dropCaseItem: the item falls out 0.4 in front of the case at height 0.7, without motion. */
    private void dropCaseItem(Level level, BlockPos pos, BlockState state, WeaponCaseBlockEntity be) {
        ItemStack stuff = be.getCaseStack();
        if (stuff.isEmpty()) {
            return;
        }
        int angle = caseAngle(state) % 4;
        float iAdjust = angle == 0 ? -0.4F : angle == 2 ? 0.4F : 0.0F;
        float kAdjust = angle == 1 ? -0.4F : angle == 3 ? 0.4F : 0.0F;
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5F + iAdjust, pos.getY() + 0.7F, pos.getZ() + 0.5F + kAdjust, stuff.copy(), 0.0, 0.0,
                0.0);
        level.addFreshEntity(entity);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    /** isProvidingWeakPower: 15 with a block of redstone inside, not to the side behind the back (face 5, 3, 4, 2). */
    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof WeaponCaseBlockEntity be) || !be.hasRedstone()) {
            return 0;
        }
        int back = switch (caseAngle(state) % 4) {
            case 0 -> 5;
            case 1 -> 3;
            case 2 -> 4;
            default -> 2;
        };
        return direction.get3DDataValue() == back ? 0 : 15;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSignal(state, level, pos, direction);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        DisplayLight.remove(level, pos);
        level.updateNeighborsAt(pos, this, null);
        for (Direction d : Direction.values()) {
            level.updateNeighborsAt(pos.relative(d), this, null);
        }
    }
}
