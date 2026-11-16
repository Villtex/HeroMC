package harkenscythe.altar;

import harkenscythe.HSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEntityHSSoulAltar / TileEntityHSBloodAltar: an enchanting table (book animation of TileEntityEnchantmentTable)
 * with one slot. The slot is the altar's slot in the GUI; closing the GUI gives its item back to the player, breaking
 * the altar drops it.
 */
public class AltarBlockEntity extends BlockEntity implements Container {
    private static final RandomSource RANDOM = RandomSource.create();
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(HSBlocks.ALTAR_ENTITY.get(), pos, state);
    }

    /** TileEntityEnchantmentTable.updateEntity (client): the book opens and turns to the nearest player within 3 blocks. */
    public static void bookAnimationTick(Level level, BlockPos pos, BlockState state, AltarBlockEntity entity) {
        entity.oOpen = entity.open;
        entity.oRot = entity.rot;
        Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);
        if (player != null) {
            double xd = player.getX() - (pos.getX() + 0.5);
            double zd = player.getZ() - (pos.getZ() + 0.5);
            entity.tRot = (float) Mth.atan2(zd, xd);
            entity.open += 0.1F;
            if (entity.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float old = entity.flipT;
                do {
                    entity.flipT += RANDOM.nextInt(4) - RANDOM.nextInt(4);
                } while (old == entity.flipT);
            }
        } else {
            entity.tRot += 0.02F;
            entity.open -= 0.1F;
        }
        entity.rot = wrap(entity.rot);
        entity.tRot = wrap(entity.tRot);
        float rotDir = wrap(entity.tRot - entity.rot);
        entity.rot += rotDir * 0.4F;
        entity.open = Mth.clamp(entity.open, 0.0F, 1.0F);
        entity.time++;
        entity.oFlip = entity.flip;
        float diff = Mth.clamp((entity.flipT - entity.flip) * 0.4F, -0.2F, 0.2F);
        entity.flipA += (diff - entity.flipA) * 0.9F;
        entity.flip += entity.flipA;
    }

    private static float wrap(float angle) {
        while (angle >= (float) Math.PI) {
            angle -= (float) (Math.PI * 2);
        }
        while (angle < (float) -Math.PI) {
            angle += (float) (Math.PI * 2);
        }
        return angle;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items.set(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.items.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, count);
        if (!stack.isEmpty()) {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        this.setChanged();
    }

    /** isStackValidForSlot: nothing goes in by automation. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.set(0, ItemStack.EMPTY);
    }
}
