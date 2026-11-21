package tconstruct.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import tconstruct.TDeco;

/**
 * EssenceExtractorLogic (a TileEntityEnchantmentTable): the stored essence. On the client an enchanting table block
 * entity of its own turns the book (bookAnimationTick, the same animation as 1.5.2) for the renderer.
 */
public class EssenceExtractorBlockEntity extends BlockEntity {
    public int essence;
    private @Nullable EnchantingTableBlockEntity book;

    public EssenceExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(TDeco.EXTRACTOR_ENTITY.get(), pos, state);
    }

    public EnchantingTableBlockEntity book() {
        if (this.book == null) {
            this.book = new EnchantingTableBlockEntity(this.worldPosition, Blocks.ENCHANTING_TABLE.defaultBlockState());
            if (this.level != null) {
                this.book.setLevel(this.level);
            }
        }
        return this.book;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, EssenceExtractorBlockEntity extractor) {
        EnchantingTableBlockEntity.bookAnimationTick(level, pos, state, extractor.book());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Essence", this.essence);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.essence = input.getIntOr("Essence", 0);
    }
}
