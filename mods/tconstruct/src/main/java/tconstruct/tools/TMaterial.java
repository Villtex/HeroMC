package tconstruct.tools;

import net.minecraft.ChatFormatting;
import org.jspecify.annotations.Nullable;

/**
 * The tool materials of {@code TContent.registerMaterials} (TConstructRegistry.addToolMaterial / addBowMaterial /
 * addArrowMaterial) with the pack configuration (TinkersWorkshop.txt: "Modifiers increase Mining Level" = true, so slime
 * mines at level 3 and blue slime at 1). The id is the material id of 1.5.2 (the item damage of the parts).
 * Thaumium (31) only existed with Thaumcraft, which the pack does not have.
 *
 * @param durability   base durability of a head
 * @param miningSpeed  speed x 100
 * @param handle       handle durability modifier
 * @param reinforced   reinforced level
 * @param stonebound   shoddy value: positive = stonebound (faster when worn, weaker), negative = jagged
 * @param ability      the ability shown in the tooltip ("" none)
 */
public enum TMaterial {
    WOOD(0, "wood", "wood", "Wooden ", 0, 59, 200, 0, 1.0F, 0, 0.0F, ChatFormatting.YELLOW, "", 384, 20, 1.0F, 0.69F, 1.0F, 100.0F),
    STONE(1, "stone", "stone", "Stone ", 1, 131, 400, 1, 0.5F, 0, 1.0F, null, "Stonebound", 10, 80, 0.2F, 2.5F, 5.0F, 100.0F),
    IRON(2, "iron", "iron", "Iron ", 2, 250, 600, 2, 1.3F, 1, 0.0F, ChatFormatting.WHITE, "", 576, 40, 1.2F, 7.2F, 0.5F, 100.0F),
    FLINT(3, "flint", "flint", "Flint ", 1, 171, 525, 2, 0.7F, 0, 0.0F, ChatFormatting.DARK_GRAY, "", 10, 80, 0.2F, 2.65F, 1.0F, 100.0F),
    CACTUS(4, "cactus", "cactus", "Cactus ", 1, 150, 500, 2, 1.0F, 0, -1.0F, ChatFormatting.DARK_GREEN, "Jagged", 384, 20, 1.0F, 0.76F, 1.0F, 100.0F),
    BONE(5, "bone", "bone", "Bone ", 1, 200, 400, 1, 1.0F, 0, 0.0F, ChatFormatting.YELLOW, "", 192, 30, 1.0F, 0.69F, 1.0F, 100.0F),
    OBSIDIAN(6, "obsidian", "obsidian", "Obsidian ", 3, 89, 700, 2, 0.8F, 3, 0.0F, ChatFormatting.LIGHT_PURPLE, "", 10, 80, 0.2F, 2.4F, 1.0F, 100.0F),
    NETHERRACK(7, "netherrack", "netherrack", "Netherrack ", 2, 131, 400, 1, 1.2F, 0, 1.0F, ChatFormatting.DARK_RED, "Stonebound", 10, 80, 0.2F, 3.5F, 1.0F, 100.0F),
    SLIME(8, "slime", "slime", "Slime ", 3, 1500, 150, 0, 2.0F, 0, 0.0F, ChatFormatting.GREEN, "", 1536, 20, 1.2F, 0.42F, 0.0F, 100.0F),
    PAPER(9, "paper", "paper", "Paper ", 0, 30, 200, 0, 0.3F, 0, 0.0F, ChatFormatting.WHITE, "Writable", 48, 25, 0.5F, 1.1F, 3.0F, 90.0F),
    COBALT(10, "cobalt", "cobalt", "Cobalt ", 4, 800, 1100, 3, 1.75F, 2, 0.0F, ChatFormatting.DARK_AQUA, "", 1152, 40, 1.2F, 8.9F, 0.25F, 100.0F),
    ARDITE(11, "ardite", "ardite", "Ardite ", 4, 600, 800, 3, 2.0F, 0, 2.0F, ChatFormatting.DARK_RED, "Stonebound", 960, 40, 1.2F, 7.2F, 0.25F, 100.0F),
    MANYULLYN(12, "manyullyn", "manyullyn", "Manyullyn ", 5, 1200, 900, 4, 2.5F, 0, 0.0F, ChatFormatting.DARK_PURPLE, "", 1536, 40, 1.2F, 10.6F, 0.1F, 100.0F),
    COPPER(13, "copper", "copper", "Copper ", 1, 180, 500, 2, 1.15F, 0, 0.0F, ChatFormatting.RED, "", 384, 40, 1.2F, 8.96F, 0.5F, 100.0F),
    BRONZE(14, "bronze", "bronze", "Bronze ", 2, 350, 700, 2, 1.3F, 1, 0.0F, ChatFormatting.GOLD, "", 576, 40, 1.2F, 7.9F, 0.25F, 100.0F),
    ALUMITE(15, "alumite", "alumite", "Alumite ", 4, 550, 800, 3, 1.3F, 2, 0.0F, ChatFormatting.LIGHT_PURPLE, "", 768, 40, 1.2F, 4.7F, 0.25F, 100.0F),
    STEEL(16, "steel", "steel", "Steel ", 4, 750, 800, 3, 1.3F, 2, 0.0F, null, "", 768, 40, 1.2F, 7.6F, 0.25F, 100.0F),
    BLUE_SLIME(17, "blue_slime", "blueslime", "Slime ", 1, 500, 150, 0, 1.5F, 0, 0.0F, ChatFormatting.AQUA, "", 576, 20, 1.2F, 0.42F, 0.0F, 100.0F);

    private static final TMaterial[] BY_ID = values();

    public final int id;
    /** Registry name part ({@code tconstruct:<key>_pickaxe_head}). */
    public final String key;
    /** The texture prefix of 1.5.2 ({@code parts/<tex>_pickaxe_head.png}). */
    public final String tex;
    /** displayName of 1.5.2 (creative tools are called "Iron Pickaxe"). */
    public final String displayName;
    public final int harvestLevel;
    public final int durability;
    public final int miningSpeed;
    public final int attack;
    public final float handle;
    public final int reinforced;
    public final float stonebound;
    /** tipStyle: the colour of the ability line. */
    public final @Nullable ChatFormatting style;
    public final String ability;
    /** BowMaterial: durability, draw speed (ticks), maximum flight speed. */
    public final int bowDurability;
    public final int bowDrawSpeed;
    public final float bowFlightSpeed;
    /** ArrowMaterial: mass, break chance, accuracy. */
    public final float arrowMass;
    public final float arrowBreakChance;
    public final float arrowAccuracy;

    TMaterial(int id, String key, String tex, String displayName, int harvestLevel, int durability, int miningSpeed, int attack, float handle,
            int reinforced, float stonebound, @Nullable ChatFormatting style, String ability, int bowDurability, int bowDrawSpeed, float bowFlightSpeed,
            float arrowMass, float arrowBreakChance, float arrowAccuracy) {
        this.id = id;
        this.key = key;
        this.tex = tex;
        this.displayName = displayName;
        this.harvestLevel = harvestLevel;
        this.durability = durability;
        this.miningSpeed = miningSpeed;
        this.attack = attack;
        this.handle = handle;
        this.reinforced = reinforced;
        this.stonebound = stonebound;
        this.style = style;
        this.ability = ability;
        this.bowDurability = bowDurability;
        this.bowDrawSpeed = bowDrawSpeed;
        this.bowFlightSpeed = bowFlightSpeed;
        this.arrowMass = arrowMass;
        this.arrowBreakChance = arrowBreakChance;
        this.arrowAccuracy = arrowAccuracy;
    }

    public static @Nullable TMaterial byId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : null;
    }

    /** Materials whose parts the part builder makes with wooden patterns ("Craft metals with Wood Patterns" = false). */
    public boolean nonMetal() {
        return switch (this) {
            case WOOD, STONE, FLINT, CACTUS, BONE, OBSIDIAN, NETHERRACK, SLIME, PAPER, BLUE_SLIME -> true;
            default -> false;
        };
    }

    /** Whether the material has a shard item ({@code toolShard}); wood, bone and paper use sticks, bone meal and paper. */
    public boolean hasShard() {
        return this != WOOD && this != BONE && this != PAPER;
    }

    public String langKey() {
        return "material.tconstruct." + this.key;
    }
}
