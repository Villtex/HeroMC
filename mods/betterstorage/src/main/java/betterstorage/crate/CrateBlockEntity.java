package betterstorage.crate;

import betterstorage.BS;
import betterstorage.BSUtil;
import betterstorage.menu.CrateMenu;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityCrate: one crate of a pile. A new crate joins the pile next to it if that pile can take it (no crate
 * above another pile's crate, the pile keeps roughly a box shape, at most 8192 crates); if it can't it breaks again
 * and drops. Removing a crate drops what no longer fits, breaks the crate above if it belonged to the same pile and
 * splits the pile if it fell apart (the new pile gets its share of the items). Only the server knows the piles.
 */
public class CrateBlockEntity extends BlockEntity {
    private static final Direction[] SIDES = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    public static final int SLOTS_PER_CRATE = 18;
    private @Nullable CratePileData data;
    private int id = -1;
    private @Nullable Component customTitle;

    public CrateBlockEntity(BlockPos pos, BlockState state) {
        super(BS.CRATE_ENTITY.get(), pos, state);
    }

    private @Nullable CrateBlockEntity crateAt(BlockPos pos) {
        return this.level != null && this.level.getBlockEntity(pos) instanceof CrateBlockEntity crate ? crate : null;
    }

    private boolean isCrate(BlockPos pos) {
        return this.level != null && this.level.getBlockState(pos).is(BS.CRATE.get());
    }

    public @Nullable CratePileData getPileData() {
        if (!(this.level instanceof ServerLevel server)) {
            return null;
        }
        if (this.data == null) {
            CratePileCollection collection = CratePileCollection.get(server);
            if (this.id == -1) {
                this.setPileData(collection.createCratePile(), true);
            } else {
                this.setPileData(collection.getCratePile(this.id), false);
            }
        }
        return this.data;
    }

    private void setPileData(@Nullable CratePileData data, boolean addCrate) {
        if (this.data != null) {
            this.data.removeCrate(this.worldPosition);
        }
        this.data = data;
        if (data != null) {
            this.id = data.id;
            if (addCrate) {
                data.addCrate(this.worldPosition);
            }
        } else {
            this.id = -1;
        }
        this.setChanged();
    }

    /** updateEntity: until it has a pile it looks for one next to it, or starts its own. */
    public void tick() {
        if (this.level != null && !this.level.isClientSide() && this.data == null && (this.id != -1 || !this.findNeighborCratePile())
                && !this.isRemoved()) {
            this.getPileData();
        }
    }

    /** invalidate: the crate was removed. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (!(this.level instanceof ServerLevel) || this.data == null && this.id == -1) {
            return;
        }
        CratePileData data = this.getPileData();
        this.setPileData(null, false);
        if (data != null) {
            this.dropOverflowContents(data);
            this.checkPileConnections(data);
        }
    }

    private void checkPileConnections(CratePileData data) {
        BlockPos pos = this.worldPosition;
        CrateBlockEntity crateAbove = this.crateAt(pos.above());
        if (crateAbove != null && crateAbove.data == data) {
            crateAbove.destroyCrate();
        }
        if (data.getNumCrates() <= 0 || this.isCrate(pos.below())) {
            return;
        }
        List<Set<BlockPos>> crateSets = new ArrayList<>();
        int checkedChecks = 0;
        outer:
        for (Direction dir : SIDES) {
            BlockPos n = pos.relative(dir);
            if (this.crateAt(n) == null) {
                continue;
            }
            for (Set<BlockPos> set : crateSets) {
                if (set.contains(n)) {
                    continue outer;
                }
            }
            Set<BlockPos> set = new HashSet<>();
            set.add(n);
            for (Direction ndir : SIDES) {
                this.checkConnections(n.relative(ndir), set);
            }
            crateSets.add(set);
            checkedChecks += set.size();
            if (checkedChecks == data.getNumCrates()) {
                break;
            }
        }
        if (crateSets.isEmpty() || data.collection == null) {
            return;
        }
        for (int i = 1; i < crateSets.size(); i++) {
            Set<BlockPos> set = crateSets.get(i);
            CratePileData newPileData = data.collection.createCratePile();
            int numCrates = set.size();
            for (BlockPos p : set) {
                CrateBlockEntity crate = this.crateAt(p);
                if (crate == null) {
                    continue;
                }
                crate.setPileData(newPileData, true);
                for (BlockPos up = p.above(); (crate = this.crateAt(up)) != null; up = up.above()) {
                    crate.setPileData(newPileData, true);
                    numCrates++;
                }
            }
            int count = numCrates * data.getOccupiedSlots() / (data.getNumCrates() + numCrates);
            List<ItemStack> stacks = data.pickAndRemoveItemStacks(count);
            // the original lost what did not fit into the new pile; it falls out here
            this.dropItems(newPileData.addItems(stacks));
        }
        data.trimMap();
    }

    /** checkConnections: all crates connected to {@code start} in this layer (flood fill, without recursion). */
    private void checkConnections(BlockPos start, Set<BlockPos> set) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            if (set.contains(p) || this.crateAt(p) == null) {
                continue;
            }
            set.add(p);
            for (Direction ndir : SIDES) {
                queue.add(p.relative(ndir));
            }
        }
    }

    private boolean findNeighborCratePile() {
        CratePileData pileData = null;
        boolean crateBelow = false;
        for (Direction dir : Direction.values()) {
            BlockPos p = this.worldPosition.relative(dir);
            CrateBlockEntity checked = this.crateAt(p);
            if (checked == null) {
                continue;
            }
            CratePileData checkedPileData = checked.getPileData();
            if (checkedPileData == null || pileData != null && checkedPileData.id != pileData.id
                    || dir != Direction.DOWN && !crateBelow && this.isCrate(p.below()) || dir == Direction.UP
                    || !checkedPileData.canAdd(this.worldPosition)) {
                this.destroyCrate();
                return false;
            }
            if (dir == Direction.DOWN) {
                crateBelow = true;
            }
            pileData = checkedPileData;
        }
        if (pileData != null) {
            this.setPileData(pileData, true);
        }
        return pileData != null;
    }

    private void destroyCrate() {
        if (this.level != null) {
            this.level.removeBlock(this.worldPosition, false);
            this.dropItem(new ItemStack(BS.CRATE_ITEM.get()));
        }
    }

    private void dropItem(ItemStack stack) {
        if (this.level != null) {
            BSUtil.dropFromBlock(this.level, this.worldPosition, stack);
        }
    }

    private void dropItems(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            this.dropItem(stack);
        }
    }

    private void dropOverflowContents(CratePileData data) {
        this.dropItems(data.pickAndRemoveItemStacks(-data.getFreeSlots()));
    }

    public Component getContainerTitle() {
        return this.customTitle != null ? this.customTitle : Component.translatable("container.betterstorage.crate");
    }

    public boolean canPlayerUseContainer(net.minecraft.world.entity.player.Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    /** openGui: a random view of the pile, two rows per crate up to six. */
    public void openGui(ServerPlayer player) {
        CratePileData data = this.getPileData();
        if (data != null && data.getNumCrates() > 0 && this.canPlayerUseContainer(player)) {
            CrateMenu.open(player, this.getContainerTitle(), new CratePlayerView(this, data));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        Component name = components.get(DataComponents.CUSTOM_NAME);
        if (name != null) {
            this.customTitle = name;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.id = input.getIntOr("crateId", -1);
        this.customTitle = parseCustomNameSafe(input, "CustomName");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("crateId", this.id);
        output.storeNullable("CustomName", ComponentSerialization.CODEC, this.customTitle);
    }
}
