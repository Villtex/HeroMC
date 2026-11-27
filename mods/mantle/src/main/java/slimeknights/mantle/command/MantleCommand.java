package slimeknights.mantle.command;

import net.minecraft.server.permissions.PermissionCheck;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import slimeknights.mantle.command.argument.TagSourceArgument;
import slimeknights.mantle.command.tags.ModifyTagCommand;

import java.util.function.Consumer;

/**
 * Root command for all commands in mantle
 */
public class MantleCommand {
  /** Permission level that allows a user to build in spawn protected areas */
  public static final int PERMISSION_EDIT_SPAWN = 1;
  /** Permission level that can run standard game commands, used by command blocks and functions */
  public static final int PERMISSION_GAME_COMMANDS = 2;
  /** Standard permission level for server operators */
  public static final int PERMISSION_PLAYER_COMMANDS = 3;
  /** Permission level for the server owner, server console, or the player in single player */
  public static final int PERMISSION_OWNER = 4;

  /** @deprecated use {@link RegistryArgument#TAG} or {@link TagSourceArgument#TAG} */
  @Deprecated(forRemoval = true)
  public static SuggestionProvider<CommandSourceStack> VALID_TAGS;
  /** @deprecated use {@link RegistryArgument#VALUE} or {@link TagSourceArgument#VALUE} */
  @Deprecated(forRemoval = true)
  public static SuggestionProvider<CommandSourceStack> REGISTRY_VALUES;
  /** @deprecated use {@link RegistryArgument#REGISTRY} or {@link TagSourceArgument#SOURCE} */
  @Deprecated(forRemoval = true)
  public static SuggestionProvider<CommandSourceStack> REGISTRY;

  /** Registers all Mantle command related content */
  /**
   * Port: 26.3 replaced permission levels by permission checks; same meaning as 1.20.1's {@code MantleCommand.hasPermission(source, level)}.
   */
  public static boolean hasPermission(CommandSourceStack source, int level) {
    PermissionCheck check = switch (level) {
      case 0 -> Commands.LEVEL_ALL;
      case 1 -> Commands.LEVEL_MODERATORS;
      case 2 -> Commands.LEVEL_GAMEMASTERS;
      case 3 -> Commands.LEVEL_ADMINS;
      default -> Commands.LEVEL_OWNERS;
    };
    return Commands.hasPermission(check).test(source);
  }

  public static void init() {
    RegistryArgument.registerSuggestions();
    VALID_TAGS = RegistryArgument.TAG;
    REGISTRY_VALUES = RegistryArgument.VALUE;
    REGISTRY = RegistryArgument.REGISTRY;
    TagSourceArgument.registerSuggestions();

    // register interesting sources
    // Port: 26.3 loot tables are a reloadable registry, recipes are keyed by resource keys
    SourcesCommand.register(net.minecraft.core.registries.Registries.elementsDirPath(net.minecraft.core.registries.Registries.LOOT_TABLE), (context, builder)
      -> SharedSuggestionProvider.suggestResource(context.getSource().getServer().reloadableRegistries().lookup().lookupOrThrow(net.minecraft.core.registries.Registries.LOOT_TABLE).listElementIds().map(net.minecraft.resources.ResourceKey::identifier), builder));
    SourcesCommand.register("recipe", (context, builder)
      -> SharedSuggestionProvider.suggestResource(context.getSource().getServer().getRecipeManager().getRecipes().stream().map(holder -> holder.id().identifier()), builder));

    // add command listener
    NeoForge.EVENT_BUS.addListener(MantleCommand::registerCommand);
  }

  /** Registers a sub command for the root Mantle command */
  private static void register(LiteralArgumentBuilder<CommandSourceStack> root, String name, Consumer<LiteralArgumentBuilder<CommandSourceStack>> consumer) {
    LiteralArgumentBuilder<CommandSourceStack> subCommand = Commands.literal(name);
    consumer.accept(subCommand);
    root.then(subCommand);
  }

  /** Event listener to register the Mantle command */
  private static void registerCommand(RegisterCommandsEvent event) {
    LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("mantle");
    CommandBuildContext context = event.getBuildContext();

    // sub commands
    register(builder, "tags", b -> {
      register(b, "view", ViewTagCommand::register);
      register(b, "entries", DumpTagCommand::register);
      register(b, "dump", DumpAllTagsCommand::register);
      register(b, "for", TagsForCommand::register);
      register(b, "preference", TagPreferenceCommand::register);
      ModifyTagCommand.register(b);
    });
    register(builder, "dump_loot_modifiers", DumpLootModifiers::register);
    // Port: harvest_tiers is gone, 26.3 has no tool tiers (tool materials with tags)
    register(builder, "remove", b -> {
      b = b.requires(sender -> MantleCommand.hasPermission(sender, MantleCommand.PERMISSION_GAME_COMMANDS));
      register(b, "recipes", b2 -> RemoveRecipesCommand.register(b2, context));
      RemoveDataCommand.register(b);
    });
    // sources assets is registered as a client command
    register(builder, "sources", b -> {
      register(b, "data", SourcesCommand::register);
    });
    register(builder, "hunger", HungerCommand::register);

    // register final command
    event.getDispatcher().register(builder);
  }

  /* Helpers */

  /**
   * Returns true if the source either does not have reduced debug info or they have the proper level
   * Allows limiting a command that prints debug info to not work in reduced debug info
   * @param source             Command source
   * @param reducedDebugLevel  Level to use when reduced debug info is true
   * @return  True if the command can be run
   */
  public static boolean requiresDebugInfoOrOp(CommandSourceStack source, int reducedDebugLevel) {
    return !source.getLevel().getGameRules().get(GameRules.REDUCED_DEBUG_INFO) || MantleCommand.hasPermission(source, reducedDebugLevel);
  }
}
