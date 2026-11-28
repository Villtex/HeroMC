package slimeknights.tconstruct.shared;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.registration.RegistryObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.json.BlockOrEntityCondition;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.mantle.data.predicate.block.BlockPredicate;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;
import slimeknights.mantle.data.predicate.item.ItemPredicate;
import slimeknights.tconstruct.library.json.predicate.BlockAtFeetEntityPredicate;
import slimeknights.tconstruct.library.json.predicate.BlockVariableRangePredicate;
import slimeknights.tconstruct.library.json.predicate.EntityVariableRangePredicate;
import slimeknights.tconstruct.library.json.predicate.HarvestTierPredicate;
import slimeknights.tconstruct.library.json.predicate.HasMobEffectPredicate;
import slimeknights.tconstruct.library.json.predicate.TinkerPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialDefinitionPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialHasPartPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialIdPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialStatTypePredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialTierPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialVariantPredicate;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Teil 27d: only the conditions the library data uses ({@code tconstruct3:config} in material and recipe data,
 * the loot conditions). The blocks, items and other contents of TinkerCommons come in Teil 27g, which extends this.
 */
public final class TinkerCommons extends TinkerModule {
  private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, TConstruct.MOD_ID);

  /** Recipe and data condition {@code tconstruct3:config} */
  public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigEnabledCondition>> configCondition = CONDITIONS.register("config", () -> ConfigEnabledCondition.CODEC);

  // loot conditions
  public static final RegistryObject<MapCodec<? extends LootItemCondition>> lootConfig = LOOT_CONDITIONS.register("config", () -> ConfigEnabledCondition.CODEC);
  public static final RegistryObject<MapCodec<? extends LootItemCondition>> lootBlockOrEntity = LOOT_CONDITIONS.register("block_or_entity", () -> BlockOrEntityCondition.CODEC);
  /** 1.20.1 had its own copy of Mantle's condition, data still names it {@code tconstruct3:has_context_set} */
  public static final RegistryObject<MapCodec<? extends LootItemCondition>> hasLootContextSet = LOOT_CONDITIONS.register("has_context_set", () -> slimeknights.mantle.loot.condition.HasLootContextSetCondition.CODEC.xmap(c -> c, c -> c));  // own instance: a registry refuses the same codec twice

  public TinkerCommons(IEventBus modBus) {
    CONDITIONS.register(modBus);
  }

  /** Teil 27d: loaders of the predicates the library data uses (1.20.1 TinkerCommons and TinkerMaterials #registerSerializers) */
  public static void registerSerializers(RegisterEvent event) {
    if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
      MaterialPredicate.LOADER.register(TConstruct.getResource("castable"), MaterialPredicate.CASTABLE.getLoader());
      MaterialPredicate.LOADER.register(TConstruct.getResource("composite"), MaterialPredicate.COMPOSITE.getLoader());
      slimeknights.mantle.data.predicate.fluid.FluidPredicate.LOADER.register(TConstruct.getResource("fuel"), TinkerPredicate.FUEL.getLoader());
      MaterialPredicate.LOADER.register(TConstruct.getResource("variant"), MaterialVariantPredicate.LOADER);
      MaterialPredicate.LOADER.register(TConstruct.getResource("id"), MaterialIdPredicate.LOADER);
      MaterialPredicate.LOADER.register(TConstruct.getResource("has_part"), MaterialHasPartPredicate.LOADER);
      MaterialPredicate.LOADER.register(TConstruct.getResource("stat_type"), MaterialStatTypePredicate.LOADER);
      MaterialPredicate.LOADER.register(TConstruct.getResource("craftable"), MaterialDefinitionPredicate.CRAFTABLE.getLoader());
      MaterialPredicate.LOADER.register(TConstruct.getResource("not_hidden"), MaterialDefinitionPredicate.NOT_HIDDEN.getLoader());
      MaterialPredicate.LOADER.register(TConstruct.getResource("registered"), MaterialDefinitionPredicate.REGISTERED.getLoader());
      MaterialPredicate.LOADER.register(TConstruct.getResource("tier"), MaterialTierPredicate.LOADER);

      DamageSourcePredicate.LOADER.register(TConstruct.getResource("direct"), TinkerPredicate.DIRECT_DAMAGE.getLoader());
      // entity
      LivingEntityPredicate.LOADER.register(TConstruct.getResource("airborne"), TinkerPredicate.AIRBORNE.getLoader());
      LivingEntityPredicate.LOADER.register(TConstruct.getResource("targeting_block"), TinkerPredicate.TARGETING_BLOCK.getLoader());
      LivingEntityPredicate.LOADER.register(TConstruct.getResource("full_health"), TinkerPredicate.FULL_HEALTH.getLoader());
      LivingEntityPredicate.LOADER.register(TConstruct.getResource("variable_range"), EntityVariableRangePredicate.LOADER);
      LivingEntityPredicate.LOADER.register(TConstruct.getResource("has_effect"), HasMobEffectPredicate.LOADER);
      LivingEntityPredicate.LOADER.register(TConstruct.getResource("block_at_feet"), BlockAtFeetEntityPredicate.LOADER);
      // item
      ItemPredicate.LOADER.register(TConstruct.getResource("arrow"), TinkerPredicate.ARROW.getLoader());
      ItemPredicate.LOADER.register(TConstruct.getResource("bucket"), TinkerPredicate.BUCKET.getLoader());
      ItemPredicate.LOADER.register(TConstruct.getResource("map"), TinkerPredicate.MAP.getLoader());
      ItemPredicate.LOADER.register(TConstruct.getResource("can_melt"), TinkerPredicate.CAN_MELT_ITEM.getLoader());
      ItemPredicate.LOADER.register(TConstruct.getResource("castable"), TinkerPredicate.CASTABLE.getLoader());
      // block
      BlockPredicate.LOADER.register(TConstruct.getResource("blocks_motion"), TinkerPredicate.BLOCKS_MOTION.getLoader());
      BlockPredicate.LOADER.register(TConstruct.getResource("can_be_replaced"), TinkerPredicate.CAN_BE_REPLACED.getLoader());
      BlockPredicate.LOADER.register(TConstruct.getResource("bush"), TinkerPredicate.BUSH.getLoader());
      BlockPredicate.LOADER.register(TConstruct.getResource("can_melt"), TinkerPredicate.CAN_MELT_BLOCK.getLoader());
      BlockPredicate.LOADER.register(TConstruct.getResource("harvest_tier"), HarvestTierPredicate.LOADER);
      BlockPredicate.LOADER.register(TConstruct.getResource("variable_range"), BlockVariableRangePredicate.LOADER);
      // fluid

    }
  }
}
