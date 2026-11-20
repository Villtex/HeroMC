package tconstruct.tools;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;
import tconstruct.TConstruct;

import java.util.Set;

import static tconstruct.tools.PartType.*;

/**
 * The 20 tools of {@code items/tools} (ToolCore subclasses) with their values: base damage (constructor),
 * the parts (getHeadItem / getHandleItem / getAccessoryItem / getExtraItem), how the parts add to the durability
 * (durabilityTypeHandle / Accessory / Extra: 2 = a second head, 1 = a handle, 0 = nothing), getDurabilityModifier,
 * getRepairCost, getDamageModifier, chargeAttack, pierceArmor and toolCategories. The order is the order of the tool
 * recipes in {@code TContent.addCraftingRecipes} (ToolBuilder.getMatchingRecipe takes the first match).
 */
public enum ToolType {
    PICKAXE("pickaxe", 6069, Kind.HARVEST, 1, PICKAXE_HEAD, TOOL_ROD, BINDING, null, 0, 0, 1.0F, -1, 1.0F, "pickaxe", null, 100, 72, Set.of("harvest")),
    BROADSWORD("broadsword", 6060, Kind.WEAPON, 4, SWORD_BLADE, TOOL_ROD, WIDE_GUARD, null, 0, 0, 1.2F, -1, 1.0F, null, null, 0, 0, Set.of("weapon", "melee")),
    HATCHET("hatchet", 6058, Kind.HARVEST, 3, AXE_HEAD, TOOL_ROD, null, null, 0, 0, 1.0F, -1, 1.0F, "hatchet", null, 100, 72, Set.of("harvest")),
    SHOVEL("shovel", 6073, Kind.HARVEST, 2, SHOVEL_HEAD, TOOL_ROD, null, null, 0, 0, 1.0F, -1, 1.0F, "shovel", null, 100, 72, Set.of("harvest")),
    LONGSWORD("longsword", 6066, Kind.WEAPON, 4, SWORD_BLADE, TOOL_ROD, HAND_GUARD, null, 0, 0, 1.0F, -1, 1.0F, null, null, 0, 0, Set.of("weapon", "melee")),
    RAPIER("rapier", 6071, Kind.WEAPON, 2, SWORD_BLADE, TOOL_ROD, CROSSBAR, null, 0, 0, 0.7F, -1, 0.8F, null, null, 0, 0, Set.of("weapon", "melee")),
    FRYPAN("frypan", 6063, Kind.WEAPON, 2, PAN, TOOL_ROD, null, null, 0, 0, 1.0F, -1, 1.0F, null, null, 0, 0, Set.of("weapon", "melee")),
    BATTLESIGN("battlesign", 6059, Kind.WEAPON, 1, SIGN_HEAD, TOOL_ROD, null, null, 0, 0, 1.0F, -1, 1.0F, null, null, 0, 0, Set.of("weapon", "melee")),
    MATTOCK("mattock", 6068, Kind.DUAL, 3, AXE_HEAD, TOOL_ROD, SHOVEL_HEAD, null, 2, 0, 1.0F, -1, 1.0F, "mattock_axe", "mattock_shovel", 100, 0,
            Set.of("harvest", "dualharvest")),
    DAGGER("dagger", 6062, Kind.WEAPON, 1, KNIFE_BLADE, TOOL_ROD, CROSSBAR, null, 0, 0, 1.0F, -1, 1.0F, null, null, 0, 0, Set.of("weapon", "melee", "throwing")),
    CUTLASS("cutlass", 6090, Kind.WEAPON, 4, SWORD_BLADE, TOOL_ROD, FULL_GUARD, null, 1, 0, 1.0F, -1, 1.0F, null, null, 0, 0, Set.of("weapon", "melee")),
    CHISEL("chisel", 6061, Kind.CORE, 0, CHISEL_HEAD, TOOL_ROD, null, null, 0, 0, 1.0F, -1, 1.0F, null, null, 0, 0, Set.of("utility")),
    SCYTHE("scythe", 6093, Kind.WEAPON, 4, SCYTHE_BLADE, TOUGH_ROD, TOUGH_BINDING, TOUGH_ROD, 1, 1, 3.0F, 4.0F, 0.75F, null, null, 0, 0,
            Set.of("weapon", "melee", "harvest")),
    LUMBERAXE("lumberaxe", 6067, Kind.HARVEST, 0, BROAD_AXE_HEAD, TOUGH_ROD, LARGE_PLATE, TOUGH_BINDING, 2, 1, 2.5F, 4.0F, 1.0F, "lumberaxe", null, 300,
            72, Set.of("harvest")),
    CLEAVER("cleaver", 6089, Kind.WEAPON, 4, LARGE_SWORD_BLADE, TOUGH_ROD, LARGE_PLATE, TOUGH_ROD, 2, 1, 2.5F, 4.0F, 1.25F, null, null, 0, 0,
            Set.of("weapon", "melee")),
    EXCAVATOR("excavator", 6091, Kind.HARVEST, 2, EXCAVATOR_HEAD, TOUGH_ROD, LARGE_PLATE, TOUGH_BINDING, 2, 1, 2.75F, 4.0F, 1.0F, "shovel", null, 300,
            72, Set.of("harvest")),
    HAMMER("hammer", 6092, Kind.HARVEST, 2, HAMMER_HEAD, TOUGH_ROD, LARGE_PLATE, LARGE_PLATE, 2, 2, 4.5F, -1, 1.0F, "hammer", null, 300, 216,
            Set.of("weapon", "harvest", "melee", "bludgeoning")),
    BATTLEAXE("battleaxe", 6088, Kind.HARVEST, 4, BROAD_AXE_HEAD, TOUGH_ROD, BROAD_AXE_HEAD, TOUGH_BINDING, 2, 1, 2.5F, 4.0F, 1.0F, "lumberaxe", null,
            100, 72, Set.of("weapon", "harvest", "melee", "slicing")),
    /** BowRecipe(toolRod, bowstring, toolRod): head and accessory are the limbs, the handle is the string. */
    SHORTBOW("shortbow", 6072, Kind.CORE, 0, TOOL_ROD, BOWSTRING, TOOL_ROD, null, 2, 0, 1.0F, -1, 1.0F, null, null, 0, 0,
            Set.of("weapon", "ranged", "bow")),
    ARROW("arrow", 6087, Kind.CORE, 3, ARROWHEAD, TOOL_ROD, FLETCHING, null, 0, 0, 1.0F, -1, 1.0F, null, null, 0, 0, Set.of("ammo"));

    /** HarvestTool, DualHarvestTool, Weapon or a plain ToolCore. */
    public enum Kind { HARVEST, DUAL, WEAPON, CORE }

    public final String key;
    public final int oldId;
    public final Kind kind;
    public final int baseDamage;
    public final PartType head;
    public final PartType handle;
    public final @Nullable PartType accessory;
    public final @Nullable PartType extra;
    public final int durabilityTypeAccessory;
    public final int durabilityTypeExtra;
    public final float durabilityModifier;
    public final float repairCost;
    public final float damageModifier;
    /** getEffectiveMaterials as block tags {@code tconstruct:effective/<name>} (the second set of the mattock). */
    public final @Nullable TagKey<Block> effective;
    public final @Nullable TagKey<Block> effective2;
    /** The divisor of the summed mining speed (heads x 100, the broad tools heads x 300) and of the shoddy log term. */
    public final int speedDivisor;
    public final int shoddyDivisor;
    public final Set<String> categories;

    ToolType(String key, int oldId, Kind kind, int baseDamage, PartType head, PartType handle, @Nullable PartType accessory, @Nullable PartType extra,
            int durabilityTypeAccessory, int durabilityTypeExtra, float durabilityModifier, float repairCost, float damageModifier,
            @Nullable String effective, @Nullable String effective2, int speedDivisor, int shoddyDivisor, Set<String> categories) {
        this.key = key;
        this.oldId = oldId;
        this.kind = kind;
        this.baseDamage = baseDamage;
        this.head = head;
        this.handle = handle;
        this.accessory = accessory;
        this.extra = extra;
        this.durabilityTypeAccessory = durabilityTypeAccessory;
        this.durabilityTypeExtra = durabilityTypeExtra;
        this.durabilityModifier = durabilityModifier;
        this.repairCost = repairCost < 0 ? durabilityModifier : repairCost;
        this.damageModifier = damageModifier;
        this.effective = effective == null ? null : TagKey.create(Registries.BLOCK, TConstruct.id("effective/" + effective));
        this.effective2 = effective2 == null ? null : TagKey.create(Registries.BLOCK, TConstruct.id("effective/" + effective2));
        this.speedDivisor = speedDivisor;
        this.shoddyDivisor = shoddyDivisor;
        this.categories = categories;
    }

    /** durabilityTypeHandle: always 1 (a handle). */
    public int durabilityTypeHandle() {
        return 1;
    }

    /** getModifierAmount: 3 for every tool. */
    public int modifierAmount() {
        return 3;
    }

    /** getPartAmount: how many parts the tooltip and the reinforced name look at. */
    public int partAmount() {
        return this.extra != null ? 4 : this.accessory != null ? 3 : 2;
    }

    /** chargeAttack: the sprint lunge of the longsword. */
    public float chargeAttack() {
        return this == LONGSWORD ? 1.5F : 1.0F;
    }

    public boolean pierceArmor() {
        return this == RAPIER;
    }

    /** Weapon.getItemUseAction = block (Broadsword, Cutlass, Frying Pan, Battlesign, Scythe, Cleaver). */
    public boolean blocks() {
        return this == BROADSWORD || this == CUTLASS || this == FRYPAN || this == BATTLESIGN || this == SCYTHE || this == CLEAVER;
    }

    /** Axes do not wear on leaves (Hatchet / LumberAxe / Battleaxe.onBlockDestroyed). */
    public boolean sparesLeaves() {
        return this == HATCHET || this == LUMBERAXE || this == BATTLEAXE;
    }

    /** ToolCore.onItemUse: places the block of the next hotbar slot (tools that do not override it). */
    public boolean placesBlocks() {
        return this == PICKAXE || this == SHOVEL || this == HATCHET || this == LUMBERAXE || this == EXCAVATOR || this == HAMMER;
    }

    public boolean is(String category) {
        return this.categories.contains(category);
    }

    /** The parts in the order head, handle, accessory, extra. */
    public PartType[] parts() {
        return this.extra != null ? new PartType[]{this.head, this.handle, this.accessory, this.extra}
                : this.accessory != null ? new PartType[]{this.head, this.handle, this.accessory} : new PartType[]{this.head, this.handle};
    }

    public String langKey() {
        return "item.tconstruct." + this.key;
    }
}
