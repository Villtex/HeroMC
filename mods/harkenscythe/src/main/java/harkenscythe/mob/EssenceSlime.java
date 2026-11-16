package harkenscythe.mob;

import harkenscythe.HSItems;
import harkenscythe.item.Essence;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * EntityHSSlimeSoul (Ectoplasm) / EntityHSSlimeBlood (Hemoglobin): a slime of freed souls or blood (size 1-3, 5 health
 * per size, 1 xp, no drops). A keeper or vessel that has room (or an empty one) takes in its worth on right-click;
 * then, as when it dies, it splits into two of one size smaller, each worth half. Size 1 does not hurt.
 * <p>
 * Unlike 1.5.2 the worth is saved with the slime (the original lost it when the chunk was saved).
 */
public class EssenceSlime extends Slime {
    private final boolean blood;
    private int resourceWorth;
    private boolean splitDone;

    public EssenceSlime(EntityType<? extends EssenceSlime> type, Level level, boolean blood) {
        super(type, level);
        this.blood = blood;
    }

    public boolean isBlood() {
        return this.blood;
    }

    public int getResourceWorth() {
        return this.resourceWorth;
    }

    @Override
    protected void setCubeMobHealth(int size) {
        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(size * 5);
    }

    @Override
    public void setSize(int size, boolean updateHealth) {
        super.setSize(size, updateHealth);
        this.xpReward = 1;
    }

    /** setResourceWorthTotal: a crucible's content spread over the starting size. */
    public void setResourceWorthTotal(int total, int startSize) {
        if (startSize == 3) {
            this.resourceWorth = Math.round(total / 3);
        } else if (startSize == 2) {
            this.resourceWorth = Math.round(total / 2);
        } else if (startSize == 1) {
            this.resourceWorth = total;
        }
    }

    /** setResourceWorth: half of what the parent was worth. */
    public void setResourceWorth(int parentWorth) {
        this.resourceWorth = parentWorth / 2;
    }

    @Override
    protected ParticleOptions getParticleType() {
        return this.blood ? new DustParticleOptions(0xCC0000, 1.0F) : SpellParticleOption.create(ParticleTypes.EFFECT, 0xFFFFFF, 1.0F);
    }

    private boolean takesWorth(ItemStack stack) {
        if (this.blood) {
            return stack.is(HSItems.BLOODKEEPER.get()) && stack.getDamageValue() != 0 || stack.is(HSItems.ESSENCE_KEEPER.get())
                    || stack.is(HSItems.BLOOD_VESSEL.get()) && stack.getDamageValue() != 0 || stack.is(HSItems.ESSENCE_VESSEL.get());
        }
        return stack.is(HSItems.SOULKEEPER.get()) && stack.getDamageValue() != 0 || stack.is(HSItems.ESSENCE_KEEPER.get())
                || stack.is(HSItems.SOUL_VESSEL.get()) && stack.getDamageValue() != 0 || stack.is(HSItems.ESSENCE_VESSEL.get());
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !this.takesWorth(stack)) {
            return super.mobInteract(player, hand);
        }
        if (this.level() instanceof ServerLevel level) {
            Essence.fill(player, this.resourceWorth, this.blood);
            level.playSound(null, player, SoundEvents.SLIME_ATTACK, SoundSource.PLAYERS, 0.3F, 0.5F);
            this.split(level);
            this.discard();
        }
        return InteractionResult.SUCCESS;
    }

    /** setDead: two slimes of one size smaller, each worth half. */
    private void split(ServerLevel level) {
        int size = this.getSize();
        this.splitDone = true;
        if (size <= 1) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            float dx = (i % 2 - 0.5F) * size / 4.0F;
            float dz = (i / 2 - 0.5F) * size / 4.0F;
            EssenceSlime child = (EssenceSlime) this.getType().create(level, EntitySpawnReason.TRIGGERED);
            if (child == null) {
                continue;
            }
            child.snapTo(this.getX() + dx, this.getY() + 0.5, this.getZ() + dz, this.random.nextFloat() * 360.0F, 0.0F);
            child.setSize(size - 1, true);
            child.setResourceWorth(this.resourceWorth);
            level.addFreshEntity(child);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel level && reason == RemovalReason.KILLED && !this.splitDone) {
            this.split(level);
        }
        // the vanilla split (size / 2, two to four) must not happen
        this.entityData.set(ID_SIZE, 1);
        super.remove(reason);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Worth", this.resourceWorth);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.resourceWorth = input.getIntOr("Worth", 0);
    }
}
