package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * BlockTFMazestone (Material.rock, hardness 100, resistance 5). Breaking it with a tool (ItemTool: pickaxe, axe,
 * shovel) costs the tool 16 extra durability. The mazebreaker pickaxe (ItemTFMazebreakerPick, Teil 24f) is to be
 * spared: TODO add it to {@link #SPARED} once it exists.
 */
public class MazestoneBlock extends Block {
    /** Tools that break maze stone without the extra wear (the mazebreaker pickaxe, Teil 24f). */
    public static java.util.function.Predicate<ItemStack> SPARED = stack -> false;

    public MazestoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void playerDestroy(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!tool.isEmpty() && (tool.is(ItemTags.PICKAXES) || tool.is(ItemTags.AXES) || tool.is(ItemTags.SHOVELS)) && !SPARED.test(tool)) {
            tool.hurtAndBreak(16, player, EquipmentSlot.MAINHAND);
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }
}
