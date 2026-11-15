package bspkrs.treecapitator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * One tree chop, TreeCapitator.java of r14. Runs when a player breaks a registered log (before the log itself is
 * removed, like the ASM hook in ItemInWorldManager.removeBlock). The chopped log is left to the normal block
 * breaking, which also damages the axe once for it; the other logs, then the leaves, are destroyed here.
 */
public final class TreeCapitator {
    private static final int LOG_DECAY_RANGE = 4; // BlockLog.breakBlock: leaves within 4 blocks start to decay

    private final Player player;
    private final TreeDefinition treeDef;
    private final TreeDefinition master;
    private BlockPos startPos;
    private ItemStack axe = ItemStack.EMPTY;
    private ItemStack shears = ItemStack.EMPTY;
    private float currentAxeDamage;
    private float currentShearsDamage;
    private int numLogsBroken = 1;
    private int numLeavesSheared = 1;
    private float logDamageMultiplier;
    private float leafDamageMultiplier;
    /** Logs removed so far (and the chopped one): the leaves around them got the "check decay" bit in 1.5.2. */
    private final List<BlockPos> brokenLogs = new ArrayList<>();

    public TreeCapitator(Player player, TreeDefinition treeDef, TreeDefinition master) {
        this.player = player;
        this.treeDef = treeDef;
        this.master = master;
        this.logDamageMultiplier = TCSettings.DAMAGE_MULTIPLIER.get().floatValue();
        this.leafDamageMultiplier = logDamageMultiplier;
    }

    // ---- static checks shared with the break speed handler ----

    public static boolean isAxe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(TreeCapitatorMod.AXES);
    }

    public static boolean isShears(ItemStack stack) {
        return !stack.isEmpty() && stack.is(TreeCapitatorMod.SHEARS);
    }

    public static boolean isAxeItemEquipped(Player player) {
        return isAxe(player.getMainHandItem());
    }

    public static boolean isBreakingPossible(Player player, boolean shouldLog) {
        ItemStack axe = player.getMainHandItem();
        if (!isAxeItemEquipped(player) && TCSettings.NEED_ITEM.get()) {
            if (shouldLog) {
                TCLog.debug("Player does not have an axe equipped.");
            }
            return false;
        }
        if (!player.isCreative() && TCSettings.ALLOW_ITEM_DAMAGE.get() && !axe.isEmpty() && axe.isDamageableItem()
                && axe.getMaxDamage() - axe.getDamageValue() <= TCSettings.DAMAGE_MULTIPLIER.get() && !TCSettings.ALLOW_MORE_BLOCKS_THAN_DAMAGE.get()) {
            if (shouldLog) {
                TCLog.debug("Chopping disabled due to axe durability.");
            }
            return false;
        }
        return true;
    }

    public static boolean isBreakingEnabled(Player player) {
        String sneakAction = TCSettings.SNEAK_ACTION.get();
        return (sneakAction.equalsIgnoreCase("none") || sneakAction.equalsIgnoreCase("disable") && !player.isShiftKeyDown()
                || sneakAction.equalsIgnoreCase("enable") && player.isShiftKeyDown())
                && (!player.isCreative() || !TCSettings.DISABLE_IN_CREATIVE.get());
    }

    public static int getTreeHeight(TreeDefinition tree, Level level, BlockPos pos, Player player) {
        BlockPos start = pos;
        if (isBreakingPossible(player, false)) {
            if (!tree.onlyDestroyUpwards()) {
                start = getBottomLog(tree, level, start);
            }
            BlockPos topLog = getTopLog(tree, level, pos);
            if (!tree.allowSmartTreeDetection() || !tree.hasLeaves()
                    || hasXLeavesInDist(tree, level, topLog, tree.maxLeafIDDist(), tree.minLeavesToID(), false)) {
                return topLog.getY() - start.getY() + 1;
            }
        }
        return 1;
    }

    private static BlockPos getTopLog(TreeDefinition tree, Level level, BlockPos pos) {
        while (tree.isLogBlock(level.getBlockState(pos.above()))) {
            pos = pos.above();
        }
        return pos;
    }

    private static BlockPos getBottomLog(TreeDefinition tree, Level level, BlockPos pos) {
        while (tree.isLogBlock(level.getBlockState(pos.below()))) {
            pos = pos.below();
        }
        return pos;
    }

    private static boolean hasXLeavesInDist(TreeDefinition tree, Level level, BlockPos pos, int range, int limit, boolean shouldLog) {
        if (shouldLog) {
            TCLog.debug("Attempting to identify tree...");
        }
        int i = 0;
        for (int x = -range; x <= range; x++) {
            for (int y = -1; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        BlockState state = level.getBlockState(pos.offset(x, y, z));
                        if (!state.isAir() && tree.isLeafBlock(state)) {
                            if (shouldLog) {
                                TCLog.debug("Found leaf block: %s", state);
                            }
                            if (++i >= limit) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        if (shouldLog) {
            TCLog.debug("Number of leaf blocks is less than the limit. Found: %s", i);
        }
        return false;
    }

    // ---- the chop ----

    public void onBlockHarvested(ServerLevel level, BlockPos pos, BlockState startState) {
        TCLog.debug("In TreeCapitator.onBlockHarvested() %s", pos);
        startPos = pos.immutable();
        brokenLogs.add(startPos);
        if (!isBreakingEnabled(player)) {
            TCLog.debug("Tree Chopping is disabled due to player state or gamemode.");
            return;
        }
        BlockPos topLog = getTopLog(treeDef, level, startPos);
        if (treeDef.allowSmartTreeDetection() && treeDef.hasLeaves()
                && !hasXLeavesInDist(treeDef, level, topLog, treeDef.maxLeafIDDist(), treeDef.minLeavesToID(), true)) {
            TCLog.debug("Could not identify tree.");
            return;
        }
        if (!checkAxe() && TCSettings.NEED_ITEM.get()) {
            TCLog.debug("Axe item is not equipped.");
            return;
        }
        TCLog.debug("Proceeding to chop tree...");
        List<BlockPos> listFinal = new ArrayList<>();
        List<BlockPos> logs = addLogs(level, startPos);
        addLogsAbove(level, startPos, listFinal);
        TCLog.debug("Destroying %d log blocks...", logs.size());
        destroyBlocks(level, logs, false);
        if (numLogsBroken > 1) {
            TCLog.debug("Number of logs broken: %d", numLogsBroken);
        }
        if (TCSettings.DESTROY_LEAVES.get() && treeDef.hasLeaves()) {
            List<BlockPos> leaves = new ArrayList<>();
            Set<BlockPos> seen = new HashSet<>();
            for (BlockPos top : listFinal) {
                addLeaves(level, top, leaves, seen);
            }
            TCLog.debug("Destroying %d leaf blocks...", leaves.size());
            destroyBlocks(level, leaves, hasShearsInHotbar());
            if (numLeavesSheared > 1) {
                TCLog.debug("Number of leaves sheared: %d", numLeavesSheared);
            }
        }
        // the rest of the rounded damage (only with damage multipliers that are not whole numbers)
        if (currentAxeDamage > 0.0F && !axe.isEmpty()) {
            currentAxeDamage = Math.round(currentAxeDamage);
            for (int i = 0; i < (int) Math.floor(currentAxeDamage) && !axe.isEmpty(); i++) {
                axe.mineBlock(level, startState, startPos, player);
            }
        }
        if (currentShearsDamage > 0.0F && !shears.isEmpty()) {
            currentShearsDamage = Math.round(currentShearsDamage);
            for (int i = 0; i < (int) Math.floor(currentShearsDamage) && !shears.isEmpty(); i++) {
                damageShears(level);
            }
        }
    }

    private boolean checkAxe() {
        ItemStack item = player.getMainHandItem();
        axe = isAxe(item) ? item : ItemStack.EMPTY;
        return !axe.isEmpty();
    }

    private boolean hasShearsInHotbar() {
        return shearsHotbarIndex() != -1;
    }

    private int shearsHotbarIndex() {
        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (!item.isEmpty() && isShears(item)) {
                shears = item;
                return i;
            }
        }
        shears = ItemStack.EMPTY;
        return -1;
    }

    private boolean isVine(BlockState state) {
        return state.is(Blocks.VINE);
    }

    private void destroyBlocks(ServerLevel level, List<BlockPos> list, boolean canShear) {
        boolean creative = player.isCreative();
        boolean noDrops = creative && TCSettings.DISABLE_CREATIVE_DROPS.get();
        while (!list.isEmpty()) {
            BlockPos pos = list.remove(0);
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            boolean vine = isVine(state);
            boolean leaf = treeDef.isLeafBlock(state);
            if (pos.equals(startPos)) {
                continue; // removed by the normal block breaking right after this
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if ((vine && TCSettings.SHEAR_VINES.get() || leaf && TCSettings.SHEAR_LEAVES.get()) && canShear && !noDrops) {
                // sheared: the block itself (ItemStack(id, 1, damageDropped(meta)))
                ItemEntity item = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(state.getBlock()));
                item.setDefaultPickUpDelay();
                level.addFreshEntity(item);
                if (TCSettings.ALLOW_ITEM_DAMAGE.get() && !creative && !shears.isEmpty()) {
                    canShear = damageShearsAndContinue(level);
                    numLeavesSheared++;
                    if (canShear && TCSettings.USE_INCREASING_ITEM_DAMAGE.get() && numLeavesSheared % TCSettings.INCREASE_DAMAGE_EVERY_X_BLOCKS.get() == 0) {
                        leafDamageMultiplier += TCSettings.DAMAGE_INCREASE_AMOUNT.get().floatValue();
                    }
                }
            } else if (!noDrops) {
                Block.dropResources(state, level, pos, blockEntity, player, axe.isEmpty() ? player.getMainHandItem() : axe);
                if (TCSettings.ALLOW_ITEM_DAMAGE.get() && !creative && !axe.isEmpty() && !vine && !leaf) {
                    if (!damageAxeAndContinue(level, state)) {
                        list.clear();
                    }
                    numLogsBroken++;
                    if (TCSettings.USE_INCREASING_ITEM_DAMAGE.get() && numLogsBroken % TCSettings.INCREASE_DAMAGE_EVERY_X_BLOCKS.get() == 0) {
                        logDamageMultiplier += TCSettings.DAMAGE_INCREASE_AMOUNT.get().floatValue();
                    }
                }
            }
            level.setBlock(pos, level.getFluidState(pos).createLegacyBlock(), Block.UPDATE_ALL);
            if (master.isLogBlock(state)) {
                brokenLogs.add(pos);
            }
        }
    }

    private boolean damageAxeAndContinue(ServerLevel level, BlockState state) {
        if (!axe.isEmpty()) {
            currentAxeDamage += logDamageMultiplier;
            for (int i = 0; i < (int) Math.floor(currentAxeDamage) && !axe.isEmpty(); i++) {
                axe.mineBlock(level, state, startPos, player);
            }
            currentAxeDamage = (float) (currentAxeDamage - Math.floor(currentAxeDamage));
        }
        return !TCSettings.NEED_ITEM.get() || TCSettings.ALLOW_MORE_BLOCKS_THAN_DAMAGE.get() || checkAxe();
    }

    private boolean damageShearsAndContinue(ServerLevel level) {
        if (!shears.isEmpty()) {
            shearsHotbarIndex();
            currentShearsDamage += leafDamageMultiplier;
            for (int i = 0; i < Math.floor(currentShearsDamage) && !shears.isEmpty(); i++) {
                damageShears(level);
            }
            currentShearsDamage = (float) (currentShearsDamage - Math.floor(currentShearsDamage));
        }
        return TCSettings.ALLOW_MORE_BLOCKS_THAN_DAMAGE.get() || hasShearsInHotbar();
    }

    private void damageShears(ServerLevel level) {
        if (player instanceof ServerPlayer sp) {
            shears.hurtAndBreak(1, level, sp, item -> {
            });
        }
    }

    private List<BlockPos> addLogs(Level level, BlockPos pos) {
        int lowY = pos.getY();
        List<BlockPos> list = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        list.add(pos);
        seen.add(pos);
        int index = 0;
        do {
            BlockPos current = list.get(index);
            for (int x = -1; x <= 1; x++) {
                for (int y = treeDef.onlyDestroyUpwards() ? 0 : -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos newPos = current.offset(x, y, z);
                        if (treeDef.isLogBlock(level.getBlockState(newPos))) {
                            int hor = treeDef.maxHorLogBreakDist();
                            int ver = treeDef.maxVerLogBreakDist();
                            // r14 skipped the remaining checks when maxHorLogBreakDist was -1 (and then never ended); here they always apply
                            if ((hor == -1 || Math.abs(newPos.getX() - startPos.getX()) <= hor && Math.abs(newPos.getZ() - startPos.getZ()) <= hor)
                                    && (ver == -1 || Math.abs(newPos.getY() - startPos.getY()) <= ver) && !seen.contains(newPos)
                                    && (newPos.getY() >= lowY || !treeDef.onlyDestroyUpwards())) {
                                list.add(newPos);
                                seen.add(newPos);
                            }
                        }
                    }
                }
            }
        } while (++index < list.size());
        return list;
    }

    /** Collects the top logs of every branch (logs with no log above them). */
    private void addLogsAbove(Level level, BlockPos position, List<BlockPos> listFinal) {
        List<BlockPos> listAbove = new ArrayList<>();
        listAbove.add(position);
        do {
            List<BlockPos> list = listAbove;
            listAbove = new ArrayList<>();
            Set<BlockPos> seen = new HashSet<>();
            for (BlockPos pos : list) {
                int counter = 0;
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos newPos = pos.offset(x, 1, z);
                        if (treeDef.isLogBlock(level.getBlockState(newPos))) {
                            if (seen.add(newPos)) {
                                listAbove.add(newPos);
                            }
                            counter++;
                        }
                    }
                }
                if (counter == 0) {
                    listFinal.add(pos);
                }
            }
            int index = -1;
            while (++index < listAbove.size()) {
                BlockPos pos = listAbove.get(index);
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos newPos = pos.offset(x, 0, z);
                        if (treeDef.isLogBlock(level.getBlockState(newPos)) && seen.add(newPos)) {
                            listAbove.add(newPos);
                        }
                    }
                }
            }
        } while (!listAbove.isEmpty());
    }

    private void addLeaves(Level level, BlockPos pos, List<BlockPos> list, Set<BlockPos> seen) {
        int index = list.size() - 1;
        addLeavesInDistance(level, pos, treeDef.maxHorLeafBreakDist(), list, seen);
        while (++index < list.size()) {
            BlockPos pos2 = list.get(index);
            if (horSquaredDistance(pos, pos2) < treeDef.maxHorLeafBreakDist()) {
                addLeavesInDistance(level, pos2, 1, list, seen);
            }
        }
    }

    /** CommonUtils.getHorSquaredDistance (despite the name it is the plain horizontal distance). */
    private static double horSquaredDistance(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private void addLeavesInDistance(Level level, BlockPos pos, int range, List<BlockPos> list, Set<BlockPos> seen) {
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos newPos = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(newPos);
                    if (treeDef.isLeafBlock(state) || isVine(state)) {
                        if (!treeDef.requireLeafDecayCheck() || wouldDecay(newPos, state)) {
                            if (!seen.contains(newPos) && !hasLogClose(level, newPos, 1)) {
                                list.add(newPos);
                                seen.add(newPos);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The decay check of 1.5.2: metadata bit 8 ("check decay", set by every broken log on the leaves within 4 blocks)
     * and not bit 4 (placed by a player). Bit 4 is the "persistent" property now. For vines the same bits were
     * attachment sides (8 = east, 4 = north), so the original test is kept for them as it was.
     */
    private boolean wouldDecay(BlockPos pos, BlockState state) {
        if (isVine(state)) {
            return state.getValue(VineBlock.EAST) && !state.getValue(VineBlock.NORTH);
        }
        if (state.hasProperty(BlockStateProperties.PERSISTENT) && state.getValue(BlockStateProperties.PERSISTENT)) {
            return false;
        }
        for (BlockPos log : brokenLogs) {
            if (Math.abs(log.getX() - pos.getX()) <= LOG_DECAY_RANGE && Math.abs(log.getY() - pos.getY()) <= LOG_DECAY_RANGE
                    && Math.abs(log.getZ() - pos.getZ()) <= LOG_DECAY_RANGE) {
                return true;
            }
        }
        return false;
    }

    private boolean hasLogClose(Level level, BlockPos pos, int i) {
        for (int x = -i; x <= i; x++) {
            for (int y = -i; y <= i; y++) {
                for (int z = -i; z <= i; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        BlockPos neighbor = pos.offset(x, y, z);
                        if (!neighbor.equals(startPos) && master.isLogBlock(level.getBlockState(neighbor))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
