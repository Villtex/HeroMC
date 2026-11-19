package betterstorage.block;

import betterstorage.BS;
import betterstorage.BSConfig;
import betterstorage.backpack.Backpacks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEntityBackpack: a backpack set down as a block. {@link #stack} is the backpack item (colour, name, damage,
 * enchantments), the contents are those of the backpack (9 x backpackRows). Picking it up (sneak + break with an
 * empty chest slot) puts both back on the player.
 */
public class BackpackBlockEntity extends ContainerBlockEntity {
    public ItemStack stack = ItemStack.EMPTY;
    public boolean equipped;
    public boolean brokenInCreative;

    public BackpackBlockEntity(BlockPos pos, BlockState state) {
        super(BS.BACKPACK_ENTITY.get(), pos, state);
    }

    @Override
    public String getName() {
        return "container.betterstorage.backpack";
    }

    @Override
    public int getRows() {
        return BSConfig.backpackRows();
    }

    @Override
    protected float getLidSpeed() {
        return 0.2F;
    }

    /** equip: removes the block (without drops) and puts backpack and contents on the player. */
    public boolean equip(Player player) {
        if (this.level == null) {
            return false;
        }
        this.equipped = true;
        this.equipped = this.level.removeBlock(this.worldPosition, false);
        if (this.equipped) {
            Backpacks.setBackpack(player, this.stack.copy(), List.copyOf(this.contents));
        }
        return this.equipped;
    }

    /** unequip: the worn backpack is set down: its contents move into this block. */
    public void unequip(Player player) {
        if (this.level != null && !this.level.isClientSide()) {
            List<ItemStack> items = Backpacks.contents(player);
            if (items != null) {
                for (int i = 0; i < items.size() && i < this.contents.size(); i++) {
                    this.contents.set(i, items.get(i).copy());
                }
            }
        }
        Backpacks.removeBackpack(player);
    }

    /** updateEntity: the lid sounds (snow steps, opening deep and closing deeper). */
    @Override
    public void tick(Level level) {
        super.tick(level);
        if (level.isClientSide()) {
            return;
        }
        double x = this.worldPosition.getX() + 0.5;
        double y = this.worldPosition.getY() + 0.5;
        double z = this.worldPosition.getZ() + 0.5;
        if (this.lidAngle > 0.0F && this.prevLidAngle <= 0.0F) {
            level.playSound(null, x, y, z, SoundEvents.SNOW_STEP, SoundSource.BLOCKS, 1.0F, 0.5F);
        }
        if (this.lidAngle < 0.2F && this.prevLidAngle >= 0.2F) {
            level.playSound(null, x, y, z, SoundEvents.SNOW_STEP, SoundSource.BLOCKS, 0.8F, 0.3F);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BackpackBlock block) {
            block.onRemoved(this);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.stack = input.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.stack.isEmpty()) {
            output.store("stack", ItemStack.CODEC, this.stack);
        }
    }
}
