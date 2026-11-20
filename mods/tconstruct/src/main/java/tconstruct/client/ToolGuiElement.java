package tconstruct.client;

import java.util.List;

/**
 * {@code ToolGuiElement} / TProxyClient.addToolButtons: a button of the tool station (its icon in icons.png at
 * buttonX/Y * 18), the slot layout it selects, the empty-slot icons and the title / text (lang key
 * {@code gui.tconstruct.tool.<key>}).
 */
public record ToolGuiElement(String key, int slotType, int buttonX, int buttonY, int[] iconsX, int[] iconsY) {
    /** The buttons of the tool station (the first is repair and modification). */
    public static final List<ToolGuiElement> TIER_ONE = List.of(
            new ToolGuiElement("repair", 0, 3, 0, new int[]{0, 1, 2, 13}, new int[]{13, 13, 13, 13}),
            new ToolGuiElement("pickaxe", 1, 4, 0, new int[]{0, 0, 1, 13}, new int[]{2, 3, 3, 13}),
            new ToolGuiElement("shovel", 2, 5, 0, new int[]{3, 0, 13, 13}, new int[]{2, 3, 13, 13}),
            new ToolGuiElement("hatchet", 2, 6, 0, new int[]{2, 0, 13, 13}, new int[]{2, 3, 13, 13}),
            new ToolGuiElement("mattock", 3, 8, 0, new int[]{2, 0, 3, 13}, new int[]{2, 3, 2, 13}),
            new ToolGuiElement("broadsword", 1, 0, 1, new int[]{1, 0, 2, 13}, new int[]{2, 3, 3, 13}),
            new ToolGuiElement("longsword", 1, 1, 1, new int[]{1, 0, 3, 13}, new int[]{2, 3, 3, 13}),
            new ToolGuiElement("rapier", 1, 2, 1, new int[]{1, 0, 4, 13}, new int[]{2, 3, 3, 13}),
            new ToolGuiElement("dagger", 1, 5, 1, new int[]{7, 0, 4, 13}, new int[]{2, 3, 3, 13}),
            new ToolGuiElement("frypan", 2, 3, 1, new int[]{4, 0, 13, 13}, new int[]{2, 3, 13, 13}),
            new ToolGuiElement("battlesign", 2, 4, 1, new int[]{5, 0, 13, 13}, new int[]{2, 3, 13, 13}),
            new ToolGuiElement("chisel", 2, 6, 1, new int[]{7, 0, 13, 13}, new int[]{3, 3, 13, 13}),
            new ToolGuiElement("shortbow", 3, 9, 1, new int[]{0, 10, 0, 13}, new int[]{3, 3, 3, 13}),
            new ToolGuiElement("arrow", 7, 10, 1, new int[]{11, 0, 12, 13}, new int[]{3, 3, 3, 13}));
    /** The extra buttons of the tool forge (TConstructClientRegistry.tierTwoButtons). */
    public static final List<ToolGuiElement> TIER_TWO = List.of(
            new ToolGuiElement("hammer", 6, 13, 0, new int[]{11, 8, 9, 9}, new int[]{2, 3, 2, 2}),
            new ToolGuiElement("lumberaxe", 5, 11, 0, new int[]{6, 8, 9, 9}, new int[]{2, 3, 2, 3}),
            new ToolGuiElement("excavator", 5, 12, 0, new int[]{10, 8, 9, 9}, new int[]{2, 3, 2, 3}),
            new ToolGuiElement("scythe", 4, 10, 0, new int[]{8, 8, 9, 8}, new int[]{2, 3, 3, 3}),
            new ToolGuiElement("cleaver", 5, 7, 1, new int[]{6, 8, 9, 8}, new int[]{3, 3, 2, 3}),
            new ToolGuiElement("battleaxe", 5, 8, 1, new int[]{6, 8, 6, 9}, new int[]{2, 3, 2, 3}));

    /** ToolStationGui / ToolForgeGui.setSlotType: positions of the inputs (the slot is drawn one pixel inside). */
    public static int[][] slotPositions(int type, boolean forge) {
        if (forge) {
            return switch (type) {
                case 1 -> new int[][]{{56, 56, 56, 14}, {19, 55, 37, 37}};
                case 2 -> new int[][]{{56, 56, 14, 14}, {28, 46, 28, 46}};
                case 3 -> new int[][]{{38, 47, 56, 14}, {28, 46, 28, 37}};
                case 4 -> new int[][]{{47, 38, 56, 47}, {19, 37, 37, 55}};
                case 5 -> new int[][]{{38, 47, 56, 47}, {19, 55, 19, 37}};
                case 6 -> new int[][]{{38, 38, 20, 56}, {28, 46, 28, 28}};
                case 7 -> new int[][]{{56, 56, 56, 14}, {19, 37, 55, 37}};
                default -> new int[][]{{56, 38, 38, 14}, {37, 28, 46, 37}};
            };
        }
        return switch (type) {
            case 1 -> new int[][]{{56, 56, 56}, {19, 55, 37}};
            case 2 -> new int[][]{{56, 56, 14}, {28, 46, 37}};
            // case 3 falls through to the default positions of 1.5.2's switch (no break): keeps the last layout there
            case 3 -> new int[][]{{38, 47, 56}, {28, 46, 28}};
            case 7 -> new int[][]{{56, 56, 56}, {19, 37, 55}};
            default -> new int[][]{{56, 38, 38}, {37, 28, 46}};
        };
    }
}
