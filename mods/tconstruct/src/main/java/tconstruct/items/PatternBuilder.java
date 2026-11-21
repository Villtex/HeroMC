package tconstruct.items;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import tconstruct.TItems;
import tconstruct.tools.PartType;
import tconstruct.tools.TMaterial;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * {@code library.crafting.PatternBuilder} with the registrations of TContent.registerMaterials (all "Enable mod ...
 * tools" options are on in the pack): which items are worth how much of which material in the part builder, and the
 * shard and rod of every material. The first matching entry wins, as in 1.5.2.
 */
public final class PatternBuilder {
    /** An ItemKey: an item (any damage where 1.5.2 had 32767), its value in half ingots and its material. */
    public record ItemKey(Predicate<ItemStack> test, Supplier<List<ItemStack>> examples, int value, TMaterial material) {
    }

    /** A MaterialSet: the shard (the leftover) and the rod of a material. */
    public record MaterialSet(Supplier<ItemStack> shard, Supplier<ItemStack> rod) {
    }

    private static final List<ItemKey> MATERIALS = new ArrayList<>();
    private static final Map<TMaterial, MaterialSet> SETS = new EnumMap<>(TMaterial.class);

    static {
        // registerFullMaterial(material, 2, key, shard, rod, id): the material (2) and the shard (1)
        full(stack -> stack.is(ItemTags.PLANKS), () -> List.of(new ItemStack(Items.OAK_PLANKS)), TMaterial.WOOD, () -> new ItemStack(Items.STICK),
                () -> new ItemStack(Items.STICK));
        full(Items.STONE, TMaterial.STONE);
        material(Items.COBBLESTONE, 2, TMaterial.STONE);
        full(Items.IRON_INGOT, TMaterial.IRON);
        full(Items.FLINT, TMaterial.FLINT);
        full(Items.CACTUS, TMaterial.CACTUS);
        full(stack -> stack.is(Items.BONE), () -> List.of(new ItemStack(Items.BONE)), TMaterial.BONE, () -> new ItemStack(Items.BONE_MEAL),
                () -> new ItemStack(Items.BONE));
        full(Items.OBSIDIAN, TMaterial.OBSIDIAN);
        material(TItems.OBSIDIAN_INGOT, 2, TMaterial.OBSIDIAN);
        full(Items.NETHERRACK, TMaterial.NETHERRACK);
        full(stack -> stack.is(TItems.SLIME_CRYSTAL.get()), () -> List.of(new ItemStack(TItems.SLIME_CRYSTAL.get())), TMaterial.SLIME,
                () -> shard(TMaterial.SLIME), () -> rod(TMaterial.SLIME));
        // paper: the shard is paper x2 (a value 1 item)
        full(stack -> stack.is(TItems.PAPER_STACK.get()), () -> List.of(new ItemStack(TItems.PAPER_STACK.get())), TMaterial.PAPER,
                () -> new ItemStack(Items.PAPER, 2), () -> rod(TMaterial.PAPER));
        for (TMaterial metal : new TMaterial[]{TMaterial.COBALT, TMaterial.ARDITE, TMaterial.MANYULLYN, TMaterial.COPPER, TMaterial.BRONZE,
                TMaterial.ALUMITE, TMaterial.STEEL}) {
            set(metal, () -> shard(metal), () -> rod(metal));
        }
        full(stack -> stack.is(TItems.BLUE_SLIME_CRYSTAL.get()), () -> List.of(new ItemStack(TItems.BLUE_SLIME_CRYSTAL.get())), TMaterial.BLUE_SLIME,
                () -> shard(TMaterial.BLUE_SLIME), () -> rod(TMaterial.BLUE_SLIME));
        // Natura (NContent.modIntegration): the saguaro is cactus, worth 2
        net.minecraft.resources.Identifier saguaro = net.minecraft.resources.Identifier.fromNamespaceAndPath("natura", "saguaro");
        MATERIALS.add(new ItemKey(stack -> saguaro.equals(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())),
                () -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(saguaro).map(i -> List.of(new ItemStack(i))).orElse(List.of()), 2,
                TMaterial.CACTUS));
    }

    private PatternBuilder() {
    }

    private static ItemStack shard(TMaterial material) {
        return new ItemStack(TItems.part(PartType.SHARD, material));
    }

    private static ItemStack rod(TMaterial material) {
        return new ItemStack(TItems.part(PartType.TOOL_ROD, material));
    }

    private static void full(Item item, TMaterial material) {
        full(stack -> stack.is(item), () -> List.of(new ItemStack(item)), material, () -> shard(material), () -> rod(material));
    }

    private static void full(Predicate<ItemStack> test, Supplier<List<ItemStack>> examples, TMaterial material, Supplier<ItemStack> shard,
            Supplier<ItemStack> rod) {
        MATERIALS.add(new ItemKey(test, examples, 2, material));
        set(material, shard, rod);
    }

    private static void material(Item item, int value, TMaterial material) {
        MATERIALS.add(new ItemKey(stack -> stack.is(item), () -> List.of(new ItemStack(item)), value, material));
    }

    private static void material(Supplier<? extends Item> item, int value, TMaterial material) {
        MATERIALS.add(new ItemKey(stack -> stack.is(item.get()), () -> List.of(new ItemStack(item.get())), value, material));
    }

    /** registerMaterialSet: the set, and the shard as a material worth 1. */
    private static void set(TMaterial material, Supplier<ItemStack> shard, Supplier<ItemStack> rod) {
        SETS.put(material, new MaterialSet(shard, rod));
        MATERIALS.add(new ItemKey(stack -> {
            ItemStack s = shard.get();
            return stack.is(s.getItem());
        }, () -> List.of(shard.get().copyWithCount(1)), 1, material));
    }

    public static List<ItemKey> materials() {
        return MATERIALS;
    }

    public static @Nullable ItemKey itemKey(ItemStack material) {
        if (material.isEmpty()) {
            return null;
        }
        for (ItemKey key : MATERIALS) {
            if (key.test().test(material)) {
                return key;
            }
        }
        return null;
    }

    /** getPartID: the material id of the item's material set, 32767 for no material. */
    public static int partId(ItemStack material) {
        ItemKey key = itemKey(material);
        return key == null ? 32767 : key.material().id;
    }

    /** getPartValue: the item's value, or the value of a custom material (string, feather, leaves). */
    public static int partValue(ItemStack material) {
        if (material.isEmpty()) {
            return 0;
        }
        ItemKey key = itemKey(material);
        return key != null ? key.value() : CustomMaterials.value(material);
    }

    public static @Nullable MaterialSet set(TMaterial material) {
        return SETS.get(material);
    }

    /**
     * getToolPart with the PartBuilderEvent of TEventHandler.craftPart: the part (and the leftover shard, or empty) a
     * pattern makes of the material, or null. Bowstring (pattern 23, three string) and fletching (pattern 24) come from
     * their custom materials. Parts exist for the non-metals only ("Craft metals with Wood Patterns" = false).
     */
    public static ItemStack @Nullable [] toolPart(ItemStack material, ItemStack pattern) {
        if (material.isEmpty() || pattern.isEmpty() || !(pattern.getItem() instanceof PatternItem p) || p.cast) {
            return null;
        }
        if (p.meta == 23) {
            return material.getCount() >= 3 && material.is(Items.STRING) ? new ItemStack[]{new ItemStack(TItems.BOWSTRING.get()), ItemStack.EMPTY}
                    : null;
        }
        if (p.meta == 24) {
            int id = CustomMaterials.fletchingId(material);
            return id >= 0 ? new ItemStack[]{new ItemStack(TItems.FLETCHINGS.get(id).get()), ItemStack.EMPTY} : null;
        }
        ItemKey key = itemKey(material);
        if (key == null) {
            return null;
        }
        MaterialSet set = SETS.get(key.material());
        ItemStack part = partFor(p.meta, key.material());
        if (part == null || set == null) {
            return null;
        }
        int patternValue = p.cost();
        int totalMaterial = key.value() * material.getCount();
        if (totalMaterial < patternValue) {
            return null;
        }
        if (patternValue == key.value()) {
            return new ItemStack[]{part, ItemStack.EMPTY};
        }
        if (patternValue % 2 == 1) {
            return new ItemStack[]{part, set.shard().get().copy()};
        }
        return new ItemStack[]{part, ItemStack.EMPTY};
    }

    /** The part mapping (TConstructRegistry.addPartMapping) of a wooden pattern for a material, or null. */
    public static @Nullable ItemStack partFor(int patternMeta, TMaterial material) {
        if (!material.nonMetal()) {
            return null;
        }
        PartType type = PartType.byPattern(patternMeta);
        if (type == null || !type.exists(material)) {
            return null;
        }
        return new ItemStack(TItems.part(type, material));
    }
}
