package invtweaks.client;

/** InvTweaksConfigSortingRule: a constraint (slot, row, column, rectangle) and a keyword. Unchanged from 1.5.2. */
public final class InvTweaksConfigSortingRule implements Comparable<InvTweaksConfigSortingRule> {
    /** InvTweaksConfigSortingRuleType: the kind of a constraint and its priority range. */
    public enum Type {
        RECTANGLE(1),
        ROW(2),
        COLUMN(3),
        SLOT(4);

        private final int lowestPriority;
        private final int highestPriority;

        Type(int base) {
            this.lowestPriority = base * 1000000;
            this.highestPriority = (base + 1) * 1000000 - 1;
        }

        public int getLowestPriority() {
            return this.lowestPriority;
        }

        public int getHighestPriority() {
            return this.highestPriority;
        }
    }

    private final String constraint;
    private final int[] preferredPositions;
    private final String keyword;
    private final Type type;
    private final int priority;
    private final int containerSize;
    private final int containerRowSize;

    public InvTweaksConfigSortingRule(InvTweaksItemTree tree, String constraint, String keyword, int containerSize, int containerRowSize) {
        this.keyword = keyword;
        this.constraint = constraint;
        this.containerSize = containerSize;
        this.containerRowSize = containerRowSize;
        this.type = getRuleType(constraint, containerRowSize);
        this.preferredPositions = this.getRulePreferredPositions(constraint);
        this.priority = this.type.getLowestPriority() + 100000 + tree.getKeywordDepth(keyword) * 1000 - tree.getKeywordOrder(keyword);
    }

    public Type getType() {
        return this.type;
    }

    public int[] getPreferredSlots() {
        return this.preferredPositions;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public String getRawConstraint() {
        return this.constraint;
    }

    public int getPriority() {
        return this.priority;
    }

    @Override
    public int compareTo(InvTweaksConfigSortingRule other) {
        return this.getPriority() - other.getPriority();
    }

    public int[] getRulePreferredPositions(String constraint) {
        return getRulePreferredPositions(constraint, this.containerSize, this.containerRowSize);
    }

    public static int[] getRulePreferredPositions(String constraint, int containerSize, int containerRowSize) {
        int[] result = null;
        int columnSize = containerSize / containerRowSize;
        if (constraint.length() >= 5) {
            boolean vertical = false;
            if (constraint.contains("v")) {
                vertical = true;
                constraint = constraint.replaceAll("v", "");
            }
            String[] parts = constraint.split("-");
            if (parts.length == 2) {
                int[] from = getRulePreferredPositions(parts[0], containerSize, containerRowSize);
                int[] to = getRulePreferredPositions(parts[1], containerSize, containerRowSize);
                if (from.length == 1 && to.length == 1) {
                    int a = from[0];
                    int b = to[0];
                    int[] p1 = {a % containerRowSize, a / containerRowSize};
                    int[] p2 = {b % containerRowSize, b / containerRowSize};
                    result = new int[(Math.abs(p2[1] - p1[1]) + 1) * (Math.abs(p2[0] - p1[0]) + 1)];
                    int index = 0;
                    if (vertical) {
                        for (int[] p : new int[][]{p1, p2}) {
                            int tmp = p[0];
                            p[0] = p[1];
                            p[1] = tmp;
                        }
                    }
                    for (int i = p1[1]; p1[1] < p2[1] ? i <= p2[1] : i >= p2[1]; i += p1[1] < p2[1] ? 1 : -1) {
                        for (int j = p1[0]; p1[0] < p2[0] ? j <= p2[0] : j >= p2[0]; j += p1[0] < p2[0] ? 1 : -1) {
                            result[index++] = vertical ? index(containerRowSize, j, i) : index(containerRowSize, i, j);
                        }
                    }
                    if (constraint.contains("r")) {
                        reverseArray(result);
                    }
                }
            }
        } else {
            int column = -1;
            int row = -1;
            boolean reverse = false;
            for (int i = 0; i < constraint.length(); i++) {
                char c = constraint.charAt(i);
                if (c >= '1' && c - '1' <= containerRowSize) {
                    column = c - '1';
                } else if (c >= 'a' && c - 'a' <= columnSize) {
                    row = c - 'a';
                } else if (c == 'r') {
                    reverse = true;
                }
            }
            if (column != -1 && row != -1) {
                result = new int[]{index(containerRowSize, row, column)};
            } else if (row != -1) {
                result = new int[containerRowSize];
                for (int i = 0; i < containerRowSize; i++) {
                    result[i] = index(containerRowSize, row, reverse ? containerRowSize - 1 - i : i);
                }
            } else {
                result = new int[columnSize];
                for (int i = 0; i < columnSize; i++) {
                    result[i] = index(containerRowSize, reverse ? i : columnSize - 1 - i, column);
                }
            }
        }
        return result;
    }

    public static Type getRuleType(String constraint, int rowSize) {
        Type type = Type.SLOT;
        if (constraint.length() == 1 || constraint.length() == 2 && constraint.contains("r")) {
            constraint = constraint.replace("r", "");
            if (constraint.charAt(0) - '1' <= rowSize && constraint.charAt(0) >= '1') {
                type = Type.COLUMN;
            } else {
                type = Type.ROW;
            }
        } else if (constraint.length() > 4) {
            if (constraint.charAt(1) == constraint.charAt(4)) {
                type = Type.COLUMN;
            } else if (constraint.charAt(0) == constraint.charAt(3)) {
                type = Type.ROW;
            } else {
                type = Type.RECTANGLE;
            }
        }
        return type;
    }

    @Override
    public String toString() {
        return this.constraint + " " + this.keyword;
    }

    private static int index(int rowSize, int row, int column) {
        return row * rowSize + column;
    }

    private static void reverseArray(int[] array) {
        for (int i = 0, j = array.length - 1; i < j; i++, j--) {
            int tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }
}
