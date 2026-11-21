package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import twilightforest.TFUncrafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ContainerTFUncrafting with InventoryTFGoblinUncrafting, InventoryTFGoblinInput and the goblin slots. An item put in
 * takes apart into the ingredients of its (first) shaped recipe, shown in the assembly grid; damaged items lose as
 * many breakable parts (not sticks) as their damage share of max(4, parts); containers (buckets) never come back.
 * Taking one of them costs one level per breakable part and moves the rest into the grid. With the ingredients still
 * shown, items put into the grid replace them: a tool or armour piece of the same kind comes out with the input's
 * name and those of its enchantments that fit, for a re-crafting cost in levels. An empty input makes it a crafting
 * table. Slots: 0 input, 1 result, 2..10 the uncrafting matrix (not on screen), 11..19 assembly, 20..55 inventory.
 * The server does all recipe work; the costs and the broken parts reach the screen as container data.
 */
public class UncraftingMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Player player;
    final SimpleContainer input = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            UncraftingMenu.this.slotsChanged(this);
        }
    };
    final SimpleContainer uncrafting = new SimpleContainer(9);
    final TransientCraftingContainer assembly = new TransientCraftingContainer(this, 3, 3);
    final SimpleContainer result = new SimpleContainer(1);
    /** uncraftingCost, recraftingCost, the broken parts (bit per slot), numberOfInputItems. */
    private final ContainerData data;
    private boolean updating;

    public UncraftingMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        this(id, inventory, access, new SimpleContainerData(4));
    }

    private UncraftingMenu(int id, Inventory inventory, ContainerLevelAccess access, ContainerData data) {
        super(TFUncrafting.MENU.get(), id);
        this.access = access;
        this.player = inventory.player;
        this.data = data;
        this.addSlot(new Slot(this.input, 0, 13, 35));
        this.addSlot(new ResultSlot(this.result, 0, 147, 35));
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlot(new Slot(this.uncrafting, x + y * 3, -10000, -10000) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(Player p) {
                        return false;
                    }
                });
            }
        }
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlot(new Slot(this.assembly, x + y * 3, 62 + x * 18, 17 + y * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
        this.addDataSlots(data);
    }

    public static UncraftingMenu fromNetwork(int id, Inventory inventory, FriendlyByteBuf buf) {
        return new UncraftingMenu(id, inventory, ContainerLevelAccess.NULL);
    }

    public int uncraftingCost() {
        return this.data.get(0);
    }

    public int recraftingCost() {
        return this.data.get(1);
    }

    public boolean isBroken(int slot) {
        return (this.data.get(2) >> slot & 1) != 0;
    }

    public ItemStack uncraftingItem(int slot) {
        return this.uncrafting.getItem(slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, TFUncrafting.UNCRAFTING_TABLE.get());
    }

    // ---------------------------------------------------------------- onCraftMatrixChanged (server only: recipes)

    @Override
    public void slotsChanged(Container container) {
        if (this.updating || !(this.player.level() instanceof ServerLevel level)) {
            return;
        }
        this.updating = true;
        try {
            this.update(level, container);
        } finally {
            this.updating = false;
        }
        this.broadcastChanges();
    }

    private void update(ServerLevel level, Container changed) {
        ItemStack inputStack = this.input.getItem(0);
        if (changed == this.input) {
            this.uncrafting.clearContent();
            this.data.set(2, 0);
            Uncraft found = this.getRecipeFor(level, inputStack);
            if (found != null) {
                for (int y = 0; y < found.height; y++) {
                    for (int x = 0; x < found.width; x++) {
                        this.uncrafting.setItem(x + y * 3, found.items.get(x + y * found.width).copy());
                    }
                }
                int broken = 0;
                if (inputStack.isDamaged()) {
                    int damagedParts = this.countDamagedParts(inputStack);
                    for (int i = 0; i < 9 && damagedParts > 0; i++) {
                        if (isDamageableComponent(this.uncrafting.getItem(i))) {
                            broken |= 1 << i;
                            damagedParts--;
                        }
                    }
                }
                for (int i = 0; i < 9; i++) {
                    ItemStack ingredient = this.uncrafting.getItem(i);
                    if (!ingredient.isEmpty() && ingredient.getItem().getCraftingRemainder() != null) {
                        broken |= 1 << i;
                    }
                }
                this.data.set(2, broken);
                this.data.set(3, found.count);
                this.data.set(0, this.calculateUncraftingCost());
                this.data.set(1, 0);
            } else {
                this.data.set(3, 0);
                this.data.set(0, 0);
            }
        }
        if (changed == this.assembly || changed == this.input) {
            if (inputStack.isEmpty()) {
                this.result.setItem(0, this.craft(level, this.assembly.asCraftInput()));
                this.data.set(1, 0);
            } else {
                this.result.setItem(0, ItemStack.EMPTY);
                this.data.set(0, this.calculateUncraftingCost());
                this.data.set(1, 0);
            }
        }
        if (!this.uncrafting.isEmpty() && !this.assembly.isEmpty()) {
            List<ItemStack> combine = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                if (!this.assembly.getItem(i).isEmpty()) {
                    combine.add(this.assembly.getItem(i));
                } else if (!this.uncrafting.getItem(i).isEmpty() && !this.isBroken(i)) {
                    combine.add(this.uncrafting.getItem(i));
                } else {
                    combine.add(ItemStack.EMPTY);
                }
            }
            ItemStack made = this.craft(level, CraftingInput.of(3, 3, combine));
            if (!made.isEmpty() && isValidMatchForInput(inputStack, made)) {
                ItemEnchantments innate = made.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                if (inputStack.has(DataComponents.CUSTOM_NAME)) {
                    made.set(DataComponents.CUSTOM_NAME, inputStack.get(DataComponents.CUSTOM_NAME));
                }
                made.set(DataComponents.REPAIR_COST, inputStack.getOrDefault(DataComponents.REPAIR_COST, 0));
                ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(inputStack).entrySet()) {
                    if (entry.getKey().value().canEnchant(made)) {
                        enchantments.set(entry.getKey(), entry.getIntValue());
                    }
                }
                for (var entry : innate.entrySet()) {
                    if (enchantments.getLevel(entry.getKey()) < entry.getIntValue()) {
                        enchantments.set(entry.getKey(), entry.getIntValue());
                    }
                }
                EnchantmentHelper.setEnchantments(made, enchantments.toImmutable());
                this.result.setItem(0, made);
                this.data.set(0, 0);
                int cost = this.calculateRecraftingCost(made);
                this.data.set(1, cost);
                if (cost > 0 && !made.has(DataComponents.CUSTOM_NAME)) {
                    made.set(DataComponents.REPAIR_COST, inputStack.getOrDefault(DataComponents.REPAIR_COST, 0) + 2);
                }
            }
        }
    }

    private ItemStack craft(ServerLevel level, CraftingInput craftInput) {
        Optional<RecipeHolder<CraftingRecipe>> recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, craftInput, level);
        return recipe.map(r -> r.value().assemble(craftInput)).orElse(ItemStack.EMPTY);
    }

    private record Uncraft(int width, int height, List<ItemStack> items, int count) {
    }

    /** getRecipeFor: a shaped recipe making the input (as many as its output count at least). */
    private @Nullable Uncraft getRecipeFor(ServerLevel level, ItemStack inputStack) {
        if (inputStack.isEmpty()) {
            return null;
        }
        for (RecipeHolder<?> holder : level.getServer().getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof ShapedRecipe shaped)) {
                continue;
            }
            List<ItemStack> items = new ArrayList<>();
            for (Optional<Ingredient> ingredient : shaped.getIngredients()) {
                items.add(ingredient.flatMap(i -> i.items().findFirst()).map(h -> new ItemStack((Holder<Item>) h)).orElse(ItemStack.EMPTY));
            }
            ItemStack output;
            try {
                output = shaped.assemble(CraftingInput.of(shaped.getWidth(), shaped.getHeight(), items));
            } catch (RuntimeException e) {
                continue;
            }
            if (!output.isEmpty() && output.is(inputStack.getItem()) && inputStack.getCount() >= output.getCount()) {
                return new Uncraft(shaped.getWidth(), shaped.getHeight(), items, output.getCount());
            }
        }
        return null;
    }

    private static boolean isValidMatchForInput(ItemStack input, ItemStack result) {
        for (var tag : List.of(ItemTags.PICKAXES, ItemTags.AXES, ItemTags.SHOVELS, ItemTags.HOES, ItemTags.SWORDS)) {
            if (input.is(tag) && result.is(tag)) {
                return true;
            }
        }
        if (input.getItem() instanceof BowItem && result.getItem() instanceof BowItem) {
            return true;
        }
        Equippable a = input.get(DataComponents.EQUIPPABLE);
        Equippable b = result.get(DataComponents.EQUIPPABLE);
        return a != null && b != null && a.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR && a.slot() == b.slot();
    }

    private static boolean isDamageableComponent(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(net.minecraft.world.item.Items.STICK);
    }

    private int countDamageableParts() {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            if (isDamageableComponent(this.uncrafting.getItem(i))) {
                count++;
            }
        }
        return count;
    }

    private int countDamagedParts(ItemStack input) {
        int total = Math.max(4, this.countDamageableParts());
        float damage = (float) input.getDamageValue() / input.getMaxDamage();
        return (int) Math.ceil(total * damage);
    }

    private int calculateUncraftingCost() {
        return !this.assembly.isEmpty() ? 0 : this.countDamageableParts();
    }

    private int calculateRecraftingCost(ItemStack output) {
        ItemStack input = this.input.getItem(0);
        if (!input.isEnchanted() || output.isEmpty()) {
            return 0;
        }
        int cost = input.getOrDefault(DataComponents.REPAIR_COST, 0);
        for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(input).entrySet()) {
            cost += weightModifier(entry.getKey().value()) * entry.getIntValue() + 1;
        }
        cost += (1 + this.countDamagedParts(input)) * EnchantmentHelper.getEnchantmentsForCrafting(output).size();
        cost += enchantability(input) - enchantability(output);
        return Math.max(1, cost);
    }

    private static int enchantability(ItemStack stack) {
        var enchantable = stack.get(DataComponents.ENCHANTABLE);
        return enchantable == null ? 0 : enchantable.value();
    }

    private static int weightModifier(Enchantment enchantment) {
        return switch (enchantment.getWeight()) {
            case 1 -> 8;
            case 2 -> 4;
            case 3, 4, 5 -> 2;
            default -> 1;
        };
    }

    // ---------------------------------------------------------------- slotClick

    private boolean canAfford(int cost) {
        return cost <= this.player.experienceLevel || this.player.getAbilities().instabuild;
    }

    @Override
    public void clicked(int slotNum, int button, ContainerInput clickType, Player player) {
        if (slotNum >= 11 && slotNum < 20 && this.getCarried().isEmpty() && !this.slots.get(slotNum).hasItem() && this.assembly.isEmpty()) {
            int i = slotNum - 11;
            ItemStack part = this.uncrafting.getItem(i);
            if (!part.isEmpty() && !this.isBroken(i) && clickType == ContainerInput.PICKUP) {
                if (this.canAfford(this.uncraftingCost())) {
                    this.uncraft(i);
                }
                return;
            }
        }
        if (slotNum == 1 && !this.canAfford(this.recraftingCost())) {
            return;
        }
        super.clicked(slotNum, button, clickType, player);
    }

    /** SlotTFGoblinUncrafting.onPickupFromSlot: the part in hand, the others into the grid, the input used. */
    private void uncraft(int clicked) {
        if (!(this.player.level() instanceof ServerLevel)) {
            return;
        }
        int cost = this.uncraftingCost();
        int count = this.data.get(3);
        int broken = this.data.get(2);
        List<ItemStack> parts = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            parts.add(this.uncrafting.getItem(i).copy());
        }
        this.updating = true;
        try {
            if (cost > 0 && !this.player.getAbilities().instabuild) {
                this.player.giveExperienceLevels(-cost);
            }
            this.setCarried(parts.get(clicked));
            for (int i = 0; i < 9; i++) {
                if (i != clicked && (broken >> i & 1) == 0 && !parts.get(i).isEmpty()) {
                    this.assembly.setItem(i, parts.get(i));
                }
            }
            this.input.getItem(0).shrink(count);
        } finally {
            this.updating = false;
        }
        this.slotsChanged(this.input);
        this.slotsChanged(this.assembly);
    }

    /** SlotTFGoblinCraftResult: crafted normally, or re-crafted (levels, the input and the shown parts used). */
    class ResultSlot extends Slot {
        ResultSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack taken) {
            UncraftingMenu menu = UncraftingMenu.this;
            boolean combined = !menu.input.getItem(0).isEmpty();
            menu.updating = true;
            try {
                if (combined) {
                    int cost = menu.recraftingCost();
                    if (cost > 0 && !player.getAbilities().instabuild) {
                        player.giveExperienceLevels(-cost);
                    }
                    menu.uncrafting.clearContent();
                    menu.data.set(2, 0);
                    // decrStackSize(0, numberOfInputItems) for each of the 9 slots
                    menu.input.getItem(0).shrink(9 * menu.data.get(3));
                }
                taken.onCraftedBy(player, taken.getCount());
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = menu.assembly.getItem(i);
                    if (!stack.isEmpty()) {
                        var remainder = stack.getItem().getCraftingRemainder();
                        menu.assembly.removeItem(i, 1);
                        if (remainder != null) {
                            ItemStack rest = remainder.create();
                            if (menu.assembly.getItem(i).isEmpty()) {
                                menu.assembly.setItem(i, rest);
                            } else if (!player.getInventory().add(rest)) {
                                player.drop(rest, false, net.minecraft.util.Prediction.SERVER_ONLY);
                            }
                        }
                    }
                }
            } finally {
                menu.updating = false;
            }
            menu.slotsChanged(menu.input);
            menu.slotsChanged(menu.assembly);
            super.onTake(player, taken);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotNum) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotNum);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (slotNum == 1) {
                if (!this.canAfford(this.recraftingCost())) {
                    return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(stack, 20, 56, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, copy);
            } else if (slotNum == 0) {
                if (!this.moveItemStackTo(stack, 20, 56, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotNum >= 20 && slotNum < 47) {
                if (!this.moveItemStackTo(stack, 47, 56, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotNum >= 47 && slotNum < 56) {
                if (!this.moveItemStackTo(stack, 20, 47, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 20, 56, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == copy.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            this.clearContainer(player, this.assembly);
            this.clearContainer(player, this.input);
        });
    }

    public static ContainerLevelAccess access(Level level, BlockPos pos) {
        return ContainerLevelAccess.create(level, pos);
    }
}
