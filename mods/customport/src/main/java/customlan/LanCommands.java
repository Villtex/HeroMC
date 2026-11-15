package customlan;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.commands.BanListCommands;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.commands.DeOpCommands;
import net.minecraft.server.commands.OpCommand;
import net.minecraft.server.commands.PardonCommand;
import net.minecraft.server.commands.PardonIpCommand;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.commands.SaveOffCommand;
import net.minecraft.server.commands.SaveOnCommand;
import net.minecraft.server.commands.StopCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * The commands of Custom LAN 2.2. A command whose key in CLPermissions.txt is 0 is open to everyone, otherwise it
 * needs the permission level of the original. The server commands of 1.5.2 that a LAN world did not have (ban, op,
 * save ...) are the vanilla ones of the dedicated server, registered here for the integrated server too; /kick and
 * /tp are vanilla in every world now, /reload is the datapack reload of vanilla.
 */
public final class LanCommands {
    private LanCommands() {
    }

    private static final Predicate<CommandSourceStack> ALL = s -> true;

    private static Predicate<CommandSourceStack> level(int level) {
        PermissionCheck check = switch (level) {
            case 0 -> Commands.LEVEL_ALL;
            case 1 -> Commands.LEVEL_MODERATORS;
            case 2 -> Commands.LEVEL_GAMEMASTERS;
            case 3 -> Commands.LEVEL_ADMINS;
            default -> Commands.LEVEL_OWNERS;
        };
        return Commands.hasPermission(check);
    }

    /** canCommandSenderUseCommand of the commands: key open, or the permission level. */
    private static Predicate<CommandSourceStack> perm(String name, int level) {
        Predicate<CommandSourceStack> lv = level(level);
        return s -> CLPermissions.canSenderUse(name) || lv.test(s);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> cmd(String name, int level) {
        return Commands.literal(name).requires(perm(name, level));
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        // vanilla server commands, with the keys of Custom LAN and its aliases
        copyVanilla(d, BanPlayerCommands::register, "ban", perm("ban", 2), "ban");
        copyVanilla(d, BanIpCommands::register, "ban-ip", perm("ban-ip", 2), "ban-ip", "banip");
        copyVanilla(d, BanListCommands::register, "banlist", perm("banlist", 2), "banlist");
        copyVanilla(d, PardonCommand::register, "pardon", perm("unban", 2), "pardon", "unban");
        copyVanilla(d, PardonIpCommand::register, "pardon-ip", perm("unban-ip", 2), "pardon-ip", "unban-ip", "unbanip");
        copyVanilla(d, OpCommand::register, "op", level(3), "op");
        copyVanilla(d, DeOpCommands::register, "deop", level(3), "deop");
        copyVanilla(d, SaveAllCommand::register, "save-all", level(2), "save-all");
        copyVanilla(d, SaveOffCommand::register, "save-off", level(4), "save-off");
        copyVanilla(d, SaveOnCommand::register, "save-on", level(4), "save-on");
        copyVanilla(d, StopCommand::register, "stop", level(2), "stop");

        d.register(Commands.literal("reloadops").requires(level(4)).executes(c -> {
            String ops = String.join(", ", c.getSource().getServer().getPlayerList().getOps().getUserList());
            c.getSource().sendSuccess(() -> Component.literal("Reloaded ops. Current ops are: " + ops), false);
            return 1;
        }));
        d.register(cmd("back", 2).executes(LanCommands::back));
        d.register(cmd("broadcast", 4).then(Commands.argument("message", StringArgumentType.greedyString()).executes(c -> {
            Component msg = Component.literal("§5[BROADCAST] " + StringArgumentType.getString(c, "message"));
            for (ServerPlayer p : c.getSource().getServer().getPlayerList().getPlayers()) {
                p.sendSystemMessage(msg);
            }
            return 1;
        })));
        d.register(Commands.literal("explosion").requires(level(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            BlockPos hit = lookedAt(p, 125.0);
            if (hit != null) {
                p.level().explode(p, hit.getX(), hit.getY(), hit.getZ(), 5.0F, true, Level.ExplosionInteraction.TNT);
            }
            return 1;
        }));
        d.register(cmd("fireball", 4).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            Vec3 look = p.getLookAngle();
            LargeFireball fireball = new LargeFireball(p.level(), p, look.scale(0.1), 1);
            fireball.setPos(p.getX() + look.x * 5.0, p.getY() + look.y * 5.0, p.getZ() + look.z * 5.0);
            p.level().addFreshEntity(fireball);
            return 1;
        }));
        d.register(cmd("fly", 4).executes(c -> fly(c, c.getSource().getPlayerOrException(), true))
                .then(Commands.argument("player", StringArgumentType.word()).suggests(LanCommands::suggestPlayers)
                        .executes(c -> fly(c, player(c), false))));
        d.register(cmd("gettime", 4).executes(LanCommands::getTime));
        d.register(cmd("god", 2).executes(c -> god(c, c.getSource().getPlayerOrException(), true))
                .then(Commands.argument("player", StringArgumentType.word()).suggests(LanCommands::suggestPlayers)
                        .executes(c -> god(c, player(c), false))));
        d.register(cmd("hat", 0).executes(c -> hat(c, c.getSource().getPlayerOrException()))
                .then(Commands.argument("player", StringArgumentType.word()).suggests(LanCommands::suggestPlayers)
                        .executes(c -> hat(c, player(c)))));
        d.register(cmd("heal", 2).executes(c -> heal(c, c.getSource().getPlayerOrException(), true))
                .then(Commands.argument("player", StringArgumentType.word()).suggests(LanCommands::suggestPlayers)
                        .executes(c -> heal(c, player(c), false))));
        d.register(cmd("home", 0).executes(LanCommands::home));
        d.register(cmd("sethome", 0).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            p.setRespawnPosition(new ServerPlayer.RespawnConfig(LevelData.RespawnData.of(p.level().dimension(), p.blockPosition(), p.getYRot(), p.getXRot()), true), false);
            c.getSource().sendSuccess(() -> Component.literal("Your home has been set to this location."), true);
            return 1;
        }));
        d.register(cmd("lightning", 2).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            BlockPos hit = lookedAt(p, 125.0);
            if (hit != null) {
                LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(p.level(), EntitySpawnReason.COMMAND);
                if (bolt != null) {
                    bolt.snapTo(hit.getX(), hit.getY(), hit.getZ());
                    p.level().addFreshEntity(bolt);
                }
            }
            return 1;
        }));
        // CommandLANMOTD: always allowed
        d.register(Commands.literal("viewmotd").requires(ALL).executes(c -> {
            String motd = c.getSource().getServer().getMotd();
            c.getSource().sendSuccess(() -> Component.literal("MOTD: " + motd), false);
            return 1;
        }));
        d.register(cmd("pm", 2).then(Commands.argument("player", StringArgumentType.word()).suggests(LanCommands::suggestPlayers)
                .then(Commands.argument("message", StringArgumentType.greedyString()).executes(LanCommands::pm))));
        d.register(cmd("setspawn", 2).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            BlockPos pos = p.blockPosition();
            c.getSource().getServer().setRespawnData(LevelData.RespawnData.of(p.level().dimension(), pos, 0.0F, 0.0F));
            c.getSource().sendSuccess(() -> Component.literal("Spawn coordinates set to (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")."), true);
            return 1;
        }));
        d.register(cmd("spawn", 0).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            MinecraftServer server = c.getSource().getServer();
            LevelData.RespawnData spawn = server.getRespawnData();
            ServerLevel level = server.getLevel(spawn.dimension());
            BlockPos pos = spawn.pos();
            p.teleportTo(level == null ? server.overworld() : level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, Set.of(), 0.0F, 0.0F, true);
            c.getSource().sendSuccess(() -> Component.literal("You have been teleported to spawn."), true);
            return 1;
        }));
        d.register(cmd("spawnmob", 2).then(Commands.argument("mob", StringArgumentType.word()).executes(c -> spawnMob(c, null))
                .then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("y", IntegerArgumentType.integer())
                        .then(Commands.argument("z", IntegerArgumentType.integer()).executes(c -> spawnMob(c,
                                new BlockPos(IntegerArgumentType.getInteger(c, "x"), IntegerArgumentType.getInteger(c, "y"), IntegerArgumentType.getInteger(c, "z")))))))));
        d.register(cmd("setwarp", 0).then(Commands.argument("warppoint", StringArgumentType.word()).executes(LanCommands::setWarp)));
        d.register(cmd("warp", 0).then(Commands.argument("warppoint", StringArgumentType.word()).suggests(LanCommands::suggestWarps).executes(LanCommands::warp)));
        d.register(cmd("removewarp", 2).then(Commands.argument("warppoint", StringArgumentType.word()).suggests(LanCommands::suggestWarps).executes(LanCommands::removeWarp)));
        d.register(cmd("warplist", 2).executes(c -> {
            List<String> warps = warpNames(c.getSource().getServer());
            if (warps.isEmpty()) {
                c.getSource().sendSuccess(() -> Component.literal("You have no saved warppoints."), false);
            } else {
                c.getSource().sendSuccess(() -> Component.literal("Saved warppoints are: §6" + String.join(", ", warps)), false);
            }
            return 1;
        }));
    }

    @FunctionalInterface
    private interface Registrar {
        void register(CommandDispatcher<CommandSourceStack> dispatcher);
    }

    /** Registers a vanilla command in a scratch dispatcher and copies it under each name with another requirement. */
    private static void copyVanilla(CommandDispatcher<CommandSourceStack> d, Registrar registrar, String vanillaName,
            Predicate<CommandSourceStack> requirement, String... names) {
        CommandDispatcher<CommandSourceStack> scratch = new CommandDispatcher<>();
        registrar.register(scratch);
        CommandNode<CommandSourceStack> node = scratch.getRoot().getChild(vanillaName);
        if (node == null) {
            CustomLAN.LOGGER.warn("[CustomLAN]: vanilla command {} not found", vanillaName);
            return;
        }
        for (String name : names) {
            if (d.getRoot().getChild(name) != null) {
                continue; // already there (e.g. a dedicated server)
            }
            LiteralArgumentBuilder<CommandSourceStack> copy = Commands.literal(name).requires(requirement);
            if (node.getCommand() != null) {
                copy.executes(node.getCommand());
            }
            for (CommandNode<CommandSourceStack> child : node.getChildren()) {
                copy.then(child);
            }
            d.register(copy);
        }
    }

    // ---- helpers ----

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestPlayers(
            CommandContext<CommandSourceStack> c, com.mojang.brigadier.suggestion.SuggestionsBuilder b) {
        for (String n : c.getSource().getServer().getPlayerNames()) {
            if (n.toLowerCase(Locale.ROOT).startsWith(b.getRemainingLowerCase())) {
                b.suggest(n);
            }
        }
        return b.buildFuture();
    }

    /** The prefix lookup of the commands: the first online player whose name starts with the argument. */
    private static ServerPlayer findPlayer(MinecraftServer server, String arg) {
        String lower = arg.toLowerCase(Locale.ROOT);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.getGameProfile().name().toLowerCase(Locale.ROOT).startsWith(lower)) {
                return p;
            }
        }
        return null;
    }

    private static ServerPlayer player(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        String arg = StringArgumentType.getString(c, "player");
        ServerPlayer p = findPlayer(c.getSource().getServer(), arg);
        if (p == null) {
            throw net.minecraft.commands.arguments.EntityArgument.NO_PLAYERS_FOUND.create();
        }
        return p;
    }

    private static String name(ServerPlayer p) {
        return p.getGameProfile().name();
    }

    private static BlockPos lookedAt(ServerPlayer p, double reach) {
        HitResult hit = p.pick(reach, 1.0F, false);
        return hit instanceof BlockHitResult b && hit.getType() == HitResult.Type.BLOCK ? b.getBlockPos() : null;
    }

    // ---- commands ----

    private static Path lastPosFile(ServerPlayer p) {
        return FMLPaths.GAMEDIR.get().resolve("lastPos").resolve(name(p) + ".pos");
    }

    /** The death point for /back (the original command read lastPos/<name>.pos, but nothing wrote it). */
    static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) {
            return;
        }
        Properties props = new Properties();
        props.setProperty("x", String.valueOf(p.getX()));
        props.setProperty("y", String.valueOf(p.getY()));
        props.setProperty("z", String.valueOf(p.getZ()));
        props.setProperty("yaw", String.valueOf(p.getYRot()));
        props.setProperty("pitch", String.valueOf(p.getXRot()));
        props.setProperty("dimension", p.level().dimension().identifier().toString());
        try {
            Files.createDirectories(lastPosFile(p).getParent());
            try (OutputStream out = Files.newOutputStream(lastPosFile(p))) {
                props.store(out, null);
            }
        } catch (IOException e) {
            CustomLAN.LOGGER.warn("[CustomLAN]: could not save the death point of {}", name(p), e);
        }
    }

    private static int back(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        Path file = lastPosFile(p);
        if (!Files.exists(file)) {
            p.sendSystemMessage(Component.literal("You do not have a saved death point."));
            return 0;
        }
        try (InputStream in = Files.newInputStream(file)) {
            Properties props = new Properties();
            props.load(in);
            ServerLevel level = p.level();
            String dim = props.getProperty("dimension");
            if (dim != null) {
                Identifier id = Identifier.tryParse(dim);
                ServerLevel l = id == null ? null : c.getSource().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
                if (l != null) {
                    level = l;
                }
            }
            p.teleportTo(level, Double.parseDouble(props.getProperty("x")), Double.parseDouble(props.getProperty("y")),
                    Double.parseDouble(props.getProperty("z")), Set.of(), Float.parseFloat(props.getProperty("yaw")),
                    Float.parseFloat(props.getProperty("pitch")), true);
        } catch (IOException | RuntimeException e) {
            p.sendSystemMessage(Component.literal("Error. Could not get your last death point."));
            return 0;
        }
        return 1;
    }

    private static int fly(CommandContext<CommandSourceStack> c, ServerPlayer target, boolean self) {
        if (target.isCreative()) {
            String msg = self ? "The fly command does not work while you are in Creative mode."
                    : "The fly command does not work while " + name(target) + " is in Creative mode.";
            c.getSource().sendSuccess(() -> Component.literal(msg), true);
            return 0;
        }
        target.getAbilities().mayfly = !target.getAbilities().mayfly;
        target.getAbilities().flying = false;
        target.onUpdateAbilities();
        String state = target.getAbilities().mayfly ? "enabled" : "disabled";
        String msg = self ? "Flying is now " + state + "." : "Flying is now " + state + " for " + name(target) + ".";
        c.getSource().sendSuccess(() -> Component.literal(msg), true);
        return 1;
    }

    /** CommandLANGetTime, with its formula (hour 1-24, "PM" from 14 on). */
    private static int getTime(CommandContext<CommandSourceStack> c) {
        ServerLevel level = c.getSource().getLevel();
        long day = level.getGameTime() / 24000L;
        long time = level.getOverworldClockTime();
        long hour = (time / 1000L + 6L) % 24L + 1L;
        long minute = time % 1000L * 60L / 1000L;
        String ampm = "AM";
        if (hour > 13L) {
            hour -= 12L;
            ampm = "PM";
        }
        String text = "Day " + day + ", " + String.format("%2d:%02d", hour, minute).replace(" ", "") + " " + ampm;
        c.getSource().sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static int god(CommandContext<CommandSourceStack> c, ServerPlayer target, boolean self) {
        if (target.isCreative()) {
            String msg = self ? "God mode does not work while you are in Creative mode."
                    : "God mode does not work while " + name(target) + " is in Creative mode.";
            c.getSource().sendSuccess(() -> Component.literal(msg), true);
            return 0;
        }
        target.getAbilities().invulnerable = !target.getAbilities().invulnerable;
        target.onUpdateAbilities();
        String state = target.getAbilities().invulnerable ? "on" : "off";
        if (self) {
            c.getSource().sendSuccess(() -> Component.literal("God mode is now " + state + "."), true);
        } else {
            c.getSource().sendSuccess(() -> Component.literal("God mode is now " + state + " for " + name(target) + "."), true);
            target.sendSystemMessage(Component.literal(c.getSource().getTextName() + " turned " + state + " god mode for you."));
        }
        return 1;
    }

    private static int hat(CommandContext<CommandSourceStack> c, ServerPlayer target) {
        ItemStack held = target.getMainHandItem();
        if (held.isEmpty()) {
            c.getSource().sendSuccess(() -> Component.literal("You must have something to wear as a hat!"), true);
            return 0;
        }
        ItemStack helmet = target.getItemBySlot(EquipmentSlot.HEAD);
        target.setItemSlot(EquipmentSlot.HEAD, held);
        target.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        if (!helmet.isEmpty() && !target.getInventory().add(helmet)) {
            target.drop(helmet, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        Component name = held.getHoverName();
        c.getSource().sendSuccess(() -> Component.literal("You are now wearing ").append(name).append(" as a hat."), true);
        return 1;
    }

    private static int heal(CommandContext<CommandSourceStack> c, ServerPlayer target, boolean self) {
        String arg = self ? "" : name(target);
        if (target.isCreative()) {
            String msg = self ? "Cannot heal while you are in Creative mode." : "You cannot heal " + arg + " while he is in Creative mode.";
            c.getSource().sendSuccess(() -> Component.literal(msg), true);
            return 0;
        }
        target.heal(20.0F);
        if (self) {
            c.getSource().sendSuccess(() -> Component.literal("Your health is now set to full."), true);
        } else {
            c.getSource().sendSuccess(() -> Component.literal("Succesfully healed " + arg + "."), true);
            target.sendSystemMessage(Component.literal(c.getSource().getTextName() + " has healed you."));
        }
        return 1;
    }

    private static int home(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        ServerPlayer.RespawnConfig respawn = p.getRespawnConfig();
        if (respawn == null) {
            p.sendSystemMessage(Component.literal("You do not have a home. Use /sethome first."));
            return 0;
        }
        ServerLevel level = c.getSource().getServer().getLevel(respawn.respawnData().dimension());
        BlockPos pos = respawn.respawnData().pos();
        p.teleportTo(level == null ? p.level() : level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, Set.of(), p.getYRot(), p.getXRot(), true);
        c.getSource().sendSuccess(() -> Component.literal("Welcome home, " + name(p) + "."), true);
        return 1;
    }

    private static int pm(CommandContext<CommandSourceStack> c) {
        String arg = StringArgumentType.getString(c, "player");
        String text = StringArgumentType.getString(c, "message");
        ServerPlayer target = findPlayer(c.getSource().getServer(), arg);
        if (target == null) {
            c.getSource().sendFailure(Component.literal("Could not find a player starting with \"" + arg + "\"."));
            return 0;
        }
        target.sendSystemMessage(Component.literal("PM with " + c.getSource().getTextName() + ": " + text));
        c.getSource().sendSystemMessage(Component.literal("PM with " + name(target) + ": " + text));
        return 1;
    }

    /** CommandLANSpawnMob: the mob by a part of its name, in the order of the original. */
    private static EntityType<?> mobByName(String arg) {
        String n = arg.toLowerCase(Locale.ROOT);
        if (n.contains("creeper")) return EntityTypes.CREEPER;
        if (n.contains("skeleton") && !n.contains("wither")) return EntityTypes.SKELETON;
        if (n.contains("spider") && !n.contains("cave")) return EntityTypes.SPIDER;
        if (n.contains("giant")) return EntityTypes.GIANT;
        if (n.contains("zombie") && n.contains("pig")) return EntityTypes.ZOMBIFIED_PIGLIN;
        if (n.contains("zombie")) return EntityTypes.ZOMBIE;
        if (n.contains("slime")) return EntityTypes.SLIME;
        if (n.contains("ghast")) return EntityTypes.GHAST;
        if (n.contains("ender") && n.contains("man")) return EntityTypes.ENDERMAN;
        if (n.contains("cave") && n.contains("spider")) return EntityTypes.CAVE_SPIDER;
        if (n.contains("silver")) return EntityTypes.SILVERFISH;
        if (n.contains("blaze")) return EntityTypes.BLAZE;
        if (n.contains("magma")) return EntityTypes.MAGMA_CUBE;
        if (n.contains("dragon")) return EntityTypes.ENDER_DRAGON;
        if (n.contains("wither") && n.contains("skeleton")) return EntityTypes.WITHER_SKELETON;
        if (n.contains("wither")) return EntityTypes.WITHER;
        if (n.contains("witch")) return EntityTypes.WITCH;
        if (n.contains("bat")) return EntityTypes.BAT;
        if (n.contains("pig")) return EntityTypes.PIG;
        if (n.contains("sheep")) return EntityTypes.SHEEP;
        if (n.contains("mooshroom") || n.contains("mushroom") || n.contains("mush")) return EntityTypes.MOOSHROOM;
        if (n.contains("cow")) return EntityTypes.COW;
        if (n.contains("chicken")) return EntityTypes.CHICKEN;
        if (n.contains("squid")) return EntityTypes.SQUID;
        if (n.contains("wolf")) return EntityTypes.WOLF;
        if (n.contains("snow")) return EntityTypes.SNOW_GOLEM;
        if (n.contains("ocelot") || n.contains("cat")) return EntityTypes.OCELOT;
        if (n.contains("iron") || n.contains("golem")) return EntityTypes.IRON_GOLEM;
        if (n.contains("village")) return EntityTypes.VILLAGER;
        return null;
    }

    private static int spawnMob(CommandContext<CommandSourceStack> c, BlockPos given) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String arg = StringArgumentType.getString(c, "mob");
        EntityType<?> type = mobByName(arg);
        if (type == null) {
            c.getSource().sendSuccess(() -> Component.literal("Could not find an entity with the name \"" + arg + "\"."), true);
            return 0;
        }
        BlockPos pos = given != null ? given : lookedAt(p, 256.0);
        if (pos == null) {
            c.getSource().sendFailure(Component.literal("You are not looking at a block."));
            return 0;
        }
        ServerLevel level = p.level();
        Entity entity = type.create(level, EntitySpawnReason.COMMAND);
        if (entity == null) {
            return 0;
        }
        entity.snapTo(pos.getX(), pos.getY() + 1, pos.getZ(), p.getYRot(), 0.0F);
        if (entity instanceof Mob mob) {
            mob.yHeadRot = mob.getYRot();
            mob.yBodyRot = mob.getYRot();
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.COMMAND, null);
        }
        level.addFreshEntity(entity);
        if (entity instanceof Mob mob) {
            mob.playAmbientSound();
        }
        Component name = entity.getName();
        double x = entity.getX(), y = entity.getY(), z = entity.getZ();
        c.getSource().sendSuccess(() -> Component.literal("Spawned ").append(name).append(" at " + x + ", " + y + ", " + z + "."), true);
        return 1;
    }

    // ---- warps: warppoints/<world seed>/<name>, a Properties file with x, y, z ----

    private static Path warpDir(MinecraftServer server) {
        return FMLPaths.GAMEDIR.get().resolve("warppoints").resolve(Long.toString(server.overworld().getSeed()));
    }

    private static boolean validWarpName(String name) {
        return name.matches("[A-Za-z0-9_.+-]+") && !name.startsWith(".");
    }

    private static List<String> warpNames(MinecraftServer server) {
        List<String> names = new ArrayList<>();
        Path dir = warpDir(server);
        if (Files.isDirectory(dir)) {
            try (Stream<Path> s = Files.list(dir)) {
                s.filter(Files::isRegularFile).map(f -> f.getFileName().toString()).sorted().forEach(names::add);
            } catch (IOException ignored) {
            }
        }
        return names;
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestWarps(
            CommandContext<CommandSourceStack> c, com.mojang.brigadier.suggestion.SuggestionsBuilder b) {
        for (String n : warpNames(c.getSource().getServer())) {
            if (n.toLowerCase(Locale.ROOT).startsWith(b.getRemainingLowerCase())) {
                b.suggest(n);
            }
        }
        return b.buildFuture();
    }

    private static int setWarp(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(c, "warppoint");
        if (!validWarpName(name)) {
            c.getSource().sendFailure(Component.literal("Could not create warppoint files."));
            return 0;
        }
        Properties props = new Properties();
        props.setProperty("x", String.valueOf(p.getX()));
        props.setProperty("y", String.valueOf(p.getY()));
        props.setProperty("z", String.valueOf(p.getZ()));
        try {
            Path dir = warpDir(c.getSource().getServer());
            Files.createDirectories(dir);
            try (OutputStream out = Files.newOutputStream(dir.resolve(name))) {
                props.store(out, null);
            }
        } catch (IOException e) {
            c.getSource().sendFailure(Component.literal("Could not create warppoint files."));
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.literal("Warppoint \"" + name + "\" has been saved."), true);
        return 1;
    }

    /** CommandLANWarp: the coordinates cut to whole numbers (towards zero) as in the original, rotation kept. */
    private static int warp(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(c, "warppoint");
        Path file = warpDir(c.getSource().getServer()).resolve(name);
        if (!validWarpName(name) || !Files.isRegularFile(file)) {
            c.getSource().sendSuccess(() -> Component.literal("Could not find warppoint \"" + name + "\"."), true);
            return 0;
        }
        try (InputStream in = Files.newInputStream(file)) {
            Properties props = new Properties();
            props.load(in);
            int x = (int) Double.parseDouble(props.getProperty("x"));
            int y = (int) Double.parseDouble(props.getProperty("y"));
            int z = (int) Double.parseDouble(props.getProperty("z"));
            p.teleportTo(x, y, z);
        } catch (IOException | RuntimeException e) {
            c.getSource().sendSuccess(() -> Component.literal("Could not find warppoint \"" + name + "\"."), true);
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.literal("Warped to warppoint \"" + name + "\"."), true);
        return 1;
    }

    private static int removeWarp(CommandContext<CommandSourceStack> c) {
        String name = StringArgumentType.getString(c, "warppoint");
        Path file = warpDir(c.getSource().getServer()).resolve(name);
        if (!validWarpName(name) || !Files.isRegularFile(file)) {
            c.getSource().sendSuccess(() -> Component.literal("Could not find warppoint \"" + name + "\"."), true);
            return 0;
        }
        try {
            Files.delete(file);
        } catch (IOException e) {
            c.getSource().sendSuccess(() -> Component.literal("Could not remove \"" + name + "\"."), true);
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.literal("Warppoint \"" + name + "\" has been removed."), true);
        return 1;
    }
}
