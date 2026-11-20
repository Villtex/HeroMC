package meteors.block;

import meteors.MeteorData;
import meteors.MeteorType;
import meteors.MeteorsConfig;
import meteors.MeteorsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEntityMeteorShield: the owner, the shield registration in {@link MeteorData} once the shield is charged (power
 * > 0) and the colour of the last meteor stopped for the owner. On the client it animates the ray that climbs from
 * the shield while it charges up (power 1..4) and the beam of the full shield (power 5) under open sky.
 */
public class MeteorShieldBlockEntity extends BlockEntity {
    private static final double[][] DIRECTION_MOTION = {
            {0.0, 0.0}, {0.0, 0.3}, {-0.3, 0.3}, {-0.3, 0.0}, {-0.3, -0.3}, {0.0, -0.3}, {0.3, -0.3}, {0.3, 0.0}, {0.3, 0.3}};

    private boolean shieldedChunks;
    private String owner = "";
    private MeteorType lastStopped = MeteorType.METEORITE;
    // client animation
    public int direction;
    public float rayHeight;
    public float rayXMod;
    public float rayZMod;
    public boolean renderRay;

    public MeteorShieldBlockEntity(BlockPos pos, BlockState state) {
        super(MeteorsMod.METEOR_SHIELD_ENTITY.get(), pos, state);
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
        this.setChanged();
    }

    public MeteorType getLastStopped() {
        return this.lastStopped;
    }

    public void setLastStopped(MeteorType type) {
        this.lastStopped = type;
        this.sync();
    }

    public void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    /** The shield of this block for that power: its chunk and ShieldRadiusMultiplier * power chunks. */
    public MeteorData.Shield shield(int power) {
        return new MeteorData.Shield(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4, MeteorsConfig.SHIELD_RADIUS.get() * power, this.owner);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MeteorShieldBlockEntity be) {
        if (!be.shieldedChunks) {
            int power = state.getValue(MeteorShieldBlock.POWER);
            if (power > 0 && level instanceof ServerLevel server) {
                MeteorData data = MeteorData.get(server.getServer());
                data.addShield(pos, be.shield(power));
                be.shieldedChunks = true;
                be.lastStopped = data.lastPrevented(be.owner);
                be.sync();
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level instanceof ServerLevel server) {
            MeteorData.get(server.getServer()).removeShield(pos);
        }
    }

    /** updateEntity on the client: the ray of the charging shield and the sky check of the beam. */
    public static void clientTick(Level level, BlockPos pos, BlockState state, MeteorShieldBlockEntity be) {
        int power = state.getValue(MeteorShieldBlock.POWER);
        if (power <= 0) {
            return;
        }
        if (!be.shieldedChunks) {
            be.shieldedChunks = be.renderRay = true;
        }
        if (power == 5) {
            if (level.getGameTime() % 60L == 0L) {
                be.renderRay = level.canSeeSky(pos.above());
            }
            if (!level.getBlockState(pos.above()).isSolidRender()) {
                particles(level, pos, level.getRandom(), 32, be.lastStopped.id());
            }
        } else {
            int mult = Mth.clamp(power, 1, 4);
            if (be.rayHeight < 100.0F) {
                be.rayHeight += 0.3F * mult;
                be.rayXMod = (float) (be.rayXMod + DIRECTION_MOTION[be.direction][0] * mult);
                be.rayZMod = (float) (be.rayZMod + DIRECTION_MOTION[be.direction][1] * mult);
            } else {
                be.rayHeight = 0.0F;
                be.rayXMod = 0.0F;
                be.rayZMod = 0.0F;
                be.renderRay = true;
                be.direction = level.getRandom().nextInt(9);
            }
            if (be.rayHeight > 1.0F && be.renderRay) {
                // isBlockNormalCubeDefault(xCoord, y, zCoord): the column straight above the shield
                BlockPos above = new BlockPos(pos.getX(), (int) (pos.getY() + be.rayHeight), pos.getZ());
                be.renderRay = !level.getBlockState(above).isRedstoneConductor(level, above);
            }
            if (be.rayHeight > 100.0F - (35.0F + 5 * mult) && be.rayHeight < 100.0F - (10.0F + 5 * mult)
                    && !level.getBlockState(pos.above()).isSolidRender()) {
                particles(level, pos, level.getRandom(), 32, -1);
            }
        }
    }

    /**
     * GenerateParticles / randomDisplayTick: enchantment-table particles flying into the shield from the ring two
     * blocks around it, 1 of {@code chance} per place; {@code type} is the colour row of particles.png, -1 mixed.
     */
    public static void particles(Level level, BlockPos pos, net.minecraft.util.RandomSource random, int chance, int type) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        var particle = MeteorsMod.SHIELD_PARTICLES.get(type < 0 ? 5 : type).get();
        for (int l = i - 2; l <= i + 2; l++) {
            for (int i1 = k - 2; i1 <= k + 2; i1++) {
                if (l > i - 2 && l < i + 2 && i1 == k - 1) {
                    i1 = k + 2;
                }
                if (random.nextInt(chance) == 0) {
                    for (int j1 = j; j1 <= j + 1 && level.isEmptyBlock(new BlockPos((l - i) / 2 + i, j1, (i1 - k) / 2 + k)); j1++) {
                        level.addParticle(particle, i + 0.5, j + 2.0, k + 0.5, l - i + random.nextFloat() - 0.5, j1 - j - random.nextFloat() - 1.0F,
                                i1 - k + random.nextFloat() - 0.5);
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("owner", this.owner);
        output.putInt("metprevent", this.lastStopped.id());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.owner = input.getStringOr("owner", "");
        this.lastStopped = MeteorType.byId(input.getIntOr("metprevent", 0));
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
