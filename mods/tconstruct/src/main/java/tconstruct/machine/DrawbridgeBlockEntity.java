package tconstruct.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import tconstruct.TDeco;

import java.util.Set;

/**
 * DrawbridgeLogic: slot 0 the blocks to lay, slot 1 the camouflage. While powered it lays one block every 5 ticks in
 * front of it (up to 15, keeping one in the slot), stopping at the first block in the way; unpowered it takes them back,
 * last first, as long as the block there is the one in the slot and the slot has room. The blocks are put down as by a
 * player looking in the chosen placement direction (the arrows of the GUI, 4 = straight on).
 */
public class DrawbridgeBlockEntity extends CamoBlockEntity {
    /** drawbridgeState 2 of 1.5.2: never taken back. */
    private static final Set<Block> NEVER = Set.of(Blocks.BEDROCK, Blocks.PISTON_HEAD, Blocks.MOVING_PISTON, Blocks.SPAWNER, Blocks.WHEAT,
            Blocks.CACTUS, Blocks.NETHER_PORTAL, Blocks.CAKE, Blocks.PUMPKIN_STEM, Blocks.MELON_STEM, Blocks.NETHER_WART, Blocks.END_PORTAL,
            Blocks.COCOA, Blocks.CARROTS, Blocks.POTATOES);

    boolean active;
    boolean working;
    int ticks;
    int extension;
    public int placement = 4;

    public DrawbridgeBlockEntity(BlockPos pos, BlockState state) {
        super(TDeco.DRAWBRIDGE_ENTITY.get(), pos, state, 2);
    }

    @Override
    protected int camoSlot() {
        return 1;
    }

    /** canDropInventorySlot: nothing, the drawbridge item keeps its contents. */
    @Override
    protected boolean canDrop(int slot) {
        return false;
    }

    public boolean hasExtended() {
        return this.extension != 0;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.working = true;
        this.setChanged();
    }

    private Direction direction() {
        return this.getBlockState().getValue(DrawbridgeBlock.FACING);
    }

    /** mapDirection: north 0, east 1, south 2, west 3. */
    private int mapDirection() {
        return switch (this.direction()) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            default -> 3;
        };
    }

    /** setPlacementDirection: the yaw and pitch of the fake player. */
    private float[] rotation() {
        int keycode = this.placement;
        Direction direction = this.direction();
        if (keycode == 4) {
            return new float[]{0.0F, 0.0F};
        }
        if (direction.getAxis() == Direction.Axis.Y) {
            return new float[]{keycode * 90.0F, direction == Direction.DOWN ? -90.0F : 90.0F};
        }
        if (keycode == 0) {
            return new float[]{this.mapDirection() * 90, 90.0F};
        }
        if (keycode == 2) {
            int face = this.mapDirection() + 2;
            if (face > 3) {
                face -= 4;
            }
            return new float[]{face * 90, -90.0F};
        }
        int facing = this.mapDirection() + (keycode == 1 ? 1 : -1);
        if (facing >= 4) {
            facing = 0;
        }
        if (facing < 0) {
            facing = 3;
        }
        return new float[]{facing * 90, 0.0F};
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DrawbridgeBlockEntity bridge) {
        bridge.tick((ServerLevel) level, pos);
    }

    private void tick(ServerLevel level, BlockPos pos) {
        if (!this.working) {
            return;
        }
        if (++this.ticks != 5) {
            return;
        }
        this.ticks = 0;
        ItemStack stack = this.items.get(0);
        Direction direction = this.direction();
        if (this.active) {
            if (!stack.isEmpty() && stack.getCount() > 1 && this.extension < 15) {
                this.extension++;
                BlockPos at = pos.relative(direction, this.extension);
                BlockState there = level.getBlockState(at);
                if (!there.isAir() && !there.canBeReplaced()) {
                    this.extension--;
                    this.working = false;
                } else {
                    if (!(stack.getItem() instanceof BlockItem item)) {
                        this.extension--;
                        return;
                    }
                    this.place(level, at, direction, item, stack);
                    level.playSound(null, at, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.25F, level.getRandom().nextFloat() * 0.25F + 0.6F);
                    stack.shrink(1);
                }
            } else {
                this.working = false;
            }
        } else if ((stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) && this.extension > 0) {
            BlockPos at = pos.relative(direction, this.extension);
            BlockState there = level.getBlockState(at);
            if (!there.isAir()) {
                if (!stack.isEmpty() && !NEVER.contains(there.getBlock()) && there.getBlock().asItem() == stack.getItem()) {
                    level.playSound(null, at, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.25F, level.getRandom().nextFloat() * 0.15F + 0.6F);
                    if (level.setBlock(at, Blocks.AIR.defaultBlockState(), 3)) {
                        stack.grow(1);
                    }
                } else {
                    this.working = false;
                }
            }
            this.extension--;
        } else {
            this.working = false;
        }
        this.setChanged();
    }

    /** placeBlockAt: the state the block takes for a player looking the placement way, against the block before it. */
    private void place(ServerLevel level, BlockPos at, Direction direction, BlockItem item, ItemStack stack) {
        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        float[] rotation = this.rotation();
        player.snapTo(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, rotation[0], rotation[1]);
        Vec3 hit = Vec3.atCenterOf(at).relative(direction.getOpposite(), 0.5);
        BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stack.copyWithCount(1),
                new BlockHitResult(hit, direction, at.relative(direction.getOpposite()), false));
        BlockState state = item.getBlock().getStateForPlacement(context);
        if (state == null) {
            state = item.getBlock().defaultBlockState();
        }
        if (level.setBlock(at, state, 3)) {
            state.getBlock().setPlacedBy(level, at, state, player, stack);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Active", this.active);
        output.putBoolean("Working", this.working);
        output.putInt("Extension", this.extension);
        output.putInt("Placement", this.placement);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.active = input.getBooleanOr("Active", false);
        this.working = input.getBooleanOr("Working", false);
        this.extension = input.getIntOr("Extension", 0);
        this.placement = input.getIntOr("Placement", 4);
    }

    /** RedstoneMachine.removeBlockByPlayer: the item keeps Contents, Camoflauge and Placement. */
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!this.isEmpty()) {
            components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.items));
        }
        if (this.placement != 4) {
            components.set(TDeco.PLACEMENT.get(), this.placement);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        NonNullList<ItemStack> list = NonNullList.withSize(2, ItemStack.EMPTY);
        components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(list);
        for (int i = 0; i < 2; i++) {
            this.items.set(i, list.get(i));
        }
        this.placement = components.getOrDefault(TDeco.PLACEMENT.get(), 4);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard("Items");
        output.discard("Placement");
    }
}
