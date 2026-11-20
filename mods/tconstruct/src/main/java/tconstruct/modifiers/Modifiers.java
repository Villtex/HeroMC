package tconstruct.modifiers;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jspecify.annotations.Nullable;
import tconstruct.TItems;
import tconstruct.items.PatternBuilder;
import tconstruct.tools.InfiTool;
import tconstruct.tools.PartType;
import tconstruct.tools.TMaterial;
import tconstruct.tools.ToolType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The modifiers of {@code TContent.addCraftingRecipes} (ToolBuilder.registerToolMod), in that order. Values as in 1.5.2;
 * "Modifiers increase Mining Level" is on in the pack. The electric modifier needs IC2 (not in the pack) and is left out.
 */
public final class Modifiers {
    /** ModLapis max. */
    public static final int LAPIS_MAX = 450;
    private static final List<ToolMod> MODS = new ArrayList<>();

    private Modifiers() {
    }

    public static List<ToolMod> all() {
        if (MODS.isEmpty()) {
            register();
        }
        return MODS;
    }

    private static Supplier<ToolMod.Ingredient> item(net.minecraft.world.item.Item item) {
        return () -> new ToolMod.Ingredient(stack -> stack.is(item), () -> List.of(new ItemStack(item)));
    }

    private static Supplier<ToolMod.Ingredient> item(Supplier<? extends net.minecraft.world.item.Item> item) {
        return () -> ToolMod.Ingredient.of(item);
    }

    @SafeVarargs
    private static List<Supplier<ToolMod.Ingredient>> list(Supplier<ToolMod.Ingredient>... items) {
        return List.of(items);
    }

    private static void register() {
        Supplier<ToolMod.Ingredient> diamond = item(Items.DIAMOND);
        MODS.add(new Repair());
        MODS.add(new Durability(list(diamond), 0, 500, 0.0F, 3, "Diamond", "§bDurability +500", "§b"));
        MODS.add(new Durability(list(item(Items.EMERALD)), 1, 0, 0.5F, 2, "Emerald", "§2Durability +50%", "§2"));
        Supplier<ToolMod.Ingredient> redstone = item(Items.REDSTONE);
        Supplier<ToolMod.Ingredient> redstoneBlock = item(Items.REDSTONE_BLOCK);
        MODS.add(new Redstone(list(redstone), 2, 1));
        MODS.add(new Redstone(list(redstone, redstone), 2, 2));
        MODS.add(new Redstone(list(redstoneBlock), 2, 9));
        MODS.add(new Redstone(list(redstone, redstoneBlock), 2, 10));
        MODS.add(new Redstone(list(redstoneBlock, redstoneBlock), 2, 18));
        Supplier<ToolMod.Ingredient> lapis = item(Items.LAPIS_LAZULI);
        Supplier<ToolMod.Ingredient> lapisBlock = item(Items.LAPIS_BLOCK);
        MODS.add(new Lapis(list(lapis), 10, 1));
        MODS.add(new Lapis(list(lapis, lapis), 10, 2));
        MODS.add(new Lapis(list(lapisBlock), 10, 9));
        MODS.add(new Lapis(list(lapis, lapisBlock), 10, 10));
        MODS.add(new Lapis(list(lapisBlock, lapisBlock), 10, 18));
        MODS.add(new IntegerMod(list(item(TItems.MOSS_BALL)), 4, "Moss", 3, "§2", "Auto-Repair"));
        Supplier<ToolMod.Ingredient> blaze = item(Items.BLAZE_POWDER);
        MODS.add(new Blaze(list(blaze), 7, 1));
        MODS.add(new Blaze(list(blaze, blaze), 7, 2));
        MODS.add(new AutoSmelt(list(item(TItems.LAVA_CRYSTAL)), 6, "Lava", "§4", "Auto-Smelt"));
        MODS.add(new IntegerMod(list(item(TItems.NECROTIC_BONE)), 8, "Necrotic", 1, "§8", "Life Steal"));
        Supplier<ToolMod.Ingredient> quartz = item(Items.QUARTZ);
        // Block.blockNetherQuartz with damage 32767: the quartz block, chiseled quartz and the quartz pillar
        Supplier<ToolMod.Ingredient> quartzBlock = () -> new ToolMod.Ingredient(
                stack -> stack.is(Items.QUARTZ_BLOCK) || stack.is(Items.CHISELED_QUARTZ_BLOCK) || stack.is(Items.QUARTZ_PILLAR),
                () -> List.of(new ItemStack(Items.QUARTZ_BLOCK), new ItemStack(Items.CHISELED_QUARTZ_BLOCK), new ItemStack(Items.QUARTZ_PILLAR)));
        MODS.add(new Attack("Quartz", list(quartz), 11, 1));
        MODS.add(new Attack("Quartz", list(quartz, quartz), 11, 2));
        MODS.add(new Attack("Quartz", list(quartzBlock), 11, 4));
        MODS.add(new Attack("Quartz", list(quartz, quartzBlock), 11, 5));
        MODS.add(new Attack("Quartz", list(quartzBlock, quartzBlock), 11, 8));
        MODS.add(new ExtraModifier(list(diamond, item(Items.GOLD_BLOCK)), "Tier1Free"));
        MODS.add(new ExtraModifier(list(item(Items.NETHER_STAR)), "Tier2Free"));
        MODS.add(new Buttertouch(list(item(TItems.SILKY_JEWEL)), 12));
        Supplier<ToolMod.Ingredient> piston = item(Items.PISTON);
        MODS.add(new Piston(list(piston), 3, 1));
        MODS.add(new Piston(list(piston, piston), 3, 2));
        MODS.add(new IntegerMod(list(item(Items.OBSIDIAN), item(Items.ENDER_PEARL)), 13, "Beheading", 1, "§d", "Beheading"));
        // consecrated soil (craftedSoil 4) comes with the decorative soils of part 25f
        Supplier<ToolMod.Ingredient> holySoil = () -> TItems.CONSECRATED_SOIL == null ? new ToolMod.Ingredient(stack -> false, List::of)
                : ToolMod.Ingredient.of(TItems.CONSECRATED_SOIL);
        MODS.add(new Smite("Smite", list(holySoil), 14, 1));
        MODS.add(new Smite("Smite", list(holySoil, holySoil), 14, 2));
        Supplier<ToolMod.Ingredient> eye = item(Items.FERMENTED_SPIDER_EYE);
        MODS.add(new AntiSpider("Anti-Spider", list(eye), 15, 1));
        MODS.add(new AntiSpider("Anti-Spider", list(eye, eye), 15, 2));
        MODS.add(new Reinforced(list(item(() -> TItems.part(PartType.LARGE_PLATE, TMaterial.OBSIDIAN))), 16));
    }

    /** addEnchantment of ModLapis / ModButtertouch: sets the level of the enchantment (adds it if missing). */
    public static void enchant(ItemStack stack, HolderLookup.@Nullable Provider registries, ResourceKey<Enchantment> key, int level) {
        if (registries == null) {
            return;
        }
        Optional<Holder.Reference<Enchantment>> holder = registries.lookupOrThrow(Registries.ENCHANTMENT).get(key);
        holder.ifPresent(h -> EnchantmentHelper.updateEnchantments(stack, e -> e.set(h, level)));
    }

    // ------------------------------------------------------------------ ModRepair

    /** ModRepair: one piece of the head material (a shard, an ingot... worth its part value) repairs the tool. */
    public static final class Repair extends ToolMod {
        Repair() {
            super(List.of(), 0, "");
        }

        @Override
        public boolean matches(ItemStack[] input, InfiTool tool, ToolType type) {
            return this.canModify(tool, type, input);
        }

        @Override
        protected boolean canModify(InfiTool tool, ToolType type, ItemStack[] input) {
            if (input[0].isEmpty() == input[1].isEmpty()) {
                return false;
            }
            if (tool.damage <= 0) {
                return false;
            }
            ItemStack material = input[0].isEmpty() ? input[1] : input[0];
            return PatternBuilder.partId(material) == tool.head;
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            tool.broken = false;
            int damage = tool.damage;
            int dur = tool.baseDurability;
            int materialValue = PatternBuilder.partValue(input[0].isEmpty() ? input[1] : input[0]);
            int increase = (int) (50.0F + dur * 0.4F * materialValue);
            float mods = switch (tool.modifiers) {
                case 2 -> 0.8F;
                case 1 -> 0.6F;
                case 0 -> 0.4F;
                default -> 1.0F;
            };
            increase = (int) (increase * mods);
            int repair = ++tool.repairCount;
            float repairCount = (100 - repair) / 100.0F;
            if (repairCount < 0.5F) {
                repairCount = 0.5F;
            }
            increase = (int) (increase * repairCount);
            increase = (int) (increase / type.repairCost);
            damage -= increase;
            if (damage < 0) {
                damage = 0;
            }
            tool.damage = damage;
            AbilityHelperBridge.damage(tool, 0);
        }

        @Override
        public void addMatchingEffect(InfiTool tool) {
        }
    }

    /** AbilityHelper.damageTool(stack, 0, null, true) after a repair: clears the broken flag when the damage is 0. */
    static final class AbilityHelperBridge {
        static void damage(InfiTool tool, int dam) {
            int damageTrue = tool.damage + dam;
            if (damageTrue <= 0) {
                tool.damage = 0;
                tool.broken = false;
            } else if (damageTrue > tool.totalDurability) {
                tool.broken = true;
            } else {
                tool.damage = damageTrue;
            }
        }
    }

    // ------------------------------------------------------------------ ModDurability

    /** ModDurability: diamond (+500, mining level 3) and emerald (+50 %, mining level 2), once each. */
    public static final class Durability extends ToolMod {
        final String tooltipName;
        final String color;
        final int durability;
        final float modifier;
        final int miningLevel;

        Durability(List<Supplier<Ingredient>> items, int effect, int dur, float mod, int level, String key, String tip, String color) {
            super(items, effect, key);
            this.durability = dur;
            this.modifier = mod;
            this.miningLevel = level;
            this.tooltipName = tip;
            this.color = color;
        }

        public int durability() {
            return this.durability;
        }

        public float modifier() {
            return this.modifier;
        }

        @Override
        protected boolean canModify(InfiTool tool, ToolType type, ItemStack[] input) {
            return !tool.has(this.key) && super.canModify(tool, type, input);
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            int base = tool.baseDurability;
            int bonus = tool.bonusDurability + this.durability;
            float modDur = tool.modDurability + this.modifier;
            int total = (int) ((base + bonus) * (modDur + 1.0F));
            if (total <= 0) {
                total = 1;
            }
            tool.totalDurability = total;
            tool.bonusDurability = bonus;
            tool.modDurability = modDur;
            // PHConstruct.miningLevelIncrease = true
            if (tool.harvestLevel < this.miningLevel) {
                tool.harvestLevel = this.miningLevel;
            }
            tool.modifiers--;
            tool.setBool(this.key, true);
            this.addToolTip(tool, this.tooltipName, this.color + this.key);
        }
    }

    // ------------------------------------------------------------------ the "amount / max" modifiers

    /**
     * The common part of ModRedstone, ModBlaze, ModAttack, ModPiston, ModSmite and ModAntiSpider: the key holds
     * [amount, max, tooltip line]; a full step (amount == max) costs another modifier and raises max.
     */
    abstract static class Stepped extends ToolMod {
        final int increase;
        final int max;
        final String tooltipName;

        Stepped(List<Supplier<Ingredient>> items, int effect, String key, int increase, int max, String tooltipName) {
            super(items, effect, key);
            this.increase = increase;
            this.max = max;
            this.tooltipName = tooltipName;
        }

        @Override
        protected boolean canModify(InfiTool tool, ToolType type, ItemStack[] input) {
            if (!this.validType(type)) {
                return false;
            }
            if (!tool.has(this.key)) {
                return tool.modifiers > 0;
            }
            int[] keyPair = tool.getArray(this.key);
            if (keyPair[0] + this.increase <= keyPair[1]) {
                return true;
            }
            return keyPair[0] == keyPair[1] && tool.modifiers > 0;
        }

        /** The modName ("§4Redstone (1/50)"). */
        abstract String modName(int amount, int max);

        /** keyPair[0] % max == 0 (Redstone, Attack, Piston, Smite, Anti-Spider) or keyPair[0] == max (Blaze). */
        boolean fullStep(int[] keyPair) {
            return keyPair[0] % this.max == 0;
        }

        /** Adds the increase; returns the new [amount, max, tooltip]. */
        int[] step(InfiTool tool) {
            int[] keyPair;
            if (tool.has(this.key)) {
                keyPair = tool.getArray(this.key);
                if (this.fullStep(keyPair)) {
                    keyPair[0] += this.increase;
                    keyPair[1] += this.max;
                    tool.modifiers--;
                } else {
                    keyPair[0] += this.increase;
                }
                tool.setArray(this.key, keyPair);
                tool.setModifierTip(keyPair[2], this.modName(keyPair[0], keyPair[1]));
            } else {
                tool.modifiers--;
                int tooltipIndex = this.addToolTip(tool, this.tooltipName, this.modName(this.increase, this.max));
                keyPair = new int[]{this.increase, this.max, tooltipIndex};
                tool.setArray(this.key, keyPair);
            }
            return keyPair;
        }
    }

    /** ModRedstone: haste, +8 mining speed per redstone (and a faster bow draw). */
    public static final class Redstone extends Stepped {
        Redstone(List<Supplier<Ingredient>> items, int effect, int inc) {
            super(items, effect, "Redstone", inc, 50, "§4Haste");
        }

        @Override
        String modName(int amount, int max) {
            return "§4Redstone (" + amount + "/" + max + ")";
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            int[] keyPair = this.step(tool);
            tool.miningSpeed += this.increase * 8;
            if (tool.miningSpeed2 >= 0) {
                tool.miningSpeed2 += this.increase * 8;
            }
            if (tool.has("DrawSpeed")) {
                int baseDrawSpeed = tool.getInt("BaseDrawSpeed");
                tool.setInt("DrawSpeed", (int) (baseDrawSpeed - 0.1F * baseDrawSpeed * (keyPair[0] / 50.0F)));
            }
        }

        @Override
        public boolean validType(ToolType type) {
            return type.is("harvest") || type.is("utility") || type.is("bow");
        }
    }

    /** ModBlaze: fiery. */
    public static final class Blaze extends Stepped {
        Blaze(List<Supplier<Ingredient>> items, int effect, int inc) {
            super(items, effect, "Blaze", inc, 25, "§6Fiery");
        }

        @Override
        String modName(int amount, int max) {
            return "§6Blaze (" + amount + "/" + max + ")";
        }

        @Override
        boolean fullStep(int[] keyPair) {
            return keyPair[0] == this.max;
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            this.step(tool);
            tool.setInt("Fiery", tool.getInt("Fiery") + this.increase);
        }

        @Override
        public boolean validType(ToolType type) {
            return type.is("melee") || type.is("ammo");
        }
    }

    /** ModAttack: sharpness with quartz, +1 attack every 24 (36 for piercing / non-throwing weapons) quartz. */
    public static final class Attack extends Stepped {
        final String guiType;

        Attack(String type, List<Supplier<Ingredient>> items, int effect, int inc) {
            super(items, effect, "ModAttack", inc, 72, "§fSharpness");
            this.guiType = type;
        }

        @Override
        String modName(int amount, int max) {
            return "§f" + this.guiType + " (" + amount + "/" + max + ")";
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            if (tool.has(this.key)) {
                int amount = type.pierceArmor() || !nerfType(type) ? 36 : 24;
                int[] keyPair = tool.getArray(this.key);
                int leftToBoost = amount - keyPair[0] % amount;
                if (this.increase >= leftToBoost) {
                    tool.attack++;
                }
                this.step(tool);
            } else {
                this.step(tool);
                tool.attack++;
            }
        }

        static boolean nerfType(ToolType type) {
            return type.is("throwing") || type.is("ammo");
        }
    }

    /** ModPiston: knockback. */
    public static final class Piston extends Stepped {
        Piston(List<Supplier<Ingredient>> items, int effect, int inc) {
            super(items, effect, "Piston", inc, 10, "§7Knockback");
        }

        @Override
        String modName(int amount, int max) {
            // 1.5.2 named the first line "§4Knockback", every later update "§7Knockback"
            return "§7Knockback (" + amount + "/" + max + ")";
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            boolean first = !tool.has(this.key);
            int[] keyPair = this.step(tool);
            if (first) {
                tool.setModifierTip(keyPair[2], "§4Knockback (" + keyPair[0] + "/" + keyPair[1] + ")");
            }
            tool.setFloat("Knockback", (float) (tool.getFloat("Knockback") + 0.1 * this.increase));
        }

        @Override
        public boolean validType(ToolType type) {
            return type.is("weapon");
        }
    }

    /** ModSmite: with consecrated soil. */
    public static final class Smite extends Stepped {
        final String tagName;

        Smite(String type, List<Supplier<Ingredient>> items, int effect, int inc) {
            super(items, effect, "ModSmite", inc, 36, "§eSmite");
            this.tagName = type;
        }

        @Override
        String modName(int amount, int max) {
            return "§e" + this.tagName + " (" + amount + "/" + max + ")";
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            this.step(tool);
        }
    }

    /** ModAntiSpider: bane of arthropods with fermented spider eyes. */
    public static final class AntiSpider extends Stepped {
        final String guiType;

        AntiSpider(String type, List<Supplier<Ingredient>> items, int effect, int inc) {
            super(items, effect, "ModAntiSpider", inc, 4, "§2Bane of Arthropods");
            this.guiType = type;
        }

        @Override
        String modName(int amount, int max) {
            return "§2" + this.guiType + " (" + amount + "/" + max + ")";
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            this.step(tool);
        }
    }

    // ------------------------------------------------------------------ ModLapis

    /** ModLapis: luck, fortune (tools) or looting (weapons) at 100 / 300 / 450 lapis. */
    public static final class Lapis extends ToolMod {
        final int increase;
        static final int MAX = 450;

        Lapis(List<Supplier<Ingredient>> items, int effect, int inc) {
            super(items, effect, "Lapis");
            this.increase = inc;
        }

        @Override
        protected boolean canModify(InfiTool tool, ToolType type, ItemStack[] input) {
            if (!this.validType(type) || tool.getBool("Silk Touch")) {
                return false;
            }
            if (!tool.has(this.key)) {
                return tool.modifiers > 0;
            }
            return tool.getArray(this.key)[0] + this.increase <= MAX;
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            if (!tool.has(this.key)) {
                int tooltipIndex = this.addToolTip(tool, "§9Luck", "§9Lapis (0/450)");
                tool.setArray(this.key, new int[]{0, tooltipIndex});
                tool.modifiers--;
            }
            int[] keyPair = tool.getArray(this.key);
            keyPair[0] += this.increase;
            tool.setArray(this.key, keyPair);
            // "tool.getItem() instanceof Weapon": the weapons of 1.5.2 (not the hammer or the battleaxe)
            ResourceKey<Enchantment> enchantment = type.kind == ToolType.Kind.WEAPON ? Enchantments.LOOTING : Enchantments.FORTUNE;
            if (keyPair[0] >= 450) {
                enchant(stack, registries, enchantment, 3);
            } else if (keyPair[0] >= 300) {
                enchant(stack, registries, enchantment, 2);
            } else if (keyPair[0] >= 100) {
                enchant(stack, registries, enchantment, 1);
            }
            tool.setModifierTip(keyPair[1], "§9Lapis (" + keyPair[0] + "/" + MAX + ")");
        }

        @Override
        public boolean validType(ToolType type) {
            return type.is("weapon") || type.is("harvest");
        }
    }

    // ------------------------------------------------------------------ ModInteger, ModBoolean, ModReinforced

    /** ModInteger: Moss (auto-repair), Necrotic (life steal), Beheading. */
    public static class IntegerMod extends ToolMod {
        final String color;
        final String tooltipName;
        final int initialIncrease;
        final int secondaryIncrease;

        IntegerMod(List<Supplier<Ingredient>> items, int effect, String key, int increase, String color, String tip) {
            super(items, effect, key);
            this.initialIncrease = this.secondaryIncrease = increase;
            this.color = color;
            this.tooltipName = tip;
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            tool.setInt(this.key, tool.has(this.key) ? tool.getInt(this.key) + this.secondaryIncrease : this.initialIncrease);
            tool.modifiers--;
            this.addToolTip(tool, this.color + this.tooltipName, this.color + this.key);
        }
    }

    /** ModReinforced: an obsidian large plate, one more level of reinforced. */
    public static final class Reinforced extends IntegerMod {
        Reinforced(List<Supplier<Ingredient>> items, int effect) {
            super(items, effect, "Reinforced", 1, "§5", "Reinforced");
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            super.modify(input, stack, tool, type, registries);
            tool.unbreaking++;
        }
    }

    /** ModBoolean (and ModAutoSmelt): once per tool. */
    public static class BooleanMod extends ToolMod {
        final String color;
        final String tooltipName;

        BooleanMod(List<Supplier<Ingredient>> items, int effect, String key, String color, String tip) {
            super(items, effect, key);
            this.color = color;
            this.tooltipName = tip;
        }

        @Override
        protected boolean canModify(InfiTool tool, ToolType type, ItemStack[] input) {
            return tool.modifiers > 0 && !tool.getBool(this.key);
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            tool.setBool(this.key, true);
            tool.modifiers--;
            this.addToolTip(tool, this.color + this.tooltipName, this.color + this.key);
        }
    }

    /** ModAutoSmelt: not together with silky. */
    public static final class AutoSmelt extends BooleanMod {
        AutoSmelt(List<Supplier<Ingredient>> items, int effect, String key, String color, String tip) {
            super(items, effect, key, color, tip);
        }

        @Override
        protected boolean canModify(InfiTool tool, ToolType type, ItemStack[] input) {
            return !tool.getBool("Silk Touch") && super.canModify(tool, type, input);
        }
    }

    /** ModButtertouch: silky (silk touch), not with auto-smelt or luck; -3 attack, -300 mining speed. */
    public static final class Buttertouch extends BooleanMod {
        Buttertouch(List<Supplier<Ingredient>> items, int effect) {
            super(items, effect, "Silk Touch", "§e", "Silky");
        }

        @Override
        protected boolean canModify(InfiTool tool, ToolType type, ItemStack[] input) {
            if (!this.validType(type)) {
                return false;
            }
            return !tool.getBool("Lava") && !tool.has("Lapis") && tool.modifiers > 0 && !tool.getBool(this.key);
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            tool.setBool(this.key, true);
            enchant(stack, registries, Enchantments.SILK_TOUCH, 1);
            tool.modifiers--;
            tool.attack = Math.max(0, tool.attack - 3);
            tool.miningSpeed = Math.max(0, tool.miningSpeed - 300);
            if (tool.miningSpeed2 >= 0) {
                tool.miningSpeed2 = Math.max(0, tool.miningSpeed2 - 300);
            }
            this.addToolTip(tool, this.color + this.tooltipName, this.color + this.key);
        }

        @Override
        public boolean validType(ToolType type) {
            return type.is("weapon") || type.is("harvest");
        }
    }

    /** ModExtraModifier: diamond + gold block, nether star: one more free modifier, once each. */
    public static final class ExtraModifier extends ToolMod {
        ExtraModifier(List<Supplier<Ingredient>> items, String key) {
            super(items, 0, key);
        }

        @Override
        protected boolean canModify(InfiTool tool, ToolType type, ItemStack[] input) {
            return !tool.getBool(this.key);
        }

        @Override
        public void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries) {
            tool.setBool(this.key, true);
            tool.modifiers++;
        }

        @Override
        public void addMatchingEffect(InfiTool tool) {
        }
    }
}
