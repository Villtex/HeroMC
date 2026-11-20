package tconstruct.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import tconstruct.TItems;
import tconstruct.items.CustomMaterials;
import tconstruct.items.IToolPart;
import tconstruct.modifiers.Modifiers;
import tconstruct.modifiers.ToolMod;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code library.crafting.ToolBuilder} with the ToolCraftEvent of TEventHandler.craftTool (bow and arrow stats):
 * builds a tool from its parts and applies modifiers to a tool.
 */
public final class ToolBuilder {
    private ToolBuilder() {
    }

    /** getMaterialID: a stick is wood (0), a bone is bone (5), a part its material; -1 for anything else. */
    public static int materialId(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }
        if (stack.is(Items.STICK)) {
            return 0;
        }
        if (stack.is(Items.BONE)) {
            return 5;
        }
        return stack.getItem() instanceof IToolPart part ? part.materialId() : -1;
    }

    /** ToolRecipe.validHead / validHandle / ...: the part type, or a stick or bone for a tool rod. */
    private static boolean valid(@Nullable PartType wanted, ItemStack stack, boolean stickIsRod) {
        if (stack.isEmpty()) {
            return wanted == null;
        }
        if (wanted == null) {
            return false;
        }
        if (stack.getItem() instanceof IToolPart part && part.partType() == wanted) {
            return true;
        }
        return stickIsRod && wanted == PartType.TOOL_ROD && (stack.is(Items.STICK) || stack.is(Items.BONE));
    }

    /** getMatchingRecipe: the first tool whose parts fit. The bow also takes a stick as its top limb (BowRecipe). */
    public static @Nullable ToolType match(ItemStack head, ItemStack handle, ItemStack accessory, ItemStack extra) {
        for (ToolType type : ToolType.values()) {
            if (valid(type.head, head, type == ToolType.SHORTBOW) && valid(type.handle, handle, true) && valid(type.accessory, accessory, true)
                    && valid(type.extra, extra, true)) {
                return type;
            }
        }
        return null;
    }

    /**
     * buildTool: a tool from head, handle, accessory and extra (empty stacks for missing slots), or a modified tool when
     * the "head" is a tool (then {@code registries} looks up the enchantments of luck and silky). Null if nothing fits.
     */
    public static @Nullable ItemStack buildTool(ItemStack headStack, ItemStack handleStack, ItemStack accessoryStack, ItemStack extraStack,
            @Nullable String name, HolderLookup.@Nullable Provider registries) {
        if (headStack.getItem() instanceof ToolCoreItem) {
            return modifyTool(headStack, handleStack, accessoryStack, extraStack, registries);
        }
        if (headStack.isEmpty() || handleStack.isEmpty()) {
            return null;
        }
        int head = materialId(headStack);
        int handle = materialId(handleStack);
        if (head == -1 || handle == -1) {
            return null;
        }
        int accessory = -1;
        int extra = -1;
        ToolType type;
        if (accessoryStack.isEmpty()) {
            type = match(headStack, handleStack, ItemStack.EMPTY, ItemStack.EMPTY);
        } else {
            accessory = materialId(accessoryStack);
            if (accessory == -1) {
                return null;
            }
            if (!extraStack.isEmpty()) {
                extra = materialId(extraStack);
                if (extra == -1) {
                    return null;
                }
                type = match(headStack, handleStack, accessoryStack, extraStack);
            } else {
                type = match(headStack, handleStack, accessoryStack, ItemStack.EMPTY);
            }
        }
        if (type == null) {
            return null;
        }
        return build(type, head, handle, accessory, extra, name);
    }

    /** The NBT part of buildTool and TEventHandler.craftTool for a recipe that matched. */
    public static @Nullable ItemStack build(ToolType type, int head, int handle, int accessory, int extra, @Nullable String name) {
        TMaterial headMat = TMaterial.byId(head);
        TMaterial handleMat = TMaterial.byId(handle);
        TMaterial accessoryMat = accessory != -1 ? TMaterial.byId(accessory) : null;
        TMaterial extraMat = extra != -1 ? TMaterial.byId(extra) : null;
        if (headMat == null || handleMat == null || accessory != -1 && accessoryMat == null || extra != -1 && extraMat == null) {
            return null;
        }
        int durability = headMat.durability;
        int heads = 1;
        int handles = 0;
        float modifier = 1.0F;
        int attack = headMat.attack;
        if (type.durabilityTypeHandle() == 2) {
            heads++;
            durability += handleMat.durability;
            attack += handleMat.attack;
        } else if (type.durabilityTypeHandle() == 1) {
            handles++;
            modifier = handleMat.handle;
        }
        if (accessoryMat != null) {
            if (type.durabilityTypeAccessory == 2) {
                heads++;
                durability += accessoryMat.durability;
                attack += accessoryMat.attack;
            } else if (type.durabilityTypeAccessory == 1) {
                handles++;
                modifier += accessoryMat.handle;
            }
        }
        if (extraMat != null) {
            if (type.durabilityTypeExtra == 2) {
                heads++;
                durability += extraMat.durability;
                attack += extraMat.attack;
            } else if (type.durabilityTypeExtra == 1) {
                handles++;
                modifier += extraMat.handle;
            }
        }
        if (handles > 0) {
            modifier = (float) (modifier * (0.5 + handles * 0.5));
            modifier /= handles;
        }
        durability = (int) (durability / heads * (0.5 + heads * 0.5) * modifier * type.durabilityModifier);
        attack = attack / heads + type.baseDamage;
        if (attack % heads != 0) {
            attack++;
        }

        InfiTool t = new InfiTool();
        t.head = head;
        t.handle = handle;
        t.accessory = accessory;
        t.extra = extra;
        t.damage = 0;
        t.totalDurability = durability;
        t.baseDurability = durability;
        t.bonusDurability = 0;
        t.modDurability = 0.0F;
        t.broken = false;
        t.attack = attack;
        t.miningSpeed = headMat.miningSpeed;
        t.harvestLevel = headMat.harvestLevel;
        if (type.durabilityTypeHandle() == 2) {
            t.miningSpeedHandle = handleMat.miningSpeed;
            t.harvestLevelHandle = handleMat.harvestLevel;
        }
        if (accessoryMat != null && type.durabilityTypeAccessory == 2) {
            t.miningSpeed2 = accessoryMat.miningSpeed;
            t.harvestLevel2 = accessoryMat.harvestLevel;
        }
        if (extraMat != null && type.durabilityTypeExtra == 2) {
            t.miningSpeedExtra = extraMat.miningSpeed;
            t.harvestLevelExtra = extraMat.harvestLevel;
        }
        t.unbreaking = reinforced(headMat, handleMat, accessoryMat, extraMat);
        t.shoddy = shoddy(headMat, handleMat, accessoryMat, extraMat);
        int modifiers = type.modifierAmount();
        if (accessory == -1) {
            modifiers += head == TMaterial.PAPER.id ? 2 : 0;
        } else {
            modifiers += (head == TMaterial.PAPER.id ? 1 : 0) + (accessory == TMaterial.PAPER.id ? 1 : 0);
        }
        modifiers += handle == TMaterial.PAPER.id ? 1 : 0;
        modifiers += extra == TMaterial.PAPER.id ? 1 : 0;
        t.modifiers = modifiers;

        ItemStack tool = new ItemStack(TItems.tool(type));
        // TEventHandler.craftTool
        if (type == ToolType.SHORTBOW) {
            CustomMaterials.Bowstring string = CustomMaterials.bowstring(handle);
            TMaterial bottom = TMaterial.byId(accessory);
            if (string != null && bottom != null) {
                if (handle == 1) {
                    t.modifiers++;
                }
                int bowDurability = (int) ((headMat.bowDurability + bottom.bowDurability) / 2 * string.durability());
                t.totalDurability = bowDurability;
                t.baseDurability = bowDurability;
                int drawSpeed = (int) ((headMat.bowDrawSpeed + bottom.bowDrawSpeed) / 2 * string.drawSpeed());
                t.setInt("DrawSpeed", drawSpeed);
                t.setInt("BaseDrawSpeed", drawSpeed);
                t.setFloat("FlightSpeed", (headMat.bowFlightSpeed + bottom.bowFlightSpeed) / 2.0F * string.flightSpeed());
            }
        }
        if (type == ToolType.ARROW) {
            CustomMaterials.Fletching fletching = CustomMaterials.fletching(accessory);
            if (fletching != null) {
                t.totalDurability = 0;
                t.setFloat("Mass", headMat.arrowMass / 5.0F + handleMat.arrowMass + fletching.mass());
                t.setFloat("BreakChance", (headMat.arrowBreakChance + handleMat.arrowBreakChance + fletching.breakChance()) / 4.0F);
                t.setFloat("Accuracy", (headMat.arrowAccuracy + handleMat.arrowAccuracy + fletching.accuracy()) / 3.0F);
                t.unbreaking = 10;
                tool.setCount(4);
            }
        }
        t.save(tool);
        if (name != null && !name.isEmpty()) {
            tool.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(s -> s.withItalic(false).withColor(ChatFormatting.WHITE)));
        }
        return tool;
    }

    /**
     * modifyTool: the tool with the modifiers whose items lie in the two slots (an extra item in the forge's fourth slot
     * forbids it). Nothing in both slots gives the tool back unchanged; nothing matching gives null.
     */
    public static @Nullable ItemStack modifyTool(ItemStack input, ItemStack topSlot, ItemStack bottomSlot, ItemStack extraStack,
            HolderLookup.@Nullable Provider registries) {
        if (!extraStack.isEmpty() || !(input.getItem() instanceof ToolCoreItem item)) {
            return null;
        }
        ItemStack tool = input.copy();
        if (topSlot.isEmpty() && bottomSlot.isEmpty()) {
            return tool;
        }
        InfiTool t = InfiTool.of(tool);
        if (t == null) {
            return null;
        }
        boolean built = false;
        ItemStack[] slots = {topSlot, bottomSlot};
        for (ToolMod mod : Modifiers.all()) {
            if (mod.matches(slots, t, item.type)) {
                built = true;
                mod.addMatchingEffect(t);
                mod.modify(slots, tool, t, item.type, registries);
            }
        }
        if (!built) {
            return null;
        }
        t.save(tool);
        return tool;
    }

    static int reinforced(TMaterial head, TMaterial handle, @Nullable TMaterial accessory, @Nullable TMaterial extra) {
        int reinforced = Math.max(0, Math.max(head.reinforced, handle.reinforced));
        if (accessory != null) {
            reinforced = Math.max(reinforced, accessory.reinforced);
        }
        if (extra != null) {
            reinforced = Math.max(reinforced, extra.reinforced);
        }
        return reinforced;
    }

    static float shoddy(TMaterial head, TMaterial handle, @Nullable TMaterial accessory, @Nullable TMaterial extra) {
        if (extra != null) {
            return (head.stonebound + handle.stonebound + (accessory == null ? 0.0F : accessory.stonebound) + extra.stonebound) / 4.0F;
        }
        if (accessory != null) {
            return (head.stonebound + handle.stonebound + accessory.stonebound) / 3.0F;
        }
        return (head.stonebound + handle.stonebound) / 2.0F;
    }

    // ------------------------------------------------------------------ creative tools (ToolCore.getSubItems)

    /** The part item of a material for a slot of a tool (the bowstring is string, the fletching feathers). */
    private static ItemStack partStack(@Nullable PartType type, TMaterial material) {
        if (type == null) {
            return ItemStack.EMPTY;
        }
        if (type == PartType.BOWSTRING) {
            return new ItemStack(TItems.BOWSTRING.get());
        }
        if (type == PartType.FLETCHING) {
            return new ItemStack(TItems.FLETCHINGS.get(0).get());
        }
        return new ItemStack(TItems.part(type, material));
    }

    /** A tool of the given materials, named like the creative tools of 1.5.2. */
    public static ItemStack creativeTool(ToolType type, TMaterial head, TMaterial handle, TMaterial accessory) {
        ItemStack tool = buildTool(partStack(type.head, head), partStack(type.handle, handle), partStack(type.accessory, accessory),
                partStack(type.extra, accessory), null, null);
        return tool == null ? new ItemStack(TItems.tool(type)) : tool;
    }

    /**
     * ToolCore.getSubItems: one tool of every material (all parts of it), named "Iron Pickaxe"; the arrow also as the
     * "vanilla style" arrow (flint head, wooden shaft, feathers).
     */
    public static List<ItemStack> creativeTools(ToolType type) {
        List<ItemStack> list = new ArrayList<>();
        if (type == ToolType.ARROW) {
            ItemStack vanilla = buildTool(partStack(PartType.ARROWHEAD, TMaterial.FLINT), partStack(PartType.TOOL_ROD, TMaterial.WOOD),
                    partStack(PartType.FLETCHING, TMaterial.WOOD), ItemStack.EMPTY, null, null);
            if (vanilla != null) {
                vanilla.setCount(1);
                list.add(vanilla);
            }
        }
        for (TMaterial material : TMaterial.values()) {
            // BowBase / Arrow.buildTool: the string and the feathers, the shaft of the arrow of the material
            ItemStack tool = buildTool(partStack(type.head, material), partStack(type.handle, material), partStack(type.accessory, material),
                    partStack(type.extra, material), null, null);
            if (tool != null) {
                tool.setCount(1);
                tool.set(DataComponents.CUSTOM_NAME, Component.translatable("tool.tconstruct.creative_name", Component.translatable(material.langKey()),
                        Component.translatable(type.langKey())).withStyle(s -> s.withItalic(false).withColor(ChatFormatting.WHITE)));
                list.add(tool);
            }
        }
        return list;
    }
}
