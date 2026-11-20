package meteors.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import meteors.MeteorNetwork;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * TextureDetector: the needle of a detector (kind 0 time, 1 proximity, 2 crash) turns towards the meteor that falls
 * soonest, the nearest announced meteor or the last crash, swinging like a compass; spinning without a target or
 * outside the overworld. Returns the frame of the 32-frame strip as 0..1.
 */
public record DetectorAngle(int kind) implements RangeSelectItemModelProperty {
    public static final MapCodec<DetectorAngle> MAP_CODEC = Codec.INT.fieldOf("kind").xmap(DetectorAngle::new, DetectorAngle::kind);
    private static final double[] CURRENT = new double[3];
    private static final double[] DELTA = new double[3];
    private static final long[] LAST = new long[3];

    @Override
    public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
        Entity holder = owner == null ? null : owner.asLivingEntity();
        if (holder == null) {
            return (float) (CURRENT[this.kind] / (Math.PI * 2) % 1.0 + 1.0) % 1.0F;
        }
        double target;
        Optional<BlockPos> pos = switch (this.kind) {
            case 0 -> MeteorNetwork.nearestTime;
            case 1 -> MeteorNetwork.ghosts.stream().min((a, b) -> Double.compare(dist(holder, a), dist(holder, b)));
            default -> MeteorNetwork.lastCrash;
        };
        if (pos.isPresent() && level.dimension() == Level.OVERWORLD) {
            double dx = pos.get().getX() - holder.getX();
            double dz = pos.get().getZ() - holder.getZ();
            double yaw = holder.getYRot() % 360.0;
            target = -((yaw - 90.0) * Math.PI / 180.0 - Math.atan2(dz, dx));
        } else {
            target = Math.random() * Math.PI * 2.0;
        }
        long tick = level.getGameTime();
        if (tick != LAST[this.kind]) {
            LAST[this.kind] = tick;
            double d = target - CURRENT[this.kind];
            while (d < -Math.PI) {
                d += Math.PI * 2;
            }
            while (d >= Math.PI) {
                d -= Math.PI * 2;
            }
            d = Math.max(-1.0, Math.min(1.0, d));
            DELTA[this.kind] = (DELTA[this.kind] + d * 0.1) * 0.8;
            CURRENT[this.kind] += DELTA[this.kind];
        }
        double frame = (CURRENT[this.kind] / (Math.PI * 2) + 1.0) % 1.0;
        return (float) (frame < 0 ? frame + 1.0 : frame);
    }

    private static double dist(Entity e, BlockPos p) {
        double dx = p.getX() - e.getX();
        double dz = p.getZ() - e.getZ();
        return dx * dx + dz * dz;
    }

    @Override
    public MapCodec<DetectorAngle> type() {
        return MAP_CODEC;
    }
}
