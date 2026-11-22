package tconstruct;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.slf4j.LoggerFactory;

/**
 * Teil 26e: {@code /hexxitcompat} (operators) checks the interfaces for other mods at a server without players
 * (Skill server-tests-263): {@code item <pos> "<item>"} inserts 5, extracts 2 and aborts an extract of 1 through
 * {@code Capabilities.Item.BLOCK}; {@code fluid <pos> "<fluid>"} does the same with buckets through
 * {@code Capabilities.Fluid.BLOCK}; {@code melt "<item>"} shows the smeltery result; {@code tag "<item>" "<tag>"} and
 * {@code recipe "<id>"} show whether an item is in a tag and a recipe is loaded; {@code mine <pos> "<tool>"} (Teil 26f)
 * shows whether a tool harvests the block there, its speed and the drops. Ids in quotes.
 */
final class CompatCommand {
    static void init() {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> e.getDispatcher().register(Commands.literal("hexxitcompat").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("item").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("what", StringArgumentType.string()).executes(c -> {
                    ServerLevel level = c.getSource().getLevel();
                    BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
                    var h = level.getCapability(Capabilities.Item.BLOCK, pos, Direction.UP);
                    String msg;
                    if (h == null) {
                        msg = "item cap: none";
                    } else {
                        var item = BuiltInRegistries.ITEM.getValue(Identifier.parse(StringArgumentType.getString(c, "what")));
                        int ins;
                        try (var tx = Transaction.openRoot()) {
                            ins = h.insert(ItemResource.of(item), 5, tx);
                            tx.commit();
                        }
                        int ext;
                        try (var tx = Transaction.openRoot()) {
                            ext = h.extract(ItemResource.of(item), 2, tx);
                            tx.commit();
                        }
                        int aborted;
                        try (var tx = Transaction.openRoot()) {
                            aborted = h.extract(ItemResource.of(item), 1, tx);
                        }
                        msg = "item cap: size " + h.size() + " inserted " + ins + " extracted " + ext + " aborted " + aborted;
                    }
                    log(c.getSource(), msg);
                    return 1;
                }))))
                .then(Commands.literal("fluid").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("what", StringArgumentType.string()).executes(c -> {
                    ServerLevel level = c.getSource().getLevel();
                    BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
                    var h = level.getCapability(Capabilities.Fluid.BLOCK, pos, Direction.UP);
                    String msg;
                    if (h == null) {
                        msg = "fluid cap: none";
                    } else {
                        var fluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(StringArgumentType.getString(c, "what")));
                        int ins, ins2, ext, aborted;
                        try (var tx = Transaction.openRoot()) {
                            ins = h.insert(FluidResource.of(fluid), 1000, tx);
                            ins2 = h.insert(FluidResource.of(fluid), 1000000, tx);
                            tx.commit();
                        }
                        long after = h.getAmountAsLong(0);
                        try (var tx = Transaction.openRoot()) {
                            aborted = h.extract(FluidResource.of(fluid), 500, tx);
                        }
                        try (var tx = Transaction.openRoot()) {
                            ext = h.extract(FluidResource.of(fluid), 144, tx);
                            tx.commit();
                        }
                        msg = "fluid cap: size " + h.size() + " inserted " + ins + "+" + ins2 + " amount " + after + " aborted " + aborted
                                + " extracted " + ext + " now " + h.getAmountAsLong(0) + "/" + h.getCapacityAsLong(0, FluidResource.of(fluid));
                    }
                    log(c.getSource(), msg);
                    return 1;
                }))))
                .then(Commands.literal("melt").then(Commands.argument("what", StringArgumentType.string()).executes(c -> {
                    var item = BuiltInRegistries.ITEM.getValue(Identifier.parse(StringArgumentType.getString(c, "what")));
                    var r = tconstruct.smeltery.SmelteryRecipes.result(new ItemStack(item));
                    log(c.getSource(), "melt " + item + " -> " + (r.isEmpty() ? "nothing" : r.getAmount() + " " + BuiltInRegistries.FLUID.getKey(r.getFluid())));
                    return 1;
                })))
                .then(Commands.literal("tag").then(Commands.argument("what", StringArgumentType.string()).then(Commands.argument("tag", StringArgumentType.string()).executes(c -> {
                    var item = BuiltInRegistries.ITEM.getValue(Identifier.parse(StringArgumentType.getString(c, "what")));
                    var tag = TagKey.create(Registries.ITEM, Identifier.parse(StringArgumentType.getString(c, "tag")));
                    log(c.getSource(), "tag " + item + " in " + tag.location() + ": " + new ItemStack(item).is(tag));
                    return 1;
                }))))
                .then(Commands.literal("recipe").then(Commands.argument("id", StringArgumentType.string()).executes(c -> {
                    var key = ResourceKey.create(Registries.RECIPE, Identifier.parse(StringArgumentType.getString(c, "id")));
                    var r = c.getSource().getServer().getRecipeManager().byKey(key);
                    log(c.getSource(), "recipe " + key.identifier() + ": " + r.map(h -> h.value().toString()).orElse("absent"));
                    return 1;
                })))
                .then(Commands.literal("mine").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("tool", StringArgumentType.string()).executes(c -> {
                    ServerLevel level = c.getSource().getLevel();
                    BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
                    var state = level.getBlockState(pos);
                    var tool = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(StringArgumentType.getString(c, "tool"))));
                    var drops = net.minecraft.world.level.block.Block.getDrops(state, level, pos, level.getBlockEntity(pos), null, tool);
                    boolean harvest = !state.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(state);
                    log(c.getSource(), "mine " + BuiltInRegistries.BLOCK.getKey(state.getBlock()) + " with " + BuiltInRegistries.ITEM.getKey(tool.getItem())
                            + ": correct tool " + harvest + " speed " + tool.getDestroySpeed(state)
                            + " drops " + (harvest ? drops.stream().map(s -> s.getCount() + " " + BuiltInRegistries.ITEM.getKey(s.getItem())).toList() : "[]"));
                    return 1;
                }))))));
    }

    private static void log(net.minecraft.commands.CommandSourceStack src, String msg) {
        LoggerFactory.getLogger("hexxitcompat").info(msg);
        src.sendSuccess(() -> Component.literal(msg), false);
    }
}
