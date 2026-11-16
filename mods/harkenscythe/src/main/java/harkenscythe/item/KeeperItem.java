package harkenscythe.item;

import harkenscythe.HSItems;
import harkenscythe.HarkenScythe;
import harkenscythe.block.CreepBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * ItemHSKeeper: the Essence Keeper and Essence Vessel are empty; the Soulkeeper (20 souls), Bloodkeeper (40 blood),
 * Soul Vessel (40 souls) and Blood Vessel (80 blood) hold essence as durability ({@link Essence}). Holding right-click
 * drinks from a filled one, one soul (two blood) every 32 ticks ({@link HarkenScythe#DRINK_COUNT}); a drink of blood
 * heals 1. Letting go, or reaching what is stored, ends the feast: souls give a random potion effect (good or bad,
 * stronger and longer with more souls and soulweave armour) and hunger, blood gives strength (and speed with a full
 * set of bloodweave) and hunger. In a recipe a keeper is used up and leaves an Essence Keeper; a vessel gives half its
 * capacity and leaves the rest. A Bloodkeeper or Blood Vessel soaks tilled creep with blood (one blood).
 */
public class KeeperItem extends Item {
    public enum Kind {
        ESSENCE_KEEPER(0, false), SOULKEEPER(20, false), BLOODKEEPER(40, true), ESSENCE_VESSEL(0, false), SOUL_VESSEL(40, false),
        BLOOD_VESSEL(80, true);

        final int capacity;
        final boolean blood;

        Kind(int capacity, boolean blood) {
            this.capacity = capacity;
            this.blood = blood;
        }

        boolean filled() {
            return this.capacity > 0;
        }

        boolean vessel() {
            return this == ESSENCE_VESSEL || this == SOUL_VESSEL || this == BLOOD_VESSEL;
        }
    }

    /** The potion ids of 1.5.2 the soul feast picks from: speed, haste, strength, instant health, jump boost, ... */
    private static final List<Holder<MobEffect>> SOUL_BUFFS = List.of(MobEffects.SPEED, MobEffects.HASTE, MobEffects.STRENGTH,
            MobEffects.INSTANT_HEALTH, MobEffects.JUMP_BOOST, MobEffects.REGENERATION, MobEffects.RESISTANCE, MobEffects.FIRE_RESISTANCE,
            MobEffects.WATER_BREATHING, MobEffects.INVISIBILITY, MobEffects.NIGHT_VISION);
    private static final List<Holder<MobEffect>> SOUL_DEBUFFS = List.of(MobEffects.SLOWNESS, MobEffects.MINING_FATIGUE,
            MobEffects.INSTANT_DAMAGE, MobEffects.NAUSEA, MobEffects.BLINDNESS, MobEffects.WEAKNESS, MobEffects.POISON, MobEffects.WITHER);

    private final Kind kind;

    public KeeperItem(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public static Item.Properties properties(Item.Properties p, Kind kind) {
        p.stacksTo(1);
        if (kind.filled()) {
            p.durability(kind.capacity).setNoCombineRepair().rarity(Rarity.EPIC).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        return p;
    }

    public Kind kind() {
        return this.kind;
    }

    public boolean isBlood() {
        return this.kind.blood;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        if (this.kind.filled()) {
            builder.accept(Component.translatable(this.kind.blood ? "tooltip.harkenscythe_core.stored_blood" : "tooltip.harkenscythe_core.stored_souls",
                    Essence.stored(stack), stack.getMaxDamage()).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return this.kind.filled() ? ItemUseAnimation.DRINK : ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return this.kind.filled() ? 32 : 0;
    }

    private int perDrink() {
        return this.kind.blood ? 2 : 1;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!this.kind.filled()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        int drinks = stack.getOrDefault(HarkenScythe.DRINK_COUNT.get(), 0);
        if (drinks * this.perDrink() < Essence.stored(stack)) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        player.stopUsingItem();
        this.endFeast(stack, level, player, hand);
        return InteractionResult.SUCCESS;
    }

    /** onEaten: one more drink; blood heals 1 at once. */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
            stack.set(HarkenScythe.DRINK_COUNT.get(), stack.getOrDefault(HarkenScythe.DRINK_COUNT.get(), 0) + 1);
            if (this.kind.blood && entity.getHealth() < entity.getMaxHealth()) {
                entity.heal(1.0F);
            }
        }
        return stack;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (entity instanceof Player player) {
            this.endFeast(stack, level, player, player.getUsedItemHand());
        }
        return true;
    }

    /** onPlayerStoppedUsing: the effects of the drinks taken, which are paid from the keeper. */
    private void endFeast(ItemStack stack, Level level, Player player, InteractionHand hand) {
        int drinks = stack.getOrDefault(HarkenScythe.DRINK_COUNT.get(), 0);
        if (level.isClientSide() || drinks == 0) {
            return;
        }
        stack.remove(HarkenScythe.DRINK_COUNT.get());
        stack.setDamageValue(Math.min(stack.getMaxDamage(), stack.getDamageValue() + drinks * this.perDrink()));
        if (this.kind.blood) {
            bloodDrinking(player, drinks);
        } else {
            soulFeasting(player, drinks, level.getRandom());
        }
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            player.setItemInHand(hand, new ItemStack(this.kind.vessel() ? HSItems.ESSENCE_VESSEL.get() : HSItems.ESSENCE_KEEPER.get()));
        }
    }

    private static void soulFeasting(Player player, int amount, RandomSource random) {
        int armor = WeaveArmor.count(player, false);
        int rank = armor;
        if (rank < 4 && amount >= 5) {
            rank += amount / 5;
        }
        rank = Math.min(rank, 3);
        int duration = 410 + 205 * amount + 300 * armor;
        int hungerRank = Math.min(amount >= 10 ? amount / 10 : 0, 3);
        int hungerDuration = 205 + 102 * amount;
        Holder<MobEffect> effect = random.nextInt(100) + 1 >= 50 && armor < 4 ? SOUL_DEBUFFS.get(random.nextInt(SOUL_DEBUFFS.size()))
                : SOUL_BUFFS.get(random.nextInt(SOUL_BUFFS.size()));
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(effect, duration, rank));
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, hungerDuration, hungerRank));
    }

    private static void bloodDrinking(Player player, int amount) {
        int armor = WeaveArmor.count(player, true);
        int rank = armor;
        if (rank < 4 && amount >= 5) {
            rank += amount / 5;
        }
        rank = Math.min(rank, 3);
        int duration = 103 + 103 * amount + 100 * armor;
        int hungerRank = Math.min(amount >= 10 ? amount / 10 : 0, 3);
        int hungerDuration = 43 + 43 * amount;
        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, duration, rank));
        if (armor >= 4) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, 0));
        }
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, hungerDuration, hungerRank));
    }

    /** getContainerItemStack: a keeper leaves an empty one; a vessel gives half its capacity (and is empty after the second time). */
    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        if (!this.kind.filled()) {
            return null;
        }
        if (!this.kind.vessel()) {
            return new ItemStackTemplate(HSItems.ESSENCE_KEEPER.get());
        }
        int damage = instance.getOrDefault(DataComponents.DAMAGE, 0);
        int half = this.kind.capacity / 2;
        if (damage >= half) {
            return new ItemStackTemplate(HSItems.ESSENCE_VESSEL.get());
        }
        ItemStack rest = new ItemStack(this);
        rest.setDamageValue(damage + half);
        return ItemStackTemplate.fromNonEmptyStack(rest);
    }

    /** A Bloodkeeper or Blood Vessel on top of tilled creep soaks it with blood (metadata 2) for one blood. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!this.kind.blood || context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        if (player == null || !player.mayUseItemAt(pos, Direction.UP, stack)) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(pos);
        if (!CreepBlock.isCreep(state) || state.getValue(CreepBlock.SOIL) != 1) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(CreepBlock.SOIL, 2), Block.UPDATE_CLIENTS);
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.PLAYER_SWIM, SoundSource.BLOCKS, 0.05F, 0.05F);
            ItemStack rest = Essence.drain(stack, 1);
            if (rest != stack) {
                player.setItemInHand(context.getHand(), rest);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
