package qcraft;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * The "general" section of QuantumCraft.cfg with the values of the pack (all three off). The block and item ids of the
 * file only matter for old ids ({@code QuantumCraft.registerOldIds}).
 */
public final class QCConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    static final ModConfigSpec.BooleanValue OCCLUSION_TESTING = BUILDER
            .comment("Set whether QBlocks should not be observed if their line of sight to the player is obstructed. WARNING: This has a very high performance cost if you have lots of QBlocks in your world!!")
            .define("enableQBlockOcclusionTesting", false);
    static final ModConfigSpec.BooleanValue WORLD_GEN = BUILDER
            .comment("Set whether Quantum Ore will spawn in new chunks (the ore template of CoFH World is not affected)")
            .define("enableWorldGen", false);
    static final ModConfigSpec.BooleanValue REPLACEMENT_RECIPES = BUILDER
            .comment("Set whether Quantum Dust can be crafted instead of mined")
            .define("enableWorldGenReplacementRecipes", false);
    public static final ModConfigSpec SPEC = BUILDER.build();

    private QCConfig() {
    }

    public static boolean occlusionTesting() {
        return SPEC.isLoaded() && OCCLUSION_TESTING.get();
    }

    public static boolean worldGen() {
        return SPEC.isLoaded() && WORLD_GEN.get();
    }

    public static boolean replacementRecipes() {
        return SPEC.isLoaded() && REPLACEMENT_RECIPES.get();
    }

    /** Recipe condition {@code quantumcraft:config} with {@code "option": "enableWorldGenReplacementRecipes"}. */
    public record Condition(String option) implements ICondition {
        public static final MapCodec<Condition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("option").forGetter(Condition::option)).apply(i, Condition::new));

        @Override
        public boolean test(IContext context) {
            return switch (this.option) {
                case "enableWorldGenReplacementRecipes" -> replacementRecipes();
                case "enableWorldGen" -> worldGen();
                case "enableQBlockOcclusionTesting" -> occlusionTesting();
                default -> false;
            };
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return CODEC;
        }
    }
}
