package bspkrs.treecapitator;

import bspkrs.util.BlockID;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The trees of one registry access (server or client), built from the treecapitator:tree data entries with the merge
 * rules of TreeRegistry.registerTree: the vanilla trees (namespace treecapitator) first, then the other namespaces;
 * entries of the same key are appended, a tree that shares a log with an existing tree absorbs it.
 */
public final class TreeRegistry {
    private static Registry<TreeDefinition.Data> lastSource;
    private static TreeRegistry lastRegistry;

    private final Map<String, TreeDefinition> treeDefs = new LinkedHashMap<>();
    private final Map<BlockID, String> logToStringMap = new HashMap<>();
    private final TreeDefinition masterDefinition = new TreeDefinition();

    public static synchronized TreeRegistry of(RegistryAccess access) {
        Registry<TreeDefinition.Data> source = access.lookupOrThrow(TreeCapitatorMod.TREE_REGISTRY);
        if (source != lastSource) {
            lastSource = source;
            lastRegistry = new TreeRegistry(source);
        }
        return lastRegistry;
    }

    private TreeRegistry(Registry<TreeDefinition.Data> source) {
        List<Identifier> ids = new ArrayList<>(source.keySet());
        ids.sort(Comparator.comparing((Identifier id) -> !id.getNamespace().equals(TreeCapitatorMod.MOD_ID))
                .thenComparing(Identifier::getNamespace).thenComparing(Identifier::getPath));
        for (Identifier id : ids) {
            source.getOptional(id).ifPresent(d -> registerTree(id.getPath(), new TreeDefinition(d)));
        }
    }

    private boolean isRegisteredLog(BlockID log) {
        return masterDefinition.logBlocks.contains(log);
    }

    /** TreeRegistry.registerTree of r14. */
    private void registerTree(String newKey, TreeDefinition newTD) {
        List<String> sharedLogTrees = new LinkedList<>();
        Map<BlockID, String> toAdd = new HashMap<>();
        for (BlockID log : newTD.logs()) {
            if (!isRegisteredLog(log)) {
                toAdd.put(log, newKey);
            } else if (logToStringMap.containsKey(log) && !sharedLogTrees.contains(logToStringMap.get(log))) {
                sharedLogTrees.add(logToStringMap.get(log));
            }
        }
        if (!treeDefs.containsKey(newKey) && sharedLogTrees.isEmpty()) {
            treeDefs.put(newKey, newTD);
            logToStringMap.putAll(toAdd);
        } else if (sharedLogTrees.isEmpty()) {
            treeDefs.get(newKey).appendWithSettings(newTD);
            logToStringMap.putAll(toAdd);
        } else {
            for (String existingKey : sharedLogTrees) {
                TreeDefinition existing = treeDefs.remove(existingKey);
                if (existing != null) {
                    newTD.appendWithSettings(existing);
                }
            }
            TreeDefinition same = treeDefs.remove(newKey);
            if (same != null) {
                newTD.appendWithSettings(same);
            }
            treeDefs.put(newKey, newTD);
            for (BlockID log : newTD.logs()) {
                logToStringMap.put(log, newKey);
            }
        }
        masterDefinition.append(treeDefs.get(newKey));
    }

    public boolean isRegistered(BlockState state) {
        return masterDefinition.isLogBlock(state);
    }

    /** The tree of a log: with useStrictBlockPairing the definition the log belongs to, otherwise all trees as one. */
    public TreeDefinition get(BlockState state) {
        if (!isRegistered(state)) {
            return null;
        }
        if (TCSettings.USE_STRICT_BLOCK_PAIRING.get()) {
            for (Map.Entry<String, TreeDefinition> e : treeDefs.entrySet()) {
                if (e.getValue().isLogBlock(state)) {
                    return e.getValue();
                }
            }
        }
        return masterDefinition;
    }

    public TreeDefinition masterDefinition() {
        return masterDefinition;
    }

    public Map<String, TreeDefinition> trees() {
        return java.util.Collections.unmodifiableMap(treeDefs);
    }
}
