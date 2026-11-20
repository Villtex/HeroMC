package tconstruct.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import tconstruct.TBlocks;

import java.util.Optional;

/**
 * FrypanLogic: slot 0 holds the pan, slot 1 the fuel, slots 2..9 the food. With something to cook it burns fuel (2.5
 * times its frying pan burn time) and every 1000 ticks of fire it cooks one of each kind of food (smelting results that
 * are food) into free room among the eight slots.
 */
public class FrypanBlockEntity extends TableBlockEntity {
    public int fuel;
    public int fuelGague;
    public int progress;
    /** fuel, fuelGague, progress for the menu. */
    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> FrypanBlockEntity.this.fuel;
                case 1 -> FrypanBlockEntity.this.fuelGague;
                default -> FrypanBlockEntity.this.progress;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> FrypanBlockEntity.this.fuel = value;
                case 1 -> FrypanBlockEntity.this.fuelGague = value;
                default -> FrypanBlockEntity.this.progress = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public FrypanBlockEntity(BlockPos pos, BlockState state) {
        super(TBlocks.FRYPAN_ENTITY.get(), pos, state, 10);
    }

    /** EquipLogic.setEquipmentItem. */
    public void setEquipmentItem(ItemStack stack) {
        this.items.set(0, stack.copy());
        this.setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FrypanBlockEntity pan) {
        pan.tick((ServerLevel) level, pos, state);
    }

    /** updateEntity. */
    private void tick(ServerLevel level, BlockPos pos, BlockState state) {
        boolean burning = this.isBurning();
        boolean updateInventory = false;
        if (this.fuel <= 0 && this.canCook(level)) {
            ItemStack fuelStack = this.items.get(1);
            this.fuel = this.fuelGague = (int) (this.burnTime(level, fuelStack) * 2.5);
            if (this.fuel > 0) {
                ItemStack remainder = fuelStack.getItem().getCraftingRemainder() == null ? ItemStack.EMPTY
                        : fuelStack.getItem().getCraftingRemainder().create();
                if (!remainder.isEmpty()) {
                    this.items.set(1, remainder);
                } else {
                    fuelStack.shrink(1);
                }
                updateInventory = true;
            }
        }
        if (this.isBurning() && this.canCook(level)) {
            this.progress++;
            if (this.progress >= 1000) {
                this.progress = 0;
                this.cookItems(level);
                updateInventory = true;
            }
        } else {
            this.progress = 0;
        }
        if (this.fuel > 0) {
            this.fuel--;
        }
        if (burning != this.isBurning()) {
            level.setBlock(pos, state.setValue(FrypanBlock.LIT, this.isBurning()), 3);
            updateInventory = true;
        }
        if (updateInventory) {
            this.setChanged();
        }
    }

    public boolean isBurning() {
        return this.fuel > 0;
    }

    /** getResultFor: the smelting result if it is food. */
    private static @Nullable ItemStack resultFor(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        SingleRecipeInput input = new SingleRecipeInput(stack);
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level);
        if (recipe.isEmpty()) {
            return null;
        }
        ItemStack result = recipe.get().value().assemble(input);
        return !result.isEmpty() && result.has(net.minecraft.core.component.DataComponents.FOOD) ? result.copy() : null;
    }

    private boolean canCook(ServerLevel level) {
        for (int id = 2; id < 10; id++) {
            ItemStack result = resultFor(level, this.items.get(id));
            if (result != null) {
                for (int slot = 2; slot < 10; slot++) {
                    ItemStack there = this.items.get(slot);
                    if (there.isEmpty()) {
                        return true;
                    }
                    if (ItemStack.isSameItemSameComponents(there, result) && there.getCount() + result.getCount() <= there.getMaxStackSize()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void cookItems(ServerLevel level) {
        for (int id = 2; id < 10; id++) {
            if (!this.canCook(level)) {
                return;
            }
            ItemStack result = resultFor(level, this.items.get(id));
            if (result == null) {
                continue;
            }
            boolean placed = false;
            for (int slot = 2; slot < 10 && !placed; slot++) {
                ItemStack there = this.items.get(slot);
                if (!there.isEmpty() && ItemStack.isSameItemSameComponents(there, result) && there.getCount() < there.getMaxStackSize()) {
                    if (there.getCount() + result.getCount() <= there.getMaxStackSize()) {
                        there.grow(result.getCount());
                        placed = true;
                    } else {
                        int decrement = there.getMaxStackSize() - there.getCount();
                        there.setCount(there.getMaxStackSize());
                        result.shrink(decrement);
                    }
                }
            }
            for (int slot = 2; !placed && slot < 10; slot++) {
                if (this.items.get(slot).isEmpty()) {
                    this.items.set(slot, result);
                    placed = true;
                }
            }
            if (placed) {
                this.items.get(id).shrink(1);
            }
        }
    }

    /**
     * getItemBurnTime: wooden slabs 150, logs 2400, other wooden blocks 300, wooden tools 200, sticks and saplings 100,
     * coal 800, lava 20000, blaze rods 2400, everything else the furnace value.
     */
    public int burnTime(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() instanceof BlockItem block) {
            BlockState state = block.getBlock().defaultBlockState();
            if (state.is(BlockTags.WOODEN_SLABS)) {
                return 150;
            }
            if (state.is(BlockTags.LOGS)) {
                return 2400;
            }
            if (state.is(BlockTags.MINEABLE_WITH_AXE) && !state.is(BlockTags.SAPLINGS)) {
                return 300;
            }
        }
        if (stack.is(Items.WOODEN_PICKAXE) || stack.is(Items.WOODEN_AXE) || stack.is(Items.WOODEN_SHOVEL)
                || stack.is(Items.WOODEN_SWORD) || stack.is(Items.WOODEN_HOE)) {
            return 200;
        }
        if (stack.is(Items.STICK) || stack.is(ItemTags.SAPLINGS)) {
            return 100;
        }
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) {
            return 800;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            return 20000;
        }
        if (stack.is(Items.BLAZE_ROD)) {
            return 2400;
        }
        return net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.getFromItem(stack,
                net.minecraft.core.component.DataComponents.COOKING_FUEL, net.minecraft.world.item.component.CookingFuel::burnTime,
                this.lootContext(level, stack), 0);
    }

    /** BaseContainerBlockEntity.getLootContext: what a fuel's burn time is computed with. */
    private net.minecraft.world.level.storage.loot.LootContext lootContext(ServerLevel level, ItemStack stack) {
        return new net.minecraft.world.level.storage.loot.LootContext.Builder(new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE, this.getBlockState())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY, this)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.atCenterOf(this.getBlockPos()))
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.CONTAINER, this)
                .withOptionalParameter(net.neoforged.neoforge.common.loot.NeoForgeLootContextParams.QUERIED_STACK, stack)
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CONTAINER_PROCESS))
                .create(Optional.empty());
    }

    /** The pan is not shown in the menu; it drops with the rest. */
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("fuel", this.fuel);
        output.putInt("fuel_gague", this.fuelGague);
        output.putInt("progress", this.progress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.fuel = input.getIntOr("fuel", 0);
        this.fuelGague = input.getIntOr("fuel_gague", 0);
        this.progress = input.getIntOr("progress", 0);
    }
}
