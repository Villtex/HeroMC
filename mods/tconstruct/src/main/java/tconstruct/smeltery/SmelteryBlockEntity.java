package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;
import tconstruct.TConstruct;
import tconstruct.TSmeltery;

import java.util.ArrayList;
import java.util.List;

/**
 * SmelteryLogic: the controller of a smeltery. It finds its structure (a 3x3 hollow over a 3x3 floor of seared bricks,
 * walls of 12 seared blocks per layer with at least one seared tank on the controller's layer), takes 9 slots per
 * layer, burns lava from the tanks (150 mB at a time, 3 per second while something melts), heats each slot by one
 * degree every 4 ticks up to the melting point of its item (internal temperature 800) and melts it into the molten
 * metal (20000 mB per layer), mixing alloys right away. Every three seconds it hurts what stands inside and melts
 * villagers into liquid emerald and endermen into liquid ender.
 */
public class SmelteryBlockEntity extends BlockEntity implements Container, MasterLogic {
    public boolean validStructure;
    private boolean tempValidStructure;
    private int internalTemp;
    public int useTime;
    public int fuelGague;
    public int fuelAmount;
    private boolean inUse;
    private final List<BlockPos> lavaTanks = new ArrayList<>();
    private @Nullable BlockPos activeLavaTank;
    public @Nullable BlockPos centerPos;
    public int[] activeTemps = new int[0];
    public int[] meltingTemps = new int[0];
    private int tick;
    public final List<FluidStack> moltenMetal = new ArrayList<>();
    private int maxLiquid;
    private int currentLiquid;
    public int layers;
    private int numBricks;
    private boolean needsUpdate;
    private NonNullList<ItemStack> inventory = NonNullList.create();

    /** For the menu: 0 fuel gauge, 1 fuel amount, then active and melting temperature of each slot. */
    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            SmelteryBlockEntity s = SmelteryBlockEntity.this;
            if (index == 0) {
                return s.fuelGague;
            }
            if (index == 1) {
                return s.fuelAmount;
            }
            int slot = (index - 2) / 2;
            int[] temps = (index - 2) % 2 == 0 ? s.activeTemps : s.meltingTemps;
            return slot < temps.length ? temps[slot] : 20;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2 + 2 * SmelteryBlockEntity.this.inventory.size();
        }
    };

    public SmelteryBlockEntity(BlockPos pos, BlockState state) {
        super(TSmeltery.SMELTERY_ENTITY.get(), pos, state);
    }

    public Direction facing() {
        return this.getBlockState().getValue(SmelteryControllerBlock.FACING);
    }

    /** adjustLayers: 9 slots per layer, 20000 mB per layer; items of removed slots fly out of the front. */
    private void adjustLayers(int lay, boolean forceAdjust) {
        if (lay == this.layers && !forceAdjust) {
            return;
        }
        this.needsUpdate = true;
        this.layers = lay;
        this.maxLiquid = 20000 * lay;
        int[] tempActive = this.activeTemps;
        this.activeTemps = new int[9 * lay];
        System.arraycopy(tempActive, 0, this.activeTemps, 0, Math.min(tempActive.length, this.activeTemps.length));
        int[] tempMelting = this.meltingTemps;
        this.meltingTemps = new int[9 * lay];
        System.arraycopy(tempMelting, 0, this.meltingTemps, 0, Math.min(tempMelting.length, this.meltingTemps.length));
        NonNullList<ItemStack> tempInv = this.inventory;
        this.inventory = NonNullList.withSize(9 * lay, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(tempInv.size(), this.inventory.size()); i++) {
            this.inventory.set(i, tempInv.get(i));
        }
        if (this.activeTemps.length > tempActive.length) {
            for (int i = tempActive.length; i < this.activeTemps.length; i++) {
                this.activeTemps[i] = 20;
                this.meltingTemps[i] = 20;
            }
        }
        if (tempInv.size() > this.inventory.size() && this.level != null) {
            RandomSource rand = this.level.getRandom();
            Direction face = this.facing();
            for (int i = this.inventory.size(); i < tempInv.size(); i++) {
                ItemStack stack = tempInv.get(i);
                while (!stack.isEmpty()) {
                    ItemStack part = stack.split(rand.nextInt(21) + 10);
                    ItemEntity item = new ItemEntity(this.level, this.worldPosition.getX() + rand.nextFloat() * 0.8F + 0.1F + face.getStepX(),
                            this.worldPosition.getY() + rand.nextFloat() * 0.8F + 0.1F,
                            this.worldPosition.getZ() + rand.nextFloat() * 0.8F + 0.1F + face.getStepZ(), part);
                    item.setDeltaMovement(rand.nextGaussian() * 0.05, rand.nextGaussian() * 0.05 + 0.2, rand.nextGaussian() * 0.05);
                    this.level.addFreshEntity(item);
                }
            }
        }
    }

    public int getScaledFuelGague(int scale) {
        return Math.max(1, this.fuelGague * scale / 52);
    }

    public int getTempForSlot(int slot) {
        return this.activeTemps[slot];
    }

    public int getMeltingPointForSlot(int slot) {
        return this.meltingTemps[slot];
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SmelteryBlockEntity smeltery) {
        smeltery.updateEntity();
    }

    private void updateEntity() {
        this.tick++;
        if (this.tick % 4 == 0) {
            this.heatItems();
        }
        if (this.tick % 20 == 0) {
            if (!this.validStructure) {
                this.checkValidPlacement();
            }
            if (this.useTime > 0 && this.inUse) {
                this.useTime -= 3;
            }
            if (this.validStructure && this.useTime <= 0) {
                this.updateFuelGague();
            }
            if (this.needsUpdate) {
                this.needsUpdate = false;
                this.sync();
            }
        }
        if (this.tick == 60) {
            this.tick = 0;
            if (this.validStructure) {
                this.detectEntities();
            }
        }
    }

    private void detectEntities() {
        if (this.centerPos == null || !(this.level instanceof ServerLevel server)) {
            return;
        }
        AABB box = new AABB(this.centerPos).inflate(1.0, 0.0, 1.0);
        for (Entity entity : server.getEntitiesOfClass(Entity.class, box)) {
            if (entity instanceof Villager villager) {
                if (villager.hurtServer(server, damage(server), 1.0F) && this.currentLiquid + 8 < this.maxLiquid) {
                    this.addMoltenMetal(new FluidStack(TFluids.fluid(TFluids.EMERALD), villager.isBaby() ? 2 : 8), false);
                }
            } else if (entity instanceof Enderman enderman) {
                if (enderman.hurtServer(server, damage(server), 1.0F) && this.currentLiquid + 25 < this.maxLiquid) {
                    this.addMoltenMetal(new FluidStack(TFluids.fluid(TFluids.ENDER), 25), false);
                }
            } else if (entity instanceof LivingEntity living) {
                living.hurtServer(server, damage(server), 1.0F);
            }
        }
    }

    /** SmelteryDamageSource: one of four death messages. */
    private static DamageSource damage(ServerLevel level) {
        int which = level.getRandom().nextInt(4) + 1;
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, TConstruct.id("smeltery_" + which));
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key));
    }

    private void heatItems() {
        if (this.useTime <= 0) {
            return;
        }
        boolean hasUse = false;
        for (int i = 0; i < 9 * this.layers; i++) {
            if (this.meltingTemps[i] > 20 && !this.inventory.get(i).isEmpty()) {
                hasUse = true;
                if (this.activeTemps[i] < this.internalTemp && this.activeTemps[i] < this.meltingTemps[i]) {
                    this.activeTemps[i]++;
                } else if (this.activeTemps[i] >= this.meltingTemps[i]) {
                    FluidStack result = SmelteryRecipes.result(this.inventory.get(i));
                    if (!result.isEmpty() && this.addMoltenMetal(result, false)) {
                        this.inventory.set(i, ItemStack.EMPTY);
                        this.activeTemps[i] = 20;
                        for (FluidStack alloy : SmelteryRecipes.mixMetals(this.moltenMetal)) {
                            this.addMoltenMetal(alloy, true);
                        }
                        this.setChanged();
                        this.updateTemperatures();
                    }
                }
            } else {
                this.activeTemps[i] = 20;
            }
        }
        this.inUse = hasUse;
    }

    /** addMoltenMetal: the first liquid always fits; alloys go to the bottom ({@code first}). */
    private boolean addMoltenMetal(FluidStack liquid, boolean first) {
        this.needsUpdate = true;
        if (this.moltenMetal.isEmpty()) {
            this.moltenMetal.add(liquid.copy());
            this.currentLiquid += liquid.getAmount();
            return true;
        }
        if (liquid.getAmount() + this.currentLiquid > this.maxLiquid) {
            return false;
        }
        this.currentLiquid += liquid.getAmount();
        boolean added = false;
        for (int i = 0; i < this.moltenMetal.size(); i++) {
            FluidStack l = this.moltenMetal.get(i);
            if (FluidStack.isSameFluid(l, liquid)) {
                l.grow(liquid.getAmount());
                added = true;
            }
            if (l.getAmount() <= 0) {
                this.moltenMetal.remove(i);
                i--;
            }
        }
        if (!added) {
            if (first) {
                this.moltenMetal.add(0, liquid.copy());
            } else {
                this.moltenMetal.add(liquid.copy());
            }
        }
        return true;
    }

    private void updateTemperatures() {
        this.inUse = true;
        for (int i = 0; i < 9 * this.layers; i++) {
            this.meltingTemps[i] = SmelteryRecipes.temperature(this.inventory.get(i));
        }
    }

    /** updateFuelDisplay (when the GUI opens). */
    public void updateFuelDisplay() {
        if (this.activeLavaTank != null && this.useTime <= 0 && this.level != null) {
            if (this.level.isLoaded(this.activeLavaTank) && this.level.getBlockEntity(this.activeLavaTank) instanceof TankBlockEntity tank) {
                this.needsUpdate = true;
                FluidStack liquid = tank.drain(Direction.DOWN, 150, false);
                if (!liquid.isEmpty() && liquid.getFluid() == Fluids.LAVA) {
                    this.fuelAmount = liquid.getAmount();
                    this.fuelGague = liquid.getAmount() * 52 / tank.capacity();
                    return;
                }
            }
            this.fuelAmount = 0;
            this.fuelGague = 0;
        }
    }

    /** updateFuelGague: takes 150 mB of lava from the active tank, else looks for another tank with lava. */
    private void updateFuelGague() {
        if (this.activeLavaTank == null || this.useTime > 0 || this.level == null) {
            return;
        }
        if (!this.level.isLoaded(this.activeLavaTank)) {
            this.fuelAmount = 0;
            this.fuelGague = 0;
            return;
        }
        BlockEntity container = this.level.getBlockEntity(this.activeLavaTank);
        if (!(container instanceof TankBlockEntity tank)) {
            this.fuelAmount = 0;
            this.fuelGague = 0;
            return;
        }
        this.needsUpdate = true;
        FluidStack liquid = tank.drain(Direction.DOWN, 150, false);
        if (!liquid.isEmpty() && liquid.getFluid() == Fluids.LAVA) {
            liquid = tank.drain(Direction.DOWN, 150, true);
            this.useTime += liquid.getAmount();
            this.showFuel(tank);
        } else {
            for (BlockPos possibleTank : this.lavaTanks) {
                if (this.level.getBlockEntity(possibleTank) instanceof TankBlockEntity newTank) {
                    FluidStack newLiquid = newTank.drain(null, 150, false);
                    if (!newLiquid.isEmpty() && newLiquid.getFluid() == Fluids.LAVA) {
                        this.activeLavaTank = possibleTank;
                        this.showFuel(newTank);
                        break;
                    }
                }
            }
        }
    }

    private void showFuel(TankBlockEntity tank) {
        FluidStack left = tank.liquid();
        if (!left.isEmpty()) {
            this.fuelAmount = left.getAmount();
            this.fuelGague = left.getAmount() * 52 / tank.capacity();
        } else {
            this.fuelAmount = 0;
            this.fuelGague = 0;
        }
    }

    @Override
    public void notifyChange(BlockPos pos) {
        this.checkValidPlacement();
    }

    /** checkValidPlacement: the hollow begins two blocks behind the front. */
    public void checkValidPlacement() {
        BlockPos start = this.worldPosition.relative(this.facing().getOpposite(), 2);
        this.alignInitialPlacement(start);
    }

    /** alignInitialPlacement: with the controller's block next to the start, the center is one further. */
    private void alignInitialPlacement(BlockPos p) {
        boolean north = !this.isAir(p.south());
        boolean south = !this.isAir(p.north());
        boolean east = !this.isAir(p.east());
        boolean west = !this.isAir(p.west());
        if (!north && !south && !east && !west) {
            this.checkValidStructure(p);
        } else if (north && !south && !east && !west) {
            this.checkValidStructure(p.north());
        } else if (!north && south && !east && !west) {
            this.checkValidStructure(p.south());
        } else if (!north && !south && east && !west) {
            this.checkValidStructure(p.west());
        } else if (!north && !south && !east && west) {
            this.checkValidStructure(p.east());
        }
    }

    private boolean isAir(BlockPos pos) {
        return this.level.getBlockState(pos).isAir();
    }

    private void checkValidStructure(BlockPos center) {
        int checkLayers = 0;
        this.tempValidStructure = false;
        if (this.checkSameLevel(center)) {
            checkLayers = 1 + this.recurseStructureUp(center.above(), 0);
            checkLayers += this.recurseStructureDown(center.below(), 0);
        }
        if (this.tempValidStructure != this.validStructure || checkLayers != this.layers) {
            if (this.tempValidStructure) {
                this.internalTemp = 800;
                this.activeLavaTank = this.lavaTanks.get(0);
                this.adjustLayers(checkLayers, false);
                this.validStructure = true;
            } else {
                this.internalTemp = 20;
                this.validStructure = false;
            }
            BlockState state = this.getBlockState();
            if (state.getValue(SmelteryControllerBlock.LIT) != this.validStructure) {
                this.level.setBlock(this.worldPosition, state.setValue(SmelteryControllerBlock.LIT, this.validStructure), Block.UPDATE_ALL);
            }
            this.sync();
        }
    }

    private boolean hollow(BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (!this.isAir(center.offset(x, 0, z))) {
                    return false;
                }
            }
        }
        return true;
    }

    private int ring(BlockPos c) {
        int bricks = 0;
        for (int x = -1; x <= 1; x++) {
            bricks += this.checkBricks(c.offset(x, 0, -2));
            bricks += this.checkBricks(c.offset(x, 0, 2));
        }
        for (int z = -1; z <= 1; z++) {
            bricks += this.checkBricks(c.offset(-2, 0, z));
            bricks += this.checkBricks(c.offset(2, 0, z));
        }
        return bricks;
    }

    private boolean checkSameLevel(BlockPos center) {
        this.numBricks = 0;
        this.lavaTanks.clear();
        if (!this.hollow(center)) {
            return false;
        }
        this.numBricks = this.ring(center);
        return this.numBricks == 12 && !this.lavaTanks.isEmpty();
    }

    private int recurseStructureUp(BlockPos center, int count) {
        this.numBricks = 0;
        if (!this.hollow(center)) {
            return count;
        }
        this.numBricks = this.ring(center);
        return this.numBricks != 12 ? count : this.recurseStructureUp(center.above(), count + 1);
    }

    private int recurseStructureDown(BlockPos center, int count) {
        this.numBricks = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockState state = this.level.getBlockState(center.offset(x, 0, z));
                if (!state.isAir()) {
                    if (state.getBlock() instanceof SearedBrickBlock) {
                        return this.validateBottom(center, count);
                    }
                    return count;
                }
            }
        }
        this.numBricks = this.ring(center);
        if (this.numBricks != 12) {
            return count;
        }
        return this.recurseStructureDown(center.below(), count + 1);
    }

    /** validateBottom: 9 seared blocks (metadata 2 and up: not the controller or a drain). */
    private int validateBottom(BlockPos center, int count) {
        int bottomBricks = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = this.level.getBlockState(center.offset(x, 0, z)).getBlock();
                if (block instanceof SearedBrickBlock && !(block instanceof SmelteryControllerBlock) && !(block instanceof SmelteryDrainBlock)) {
                    bottomBricks++;
                }
            }
        }
        if (bottomBricks == 9) {
            this.tempValidStructure = true;
            this.centerPos = center.above();
        }
        return count;
    }

    private int checkBricks(BlockPos pos) {
        Block block = this.level.getBlockState(pos).getBlock();
        if (!(block instanceof SearedBrickBlock) && !(block instanceof SearedTankBlock)) {
            return 0;
        }
        BlockEntity entity = this.level.getBlockEntity(pos);
        int bricks = 0;
        if (entity == this) {
            bricks++;
        } else if (entity instanceof ServantBlockEntity servant) {
            if (servant.hasValidMaster()) {
                if (servant.verifyMaster(this.worldPosition)) {
                    bricks++;
                }
            } else if (servant.setMaster(this.worldPosition)) {
                bricks++;
            }
            if (entity instanceof TankBlockEntity) {
                this.lavaTanks.add(pos.immutable());
            }
        }
        return bricks;
    }

    public int getCapacity() {
        return this.maxLiquid;
    }

    public int getTotalLiquid() {
        return this.currentLiquid;
    }

    /** drain: from the bottom liquid. */
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (this.moltenMetal.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack liquid = this.moltenMetal.get(0);
        if (liquid.getAmount() - maxDrain <= 0) {
            FluidStack copy = liquid.copy();
            if (doDrain) {
                this.moltenMetal.remove(0);
                this.currentLiquid = 0;
                this.needsUpdate = true;
                this.sync();
            }
            return copy;
        }
        if (doDrain) {
            liquid.shrink(maxDrain);
            this.currentLiquid -= maxDrain;
            this.needsUpdate = true;
            this.sync();
        }
        return liquid.copyWithAmount(maxDrain);
    }

    public int fill(FluidStack resource, boolean doFill) {
        if (resource.isEmpty() || resource.getAmount() + this.currentLiquid >= this.maxLiquid) {
            return 0;
        }
        int amount = resource.getAmount();
        if (doFill) {
            if (this.addMoltenMetal(resource, false)) {
                for (FluidStack alloy : SmelteryRecipes.mixMetals(this.moltenMetal)) {
                    this.addMoltenMetal(alloy, true);
                }
            }
            this.needsUpdate = true;
        }
        return amount;
    }

    public FluidStack liquid() {
        return this.moltenMetal.isEmpty() ? FluidStack.EMPTY : this.moltenMetal.get(0);
    }

    public void openMenu(ServerPlayer player) {
        this.updateFuelDisplay();
        int size = this.inventory.size();
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new SmelteryMenu(id, inv, this, size),
                Component.translatable("container.tconstruct.smeltery")), buf -> {
            buf.writeBlockPos(this.worldPosition);
            buf.writeVarInt(size);
        });
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // ------------------------------------------------------------------ inventory (stack limit 1)

    @Override
    public int getContainerSize() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot < this.inventory.size() ? this.inventory.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = ContainerHelper.removeItem(this.inventory, slot, count);
        if (!stack.isEmpty()) {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < this.inventory.size()) {
            this.inventory.set(slot, stack);
            this.setChanged();
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    /** onInventoryChanged. */
    @Override
    public void setChanged() {
        if (this.level != null && !this.level.isClientSide() && this.meltingTemps.length == this.inventory.size()) {
            this.updateTemperatures();
            this.needsUpdate = true;
        }
        super.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.inventory.size(); i++) {
            this.inventory.set(i, ItemStack.EMPTY);
        }
    }

    /** SmelteryBlock.breakBlock removed the tile entity without dropping anything; the port drops the items. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level != null) {
            net.minecraft.world.Containers.dropContents(this.level, pos, this.inventory);
        }
    }

    // ------------------------------------------------------------------ saving and syncing

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Layers", this.layers);
        ContainerHelper.saveAllItems(output, this.inventory);
        output.putInt("InternalTemp", this.internalTemp);
        output.putBoolean("InUse", this.inUse);
        output.putBoolean("Valid", this.validStructure);
        if (this.centerPos != null) {
            output.store("CenterPos", BlockPos.CODEC, this.centerPos);
        }
        if (this.activeLavaTank != null) {
            output.store("ActiveTank", BlockPos.CODEC, this.activeLavaTank);
        }
        output.store("Tanks", BlockPos.CODEC.listOf(), this.lavaTanks);
        output.putInt("UseTime", this.useTime);
        output.putInt("CurrentLiquid", this.currentLiquid);
        output.putInt("MaxLiquid", this.maxLiquid);
        output.putIntArray("MeltingTemps", this.meltingTemps);
        output.putIntArray("ActiveTemps", this.activeTemps);
        output.putInt("FuelGague", this.fuelGague);
        output.putInt("FuelAmount", this.fuelAmount);
        output.store("Liquids", FluidStack.CODEC.listOf(), this.moltenMetal);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.layers = input.getIntOr("Layers", 0);
        this.inventory = NonNullList.withSize(this.layers * 9, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.inventory);
        this.internalTemp = input.getIntOr("InternalTemp", 20);
        this.inUse = input.getBooleanOr("InUse", false);
        this.validStructure = input.getBooleanOr("Valid", false);
        this.centerPos = input.read("CenterPos", BlockPos.CODEC).orElse(this.worldPosition);
        this.activeLavaTank = input.read("ActiveTank", BlockPos.CODEC).orElse(null);
        this.lavaTanks.clear();
        this.lavaTanks.addAll(input.read("Tanks", BlockPos.CODEC.listOf()).orElse(List.of()));
        this.useTime = input.getIntOr("UseTime", 0);
        this.currentLiquid = input.getIntOr("CurrentLiquid", 0);
        this.maxLiquid = input.getIntOr("MaxLiquid", 0);
        this.meltingTemps = fit(input.getIntArray("MeltingTemps").orElse(new int[0]), this.layers * 9);
        for (int i = 0; i < this.inventory.size(); i++) {
            this.meltingTemps[i] = SmelteryRecipes.temperature(this.inventory.get(i));
        }
        this.activeTemps = fit(input.getIntArray("ActiveTemps").orElse(new int[0]), this.layers * 9);
        this.fuelGague = input.getIntOr("FuelGague", 0);
        this.fuelAmount = input.getIntOr("FuelAmount", 0);
        this.moltenMetal.clear();
        input.read("Liquids", FluidStack.CODEC.listOf()).ifPresent(list -> list.forEach(l -> this.moltenMetal.add(l.copy())));
    }

    private static int[] fit(int[] array, int size) {
        if (array.length == size) {
            return array;
        }
        int[] fitted = new int[size];
        java.util.Arrays.fill(fitted, 20);
        System.arraycopy(array, 0, fitted, 0, Math.min(array.length, size));
        return fitted;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
