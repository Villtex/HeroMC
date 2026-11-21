package tconstruct.client.manual;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import tconstruct.TItems;
import tconstruct.TPlayer;
import tconstruct.tools.PartType;
import tconstruct.tools.TMaterial;
import tconstruct.tools.ToolBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * TConstructClientRegistry manual data of TProxyClient (initManualIcons, initManualRecipes): the icons by name and the
 * recipe pictures (index 0 = output, then the grid or the inputs). Items of later parts (glass, brownstone, punji
 * sticks, barricade, drawbridge, igniter: part 25f) are looked up by name and stay empty until they exist.
 */
public final class ManualIcons {
    private static final Map<String, ItemStack> ICONS = new HashMap<>();
    private static final Map<String, ItemStack[]> RECIPES = new HashMap<>();
    private static boolean done;

    private ManualIcons() {
    }

    public static ItemStack icon(String name) {
        init();
        return ICONS.getOrDefault(name, ItemStack.EMPTY);
    }

    public static ItemStack @Nullable [] recipe(String name) {
        init();
        return RECIPES.get(name);
    }

    private static ItemStack s(Item item) {
        return new ItemStack(item);
    }

    /** An item by name (empty if not registered, e.g. blocks of part 25f). */
    private static ItemStack s(String name) {
        Identifier id = Identifier.parse(name.contains(":") ? name : "tconstruct:" + name);
        return BuiltInRegistries.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    private static ItemStack s(String name, int count) {
        ItemStack stack = s(name);
        stack.setCount(count);
        return stack;
    }

    /** materials, damage n. */
    private static ItemStack mat(int damage) {
        String name = TItems.MATERIAL_NAMES[damage];
        return name == null ? s(Items.IRON_NUGGET) : s(name);
    }

    private static ItemStack part(PartType type, int material) {
        TMaterial m = TMaterial.byId(material);
        var holder = m == null ? null : TItems.partHolder(type, m);
        return holder == null ? ItemStack.EMPTY : new ItemStack(holder.get());
    }

    private static ItemStack tool(ItemStack head, ItemStack handle, ItemStack accessory, ItemStack extra) {
        ItemStack tool = ToolBuilder.buildTool(head, handle, accessory, extra, null, null);
        return tool == null ? ItemStack.EMPTY : tool;
    }

    private static void icon(String name, ItemStack stack) {
        ICONS.put(name, stack);
    }

    private static void small(String name, ItemStack out, ItemStack a, ItemStack b, ItemStack c, ItemStack d) {
        RECIPES.put(name, new ItemStack[]{out, a, b, c, d});
    }

    private static void large(String name, ItemStack out, ItemStack... grid) {
        ItemStack[] r = new ItemStack[10];
        r[0] = out;
        System.arraycopy(grid, 0, r, 1, 9);
        RECIPES.put(name, r);
    }

    private static void furnace(String name, ItemStack out, ItemStack in) {
        RECIPES.put(name, new ItemStack[]{out, in});
    }

    private static void modifier(String name, ItemStack tool, ItemStack top, ItemStack bottom) {
        ItemStack result = ToolBuilder.buildTool(tool.copy(), top, bottom, ItemStack.EMPTY, null, null);
        RECIPES.put(name, new ItemStack[]{result == null ? tool.copy() : result, top, bottom});
    }

    private static void smeltery(String name, ItemStack out, ItemStack liquid, ItemStack cast) {
        RECIPES.put(name, new ItemStack[]{out, liquid, cast});
    }

    private static void init() {
        if (done) {
            return;
        }
        done = true;
        ItemStack e = ItemStack.EMPTY;
        icon("smelterybook", s(TPlayer.MANUALS.get(2).get()));
        icon("smeltery", s("smeltery_controller"));
        icon("blankcast", s(TItems.BLANK_CAST.get()));
        icon("castingtable", s("casting_table"));
        icon("liquidiron", s("molten_iron_bucket"));
        icon("lavatank", s("seared_tank"));
        icon("searedbrick", s("seared_bricks"));
        icon("drain", s("smeltery_drain"));
        icon("faucet", s("seared_faucet"));
        icon("bronzeingot", mat(13));
        icon("alubrassingot", mat(14));
        icon("manyullyningot", mat(5));
        icon("alumiteingot", mat(15));
        icon("blankpattern", s(TItems.BLANK_PATTERN.get()));
        icon("toolstation", s("tool_station"));
        icon("partcrafter", s("oak_part_builder"));
        icon("patternchest", s("pattern_chest"));
        icon("stenciltable", s("oak_stencil_table"));
        icon("torch", s(Items.TORCH));
        icon("sapling", s(Items.OAK_SAPLING));
        icon("workbench", s(Items.CRAFTING_TABLE));
        icon("coal", s(Items.COAL));
        icon("obsidianingot", mat(18));
        icon("lavacrystal", mat(7));
        icon("woodplanks", s(Items.OAK_PLANKS));
        icon("stoneblock", s(Items.STONE));
        icon("ironingot", s(Items.IRON_INGOT));
        icon("flint", s(Items.FLINT));
        icon("cactus", s(Items.CACTUS));
        icon("bone", s(Items.BONE));
        icon("obsidian", s(Items.OBSIDIAN));
        icon("netherrack", s(Items.NETHERRACK));
        icon("blueslimecrystal", mat(17));
        icon("slimecrystal", mat(1));
        icon("paperstack", mat(0));
        icon("cobaltingot", mat(3));
        icon("arditeingot", mat(4));
        icon("copperingot", mat(9));
        icon("steelingot", mat(16));
        icon("pickhead", part(PartType.PICKAXE_HEAD, 2));
        icon("shovelhead", part(PartType.SHOVEL_HEAD, 2));
        icon("axehead", part(PartType.AXE_HEAD, 2));
        icon("swordblade", part(PartType.SWORD_BLADE, 2));
        icon("pan", part(PartType.PAN, 2));
        icon("board", part(PartType.SIGN_HEAD, 2));
        icon("knifeblade", part(PartType.KNIFE_BLADE, 2));
        icon("chiselhead", part(PartType.CHISEL_HEAD, 2));
        icon("hammerhead", part(PartType.HAMMER_HEAD, 2));
        icon("excavatorhead", part(PartType.EXCAVATOR_HEAD, 2));
        icon("scythehead", part(PartType.SCYTHE_BLADE, 2));
        icon("broadaxehead", part(PartType.BROAD_AXE_HEAD, 2));
        icon("largeswordblade", part(PartType.LARGE_SWORD_BLADE, 2));
        icon("toolrod", s(Items.STICK));
        icon("binding", part(PartType.BINDING, 4));
        icon("wideguard", part(PartType.WIDE_GUARD, 4));
        icon("handguard", part(PartType.HAND_GUARD, 4));
        icon("crossbar", part(PartType.CROSSBAR, 4));
        icon("toughrod", part(PartType.TOUGH_ROD, 0));
        icon("toughbinding", part(PartType.TOUGH_BINDING, 17));
        icon("largeplate", part(PartType.LARGE_PLATE, 17));
        icon("bowstring", s(TItems.BOWSTRING.get()));
        icon("arrowhead", part(PartType.ARROWHEAD, 2));
        icon("fletching", s(TItems.FLETCHINGS.get(0).get()));
        ItemStack rod = part(PartType.TOOL_ROD, 11);
        icon("pickicon", tool(part(PartType.PICKAXE_HEAD, 10), rod, part(PartType.BINDING, 12), e));
        icon("shovelicon", tool(part(PartType.SHOVEL_HEAD, 10), rod, e, e));
        icon("axeicon", tool(part(PartType.AXE_HEAD, 10), rod, e, e));
        icon("mattockicon", tool(part(PartType.AXE_HEAD, 10), rod, part(PartType.SHOVEL_HEAD, 12), e));
        icon("swordicon", tool(part(PartType.SWORD_BLADE, 10), rod, part(PartType.WIDE_GUARD, 12), e));
        icon("longswordicon", tool(part(PartType.SWORD_BLADE, 10), rod, part(PartType.HAND_GUARD, 12), e));
        icon("rapiericon", tool(part(PartType.SWORD_BLADE, 10), rod, part(PartType.CROSSBAR, 12), e));
        icon("daggerIcon", tool(part(PartType.KNIFE_BLADE, 10), rod, part(PartType.CROSSBAR, 12), e));
        icon("frypanicon", tool(part(PartType.PAN, 10), rod, e, e));
        icon("battlesignicon", tool(part(PartType.SIGN_HEAD, 10), rod, e, e));
        icon("chiselicon", tool(part(PartType.CHISEL_HEAD, 10), rod, e, e));
        icon("shortbowIcon", tool(part(PartType.TOOL_ROD, 10), s(TItems.BOWSTRING.get()), part(PartType.TOOL_ROD, 12), e));
        icon("arrowIcon", tool(part(PartType.ARROWHEAD, 10), rod, s(TItems.FLETCHINGS.get(0).get()), e));
        ItemStack tough = part(PartType.TOUGH_ROD, 11);
        // the extra slot of 1.5.2 was "new ItemStack(part, 8)": item damage 0 = wood
        icon("hammericon", tool(part(PartType.HAMMER_HEAD, 10), tough, part(PartType.LARGE_PLATE, 12), part(PartType.LARGE_PLATE, 0)));
        icon("lumbericon", tool(part(PartType.BROAD_AXE_HEAD, 10), tough, part(PartType.LARGE_PLATE, 12), part(PartType.TOUGH_BINDING, 0)));
        icon("excavatoricon", tool(part(PartType.EXCAVATOR_HEAD, 10), tough, part(PartType.LARGE_PLATE, 12), part(PartType.TOUGH_BINDING, 0)));
        icon("scytheicon", tool(part(PartType.SCYTHE_BLADE, 10), tough, part(PartType.TOUGH_BINDING, 12), part(PartType.TOUGH_ROD, 0)));
        icon("cleavericon", tool(part(PartType.LARGE_SWORD_BLADE, 10), tough, part(PartType.LARGE_PLATE, 12), part(PartType.TOUGH_ROD, 0)));
        icon("battleaxeicon", tool(part(PartType.BROAD_AXE_HEAD, 10), tough, part(PartType.BROAD_AXE_HEAD, 12), part(PartType.TOUGH_BINDING, 0)));

        // initManualRecipes
        ItemStack pattern = s(TItems.BLANK_PATTERN.get());
        ItemStack stick = s(Items.STICK);
        ItemStack plank = s(Items.OAK_PLANKS);
        ItemStack workbench = s(Items.CRAFTING_TABLE);
        ItemStack chest = s(Items.CHEST);
        ItemStack log = s(Items.OAK_LOG);
        ItemStack mossy = s(Items.MOSSY_COBBLESTONE);
        ItemStack dirt = s(Items.DIRT);
        ItemStack sand = s(Items.SAND);
        ItemStack gravel = s(Items.GRAVEL);
        ItemStack clay = s(Items.CLAY_BALL);
        ItemStack glass = s(Items.GLASS);
        ItemStack ironBlock = s(Items.IRON_BLOCK);
        ItemStack grout = s("grout", 2);
        ItemStack searedBrick = mat(2);
        ItemStack searedBricks = s("seared_bricks");
        ItemStack paper = s(Items.PAPER);
        ItemStack slimeball = s(Items.SLIME_BALL);
        ItemStack slimyMud = s("slimy_mud");
        ItemStack blazeRod = s(Items.BLAZE_ROD);
        ItemStack fireCharge = s(Items.FIRE_CHARGE);
        ItemStack string = s(Items.STRING);
        ItemStack silkyCloth = mat(25);
        ItemStack graveyard = s("graveyard_soil");
        ItemStack consecrated = s("consecrated_soil");
        small("patternbook1", s(TPlayer.MANUALS.get(0).get()), paper, pattern, e, e);
        small("patternbook2", s(TPlayer.MANUALS.get(1).get()), s(TPlayer.MANUALS.get(0).get()), e, e, e);
        small("patternbook3", s(TPlayer.MANUALS.get(2).get()), s(TPlayer.MANUALS.get(1).get()), e, e, e);
        small("blankpattern", pattern, plank, stick, stick, plank);
        small("toolstation", s("tool_station"), e, pattern, e, workbench);
        small("partcrafter", s("oak_part_builder"), e, pattern, e, log);
        small("patternchest", s("pattern_chest"), e, pattern, e, chest);
        small("stenciltable", s("oak_stencil_table"), e, pattern, e, plank);
        large("toolforge", s("iron_tool_forge"), searedBricks, searedBricks, searedBricks, ironBlock, s("tool_station"), ironBlock, ironBlock, e,
                ironBlock);
        large("slimymud", slimyMud, e, slimeball, slimeball, e, slimeball, slimeball, e, dirt, sand);
        furnace("slimecrystal", mat(1), slimyMud);
        small("paperstack", mat(0), paper, paper, paper, paper);
        large("mossball", mat(6), mossy, mossy, mossy, mossy, mossy, mossy, mossy, mossy, mossy);
        large("lavacrystal", mat(7), blazeRod, fireCharge, blazeRod, fireCharge, s(Items.LAVA_BUCKET), fireCharge, blazeRod, fireCharge, blazeRod);
        large("silkycloth", silkyCloth, string, string, string, string, mat(24), string, string, string, string);
        large("silkyjewel", mat(26), e, silkyCloth, e, silkyCloth, s(Items.EMERALD), silkyCloth, e, silkyCloth, e);
        small("graveyardsoil", graveyard, s(Items.DIRT), s(Items.ROTTEN_FLESH), s(Items.BONE_MEAL), e);
        furnace("consecratedsoil", consecrated, graveyard);
        small("grout", grout, sand, gravel, e, clay);
        furnace("searedbrick", searedBrick, grout);
        small("searedbricks", searedBricks, searedBrick, searedBrick, searedBrick, searedBrick);
        large("smelterycontroller", s("smeltery_controller"), searedBrick, searedBrick, searedBrick, searedBrick, e, searedBrick, searedBrick,
                searedBrick, searedBrick);
        large("smelterydrain", s("smeltery_drain"), searedBrick, e, searedBrick, searedBrick, e, searedBrick, searedBrick, e, searedBrick);
        large("smelterytank1", s("seared_tank"), searedBrick, searedBrick, searedBrick, searedBrick, glass, searedBrick, searedBrick, searedBrick,
                searedBrick);
        large("smelterytank2", s("seared_glass"), searedBrick, glass, searedBrick, glass, glass, glass, searedBrick, glass, searedBrick);
        large("smelterytank3", s("seared_window"), searedBrick, glass, searedBrick, searedBrick, glass, searedBrick, searedBrick, glass, searedBrick);
        large("smelterytable", s("casting_table"), searedBrick, searedBrick, searedBrick, searedBrick, e, searedBrick, searedBrick, e, searedBrick);
        large("smelteryfaucet", s("seared_faucet"), searedBrick, e, searedBrick, e, searedBrick, e, e, e, e);
        large("smelterybasin", s("casting_basin"), searedBrick, e, searedBrick, searedBrick, e, searedBrick, searedBrick, searedBrick, searedBrick);
        ItemStack reed = s(Items.SUGAR_CANE);
        large("punji", s("punji_sticks"), reed, e, reed, e, reed, e, reed, e, reed);
        small("barricade", s("oak_barricade"), e, log, e, log);
        ItemStack alubrass = mat(14);
        ItemStack bronze = mat(13);
        ItemStack blankCast = s(TItems.BLANK_CAST.get());
        ItemStack redstone = s(Items.REDSTONE);
        large("drawbridge", s("drawbridge"), alubrass, blankCast, alubrass, bronze, s(Items.DISPENSER), bronze, bronze, redstone, bronze);
        large("igniter", s("igniter"), alubrass, part(PartType.LARGE_PLATE, 7), alubrass, bronze, s(Items.FLINT_AND_STEEL), bronze, bronze, redstone,
                bronze);
        ItemStack ironPick = tool(part(PartType.PICKAXE_HEAD, 6), part(PartType.TOOL_ROD, 2), part(PartType.BINDING, 6), e);
        icon("ironpick", ironPick);
        ItemStack ironLongsword = tool(part(PartType.SWORD_BLADE, 6), part(PartType.TOOL_ROD, 2), part(PartType.HAND_GUARD, 10), e);
        icon("ironlongsword", ironLongsword);
        modifier("diamondmod", ironPick, s(Items.DIAMOND), e);
        modifier("emeraldmod", ironPick, s(Items.EMERALD), e);
        modifier("redstonemod", ironPick, s(Items.REDSTONE), s(Items.REDSTONE_BLOCK));
        modifier("lavacrystalmod", ironPick, mat(7), e);
        modifier("lapismod", ironPick, s(Items.LAPIS_LAZULI), s(Items.LAPIS_BLOCK));
        modifier("mossmod", ironPick, mat(6), e);
        modifier("quartzmod", ironLongsword, s(Items.QUARTZ), s(Items.QUARTZ_BLOCK));
        modifier("blazemod", ironLongsword, s(Items.BLAZE_POWDER), e);
        modifier("necroticmod", ironLongsword, mat(8), e);
        modifier("silkymod", ironPick, mat(26), e);
        modifier("reinforcedmod", ironPick, part(PartType.LARGE_PLATE, 6), e);
        modifier("pistonmod", ironLongsword, s(Items.PISTON), e);
        modifier("beheadingmod", ironLongsword, s(Items.ENDER_PEARL), s(Items.OBSIDIAN));
        modifier("spidermod", ironLongsword, s(Items.FERMENTED_SPIDER_EYE), e);
        modifier("smitemod", ironLongsword, consecrated, e);
        modifier("electricmod", ironPick, dirt, dirt);
        modifier("tier1free", ironPick, s(Items.DIAMOND), s(Items.GOLD_BLOCK));
        modifier("tier2free", ironPick, s(Items.NETHER_STAR), e);
        smeltery("brownstone", s("rough_brownstone"), s("molten_tin_bucket"), gravel);
        smeltery("clearglass", s("clear_glass"), s("molten_glass_bucket"), e);
        smeltery("searedstone", s("seared_stone"), s("liquid_seared_stone_bucket"), e);
        smeltery("endstone", s(Items.END_STONE), s("liquid_ender_bucket"), s(Items.OBSIDIAN));
    }
}
