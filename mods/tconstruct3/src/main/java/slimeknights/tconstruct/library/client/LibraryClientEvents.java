package slimeknights.tconstruct.library.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.client.model.FluidContainerModel;
import slimeknights.tconstruct.library.client.model.TinkerItemProperties;
import slimeknights.tconstruct.library.client.model.tools.MaterialModel;
import slimeknights.tconstruct.library.client.model.tools.ToolModel;
import slimeknights.tconstruct.library.client.modifiers.DyedModifierModel;
import slimeknights.tconstruct.library.client.modifiers.ModifierIconManager;
import slimeknights.tconstruct.library.client.modifiers.ModifierModelManager;
import slimeknights.tconstruct.library.client.modifiers.ModifierModelManager.ModifierModelRegistrationEvent;
import slimeknights.tconstruct.library.client.modifiers.ModifierModelMapManager;
import slimeknights.tconstruct.library.client.modifiers.NormalModifierModel;
import slimeknights.tconstruct.library.client.modifiers.PotionModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.CompoundModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.ConditionalModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.MaterialHasFallbackModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.ModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.NestedModifierModel;

import static slimeknights.tconstruct.TConstruct.getResource;

/**
 * Port (Teil 27f2): client registrations of the Tinkers library, from 1.20.1 {@code TinkerClient.onConstruct} and the
 * library parts of {@code ToolClientEvents}. Models and loaders of tools, armor and smeltery (overslime, slimeskull,
 * trim, banner, smashing fluid, tank) are registered with their parts (27h, 27i).
 */
public final class LibraryClientEvents {
  private LibraryClientEvents() {}

  /** Called from the mod constructor on the client */
  public static void init(IEventBus modBus) {
    // modifier models (new system, modifier model maps)
    ModifierModel.LOADER.register(getResource("empty"), ModifierModel.EMPTY.getLoader());
    ModifierModel.LOADER.register(getResource("compound"), CompoundModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("conditional"), ConditionalModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("trait"), NestedModifierModel.Trait.LOADER);
    ModifierModel.LOADER.register(getResource("crafted"), NestedModifierModel.Crafted.LOADER);
    ModifierModel.LOADER.register(getResource("basic"), NormalModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("dyed"), DyedModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("material_index"), slimeknights.tconstruct.library.client.modifiers.model.MaterialModifierModel.Index.LOADER);
    ModifierModel.LOADER.register(getResource("persistent_material"), slimeknights.tconstruct.library.client.modifiers.model.MaterialModifierModel.PersistentData.LOADER);
    ModifierModel.LOADER.register(getResource("dyed_material"), slimeknights.tconstruct.library.client.modifiers.model.MaterialModifierModel.Dyed.LOADER);
    ModifierModel.LOADER.register(getResource("potion"), PotionModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("fluid"), slimeknights.tconstruct.library.client.modifiers.model.FluidModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("tank"), slimeknights.tconstruct.library.client.modifiers.model.TankModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("material_has_fallback"), MaterialHasFallbackModifierModel.LOADER);

    modBus.addListener(ModelEvent.RegisterLoaders.class, LibraryClientEvents::registerModelLoaders);
    modBus.addListener(RegisterItemModelsEvent.class, LibraryClientEvents::registerItemModels);
    modBus.addListener(RegisterConditionalItemModelPropertyEvent.class, TinkerItemProperties::registerConditions);
    modBus.addListener(AddClientReloadListenersEvent.class, LibraryClientEvents::addReloadListeners);
    modBus.addListener(ModifierModelRegistrationEvent.class, LibraryClientEvents::registerModifierModels);
  }

  /** Model loaders of the 1.20.1 model files ({@code "loader"}) */
  private static void registerModelLoaders(ModelEvent.RegisterLoaders event) {
    event.register(getResource("material"), MaterialModel.LOADER);
    event.register(getResource("tool"), ToolModel.LOADER);
    event.register(getResource("fluid_container"), FluidContainerModel.LOADER);
  }

  /** Item model types of the item model definitions ({@code assets/tconstruct3/items/*.json}) */
  private static void registerItemModels(RegisterItemModelsEvent event) {
    event.register(getResource("material"), MaterialModel.Unbaked.MAP_CODEC);
    event.register(getResource("tool"), ToolModel.Unbaked.MAP_CODEC);
    event.register(getResource("fluid_container"), FluidContainerModel.Unbaked.MAP_CODEC);
  }

  private static void addReloadListeners(AddClientReloadListenersEvent event) {
    MaterialRenderInfoLoader.init(event);
    ModifierModelManager.init(event);
    event.addListener(getResource("modifier_model_maps"), ModifierModelMapManager.INSTANCE);
    ModifierIconManager.init(event);
  }

  /** Modifier models of the old system ({@code tinkering/modifiers.json}) */
  private static void registerModifierModels(ModifierModelRegistrationEvent event) {
    event.registerModel(getResource("normal"), NormalModifierModel.UNBAKED_INSTANCE);
    event.registerModel(getResource("fluid"), slimeknights.tconstruct.library.client.modifiers.FluidModifierModel.UNBAKED_INSTANCE);
    event.registerModel(getResource("tank"), slimeknights.tconstruct.library.client.modifiers.TankModifierModel.UNBAKED_INSTANCE);
    event.registerModel(getResource("material"), slimeknights.tconstruct.library.client.modifiers.MaterialModifierModel.UNBAKED_INSTANCE);
    event.registerModel(getResource("dyed"), DyedModifierModel.UNBAKED_INSTANCE);
    event.registerModel(getResource("potion"), PotionModifierModel.UNBAKED_INSTANCE);
    TConstruct.LOG.debug("Registered library modifier models");
  }
}
