package cofh;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * {@code /cofh} of CoFH Core 1.5.2.5 ({@code CommandHandler}): {@code tps} as in {@code CommandTPS}, {@code help} and
 * {@code version} with the original's text. Usable by everyone, as in the original. {@code cape} and {@code skin}
 * are not ported (see {@link CoFHCore}).
 */
public final class CoFHCommand {
    private static final DecimalFormat FMT = new DecimalFormat("##0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final String SORRY = "Sorry about that, still working on this. Available commands are /cofh cape and /cofh skin.";
    private static final String USAGE = "Type '/cofh help' for help.";

    private CoFHCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cofh")
                .executes(c -> fail(c))
                .then(Commands.literal("help").executes(c -> say(c, SORRY)))
                .then(Commands.literal("version").executes(c -> say(c, SORRY)))
                .then(Commands.literal("tps")
                        .executes(c -> tps(c, null))
                        .then(Commands.argument("arg", StringArgumentType.word())
                                .executes(c -> tps(c, StringArgumentType.getString(c, "arg"))))));
    }

    private static int fail(CommandContext<CommandSourceStack> c) {
        c.getSource().sendFailure(Component.literal(USAGE));
        return 0;
    }

    private static int say(CommandContext<CommandSourceStack> c, String text) {
        c.getSource().sendSystemMessage(Component.literal(text));
        return 1;
    }

    /** Mean of the tick times in nanoseconds, in milliseconds (the original divided as long, then scaled). */
    private static double tickMs(long[] times) {
        if (times == null || times.length == 0) {
            return 0.0;
        }
        long sum = 0L;
        for (long t : times) {
            sum += t;
        }
        return (double) (sum / times.length) * 1.0E-6;
    }

    private static double tps(double ms) {
        double tps = 1000.0 / ms;
        return tps > 20.0 ? 20.0 : tps;
    }

    private static String name(ServerLevel level) {
        return level.dimension().identifier().toString();
    }

    private static int tps(CommandContext<CommandSourceStack> c, String arg) {
        MinecraftServer server = c.getSource().getServer();
        Consumer<String> out = s -> c.getSource().sendSystemMessage(Component.literal(s));
        double overallMs = tickMs(server.getTickTimesNanos());
        double overallTps = tps(overallMs);
        if (arg == null) {
            out.accept("Overall: " + FMT.format(overallTps) + " TPS/" + FMT.format(overallMs) + "MS (" + (int) (overallTps / 20.0 * 100.0) + "%)");
            for (ServerLevel level : server.getAllLevels()) {
                double ms = tickMs(server.getTickTime(level.dimension()));
                double tps = tps(ms);
                out.accept(name(level) + ": " + FMT.format(tps) + " TPS/" + FMT.format(ms) + "MS (" + (int) (tps / 20.0 * 100.0) + "%)");
            }
            return 1;
        }
        char first = arg.toLowerCase(Locale.ROOT).charAt(0);
        if (first == 'o' || first == 'a') {
            out.accept("Overall server tick");
            out.accept("TPS: " + FMT.format(overallTps) + " TPS of " + FMT.format(20L) + " TPS (" + (int) (overallTps / 20.0 * 100.0) + "%)");
            out.accept("Tick time: " + FMT.format(overallMs) + " ms of " + FMT.format(50L) + " ms");
            if (first == 'a') {
                int chunks = 0;
                int entities = 0;
                int worlds = 0;
                for (ServerLevel level : server.getAllLevels()) {
                    chunks += level.getChunkSource().getLoadedChunksCount();
                    entities += countEntities(level);
                    worlds++;
                }
                out.accept("Total Loaded Worlds/Chunks: " + worlds + "/" + chunks);
                out.accept("Total Entities: " + entities);
            }
            return 1;
        }
        // the original took the dimension id; here also the dimension's name
        String wanted = switch (arg) {
            case "0" -> "overworld";
            case "-1" -> "the_nether";
            case "1" -> "the_end";
            default -> arg;
        };
        ServerLevel found = null;
        for (ServerLevel level : server.getAllLevels()) {
            if (name(level).equals(wanted) || level.dimension().identifier().getPath().equals(wanted)) {
                found = level;
            }
        }
        if (found == null) {
            c.getSource().sendFailure(Component.literal("World not found"));
            return 0;
        }
        double ms = tickMs(server.getTickTime(found.dimension()));
        double tps = tps(ms);
        out.accept("World " + name(found) + " - Loaded chunks: " + found.getChunkSource().getLoadedChunksCount());
        out.accept("TPS: " + FMT.format(tps) + "/" + FMT.format(20L) + " TPS (" + (int) (tps / 20.0 * 100.0) + "%) - Tick: "
                + FMT.format(ms) + " ms of " + FMT.format(50L) + " ms");
        out.accept("Entities: " + countEntities(found));
        return 1;
    }

    private static int countEntities(ServerLevel level) {
        int n = 0;
        for (var ignored : level.getAllEntities()) {
            n++;
        }
        return n;
    }
}
