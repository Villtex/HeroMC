package twilightforest.entity.passive;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import twilightforest.entity.TFMobUtil;
import twilightforest.gen.TFFeature;

import java.util.EnumSet;

/**
 * EntityTFQuestRam: the ram of the quest grove (70 health, 1.25 x 2.9). It eats wool of every colour it has not had
 * yet, from the hand or off the ground (EntityAITFEatLoose, EntityAITFFindLoose), and grows a coloured band for each.
 * With all 16 colours it drops a block each of diamond, iron, emerald, gold and lapis and the crumble horn (once). In a
 * quest grove its home is the grove centre (radius 13). Loud sheep sounds, low pitch.
 */
public class QuestRam extends Animal {
    private static final EntityDataAccessor<Integer> DATA_COLORS = SynchedEntityData.defineId(QuestRam.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_REWARDED = SynchedEntityData.defineId(QuestRam.class, EntityDataSerializers.BOOLEAN);
    private int randomTickDivider;

    public QuestRam(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 70.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.38 / 0.25));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.0, stack -> woolColor(stack) != null, false));
        this.goalSelector.addGoal(3, new EatLooseGoal(this));
        this.goalSelector.addGoal(4, new FindLooseGoal(this, 1.0));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COLORS, 0);
        builder.define(DATA_REWARDED, false);
    }

    /** The colour of a wool block item, null for anything else. */
    public static @Nullable DyeColor woolColor(ItemStack stack) {
        if (!(stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem)) {
            return null;
        }
        Block block = blockItem.getBlock();
        for (DyeColor color : DyeColor.values()) {
            Block wool = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(color.getSerializedName() + "_wool"));
            if (wool == block) {
                return color;
            }
        }
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (--this.randomTickDivider <= 0) {
            this.randomTickDivider = 70 + this.random.nextInt(50);
            TFFeature near = TFMobUtil.nearestFeature(this);
            if (near != TFFeature.QUEST_GROVE) {
                this.clearHome();
            } else {
                int fx = (int) (Math.round(this.getBlockX() / 256.0) * 256L + 8L);
                int fz = (int) (Math.round(this.getBlockZ() / 256.0) * 256L + 8L);
                this.setHomeTo(new BlockPos(fx, twilightforest.gen.TFTerrain.SEALEVEL, fz), 13);
            }
            if (this.countColorsSet() > 15 && !this.getRewarded()) {
                this.rewardQuest(level);
                this.setRewarded(true);
            }
        }
        super.customServerAiStep(level);
    }

    private void rewardQuest(ServerLevel level) {
        for (Block block : new Block[]{Blocks.DIAMOND_BLOCK, Blocks.IRON_BLOCK, Blocks.EMERALD_BLOCK, Blocks.GOLD_BLOCK, Blocks.LAPIS_BLOCK}) {
            this.spawnAtLocation(level, new ItemStack(block), 1.0F);
        }
        BuiltInRegistries.ITEM.getOptional(twilightforest.TwilightForest.id("crumble_horn"))
                .ifPresent(horn -> this.spawnAtLocation(level, new ItemStack(horn), 1.0F));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        DyeColor color = woolColor(stack);
        if (color != null && !this.isColorPresent(color.getId())) {
            this.setColorPresent(color.getId());
            this.animateAddColor(color.getId(), 50);
            stack.consume(1, player);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.countColorsSet() > 15 && !this.getRewarded()) {
            this.animateAddColor(this.random.nextInt(16), 5);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("ColorFlags", this.getColorFlags());
        output.putBoolean("Rewarded", this.getRewarded());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setColorFlags(input.getIntOr("ColorFlags", 0));
        this.setRewarded(input.getBooleanOr("Rewarded", false));
    }

    public int getColorFlags() {
        return this.entityData.get(DATA_COLORS);
    }

    public void setColorFlags(int flags) {
        this.entityData.set(DATA_COLORS, flags);
    }

    public boolean isColorPresent(int color) {
        return (this.getColorFlags() & 1 << color) > 0;
    }

    public void setColorPresent(int color) {
        this.setColorFlags(this.getColorFlags() | 1 << color);
    }

    public boolean getRewarded() {
        return this.entityData.get(DATA_REWARDED);
    }

    public void setRewarded(boolean rewarded) {
        this.entityData.set(DATA_REWARDED, rewarded);
    }

    public static int countColors(int flags) {
        return Integer.bitCount(flags & 0xFFFF);
    }

    public int countColorsSet() {
        return countColors(this.getColorFlags());
    }

    /** "mobSpell" particles in the fleece colour, then the sheep sound. */
    public void animateAddColor(int color, int iterations) {
        int rgb = DyeColor.byId(color).getTextureDiffuseColor();
        for (int i = 0; i < iterations; i++) {
            TFMobUtil.particle(this, ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF000000 | rgb),
                    this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 1.5,
                    this.getY() + this.random.nextDouble() * this.getBbHeight() * 1.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 1.5, 0.0, 0.0, 0.0);
        }
        this.playAmbientSound();
    }

    @Override
    protected float getSoundVolume() {
        return 5.0F;
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.7F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SHEEP_STEP, 0.15F, 1.0F);
    }

    /** EntityAITFEatLoose: eats wool lying within 2 blocks that it lacks (mutex 0), then waits 100 ticks. */
    static final class EatLooseGoal extends Goal {
        private final QuestRam ram;
        private int delay;
        private @Nullable ItemEntity item;

        EatLooseGoal(QuestRam ram) {
            this.ram = ram;
            this.setFlags(EnumSet.noneOf(Flag.class));
        }

        @Override
        public boolean canUse() {
            if (this.delay > 0) {
                this.delay--;
                return false;
            }
            this.item = null;
            for (ItemEntity nearby : this.ram.level().getEntitiesOfClass(ItemEntity.class, this.ram.getBoundingBox().inflate(2.0))) {
                DyeColor color = woolColor(nearby.getItem());
                if (color != null && !this.ram.isColorPresent(color.getId()) && nearby.isAlive()) {
                    this.item = nearby;
                    break;
                }
            }
            return this.item != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            this.item = null;
            this.ram.getNavigation().stop();
            this.delay = 100;
        }

        @Override
        public void tick() {
            if (this.item == null) {
                return;
            }
            this.ram.getLookControl().setLookAt(this.item, 30.0F, this.ram.getMaxHeadXRot());
            DyeColor color = woolColor(this.item.getItem());
            if (color != null && this.ram.distanceToSqr(this.item) < 6.25 && !this.ram.isColorPresent(color.getId())) {
                this.item.discard();
                this.ram.playAmbientSound();
                this.ram.setColorPresent(color.getId());
                this.ram.animateAddColor(color.getId(), 50);
            }
        }
    }

    /** EntityAITFFindLoose: walks to wool lying within 16 x 4 blocks (mutex 3), then waits 100 ticks. */
    static final class FindLooseGoal extends Goal {
        private final QuestRam ram;
        private final double speed;
        private int delay;
        private @Nullable ItemEntity item;

        FindLooseGoal(QuestRam ram, double speed) {
            this.ram = ram;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.delay > 0) {
                this.delay--;
                return false;
            }
            this.item = null;
            for (ItemEntity nearby : this.ram.level().getEntitiesOfClass(ItemEntity.class, this.ram.getBoundingBox().inflate(16.0, 4.0, 16.0))) {
                if (woolColor(nearby.getItem()) != null && nearby.isAlive()) {
                    this.item = nearby;
                    break;
                }
            }
            return this.item != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            this.item = null;
            this.ram.getNavigation().stop();
            this.delay = 100;
        }

        @Override
        public void tick() {
            if (this.item == null) {
                return;
            }
            this.ram.getLookControl().setLookAt(this.item, 30.0F, this.ram.getMaxHeadXRot());
            if (this.ram.distanceToSqr(this.item) < 6.25) {
                this.ram.getNavigation().stop();
            } else {
                this.ram.getNavigation().moveTo(this.item.getX(), this.item.getY(), this.item.getZ(), this.speed);
            }
        }
    }
}
