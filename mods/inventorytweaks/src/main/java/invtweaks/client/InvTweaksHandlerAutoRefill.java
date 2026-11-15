package invtweaks.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

/**
 * InvTweaksHandlerAutoRefill: when the selected stack is used up (or a tool is about to break with
 * autoRefillBeforeBreak), the best replacement from the inventory is moved into the hotbar slot. The original waited
 * in a thread for the server's inventory update and at least 200 ms; here the move is scheduled 200 ms later on the
 * client thread.
 */
public final class InvTweaksHandlerAutoRefill {
    private final Minecraft mc;
    private InvTweaksConfig config;

    public InvTweaksHandlerAutoRefill(Minecraft mc, InvTweaksConfig config) {
        this.mc = mc;
        this.config = config;
    }

    public void setConfig(InvTweaksConfig config) {
        this.config = config;
    }

    /** @param targetedSlot index in the INVENTORY section (27-35 = hotbar), stored the stack that was there */
    public void autoRefillSlot(int targetedSlot, ItemStack stored) throws Exception {
        InvTweaksContainerSectionManager container = new InvTweaksContainerSectionManager(this.mc, ContainerSection.INVENTORY);
        ItemStack candidate = null;
        int replacementSlot = -1;
        boolean refillBeforeBreak = this.config.getProperty(InvTweaksConfig.PROP_AUTO_REFILL_BEFORE_BREAK).equals(InvTweaksConfig.VALUE_TRUE);
        int threshold = this.config.getIntProperty(InvTweaksConfig.PROP_AUTO_REFILL_DAMAGE_THRESHHOLD);
        InvTweaksItemTree tree = this.config.getTree();
        if (!tree.isItemUnknown(stored)) {
            List<InvTweaksConfigSortingRule> matchingRules = new ArrayList<>();
            for (InvTweaksItemTreeItem item : tree.getItems(stored)) {
                matchingRules.add(new InvTweaksConfigSortingRule(tree, "d" + (targetedSlot - 26), item.getName(), 36, 9));
            }
            for (InvTweaksConfigSortingRule rule : this.config.getRules()) {
                if (rule.getType() == InvTweaksConfigSortingRule.Type.SLOT || rule.getType() == InvTweaksConfigSortingRule.Type.COLUMN) {
                    for (int slot : rule.getPreferredSlots()) {
                        if (targetedSlot == slot) {
                            matchingRules.add(rule);
                            break;
                        }
                    }
                }
            }
            for (InvTweaksConfigSortingRule rule : matchingRules) {
                for (int i = 0; i < InvTweaksConst.INVENTORY_SIZE; i++) {
                    ItemStack stack = container.getItemStack(i);
                    if (stack != null && tree.matches(tree.getItems(stack), rule.getKeyword())) {
                        if (stack.getMaxStackSize() == 1) {
                            if ((candidate == null || InvTweaksContainerManager.getItemDamage(stack) > InvTweaksContainerManager.getItemDamage(candidate))
                                    && (!refillBeforeBreak || stack.getMaxDamage() - InvTweaksContainerManager.getItemDamage(stack) > threshold)) {
                                candidate = stack;
                                replacementSlot = i;
                            }
                        } else if (candidate == null || stack.getCount() < candidate.getCount()) {
                            candidate = stack;
                            replacementSlot = i;
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < InvTweaksConst.INVENTORY_SIZE; i++) {
                ItemStack stack = container.getItemStack(i);
                if (stack != null && stack.getItem() == stored.getItem()
                        && InvTweaksContainerManager.getItemDamage(stack) == InvTweaksContainerManager.getItemDamage(stored)) {
                    candidate = stack;
                    replacementSlot = i;
                    break;
                }
            }
        }
        if (candidate != null || refillBeforeBreak && container.getSlot(targetedSlot).hasItem()) {
            if (InvTweaks.debug) {
                InvTweaks.log.info("Automatic stack replacement.");
            }
            final int from = replacementSlot != -1 ? replacementSlot : container.getFirstEmptyIndex();
            final ItemStack expected = replacementSlot != -1 ? container.getItemStack(replacementSlot).copy() : null;
            final boolean beforeBreak = refillBeforeBreak;
            InvTweaks.schedule(InvTweaksConst.AUTO_REFILL_DELAY, () -> this.refill(targetedSlot, from, expected, beforeBreak));
        }
    }

    private void refill(int targetedSlot, int from, ItemStack expected, boolean refillBeforeBreak) {
        if (this.mc.player == null || from == -1) {
            return;
        }
        try {
            InvTweaksContainerSectionManager container = new InvTweaksContainerSectionManager(this.mc, ContainerSection.INVENTORY);
            ItemStack stack = container.getItemStack(from);
            if (stack != null && expected != null && stack.getItem() == expected.getItem() || refillBeforeBreak) {
                if (!container.move(targetedSlot, from) && !container.move(from, targetedSlot)) {
                    InvTweaks.log.warn("Failed to move stack for autoreplace, despite of prior tests.");
                } else {
                    if (!this.config.getProperty(InvTweaksConfig.PROP_ENABLE_SOUNDS).equals(InvTweaksConfig.VALUE_FALSE)) {
                        InvTweaks.playSound(SoundEvents.CHICKEN_EGG, 1.4F, 0.5F);
                    }
                    if (container.getItemStack(from) != null && from >= 27) {
                        for (int i = 0; i < InvTweaksConst.INVENTORY_SIZE; i++) {
                            if (container.getItemStack(i) == null) {
                                container.move(from, i);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            InvTweaks.log.error("Failed to trigger autoreplace", e);
        }
    }
}
