package hexxit.worldgen152;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

/**
 * {@code neoforge:remove_features} that only acts while {@code replaceVanillaOres} is on in
 * {@code config/worldgen152-startup.toml}: CoFH World's ReplaceVanillaGeneration for Hexxit Integrated. The pack
 * generated the vanilla ores with its own values (feature {@code cofhcore:ores}) instead of vanilla's. Only the
 * named features go (ores CoFH makes); other ores and other mods' features stay.
 */
public record ReplaceVanillaOresModifier(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features) implements BiomeModifier {
    public static final MapCodec<ReplaceVanillaOresModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.holderSet(Registries.BIOME).fieldOf("biomes").forGetter(ReplaceVanillaOresModifier::biomes),
            RegistryCodecs.holderSet(Registries.PLACED_FEATURE).fieldOf("features").forGetter(ReplaceVanillaOresModifier::features))
            .apply(instance, ReplaceVanillaOresModifier::new));

    @Override
    public void modify(RegistryAccess registries, Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.REMOVE || !IntegratedConfig.replaceVanillaOres() || !this.biomes.contains(biome)) {
            return;
        }
        BiomeGenerationSettingsBuilder settings = builder.getGenerationSettings();
        for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
            settings.getFeatures(step).removeIf(this.features::contains);
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
