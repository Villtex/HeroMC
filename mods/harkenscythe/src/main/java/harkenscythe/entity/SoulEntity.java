package harkenscythe.entity;

import harkenscythe.HSBlocks;
import harkenscythe.HSEntities;
import harkenscythe.item.Essence;
import harkenscythe.item.KeeperItem;
import harkenscythe.item.SpecialTierAbilities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * EntityHSSoul and its kinds EntityHSSoulGrieving (of a player), EntityHSSoulCulled (of a Harbinger) and
 * EntityHSSoulWrathful (of a boss): a floating crystal that a reaped mob leaves behind. It sinks while two blocks below
 * it are free, lights its block ({@link harkenscythe.block.SoulLightBlock}) and vanishes after 1000 ticks. Right-clicked
 * with a keeper or vessel that has room it goes into it (its worth in souls) and repairs livingmetal gear (2, 10 or
 * 100). Only a Harbinger can hurt it. It remembers what it was ({@link #getFallenSoulName}) for the Necronomicon.
 */
public class SoulEntity extends Mob {
    private int soulWorth;
    private int soulAge;
    private String fallenSoulName = "null";
    /** Client: the spin of the crystal (random start). */
    public final int innerRotation;

    public SoulEntity(EntityType<? extends SoulEntity> type, Level level) {
        super(type, level);
        this.innerRotation = this.random.nextInt(100000);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 5.0);
    }

    /** spawnEntityInWorld of a new soul at the rounded death position of {@code fallen}. */
    public static SoulEntity spawn(EntityType<? extends SoulEntity> type, ServerLevel level, double x, double y, double z, int worth, LivingEntity fallen) {
        SoulEntity soul = type.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (soul == null) {
            return null;
        }
        soul.setPos(x, y, z);
        soul.soulWorth = worth;
        soul.fallenSoulName = FallenSoul.nameOf(fallen);
        level.addFreshEntity(soul);
        return soul;
    }

    public int getSoulWorth() {
        return this.soulWorth;
    }

    public String getFallenSoulName() {
        return this.fallenSoulName;
    }

    /** What taking it in repairs: 2, the culled soul 10, the wrathful one 100. */
    private int repairAmount() {
        EntityType<?> type = this.getType();
        if (type == HSEntities.SOUL_CULLED.get()) {
            return 10;
        }
        if (type == HSEntities.SOUL_WRATHFUL.get()) {
            return 100;
        }
        return 2;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() || this.isDeadOrDying()) {
            return;
        }
        int rx = (int) Math.round(this.getX());
        int ry = (int) Math.round(this.getY());
        int rz = (int) Math.round(this.getZ());
        if (this.level().isEmptyBlock(new BlockPos(rx, ry - 1, rz)) && this.level().isEmptyBlock(new BlockPos(rx, ry - 2, rz))) {
            this.setPos(this.getX(), this.getY() - 1.0, this.getZ());
        }
        BlockPos pos = this.blockPosition();
        if (this.level().isEmptyBlock(pos)) {
            this.level().setBlockAndUpdate(pos, HSBlocks.SOUL_LIGHT.get().defaultBlockState());
        }
        if (++this.soulAge >= 1000) {
            this.vanish();
        }
    }

    /** setDead of 1.5.2: takes the light along and makes a faint wither sound. */
    public void vanish() {
        this.discard();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide() && reason.shouldDestroy()) {
            clearLight(this);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.NEUTRAL, 0.1F, 1.0F);
        }
        super.remove(reason);
    }

    static void clearLight(net.minecraft.world.entity.Entity entity) {
        BlockPos pos = entity.blockPosition();
        if (entity.level().getBlockState(pos).is(HSBlocks.SOUL_LIGHT.get())) {
            entity.level().removeBlock(pos, false);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!acceptsSouls(stack)) {
            return super.mobInteract(player, hand);
        }
        if (!this.level().isClientSide()) {
            Essence.fill(player, this.soulWorth, false);
            SpecialTierAbilities.livingmetal(player, this.repairAmount());
            this.vanish();
        }
        return InteractionResult.SUCCESS;
    }

    /** A Soulkeeper or Soul Vessel with room, or an empty Essence Keeper or Vessel. */
    static boolean acceptsSouls(ItemStack stack) {
        if (!(stack.getItem() instanceof KeeperItem keeper)) {
            return false;
        }
        return switch (keeper.kind()) {
            case SOULKEEPER, SOUL_VESSEL -> stack.getDamageValue() != 0;
            case ESSENCE_KEEPER, ESSENCE_VESSEL -> true;
            default -> false;
        };
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (SpectralPets.isHarbinger(source.getEntity()) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, damage);
        }
        return false;
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
    public void checkDespawn() {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putShort("Age", (short) this.soulAge);
        output.putShort("Value", (short) this.soulWorth);
        output.putString("fallenSoul", this.fallenSoulName);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.soulAge = input.getShortOr("Age", (short) 0);
        this.soulWorth = input.getShortOr("Value", (short) 0);
        this.fallenSoulName = input.getStringOr("fallenSoul", "null");
    }

    /** Client: the bobbing of the crystal (RenderHSSoul). */
    public static float bob(float time) {
        float h = Mth.sin(time * 0.2F) / 2.0F + 0.5F;
        return (h + h * h) * 0.2F;
    }
}
