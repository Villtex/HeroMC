package tconstruct.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import tconstruct.modifiers.Modifiers;

import java.util.List;
import java.util.Optional;

/**
 * {@code onBlockStartBreak} of the tools and {@code TActiveOmniMod.beforeBlockBreak}: the area tools (hammer and
 * excavator 3x3, scythe 3x3x3), the lumber axe (whole trees), the battleaxe (the wood above), auto-smelt (lava) and the
 * luck that grows with every block (lapis). They hook into the block break of the server ({@link BreakBlockEvent}, which
 * comes before the item's {@code mineBlock}); the other blocks of an area break through the player's own
 * {@code destroyBlock}, so drops, statistics, protection and the tool wear stay vanilla. While an area breaks only the
 * modifiers act on each block ({@link #areaBreak}).
 */
public final class ToolAbilities {
    private static final RandomSource RANDOM = RandomSource.createThreadSafe();
    /** Scythe.materials: web, cactus, pumpkin, plants, vine, leaves. */
    private static final net.minecraft.tags.TagKey<Block> SCYTHE = net.minecraft.tags.TagKey.create(Registries.BLOCK,
            tconstruct.TConstruct.id("effective/scythe"));
    /** True while a tool breaks the other blocks of its area (the 1.5.2 loops called the modifiers directly). */
    private static final ThreadLocal<Boolean> AREA = ThreadLocal.withInitial(() -> false);

    private ToolAbilities() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, BreakBlockEvent.class, ToolAbilities::onBreak);
    }

    private static void onBreak(BreakBlockEvent event) {
        if (event.isCanceled() || !(event.getPlayer() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ToolCoreItem tool) || InfiTool.of(stack) == null) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (AREA.get()) {
            if (beforeBlockBreak(tool, stack, level, pos, player)) {
                event.setCanceled(true);
            }
            return;
        }
        boolean cancel = switch (tool.type) {
            case HAMMER, EXCAVATOR -> broadBreak(tool, stack, level, pos, state, player);
            case LUMBERAXE -> {
                lumberAxe(tool, stack, level, pos, state, player);
                yield beforeBlockBreak(tool, stack, level, pos, player);
            }
            case BATTLEAXE -> {
                battleaxe(tool, stack, level, pos, state, player);
                yield beforeBlockBreak(tool, stack, level, pos, player);
            }
            case SCYTHE -> {
                scythe(tool, stack, level, pos, state, player);
                yield beforeBlockBreak(tool, stack, level, pos, player);
            }
            default -> beforeBlockBreak(tool, stack, level, pos, player);
        };
        if (cancel) {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------ the modifiers (TActiveOmniMod.beforeBlockBreak)

    /**
     * Lapis grows (midStreamModify), then lava smelts the drop of the block: the block vanishes, the tool wears, the
     * smelted item (times a fortune roll unless it is a block) and its experience drop. True = the normal harvest is cancelled.
     */
    public static boolean beforeBlockBreak(ToolCoreItem tool, ItemStack stack, ServerLevel level, BlockPos pos, Player player) {
        if (player.getAbilities().instabuild) {
            return false;
        }
        midStreamModify(stack, tool.type, level);
        InfiTool t = InfiTool.of(stack);
        BlockState state = level.getBlockState(pos);
        if (t == null || state.isAir() || !t.getBool("Lava")) {
            return false;
        }
        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, ItemStack.EMPTY);
        if (drops.isEmpty() || drops.getFirst().isEmpty()) {
            return false;
        }
        ItemStack drop = drops.getFirst();
        SingleRecipeInput input = new SingleRecipeInput(drop);
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level);
        if (recipe.isEmpty()) {
            return false;
        }
        ItemStack result = recipe.get().value().assemble(input);
        if (result.isEmpty()) {
            return false;
        }
        level.removeBlock(pos, false);
        tool.mineBlock(stack, level, state, pos, player);
        ItemStack spawn = result.copy();
        spawn.setCount(result.getCount() * drop.getCount());
        if (!(result.getItem() instanceof BlockItem)) {
            int loot = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.FORTUNE)
                    .map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, stack)).orElse(0);
            if (loot > 0) {
                spawn.setCount(spawn.getCount() * (RANDOM.nextInt(loot + 1) + 1));
            }
        }
        ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, spawn);
        item.setPickUpDelay(10);
        level.addFreshEntity(item);
        level.levelEvent(2001, pos, Block.getId(state));
        int xp = spawn.getCount();
        float f = recipe.get().value().experience();
        if (f == 0.0F) {
            xp = 0;
        } else if (f < 1.0F) {
            int j = Mth.floor(xp * f);
            if (j < Mth.ceil(xp * f) && Math.random() < xp * f - j) {
                j++;
            }
            xp = j;
        }
        if (xp > 0) {
            ExperienceOrb.award(level, new Vec3(pos.getX(), pos.getY() + 0.5, pos.getZ()), xp);
        }
        for (int i = 0; i < 5; i++) {
            double x = pos.getX() + RANDOM.nextFloat();
            double y = pos.getY() + RANDOM.nextFloat();
            double z = pos.getZ() + RANDOM.nextFloat();
            double d = 0.52;
            double r = RANDOM.nextFloat() * 0.6 - 0.3;
            for (double[] o : new double[][]{{-d, r}, {d, r}, {r, -d}, {r, d}}) {
                level.sendParticles(ParticleTypes.SMOKE, x + o[0], y, z + o[1], 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.FLAME, x + o[0], y, z + o[1], 1, 0, 0, 0, 0);
            }
        }
        return true;
    }

    /**
     * ModLapis.midStreamModify: one more lapis in 50 (until the maximum), and a chance for the next level of looting
     * (weapons) and fortune (harvest tools) that grows with the lapis.
     */
    public static void midStreamModify(ItemStack stack, ToolType type, Level level) {
        InfiTool t = InfiTool.of(stack);
        if (t == null || !t.has("Lapis")) {
            return;
        }
        int[] keyPair = t.getArray("Lapis");
        if (keyPair[0] == Modifiers.LAPIS_MAX) {
            return;
        }
        if (RANDOM.nextInt(50) == 0) {
            keyPair[0]++;
            t.setArray("Lapis", keyPair);
            t.setModifierTip(keyPair[1], "§9Lapis (" + keyPair[0] + "/" + Modifiers.LAPIS_MAX + ")");
            t.save(stack);
        }
        if (type.is("weapon")) {
            int lvl = luckLevel(keyPair[0]);
            if (lvl > 0) {
                Modifiers.enchant(stack, level.registryAccess(), Enchantments.LOOTING, lvl);
            }
        }
        if (type.is("harvest")) {
            int lvl = luckLevel(keyPair[0]);
            if (lvl > 0) {
                Modifiers.enchant(stack, level.registryAccess(), Enchantments.FORTUNE, lvl);
            }
        }
    }

    /** The random level of midStreamModify (0 = nothing this time). */
    private static int luckLevel(int lapis) {
        if (lapis >= 450) {
            return 3;
        }
        if (lapis >= 350) {
            return RANDOM.nextInt(1000 - (lapis - 300)) == 0 ? 3 : 0;
        }
        if (lapis >= 125) {
            return RANDOM.nextInt(600 - (lapis - 175)) == 0 ? 2 : 0;
        }
        if (lapis >= 10) {
            return RANDOM.nextInt(250 - (lapis - 25)) == 0 ? 1 : 0;
        }
        return 0;
    }

    // ------------------------------------------------------------------ the tools

    /** Breaks one more block of an area through the player (vanilla drops, wear); only the modifiers act on it. */
    private static void areaBreak(ServerPlayer player, BlockPos pos) {
        boolean old = AREA.get();
        AREA.set(true);
        try {
            player.gameMode.destroyBlock(pos);
        } finally {
            AREA.set(old);
        }
    }

    private static boolean broken(ItemStack stack) {
        InfiTool t = InfiTool.of(stack);
        return t == null || t.broken;
    }

    /**
     * Hammer / Excavator.onBlockStartBreak: on an effective block, the 3x3 plane across the side the player looks at;
     * blocks the tool may harvest that are at most 1.5 harder than the first one. Replaces the normal break.
     */
    private static boolean broadBreak(ToolCoreItem tool, ItemStack stack, ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player) {
        boolean validStart = tool.type.effective != null && state.is(tool.type.effective)
                || tool.type == ToolType.HAMMER && state.getBlock() instanceof InfestedBlock;
        BlockHitResult mop = raytrace(level, player);
        if (!validStart || mop.getType() == HitResult.Type.MISS) {
            return beforeBlockBreak(tool, stack, level, pos, player);
        }
        Direction side = mop.getDirection();
        int xRange = side.getAxis() == Direction.Axis.X ? 0 : 1;
        int yRange = side.getAxis() == Direction.Axis.Y ? 0 : 1;
        int zRange = side.getAxis() == Direction.Axis.Z ? 0 : 1;
        float hardness = state.getDestroySpeed(level, pos);
        int toolLevel = InfiTool.of(stack).harvestLevel;
        for (int x = pos.getX() - xRange; x <= pos.getX() + xRange; x++) {
            for (int y = pos.getY() - yRange; y <= pos.getY() + yRange; y++) {
                for (int z = pos.getZ() - zRange; z <= pos.getZ() + zRange; z++) {
                    if (broken(stack)) {
                        continue;
                    }
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState local = level.getBlockState(p);
                    if (local.isAir()) {
                        continue;
                    }
                    float localHardness = local.getDestroySpeed(level, p);
                    boolean material = tool.type.effective != null && local.is(tool.type.effective)
                            || tool.type == ToolType.HAMMER && local.getBlock() instanceof InfestedBlock;
                    if (ToolCoreItem.harvestLevel(local) <= toolLevel && localHardness - 1.5 <= hardness && localHardness >= 0.0F && material) {
                        areaBreak(player, p);
                    }
                }
            }
        }
        level.levelEvent(2001, pos, Block.getId(state));
        return true;
    }

    /**
     * LumberAxe.onBlockStartBreak: a log with more than 3 leaves around the top of its column fells the tree (the same
     * log upwards and sideways, breakTree); otherwise, and on other wood, the wood in the 3x3x3 around (destroyWood).
     */
    private static void lumberAxe(ToolCoreItem tool, ItemStack stack, ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player) {
        if (state.is(BlockTags.LOGS)) {
            int height = pos.getY();
            while (level.getBlockState(new BlockPos(pos.getX(), height + 1, pos.getZ())).is(state.getBlock()) && height < level.getMaxY()) {
                height++;
            }
            int numLeaves = 0;
            if (height - pos.getY() < 50) {
                for (BlockPos p : BlockPos.betweenClosed(pos.getX() - 1, height - 1, pos.getZ() - 1, pos.getX() + 1, height + 1, pos.getZ() + 1)) {
                    if (level.getBlockState(p).is(BlockTags.LEAVES)) {
                        numLeaves++;
                    }
                }
            }
            if (numLeaves > 3) {
                breakTree(tool, stack, level, pos, state.getBlock(), player);
            } else {
                destroyWood(stack, level, pos, player);
            }
            level.levelEvent(2001, pos, Block.getId(state));
        } else if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            destroyWood(stack, level, pos, player);
            level.levelEvent(2001, pos, Block.getId(state));
        }
    }

    /** breakTree: the same log in the 3x3 at this height and above, recursively (the starting block too). */
    private static void breakTree(ToolCoreItem tool, ItemStack stack, ServerLevel level, BlockPos pos, Block log, ServerPlayer player) {
        int toolLevel = InfiTool.of(stack).harvestLevel;
        for (int x = pos.getX() - 1; x <= pos.getX() + 1; x++) {
            for (int y = pos.getY(); y <= pos.getY() + 1; y++) {
                for (int z = pos.getZ() - 1; z <= pos.getZ() + 1; z++) {
                    if (broken(stack)) {
                        continue;
                    }
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState local = level.getBlockState(p);
                    if (local.is(log) && ToolCoreItem.harvestLevel(local) <= toolLevel) {
                        areaBreak(player, p);
                        breakTree(tool, stack, level, p, log, player);
                    }
                }
            }
        }
    }

    /** destroyWood: every wooden block in the 3x3x3 around. */
    private static void destroyWood(ItemStack stack, ServerLevel level, BlockPos pos, ServerPlayer player) {
        int toolLevel = InfiTool.of(stack).harvestLevel;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (broken(stack)) {
                continue;
            }
            BlockState local = level.getBlockState(p);
            if (local.is(BlockTags.MINEABLE_WITH_AXE) && ToolCoreItem.harvestLevel(local) <= toolLevel) {
                areaBreak(player, p.immutable());
            }
        }
    }

    /** Battleaxe.onBlockStartBreak: up to 8 wooden blocks straight above go with the block. */
    private static void battleaxe(ToolCoreItem tool, ItemStack stack, ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player) {
        int toolLevel = InfiTool.of(stack).harvestLevel;
        for (int y = pos.getY() + 1; y < pos.getY() + 9; y++) {
            BlockPos p = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState local = level.getBlockState(p);
            if (broken(stack) || !local.is(BlockTags.MINEABLE_WITH_AXE)) {
                break;
            }
            if (ToolCoreItem.harvestLevel(local) <= toolLevel) {
                areaBreak(player, p);
            }
        }
        level.levelEvent(2001, pos, Block.getId(state));
    }

    /** Scythe.onBlockStartBreak: every plant, leaf, vine, web, cactus and pumpkin in the 3x3x3 around. */
    private static void scythe(ToolCoreItem tool, ItemStack stack, ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (broken(stack) || p.equals(pos)) {
                continue;
            }
            BlockState local = level.getBlockState(p);
            if (!local.isAir() && local.is(SCYTHE)) {
                areaBreak(player, p.immutable());
            }
        }
        level.levelEvent(2001, pos, Block.getId(state));
    }

    /** AbilityHelper.raytraceFromEntity (blocks and fluids, the player's reach). */
    private static BlockHitResult raytrace(Level level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        double reach = player.blockInteractionRange();
        return level.clip(new ClipContext(eye, eye.add(look.scale(reach)), ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
    }
}
