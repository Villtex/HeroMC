package extrabiomes.item;

import extrabiomes.block.QuarterLogBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * LogTurner: turns a log to the next orientation (up, east-west, north-south). Quarter logs turn their bark
 * corner around the trunk instead, as BlockQuarterLog did.
 */
public class LogTurnerItem extends Item {
    public LogTurnerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(BlockTags.LOGS) || !state.hasProperty(RotatedPillarBlock.AXIS)) {
            return InteractionResult.PASS;
        }
        if (context.getPlayer() != null && !context.getPlayer().mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        SoundType sound = SoundType.WOOD;
        level.playSound(context.getPlayer(), pos, sound.getStepSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 1.55F);
        if (!level.isClientSide()) {
            BlockState turned;
            if (state.getBlock() instanceof QuarterLogBlock) {
                QuarterLogBlock.Bark bark = state.getValue(QuarterLogBlock.BARK);
                QuarterLogBlock.Bark next = switch (bark) {
                    case NW -> QuarterLogBlock.Bark.NE;
                    case NE -> QuarterLogBlock.Bark.SE;
                    case SE -> QuarterLogBlock.Bark.SW;
                    case SW -> QuarterLogBlock.Bark.NW;
                };
                turned = state.setValue(QuarterLogBlock.BARK, next);
            } else {
                turned = state.cycle(RotatedPillarBlock.AXIS);
            }
            level.setBlock(pos, turned, Block.UPDATE_ALL);
        }
        return InteractionResult.SUCCESS;
    }
}
