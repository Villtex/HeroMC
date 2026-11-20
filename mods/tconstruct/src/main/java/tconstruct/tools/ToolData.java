package tconstruct.tools;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Map;

/**
 * The {@code InfiTool} compound of 1.5.2 as the data component {@code tconstruct:tool}. Work with it through
 * {@link InfiTool} (a mutable copy with the field names of 1.5.2).
 *
 * @param head      material id of the head, the parts as in "Head", "Handle", "Accessory", "Extra" (-1 = no part)
 * @param stats     attack, mining speeds and levels, reinforced, shoddy, free modifiers
 * @param durability damage, total / base / bonus durability, durability modifier, broken flag, repair count
 * @param mods      effect icons, tooltips and the values of the modifiers (and the bow / arrow stats)
 */
public record ToolData(int head, int handle, int accessory, int extra, Stats stats, Durability durability, Mods mods) {
    /**
     * MiningSpeed / HarvestLevel, the optional second heads ("...Handle", "...2", "...Extra", -1 = absent), Unbreaking,
     * Shoddy and Modifiers.
     */
    public record Stats(int attack, int miningSpeed, int harvestLevel, int miningSpeedHandle, int harvestLevelHandle, int miningSpeed2,
            int harvestLevel2, int miningSpeedExtra, int harvestLevelExtra, int unbreaking, float shoddy, int modifiers) {
        public static final Codec<Stats> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("attack").forGetter(Stats::attack),
                Codec.INT.fieldOf("mining_speed").forGetter(Stats::miningSpeed),
                Codec.INT.fieldOf("harvest_level").forGetter(Stats::harvestLevel),
                Codec.INT.optionalFieldOf("mining_speed_handle", -1).forGetter(Stats::miningSpeedHandle),
                Codec.INT.optionalFieldOf("harvest_level_handle", -1).forGetter(Stats::harvestLevelHandle),
                Codec.INT.optionalFieldOf("mining_speed_2", -1).forGetter(Stats::miningSpeed2),
                Codec.INT.optionalFieldOf("harvest_level_2", -1).forGetter(Stats::harvestLevel2),
                Codec.INT.optionalFieldOf("mining_speed_extra", -1).forGetter(Stats::miningSpeedExtra),
                Codec.INT.optionalFieldOf("harvest_level_extra", -1).forGetter(Stats::harvestLevelExtra),
                Codec.INT.optionalFieldOf("unbreaking", 0).forGetter(Stats::unbreaking),
                Codec.FLOAT.optionalFieldOf("shoddy", 0.0F).forGetter(Stats::shoddy),
                Codec.INT.optionalFieldOf("modifiers", 0).forGetter(Stats::modifiers)
        ).apply(i, Stats::new));
    }

    /** Damage, TotalDurability, BaseDurability, BonusDurability, ModDurability, Broken, RepairCount. */
    public record Durability(int damage, int total, int base, int bonus, float modifier, boolean broken, int repairCount) {
        public static final Codec<Durability> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("damage", 0).forGetter(Durability::damage),
                Codec.INT.fieldOf("total").forGetter(Durability::total),
                Codec.INT.fieldOf("base").forGetter(Durability::base),
                Codec.INT.optionalFieldOf("bonus", 0).forGetter(Durability::bonus),
                Codec.FLOAT.optionalFieldOf("modifier", 0.0F).forGetter(Durability::modifier),
                Codec.BOOL.optionalFieldOf("broken", false).forGetter(Durability::broken),
                Codec.INT.optionalFieldOf("repair_count", 0).forGetter(Durability::repairCount)
        ).apply(i, Durability::new));
    }

    /** "Tooltip#" (the line of the item tooltip) and "ModifierTip#" (the line of the tool station). */
    public record Tip(String tooltip, String modifierTip) {
        public static final Codec<Tip> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("tooltip").forGetter(Tip::tooltip),
                Codec.STRING.fieldOf("modifier").forGetter(Tip::modifierTip)
        ).apply(i, Tip::new));
    }

    /**
     * "Effect1".."Effect6" (render layers of the modifiers), the tooltips, and every other key of 1.5.2 by name: ints,
     * booleans (1) and int arrays as int lists ("Redstone" = [amount, max, tooltip]), floats as floats.
     */
    public record Mods(List<Integer> effects, List<Tip> tips, Map<String, List<Integer>> ints, Map<String, Float> floats) {
        public static final Mods EMPTY = new Mods(List.of(), List.of(), Map.of(), Map.of());
        public static final Codec<Mods> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.listOf().optionalFieldOf("effects", List.of()).forGetter(Mods::effects),
                Tip.CODEC.listOf().optionalFieldOf("tips", List.of()).forGetter(Mods::tips),
                Codec.unboundedMap(Codec.STRING, Codec.INT.listOf()).optionalFieldOf("ints", Map.of()).forGetter(Mods::ints),
                Codec.unboundedMap(Codec.STRING, Codec.FLOAT).optionalFieldOf("floats", Map.of()).forGetter(Mods::floats)
        ).apply(i, Mods::new));
    }

    public static final Codec<ToolData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("head").forGetter(ToolData::head),
            Codec.INT.fieldOf("handle").forGetter(ToolData::handle),
            Codec.INT.optionalFieldOf("accessory", -1).forGetter(ToolData::accessory),
            Codec.INT.optionalFieldOf("extra", -1).forGetter(ToolData::extra),
            Stats.CODEC.fieldOf("stats").forGetter(ToolData::stats),
            Durability.CODEC.fieldOf("durability").forGetter(ToolData::durability),
            Mods.CODEC.optionalFieldOf("mods", Mods.EMPTY).forGetter(ToolData::mods)
    ).apply(i, ToolData::new));

    public static final StreamCodec<ByteBuf, ToolData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
