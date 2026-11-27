package slimeknights.mantle.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import slimeknights.mantle.inventory.compat.ItemHandlers;
import slimeknights.mantle.inventory.compat.IItemHandlerModifiable;
import slimeknights.mantle.inventory.compat.InvWrapper;
import slimeknights.mantle.util.ItemStackList;

/**
 * Updated version of InventoryLogic in Mantle. Also contains a few bugfixes DOES NOT OVERRIDE createMenu.
 * Port: the item capability is no longer attached by {@code getCapability}; register it for each block entity type
 * with {@link #registerItemCapability(RegisterCapabilitiesEvent, BlockEntityType)} in {@code RegisterCapabilitiesEvent}.
 */
public abstract class InventoryBlockEntity extends NameableBlockEntity implements Container, MenuProvider, Nameable {
  private static final String TAG_INVENTORY_SIZE = "InventorySize";
  private static final String TAG_ITEMS = "Items";

  private NonNullList<ItemStack> inventory;
  /** If true, the inventory size is saved to NBT, false means you are responsible for serializing it if it changes */
  private final boolean saveSizeToNBT;
  protected int stackSizeLimit;
  @Getter
  protected IItemHandlerModifiable itemHandler;

  /**
   * @param name Localization String for the inventory title. Can be overridden through setCustomName
   */
  public InventoryBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state, Component name, boolean saveSizeToNBT, int inventorySize) {
    this(tileEntityTypeIn, pos, state, name, saveSizeToNBT, inventorySize, 64);
  }

  /**
   * @param name Localization String for the inventory title. Can be overridden through setCustomName
   */
  public InventoryBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state, Component name, boolean saveSizeToNBT, int inventorySize, int maxStackSize) {
    super(tileEntityTypeIn, pos, state, name);
    this.saveSizeToNBT = saveSizeToNBT;
    this.inventory = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
    this.stackSizeLimit = maxStackSize;
    this.itemHandler = new InvWrapper(this);
  }

  /**
   * Port: offers {@link #getItemHandler()} as {@code Capabilities.Item.BLOCK} for the block entity type (replaces
   * Forge's {@code getCapability(ITEM_HANDLER)}). Call from a {@code RegisterCapabilitiesEvent} listener.
   */
  public static <T extends InventoryBlockEntity> void registerItemCapability(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
    event.registerBlockEntity(Capabilities.Item.BLOCK, type, (be, side) -> ItemHandlers.asResourceHandler(be.getItemHandler()));
  }

  /* Inventory management */

  @Override
  public ItemStack getItem(int slot) {
    if (slot < 0 || slot >= this.inventory.size()) {
      return ItemStack.EMPTY;
    }

    return this.inventory.get(slot);
  }

  public boolean isStackInSlot(int slot) {
    return !this.getItem(slot).isEmpty();
  }

  /**
   * Same as resize, but does not call markDirty. Used on loading from NBT
   */
  private void resizeInternal(int size) {
    // save effort if the size did not change
    if (size == this.inventory.size()) {
      return;
    }
    ItemStackList newInventory = ItemStackList.withSize(size);

    for (int i = 0; i < size && i < this.inventory.size(); i++) {
      newInventory.set(i, this.inventory.get(i));
    }
    this.inventory = newInventory;
  }

  public void resize(int size) {
    this.resizeInternal(size);
    this.setChangedFast();
  }

  @Override
  public int getContainerSize() {
    return this.inventory.size();
  }

  @Override
  public int getMaxStackSize() {
    return this.stackSizeLimit;
  }

  @Override
  public void setItem(int slot, ItemStack itemstack) {
    if (slot < 0 || slot >= this.inventory.size()) {
      return;
    }

    ItemStack current = this.inventory.get(slot);
    this.inventory.set(slot, itemstack);

    if (!itemstack.isEmpty() && itemstack.getCount() > this.getMaxStackSize()) {
      itemstack.setCount(this.getMaxStackSize());
    }
    if (!ItemStack.matches(current, itemstack)) {
      this.setChangedFast();
    }
  }

  @Override
  public ItemStack removeItem(int slot, int quantity) {
    if (quantity <= 0) {
      return ItemStack.EMPTY;
    }
    ItemStack itemStack = this.getItem(slot);

    if (itemStack.isEmpty()) {
      return ItemStack.EMPTY;
    }

    // whole itemstack taken out
    if (itemStack.getCount() <= quantity) {
      this.setItem(slot, ItemStack.EMPTY);
      this.setChangedFast();
      return itemStack;
    }

    // split itemstack
    itemStack = itemStack.split(quantity);
    // slot is empty, set to ItemStack.EMPTY
    // isn't this redundant to the above check?
    if (this.getItem(slot).getCount() == 0) {
      this.setItem(slot, ItemStack.EMPTY);
    }

    this.setChangedFast();
    // return remainder
    return itemStack;
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    ItemStack itemStack = this.getItem(slot);
    this.setItem(slot, ItemStack.EMPTY);
    return itemStack;
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack itemstack) {
    if (slot < this.getContainerSize()) {
      return this.inventory.get(slot).isEmpty() || itemstack.getCount() + this.inventory.get(slot).getCount() <= this.getMaxStackSize();
    }
    return false;
  }

  @Override
  public void clearContent() {
    for (int i = 0; i < this.inventory.size(); i++) {
      this.inventory.set(i, ItemStack.EMPTY);
    }
  }

  /* Supporting methods */
  @Override
  public boolean stillValid(Player entityplayer) {
    // block changed/got broken?
    if (level == null || this.level.getBlockEntity(this.worldPosition) != this || this.getBlockState().getBlock() == Blocks.AIR) {
      return false;
    }

    return entityplayer.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) <= 64D;
  }

  /* NBT */

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    if (saveSizeToNBT) {
      this.resizeInternal(input.getIntOr(TAG_INVENTORY_SIZE, this.inventory.size()));
    }
    this.readInventoryFromNBT(input);
  }

  @Override
  protected void saveSynced(ValueOutput output) {
    super.saveSynced(output);
    // only sync the size to the client by default
    if (saveSizeToNBT) {
      output.putInt(TAG_INVENTORY_SIZE, this.inventory.size());
    }
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    this.writeInventoryToNBT(output);
  }

  /**
   * Writes the contents of the inventory to the output
   */
  public void writeInventoryToNBT(ValueOutput output) {
    Container inventory = this;
    ValueOutput.TypedOutputList<ItemStackWithSlot> list = output.list(TAG_ITEMS, ItemStackWithSlot.CODEC);
    for (int i = 0; i < inventory.getContainerSize(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        list.add(new ItemStackWithSlot(i, stack));
      }
    }
  }

  /**
   * Reads an inventory from the input. Overwrites current content
   */
  public void readInventoryFromNBT(ValueInput input) {
    int limit = this.getMaxStackSize();
    for (ItemStackWithSlot item : input.listOrEmpty(TAG_ITEMS, ItemStackWithSlot.CODEC)) {
      int slot = item.slot();
      if (slot >= 0 && slot < this.inventory.size()) {
        ItemStack stack = item.stack();
        if (!stack.isEmpty() && stack.getCount() > limit) {
          stack.setCount(limit);
        }
        this.inventory.set(slot, stack);
      }
    }
  }

  @Override
  public boolean isEmpty() {
    for (ItemStack itemstack : this.inventory) {
      if (!itemstack.isEmpty()) {
        return false;
      }
    }

    return true;
  }
}
