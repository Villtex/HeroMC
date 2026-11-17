package projectzulu.entity.plant;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import projectzulu.ZuluPlants;

/**
 * EntityCreeperBlossomPrimed: primed TNT with the creeper blossom as its block (rendered by the vanilla TNT renderer,
 * like RenderCreeperBlossomPrimed did with the block), a fuse of 40 ticks and an explosion of strength 1.5 that only
 * breaks blocks when mobGriefing is on.
 */
public class CreeperBlossomPrimed extends PrimedTnt {
    public static final int FUSE = 40;
    private static final float POWER = 1.5F;

    public CreeperBlossomPrimed(EntityType<? extends CreeperBlossomPrimed> type, Level level) {
        super(type, level);
        this.setFuse(FUSE);
        this.setBlockState(ZuluPlants.CREEPER_BLOSSOM.get().defaultBlockState());
    }

    public CreeperBlossomPrimed(Level level, double x, double y, double z) {
        this(ZuluPlants.CREEPER_BLOSSOM_PRIMED.get(), level);
        this.setPos(x, y, z);
        double rot = level.getRandom().nextDouble() * (Math.PI * 2);
        this.setDeltaMovement(-Math.sin(rot) * 0.02, 0.2F, -Math.cos(rot) * 0.02);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    protected void explode() {
        if (this.level() instanceof ServerLevel level) {
            level.explode(this, this.getX(), this.getY(0.5), this.getZ(), POWER, Level.ExplosionInteraction.MOB);
        }
    }
}
