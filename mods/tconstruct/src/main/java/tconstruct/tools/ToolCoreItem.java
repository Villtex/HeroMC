package tconstruct.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import tconstruct.TConstruct;

import java.util.function.Consumer;

/**
 * {@code ToolCore} with HarvestTool, DualHarvestTool and Weapon: one item per tool, its stats in the component
 * {@code tconstruct:tool}. The durability is the tool's own (Damage / TotalDurability): a worn-out tool breaks (it
 * becomes "Broken" and slow) instead of disappearing, and is repaired in the tool station. Mining speed and level,
 * attack damage (through the attack damage attribute, so the 1.5.2 combat rules of combat152 apply), wear, the block
 * placing of the harvest tools and the hoe of the mattock are here; the special abilities follow in part 25b.
 */
public class ToolCoreItem extends Item {
    /** The harvest levels of Tinkers' Construct above diamond (cobalt / ardite ores of part 25d). */
    public static final TagKey<Block> NEEDS_COBALT_TOOL = TagKey.create(Registries.BLOCK, TConstruct.id("needs_cobalt_tool"));
    public static final TagKey<Block> NEEDS_MANYULLYN_TOOL = TagKey.create(Registries.BLOCK, TConstruct.id("needs_manyullyn_tool"));
    private static final TagKey<Block> WEB = TagKey.create(Registries.BLOCK, TConstruct.id("effective/web"));
    private static final RandomSource RANDOM = RandomSource.create();

    public final ToolType type;

    public ToolCoreItem(ToolType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    /** MinecraftForge.getBlockHarvestLevel: the level of the tool tags (0 stone/wood, 1 stone, 2 iron, 3 diamond...). */
    public static int harvestLevel(BlockState state) {
        if (state.is(NEEDS_MANYULLYN_TOOL)) {
            return 5;
        }
        if (state.is(NEEDS_COBALT_TOOL)) {
            return 4;
        }
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return 3;
        }
        if (state.is(BlockTags.NEEDS_IRON_TOOL)) {
            return 2;
        }
        if (state.is(BlockTags.NEEDS_STONE_TOOL)) {
            return 1;
        }
        return 0;
    }

    // ------------------------------------------------------------------ mining

    /**
     * getStrVsBlock. Too low a mining level gave 0.1 in 1.5.2 with the normal divisor 30 (the tool could harvest the
     * material); today a tool that may not harvest a block that needs one uses the divisor 100, so the 0.1 is scaled.
     */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        InfiTool t = InfiTool.of(stack);
        if (t == null) {
            return 1.0F;
        }
        if (t.broken) {
            return 0.1F;
        }
        float slow = state.requiresCorrectToolForDrops() ? 0.1F * 100.0F / 30.0F : 0.1F;
        int level = harvestLevel(state);
        switch (this.type.kind) {
            case HARVEST -> {
                if (this.type.effective != null && state.is(this.type.effective)) {
                    int[] heads = new int[1];
                    float speed = t.summedSpeed(heads) / (heads[0] * (float) this.type.speedDivisor);
                    speed += (float) Math.log(t.damage / (float) this.type.shoddyDivisor + 1.0F) * 2.0F * t.shoddy;
                    return level <= t.harvestLevel ? speed : slow;
                }
            }
            case DUAL -> {
                if (this.type.effective != null && state.is(this.type.effective)) {
                    float speed = t.miningSpeed / 100.0F + t.shoddy * t.damage / 100.0F;
                    return level <= t.harvestLevel ? speed : slow;
                }
                if (this.type.effective2 != null && state.is(this.type.effective2)) {
                    float speed = t.miningSpeed2 / 100.0F + t.shoddy * t.damage / 100.0F;
                    return level <= t.harvestLevel2 ? speed : slow;
                }
            }
            case WEAPON -> {
                return state.is(WEB) ? 15.0F : 1.5F;
            }
            case CORE -> {
            }
        }
        return 1.0F;
    }

    /**
     * canHarvestBlock together with the level check of onBlockStartBreak (a block above the tool's level was destroyed
     * without drops). Broken tools still harvest, slowly.
     */
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        InfiTool t = InfiTool.of(stack);
        if (t == null) {
            return false;
        }
        int level = harvestLevel(state);
        return switch (this.type.kind) {
            case HARVEST -> this.type.effective != null && state.is(this.type.effective) && level <= t.harvestLevel;
            case DUAL -> this.type.effective != null && state.is(this.type.effective) && level <= t.harvestLevel
                    || this.type.effective2 != null && state.is(this.type.effective2) && level <= t.harvestLevel2;
            case WEAPON -> state.is(WEB);
            case CORE -> false;
        };
    }

    /** onBlockDestroyed: blocks with hardness wear the tool (axes not on leaves), with the reinforced chance. */
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.getDestroySpeed(level, pos) != 0.0F && !(this.type.sparesLeaves() && state.is(BlockTags.LEAVES))) {
            onBlockChanged(stack, miner);
        }
        return true;
    }

    // ------------------------------------------------------------------ fighting

    /**
     * The "Attack" of onLeftClickEntity as attack damage (the player's 1 plus the modifier): worn stonebound tools hit
     * weaker, jagged ones harder, broken ones like a fist. The damage modifier of cleaver, rapier and scythe is applied in
     * {@link TEvents}.
     */
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        InfiTool t = InfiTool.of(stack);
        if (t == null) {
            return ItemAttributeModifiers.EMPTY;
        }
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamage(t) - 1, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND, ItemAttributeModifiers.Display.hidden())
                .build();
    }

    /** Attack + the stonebound damage, at least 1; broken: 1. */
    public static int attackDamage(InfiTool t) {
        if (t.broken) {
            return 1;
        }
        float stonebound = (float) Math.log(t.damage / 72.0F + 1.0F) * -2.0F * t.shoddy;
        return Math.max(1, (int) (t.attack + stonebound));
    }

    /** onLeftClickEntity: a hit that caused damage wears the tool, with the reinforced chance. */
    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        onBlockChanged(stack, attacker);
    }

    // ------------------------------------------------------------------ durability (AbilityHelper)

    /** AbilityHelper.onBlockChanged: one damage, skipped with a chance of Unbreaking / 10. */
    public static void onBlockChanged(ItemStack stack, @Nullable LivingEntity entity) {
        InfiTool t = InfiTool.of(stack);
        if (t == null) {
            return;
        }
        if (RANDOM.nextInt(10) < 10 - t.unbreaking) {
            damageTool(stack, 1, entity);
        }
    }

    /** AbilityHelper.damageTool (negative = heal): not in creative mode; past the total durability the tool breaks. */
    public static void damageTool(ItemStack stack, int dam, @Nullable LivingEntity entity) {
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return;
        }
        InfiTool t = InfiTool.of(stack);
        if (t == null) {
            return;
        }
        int damageTrue = t.damage + dam;
        if (damageTrue <= 0) {
            t.damage = 0;
            t.broken = false;
        } else if (damageTrue > t.totalDurability) {
            if (!t.broken && entity != null) {
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            t.broken = true;
        } else {
            t.damage = damageTrue;
        }
        t.save(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        InfiTool t = InfiTool.of(stack);
        return t != null && !t.broken && t.damage > 0 && t.totalDurability > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        InfiTool t = InfiTool.of(stack);
        return t == null || t.totalDurability <= 0 ? MAX_BAR_WIDTH : Math.round(MAX_BAR_WIDTH - t.damage * (float) MAX_BAR_WIDTH / t.totalDurability);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        InfiTool t = InfiTool.of(stack);
        float f = t == null || t.totalDurability <= 0 ? 1.0F : Math.max(0.0F, (t.totalDurability - t.damage) / (float) t.totalDurability);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    // ------------------------------------------------------------------ using

    /**
     * ToolCore.onItemUse: the harvest tools place the block of the hotbar slot right of them (slot 0 uses slot 8, slot 8
     * none), not into the player. Mattock: hoes grass and dirt.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        InfiTool t = InfiTool.of(stack);
        if (player == null || t == null) {
            return InteractionResult.PASS;
        }
        if (this.type == ToolType.MATTOCK) {
            return t.broken ? InteractionResult.PASS : hoe(context, stack);
        }
        if (!this.type.placesBlocks()) {
            return InteractionResult.PASS;
        }
        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        BlockPos feet = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        if (target.getX() == feet.getX() && target.getZ() == feet.getZ() && Math.abs(target.getY() - feet.getY()) <= 1) {
            return InteractionResult.PASS;
        }
        int hotbarSlot = player.getInventory().getSelectedSlot();
        if (hotbarSlot >= 8) {
            return InteractionResult.PASS;
        }
        int itemSlot = hotbarSlot == 0 ? 8 : hotbarSlot + 1;
        ItemStack nearby = player.getInventory().getItem(itemSlot);
        if (!(nearby.getItem() instanceof BlockItem)) {
            return InteractionResult.PASS;
        }
        BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
        InteractionResult result = nearby.useOn(new UseOnContext(context.getLevel(), player, context.getHand(), nearby, hit));
        if (nearby.isEmpty()) {
            player.getInventory().setItem(itemSlot, ItemStack.EMPTY);
        }
        return result;
    }

    /** AbilityHelper.hoeGround: grass or dirt with air above, not from below, becomes farmland. */
    private static InteractionResult hoe(UseOnContext context, ItemStack stack) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(pos);
        boolean grass = context.getClickedFace() != Direction.DOWN && level.getBlockState(pos.above()).isAir() && state.is(Blocks.GRASS_BLOCK);
        if (!grass && !state.is(Blocks.DIRT)) {
            return InteractionResult.PASS;
        }
        SoundType sound = Blocks.FARMLAND.defaultBlockState().getSoundType();
        level.playSound(player, pos, sound.getStepSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (level instanceof ServerLevel) {
            level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
            onBlockChanged(stack, player);
        }
        return InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------------ tooltip

    /**
     * ToolCore.addInformation: "Broken", the abilities of the materials (once each), the reinforced level and the
     * tooltips of the modifiers.
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        InfiTool t = InfiTool.of(stack);
        if (t == null) {
            return;
        }
        if (t.broken) {
            tooltip.accept(Component.translatable("tooltip.tconstruct.broken").withStyle(ChatFormatting.ITALIC));
            return;
        }
        int parts = this.type.partAmount();
        ability(tooltip, t.head);
        if (t.handle != t.head) {
            ability(tooltip, t.handle);
        }
        if (parts >= 3 && t.accessory != t.head && t.accessory != t.handle) {
            ability(tooltip, t.accessory);
        }
        if (parts >= 4 && t.extra != t.head && t.extra != t.handle && t.extra != t.accessory) {
            ability(tooltip, t.extra);
        }
        Component reinforced = this.reinforcedName(t, parts);
        if (reinforced != null) {
            tooltip.accept(reinforced);
        }
        for (String[] tip : t.tips) {
            if (!tip[0].isEmpty()) {
                tooltip.accept(ToolTips.translate(tip[0]));
            }
        }
    }

    private static void ability(Consumer<Component> tooltip, int id) {
        TMaterial material = TMaterial.byId(id);
        if (material != null && !material.ability.isEmpty()) {
            MutableComponent line = Component.translatable("tooltip.tconstruct.ability." + material.ability.toLowerCase());
            tooltip.accept(material.style == null ? line : line.withStyle(material.style));
        }
    }

    /** getReinforcedName: the colour of the most reinforced material, the level of Unbreaking. */
    private @Nullable Component reinforcedName(InfiTool t, int parts) {
        int reinforced = 0;
        ChatFormatting style = null;
        int[] ids = {t.head, t.handle, parts >= 3 ? t.accessory : -1, parts >= 4 ? t.extra : -1};
        for (int id : ids) {
            TMaterial material = TMaterial.byId(id);
            if (material != null && material.reinforced > 0 && material.reinforced > reinforced) {
                style = material.style;
                reinforced = material.reinforced;
            }
        }
        reinforced = t.unbreaking;
        if (reinforced <= 0) {
            return null;
        }
        MutableComponent line = reinforced > 9 ? Component.translatable("tooltip.tconstruct.unbreakable")
                : Component.translatable("tooltip.tconstruct.reinforced", Component.translatable("enchantment.level." + Math.min(reinforced, 10)));
        return style == null ? line : line.withStyle(style);
    }
}
