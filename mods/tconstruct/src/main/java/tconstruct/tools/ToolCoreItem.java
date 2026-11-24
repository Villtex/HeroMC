package tconstruct.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import tconstruct.TItems;

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
    private static final RandomSource RANDOM = RandomSource.createThreadSafe();

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

    /**
     * onLeftClickEntity: a hit that caused damage wears the tool, with the reinforced chance; then the modifiers (lapis,
     * necrotic, fire, knockback) and the hit effects of the tool ({@link ToolCombat#afterHit}).
     */
    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        onBlockChanged(stack, attacker);
        ToolCombat.afterHit(this, stack, target, attacker);
    }

    /**
     * Scythe.onLeftClickEntity: every entity around the target (the box of the target's block, 1 bigger), the target
     * too, gets the whole tool hit. FryingPan: no hit while the target has 14 or more ticks of invulnerability left.
     */
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (this.type == ToolType.SCYTHE) {
            if (!player.level().isClientSide()) {
                AABB box = new AABB(entity.getX(), entity.getY(), entity.getZ(), entity.getX() + 1.0, entity.getY() + 1.0, entity.getZ() + 1.0)
                        .inflate(1.0);
                for (Entity e : player.level().getEntities(player, box)) {
                    ToolCombat.attack(stack, player, e, this, 0);
                }
            }
            return true;
        }
        if (this.type == ToolType.FRYPAN && entity.getInvulnerableTime() >= 14) {
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ holding (onUpdate of the tools and TActiveOmniMod.updateTool)

    /**
     * Moss repairs one point now and then (chance moss in 350 under the sky, in 1150 below), not while swinging. The
     * held cleaver makes its holder slow at mining (mining fatigue III).
     */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        InfiTool t = InfiTool.of(stack);
        if (t == null) {
            return;
        }
        if (!living.isSwinging() && t.has("Moss")) {
            int chance = t.getInt("Moss");
            int check = level.canSeeSky(BlockPos.containing(entity.getX(), entity.getY(), entity.getZ())) ? 350 : 1150;
            if (RANDOM.nextInt(check) < chance) {
                damageTool(stack, -1, living);
            }
        }
        if (this.type == ToolType.CLEAVER && slot == EquipmentSlot.MAINHAND) {
            living.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 2, 2));
        }
    }

    // ------------------------------------------------------------------ right click (onItemRightClick, onPlayerStoppedUsing, onEaten)

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return switch (this.type) {
            case LONGSWORD, BATTLEAXE, DAGGER, SHORTBOW -> ItemUseAnimation.BOW;
            case CHISEL -> ItemUseAnimation.EAT;
            default -> super.getUseAnimation(stack);
        };
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return switch (this.type) {
            case LONGSWORD, BATTLEAXE, SHORTBOW -> 72000;
            case DAGGER -> 10;
            case CHISEL -> {
                InfiTool t = InfiTool.of(stack);
                yield t == null ? 20 : Math.max(1, 20 - t.miningSpeed / 100);
            }
            default -> super.getUseDuration(stack, entity);
        };
    }

    /**
     * Longsword: charge on the ground; battleaxe: charge; rapier: a jump backwards (on the ground); dagger: thrown at
     * once; chisel: start chiseling (in creative at once); shortbow: draw when there is an arrow (or in creative).
     */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InfiTool t = InfiTool.of(stack);
        switch (this.type) {
            case LONGSWORD -> {
                if (player.onGround()) {
                    player.startUsingItem(hand);
                }
                return InteractionResult.CONSUME;
            }
            case BATTLEAXE -> {
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
            case RAPIER -> {
                if (player.onGround()) {
                    player.causeFoodExhaustion(0.1F);
                    float yaw = player.getYRot() / 180.0F * (float) Math.PI;
                    float pitch = player.getXRot() / 180.0F * (float) Math.PI;
                    Vec3 m = player.getDeltaMovement();
                    player.setDeltaMovement(Mth.sin(yaw) * Mth.cos(pitch) * 0.5F, m.y + 0.32, -Mth.cos(yaw) * Mth.cos(pitch) * 0.5F);
                    player.needsSync = true;
                }
                return InteractionResult.CONSUME;
            }
            case DAGGER -> {
                if (!level.isClientSide()) {
                    level.addFreshEntity(new tconstruct.entity.ThrownDagger(level, player, stack.copy()));
                }
                stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
            case CHISEL -> {
                if (player.getAbilities().instabuild) {
                    this.chisel(stack, level, player);
                    return InteractionResult.SUCCESS;
                }
                if (t != null && !t.broken) {
                    player.startUsingItem(hand);
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.PASS;
            }
            case SHORTBOW -> {
                if (t != null && !t.broken && (player.getAbilities().instabuild || arrowSlot(player, Items.ARROW) >= 0
                        || arrowSlot(player, TItems.tool(ToolType.ARROW)) >= 0)) {
                    player.startUsingItem(hand);
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.FAIL;
            }
            case ARROW -> {
                return InteractionResult.PASS;
            }
            default -> {
                return super.use(level, player, hand);
            }
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        int time = this.getUseDuration(stack, entity) - remaining;
        switch (this.type) {
            case LONGSWORD -> {
                if (time > 5) {
                    leap(player, 0.05F, time);
                }
                return true;
            }
            case BATTLEAXE -> {
                int boost = Math.min(time / 100, 2);
                if (!level.isClientSide()) {
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, time * 4, boost));
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, time * 4, boost));
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, time * 4, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, time * 2, 0));
                }
                if (time > 5 && player.onGround()) {
                    leap(player, 0.025F, time);
                }
                return true;
            }
            case SHORTBOW -> {
                this.shoot(stack, level, player, time);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    /** The jump of the longsword and the battleaxe: up 0.2 + 0.02 per tick (at most 0.56), forward speed x ticks (at most 0.925). */
    private static void leap(Player player, float perTick, int time) {
        player.causeFoodExhaustion(0.2F);
        player.setSprinting(true);
        float increase = Math.min((float) (0.02 * time + 0.2), 0.56F);
        float speed = Math.min(perTick * time, 0.925F);
        float yaw = player.getYRot() / 180.0F * (float) Math.PI;
        float pitch = player.getXRot() / 180.0F * (float) Math.PI;
        Vec3 m = player.getDeltaMovement();
        player.setDeltaMovement(-Mth.sin(yaw) * Mth.cos(pitch) * speed, m.y + increase, Mth.cos(yaw) * Mth.cos(pitch) * speed);
        player.needsSync = true;
    }

    /** Dagger.onEaten (held for half a second): thrown as well. Chisel.onEaten: chisel the block looked at. */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (this.type == ToolType.DAGGER) {
                if (!level.isClientSide()) {
                    level.addFreshEntity(new tconstruct.entity.ThrownDagger(level, player, stack.copy()));
                }
                stack.shrink(1);
            } else if (this.type == ToolType.CHISEL) {
                this.chisel(stack, level, player);
            }
        }
        return stack;
    }

    /** Chisel.onEaten: the block looked at becomes its detailed version; the chisel wears (not in creative). */
    private void chisel(ItemStack stack, Level level, Player player) {
        if (level.isClientSide()) {
            return;
        }
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        tconstruct.recipe.Detailing.Detail detail = tconstruct.recipe.Detailing.get(state);
        if (detail == null) {
            return;
        }
        level.setBlock(pos, detail.output().get(), 3);
        if (!player.getAbilities().instabuild) {
            onBlockChanged(stack, null);
        }
        level.levelEvent(2001, pos, Block.getId(state));
        player.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
    }

    /** getInventorySlotContainItem: the first slot of the main inventory with the item, or -1. */
    private static int arrowSlot(Player player, Item item) {
        var items = player.getInventory().getNonEquipmentItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).is(item)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * BowBase.onPlayerStoppedUsing: the draw (ticks / DrawSpeed, eased, at least 0.1, at most FlightSpeed) shoots the
     * first arrow of the inventory, a Tinkers' arrow or a vanilla one (whichever comes first), crit at full draw. The
     * bow wears (reinforced chance), creative shoots without arrows and the arrow cannot be picked up.
     */
    private void shoot(ItemStack bow, Level level, Player player, int time) {
        InfiTool t = InfiTool.of(bow);
        if (t == null || t.broken) {
            return;
        }
        boolean creative = player.getAbilities().instabuild;
        int slotID = arrowSlot(player, TItems.tool(ToolType.ARROW));
        int arrowID = arrowSlot(player, Items.ARROW);
        if (!creative && slotID == -1 && arrowID == -1) {
            return;
        }
        float drawTime = t.getInt("DrawSpeed");
        float flightSpeed = t.getFloat("FlightSpeed");
        float speedBase = drawTime <= 0 ? 1.0F : time / drawTime;
        speedBase = (speedBase * speedBase + speedBase * 2.0F) / 3.0F;
        if (speedBase < 0.1) {
            return;
        }
        if (speedBase > flightSpeed) {
            speedBase = flightSpeed;
        }
        boolean vanilla = slotID == -1 || arrowID != -1 && slotID >= arrowID;
        if (!level.isClientSide()) {
            AbstractArrow arrow;
            if (vanilla) {
                arrow = new Arrow(level, player, new ItemStack(Items.ARROW), null);
                tconstruct.entity.TinkerArrow.aim(arrow, player, speedBase * 2.0F, 1.0F);
            } else {
                ItemStack arrowStack = player.getInventory().getNonEquipmentItems().get(slotID).copyWithCount(1);
                tconstruct.entity.TinkerArrow tinker = new tconstruct.entity.TinkerArrow(level, player, speedBase * 2.0F, arrowStack);
                tinker.setKnockback(t.getFloat("Knockback"), 0);
                arrow = tinker;
            }
            if (speedBase >= 1.0F) {
                arrow.setCritArrow(true);
            }
            arrow.pickup = creative ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
            level.addFreshEntity(arrow);
        }
        onBlockChanged(bow, player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (RANDOM.nextFloat() * 0.4F + 1.2F) + speedBase * 0.5F);
        if (!creative) {
            int slot = vanilla ? arrowID : slotID;
            player.getInventory().getNonEquipmentItems().get(slot).shrink(1);
        }
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
        if (this.type == ToolType.FRYPAN) {
            return putDownPan(context, stack, player);
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

    /**
     * FryingPan.onItemUse: sneaking on a solid block (not from below) puts the pan down next to the clicked side as the
     * frying pan block, which holds the pan.
     */
    private static InteractionResult putDownPan(UseOnContext context, ItemStack stack, Player player) {
        Direction side = context.getClickedFace();
        Level level = context.getLevel();
        if (side == Direction.DOWN || !player.isShiftKeyDown() || !level.getBlockState(context.getClickedPos()).isSolid()) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos().relative(side);
        BlockState pan = tconstruct.TBlocks.FRYPAN.get().defaultBlockState();
        if (!player.mayUseItemAt(pos, side, stack) || !level.getBlockState(pos).canBeReplaced() || !pan.canSurvive(level, pos)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, pan, 3);
            level.playSound(null, pos, tconstruct.TSounds.FRYPAN_HIT.get(), SoundSource.BLOCKS, 1.0F,
                    (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2F + 0.65F);
            if (level.getBlockEntity(pos) instanceof tconstruct.blocks.FrypanBlockEntity logic) {
                logic.setEquipmentItem(stack);
            }
        }
        stack.shrink(1);
        return InteractionResult.SUCCESS;
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
