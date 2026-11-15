package invtweaks.client;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * InvTweaksConfigInventoryRuleset: one section of InvTweaksRules.txt (rules, LOCKED and FROZEN slots, AUTOREFILL
 * keywords, DEBUG). Keywords may also be item ids now ("D5 minecraft:stone"), the 1.5.2 numeric ids are gone.
 */
public final class InvTweaksConfigInventoryRuleset {
    /** Keyword characters: the original allowed \w only, ids need ':' '/' '.' '-' as well. */
    private static final String KEYWORD = "[\\w:./-]*";
    private static final String ITEM_ID = "^[a-z0-9_.-]+:[a-z0-9_./-]+$";
    private final InvTweaksItemTree tree;
    private final String name;
    private final int[] lockPriorities = new int[InvTweaksConst.INVENTORY_SIZE];
    private final boolean[] frozenSlots = new boolean[InvTweaksConst.INVENTORY_SIZE];
    private final List<Integer> lockedSlots = new ArrayList<>();
    private final List<InvTweaksConfigSortingRule> rules = new ArrayList<>();
    private final List<String> autoReplaceRules = new ArrayList<>();
    private boolean debugEnabled = false;

    public InvTweaksConfigInventoryRuleset(InvTweaksItemTree tree, String name) {
        this.tree = tree;
        this.name = name.trim();
    }

    /** Returns the invalid keyword of the line, or null; throws for a line that is no rule at all. */
    public String registerLine(String rawLine) throws InvalidParameterException {
        String line = rawLine.replaceAll("[\\s]+", " ").toLowerCase();
        String[] words = line.split(" ");
        if (words.length == 2) {
            if (line.matches("^([a-d]|[1-9]|[r]){1,2} " + KEYWORD + "$")
                    || line.matches("^[a-d][1-9]-[a-d][1-9][rv]?[rv]? " + KEYWORD + "$")) {
                if (words[1].equals("locked")) {
                    int[] slots = InvTweaksConfigSortingRule.getRulePreferredPositions(words[0], 36, 9);
                    int priority = InvTweaksConfigSortingRule.getRuleType(words[0], 9).getLowestPriority() - 1;
                    for (int slot : slots) {
                        this.lockPriorities[slot] = priority;
                    }
                    return null;
                }
                if (words[1].equals("frozen")) {
                    for (int slot : InvTweaksConfigSortingRule.getRulePreferredPositions(words[0], 36, 9)) {
                        this.frozenSlots[slot] = true;
                    }
                    return null;
                }
                String keyword = words[1];
                boolean valid = this.tree.isKeywordValid(keyword);
                if (!valid) {
                    if (keyword.matches(ITEM_ID)) {
                        valid = true;
                    } else {
                        for (String variant : this.getKeywordVariants(keyword)) {
                            if (this.tree.isKeywordValid(variant.toLowerCase())) {
                                valid = true;
                                keyword = variant;
                                break;
                            }
                        }
                    }
                }
                if (valid) {
                    this.rules.add(new InvTweaksConfigSortingRule(this.tree, words[0], keyword.toLowerCase(), 36, 9));
                    return null;
                }
                return keyword.toLowerCase();
            }
            if (!words[0].equals("autorefill") && !words[0].equals("autoreplace")) {
                throw new InvalidParameterException();
            }
            if (this.tree.isKeywordValid(words[1]) || words[1].equals("nothing") || words[1].matches(ITEM_ID)) {
                this.autoReplaceRules.add(words[1]);
            }
            return null;
        }
        if (words.length == 1 && words[0].equals("debug")) {
            this.debugEnabled = true;
            return null;
        }
        throw new InvalidParameterException();
    }

    public void finalizeRules() {
        if (this.autoReplaceRules.isEmpty()) {
            if (this.tree.getRootCategory() == null) {
                throw new NullPointerException("No root category is defined.");
            }
            this.autoReplaceRules.add(this.tree.getRootCategory().getName());
        }
        this.rules.sort(Collections.reverseOrder());
        for (int i = 0; i < this.lockPriorities.length; i++) {
            if (this.lockPriorities[i] > 0) {
                this.lockedSlots.add(i);
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public int[] getLockPriorities() {
        return this.lockPriorities;
    }

    public boolean[] getFrozenSlots() {
        return this.frozenSlots;
    }

    public List<Integer> getLockedSlots() {
        return this.lockedSlots;
    }

    public List<InvTweaksConfigSortingRule> getRules() {
        return this.rules;
    }

    public List<String> getAutoReplaceRules() {
        return this.autoReplaceRules;
    }

    public boolean isDebugEnabled() {
        return this.debugEnabled;
    }

    private List<String> getKeywordVariants(String keyword) {
        List<String> variants = new ArrayList<>();
        if (keyword.endsWith("es")) {
            variants.add(keyword.substring(0, keyword.length() - 2));
        }
        if (keyword.endsWith("s")) {
            variants.add(keyword.substring(0, keyword.length() - 1));
        }
        if (keyword.contains("en")) {
            variants.add(keyword.replaceAll("en", ""));
        } else {
            if (keyword.contains("wood")) {
                variants.add(keyword.replaceAll("wood", "wooden"));
            }
            if (keyword.contains("gold")) {
                variants.add(keyword.replaceAll("gold", "golden"));
            }
        }
        if (keyword.matches("\\w*[A-Z]\\w*")) {
            byte[] bytes = keyword.getBytes();
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] >= 'A' && bytes[i] <= 'Z') {
                    String rotated = (keyword.substring(i) + keyword.substring(0, i)).toLowerCase();
                    variants.add(rotated);
                    variants.addAll(this.getKeywordVariants(rotated));
                }
            }
        }
        return variants;
    }
}
