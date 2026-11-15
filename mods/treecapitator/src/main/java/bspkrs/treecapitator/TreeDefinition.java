package bspkrs.treecapitator;

import bspkrs.util.BlockID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One tree: its log blocks, its leaf blocks and the per-tree settings that differ from the defaults
 * (general_settings.per_tree_defaults). Loaded from data/&lt;namespace&gt;/treecapitator/tree/&lt;name&gt;.json; the file name
 * is the tree key of the 1.5.2 config ("vanilla_oak", "fir", ...). Files with the same name in different namespaces
 * are appended to one tree, like the per-mod tree sections of the old config were.
 */
public final class TreeDefinition {
    /** The JSON form; absent settings fall back to the config defaults. */
    public record Data(List<BlockID> logs, List<BlockID> leaves, Optional<Boolean> allowSmartTreeDetection, Optional<Boolean> onlyDestroyUpwards,
            Optional<Boolean> requireLeafDecayCheck, Optional<Integer> maxHorLogBreakDist, Optional<Integer> maxVerLogBreakDist,
            Optional<Integer> maxLeafIDDist, Optional<Integer> maxHorLeafBreakDist, Optional<Integer> minLeavesToID, Optional<Float> breakSpeedModifier) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockID.CODEC.listOf().optionalFieldOf("logs", List.of()).forGetter(Data::logs),
                BlockID.CODEC.listOf().optionalFieldOf("leaves", List.of()).forGetter(Data::leaves),
                Codec.BOOL.optionalFieldOf("allowSmartTreeDetection").forGetter(Data::allowSmartTreeDetection),
                Codec.BOOL.optionalFieldOf("onlyDestroyUpwards").forGetter(Data::onlyDestroyUpwards),
                Codec.BOOL.optionalFieldOf("requireLeafDecayCheck").forGetter(Data::requireLeafDecayCheck),
                Codec.INT.optionalFieldOf("maxHorLogBreakDist").forGetter(Data::maxHorLogBreakDist),
                Codec.INT.optionalFieldOf("maxVerLogBreakDist").forGetter(Data::maxVerLogBreakDist),
                Codec.INT.optionalFieldOf("maxLeafIDDist").forGetter(Data::maxLeafIDDist),
                Codec.INT.optionalFieldOf("maxHorLeafBreakDist").forGetter(Data::maxHorLeafBreakDist),
                Codec.INT.optionalFieldOf("minLeavesToID").forGetter(Data::minLeavesToID),
                Codec.FLOAT.optionalFieldOf("breakSpeedModifier").forGetter(Data::breakSpeedModifier)).apply(i, Data::new));
    }

    final List<BlockID> logBlocks = new ArrayList<>();
    final List<BlockID> leafBlocks = new ArrayList<>();
    private Optional<Boolean> allowSmartTreeDetection = Optional.empty();
    private Optional<Boolean> onlyDestroyUpwards = Optional.empty();
    private Optional<Boolean> requireLeafDecayCheck = Optional.empty();
    private Optional<Integer> maxHorLogBreakDist = Optional.empty();
    private Optional<Integer> maxVerLogBreakDist = Optional.empty();
    private Optional<Integer> maxLeafIDDist = Optional.empty();
    private Optional<Integer> maxHorLeafBreakDist = Optional.empty();
    private Optional<Integer> minLeavesToID = Optional.empty();
    private Optional<Float> breakSpeedModifier = Optional.empty();

    public TreeDefinition() {
    }

    public TreeDefinition(Data d) {
        append(d.logs(), d.leaves());
        allowSmartTreeDetection = d.allowSmartTreeDetection();
        onlyDestroyUpwards = d.onlyDestroyUpwards();
        requireLeafDecayCheck = d.requireLeafDecayCheck();
        maxHorLogBreakDist = d.maxHorLogBreakDist();
        maxVerLogBreakDist = d.maxVerLogBreakDist();
        maxLeafIDDist = d.maxLeafIDDist();
        maxHorLeafBreakDist = d.maxHorLeafBreakDist();
        minLeavesToID = d.minLeavesToID();
        breakSpeedModifier = d.breakSpeedModifier();
    }

    private void append(List<BlockID> logs, List<BlockID> leaves) {
        for (BlockID b : logs) {
            if (!logBlocks.contains(b)) {
                logBlocks.add(b);
            }
        }
        for (BlockID b : leaves) {
            if (!leafBlocks.contains(b)) {
                leafBlocks.add(b);
            }
        }
    }

    /** TreeDefinition.append: only the blocks. */
    public TreeDefinition append(TreeDefinition other) {
        append(other.logBlocks, other.leafBlocks);
        return this;
    }

    /** TreeDefinition.appendWithSettings: the blocks, and every setting the other one sets. */
    public TreeDefinition appendWithSettings(TreeDefinition o) {
        append(o);
        allowSmartTreeDetection = o.allowSmartTreeDetection.or(() -> allowSmartTreeDetection);
        onlyDestroyUpwards = o.onlyDestroyUpwards.or(() -> onlyDestroyUpwards);
        requireLeafDecayCheck = o.requireLeafDecayCheck.or(() -> requireLeafDecayCheck);
        maxHorLogBreakDist = o.maxHorLogBreakDist.or(() -> maxHorLogBreakDist);
        maxVerLogBreakDist = o.maxVerLogBreakDist.or(() -> maxVerLogBreakDist);
        maxLeafIDDist = o.maxLeafIDDist.or(() -> maxLeafIDDist);
        maxHorLeafBreakDist = o.maxHorLeafBreakDist.or(() -> maxHorLeafBreakDist);
        minLeavesToID = o.minLeavesToID.or(() -> minLeavesToID);
        breakSpeedModifier = o.breakSpeedModifier.or(() -> breakSpeedModifier);
        return this;
    }

    public boolean isLogBlock(BlockState state) {
        return matchesAny(logBlocks, state);
    }

    /** isLeafBlock of 1.5.2 ignored the decay bits (meta &amp; 7); here the leaf block states match regardless of properties. */
    public boolean isLeafBlock(BlockState state) {
        return matchesAny(leafBlocks, state);
    }

    static boolean matchesAny(List<BlockID> list, BlockState state) {
        if (state.isAir()) {
            return false;
        }
        for (BlockID b : list) {
            if (b.matches(state)) {
                return true;
            }
        }
        return false;
    }

    public List<BlockID> logs() {
        return List.copyOf(logBlocks);
    }

    public List<BlockID> leaves() {
        return List.copyOf(leafBlocks);
    }

    public boolean hasLeaves() {
        return !leafBlocks.isEmpty();
    }

    public boolean allowSmartTreeDetection() {
        return allowSmartTreeDetection.orElseGet(TCSettings.ALLOW_SMART_TREE_DETECTION);
    }

    public boolean onlyDestroyUpwards() {
        return onlyDestroyUpwards.orElseGet(TCSettings.ONLY_DESTROY_UPWARDS);
    }

    public boolean requireLeafDecayCheck() {
        return requireLeafDecayCheck.orElseGet(TCSettings.REQUIRE_LEAF_DECAY_CHECK);
    }

    public int maxHorLogBreakDist() {
        return maxHorLogBreakDist.orElseGet(TCSettings.MAX_HOR_LOG_BREAK_DIST);
    }

    public int maxVerLogBreakDist() {
        return maxVerLogBreakDist.orElseGet(TCSettings.MAX_VER_LOG_BREAK_DIST);
    }

    public int maxLeafIDDist() {
        return maxLeafIDDist.orElseGet(TCSettings.MAX_LEAF_ID_DIST);
    }

    public int maxHorLeafBreakDist() {
        return maxHorLeafBreakDist.orElseGet(TCSettings.MAX_HOR_LEAF_BREAK_DIST);
    }

    public int minLeavesToID() {
        return minLeavesToID.orElseGet(TCSettings.MIN_LEAVES_TO_ID);
    }

    public float breakSpeedModifier() {
        return breakSpeedModifier.orElseGet(() -> TCSettings.BREAK_SPEED_MODIFIER.get().floatValue());
    }
}
