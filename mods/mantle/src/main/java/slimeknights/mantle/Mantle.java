package slimeknights.mantle;

import slimeknights.mantle.util.TranslationHelper;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import slimeknights.mantle.config.Config;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import slimeknights.mantle.command.MantleCommand;
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.mantle.data.predicate.block.BlockPredicate;
import slimeknights.mantle.data.predicate.block.BlockPropertiesPredicate;
import slimeknights.mantle.data.predicate.item.ItemPredicate;
import slimeknights.mantle.data.predicate.fluid.FluidPredicate;
import slimeknights.mantle.data.predicate.fluid.FluidTypePredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;
import slimeknights.mantle.data.predicate.entity.HasMobEffectPredicate;
import slimeknights.mantle.data.predicate.entity.BlockAtEntityPredicate;
import slimeknights.mantle.data.predicate.entity.MobTypePredicate;
import slimeknights.mantle.data.predicate.entity.MobType;
import slimeknights.mantle.data.predicate.entity.HasEnchantmentEntityPredicate;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.mantle.data.predicate.damage.DamageTypePredicate;
import slimeknights.mantle.data.predicate.damage.SourceMessagePredicate;
import slimeknights.mantle.data.predicate.damage.SourceAttackerPredicate;
import slimeknights.mantle.block.entity.MantleHangingSignBlockEntity;
import slimeknights.mantle.block.entity.MantleSignBlockEntity;
import slimeknights.mantle.item.IFluidContainerItem;
import slimeknights.mantle.loot.LootTableInjector;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.transfer.EmptyFluidContainerTransfer;
import slimeknights.mantle.fluid.transfer.EmptyFluidWithNBTTransfer;
import slimeknights.mantle.fluid.transfer.EmptyPotionTransfer;
import slimeknights.mantle.fluid.transfer.FillFluidContainerTransfer;
import slimeknights.mantle.fluid.transfer.FillFluidWithNBTTransfer;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import slimeknights.mantle.client.MantleClient;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.registration.MantleData;
import slimeknights.mantle.util.OffhandCooldownTracker;

/**
 * Mantle
 *
 * Central mod object for Mantle (port of Mantle 1.11.117 for Forge 1.20.1 to NeoForge 26.3, Teil 27b).
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
@Mod(Mantle.modId)
public class Mantle {
  public static final String modId = "mantle";
  public static final Logger logger = LoggerFactory.getLogger("Mantle");
  /** Namespace for common tags: "c" on NeoForge 26.3 (was "forge" on 1.20.1) */
  public static final String COMMON = "c";

  /* Instance of this mod, used for grabbing prototype fields */
  public static Mantle instance;

  public Mantle(IEventBus bus, ModContainer container) {
    instance = this;
    // Port: 26.3 has no server config type; the tag preferences are a synced config (loaded on the server, sent to clients)
    container.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
    container.registerConfig(ModConfig.Type.SYNCED, Config.SERVER_SPEC);
    MantleData.init(bus);
    OffhandCooldownTracker.register(bus);
    // NeoForge registers payloads in RegisterPayloadHandlersEvent, before common setup
    MantleNetwork.registerPackets();
    MantleRecipes.init(bus);
    MantleTags.init();
    MantleLoot.init(bus);
    // Port: Mantle's signs use the vanilla sign block entity types, fluid container items get their capability here
    bus.addListener(MantleSignBlockEntity::addBlocks);
    bus.addListener(MantleHangingSignBlockEntity::addBlocks);
    bus.addListener(IFluidContainerItem::registerCapabilities);
    LootTableInjector.init(bus);
    FluidContainerTransferManager.INSTANCE.init();
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(EmptyFluidContainerTransfer.ID, EmptyFluidContainerTransfer.DESERIALIZER);
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(FillFluidContainerTransfer.ID, FillFluidContainerTransfer.DESERIALIZER);
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(EmptyFluidWithNBTTransfer.ID, EmptyFluidWithNBTTransfer.DESERIALIZER);
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(FillFluidWithNBTTransfer.ID, FillFluidWithNBTTransfer.DESERIALIZER);
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(EmptyPotionTransfer.ID, EmptyPotionTransfer.DESERIALIZER);
    registerPredicates();
    bus.addListener(FMLCommonSetupEvent.class, event -> {
      MantleCommand.init();
      TagPreference.init();
    });
    if (FMLEnvironment.getDist() == Dist.CLIENT) {
      MantleClient.onConstruct();
    }
  }

  /** Registers the predicate loaders (1.20.1: in RegisterEvent for recipe serializers) */
  private static void registerPredicates() {
    BlockPredicate.LOADER.register(getResource("requires_tool"), BlockPredicate.REQUIRES_TOOL.getLoader());
    BlockPredicate.LOADER.register(getResource("blocks_motion"), BlockPredicate.BLOCKS_MOTION.getLoader());
    BlockPredicate.LOADER.register(getResource("can_be_replaced"), BlockPredicate.CAN_BE_REPLACED.getLoader());
    BlockPredicate.LOADER.register(getResource("block_properties"), BlockPropertiesPredicate.LOADER);

    // item predicates
    ItemPredicate.LOADER.register(getResource("has_container"), ItemPredicate.HAS_CONTAINER.getLoader());
    ItemPredicate.LOADER.register(getResource("may_have_transfer"), ItemPredicate.MAY_HAVE_TRANSFER.getLoader());

    // fluid predicates
    FluidPredicate.LOADER.register(getResource("fluid_type"), FluidTypePredicate.LOADER);
    FluidPredicate.LOADER.register(getResource("is_source"), FluidPredicate.SOURCE.getLoader());
    FluidPredicate.LOADER.register(getResource("has_bucket"), FluidPredicate.HAS_BUCKET.getLoader());
    FluidPredicate.LOADER.register(getResource("lighter_than_air"), FluidPredicate.LIGHTER_THAN_AIR.getLoader());

    // entity predicates
    // simple
    LivingEntityPredicate.LOADER.register(getResource("fire_immune"), LivingEntityPredicate.FIRE_IMMUNE.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("can_freeze"), LivingEntityPredicate.CAN_FREEZE.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("water_sensitive"), LivingEntityPredicate.WATER_SENSITIVE.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("on_fire"), LivingEntityPredicate.ON_FIRE.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("is_freezing"), LivingEntityPredicate.IS_FREEZING.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("is_in_powdered_snow"), LivingEntityPredicate.IS_IN_POWDERED_SNOW.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("on_ground"), LivingEntityPredicate.ON_GROUND.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("crouching"), LivingEntityPredicate.CROUCHING.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("sprinting"), LivingEntityPredicate.SPRINTING.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("blocking"), LivingEntityPredicate.BLOCKING.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("elytra_flying"), LivingEntityPredicate.ELYTRA_FLYING.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("has_effect"), HasMobEffectPredicate.LOADER);
    LivingEntityPredicate.LOADER.register(getResource("block_at_entity"), BlockAtEntityPredicate.LOADER);
    LivingEntityPredicate.LOADER.register(getResource("eyes_in_water"), LivingEntityPredicate.EYES_IN_WATER.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("feet_in_water"), LivingEntityPredicate.FEET_IN_WATER.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("underwater"), LivingEntityPredicate.UNDERWATER.getLoader());
    LivingEntityPredicate.LOADER.register(getResource("raining_at"), LivingEntityPredicate.RAINING.getLoader());
    // property
    LivingEntityPredicate.LOADER.register(getResource("mob_type"), MobTypePredicate.LOADER);
    LivingEntityPredicate.LOADER.register(getResource("has_enchantment"), HasEnchantmentEntityPredicate.LOADER);
    // register mob types
    MobTypePredicate.MOB_TYPES.register(Identifier.parse("undefined"), MobType.UNDEFINED);
    MobTypePredicate.MOB_TYPES.register(Identifier.parse("undead"), MobType.UNDEAD);
    MobTypePredicate.MOB_TYPES.register(Identifier.parse("arthropod"), MobType.ARTHROPOD);
    MobTypePredicate.MOB_TYPES.register(Identifier.parse("illager"), MobType.ILLAGER);
    MobTypePredicate.MOB_TYPES.register(Identifier.parse("water"), MobType.WATER);

    // damage predicates
    // simple
    DamageSourcePredicate.LOADER.register(getResource("has_entity"), DamageSourcePredicate.HAS_ENTITY.getLoader());
    DamageSourcePredicate.LOADER.register(getResource("is_indirect"), DamageSourcePredicate.IS_INDIRECT.getLoader());
    DamageSourcePredicate.LOADER.register(getResource("can_protect"), DamageSourcePredicate.CAN_PROTECT.getLoader());
    // fields
    DamageSourcePredicate.LOADER.register(getResource("damage_type"), DamageTypePredicate.LOADER);
    DamageSourcePredicate.LOADER.register(getResource("message"), SourceMessagePredicate.LOADER);
    DamageSourcePredicate.LOADER.register(getResource("attacker"), SourceAttackerPredicate.LOADER);
  }

  /**
   * Gets a resource location for Mantle
   * @param name  Name
   * @return  Resource location instance
   */
  public static Identifier getResource(String name) {
    return Identifier.fromNamespaceAndPath(modId, name);
  }

  /**
   * Gets a resource location for the common namespace, which is "c" on NeoForge.
   * @param name  Name
   * @return  Resource location instance
   */
  public static Identifier commonResource(String name) {
    return Identifier.fromNamespaceAndPath(COMMON, name);
  }

  /**
   * Makes a translation key for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static String makeDescriptionId(String base, String name) {
    return Util.makeDescriptionId(base, getResource(name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static MutableComponent makeComponent(String base, String name) {
    return Component.translatable(makeDescriptionId(base, name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @param args  Additional arguments to format strings
   * @return  Translation key
   */
  public static MutableComponent makeComponent(String base, String name, Object... args) {
    return Component.translatable(makeDescriptionId(base, name), java.util.Arrays.stream(args).map(TranslationHelper::arg).toArray());
  }
}
