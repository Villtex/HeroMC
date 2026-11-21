package twilightforest.entity.boss.hydra;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import twilightforest.TFHydra;

import java.util.List;
import java.util.Optional;

/**
 * HydraHeadContainer: one head of the hydra with its five neck segments and the state machine that moves it. Every
 * state has a duration, a neck length, a pitch and yaw (per head) and a mouth opening; the head moves between the
 * values of the last and the current state. Bite (1-4: 40 + 80 + 7 + 40 ticks, the bite hurts 48), flame (5-7:
 * 40 + 100 + 30, 19 fire damage and 3 s burning on what the head looks at, within 30), mortar (8-10: 40 + 25 + 30,
 * a mortar every 10 ticks), cooldown 13 (80), dying 11 (70) and dead 12, then born 14 (20), roar 15 (10) and 16 (50).
 * The server runs the states; the client only animates a dying head (explosions along the neck) and makes the mouth
 * particles. Static fields of 1.5.2 that changed with the difficulty are per hydra here.
 */
public class HydraHeadContainer {
    private static final int FLAME_BURN_FACTOR = 3;
    private static final int FLAME_DAMAGE = 19;
    private static final int BITE_DAMAGE = 48;
    public static final int NEXT_AUTOMATIC = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_BITE_BEGINNING = 1;
    public static final int STATE_BITE_READY = 2;
    public static final int STATE_BITE_BITING = 3;
    public static final int STATE_BITE_ENDING = 4;
    public static final int STATE_FLAME_BEGINNING = 5;
    public static final int STATE_FLAME_BREATHING = 6;
    public static final int STATE_FLAME_ENDING = 7;
    public static final int STATE_MORTAR_BEGINNING = 8;
    public static final int STATE_MORTAR_LAUNCH = 9;
    public static final int STATE_MORTAR_ENDING = 10;
    public static final int STATE_DYING = 11;
    public static final int STATE_DEAD = 12;
    public static final int STATE_ATTACK_COOLDOWN = 13;
    public static final int STATE_BORN = 14;
    public static final int STATE_ROAR_START = 15;
    public static final int STATE_ROAR_RAWR = 16;
    public static final int NUM_STATES = 17;

    private static final int[] NEXT_STATES = new int[NUM_STATES];
    private static final int[] STATE_DURATIONS = new int[NUM_STATES];
    private static final float[][] STATE_NECK_LENGTH = new float[7][NUM_STATES];
    private static final float[][] STATE_X_ROTATIONS = new float[7][NUM_STATES];
    private static final float[][] STATE_Y_ROTATIONS = new float[7][NUM_STATES];
    private static final float[][] STATE_MOUTH_OPEN = new float[7][NUM_STATES];

    public @Nullable HydraHead headEntity;
    public final HydraPart necka;
    public final HydraPart neckb;
    public final HydraPart neckc;
    public final HydraPart neckd;
    public final HydraPart necke;
    public @Nullable Entity targetEntity;
    public double targetX;
    public double targetY;
    public double targetZ;
    public int prevState;
    public int currentState;
    public int nextState = NEXT_AUTOMATIC;
    public boolean didRoar;
    public boolean isSecondaryAttacking;
    public int ticksNeeded;
    public int ticksProgress;
    public final int headNum;
    public int damageTaken;
    public int respawnCounter;
    public final Hydra hydraObj;
    private double flameBreathTrackingSpeed = 0.04;

    public HydraHeadContainer(Hydra hydra, int number, boolean startActive) {
        this.headNum = number;
        this.hydraObj = hydra;
        this.damageTaken = 0;
        this.respawnCounter = -1;
        this.didRoar = false;
        this.necka = new HydraPart(hydra, "neck" + number + "a", 2.0F, 2.0F, true);
        this.neckb = new HydraPart(hydra, "neck" + number + "b", 2.0F, 2.0F, true);
        this.neckc = new HydraPart(hydra, "neck" + number + "c", 2.0F, 2.0F, true);
        this.neckd = new HydraPart(hydra, "neck" + number + "d", 2.0F, 2.0F, true);
        this.necke = new HydraPart(hydra, "neck" + number + "e", 2.0F, 2.0F, true);
        if (startActive) {
            this.prevState = STATE_IDLE;
            this.currentState = STATE_IDLE;
            this.ticksNeeded = 60;
            this.ticksProgress = 60;
        } else {
            this.prevState = STATE_DEAD;
            this.currentState = STATE_DEAD;
            this.ticksNeeded = 20;
            this.ticksProgress = 20;
        }
    }

    static {
        int[][] next = {{0, 0}, {1, 2}, {2, 3}, {3, 4}, {4, 13}, {5, 6}, {6, 7}, {7, 13}, {8, 9}, {9, 10}, {10, 13}, {13, 0}, {11, 12},
                {12, 12}, {14, 15}, {15, 16}, {16, 0}};
        for (int[] n : next) {
            NEXT_STATES[n[0]] = n[1];
        }
        int[][] durations = {{0, 10}, {1, 40}, {2, 80}, {3, 7}, {4, 40}, {5, 40}, {6, 100}, {7, 30}, {8, 40}, {9, 25}, {10, 30}, {13, 80},
                {11, 70}, {12, 20}, {14, 20}, {15, 10}, {16, 50}};
        for (int[] d : durations) {
            STATE_DURATIONS[d[0]] = d[1];
        }
        setupStateRotations();
    }

    private static void setupStateRotations() {
        // idle and cooldown
        for (int state : new int[]{STATE_IDLE, STATE_ATTACK_COOLDOWN}) {
            setAnimation(0, state, 60.0F, 0.0F, 7.0F, 0.0F);
            setAnimation(1, state, 10.0F, 60.0F, 9.0F, 0.0F);
            setAnimation(2, state, 10.0F, -60.0F, 9.0F, 0.0F);
            setAnimation(3, state, 50.0F, 90.0F, 8.0F, 0.0F);
            setAnimation(4, state, 50.0F, -90.0F, 8.0F, 0.0F);
            setAnimation(5, state, -10.0F, 90.0F, 9.0F, 0.0F);
            setAnimation(6, state, -10.0F, -90.0F, 9.0F, 0.0F);
        }
        // flame and mortar beginning (5, 8), breathing and launch (6, 9), ending (7, 10)
        for (int state : new int[]{STATE_FLAME_BEGINNING, STATE_MORTAR_BEGINNING}) {
            setAnimation(0, state, 50.0F, 0.0F, 8.0F, 0.75F);
            setAnimation(1, state, 30.0F, 45.0F, 9.0F, 0.75F);
            setAnimation(2, state, 30.0F, -45.0F, 9.0F, 0.75F);
            setAnimation(3, state, 50.0F, 90.0F, 8.0F, 0.75F);
            setAnimation(4, state, 50.0F, -90.0F, 8.0F, 0.75F);
            setAnimation(5, state, -10.0F, 90.0F, 9.0F, 0.75F);
            setAnimation(6, state, -10.0F, -90.0F, 9.0F, 0.75F);
        }
        for (int state : new int[]{STATE_FLAME_BREATHING, STATE_MORTAR_LAUNCH}) {
            setAnimation(0, state, 45.0F, 0.0F, 8.0F, 1.0F);
            setAnimation(1, state, 30.0F, 60.0F, 9.0F, 1.0F);
            setAnimation(2, state, 30.0F, -60.0F, 9.0F, 1.0F);
            setAnimation(3, state, 50.0F, 90.0F, 8.0F, 1.0F);
            setAnimation(4, state, 50.0F, -90.0F, 8.0F, 1.0F);
            setAnimation(5, state, -10.0F, 90.0F, 9.0F, 1.0F);
            setAnimation(6, state, -10.0F, -90.0F, 9.0F, 1.0F);
        }
        for (int state : new int[]{STATE_FLAME_ENDING, STATE_MORTAR_ENDING}) {
            setAnimation(0, state, 60.0F, 0.0F, 7.0F, 0.0F);
            setAnimation(1, state, 10.0F, 45.0F, 9.0F, 0.0F);
            setAnimation(2, state, 10.0F, -45.0F, 9.0F, 0.0F);
            setAnimation(3, state, 50.0F, 90.0F, 8.0F, 0.0F);
            setAnimation(4, state, 50.0F, -90.0F, 8.0F, 0.0F);
            setAnimation(5, state, -10.0F, 90.0F, 9.0F, 0.0F);
            setAnimation(6, state, -10.0F, -90.0F, 9.0F, 0.0F);
        }
        // the bite (only heads 0-2 bite; the others keep zeros there, as in 1.5.2)
        setAnimation(0, STATE_BITE_BEGINNING, -5.0F, 60.0F, 5.0F, 0.25F);
        setAnimation(1, STATE_BITE_BEGINNING, -10.0F, 60.0F, 9.0F, 0.25F);
        setAnimation(2, STATE_BITE_BEGINNING, -10.0F, -60.0F, 9.0F, 0.25F);
        setAnimation(0, STATE_BITE_READY, -5.0F, 60.0F, 5.0F, 1.0F);
        setAnimation(1, STATE_BITE_READY, -10.0F, 60.0F, 9.0F, 1.0F);
        setAnimation(2, STATE_BITE_READY, -10.0F, -60.0F, 9.0F, 1.0F);
        setAnimation(0, STATE_BITE_BITING, -5.0F, -30.0F, 5.0F, 0.2F);
        setAnimation(1, STATE_BITE_BITING, -10.0F, -30.0F, 5.0F, 0.2F);
        setAnimation(2, STATE_BITE_BITING, -10.0F, 30.0F, 5.0F, 0.2F);
        setAnimation(0, STATE_BITE_ENDING, 60.0F, 0.0F, 7.0F, 0.0F);
        setAnimation(1, STATE_BITE_ENDING, -10.0F, 60.0F, 9.0F, 0.0F);
        setAnimation(2, STATE_BITE_ENDING, -10.0F, -60.0F, 9.0F, 0.0F);
        // dying, dead, born, roar
        setAnimation(0, STATE_DYING, -20.0F, 0.0F, 7.0F, 0.0F);
        setAnimation(1, STATE_DYING, -20.0F, 60.0F, 9.0F, 0.0F);
        setAnimation(2, STATE_DYING, -20.0F, -60.0F, 9.0F, 0.0F);
        setAnimation(3, STATE_DYING, -20.0F, 90.0F, 8.0F, 0.0F);
        setAnimation(4, STATE_DYING, -20.0F, -90.0F, 8.0F, 0.0F);
        setAnimation(5, STATE_DYING, -10.0F, 90.0F, 9.0F, 0.0F);
        setAnimation(6, STATE_DYING, -10.0F, -90.0F, 9.0F, 0.0F);
        setAnimation(0, STATE_DEAD, 0.0F, 179.0F, 4.0F, 0.0F);
        setAnimation(1, STATE_DEAD, 0.0F, 179.0F, 4.0F, 0.0F);
        setAnimation(2, STATE_DEAD, 0.0F, -180.0F, 4.0F, 0.0F);
        setAnimation(3, STATE_DEAD, 0.0F, 179.0F, 4.0F, 0.0F);
        setAnimation(4, STATE_DEAD, 0.0F, -180.0F, 4.0F, 0.0F);
        setAnimation(5, STATE_DEAD, 0.0F, 179.0F, 4.0F, 0.0F);
        setAnimation(6, STATE_DEAD, 0.0F, -180.0F, 4.0F, 0.0F);
        for (float[] row : new float[][]{{STATE_BORN, 7.0F, 9.0F, 8.0F, 0.0F}, {STATE_ROAR_START, 7.0F, 9.0F, 8.0F, 0.25F},
                {STATE_ROAR_RAWR, 9.0F, 11.0F, 10.0F, 1.0F}}) {
            int state = (int) row[0];
            float mouth = row[4];
            setAnimation(0, state, 60.0F, 0.0F, row[1], mouth);
            setAnimation(1, state, 10.0F, 60.0F, row[2], mouth);
            setAnimation(2, state, 10.0F, -60.0F, row[2], mouth);
            setAnimation(3, state, 50.0F, 90.0F, row[3], mouth);
            setAnimation(4, state, 50.0F, -90.0F, row[3], mouth);
            setAnimation(5, state, -10.0F, 90.0F, row[2], mouth);
            setAnimation(6, state, -10.0F, -90.0F, row[2], mouth);
        }
    }

    private static void setAnimation(int head, int state, float xRotation, float yRotation, float neckLength, float mouthOpen) {
        STATE_X_ROTATIONS[head][state] = xRotation;
        STATE_Y_ROTATIONS[head][state] = yRotation;
        STATE_NECK_LENGTH[head][state] = neckLength;
        STATE_MOUTH_OPEN[head][state] = mouthOpen;
    }

    public HydraPart[] getNeckArray() {
        return new HydraPart[]{this.necka, this.neckb, this.neckc, this.neckd, this.necke};
    }

    public void onUpdate() {
        for (HydraPart neck : this.getNeckArray()) {
            neck.tickPart();
        }
        Level level = this.hydraObj.level();
        if (this.headEntity == null || this.headEntity.isRemoved()) {
            this.headEntity = this.findNearbyHead("head" + this.headNum);
        }
        this.flameBreathTrackingSpeed = this.hydraObj.isHard() ? 0.1 : 0.04;
        if (this.headEntity != null) {
            if (!level.isClientSide()) {
                this.advanceRespawnCounter();
                this.advanceHeadState();
                this.setHeadPosition();
                this.setHeadFacing();
                this.executeAttacks((ServerLevel) level);
                this.playSounds();
            } else {
                this.clientAnimateHeadDeath();
                this.addMouthParticles();
            }
            this.setNeckPosition();
        }
    }

    protected void advanceRespawnCounter() {
        if (this.currentState == STATE_DEAD && this.respawnCounter > -1 && --this.respawnCounter <= 0) {
            this.setNextState(STATE_BORN);
            this.damageTaken = 0;
            this.endCurrentAction();
            this.respawnCounter = -1;
        }
    }

    protected void clientAnimateHeadDeath() {
        HydraHead head = this.headEntity;
        if (head.getState() == STATE_DYING) {
            head.dyingTime++;
            if (head.dyingTime > 0) {
                if (head.dyingTime < 20) {
                    this.doExplosionOn(head, true);
                } else if (head.dyingTime < 30) {
                    this.doExplosionOn(this.necka, false);
                } else if (head.dyingTime < 40) {
                    this.doExplosionOn(this.neckb, false);
                } else if (head.dyingTime < 50) {
                    this.doExplosionOn(this.neckc, false);
                } else if (head.dyingTime < 60) {
                    this.doExplosionOn(this.neckd, false);
                } else if (head.dyingTime < 70) {
                    this.doExplosionOn(this.necke, false);
                }
            }
            for (HydraPart neck : this.getNeckArray()) {
                neck.hurtTime = 20;
            }
        } else {
            head.dyingTime = 0;
        }
    }

    private void doExplosionOn(Entity part, boolean large) {
        RandomSource rand = part instanceof LivingEntity ? part.getRandom() : this.hydraObj.getRandom();
        float width = part.getBbWidth();
        float height = part.getBbHeight();
        for (int i = 0; i < 10; i++) {
            double vx = rand.nextGaussian() * 0.02;
            double vy = rand.nextGaussian() * 0.02;
            double vz = rand.nextGaussian() * 0.02;
            ParticleOptions particle = large && rand.nextInt(5) == 0 ? ParticleTypes.EXPLOSION : ParticleTypes.POOF;
            part.level().addParticle(particle, part.getX() + rand.nextFloat() * width * 2.0F - width, part.getY() + rand.nextFloat() * height,
                    part.getZ() + rand.nextFloat() * width * 2.0F - width, vx, vy, vz);
        }
    }

    protected void advanceHeadState() {
        if (++this.ticksProgress >= this.ticksNeeded) {
            int myNext;
            if (this.nextState == NEXT_AUTOMATIC) {
                myNext = NEXT_STATES[this.currentState];
                if (myNext != this.currentState && this.isSecondaryAttacking && myNext == STATE_ATTACK_COOLDOWN) {
                    this.isSecondaryAttacking = false;
                    myNext = STATE_IDLE;
                }
            } else {
                myNext = this.nextState;
                this.nextState = NEXT_AUTOMATIC;
            }
            this.ticksNeeded = STATE_DURATIONS[myNext];
            this.ticksProgress = 0;
            this.prevState = this.currentState;
            this.currentState = myNext;
        }
        if (this.headEntity.getState() != this.currentState) {
            this.headEntity.setState(this.currentState);
        }
    }

    protected void setHeadFacing() {
        HydraHead head = this.headEntity;
        if (this.currentState == STATE_BITE_READY) {
            this.faceEntity(this.targetEntity, 5.0F, this.hydraObj.getVerticalFaceSpeed());
            float biteMaxYaw = -60.0F;
            float biteMinYaw = -90.0F;
            if (this.headNum == 2) {
                biteMaxYaw = 60.0F;
                biteMinYaw = 90.0F;
            }
            // as in 1.5.2, with the bounds of head 2 swapped (so only the second test can hit)
            float yawOffOffset = Mth.wrapDegrees(head.getYRot() - this.hydraObj.yBodyRot);
            if (yawOffOffset > biteMaxYaw) {
                head.setYRot(this.hydraObj.yBodyRot + biteMaxYaw);
            }
            if (yawOffOffset < biteMinYaw) {
                head.setYRot(this.hydraObj.yBodyRot + biteMinYaw);
            }
            Vec3 look = head.look();
            double distance = 16.0;
            this.targetX = head.getX() + look.x * distance;
            this.targetY = head.getY() + 1.5 + look.y * distance;
            this.targetZ = head.getZ() + look.z * distance;
        } else if (this.currentState == STATE_BITE_BITING || this.currentState == STATE_BITE_ENDING) {
            this.faceEntity(this.targetEntity, 5.0F, this.hydraObj.getVerticalFaceSpeed());
            // 1.5.2 added PI / 4 to the pitch in degrees
            head.setXRot((float) (head.getXRot() + Math.PI / 4));
        } else if (this.currentState == STATE_ROAR_RAWR) {
            this.faceVec(this.targetX, this.targetY, this.targetZ, 10.0F, this.hydraObj.getVerticalFaceSpeed());
        } else if (this.currentState == STATE_FLAME_BREATHING || this.currentState == STATE_FLAME_BEGINNING) {
            this.moveTargetCoordsTowardsTargetEntity(this.flameBreathTrackingSpeed);
            this.faceVec(this.targetX, this.targetY, this.targetZ, 5.0F, this.hydraObj.getVerticalFaceSpeed());
        } else if (this.isActive()) {
            if (this.targetEntity != null) {
                this.faceEntity(this.targetEntity, 5.0F, this.hydraObj.getVerticalFaceSpeed());
            } else {
                this.faceIdle(1.5F, this.hydraObj.getVerticalFaceSpeed());
            }
        }
    }

    protected void moveTargetCoordsTowardsTargetEntity(double distance) {
        if (this.targetEntity != null) {
            Vec3 vect = new Vec3(this.targetEntity.getX() - this.targetX, this.targetEntity.getY() - this.targetY,
                    this.targetEntity.getZ() - this.targetZ).normalize();
            this.targetX += vect.x * distance;
            this.targetY += vect.y * distance;
            this.targetZ += vect.z * distance;
        }
    }

    /** Client: flames and smoke (5), the flame breath (6), spit (1, 2), smoke (8) at the mouth, 3.5 in front. */
    protected void addMouthParticles() {
        HydraHead head = this.headEntity;
        Level level = head.level();
        RandomSource rand = head.getRandom();
        Vec3 vector = head.look();
        double dist = 3.5;
        double px = head.getX() + vector.x * dist;
        double py = head.getY() + 1.0 + vector.y * dist;
        double pz = head.getZ() + vector.z * dist;
        int state = head.getState();
        if (state == STATE_FLAME_BEGINNING) {
            level.addParticle(ParticleTypes.FLAME, px + rand.nextDouble() - 0.5, py + rand.nextDouble() - 0.5, pz + rand.nextDouble() - 0.5, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.SMOKE, px + rand.nextDouble() - 0.5, py + rand.nextDouble() - 0.5, pz + rand.nextDouble() - 0.5, 0.0, 0.0, 0.0);
        }
        if (state == STATE_FLAME_BREATHING) {
            for (int i = 0; i < 5; i++) {
                double dx = vector.x;
                double dy = vector.y;
                double dz = vector.z;
                double spread = 5.0 + rand.nextDouble() * 2.5;
                double velocity = 1.0 + rand.nextDouble();
                dx += rand.nextGaussian() * 0.0075F * spread;
                dy += rand.nextGaussian() * 0.0075F * spread;
                dz += rand.nextGaussian() * 0.0075F * spread;
                level.addParticle(TFHydra.LARGE_FLAME.get(), px, py, pz, dx * velocity, dy * velocity, dz * velocity);
            }
        }
        if (state == STATE_BITE_BEGINNING || state == STATE_BITE_READY) {
            level.addParticle(ParticleTypes.SPLASH, px + rand.nextDouble() - 0.5, py + rand.nextDouble() - 0.5, pz + rand.nextDouble() - 0.5, 0.0, 0.0, 0.0);
        }
        if (state == STATE_MORTAR_BEGINNING) {
            level.addParticle(ParticleTypes.LARGE_SMOKE, px + rand.nextDouble() - 0.5, py + rand.nextDouble() - 0.5, pz + rand.nextDouble() - 0.5, 0.0, 0.0, 0.0);
        }
    }

    /** Server: the fire breath (every 5 ticks), the roar when it has grown back, the warning before the bite. */
    protected void playSounds() {
        HydraHead head = this.headEntity;
        Level level = head.level();
        RandomSource rand = head.getRandom();
        double x = head.getX() + 0.5;
        double y = head.getY() + 0.5;
        double z = head.getZ() + 0.5;
        if (this.currentState == STATE_FLAME_BREATHING && head.tickCount % 5 == 0) {
            level.playSound(null, x, y, z, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 0.5F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F);
        }
        // 1.5.2 started the roar again every tick of the state (didRoar was never set); once per roar here
        if (this.currentState == STATE_ROAR_RAWR && !this.didRoar) {
            this.didRoar = true;
            level.playSound(null, x, y, z, TFHydra.HYDRA_ROAR.get(), SoundSource.HOSTILE, 1.25F, rand.nextFloat() * 0.3F + 0.7F);
        }
        if (this.currentState == STATE_BITE_READY && this.ticksProgress == 60) {
            level.playSound(null, x, y, z, TFHydra.HYDRA_WARN.get(), SoundSource.HOSTILE, 2.0F, rand.nextFloat() * 0.3F + 0.7F);
        }
        if (this.currentState == STATE_IDLE) {
            this.didRoar = false;
        }
    }

    protected void setNeckPosition() {
        Vec3 vector;
        float neckRotation;
        switch (this.headNum) {
            case 0 -> {
                vector = new Vec3(0.0, 3.0, -1.0);
                neckRotation = 0.0F;
            }
            case 1 -> {
                vector = new Vec3(-1.0, 3.0, 3.0);
                neckRotation = 90.0F;
            }
            case 2 -> {
                vector = new Vec3(1.0, 3.0, 3.0);
                neckRotation = -90.0F;
            }
            case 3 -> {
                vector = new Vec3(-1.0, 3.0, 3.0);
                neckRotation = 135.0F;
            }
            case 4 -> {
                vector = new Vec3(1.0, 3.0, 3.0);
                neckRotation = -135.0F;
            }
            case 5 -> {
                vector = new Vec3(-1.0, 3.0, 5.0);
                neckRotation = 135.0F;
            }
            default -> {
                vector = new Vec3(1.0, 3.0, 5.0);
                neckRotation = -135.0F;
            }
        }
        vector = vector.yRot(-(this.hydraObj.yBodyRot + neckRotation) * 3.141593F / 180.0F);
        this.setNeckPositon(this.hydraObj.getX() + vector.x, this.hydraObj.getY() + vector.y, this.hydraObj.getZ() + vector.z,
                this.hydraObj.yBodyRot, 0.0F);
    }

    protected void setHeadPosition() {
        float neckLength = this.getCurrentNeckLength();
        float xRotation = this.getCurrentHeadXRotation();
        float yRotation = this.getCurrentHeadYRotation();
        float periodX = this.headNum != 0 && this.headNum != 3 ? (this.headNum != 1 && this.headNum != 4 ? 7.0F : 5.0F) : 20.0F;
        float periodY = this.headNum != 0 && this.headNum != 4 ? (this.headNum != 1 && this.headNum != 6 ? 5.0F : 6.0F) : 10.0F;
        float xSwing = Mth.sin(this.hydraObj.tickCount / periodX) * 3.0F;
        float ySwing = Mth.sin(this.hydraObj.tickCount / periodY) * 5.0F;
        if (!this.isActive()) {
            ySwing = 0.0F;
            xSwing = 0.0F;
        }
        // xSwing is added in degrees*PI as in 1.5.2 (so it hardly shows)
        Vec3 vector = new Vec3(0.0, 0.0, neckLength).xRot((xRotation * 3.141593F + xSwing) / 180.0F)
                .yRot(-(this.hydraObj.yBodyRot + yRotation + ySwing) * 3.141593F / 180.0F);
        this.headEntity.setPos(this.hydraObj.getX() + vector.x, this.hydraObj.getY() + vector.y + 3.0, this.hydraObj.getZ() + vector.z);
        this.headEntity.setMouthOpen(this.getCurrentMouthOpen());
    }

    protected void executeAttacks(ServerLevel level) {
        HydraHead head = this.headEntity;
        if (this.currentState == STATE_MORTAR_LAUNCH && this.ticksProgress % 10 == 0) {
            Entity lookTarget = this.getHeadLookTarget();
            if (lookTarget == null || !isHydraPart(lookTarget)) {
                HydraMortar mortar = new HydraMortar(TFHydra.HYDRA_MORTAR.get(), level, head);
                Vec3 vector = head.look();
                double dist = 3.5;
                double px = head.getX() + vector.x * dist;
                double py = head.getY() + 1.0 + vector.y * dist;
                double pz = head.getZ() + vector.z * dist;
                mortar.snapTo(px, py, pz, 0.0F, 0.0F);
                if (this.targetEntity != null && !head.hasLineOfSight(this.targetEntity)) {
                    mortar.setToBlasting();
                }
                level.levelEvent(null, LevelEvent.SOUND_GHAST_FIREBALL, BlockPos.containing(head.getX(), head.getY(), head.getZ()), 0);
                level.addFreshEntity(mortar);
            } else {
                this.endCurrentAction();
            }
        }
        if (head.getState() == STATE_BITE_BITING) {
            for (Entity nearby : level.getEntities(head, head.getBoundingBox().inflate(0.0, 1.0, 0.0))) {
                if (nearby instanceof LivingEntity && !(nearby instanceof HydraHead) && !(nearby instanceof Hydra) && !(nearby instanceof HydraPart)) {
                    nearby.hurtServer(level, this.hydraObj.damageSources().mobAttack(this.hydraObj), BITE_DAMAGE);
                }
            }
        }
        if (head.getState() == STATE_FLAME_BREATHING) {
            Entity target = this.getHeadLookTarget();
            if (target != null) {
                if (isHydraPart(target)) {
                    this.endCurrentAction();
                } else if (!target.fireImmune() && target.hurtServer(level, level.damageSources().inFire(), FLAME_DAMAGE)) {
                    target.igniteForSeconds(FLAME_BURN_FACTOR);
                }
            }
        }
    }

    static boolean isHydraPart(Entity entity) {
        return entity instanceof HydraHead || entity instanceof HydraPart;
    }

    /** What the head looks at within 30 blocks (from 1 above its feet); its own first three neck segments do not count. */
    private @Nullable Entity getHeadLookTarget() {
        HydraHead head = this.headEntity;
        Entity pointedEntity = null;
        double range = 30.0;
        Vec3 srcVec = new Vec3(head.getX(), head.getY() + 1.0, head.getZ());
        Vec3 lookVec = head.look();
        Vec3 destVec = srcVec.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);
        float grow = 3.0F;
        List<Entity> possibleList = head.level().getEntities(head,
                head.getBoundingBox().expandTowards(lookVec.x * range, lookVec.y * range, lookVec.z * range).inflate(grow, grow, grow));
        double hitDist = 0.0;
        for (Entity possibleEntity : possibleList) {
            if (possibleEntity.isPickable() && possibleEntity != head && possibleEntity != this.necka && possibleEntity != this.neckb
                    && possibleEntity != this.neckc) {
                float borderSize = possibleEntity.getPickRadius();
                AABB collisionBB = possibleEntity.getBoundingBox().inflate(borderSize, borderSize, borderSize);
                Optional<Vec3> interceptPos = collisionBB.clip(srcVec, destVec);
                if (collisionBB.contains(srcVec)) {
                    if (0.0 < hitDist || hitDist == 0.0) {
                        pointedEntity = possibleEntity;
                        hitDist = 0.0;
                    }
                } else if (interceptPos.isPresent()) {
                    double possibleDist = srcVec.distanceTo(interceptPos.get());
                    if (possibleDist < hitDist || hitDist == 0.0) {
                        pointedEntity = possibleEntity;
                        hitDist = possibleDist;
                    }
                }
            }
        }
        return pointedEntity;
    }

    public void setNextState(int next) {
        this.nextState = next;
    }

    public void endCurrentAction() {
        this.ticksProgress = this.ticksNeeded;
    }

    private @Nullable HydraHead findNearbyHead(String name) {
        Hydra hydra = this.hydraObj;
        AABB box = new AABB(hydra.getX(), hydra.getY(), hydra.getZ(), hydra.getX() + 1.0, hydra.getY() + 1.0, hydra.getZ() + 1.0).inflate(16.0);
        for (HydraHead nearbyHead : hydra.level().getEntitiesOfClass(HydraHead.class, box)) {
            if (nearbyHead.getPartName().equals(name) && !nearbyHead.isRemoved()) {
                nearbyHead.hydraObj = hydra;
                return nearbyHead;
            }
        }
        return null;
    }

    private float progress() {
        return (float) this.ticksProgress / this.ticksNeeded;
    }

    protected float getCurrentNeckLength() {
        float prev = STATE_NECK_LENGTH[this.headNum][this.prevState];
        float cur = STATE_NECK_LENGTH[this.headNum][this.currentState];
        return prev + (cur - prev) * this.progress();
    }

    protected float getCurrentHeadXRotation() {
        float prev = STATE_X_ROTATIONS[this.headNum][this.prevState];
        float cur = STATE_X_ROTATIONS[this.headNum][this.currentState];
        return prev + (cur - prev) * this.progress();
    }

    protected float getCurrentHeadYRotation() {
        float prev = STATE_Y_ROTATIONS[this.headNum][this.prevState];
        float cur = STATE_Y_ROTATIONS[this.headNum][this.currentState];
        return prev + (cur - prev) * this.progress();
    }

    protected float getCurrentMouthOpen() {
        float prev = STATE_MOUTH_OPEN[this.headNum][this.prevState];
        float cur = STATE_MOUTH_OPEN[this.headNum][this.currentState];
        return prev + (cur - prev) * this.progress();
    }

    /** The five segments from 1 behind the head (factor 0) to the start at the body (factor 1). */
    protected void setNeckPositon(double startX, double startY, double startZ, float startYaw, float startPitch) {
        HydraHead head = this.headEntity;
        double endX = head.getX();
        double endY = head.getY();
        double endZ = head.getZ();
        float endYaw = head.getYRot();
        float endPitch = head.getXRot();
        while (startYaw - endYaw < -180.0F) {
            endYaw -= 360.0F;
        }
        while (startYaw - endYaw >= 180.0F) {
            endYaw += 360.0F;
        }
        while (startPitch - endPitch < -180.0F) {
            endPitch -= 360.0F;
        }
        while (startPitch - endPitch >= 180.0F) {
            endPitch += 360.0F;
        }
        if (endPitch > 0.0F) {
            Vec3 vector = new Vec3(0.0, 0.0, -1.0).yRot(-endYaw * 3.141593F / 180.0F);
            endX += vector.x;
            endY += vector.y;
            endZ += vector.z;
        } else {
            Vec3 vector = head.look();
            endX -= vector.x;
            endY -= vector.y;
            endZ -= vector.z;
        }
        HydraPart[] necks = this.getNeckArray();
        for (int i = 0; i < necks.length; i++) {
            float factor = i * 0.25F;
            necks[i].place(endX + (startX - endX) * factor, endY + (startY - endY) * factor, endZ + (startZ - endZ) * factor,
                    endYaw + (startYaw - endYaw) * factor, endPitch + (startPitch - endPitch) * factor);
        }
    }

    protected void faceIdle(float yawConstraint, float pitchConstraint) {
        float angle = this.hydraObj.getYRot() * 3.141593F / 180.0F;
        float distance = 30.0F;
        double dx = this.hydraObj.getX() - Mth.sin(angle) * distance;
        double dy = this.hydraObj.getY() + 3.0;
        double dz = this.hydraObj.getZ() + Mth.cos(angle) * distance;
        this.faceVec(dx, dy, dz, yawConstraint, pitchConstraint);
    }

    public void faceEntity(@Nullable Entity entity, float yawConstraint, float pitchConstraint) {
        if (entity == null) {
            return;
        }
        double yTarget;
        if (entity instanceof LivingEntity living) {
            yTarget = living.getY() + living.getEyeHeight();
        } else {
            yTarget = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0;
        }
        this.faceVec(entity.getX(), yTarget, entity.getZ(), yawConstraint, pitchConstraint);
        this.targetX = entity.getX();
        this.targetY = entity.getY();
        this.targetZ = entity.getZ();
    }

    public void faceVec(double xCoord, double yCoord, double zCoord, float yawConstraint, float pitchConstraint) {
        HydraHead head = this.headEntity;
        double xOffset = xCoord - head.getX();
        double zOffset = zCoord - head.getZ();
        double yOffset = head.getY() + 1.0 - yCoord;
        double distance = Math.sqrt(xOffset * xOffset + zOffset * zOffset);
        float xyAngle = (float) (Math.atan2(zOffset, xOffset) * 180.0 / Math.PI) - 90.0F;
        float zdAngle = (float) (-(Math.atan2(yOffset, distance) * 180.0 / Math.PI));
        head.setXRot(-updateRotation(head.getXRot(), zdAngle, pitchConstraint));
        head.setYRot(updateRotation(head.getYRot(), xyAngle, yawConstraint));
    }

    private static float updateRotation(float current, float intended, float increment) {
        float delta = Mth.wrapDegrees(intended - current);
        if (delta > increment) {
            delta = increment;
        }
        if (delta < -increment) {
            delta = -increment;
        }
        return Mth.wrapDegrees(current + delta);
    }

    public @Nullable Entity getTargetEntity() {
        return this.targetEntity;
    }

    public void setTargetEntity(@Nullable Entity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public void setHurtTime(int hurtTime) {
        if (this.headEntity != null) {
            this.headEntity.hurtTime = hurtTime;
        }
        for (HydraPart neck : this.getNeckArray()) {
            neck.hurtTime = hurtTime;
        }
    }

    /** RenderTFHydraHead: the head until it has been dying for 20 ticks. */
    public boolean shouldRenderHead() {
        return this.headEntity.getState() != STATE_DEAD && this.headEntity.dyingTime < 20;
    }

    /** RenderTFHydraHead: neck segment n until the explosions have reached it. */
    public boolean shouldRenderNeck(int neckNum) {
        int time = 30 + 10 * neckNum;
        return this.headEntity.getState() != STATE_DEAD && this.headEntity.dyingTime < time;
    }

    public boolean isActive() {
        return this.currentState != STATE_DYING && this.currentState != STATE_DEAD;
    }

    public void addDamage(int damageAmount) {
        this.damageTaken += damageAmount;
    }

    public int getDamageTaken() {
        return this.damageTaken;
    }

    public void setRespawnCounter(int count) {
        this.respawnCounter = count;
    }
}
