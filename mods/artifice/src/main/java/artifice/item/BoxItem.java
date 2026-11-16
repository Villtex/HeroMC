package artifice.item;

import artifice.ArtificeRecipes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;

/**
 * ItemBox: a box packed in the crafting grid (RecipeBox) holds up to 8 items of one kind. Right-click unpacks one
 * box (at most a full stack of its contents; what does not fit stays in the box), sneak right-click every box of
 * the held stack. Packed boxes glint (hasEffect). The deep storage unit support of 1.5.2 is left out (no such
 * blocks in the pack).
 */
public class BoxItem extends Item {
    public BoxItem(Properties properties) {
        super(properties);
    }

    public static ItemStackTemplate contents(ItemStack box) {
        return box.get(ArtificeRecipes.BOX_CONTENTS.get());
    }

    /** A box holding {@code amount} of {@code thing}. */
    public static ItemStack packed(Item box, ItemStack thing, int amount) {
        ItemStack stack = new ItemStack(box);
        stack.set(ArtificeRecipes.BOX_CONTENTS.get(), ItemStackTemplate.fromNonEmptyStack(thing, amount));
        return stack;
    }

    /** The box with another amount (the item damage of 1.5.2). */
    static void setAmount(ItemStack box, int amount) {
        box.set(ArtificeRecipes.BOX_CONTENTS.get(), contents(box).withCount(amount));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(ArtificeRecipes.BOX_CONTENTS.get()) || super.isFoil(stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStackTemplate contents = contents(stack);
        if (contents == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        int amount = contents.count();
        ItemStack thing = contents.create();
        int max = thing.getMaxStackSize();
        if (player.isShiftKeyDown()) {
            give(player, thing, stack.getCount() * amount);
            stack.setCount(0);
            return InteractionResult.SUCCESS;
        }
        ItemStack extra = null;
        if (amount > max && stack.getCount() > 1) {
            extra = stack.copyWithCount(stack.getCount() - 1);
            stack.setCount(1);
        }
        if (amount > max) {
            give(player, thing, max);
            if (amount - max < 1) {
                stack.shrink(1);
            } else {
                setAmount(stack, amount - max);
            }
        } else {
            stack.shrink(1);
            give(player, thing, amount);
        }
        if (extra != null) {
            give(player, extra, extra.getCount());
        }
        return InteractionResult.SUCCESS;
    }

    /** Gives {@code count} of {@code thing} in full stacks; what does not fit drops at the player. */
    static void give(Player player, ItemStack thing, int count) {
        int max = thing.getMaxStackSize();
        for (int given = 0; given < count; ) {
            ItemStack out = thing.copyWithCount(Math.min(max, count - given));
            given += out.getCount();
            player.getInventory().add(out);
            if (!out.isEmpty()) {
                player.level().addFreshEntity(new ItemEntity(player.level(), player.getX() + 0.5, player.getY() + 0.5, player.getZ() + 0.5, out));
            }
        }
    }
}
