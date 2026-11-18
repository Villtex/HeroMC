package betterdungeons.mob;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

/**
 * Loot condition {@code betterdungeons:mob_level}: the killed human's level (getlvl, -1..4) lies in min..max. The
 * level-dependent parts of EntityHuman.dropFewItems (potions, ingots, essences) are loot pools with it.
 */
public record MobLevelCondition(int min, int max) implements LootItemCondition {
    public static final MapCodec<MobLevelCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.INT.optionalFieldOf("min", Integer.MIN_VALUE).forGetter(MobLevelCondition::min),
            Codec.INT.optionalFieldOf("max", Integer.MAX_VALUE).forGetter(MobLevelCondition::max)).apply(i, MobLevelCondition::new));

    @Override
    public MapCodec<MobLevelCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY);
    }

    @Override
    public boolean test(LootContext context) {
        Entity e = context.getOptional(LootContextParams.THIS_ENTITY);
        if (e instanceof HumanEntity human) {
            int lvl = human.getlvl();
            return lvl >= this.min && lvl <= this.max;
        }
        return false;
    }
}
