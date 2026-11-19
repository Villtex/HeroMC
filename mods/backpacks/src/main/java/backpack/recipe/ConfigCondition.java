package backpack.recipe;

import backpack.BackpackConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Recipe condition {@code backpack:config}: RecipeHelper chose the plain backpack recipe by airshipModCompatibility
 * ("airship": with a chest in the middle) and the ender backpack recipe by enderRecipe ("ender_chest" / "eye").
 */
public record ConfigCondition(String option) implements ICondition {
    public static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("option").forGetter(ConfigCondition::option)).apply(i, ConfigCondition::new));

    @Override
    public boolean test(IContext context) {
        return switch (this.option) {
            case "airship" -> BackpackConfig.airshipModCompatibility();
            case "no_airship" -> !BackpackConfig.airshipModCompatibility();
            case "ender_chest" -> BackpackConfig.enderRecipe() == 0;
            case "eye" -> BackpackConfig.enderRecipe() == 1;
            default -> false;
        };
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
