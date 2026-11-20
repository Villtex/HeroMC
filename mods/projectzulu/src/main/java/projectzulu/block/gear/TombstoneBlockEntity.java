package projectzulu.block.gear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import projectzulu.ZuluGear;

/**
 * TileEntityTombstone: four lines of at most 10 characters (saved as Text1-Text4 like the original; the renderer
 * draws five rows, the fifth always empty).
 *
 * <p>For the world generation (cemetery): {@link #setSignString(String)} wraps a sentence into the lines like the
 * original, {@link #setLine(int, String)} sets one line.
 */
public class TombstoneBlockEntity extends BlockEntity {
    public static final int LINES = 4;
    public static final int MAX_CHARS = 10;
    private final String[] signText = {"", "", "", "", ""};

    public TombstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ZuluGear.TOMBSTONE_ENTITY.get(), pos, state);
    }

    /** The five rows the renderer draws. */
    public String[] getSignText() {
        return this.signText.clone();
    }

    public String getLine(int line) {
        return this.signText[line];
    }

    /** One of the four lines, cut to 10 characters. */
    public void setLine(int line, String text) {
        if (line >= 0 && line < LINES) {
            this.signText[line] = text.length() > MAX_CHARS ? text.substring(0, MAX_CHARS) : text;
            this.changed();
        }
    }

    /**
     * setSignString of the original: splits the text at spaces and fills the four lines with as many words as fit
     * into 10 characters (a longer word stands alone and is cut when saved).
     */
    public void setSignString(String inputString) {
        String[] words = inputString.split(" ");
        int startWord = 0;
        for (int currentLine = 0; currentLine < LINES; currentLine++) {
            int curCharPerLine = 0;
            for (int i = startWord; i < words.length; i++) {
                if (curCharPerLine + 1 + words[i].length() > MAX_CHARS) {
                    this.signText[currentLine] = join(words, startWord, i);
                    startWord = i;
                    break;
                }
                curCharPerLine += 1 + words[i].length();
                if (i + 1 >= words.length) {
                    this.signText[currentLine] = join(words, startWord, i + 1);
                    startWord = i + 1;
                    break;
                }
            }
        }
        this.changed();
    }

    /** words[from] followed by words[from+1 .. to-1] (words[from] even if to == from, like the original). */
    private static String join(String[] words, int from, int to) {
        StringBuilder line = new StringBuilder(words[from]);
        for (int j = from + 1; j < to; j++) {
            line.append(' ').append(words[j]);
        }
        return line.toString();
    }

    private void changed() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < LINES; i++) {
            if (!this.signText[i].isEmpty()) {
                output.putString("Text" + (i + 1), this.signText[i]);
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < LINES; i++) {
            String text = input.getStringOr("Text" + (i + 1), "");
            this.signText[i] = text.length() > MAX_CHARS ? text.substring(0, MAX_CHARS) : text;
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
