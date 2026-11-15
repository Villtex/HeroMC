package eplus;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * ContainerEnchantTable: one item slot, the player inventory, and the cost formulas of Enchanting Plus. The same
 * class runs on both sides; the client uses it for the cost preview, the server recomputes everything.
 */
public class EnchantTableMenu extends AbstractContainerMenu {
    /** clickMenuButton ids (RepairPacket and GuiPacket of 1.5.2). */
    public static final int BUTTON_REPAIR = 1;
    public static final int BUTTON_VANILLA = 2;

    public final Level level;
    public final BlockPos pos;
    private final ContainerLevelAccess access;
    public final SimpleContainer tableInventory = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            EnchantTableMenu.this.slotsChanged(this);
        }
    };
    private Map<Holder<Enchantment>, Integer> enchantments = new LinkedHashMap<>();

    /** Client side: the position comes with the open-screen packet. */
    public static EnchantTableMenu fromNetwork(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new EnchantTableMenu(containerId, inventory, buf.readBlockPos());
    }

    public EnchantTableMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(EnchantingPlus.MENU.get(), containerId);
        this.level = inventory.player.level();
        this.pos = pos;
        this.access = ContainerLevelAccess.create(this.level, pos);
        this.addSlot(new Slot(this.tableInventory, 0, 11, 17) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return EnchantHelper.mayPlace(stack);
            }
        });
        for (int l = 0; l < 3; l++) {
            for (int i1 = 0; i1 < 9; i1++) {
                this.addSlot(new Slot(inventory, i1 + l * 9 + 9, 17 + i1 * 18, 91 + l * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inventory, i, 17 + i * 18, 149));
        }
    }

    public EplusConfig.Settings settings() {
        return EplusConfig.server();
    }

    public float bookCases() {
        return EnchantHelper.bookCases(this.level, this.pos, settings().minimumBook());
    }

    public ItemStack item() {
        return this.tableInventory.getItem(0);
    }

    public Map<Holder<Enchantment>, Integer> getEnchantments() {
        return this.enchantments;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    /** canPurchase: creative always; otherwise enough bookcases (if needed) and levels. */
    public boolean canPurchase(Player player, int cost) throws EnchantException {
        if (player.getAbilities().instabuild) {
            return true;
        }
        EplusConfig.Settings settings = settings();
        if (settings.needsBookShelves() && cost > bookCases()) {
            throw new EnchantException(Component.translatable("eplus.error.bookcases", cost));
        }
        if (player.experienceLevel < cost) {
            throw new EnchantException(Component.translatable("eplus.error.levels", cost));
        }
        return true;
    }

    /** Cost of raising an enchantment from level to enchantmentLevel (at least 1). */
    public int enchantmentCost(Holder<Enchantment> enchantment, int enchantmentLevel, int level) {
        return enchantmentCost(item(), enchantment, enchantmentLevel, level, settings(), bookCases());
    }

    public static int enchantmentCost(ItemStack itemStack, Holder<Enchantment> enchantment, int enchantmentLevel, int level,
                                      EplusConfig.Settings settings, float bookCases) {
        int enchantability = EnchantHelper.enchantability(itemStack);
        if (itemStack.isEmpty() || enchantability == 0) {
            return 0;
        }
        int maxLevel = enchantment.value().getMaxLevel();
        if (enchantmentLevel > maxLevel) {
            return 0;
        }
        int averageCost = (EnchantHelper.minEnchantability(enchantment, enchantmentLevel) + EnchantHelper.maxEnchantability(enchantment, enchantmentLevel)) / 2;
        int adjustedCost = (int) (averageCost * (enchantmentLevel - level + maxLevel) / ((double) maxLevel * enchantability));
        if (!settings.needsBookShelves()) {
            int temp = (int) (adjustedCost * (60.0F / (bookCases + 1.0F)));
            temp /= 20;
            if (temp > adjustedCost) {
                adjustedCost = temp;
            }
        }
        adjustedCost = (int) (adjustedCost * (settings.costFactor() / 3.0));
        if (enchantability > 1) {
            adjustedCost = (int) (adjustedCost * (Math.log(enchantability) / 2.0));
        } else {
            adjustedCost /= 10;
        }
        return Math.max(1, adjustedCost);
    }

    /** Cost of lowering an enchantment from level to enchantmentLevel: negative, the levels come back. */
    public int disenchantmentCost(Holder<Enchantment> enchantment, int enchantmentLevel, int level) {
        ItemStack itemStack = item();
        int enchantability = EnchantHelper.enchantability(itemStack);
        if (itemStack.isEmpty() || enchantability == 0) {
            return 0;
        }
        EplusConfig.Settings settings = settings();
        int maxLevel = enchantment.value().getMaxLevel();
        if (enchantmentLevel > maxLevel) {
            return 0;
        }
        int averageCost = (EnchantHelper.minEnchantability(enchantment, level) + EnchantHelper.maxEnchantability(enchantment, level)) / 2;
        int adjustedCost = (int) (averageCost * (enchantmentLevel - level - maxLevel) / ((double) maxLevel * enchantability));
        if (!settings.needsBookShelves()) {
            int temp = (int) (adjustedCost * (60.0F / (bookCases() + 1.0F)));
            temp /= 20;
            if (temp > adjustedCost) {
                adjustedCost = temp;
            }
        }
        adjustedCost = (int) (adjustedCost * (settings.costFactor() / 4.0));
        if (enchantability > 1) {
            adjustedCost = (int) (adjustedCost * (Math.log(enchantability) / 2.0));
        } else {
            adjustedCost /= 10;
        }
        int enchantmentCost = enchantmentCost(enchantment, level - 1, enchantmentLevel);
        return Math.min(adjustedCost, -enchantmentCost);
    }

    /**
     * EnchantPacket: the client sends the new level of every changed enchantment, the old levels and its cost. The
     * server checks the request against the item (only offered enchantments, levels in range, lowering only as the
     * GUI allows it, no conflicting new enchantments), recomputes the cost and refuses a different one.
     */
    public void enchant(ServerPlayer player, Map<Holder<Enchantment>, Integer> request, int cost) throws EnchantException {
        ItemStack itemstack = item();
        if (itemstack.isEmpty()) {
            return;
        }
        EplusConfig.Settings settings = settings();
        List<EnchantingPlus.Restriction> restrictions = EnchantHelper.restrictions(itemstack);
        Map<Holder<Enchantment>, Integer> map = new HashMap<>(request);
        Map<Holder<Enchantment>, Integer> levels = new HashMap<>();
        int serverCost = 0;
        for (Map.Entry<Holder<Enchantment>, Integer> entry : map.entrySet()) {
            Holder<Enchantment> enchantId = entry.getKey();
            int level = entry.getValue();
            Integer startingLevel = this.enchantments.get(enchantId);
            if (startingLevel == null) {
                throw new EnchantException(Component.literal("enchantment not offered for this item"));
            }
            int maxLevel = enchantId.value().getMaxLevel();
            if (level < 0 || level > Math.max(maxLevel, startingLevel)) {
                throw new EnchantException(Component.literal("level out of range"));
            }
            levels.put(enchantId, startingLevel);
            if (level > startingLevel) {
                serverCost += enchantmentCost(enchantId, level, startingLevel);
            } else if (level < startingLevel) {
                // GuiItem.scroll and updateScreen: lowering needs the config and a level reached on this table
                boolean allowed = settings.allowDisenchanting() && !itemstack.isDamaged() || settings.allowEnchantDamaged();
                if (!allowed || !EnchantHelper.containsKey(restrictions, enchantId, level)) {
                    throw new EnchantException(Component.literal("disenchanting not allowed"));
                }
                serverCost += disenchantmentCost(enchantId, level, startingLevel);
            }
        }
        if (cost != serverCost) {
            throw new EnchantException(Component.literal("Cost is different on client and server"));
        }
        for (Map.Entry<Holder<Enchantment>, Integer> entry : this.enchantments.entrySet()) {
            if (entry.getValue() != 0 && !map.containsKey(entry.getKey())) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        map.values().removeIf(level -> level == 0);
        // the GUI greys out (and never sends) enchantments that conflict with one the item has or gets
        for (Map.Entry<Holder<Enchantment>, Integer> entry : map.entrySet()) {
            if (this.enchantments.getOrDefault(entry.getKey(), 0) == 0) {
                for (Holder<Enchantment> other : map.keySet()) {
                    if (other != entry.getKey() && (!Enchantment.areCompatible(entry.getKey(), other) || !Enchantment.areCompatible(other, entry.getKey()))) {
                        throw new EnchantException(Component.literal("conflicting enchantments"));
                    }
                }
            }
        }
        if (canPurchase(player, serverCost)) {
            ItemStack result = EnchantHelper.setEnchantments(map, itemstack, levels, player.getName().getString());
            if (!player.getAbilities().instabuild) {
                player.giveExperienceLevels(-cost);
            }
            this.tableInventory.setItem(0, result);
        }
        this.slotsChanged(this.tableInventory);
    }

    /** RepairPacket: takes as many levels as the player has (at most the full price) and repairs that share of the damage. */
    public void repair(Player player) throws EnchantException {
        ItemStack itemStack = item();
        if (itemStack.isEmpty() || !settings().allowRepair()) {
            return;
        }
        int serverCost = repairCost(player);
        if (EnchantHelper.isEnchanted(itemStack) && serverCost != 0) {
            if (canPurchase(player, serverCost)) {
                int maxCost = repairCostMax();
                double percAmnt = (double) serverCost / maxCost;
                int remain = itemStack.getDamageValue();
                double remainNet = remain * percAmnt;
                double newDamage = remain - remainNet;
                newDamage = newDamage <= 0.0 ? 0.0 : newDamage;
                itemStack.setDamageValue((int) newDamage);
                if (!player.getAbilities().instabuild) {
                    player.giveExperienceLevels(-serverCost);
                }
                this.tableInventory.setChanged();
            }
            this.slotsChanged(this.tableInventory);
        }
    }

    /** Full repair price: the enchanting cost of all enchantments, by the damaged share, divided by enchantability. */
    public int repairCostMax() {
        ItemStack itemStack = item();
        if (itemStack.isEmpty()) {
            return 0;
        }
        if (EnchantHelper.isEnchanted(itemStack) && itemStack.isDamaged()) {
            int cost = 0;
            for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(itemStack).entrySet()) {
                cost += enchantmentCost(entry.getKey(), entry.getIntValue(), 0);
            }
            int maxDamage = itemStack.getMaxDamage();
            int displayDamage = itemStack.getDamageValue();
            int enchantability = EnchantHelper.enchantability(itemStack);
            double percentDamage = 1.0 - (double) (maxDamage - displayDamage) / maxDamage;
            double totalCost = percentDamage * cost / enchantability;
            totalCost *= 2 * settings().repairFactor();
            return (int) Math.max(1.0, totalCost);
        }
        return 0;
    }

    public int repairCost(Player player) {
        int costMax = repairCostMax();
        if (costMax == 0) {
            return 0;
        }
        return player.getAbilities().instabuild ? costMax : Math.min(player.experienceLevel, costMax);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (id == BUTTON_REPAIR) {
            try {
                repair(player);
            } catch (EnchantException e) {
                EnchantingPlus.LOGGER.info("Repair failed because: {}", e.getMessage());
            }
            this.broadcastChanges();
            return true;
        }
        if (id == BUTTON_VANILLA) {
            // GuiPacket: only while the player and the server use the custom table
            if (TableEvents.useMod(serverPlayer)) {
                TableEvents.openTable(serverPlayer, this.pos, false);
            }
            return true;
        }
        return false;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        this.enchantments = EnchantHelper.readItems(item(), this.level.registryAccess(), settings());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.tableInventory);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack itemStack = stack.copy();
        if (index != 0) {
            Slot slot1 = this.slots.get(0);
            if (slot1.hasItem() || !slot1.mayPlace(stack)) {
                return ItemStack.EMPTY;
            }
            slot1.setByPlayer(stack.split(1));
        } else if (!this.moveItemStackTo(stack, 1, 37, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (itemStack.getCount() == stack.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return itemStack;
    }
}
