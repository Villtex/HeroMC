package slimeknights.tconstruct.tools;

import net.minecraft.world.entity.EntityType;
import slimeknights.mantle.registration.RegistryObject;
import slimeknights.tconstruct.library.json.predicate.tool.HasMaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.tool.HasModifierPredicate;
import slimeknights.tconstruct.library.json.predicate.tool.HasStatTypePredicate;
import slimeknights.tconstruct.library.json.predicate.tool.HasToolHookPredicate;
import slimeknights.tconstruct.library.json.predicate.tool.PersistentDataPredicate;
import slimeknights.tconstruct.library.json.predicate.tool.StatInRangePredicate;
import slimeknights.tconstruct.library.json.predicate.tool.StatInSetPredicate;
import slimeknights.tconstruct.library.json.predicate.tool.ToolActionPredicate;
import slimeknights.tconstruct.library.json.predicate.tool.ToolContextPredicate;
import slimeknights.tconstruct.library.json.predicate.tool.ToolStackPredicate;
import slimeknights.tconstruct.library.json.predicate.tool.ToolVariableRangePredicate;
import slimeknights.tconstruct.library.json.predicate.tool.VolatileDataPredicate;
import slimeknights.tconstruct.library.tools.definition.module.ToolModule;
import slimeknights.tconstruct.library.tools.definition.module.aoe.AreaOfEffectIterator;
import slimeknights.tconstruct.library.tools.definition.module.aoe.BoxAOEIterator;
import slimeknights.tconstruct.library.tools.definition.module.aoe.CircleAOEIterator;
import slimeknights.tconstruct.library.tools.definition.module.aoe.ConditionalAOEIterator;
import slimeknights.tconstruct.library.tools.definition.module.aoe.TreeAOEIterator;
import slimeknights.tconstruct.library.tools.definition.module.aoe.VeiningAOEIterator;
import slimeknights.tconstruct.library.tools.definition.module.build.MultiplyStatsModule;
import slimeknights.tconstruct.library.tools.definition.module.build.SetStatsModule;
import slimeknights.tconstruct.library.tools.definition.module.build.ToolActionsModule;
import slimeknights.tconstruct.library.tools.definition.module.build.ToolSlotsModule;
import slimeknights.tconstruct.library.tools.definition.module.build.ToolTraitsModule;
import slimeknights.tconstruct.library.tools.definition.module.build.VolatileFlagModule;
import slimeknights.tconstruct.library.tools.definition.module.build.VolatileIntModule;
import slimeknights.tconstruct.library.tools.definition.module.display.CustomMaterialName;
import slimeknights.tconstruct.library.tools.definition.module.display.FixedMaterialToolName;
import slimeknights.tconstruct.library.tools.definition.module.display.MaterialToolNameModule;
import slimeknights.tconstruct.library.tools.definition.module.display.SimpleToolName;
import slimeknights.tconstruct.library.tools.definition.module.display.StatTypesToolNameModule;
import slimeknights.tconstruct.library.tools.definition.module.display.UniqueMaterialToolName;
import slimeknights.tconstruct.library.tools.definition.module.interaction.AttackInteraction;
import slimeknights.tconstruct.library.tools.definition.module.interaction.DualOptionInteraction;
import slimeknights.tconstruct.library.tools.definition.module.interaction.PreferenceSetInteraction;
import slimeknights.tconstruct.library.tools.definition.module.interaction.ToggleableSetInteraction;
import slimeknights.tconstruct.library.tools.definition.module.material.DefaultMaterialsModule;
import slimeknights.tconstruct.library.tools.definition.module.material.MaterialRepairModule;
import slimeknights.tconstruct.library.tools.definition.module.material.MaterialStatsModule;
import slimeknights.tconstruct.library.tools.definition.module.material.MaterialTraitsModule;
import slimeknights.tconstruct.library.tools.definition.module.material.PartStatsModule;
import slimeknights.tconstruct.library.tools.definition.module.material.PartsModule;
import slimeknights.tconstruct.library.tools.definition.module.material.RemappingMaterialsModule;
import slimeknights.tconstruct.library.tools.definition.module.material.StatlessPartRepairModule;
import slimeknights.tconstruct.library.tools.definition.module.mining.IsEffectiveModule;
import slimeknights.tconstruct.library.tools.definition.module.mining.MaxTierModule;
import slimeknights.tconstruct.library.tools.definition.module.mining.MiningSpeedModifierModule;
import slimeknights.tconstruct.library.tools.definition.module.mining.OneClickBreakModule;
import slimeknights.tconstruct.library.tools.definition.module.weapon.CircleWeaponAttack;
import slimeknights.tconstruct.library.tools.definition.module.weapon.ParticleWeaponAttack;
import slimeknights.tconstruct.library.tools.definition.module.weapon.SweepWeaponAttack;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;
import slimeknights.tconstruct.TConstruct;

/**
 * Teil 27d: only the entries the tool library refers to, as references to the registry entries
 * ({@link RegistryObject#create}). The tools, their entities and loot functions are registered by the full
 * TinkerTools of Teil 27h, which replaces this file.
 */
public final class TinkerTools {
  private TinkerTools() {}

  public static final RegistryObject<EntityType<?>> fishingHook = RegistryObject.create(TConstruct.getResource("fishing_hook"), Registries.ENTITY_TYPE);
  public static final RegistryObject<EntityType<?>> indestructibleItem = RegistryObject.create(TConstruct.getResource("indestructible_item"), Registries.ENTITY_TYPE);

  /** Teil 27d: the loaders of the tool definition modules, AOE iterators and tool predicates (1.20.1 TinkerTools#registerRecipeSerializers) */
  public static void registerSerializers(RegisterEvent event) {
    if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
      // TODO 27e/27f: tool stats of modules (overslime, tank, energy, edible), ToolHookIngredient, MeltingFluidEffectiveModule
      ToolModule.LOADER.register(TConstruct.getResource("empty"), ToolModule.EMPTY.getLoader());
      // tool definition components
      ToolModule.LOADER.register(TConstruct.getResource("base_stats"), SetStatsModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("multiply_stats"), MultiplyStatsModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("tool_actions"), ToolActionsModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("traits"), ToolTraitsModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("modifier_slots"), ToolSlotsModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("volatile_flag"), VolatileFlagModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("volatile_int"), VolatileIntModule.LOADER);
      // harvest
      ToolModule.LOADER.register(TConstruct.getResource("is_effective"), IsEffectiveModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("mining_speed_modifier"), MiningSpeedModifierModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("max_tier"), MaxTierModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("one_click_break"), OneClickBreakModule.LOADER);
      // material
      ToolModule.LOADER.register(TConstruct.getResource("material_stats"), MaterialStatsModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("part_stats"), PartStatsModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("material_traits"), MaterialTraitsModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("tool_parts"), PartsModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("material_repair"), MaterialRepairModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("default_materials"), DefaultMaterialsModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("remapping_materials"), RemappingMaterialsModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("statless_part_repair"), StatlessPartRepairModule.LOADER);
      // aoe
      AreaOfEffectIterator.LOADER.register(TConstruct.getResource("empty"), AreaOfEffectIterator.EMPTY.getLoader());
      AreaOfEffectIterator.register(TConstruct.getResource("box_aoe"), BoxAOEIterator.LOADER);
      AreaOfEffectIterator.register(TConstruct.getResource("circle_aoe"), CircleAOEIterator.LOADER);
      AreaOfEffectIterator.register(TConstruct.getResource("tree_aoe"), TreeAOEIterator.LOADER);
      AreaOfEffectIterator.register(TConstruct.getResource("vein_aoe"), VeiningAOEIterator.LOADER);
      AreaOfEffectIterator.register(TConstruct.getResource("conditional_aoe"), ConditionalAOEIterator.LOADER);
      // attack
      ToolModule.LOADER.register(TConstruct.getResource("sweep_melee"), SweepWeaponAttack.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("circle_melee"), CircleWeaponAttack.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("melee_particle"), ParticleWeaponAttack.LOADER);
      // generic tool modules
      ToolModule.LOADER.register(TConstruct.getResource("attack_interaction"), AttackInteraction.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("dual_option_interaction"), DualOptionInteraction.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("preference_set_interaction"), PreferenceSetInteraction.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("toggleable_set_interaction"), ToggleableSetInteraction.LOADER);
      // special tool modules
      // display name
      ToolModule.LOADER.register(TConstruct.getResource("item_name"), SimpleToolName.ITEM.getLoader());
      ToolModule.LOADER.register(TConstruct.getResource("material_name"), MaterialToolNameModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("stat_types_name"), StatTypesToolNameModule.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("fixed_material_name"), FixedMaterialToolName.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("unique_material_name"), UniqueMaterialToolName.LOADER);
      ToolModule.LOADER.register(TConstruct.getResource("custom_material_name"), CustomMaterialName.LOADER);
      // tool predicates
      ToolContextPredicate.LOADER.register(TConstruct.getResource("has_upgrades"), ToolContextPredicate.HAS_UPGRADES.getLoader());
      ToolContextPredicate.LOADER.register(TConstruct.getResource("has_modifier"), HasModifierPredicate.LOADER);
      ToolContextPredicate.LOADER.register(TConstruct.getResource("has_material"), HasMaterialPredicate.LOADER);
      ToolContextPredicate.LOADER.register(TConstruct.getResource("has_stat_type"), HasStatTypePredicate.LOADER);
      ToolContextPredicate.LOADER.register(TConstruct.getResource("has_persistent_key"), PersistentDataPredicate.LOADER);
      ToolContextPredicate.LOADER.register(TConstruct.getResource("has_hook"), HasToolHookPredicate.LOADER);
      ToolStackPredicate.LOADER.register(TConstruct.getResource("not_broken"), ToolStackPredicate.NOT_BROKEN.getLoader());
      ToolStackPredicate.LOADER.register(TConstruct.getResource("stat_in_range"), StatInRangePredicate.LOADER);
      ToolStackPredicate.LOADER.register(TConstruct.getResource("stat_in_set"), StatInSetPredicate.LOADER);
      ToolStackPredicate.LOADER.register(TConstruct.getResource("has_volatile_key"), VolatileDataPredicate.LOADER);
      ToolStackPredicate.LOADER.register(TConstruct.getResource("variable_range"), ToolVariableRangePredicate.LOADER);
      ToolStackPredicate.LOADER.register(TConstruct.getResource("tool_action"), ToolActionPredicate.LOADER);

    }
  }
}
