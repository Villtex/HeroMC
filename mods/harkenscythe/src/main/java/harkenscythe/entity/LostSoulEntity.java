package harkenscythe.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import harkenscythe.HSBlocks;

/**
 * EntityHSSoulLost: a lost soul that spawns like a monster in caves (out of the sky and below y 60) of the overworld
 * biomes of 1.5.2 (weight 3, at most two per chunk). It does not move, lights its block, cannot be hurt and vanishes
 * after 6000 ticks, under the open sky, above y 60 or on peaceful. A keeper used on it frees an evil spectral miner
 * (Teil 9, part 3); the Necronomicon raises it as a friendly one.
 */
public class LostSoulEntity extends Monster {
    private int soulAge;
    private int soulWorth;
    public final int innerRotation;

    public LostSoulEntity(EntityType<? extends LostSoulEntity> type, Level level) {
        super(type, level);
        this.innerRotation = this.random.nextInt(100000);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0);
    }

    /** getCanSpawnHere: a monster spot out of the sky and below y 60. */
    public static boolean checkSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        return !level.canSeeSky(pos) && pos.getY() < 60 && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 2;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        BlockPos pos = this.blockPosition();
        if (this.level().isEmptyBlock(pos)) {
            this.level().setBlockAndUpdate(pos, HSBlocks.SOUL_LIGHT.get().defaultBlockState());
        }
        if (this.level().getDifficulty() == Difficulty.PEACEFUL || ++this.soulAge >= 6000 || this.level().canSeeSky(pos) || this.getY() >= 60.0) {
            this.discard();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide() && reason.shouldDestroy()) {
            SoulEntity.clearLight(this);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 0.1F, 1.0F);
        }
        super.remove(reason);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!SoulEntity.acceptsSouls(player.getItemInHand(hand))) {
            return super.mobInteract(player, hand);
        }
        if (this.level() instanceof ServerLevel server && SpectralPets.spawnEvilMiner(server, this.getX(), this.getY(), this.getZ())) {
            this.discard();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && super.hurtServer(level, source, damage);
    }

    /** The original never called the movement of EntityLiving: no gravity and no AI, whatever the saved data says. */
    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean isNoAi() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putShort("Age", (short) this.soulAge);
        output.putShort("Value", (short) this.soulWorth);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.soulAge = input.getShortOr("Age", (short) 0);
        this.soulWorth = input.getShortOr("Value", (short) 0);
    }
}
