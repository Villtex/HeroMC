package twilightforest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * EntityTFMiniGhast: a small tower ghast (1.1 x 1.5, 10 health) that gets angry like an enderman: when a player looks
 * straight at it or comes within 3.5 blocks (a carved pumpkin on the head keeps it calm). Aggro range 16, stare range
 * 8, short wanders. As a minion of the Ur-Ghast (Teil 24f) it has 6 health, barely wanders and drops nothing. Up to 16
 * in a chunk.
 */
public class MiniGhast extends TowerGhast {
    private boolean isMinion;

    public MiniGhast(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.aggroRange = 16.0F;
        this.stareRange = 8.0F;
        this.wanderFactor = 4.0F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TowerGhast.createAttributes().add(Attributes.MAX_HEALTH, 10.0);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 16;
    }

    @Override
    protected boolean shouldAttackPlayer(Player player) {
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN)) {
            return false;
        }
        if (player.distanceTo(this) <= 3.5F && player.hasLineOfSight(this)) {
            return true;
        }
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 toMe = new Vec3(this.getX() - player.getX(), this.getBoundingBox().minY + this.getBbHeight() / 2.0F - player.getEyeY(),
                this.getZ() - player.getZ());
        double length = toMe.length();
        double dot = look.dot(toMe.normalize());
        return dot > 1.0 - 0.025 / length && player.hasLineOfSight(this);
    }

    public boolean isMinion() {
        return this.isMinion;
    }

    public void makeBossMinion() {
        this.wanderFactor = 0.005F;
        this.isMinion = true;
        var health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(6.0);
        }
        this.setHealth(this.getMaxHealth());
    }

    @Override
    protected boolean shouldDropLoot(net.minecraft.server.level.ServerLevel level) {
        return !this.isMinion && super.shouldDropLoot(level);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("isMinion", this.isMinion);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        if (input.getBooleanOr("isMinion", false)) {
            this.makeBossMinion();
        }
    }

    /** getCanSpawnHere: not on peaceful, no obstruction, monster light (isValidLightLevel). */
    public static boolean checkSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }
}
