package dimdoors.entity;

import dimdoors.DimDoors;
import dimdoors.world.DDData.DimData;
import dimdoors.world.DDData.LinkData;
import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Random;

/**
 * MobObelisk, the Monolith of Dimensional Doors 1.4.1: a slab 3 wide and 9 high that floats, cannot be hurt and
 * only lives in Limbo and in the pockets. It turns to face the nearest player; while it can be seen its anger grows
 * (faster in pockets or close by), its face cracks (19 textures), it tears, and at 470 the player falls into Limbo.
 */
public class Monolith extends Mob implements Enemy {
    private static final EntityDataAccessor<Byte> TEXTURE = SynchedEntityData.defineId(Monolith.class, EntityDataSerializers.BYTE);
    private float soundTime;
    private int aggro;
    private byte textureState;
    private int aggroMax;
    public final float scaleFactor;

    public Monolith(EntityType<? extends Monolith> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.scaleFactor = (float) (this.random.nextDouble() / 2.0 + 1.0);
        this.aggroMax = this.random.nextInt(245) + 200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, (byte) 0);
    }

    public int textureState() {
        return this.entityData.get(TEXTURE);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!source.is(DamageTypes.IN_WALL)) {
            this.aggro = 400;
        }
        return false;
    }

    @Override
    public void baseTick() {
        Level level = this.level();
        if (!level.isClientSide() && !DDWorld.isLimbo(level) && !DDWorld.isPocket(level)) {
            this.discard();
            return;
        }
        super.baseTick();
        if (level.isClientSide()) {
            this.clientTick();
            return;
        }
        if (this.isAlive() && this.isInWall()) {
            this.discard();
            return;
        }
        Player player = level.getNearestPlayer(this, 30.0);
        if (player != null) {
            if (this.soundTime <= 0.0F) {
                this.playSound(DimDoors.MONK.get(), 1.0F, 1.0F);
                this.soundTime = 100.0F;
            }
            this.faceEntity(player);
            if (player.hasLineOfSight(this)) {
                if (this.aggro >= 470) {
                    if (!player.getAbilities().instabuild) {
                        int dim = DDWorld.dimId(level, this.blockPosition());
                        int x = DDWorld.virtualX(dim, (int) this.getX());
                        int z = DDWorld.virtualZ(dim, (int) this.getZ());
                        LinkData link = new LinkData(dim, DimDoors.LIMBO_ID, (int) this.getX(), (int) this.getY(), (int) this.getZ(),
                                x + this.random.nextInt(500) - 250, (int) this.getY() + 500, z + this.random.nextInt(500) - 250, false, 0);
                        DDWorld.teleport(level, link, player);
                        this.aggro = 0;
                        player.level().playSound(null, player, DimDoors.CRACK.get(), SoundSource.HOSTILE, 13.0F, 1.0F);
                    }
                } else {
                    if (this.random.nextInt(11) > this.textureState || this.aggro >= 300
                            || this.random.nextInt(13) > this.textureState && this.aggroMax > this.aggro) {
                        this.aggro++;
                    }
                    if (DDWorld.isPocket(level) || level.getNearestPlayer(this, 5.0) != null) {
                        this.aggro += 2;
                        if (this.random.nextBoolean()) {
                            this.aggro++;
                        }
                    }
                    if (this.aggro > 430 && this.soundTime < 100.0F) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), DimDoors.TEARING.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
                        this.soundTime = 100.0F;
                    }
                    if (this.aggro > 445 && this.soundTime < 200.0F) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), DimDoors.TEARING.get(), SoundSource.HOSTILE, 5.0F, 1.0F);
                        this.soundTime = 200.0F;
                    }
                }
            } else if (this.aggro > 0 && this.random.nextInt(10) == 0) {
                this.aggro--;
            }
        } else if (this.aggro > 0) {
            this.aggro--;
            if (this.random.nextBoolean()) {
                this.aggro--;
            }
        }
        if (this.soundTime >= 0.0F) {
            this.soundTime--;
        }
        this.textureState = (byte) (this.aggro / 25);
        this.entityData.set(TEXTURE, this.textureState);
    }

    /** The portal particles around a player the monolith stares at (they were drawn on the client). */
    private void clientTick() {
        Player player = this.level().getNearestPlayer(this, 30.0);
        int state = this.textureState();
        if (player != null && player.hasLineOfSight(this) && (!DDWorld.isLimbo(this.level()) && this.level().getNearestPlayer(this, 5.0) == null
                || state * 25 > 300)) {
            for (int i = 0; i < -1 + state / 2; i++) {
                this.level().addParticle(ParticleTypes.PORTAL, player.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                        player.getY() + this.random.nextDouble() * player.getBbHeight() - 0.75, player.getZ() + (this.random.nextDouble() - 0.5) * player.getBbWidth(),
                        (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);
            }
        }
        if (player != null) {
            this.faceEntity(player);
        }
    }

    private void faceEntity(Player target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dy = target.getEyeY() - this.getEyeY();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(dy, horizontal) * 180.0 / Math.PI);
        this.setXRot(pitch);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.yBodyRot = yaw;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("soundTime", this.soundTime);
        output.putInt("aggro", this.aggro);
        output.putInt("aggroMax", this.aggroMax);
        output.putByte("textureState", this.textureState);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.soundTime = input.getFloatOr("soundTime", 0.0F);
        this.aggro = input.getIntOr("aggro", 0);
        this.aggroMax = input.getIntOr("aggroMax", 200);
        this.textureState = input.getByteOr("textureState", (byte) 0);
    }

    // ---- where monoliths come from ----

    private static void spawn(ServerLevel level, double x, double y, double z) {
        Monolith mob = DimDoors.MONOLITH.get().create(level, EntitySpawnReason.CHUNK_GENERATION);
        if (mob != null) {
            mob.snapTo(x, y, z, 1.0F, 1.0F);
            level.addFreshEntity(mob);
        }
    }

    /** CommonTickHandler.placeMonolithsInPockets: one monolith on the floor of each chunk of a closed dungeon (five tries). */
    public static void placeInPocketChunk(ServerLevel level, int chunkX, int chunkZ) {
        DimData data = DDWorld.data().pocketAt(chunkX * 16 + 8, chunkZ * 16 + 8);
        if (data == null || data.dungeon == null) {
            return;
        }
        Random random = new Random(level.getSeed());
        long factorA = random.nextLong() / 2L * 2L + 1L;
        long factorB = random.nextLong() / 2L * 2L + 1L;
        random.setSeed(chunkX * factorA + chunkZ * factorB ^ level.getSeed());
        int sanity = 0;
        boolean didSpawn = false;
        do {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = 245;
            BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos(x, y, z);
            BlockState state = level.getBlockState(at);
            while (state.isAir() && y > 0) {
                state = level.getBlockState(at.setY(--y));
            }
            while ((state.is(DimDoors.FABRIC.get()) || state.is(DimDoors.ETERNAL_FABRIC.get())) && y > 0) {
                state = level.getBlockState(at.setY(--y));
            }
            while (state.isAir() && y > 0) {
                state = level.getBlockState(at.setY(--y));
            }
            if (y > 0) {
                int jumpSanity = 0;
                int jumpHeight;
                do {
                    jumpHeight = y + random.nextInt(10);
                } while (!level.getBlockState(at.setY(jumpHeight + 6)).isAir() && ++jumpSanity < 20);
                spawn(level, x, jumpHeight, z);
                didSpawn = true;
            }
        } while (++sanity < 5 && !didSpawn);
    }

    /** CommonTickHandler.placeMonolithsInLimbo: in 28 of 100 new Limbo chunks one or more monoliths above the ground. */
    public static void placeInLimboChunk(WorldGenLevel level, int chunkX, int chunkZ, RandomSource rand) {
        if (rand.nextInt(100) >= DimDoors.MONOLITH_SPAWNING_CHANCE) {
            return;
        }
        int y = 0;
        int yTest;
        do {
            int x = chunkX * 16 + rand.nextInt(16);
            int z = chunkZ * 16 + rand.nextInt(16);
            while (level.getBlockState(new BlockPos(x, y, z)).isAir() && y < 255) {
                y++;
            }
            y = firstUncovered(level, x, y + 2, z);
            yTest = firstUncovered(level, x, y + 5, z);
            if (yTest > 245) {
                return;
            }
            int jumpSanity = 0;
            int jumpHeight;
            do {
                jumpHeight = y + rand.nextInt(25);
            } while (!level.getBlockState(new BlockPos(x, jumpHeight + 6, z)).isAir() && ++jumpSanity < 20);
            Monolith mob = DimDoors.MONOLITH.get().create(level.getLevel(), EntitySpawnReason.CHUNK_GENERATION);
            if (mob != null) {
                mob.snapTo(x, jumpHeight, z, 1.0F, 1.0F);
                level.addFreshEntity(mob);
            }
        } while (yTest > y);
    }

    private static int firstUncovered(WorldGenLevel level, int x, int yStart, int z) {
        int y = yStart;
        boolean blocked;
        do {
            blocked = y > 0 && (solid(level.getBlockState(new BlockPos(x, y - 1, z))) || solid(level.getBlockState(new BlockPos(x, y, z))));
        } while (blocked && ++y < 245);
        return y - 1;
    }

    private static boolean solid(BlockState state) {
        return !state.isAir() && (!state.canBeReplaced() || !state.getFluidState().isEmpty());
    }
}
