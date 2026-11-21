package tconstruct.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import tconstruct.TConstruct;
import tconstruct.TWorld;
import tconstruct.tools.InfiTool;
import tconstruct.tools.TMaterial;
import tconstruct.tools.ToolBuilder;
import tconstruct.tools.ToolType;

import java.util.Arrays;
import java.util.List;

/**
 * BlueSlime ("EdibleSlime"): an edible slime of size 2 (149 in 299), 4 (149) or 8 (1, the King Slime with 100 health,
 * a boss bar and a slime tool as loot). Health 4 / 12 / 24 / 100, damage = size, arrows do half damage, no fall damage.
 * In full daylight under the open sky every fifth jump shrinks it by one; a slime of size 1 dies of it. It splits into
 * one slime of half its size (not the King Slime). Natural spawns: none, it comes out of liquid blue slime.
 */
public class BlueSlime extends Slime {
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getUUID(), Component.translatable("entity.tconstruct.king_slime"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);

    public BlueSlime(EntityType<? extends BlueSlime> type, Level level) {
        super(type, level);
        this.bossEvent.setVisible(false);
    }

    /** The size the constructor of 1.5.2 picked: 1 << (1, 2 or 3) from nextInt(299). */
    public void randomizeSize() {
        int offset = this.random.nextInt(299);
        int exponent = offset < 149 ? 1 : offset < 298 ? 2 : 3;
        this.setSize(1 << exponent, true);
    }

    @Override
    protected void setSpawnSize(ServerLevelAccessor level, DifficultyInstance difficulty) {
        this.randomizeSize();
    }

    public boolean isKing() {
        return this.getSize() >= 8;
    }

    @Override
    protected void setCubeMobHealth(int size) {
        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                .setBaseValue(size == 1 ? 4 : size >= 8 ? 100 : Math.min(size * size + 8, 49));
    }

    @Override
    public void setSize(int size, boolean updateHealth) {
        super.setSize(size, updateHealth);
        this.xpReward = this.getSize() >= 8 ? 500 : this.getSize() + 2;
        this.bossEvent.setVisible(this.getSize() >= 8);
    }

    @Override
    protected Component getTypeName() {
        return this.isKing() ? Component.translatable("entity.tconstruct.king_slime") : super.getTypeName();
    }

    @Override
    protected ParticleOptions getParticleType() {
        return new ItemParticleOption(ParticleTypes.ITEM, TWorld.GELATINOUS_SLIME.get());
    }

    /** jump(): 0.05 * size + 0.37 upwards, then the sun may shrink it. */
    @Override
    public void jumpFromGround() {
        Vec3 motion = this.getDeltaMovement();
        double jump = 0.05 * this.getSize() + 0.37;
        if (this.hasEffect(MobEffects.JUMP_BOOST)) {
            jump += (this.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F;
        }
        this.setDeltaMovement(motion.x, jump, motion.z);
        this.needsSync = true;
        if (this.level() instanceof ServerLevel level && level.getMaxLocalRawBrightness(this.blockPosition()) >= 15 && this.random.nextInt(5) == 0
                && level.canSeeSky(this.blockPosition())) {
            int size = this.getSize() - 1;
            if (size < 7) {
                if (size <= 0) {
                    this.kill(level);
                } else {
                    this.setSize(size, false);
                }
            }
        }
        net.neoforged.neoforge.common.CommonHooks.onLivingJump(this);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypes.ARROW)) {
            damage = (int) damage / 2;
        }
        return super.hurtServer(level, source, damage);
    }

    /** setDead: one slime of half the size, a quarter block off; the King Slime does not split. */
    @Override
    protected int getSplitCount() {
        return this.isKing() ? 0 : 1;
    }

    @Override
    protected void setUpSplitCube(AbstractCubeMob cubeMob, int halfSize, float xd, float zd) {
        float offset = -0.5F * this.getSize() / 4.0F;
        cubeMob.setSize(halfSize, true);
        cubeMob.snapTo(this.getX() + offset, this.getY() + 0.5, this.getZ() + offset, this.random.nextFloat() * 360.0F, 0.0F);
    }

    /** dropFewItems of the King Slime: a slime tool without accessory, made stronger, and 1 in 5 a miniature red heart. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (this.getSize() != 8) {
            return;
        }
        List<ToolType> valid = Arrays.stream(ToolType.values()).filter(t -> t.accessory == null).toList();
        ToolType type = valid.get(this.random.nextInt(valid.size()));
        int slime = TMaterial.BLUE_SLIME.id;
        ItemStack tool = ToolBuilder.build(type, slime, slime, -1, -1, null);
        if (tool != null) {
            InfiTool t = InfiTool.of(tool);
            if (t != null) {
                t.attack = 3 + type.baseDamage;
                t.totalDurability = 1500;
                t.baseDurability = 1500;
                t.miningSpeed = 800;
                t.save(tool);
            }
            tool.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                    Component.translatable("entity.tconstruct.king_slime").append(" ").append(tool.getItem().getName(tool)));
            this.spawnAtLocation(level, tool);
        }
        if (this.random.nextInt(5) == 0) {
            this.spawnAtLocation(level, new ItemStack(TWorld.MINIATURE_RED_HEART.get()));
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    static net.minecraft.resources.Identifier texture() {
        return TConstruct.id("textures/entity/blue_slime.png");
    }
}
