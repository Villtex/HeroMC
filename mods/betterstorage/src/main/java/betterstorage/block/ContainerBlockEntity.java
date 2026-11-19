package betterstorage.block;

import betterstorage.BSUtil;
import betterstorage.menu.BSMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityContainer: contents of columns x rows, a custom title from a renamed item, the number of players using it
 * (sent to the clients as block event 0, which moves the lid) and the lid angle.
 */
public abstract class ContainerBlockEntity extends BlockEntity {
    public final NonNullList<ItemStack> contents;
    private @Nullable Component customTitle;
    private int playersUsing;
    private int ticksSinceSync;
    public float lidAngle;
    public float prevLidAngle;

    protected ContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.contents = NonNullList.withSize(this.getSizeContents(), ItemStack.EMPTY);
    }

    public int getColumns() {
        return 9;
    }

    public int getRows() {
        return 3;
    }

    protected int getSizeContents() {
        return this.getColumns() * this.getRows();
    }

    /** The translation key of the container title. */
    public abstract String getName();

    public final int getPlayersUsing() {
        return this.playersUsing;
    }

    /** WorldUtils.isTileEntityUsableByPlayer: still this block entity, the player within 8 blocks of its middle. */
    public boolean canPlayerUseContainer(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    public @Nullable Component getCustomTitle() {
        return this.customTitle;
    }

    public void setCustomTitle(@Nullable Component title) {
        this.customTitle = title;
    }

    /** getContainerTitle: the custom title or the localized name. */
    public Component getContainerTitle() {
        return this.customTitle != null ? this.customTitle : Component.translatable(this.getName());
    }

    /** getPlayerInventory: the contents of this block entity. */
    public TileInventory getPlayerInventory() {
        return new TileInventory(this, this);
    }

    public void openGui(ServerPlayer player) {
        if (this.canPlayerUseContainer(player)) {
            TileInventory inventory = this.getPlayerInventory();
            BSMenu.open(player, this.getContainerTitle(), inventory.columns, inventory.rows, inventory);
        }
    }

    private void doSyncPlayersUsing(int players) {
        if (this.level != null) {
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 0, players);
        }
    }

    @Override
    public boolean triggerEvent(int event, int value) {
        if (event == 0) {
            this.playersUsing = value;
        }
        return true;
    }

    public void onContainerOpened() {
        this.doSyncPlayersUsing(++this.playersUsing);
    }

    public void onContainerClosed() {
        this.doSyncPlayersUsing(--this.playersUsing);
    }

    protected float getLidSpeed() {
        return 0.1F;
    }

    /** updateEntity, on both sides. */
    public void tick(Level level) {
        if (!level.isClientSide()) {
            this.playersUsing = this.syncPlayersUsing(++this.ticksSinceSync, this.playersUsing);
        }
        this.prevLidAngle = this.lidAngle;
        float lidSpeed = this.getLidSpeed();
        if (this.playersUsing > 0) {
            this.lidAngle = Math.min(1.0F, this.lidAngle + lidSpeed);
        } else {
            this.lidAngle = Math.max(0.0F, this.lidAngle - lidSpeed);
        }
    }

    /** WorldUtils.syncPlayersUsing: every 200 ticks count the players within 5 blocks that have this open. */
    private int syncPlayersUsing(int ticks, int players) {
        ticks += this.worldPosition.getX() + this.worldPosition.getY() + this.worldPosition.getZ();
        if (players != 0 && ticks % 200 == 0 && this.level != null) {
            players = 0;
            for (Player player : this.level.getEntitiesOfClass(Player.class, new AABB(this.worldPosition).inflate(5.0))) {
                if (player.containerMenu instanceof BSMenu menu && menu.container instanceof TileInventory inventory
                        && inventory.main == this) {
                    players++;
                }
            }
        }
        return players;
    }

    public void dropContents() {
        if (this.level != null) {
            for (ItemStack stack : this.contents) {
                BSUtil.dropFromBlock(this.level, this.worldPosition, stack);
            }
            this.contents.replaceAll(s -> ItemStack.EMPTY);
        }
    }

    /** The renamed item's name (onBlockPlacedBy: setCustomTitle(stack.getDisplayName())). */
    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        Component name = components.get(DataComponents.CUSTOM_NAME);
        if (name != null) {
            this.setCustomTitle(name);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.customTitle = parseCustomNameSafe(input, "CustomName");
        if (!this.contents.isEmpty()) {
            this.contents.replaceAll(s -> ItemStack.EMPTY);
            ContainerHelper.loadAllItems(input, this.contents);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("CustomName", ComponentSerialization.CODEC, this.customTitle);
        if (!this.contents.isEmpty()) {
            ContainerHelper.saveAllItems(output, this.contents);
        }
    }

    /** getDescriptionPacket: what the renderer needs (subclasses add to {@link #writeClientData}). */
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    /** setChanged + markBlockForUpdate. */
    public void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    /** The contents of the block entity itself, never all of a double chest (dropContents does that per half). */
    public static boolean isValid(@Nullable Container c) {
        return c != null;
    }
}
