package harkenscythe.altar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * One entry of RecipeHSSoulAltar / RecipeHSBloodAltar (soulAltarRecipeList and soulAltarAugmentList). {@code order} is the
 * old recipe number (1-6 transmutations, 20-27 augments): of several matches the highest wins, as the augment button of
 * 1.5.2 replaced the transmutation button and a later augment check overwrote an earlier one (Asgard shield over armour,
 * giant sword over sword).
 * <ul>
 * <li>{@code cost}: souls or blood per item (the GUI showed cost x stack size); with {@code recharge} it is the step of
 * the refill price: ceil(damage / cost) * cost (soulKeeperRechargeCost, bloodKeeperRechargeCost).</li>
 * <li>{@code result}: a new stack of the whole slot's size without the old data (new ItemStack(item, stackSize));
 * without it the item itself is kept (augments).</li>
 * <li>{@code augments}: the dice of soulAltarItemStackAugmentBuilder: two numbers 1-100 (roll and rank) are thrown, every
 * row whose ranges hold both adds its enchantment. Augment recipes take only unenchanted items.</li>
 * <li>{@code single}: only a stack of one (the book).</li>
 * </ul>
 */
public record AltarRecipe(Kind altar, int order, Action action, Ingredient ingredient, Optional<Ingredient> exclude, int cost, boolean recharge,
                          boolean single, Optional<ItemStackTemplate> result, DataComponentPatch components, List<Augment> augments)
        implements Recipe<SingleRecipeInput> {

    public enum Kind implements StringRepresentable {
        SOUL(5), BLOOD(10);

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);
        /** Souls or blood per crucible level. */
        public final int unit;

        Kind(int unit) {
            this.unit = unit;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    /** The label of the GUI button. */
    public enum Action implements StringRepresentable {
        ENTHRALL, ENTWINE, IMBUE, GERMINATE, AUGMENT, ATTUNE;

        public static final Codec<Action> CODEC = StringRepresentable.fromEnum(Action::values);

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public record Augment(ResourceKey<Enchantment> enchantment, int level, int minRoll, int maxRoll, int minRank, int maxRank) {
        public static final Codec<Augment> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceKey.codec(Registries.ENCHANTMENT).fieldOf("enchantment").forGetter(Augment::enchantment),
                Codec.INT.fieldOf("level").forGetter(Augment::level),
                Codec.INT.optionalFieldOf("min_roll", 1).forGetter(Augment::minRoll),
                Codec.INT.optionalFieldOf("max_roll", 100).forGetter(Augment::maxRoll),
                Codec.INT.optionalFieldOf("min_rank", 1).forGetter(Augment::minRank),
                Codec.INT.optionalFieldOf("max_rank", 100).forGetter(Augment::maxRank)).apply(i, Augment::new));

        boolean holds(int roll, int rank) {
            return roll >= this.minRoll && roll <= this.maxRoll && rank >= this.minRank && rank <= this.maxRank;
        }

        /** The chance of this row in percent. */
        public double chance() {
            return (this.maxRoll - this.minRoll + 1) * (this.maxRank - this.minRank + 1) / 100.0;
        }
    }

    public static final MapCodec<AltarRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Kind.CODEC.fieldOf("altar").forGetter(AltarRecipe::altar),
            Codec.INT.fieldOf("order").forGetter(AltarRecipe::order),
            Action.CODEC.fieldOf("action").forGetter(AltarRecipe::action),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(AltarRecipe::ingredient),
            Ingredient.CODEC.optionalFieldOf("exclude").forGetter(AltarRecipe::exclude),
            Codec.INT.fieldOf("cost").forGetter(AltarRecipe::cost),
            Codec.BOOL.optionalFieldOf("recharge", false).forGetter(AltarRecipe::recharge),
            Codec.BOOL.optionalFieldOf("single", false).forGetter(AltarRecipe::single),
            ItemStackTemplate.CODEC.optionalFieldOf("result").forGetter(AltarRecipe::result),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(AltarRecipe::components),
            Augment.CODEC.listOf().optionalFieldOf("augments", List.of()).forGetter(AltarRecipe::augments)).apply(i, AltarRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(MAP_CODEC.codec());
    public static final RecipeSerializer<AltarRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public boolean isAugment() {
        return !this.augments.isEmpty();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.matches(input.item());
    }

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty() || !this.ingredient.test(stack) || this.exclude.isPresent() && this.exclude.get().test(stack)) {
            return false;
        }
        if (this.single && stack.getCount() != 1) {
            return false;
        }
        return !this.isAugment() || !stack.isEnchanted();
    }

    /** Souls or blood for one item. */
    public int unitCost(ItemStack stack) {
        if (!this.recharge) {
            return this.cost;
        }
        int damage = stack.getDamageValue();
        return (damage + this.cost - 1) / this.cost * this.cost;
    }

    /** The price shown in the GUI: the cost of one item times the stack size. */
    public int totalCost(ItemStack stack) {
        return this.unitCost(stack) * stack.getCount();
    }

    /** The new content of the slot (the augment dice use {@code random}). */
    public ItemStack apply(ItemStack stack, RandomSource random, HolderLookup.Provider registries) {
        ItemStack out;
        if (this.result.isPresent()) {
            out = new ItemStack(this.result.get().item(), stack.getCount());
        } else {
            out = stack.copy();
        }
        out.applyComponents(this.components);
        if (this.isAugment()) {
            int roll = random.nextInt(100) + 1;
            int rank = random.nextInt(100) + 1;
            HolderLookup.RegistryLookup<Enchantment> lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
            for (Augment augment : this.augments) {
                if (augment.holds(roll, rank)) {
                    lookup.get(augment.enchantment()).ifPresent(holder -> out.enchant(holder, augment.level()));
                }
            }
        }
        return out;
    }

    /** What the slot turns into, without the augment (for JEI). */
    public ItemStack preview(ItemStack stack) {
        ItemStack out = this.result.map(r -> new ItemStack(r.item(), stack.getCount())).orElseGet(stack::copy);
        out.applyComponents(this.components);
        return out;
    }

    /** The recipe of the altar for this stack: the matching one with the highest order. */
    public static @Nullable AltarRecipe find(Collection<RecipeHolder<AltarRecipe>> recipes, Kind altar, ItemStack stack) {
        AltarRecipe best = null;
        for (RecipeHolder<AltarRecipe> holder : recipes) {
            AltarRecipe recipe = holder.value();
            if (recipe.altar == altar && recipe.matches(stack) && (best == null || recipe.order > best.order)) {
                best = recipe;
            }
        }
        return best;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return this.preview(input.item());
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<AltarRecipe> getSerializer() {
        return Altars.ALTAR_SERIALIZER.get();
    }

    @Override
    public RecipeType<AltarRecipe> getType() {
        return Altars.ALTAR_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
