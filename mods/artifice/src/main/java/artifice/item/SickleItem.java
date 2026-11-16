package artifice.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ItemSickle (an ItemTool with damage 3, half the material's uses, mining speed 1 everywhere): breaking leaves
 * harvests all leaves within the radius (1-5 by material, a sphere); breaking a plant (BlockFlower of 1.5.2: flowers,
 * saplings, crops, grass, mushrooms, dead bushes, stems, nether wart and lily pads - every {@link VegetationBlock}
 * today) harvests the plants on the same level in the square -r..r-1 within the radius. Each costs one durability.
 */
public class SickleItem extends Item {
    private final int radius;

    public SickleItem(Properties properties, int radius) {
        super(properties);
        this.radius = radius;
    }

    public int radius() {
        return this.radius;
    }

    /** onBlockStartBreak: returns whether the sickle harvested (the normal break is then skipped). */
    public boolean harvest(ItemStack stack, ServerLevel level, BlockPos pos, BlockState state, Player player) {
        int r = this.radius;
        if (state.getBlock() instanceof VegetationBlock) {
            for (int i = -r; i < r; i++) {
                for (int k = -r; k < r; k++) {
                    BlockPos p = pos.offset(i, 0, k);
                    if (level.getBlockState(p).getBlock() instanceof VegetationBlock && Math.sqrt(p.distSqr(pos)) <= r) {
                        this.harvestOne(stack, level, p, player);
                    }
                }
            }
        } else if (state.is(BlockTags.LEAVES)) {
            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-r, -r, -r), pos.offset(r, r, r))) {
                if (Math.sqrt(p.distSqr(pos)) <= r && level.getBlockState(p).is(BlockTags.LEAVES)) {
                    this.harvestOne(stack, level, p.immutable(), player);
                }
            }
        } else {
            return false;
        }
        level.playSound(null, pos, state.getSoundType(level, pos, player).getBreakSound(), SoundSource.BLOCKS, 1.0F,
                level.getRandom().nextFloat() * 0.1F + 0.9F);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        return true;
    }

    /** harvestBlock (the drops as broken by the player with this tool) and setBlockToAir. */
    private void harvestOne(ItemStack stack, ServerLevel level, BlockPos p, Player player) {
        BlockState state = level.getBlockState(p);
        Block.dropResources(state, level, p, level.getBlockEntity(p), player, stack);
        level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
}
