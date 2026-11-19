package dimdoors;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dimdoors.gen.Dungeons;
import dimdoors.world.DDData;
import dimdoors.world.DDData.DimData;
import dimdoors.world.DDData.LinkData;
import dimdoors.world.DDWorld;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The commands of Dimensional Doors 1.4.1 (op level 4, like CommandBase of 1.5.2). They work where the command is run
 * (so also through {@code execute positioned ... run}). dd-create and dd-export, which made new dungeon schematics,
 * are not ported.
 */
final class DDCommands {
    private DDCommands() {
    }

    private static void say(CommandContext<CommandSourceStack> c, String text) {
        c.getSource().sendSuccess(() -> Component.literal(text), false);
    }

    private static int here(CommandContext<CommandSourceStack> c) {
        BlockPos pos = BlockPos.containing(c.getSource().getPosition());
        return DDWorld.dimId(c.getSource().getLevel(), pos);
    }

    static void register(CommandDispatcher<CommandSourceStack> d) {
        java.util.function.Predicate<CommandSourceStack> check = Commands.hasPermission(Commands.LEVEL_OWNERS);
        d.register(Commands.literal("dd-rift").requires(check)
                .then(Commands.argument("dungeon", StringArgumentType.word()).executes(DDCommands::rift)));
        d.register(Commands.literal("dd-deletelinks").requires(check).executes(c -> deleteLinks(c, here(c), false))
                .then(Commands.argument("dimension", IntegerArgumentType.integer()).executes(c -> deleteLinks(c, IntegerArgumentType.getInteger(c,
                        "dimension"), true))));
        d.register(Commands.literal("dd-???").requires(check).executes(c -> deleteRifts(c, here(c)))
                .then(Commands.argument("dimension", IntegerArgumentType.integer()).executes(c -> deleteRifts(c, IntegerArgumentType.getInteger(c,
                        "dimension")))));
        d.register(Commands.literal("dd-deletedimension").requires(check).executes(c -> deleteDimension(c, here(c)))
                .then(Commands.argument("dimension", IntegerArgumentType.integer()).executes(c -> deleteDimension(c, IntegerArgumentType.getInteger(c,
                        "dimension")))));
        d.register(Commands.literal("dd-dimensiondata").requires(check).executes(c -> printData(c, here(c)))
                .then(Commands.argument("dimension", IntegerArgumentType.integer()).executes(c -> printData(c, IntegerArgumentType.getInteger(c,
                        "dimension")))));
        d.register(Commands.literal("dd-prune").requires(check).executes(DDCommands::prune)
                .then(Commands.literal("delete").executes(DDCommands::prune)));
        d.register(Commands.literal("dd-resetdungeons").requires(check).executes(DDCommands::resetDungeons));
    }

    /** CommandCreateDungeonRift: a rift to a dungeon one block above the sender ("list" names them, "random" lets chance choose). */
    private static int rift(CommandContext<CommandSourceStack> c) {
        String name = StringArgumentType.getString(c, "dungeon");
        if (name.equals("list")) {
            for (String dungeon : Dungeons.names()) {
                say(c, dungeon);
            }
            say(c, "");
            return 1;
        }
        BlockPos pos = BlockPos.containing(c.getSource().getPosition());
        int dim = here(c);
        LinkData link = new LinkData(dim, 0, pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX(), pos.getY() + 1, pos.getZ(), true, 3);
        if (name.equals("random")) {
            DDWorld.createLink(link, true);
            link = DDWorld.createPocket(link, true, true);
            say(c, "Created a rift to a random dungeon (Dimension ID = " + link.destDim + ").");
            return 1;
        }
        Dungeons.Dungeon result = Dungeons.byPartialName(name, Dungeons.REGISTERED);
        if (result == null) {
            result = Dungeons.byPartialName(name, Dungeons.CUSTOM);
        }
        if (result == null) {
            c.getSource().sendFailure(Component.literal("Error: The specified dungeon was not found. Use 'list' to see a list of the available dungeons."));
            return 0;
        }
        link = DDWorld.createPocket(link, true, true);
        DDWorld.data().dim(link.destDim).dungeon = result.path();
        DDWorld.data().setDirty();
        say(c, "Created a rift to \"" + result.name() + "\" dungeon (Dimension ID = " + link.destDim + ").");
        return 1;
    }

    /** CommandDeleteAllLinks: every link of the dimension and the block where it starts. */
    private static int deleteLinks(CommandContext<CommandSourceStack> c, int dim, boolean given) {
        DimData data = DDWorld.data().dim(dim);
        if (data == null) {
            if (given) {
                say(c, "Error- dim " + dim + " not registered");
            }
            return 0;
        }
        ServerLevel level = DDWorld.level(dim);
        int removed = 0;
        for (LinkData link : data.links()) {
            DDWorld.removeLink(dim, link.locX, link.locY, link.locZ);
            if (level != null) {
                level.setBlock(new BlockPos(link.locX, link.locY, link.locZ), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            removed++;
        }
        say(c, "Removed " + removed + " links.");
        return removed;
    }

    /** CommandDeleteRifts ("dd-???"): the links whose rift is open, and their rifts. */
    private static int deleteRifts(CommandContext<CommandSourceStack> c, int dim) {
        DimData data = DDWorld.data().dim(dim);
        if (data == null) {
            say(c, "Error- dim " + dim + " not registered");
            return 0;
        }
        ServerLevel level = DDWorld.level(dim);
        int removed = 0;
        for (LinkData link : data.links()) {
            BlockPos pos = new BlockPos(link.locX, link.locY, link.locZ);
            if (level != null && level.getBlockState(pos).is(DimDoors.RIFT.get())) {
                DDWorld.removeLink(dim, link.locX, link.locY, link.locZ);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                removed++;
            }
        }
        say(c, "Removed " + removed + " rifts.");
        return removed;
    }

    /** CommandDeleteDimensionData: forgets a dimension and every link into it. */
    private static int deleteDimension(CommandContext<CommandSourceStack> c, int dim) {
        DDData all = DDWorld.data();
        if (all.dim(dim) == null) {
            say(c, "Error- dimension " + dim + " not registered with dimDoors");
            return 0;
        }
        int removed = 0;
        for (DimData data : new ArrayList<>(all.dims().values())) {
            for (LinkData link : data.links()) {
                if (link.destDim == dim) {
                    DDWorld.removeLink(link.locDim, link.locX, link.locY, link.locZ);
                    removed++;
                }
                if (data.id == dim) {
                    removed++;
                }
            }
        }
        all.dims().remove(dim);
        all.setDirty();
        say(c, "Removed dimension " + dim + " from DimDoors and deleted " + removed + " links");
        return 1;
    }

    private static int printData(CommandContext<CommandSourceStack> c, int dim) {
        DimData data = DDWorld.data().dim(dim);
        if (data == null) {
            c.getSource().sendFailure(Component.literal("Error: The specified dimension is not registered with Dimensional Doors."));
            return 0;
        }
        say(c, "Dimension ID = " + data.id);
        say(c, "Dimension Depth = " + data.depth);
        if (data.dungeon != null) {
            say(c, "Dungeon = " + data.dungeon);
        }
        for (LinkData link : data.links()) {
            say(c, link.locDim + "locDimID " + link.locX + ":locXCoord " + link.locY + ":locYCoord " + link.locZ + ":locZCoord -> " + link.destDim
                    + " " + link.destX + " " + link.destY + " " + link.destZ);
        }
        return 1;
    }

    /** CommandPruneDimensions: pockets no link leads to any more are forgotten. */
    private static int prune(CommandContext<CommandSourceStack> c) {
        DDData all = DDWorld.data();
        Set<Integer> linked = new HashSet<>();
        List<DimData> dims = new ArrayList<>(all.dims().values());
        for (DimData data : dims) {
            for (LinkData link : data.links()) {
                linked.add(link.destDim);
            }
        }
        int removed = 0;
        for (DimData data : dims) {
            if (!linked.contains(data.id) && data.isPocket) {
                all.dims().remove(data.id);
                removed++;
            }
        }
        all.setDirty();
        say(c, "Removed " + removed + " unreachable pocket dims.");
        return removed;
    }

    /** CommandResetDungeons: dungeons nobody is in are emptied and built anew when entered next. */
    private static int resetDungeons(CommandContext<CommandSourceStack> c) {
        int dungeons = 0;
        int reset = 0;
        for (DimData data : DDWorld.data().dims().values()) {
            if (data.isDimRandomRift) {
                dungeons++;
                if (DDWorld.resetPocket(data)) {
                    reset++;
                }
            }
        }
        say(c, "Reset complete. " + reset + " out of " + dungeons + " dungeons were reset.");
        return reset;
    }
}
