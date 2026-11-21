package tconstruct.landmine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import tconstruct.TDeco;

import java.util.ArrayList;
import java.util.List;

/**
 * LandmineExplodeLogic with the behaviours of 1.5.2 (landmine.logic.behavior): equal trigger items are added up, the
 * firework + fire charge combination goes first (SpecialStackHandlerRocketFireball), then each item does its thing
 * and is used up; at the end the default behaviour hurts the triggerer by 5 (armour piercing) and the mine is gone.
 * Redstone dust keeps the mine (and itself) and only lets the harmless items work.
 */
public final class LandmineLogic {
    /** The kinds of Behavior. */
    public enum Kind {
        DUMMY(1, true, true, false), UTILITY(1, false, false, true), EXPLOSIVE(1, true, true, false), FIREWORK(1, false, true, false),
        BLOCK_THROW(64, true, true, false), POTION(1, false, true, false), FIREBALL(1, true, true, false), SPAWN(1, true, false, false),
        SHOOT(1, true, true, false), SHEAR(1, false, false, false);

        /** getStackLimit, isOffensive, effectStacks, the redstone mode (prevents removal, not exchangeable with offensive). */
        public final int limit;
        public final boolean offensive;
        public final boolean stacks;
        public final boolean utility;

        Kind(int limit, boolean offensive, boolean stacks, boolean utility) {
            this.limit = limit;
            this.offensive = offensive;
            this.stacks = stacks;
            this.utility = utility;
        }
    }

    /** Behavior.getBehaviorFromStack (registerBuiltInBehaviors). */
    public static @Nullable Kind kind(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.is(Items.STICK)) {
            return Kind.DUMMY;
        }
        if (stack.is(Items.REDSTONE)) {
            return Kind.UTILITY;
        }
        if (stack.is(Items.GUNPOWDER) || stack.is(Items.TNT)) {
            return Kind.EXPLOSIVE;
        }
        if (stack.is(Items.FIREWORK_ROCKET)) {
            return Kind.FIREWORK;
        }
        if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION)) {
            return Kind.POTION;
        }
        if (stack.is(Items.FIRE_CHARGE)) {
            return Kind.FIREBALL;
        }
        if (stack.getItem() instanceof SpawnEggItem) {
            return Kind.SPAWN;
        }
        if (stack.is(Items.ARROW) || stack.is(Items.SNOWBALL) || stack.is(Items.ENDER_PEARL)) {
            return Kind.SHOOT;
        }
        if (stack.is(Items.SHEARS)) {
            return Kind.SHEAR;
        }
        if (stack.is(Items.TORCH)) {
            return Kind.BLOCK_THROW;
        }
        // every opaque full block without a block entity
        if (stack.getItem() instanceof BlockItem item && isOpaqueCube(item.getBlock().defaultBlockState())) {
            return Kind.BLOCK_THROW;
        }
        return null;
    }

    /** SlotOpaqueBlocksOnly / the block throw: isOpaqueCube && renderAsNormalBlock, no tile entity. */
    public static boolean isOpaqueCube(BlockState state) {
        return state.canOcclude() && state.isCollisionShapeFullBlock(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO)
                && !(state.getBlock() instanceof EntityBlock);
    }

    private final ServerLevel level;
    private final BlockPos pos;
    private final LandmineBlockEntity mine;
    private final Entity triggerer;

    LandmineLogic(ServerLevel level, BlockPos pos, LandmineBlockEntity mine, Entity triggerer) {
        this.level = level;
        this.pos = pos;
        this.mine = mine;
        this.triggerer = triggerer;
    }

    void explode() {
        boolean preventExplode = false;
        boolean isOffensive = true;
        boolean cancelDefault = false;
        List<ItemStack> stacks = new ArrayList<>();
        this.mine.isExploding = true;
        for (int i = 0; i < 3; i++) {
            ItemStack current = this.mine.getItem(i).copy();
            Kind kind = kind(current);
            if (kind == null) {
                continue;
            }
            if (kind.utility) {
                preventExplode = true;
                isOffensive = false;
            }
            if (kind == Kind.FIREWORK) {
                cancelDefault = true;
            }
            int index = indexOf(stacks, current);
            if (index >= 0 && kind.stacks) {
                stacks.get(index).grow(current.getCount());
            } else {
                stacks.add(current);
            }
        }
        if (isOffensive) {
            this.rocketFireball(stacks);
        }
        for (ItemStack current : stacks) {
            Kind kind = kind(current);
            if (kind != null && (isOffensive || !kind.offensive)) {
                this.execute(kind, current, !preventExplode);
                boolean remove = kind == Kind.UTILITY ? false : kind == Kind.SHEAR ? !preventExplode : true;
                if (remove) {
                    for (int i = 0; i < 3; i++) {
                        if (ItemStack.isSameItem(this.mine.getItem(i), current)) {
                            this.mine.setItem(i, ItemStack.EMPTY);
                            if (!kind.stacks) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (isOffensive && !cancelDefault) {
            // BehaviorDefault: 5 damage that bypasses armour
            this.triggerer.hurtServer(this.level, TDeco.landmineDamage(this.level), 5.0F);
        }
        if (preventExplode) {
            this.mine.isExploding = false;
            this.mine.setChanged();
        } else {
            this.level.removeBlock(this.pos, false);
        }
    }

    private static int indexOf(List<ItemStack> stacks, ItemStack stack) {
        for (int i = 0; i < stacks.size(); i++) {
            if (ItemStack.isSameItem(stacks.get(i), stack)) {
                return i;
            }
        }
        return -1;
    }

    /** Behavior.getFacing: away from the side the mine lies on. */
    private Direction facing() {
        BlockState state = this.level.getBlockState(this.pos);
        return state.hasProperty(LandmineBlock.ATTACH) ? state.getValue(LandmineBlock.ATTACH).getOpposite() : Direction.UP;
    }

    private void execute(Kind kind, ItemStack stack, boolean willBlockBeRemoved) {
        int x = this.pos.getX();
        int y = this.pos.getY();
        int z = this.pos.getZ();
        RandomSource random = this.level.getRandom();
        switch (kind) {
            case DUMMY, UTILITY -> {
            }
            case EXPLOSIVE -> {
                float strength = stack.is(Items.TNT) ? 4.0F * stack.getCount() : 0.8F * stack.getCount();
                this.level.explode(null, x, y, z, strength, Level.ExplosionInteraction.TNT);
            }
            case FIREWORK -> {
                for (int i = 0; i < stack.getCount(); i++) {
                    FireworkRocketEntity rocket = new FireworkRocketEntity(this.level, x, y, z, stack.copyWithCount(1));
                    this.level.addFreshEntity(rocket);
                    this.triggerer.startRiding(rocket);
                }
            }
            case BLOCK_THROW -> {
                BlockState state = stack.getItem() instanceof BlockItem item ? item.getBlock().defaultBlockState() : Blocks.AIR.defaultBlockState();
                for (int i = 0; i < stack.getCount(); i++) {
                    double speed = 2.0 * Math.random() + 0.01;
                    double vy = random.nextDouble() * 0.25;
                    double vx = 0.0;
                    double vz = 0.0;
                    switch (random.nextInt(8)) {
                        case 0 -> vx = speed + random.nextDouble() * 0.5;
                        case 1 -> vx = -speed + random.nextDouble() * 0.5;
                        case 2 -> vz = speed + random.nextDouble() * 0.5;
                        case 3 -> vz = -speed + random.nextDouble() * 0.5;
                        case 4 -> {
                            vx = speed + random.nextDouble() * 0.5;
                            vz = speed + random.nextDouble() * 0.5;
                        }
                        case 5 -> {
                            vx = speed + random.nextDouble() * 0.5;
                            vz = -speed + random.nextDouble() * 0.5;
                        }
                        case 6 -> {
                            vx = -speed + random.nextDouble() * 0.5;
                            vz = speed + random.nextDouble() * 0.5;
                        }
                        default -> {
                            vx = -speed;
                            vz = -speed;
                        }
                    }
                    FallingBlockEntity falling = fallingBlock(this.level, x + 0.5, y + 2.0, z + 0.5, state);
                    if (falling != null) {
                        falling.time = 2;
                        falling.setDeltaMovement(vx, vy, vz);
                        this.level.addFreshEntity(falling);
                    }
                }
            }
            case POTION -> {
                if (stack.is(Items.SPLASH_POTION)) {
                    this.level.playSound(null, this.triggerer, SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5F,
                            0.4F / (random.nextFloat() * 0.4F + 0.8F));
                    ThrownSplashPotion potion = this.triggerer instanceof LivingEntity living ? new ThrownSplashPotion(this.level, living, stack)
                            : new ThrownSplashPotion(this.level, x, y, z, stack);
                    this.level.addFreshEntity(potion);
                } else if (this.triggerer instanceof LivingEntity living) {
                    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                    contents.applyToLivingEntity(living, 1.0F);
                }
            }
            case FIREBALL -> {
                Direction face = this.facing();
                for (int n = 0; n < stack.getCount(); n++) {
                    this.triggerer.hurtServer(this.level, this.level.damageSources().generic(), 9.0F);
                    this.triggerer.igniteForSeconds(4.0F);
                    // the y of 1.5.2 took the x offset of the facing too
                    double d0 = x + face.getStepX() * 0.3F;
                    double d1 = y + face.getStepX() * 0.3F;
                    double d2 = z + face.getStepZ() * 0.3F;
                    Vec3 motion = new Vec3(random.nextGaussian() * 0.05 + face.getStepX(), random.nextGaussian() * 0.05 + face.getStepY(),
                            random.nextGaussian() * 0.05 + face.getStepZ());
                    this.level.addFreshEntity(new SmallFireball(this.level, d0, d1, d2, motion.normalize()));
                    for (int i = x - 2; i <= x + 2; i++) {
                        for (int j = z - 2; j <= z + 2; j++) {
                            BlockPos at = new BlockPos(i, y, j);
                            if (this.level.getBlockState(at).canBeReplaced() && random.nextInt(5) == 0 && BaseFireBlock.canBePlacedAt(this.level, at,
                                    Direction.UP)) {
                                this.level.setBlockAndUpdate(at, BaseFireBlock.getState(this.level, at));
                            }
                        }
                    }
                }
            }
            case SPAWN -> {
                Direction face = this.facing();
                EntityType<?> type = SpawnEggItem.getType(stack);
                if (type != null) {
                    Entity entity = type.create(this.level, EntitySpawnReason.SPAWN_ITEM_USE);
                    if (entity != null) {
                        entity.snapTo(x + Math.random() + face.getStepX(), y + 0.2F, z + Math.random() + face.getStepZ(), random.nextFloat() * 360.0F, 0.0F);
                        if (entity instanceof Mob mob) {
                            mob.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
                        }
                        if (stack.has(DataComponents.CUSTOM_NAME)) {
                            entity.setCustomName(stack.getHoverName());
                        }
                        this.level.addFreshEntity(entity);
                    }
                }
            }
            case SHOOT -> {
                Direction face = this.facing();
                Projectile projectile = null;
                if (stack.is(Items.ARROW)) {
                    Arrow arrow = new Arrow(this.level, x, y, z, new ItemStack(Items.ARROW), null);
                    arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                    projectile = arrow;
                } else if (stack.is(Items.SNOWBALL)) {
                    projectile = new Snowball(this.level, x, y, z, new ItemStack(Items.SNOWBALL));
                } else if (stack.is(Items.ENDER_PEARL)) {
                    if (this.triggerer instanceof LivingEntity living) {
                        projectile = new ThrownEnderpearl(this.level, living, new ItemStack(Items.ENDER_PEARL));
                        projectile.setPos(x, y, z);
                    } else {
                        ThrownEnderpearl pearl = EntityTypes.ENDER_PEARL.create(this.level, EntitySpawnReason.TRIGGERED);
                        if (pearl != null) {
                            pearl.setPos(x, y, z);
                            projectile = pearl;
                        }
                    }
                }
                if (projectile != null) {
                    projectile.shoot(face.getStepX(), face.getStepY() + 0.1F, face.getStepZ(), 1.1F, 6.0F);
                    this.level.addFreshEntity(projectile);
                    this.level.levelEvent(1002, this.pos, 0);
                }
            }
            case SHEAR -> {
                AABB box = new AABB(x - 2, y - 2, z - 2, x + 2, y + 2, z + 2);
                for (Entity entity : this.level.getEntities((Entity) null, box, e -> e instanceof net.neoforged.neoforge.common.IShearable)) {
                    net.neoforged.neoforge.common.IShearable sheep = (net.neoforged.neoforge.common.IShearable) entity;
                    if (sheep.isShearable(null, stack, this.level, entity.blockPosition()) && random.nextInt(2) == 0) {
                        List<ItemStack> drops = sheep.onSheared(null, stack, this.level, entity.blockPosition());
                        if (entity instanceof LivingEntity) {
                            stack.hurtAndBreak(1, this.level, null, item -> {
                            });
                        }
                        for (ItemStack drop : drops) {
                            this.dropItem(entity.blockPosition(), drop);
                        }
                    }
                }
                if (willBlockBeRemoved) {
                    this.dropItem(this.pos, stack);
                }
            }
        }
    }

    /** SpecialStackHandlerRocketFireball: each pair of a firework and a fire charge shoots a ghast fireball the triggerer rides. */
    private void rocketFireball(List<ItemStack> stacks) {
        int rocket = indexOf(stacks, new ItemStack(Items.FIREWORK_ROCKET));
        int charge = indexOf(stacks, new ItemStack(Items.FIRE_CHARGE));
        if (rocket < 0 || charge < 0) {
            return;
        }
        Direction face = this.facing();
        RandomSource random = this.level.getRandom();
        while (stacks.get(rocket).getCount() > 0 && stacks.get(charge).getCount() > 0) {
            double d0 = this.pos.getX() + face.getStepX() * 0.3F;
            double d1 = this.pos.getY() + face.getStepX() * 0.3F;
            double d2 = this.pos.getZ() + face.getStepZ() * 0.3F;
            Vec3 motion = new Vec3(random.nextGaussian() * 0.05 + face.getStepX(), random.nextGaussian() * 0.05 + face.getStepY(),
                    random.nextGaussian() * 0.05 + face.getStepZ()).normalize();
            LargeFireball fireball;
            if (this.triggerer instanceof LivingEntity living) {
                fireball = new LargeFireball(this.level, living, motion, 1);
            } else {
                fireball = EntityTypes.FIREBALL.create(this.level, EntitySpawnReason.TRIGGERED);
                if (fireball == null) {
                    return;
                }
                fireball.setDeltaMovement(motion.scale(0.1));
            }
            fireball.setPos(d0, d1, d2);
            this.level.addFreshEntity(fireball);
            if (this.triggerer instanceof LivingEntity) {
                this.triggerer.startRiding(fireball);
            }
            this.removeOne(Items.FIREWORK_ROCKET);
            stacks.get(rocket).shrink(1);
            this.removeOne(Items.FIRE_CHARGE);
            stacks.get(charge).shrink(1);
        }
    }

    private void removeOne(net.minecraft.world.item.Item item) {
        for (int i = 0; i < 3; i++) {
            if (this.mine.getItem(i).is(item)) {
                this.mine.removeItem(i, 1);
                return;
            }
        }
    }

    private void dropItem(BlockPos at, ItemStack stack) {
        if (this.level.getGameRules().get(GameRules.BLOCK_DROPS)) {
            float f = 0.7F;
            double d0 = this.level.getRandom().nextFloat() * f + (1.0F - f) * 0.5;
            double d1 = this.level.getRandom().nextFloat() * f + (1.0F - f) * 0.5;
            double d2 = this.level.getRandom().nextFloat() * f + (1.0F - f) * 0.5;
            ItemEntity item = new ItemEntity(this.level, at.getX() + d0, at.getY() + d1, at.getZ() + d2, stack.copy());
            item.setPickUpDelay(10);
            this.level.addFreshEntity(item);
        }
    }

    private static java.lang.reflect.@Nullable Constructor<FallingBlockEntity> fallingConstructor;

    /** The private FallingBlockEntity(level, x, y, z, state): fall() would take the block out of the world. */
    private static @Nullable FallingBlockEntity fallingBlock(ServerLevel level, double x, double y, double z, BlockState state) {
        try {
            if (fallingConstructor == null) {
                fallingConstructor = FallingBlockEntity.class.getDeclaredConstructor(Level.class, double.class, double.class, double.class, BlockState.class);
                fallingConstructor.setAccessible(true);
            }
            return fallingConstructor.newInstance(level, x, y, z, state);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
