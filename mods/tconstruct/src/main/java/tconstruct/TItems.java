package tconstruct;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;
import tconstruct.items.CustomPartItem;
import tconstruct.items.PatternItem;
import tconstruct.items.ToolPartItem;
import tconstruct.tools.PartType;
import tconstruct.tools.TMaterial;
import tconstruct.tools.ToolBuilder;
import tconstruct.tools.ToolCoreItem;
import tconstruct.tools.ToolData;
import tconstruct.tools.ToolType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The items of the tool core (TContent.registerItems): materials, blank patterns, wooden patterns, metal casts, tool
 * parts, shards, bowstring, fletching and the 20 tools; the data component {@code tconstruct:tool}; the three creative
 * tabs of 1.5.2 (tools, materials, blocks).
 */
public final class TItems {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TConstruct.MOD_ID);
    static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TConstruct.MOD_ID);
    static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TConstruct.MOD_ID);

    /** The InfiTool compound of a tool. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolData>> TOOL = COMPONENTS.registerComponentType("tool",
            b -> b.persistent(ToolData.CODEC).networkSynchronized(ToolData.STREAM_CODEC));

    /** One old (config id, damage) of 1.5.2 per item, for the id tables of the world generation. */
    public record OldId(int id, int damage, Supplier<? extends Item> item) {
    }

    public static final List<OldId> OLD_IDS = new ArrayList<>();

    /**
     * The 34 damage values of {@code materials} (MaterialItem), registry name or null where today's vanilla item is
     * used (19 = the iron nugget).
     */
    public static final String[] MATERIAL_NAMES = {
            "paper_stack", "slime_crystal", "seared_brick", "cobalt_ingot", "ardite_ingot", "manyullyn_ingot", "moss_ball", "lava_crystal",
            "necrotic_bone", "copper_ingot", "tin_ingot", "aluminum_ingot", "raw_aluminum", "bronze_ingot", "alubrass_ingot", "alumite_ingot",
            "steel_ingot", "blue_slime_crystal", "obsidian_ingot", null, "copper_nugget", "tin_nugget", "aluminum_nugget",
            "false_essence_crystal", "alubrass_nugget", "silky_cloth", "silky_jewel", "obsidian_nugget", "cobalt_nugget", "ardite_nugget",
            "manyullyn_nugget", "bronze_nugget", "alumite_nugget", "steel_nugget"
    };
    public static final Map<String, DeferredItem<Item>> MATERIALS = new LinkedHashMap<>();

    static {
        for (int i = 0; i < MATERIAL_NAMES.length; i++) {
            String name = MATERIAL_NAMES[i];
            if (name == null) {
                OLD_IDS.add(new OldId(6035, i, () -> Items.IRON_NUGGET));
                continue;
            }
            DeferredItem<Item> item = ITEMS.registerSimpleItem(name);
            MATERIALS.put(name, item);
            OLD_IDS.add(new OldId(6035, i, item));
        }
    }

    public static final DeferredItem<Item> PAPER_STACK = MATERIALS.get("paper_stack");
    public static final DeferredItem<Item> SLIME_CRYSTAL = MATERIALS.get("slime_crystal");
    public static final DeferredItem<Item> BLUE_SLIME_CRYSTAL = MATERIALS.get("blue_slime_crystal");
    public static final DeferredItem<Item> MOSS_BALL = MATERIALS.get("moss_ball");
    public static final DeferredItem<Item> LAVA_CRYSTAL = MATERIALS.get("lava_crystal");
    public static final DeferredItem<Item> NECROTIC_BONE = MATERIALS.get("necrotic_bone");
    public static final DeferredItem<Item> OBSIDIAN_INGOT = MATERIALS.get("obsidian_ingot");
    public static final DeferredItem<Item> SILKY_JEWEL = MATERIALS.get("silky_jewel");
    /** craftedSoil 4 (consecrated soil, the smite modifier) comes with part 25f. */
    public static final @Nullable DeferredItem<Item> CONSECRATED_SOIL = null;

    /** blankPattern 0, 1 (cast from aluminum brass) and 2 (cast from gold). */
    public static final DeferredItem<Item> BLANK_PATTERN = ITEMS.registerSimpleItem("blank_pattern");
    public static final DeferredItem<Item> BLANK_CAST = ITEMS.registerSimpleItem("blank_cast");
    public static final DeferredItem<Item> GOLD_BLANK_CAST = ITEMS.registerSimpleItem("gold_blank_cast");

    /** The pattern names of 1.5.2 (Pattern.patternName), index = damage. */
    public static final String[] PATTERN_NAMES = {
            "ingot", "tool_rod", "pickaxe_head", "shovel_head", "axe_head", "sword_blade", "wide_guard", "hand_guard", "crossbar", "binding",
            "pan", "sign_head", "knife_blade", "chisel_head", "tough_rod", "tough_binding", "large_plate", "broad_axe_head", "scythe_blade",
            "excavator_head", "large_sword_blade", "hammer_head", "full_guard", "bowstring", "fletching", "arrowhead"
    };
    /** woodPattern 1..25 ({@code <name>_pattern}). */
    public static final List<DeferredItem<PatternItem>> WOOD_PATTERNS = new ArrayList<>();
    /** metalPattern 0..22 and 25 ({@code <name>_cast}); 23 and 24 (bowstring, fletching) have no cast. */
    public static final List<DeferredItem<PatternItem>> CASTS = new ArrayList<>();

    static {
        for (int i = 0; i < PATTERN_NAMES.length; i++) {
            int meta = i;
            if (i > 0) {
                DeferredItem<PatternItem> pattern = ITEMS.registerItem(PATTERN_NAMES[i] + "_pattern", p -> new PatternItem(meta, false, p),
                        p -> p.stacksTo(1));
                WOOD_PATTERNS.add(pattern);
                OLD_IDS.add(new OldId(6043, i, pattern));
            } else {
                WOOD_PATTERNS.add(null);
            }
            if (i != 23 && i != 24) {
                DeferredItem<PatternItem> cast = ITEMS.registerItem(PATTERN_NAMES[i] + "_cast", p -> new PatternItem(meta, true, p), p -> p.stacksTo(1));
                CASTS.add(cast);
                OLD_IDS.add(new OldId(6036, i, cast));
            } else {
                CASTS.add(null);
            }
        }
        OLD_IDS.add(new OldId(6032, 0, BLANK_PATTERN));
        OLD_IDS.add(new OldId(6032, 1, BLANK_CAST));
        OLD_IDS.add(new OldId(6032, 2, GOLD_BLANK_CAST));
    }

    /** The parts, one item per part and material. */
    private static final Map<PartType, Map<TMaterial, DeferredItem<ToolPartItem>>> PARTS = new EnumMap<>(PartType.class);

    static {
        for (PartType type : PartType.values()) {
            Map<TMaterial, DeferredItem<ToolPartItem>> map = new EnumMap<>(TMaterial.class);
            for (TMaterial material : TMaterial.values()) {
                if (type.exists(material)) {
                    DeferredItem<ToolPartItem> item = ITEMS.registerItem(type.itemName(material), p -> new ToolPartItem(type, material, p));
                    map.put(material, item);
                    OLD_IDS.add(new OldId(type.oldId, material.id, item));
                }
            }
            PARTS.put(type, map);
        }
    }

    /** Bowstring 0 (string); 1 (enchanted fabric) needs Thaumcraft. */
    public static final DeferredItem<CustomPartItem> BOWSTRING = ITEMS.registerItem("bowstring", p -> new CustomPartItem(PartType.BOWSTRING, 0, p));
    /** Fletching 0 feather, 1 leaf, 2 slime, 3 blue slime. */
    public static final List<DeferredItem<CustomPartItem>> FLETCHINGS = new ArrayList<>();

    static {
        OLD_IDS.add(new OldId(6077, 0, BOWSTRING));
        String[] fletchings = {"feather", "leaf", "slime", "blue_slime"};
        for (int i = 0; i < fletchings.length; i++) {
            int id = i;
            DeferredItem<CustomPartItem> item = ITEMS.registerItem(fletchings[i] + "_fletching", p -> new CustomPartItem(PartType.FLETCHING, id, p));
            FLETCHINGS.add(item);
            OLD_IDS.add(new OldId(6079, i, item));
        }
    }

    /** The tools. */
    public static final Map<ToolType, DeferredItem<ToolCoreItem>> TOOLS = new EnumMap<>(ToolType.class);

    static {
        for (ToolType type : ToolType.values()) {
            TOOLS.put(type, ITEMS.registerItem(type.key, p -> new ToolCoreItem(type, p), p -> {
                p.stacksTo(type == ToolType.ARROW ? 64 : 1);
                if (type.blocks()) {
                    p.component(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS, blocking());
                }
                float speed = useSpeed(type);
                if (speed != 0.2F) {
                    p.component(net.minecraft.core.component.DataComponents.USE_EFFECTS,
                            new net.minecraft.world.item.component.UseEffects(false, true, speed));
                }
                return p;
            }));
        }
    }

    /** PotionLauncher (6070, creative only, no recipe in 1.5.2). */
    public static final DeferredItem<tconstruct.items.PotionLauncherItem> POTION_LAUNCHER = ITEMS.registerItem("potion_launcher",
            tconstruct.items.PotionLauncherItem::new, p -> p.stacksTo(1));

    static {
        OLD_IDS.add(new OldId(6070, -1, POTION_LAUNCHER));
    }

    /**
     * The walking speed while the tool is used: 1.5.2 slowed to 0.2 and the tools multiplied it back in onUpdate
     * (blocking weapons x2.5, longsword and battleaxe x5, chisel and shortbow x2).
     */
    private static float useSpeed(ToolType type) {
        if (type.blocks()) {
            return 0.5F;
        }
        return switch (type) {
            case LONGSWORD, BATTLEAXE -> 1.0F;
            case CHISEL, SHORTBOW -> 0.4F;
            default -> 0.2F;
        };
    }

    /** Weapon EnumAction.block: halves the damage like a sword of 1.5.2 (combat152). */
    private static BlocksAttacks blocking() {
        return hexxit.combat152.Combat152.BLOCKING;
    }

    public static ToolPartItem part(PartType type, TMaterial material) {
        DeferredItem<ToolPartItem> item = PARTS.get(type).get(material);
        if (item == null) {
            throw new IllegalArgumentException("no part " + type + " of " + material);
        }
        return item.get();
    }

    public static @Nullable DeferredItem<ToolPartItem> partHolder(PartType type, TMaterial material) {
        return PARTS.get(type).get(material);
    }

    public static ToolCoreItem tool(ToolType type) {
        return TOOLS.get(type).get();
    }

    public static PatternItem woodPattern(int meta) {
        return WOOD_PATTERNS.get(meta).get();
    }

    // ------------------------------------------------------------------ creative tabs

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TOOL_TAB = TABS.register("tools", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tconstruct.tools"))
            .icon(() -> ToolBuilder.creativeTool(ToolType.LONGSWORD, TMaterial.IRON, TMaterial.WOOD, TMaterial.BRONZE))
            .displayItems((params, output) -> {
                for (ToolType type : ToolType.values()) {
                    ToolBuilder.creativeTools(type).forEach(output::accept);
                }
                output.accept(POTION_LAUNCHER.get());
            })
            .build());
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIAL_TAB = TABS.register("materials", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tconstruct.materials"))
            .icon(() -> new ItemStack(part(PartType.PICKAXE_HEAD, TMaterial.IRON)))
            .withTabsBefore(TOOL_TAB.getKey())
            .displayItems((params, output) -> {
                output.accept(BLANK_PATTERN.get());
                output.accept(BLANK_CAST.get());
                MATERIALS.values().forEach(item -> output.accept(item.get()));
                WOOD_PATTERNS.forEach(item -> {
                    if (item != null) {
                        output.accept(item.get());
                    }
                });
                CASTS.forEach(item -> {
                    if (item != null) {
                        output.accept(item.get());
                    }
                });
                for (PartType type : PartType.values()) {
                    PARTS.get(type).forEach((material, item) -> {
                        // ToolPart.getSubItems: 0..16 (blue slime parts only from the part builder)
                        if (material != TMaterial.BLUE_SLIME) {
                            output.accept(item.get());
                        }
                    });
                }
                output.accept(BOWSTRING.get());
                FLETCHINGS.forEach(item -> output.accept(item.get()));
            })
            .build());

    private TItems() {
    }

    static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        COMPONENTS.register(modBus);
        TABS.register(modBus);
    }
}
