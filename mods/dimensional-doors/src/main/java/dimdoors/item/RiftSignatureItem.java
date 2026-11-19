package dimdoors.item;

import dimdoors.DimDoors;
import dimdoors.world.DDData.LinkData;
import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * itemLinkSignature (Rift Signature): the first click stores the place, the second makes two rifts linking it with
 * the second place and uses the signature up. The stabilized one (ItemStabilizedRiftSignature) keeps its first place
 * and makes a pair of rifts for an ender pearl (or stable fabric) each time.
 */
public class RiftSignatureItem extends Item {
    private final boolean stabilized;

    public RiftSignatureItem(boolean stabilized, Properties properties) {
        super(properties);
        this.stabilized = stabilized;
    }

    /** linkX/Y/Z, linkDimID, orientation; {@code null} if nothing is stored. */
    static int[] stored(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag tag = data.copyTag();
        if (!tag.getBooleanOr("isCreated", false)) {
            return null;
        }
        return new int[] {tag.getIntOr("linkX", 0), tag.getIntOr("linkY", 0), tag.getIntOr("linkZ", 0), tag.getIntOr("linkDimID", 0),
                tag.getIntOr("orientation", 0)};
    }

    private static void store(ItemStack stack, int x, int y, int z, int dim, int orientation) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("linkX", x);
        tag.putInt("linkY", y);
        tag.putInt("linkZ", z);
        tag.putInt("linkDimID", dim);
        tag.putBoolean("isCreated", true);
        tag.putInt("orientation", orientation);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null) {
            return InteractionResult.SUCCESS;
        }
        return this.stabilized ? this.useStabilized(context, level, player) : this.useSignature(context, level, player);
    }

    private InteractionResult useSignature(UseOnContext context, Level level, Player player) {
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        int dim = DDWorld.dimId(level, pos);
        int[] coords = stored(stack);
        int offset = 2;
        BlockState clicked = level.getBlockState(pos);
        if (!clicked.isAir() && clicked.canBeReplaced()) {
            offset = 1;
        }
        BlockState above = level.getBlockState(pos.above());
        BlockState below = level.getBlockState(pos.below());
        boolean door = clicked.is(DimDoors.DIMENSIONAL_DOOR.get()) || clicked.is(DimDoors.WARP_DOOR.get());
        if (door && above.is(clicked.getBlock())) {
            offset = 1;
        } else if (door && below.is(clicked.getBlock())) {
            offset = 0;
        }
        int orientation = DDWorld.placeFacing(player);
        for (int count = 0; count < 3; count++) {
            BlockPos at = pos.above(count);
            LinkData link = DDWorld.link(dim, at.getX(), at.getY(), at.getZ());
            BlockState there = level.getBlockState(at);
            if (link != null && (there.is(DimDoors.DIMENSIONAL_DOOR.get()) || there.is(DimDoors.WARP_DOOR.get()) || there.is(DimDoors.UNSTABLE_DOOR.get()))) {
                orientation = link.orientation;
            }
        }
        if (coords != null) {
            DDWorld.createLink(dim, coords[3], pos.getX(), pos.getY() + offset, pos.getZ(), coords[0], coords[1], coords[2], orientation);
            DDWorld.createLink(coords[3], dim, coords[0], coords[1], coords[2], pos.getX(), pos.getY() + offset, pos.getZ(), coords[4]);
            stack.shrink(1);
            player.sendSystemMessage(Component.literal("Rift Created"));
            stack.remove(DataComponents.CUSTOM_DATA);
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
            level.playSound(null, player, DimDoors.RIFT_END.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
        } else {
            store(stack, pos.getX(), pos.getY() + offset, pos.getZ(), dim, orientation);
            player.sendSystemMessage(Component.literal("Rift Signature Stored"));
            level.playSound(null, player, DimDoors.RIFT_START.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult useStabilized(UseOnContext context, Level level, Player player) {
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        int dim = DDWorld.dimId(level, pos);
        int[] coords = stored(stack);
        int offset = level.getBlockState(pos).is(Blocks.SNOW) ? 1 : 2;
        if (coords != null) {
            boolean hasEnder = false;
            if (has(player, DimDoors.STABLE_FABRIC.get()) || has(player, Items.ENDER_PEARL)) {
                if (!consume(player, DimDoors.STABLE_FABRIC.get())) {
                    consume(player, Items.ENDER_PEARL);
                }
                hasEnder = true;
            }
            if (hasEnder) {
                // the original looked for an existing rift at the stored coordinates in the dimension it was used in
                if (DDWorld.link(dim, coords[0], coords[1], coords[2]) == null) {
                    DDWorld.createLink(coords[3], dim, coords[0], coords[1], coords[2], pos.getX(), pos.getY() + offset, pos.getZ());
                }
                DDWorld.createLink(dim, coords[3], pos.getX(), pos.getY() + offset, pos.getZ(), coords[0], coords[1], coords[2]);
                level.playSound(null, player, DimDoors.RIFT_END.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
                player.sendSystemMessage(Component.literal("Rift Created"));
            } else {
                player.sendSystemMessage(Component.literal("No Ender Pearls!"));
            }
        } else {
            store(stack, pos.getX(), pos.getY() + offset, pos.getZ(), dim, 0);
            level.playSound(null, player, DimDoors.RIFT_START.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
            player.sendSystemMessage(Component.literal("Rift Signature Stored"));
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean has(Player player, Item item) {
        return player.getInventory().contains(new ItemStack(item));
    }

    /** InventoryPlayer.consumeInventoryItem: one of the item from the first stack that has it. */
    private static boolean consume(Player player, Item item) {
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        int[] coords = stored(stack);
        if (coords != null) {
            tooltip.accept(Component.literal("Leads to dim " + coords[3]));
            tooltip.accept(Component.literal("at x=" + coords[0] + " y=" + coords[1] + " z=" + coords[2]));
        } else {
            tooltip.accept(Component.literal("First click stores location,"));
            tooltip.accept(Component.literal("second click creates two rifts,"));
            tooltip.accept(Component.literal("that link the first location"));
            tooltip.accept(Component.literal("with the second location"));
        }
    }
}
