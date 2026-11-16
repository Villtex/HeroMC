package artifice.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ItemSledge (an ItemTool with damage 2 and a quarter of the material's uses): a block registered in
 * {@link SledgeParts} breaks into its parts instead of its drops, every part lost with the loss chance of the
 * material (50/40/30/25/20 %). Registered blocks mine at the material's speed and count as harvestable (tool
 * component, tag {@code artifice:sledge_breakable}); everything else mines at hand speed and costs no durability.
 */
public class SledgeItem extends Item {
    private final int lossChance;

    public SledgeItem(Properties properties, int lossChance) {
        super(properties);
        this.lossChance = lossChance;
    }

    public int lossChance() {
        return this.lossChance;
    }

    /**
     * onBlockStartBreak (server, not in creative mode): drops the parts and removes the block. Returns whether the
     * block was smashed (the normal break is then skipped). onBlockDestroyed then costs one durability, but only for
     * blocks registered with a metadata value.
     */
    public boolean smash(ItemStack stack, ServerLevel level, BlockPos pos, BlockState state, Player player) {
        SledgeParts.Entry entry = SledgeParts.get(state);
        if (entry == null) {
            return false;
        }
        for (ItemStack part : entry.parts()) {
            if (level.getRandom().nextInt(100) > this.lossChance) {
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, part.copy()));
            }
        }
        level.levelEvent(player, 2001, pos, Block.getId(state));
        level.removeBlock(pos, false);
        if (entry.exact()) {
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
        return true;
    }
}
