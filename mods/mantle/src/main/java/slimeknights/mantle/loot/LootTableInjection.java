package slimeknights.mantle.loot;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import slimeknights.mantle.Mantle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Record holding a list of entries to inject into the given loot table
 */
public record LootTableInjection(Identifier name, List<LootPoolInjection> pools) {
  public static final Codec<LootTableInjection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    Identifier.CODEC.fieldOf("name").forGetter(LootTableInjection::name),
    LootPoolInjection.CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("pools").forGetter(LootTableInjection::pools)
  ).apply(instance, LootTableInjection::new));

  /**
   * Record holding a list of entries to inject into the given pool
   */
  public record LootPoolInjection(String name, List<LootPoolEntryContainer> entries) {
    public static final Codec<LootPoolInjection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      ExtraCodecs.NON_EMPTY_STRING.fieldOf("name").forGetter(LootPoolInjection::name),
      LootPoolEntries.CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("entries").forGetter(LootPoolInjection::entries)
    ).apply(instance, LootPoolInjection::new));

    /** Injects this into the given loot pool */
    public void inject(LootTable table) {
      LootPool pool = table.getPool(name);
      if (pool != null) {
        // Port: LootPool#entries is private final in 26.3, set it by reflection (Mojang names at runtime)
        try {
          java.lang.reflect.Field field = LootPool.class.getDeclaredField("entries");
          field.setAccessible(true);
          @SuppressWarnings("unchecked")
          List<LootPoolEntryContainer> old = (List<LootPoolEntryContainer>) field.get(pool);
          List<LootPoolEntryContainer> entries = new ArrayList<>(old.size() + this.entries.size());
          entries.addAll(old);
          entries.addAll(this.entries);
          field.set(pool, List.copyOf(entries));
        } catch (ReflectiveOperationException e) {
          Mantle.logger.error("Failed to inject loot into {} pool {}", table.getLootTableId(), name, e);
        }
      } else {
        Mantle.logger.warn("Failed to inject loot into {} pool {}", table.getLootTableId(), name);
      }
    }
  }

  /** Builder instance for a loot table injection */
  @SuppressWarnings("unused") // API
  @CanIgnoreReturnValue
  public static class Builder {
    private final Map<String,List<LootPoolEntryContainer>> pools = new LinkedHashMap<>();

    /** Inserts the given entries into the pool */
    public Builder addToPool(String name, List<LootPoolEntryContainer> entries) {
      pools.computeIfAbsent(name, n -> new ArrayList<>()).addAll(entries);
      return this;
    }

    /** Inserts the given entries into the pool */
    public Builder addToPool(String name, LootPoolEntryContainer... entries) {
      return addToPool(name, List.of(entries));
    }

    /** Inserts the given entries into the pool */
    public Builder addToPool(LootPoolInjection injection) {
      return addToPool(injection.name, injection.entries);
    }

    /** Builds the list of injections */
    @CheckReturnValue
    public LootTableInjection build(Identifier name) {
      return new LootTableInjection(name, pools.entrySet().stream().map(entry -> new LootPoolInjection(entry.getKey(), List.copyOf(entry.getValue()))).toList());
    }
  }
}
