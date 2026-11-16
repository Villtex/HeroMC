package harkenscythe.item;

import harkenscythe.HSBlocks;
import harkenscythe.block.CreepBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ItemHSSeeds: the biomass seed (0, cannot be planted, the altar germinates it), the creepball (1, turns the top of
 * soul sand into creep) and the germinated biomass seed (2, planted on tilled creep).
 */
public class SeedItem extends Item {
    private final int kind;

    public SeedItem(int kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (this.kind == 0 || context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player != null && !player.mayUseItemAt(pos, Direction.UP, stack)) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(pos);
        if (this.kind == 1 && state.is(Blocks.SOUL_SAND)) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, HSBlocks.CREEP_BLOCK.get().defaultBlockState());
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.SLIME_ATTACK, SoundSource.BLOCKS, 0.3F, 0.5F);
                stack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
        if (this.kind == 2 && CreepBlock.isCreep(state) && state.getValue(CreepBlock.SOIL) >= 1 && level.getBlockState(pos.above()).canBeReplaced()) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos.above(), HSBlocks.BIOMASS_PLANT.get().defaultBlockState());
                stack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
