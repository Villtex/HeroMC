package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;
import tconstruct.TSmeltery;
import tconstruct.blocks.TableBlockEntity;
import tconstruct.items.PatternItem;

/**
 * CastingTableLogic / CastingBasinLogic: slot 0 holds the cast, slot 1 the result. Liquid poured in (by a faucet) is
 * taken up to the amount of the matching casting recipe (for a pattern-shaped cast on the table: its material cost,
 * 72 mB per half ingot); once full it cools for the recipe's time and turns into the result, using up the cast if the
 * recipe says so. Hoppers may put casts into slot 0 (while empty of liquid) and take results from slot 1.
 */
public class CastingBlockEntity extends TableBlockEntity implements WorldlyContainer, LiquidContainer {
    public final boolean basin;
    public FluidStack liquid = FluidStack.EMPTY;
    private int castingDelay;
    private int capacity;
    /** renderOffset: what of the liquid is still pouring in (for the renderer). */
    public int renderOffset;

    public CastingBlockEntity(BlockPos pos, BlockState state) {
        super(TSmeltery.CASTING_ENTITY.get(), pos, state, 2);
        this.basin = state.getBlock() == TSmeltery.CASTING_BASIN.get();
    }

    private SmelteryRecipes.@Nullable Casting recipe(FluidStack metal) {
        ItemStack cast = this.items.get(0);
        return this.basin ? SmelteryRecipes.basinCasting(metal, cast) : SmelteryRecipes.tableCasting(metal, cast);
    }

    /** updateCapacity(capacity). */
    private int updateCapacity(int recipeAmount) {
        ItemStack cast = this.items.get(0);
        if (cast.getItem() instanceof PatternItem pattern) {
            int cost = pattern.cost();
            if (this.basin) {
                return (int) (144 * (cost * 0.5));
            }
            if (cost > 0) {
                return (int) (144 * (cost * 0.5));
            }
        }
        return recipeAmount;
    }

    @Override
    public int fill(@Nullable Direction from, FluidStack resource, boolean doFill) {
        if (resource.isEmpty()) {
            return 0;
        }
        if (this.liquid.isEmpty()) {
            SmelteryRecipes.Casting recipe = this.recipe(resource);
            if (recipe == null) {
                return 0;
            }
            this.capacity = this.updateCapacity(recipe.amount());
            if (!this.items.get(1).isEmpty()) {
                return 0;
            }
            int amount = Math.min(resource.getAmount(), this.capacity);
            if (doFill) {
                if (amount == this.capacity) {
                    this.castingDelay = recipe.coolTime();
                }
                this.renderOffset = amount;
                this.liquid = resource.copyWithAmount(amount);
                this.sync();
            }
            return amount;
        }
        if (!FluidStack.isSameFluidSameComponents(resource, this.liquid)) {
            return 0;
        }
        if (resource.getAmount() + this.liquid.getAmount() >= this.capacity) {
            int roomInTank = this.capacity - this.liquid.getAmount();
            if (doFill && roomInTank > 0) {
                this.renderOffset = roomInTank;
                SmelteryRecipes.Casting recipe = this.recipe(this.liquid);
                this.castingDelay = recipe == null ? -1 : recipe.coolTime();
                this.liquid.setAmount(this.capacity);
                this.sync();
            }
            return roomInTank;
        }
        if (doFill) {
            this.liquid.grow(resource.getAmount());
            this.sync();
        }
        return resource.getAmount();
    }

    /** drain: only before the liquid set (castingDelay running). */
    @Override
    public FluidStack drain(@Nullable Direction from, int maxDrain, boolean doDrain) {
        if (this.liquid.isEmpty() || this.castingDelay > 0) {
            return FluidStack.EMPTY;
        }
        int used = Math.min(maxDrain, this.liquid.getAmount());
        FluidStack drained = this.liquid.copyWithAmount(used);
        if (doDrain) {
            this.liquid.shrink(used);
            if (this.liquid.isEmpty()) {
                this.liquid = FluidStack.EMPTY;
            }
            this.sync();
        }
        return drained;
    }

    @Override
    public FluidStack liquid() {
        return this.liquid;
    }

    @Override
    public int capacity() {
        return this.capacity;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CastingBlockEntity cast) {
        if (cast.castingDelay > 0) {
            cast.castingDelay--;
            if (cast.castingDelay == 0) {
                cast.castLiquid();
            }
        }
        if (cast.renderOffset > 0) {
            cast.renderOffset = Math.max(0, cast.renderOffset - 6);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CastingBlockEntity cast) {
        if (cast.renderOffset > 0) {
            cast.renderOffset = Math.max(0, cast.renderOffset - 6);
        }
    }

    private void castLiquid() {
        SmelteryRecipes.Casting recipe = this.recipe(this.liquid);
        if (recipe != null) {
            this.items.set(1, recipe.result());
            if (recipe.consumeCast()) {
                this.items.set(0, ItemStack.EMPTY);
            }
            this.liquid = FluidStack.EMPTY;
            this.sync();
        }
    }

    /** What the player gets back (SearedBlock.activateCastingTable). */
    public boolean hasLiquid() {
        return !this.liquid.isEmpty();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void sync() {
        this.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && this.liquid.isEmpty();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0, 1};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return this.liquid.isEmpty() && slot == 0 && this.items.get(0).isEmpty();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == 1;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.liquid.isEmpty()) {
            output.store("Liquid", FluidStack.CODEC, this.liquid);
        }
        output.putInt("Capacity", this.capacity);
        output.putInt("CastingDelay", this.castingDelay);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.liquid = input.read("Liquid", FluidStack.CODEC).orElse(FluidStack.EMPTY);
        this.capacity = input.getIntOr("Capacity", 0);
        this.castingDelay = input.getIntOr("CastingDelay", 0);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
