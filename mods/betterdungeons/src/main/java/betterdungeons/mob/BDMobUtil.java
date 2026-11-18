package betterdungeons.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Field;
import java.util.Map;

/** Helpers of the human mobs: BDHelper's angles, 1.5.2 light and weapon values, teleports, server particles. */
public final class BDMobUtil {
    private BDMobUtil() {
    }

    /** BDHelper.getRotationDiffBetweenEntity: |((a.yaw - b.yaw) mod 360) - 180|, 0 when they face each other. */
    public static double rotationDiff(Entity a, Entity b) {
        double angle = a.getYRot() - b.getYRot();
        while (angle > 360.0) {
            angle -= 360.0;
        }
        while (angle < 0.0) {
            angle += 360.0;
        }
        return Math.abs(angle - 180.0);
    }

    /** BDHelper.getAngleBetweenEntities. */
    public static double angleBetween(Entity entity, Entity target) {
        double d = entity.getX() - target.getX();
        double d2 = entity.getZ() - target.getZ();
        double angle = Math.toDegrees(Math.atan2(d, d2));
        return -Mth.wrapDegrees(angle - 180.0);
    }

    /** World.getLightBrightness of 1.5.2 (overworld table): raw light 0..15 to 0..1, darker than linear. */
    public static float lightBrightness(Level level, BlockPos pos) {
        int light = level.getMaxLocalRawBrightness(pos);
        float f = 1.0F - light / 15.0F;
        return (1.0F - f) / (f * 3.0F + 1.0F);
    }

    /** isSneaking of 1.5.2: the humans' own sneak flag (goblins, assassins), the shift key of everything else. */
    public static boolean isSneaking(Entity e) {
        return e instanceof HumanEntity h ? h.isSneakingHuman() : e.isShiftKeyDown();
    }

    private static final Map<net.minecraft.world.item.Item, Integer> OLD_DAMAGE = Map.ofEntries(
            Map.entry(Items.WOODEN_SWORD, 4), Map.entry(Items.STONE_SWORD, 5), Map.entry(Items.IRON_SWORD, 6),
            Map.entry(Items.DIAMOND_SWORD, 7), Map.entry(Items.GOLDEN_SWORD, 4),
            Map.entry(Items.WOODEN_AXE, 3), Map.entry(Items.STONE_AXE, 4), Map.entry(Items.IRON_AXE, 5),
            Map.entry(Items.DIAMOND_AXE, 6), Map.entry(Items.GOLDEN_AXE, 3),
            Map.entry(Items.WOODEN_PICKAXE, 2), Map.entry(Items.STONE_PICKAXE, 3), Map.entry(Items.IRON_PICKAXE, 4),
            Map.entry(Items.DIAMOND_PICKAXE, 5), Map.entry(Items.GOLDEN_PICKAXE, 2),
            Map.entry(Items.WOODEN_SHOVEL, 1), Map.entry(Items.STONE_SHOVEL, 2), Map.entry(Items.IRON_SHOVEL, 3),
            Map.entry(Items.DIAMOND_SHOVEL, 4), Map.entry(Items.GOLDEN_SHOVEL, 1));

    /**
     * ItemStack.getDamageVsEntity of 1.5.2: the vanilla tools by their 1.5.2 values (axes and pickaxes were much weaker),
     * every other item 1 + its main hand attack damage modifiers (the ported items carry their 1.5.2 value - 1 there).
     */
    public static int oldWeaponDamage(ItemStack stack) {
        if (stack.isEmpty()) {
            return 1;
        }
        Integer old = OLD_DAMAGE.get(stack.getItem());
        if (old != null) {
            return old;
        }
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        double[] sum = {0.0};
        modifiers.forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                sum[0] += modifier.amount();
            }
        });
        return 1 + (int) sum[0];
    }

    /** spawnParticle of 1.5.2 seen by everyone: count 0 sends one particle with the given motion. */
    public static void particle(Entity near, ParticleOptions type, double x, double y, double z, double vx, double vy, double vz) {
        if (near.level() instanceof ServerLevel server) {
            server.sendParticles(type, x, y, z, 0, vx, vy, vz, 1.0);
        }
    }

    /** n particles of a kind around the entity at (+-0.5, 1 + 0..1, +-0.5), as many attackEntityFrom overrides did. */
    public static void hurtParticles(Entity e, ParticleOptions type, int n) {
        for (int r = 0; r < n; r++) {
            particle(e, type, e.getX() + e.getRandom().nextFloat() - 0.5, e.getY() + 1.0 + e.getRandom().nextFloat(),
                    e.getZ() + e.getRandom().nextFloat() - 0.5, 0.0, 0.0, 0.0);
        }
    }

    /** PacketSpawnParticle (heart 0, crit 1): seven particles at the block of the entity. */
    public static void packetParticles(Entity e, boolean crit) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        int z = (int) e.getZ();
        for (int i = 0; i < 7; i++) {
            particle(e, crit ? ParticleTypes.CRIT : ParticleTypes.HEART, x + e.getRandom().nextDouble() - 0.5, y + 1,
                    z + e.getRandom().nextDouble() - 0.5, e.getRandom().nextGaussian() * 0.02, e.getRandom().nextGaussian() * 0.02,
                    e.getRandom().nextGaussian() * 0.02);
        }
    }

    /**
     * The teleports of the enderman, the bosses and the mages (teleportTo / castTeleport): to (x, y, z), down to the first
     * block that blocks movement, only if the box is free and dry there. Plays the portal sound at both ends.
     *
     * @param portal 128 portal particles along the way (the enderman, the mages); the bosses showed large smoke instead
     */
    public static boolean teleport(Mob mob, double x, double y, double z, boolean portal, boolean smoke) {
        double oldX = mob.getX();
        double oldY = mob.getY();
        double oldZ = mob.getZ();
        Level level = mob.level();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        boolean ok = false;
        if (level.hasChunkAt(pos)) {
            boolean ground = false;
            while (!ground && pos.getY() > level.getMinY()) {
                BlockPos below = pos.below();
                if (level.getBlockState(below).isSolid()) {
                    ground = true;
                } else {
                    y--;
                    pos.setY(pos.getY() - 1);
                }
            }
            if (ground) {
                AABB box = mob.getDimensions(mob.getPose()).makeBoundingBox(x, y, z);
                ok = level.noCollision(mob, box) && !level.containsAnyLiquid(box);
            }
        }
        if (!ok) {
            return false;
        }
        if (smoke) {
            for (int n = 0; n < 10; n++) {
                particle(mob, ParticleTypes.LARGE_SMOKE, oldX + mob.getRandom().nextFloat() - 0.5, oldY, oldZ + mob.getRandom().nextFloat() - 0.5,
                        1.0, 1.0, 1.0);
            }
        }
        mob.teleportTo(x, y, z);
        mob.getNavigation().stop();
        if (portal) {
            for (int i = 0; i < 128; i++) {
                double f = i / 127.0;
                particle(mob, ParticleTypes.PORTAL, oldX + (x - oldX) * f + (mob.getRandom().nextDouble() - 0.5) * mob.getBbWidth() * 2.0,
                        oldY + (y - oldY) * f + mob.getRandom().nextDouble() * mob.getBbHeight(),
                        oldZ + (z - oldZ) * f + (mob.getRandom().nextDouble() - 0.5) * mob.getBbWidth() * 2.0,
                        (mob.getRandom().nextFloat() - 0.5F) * 0.2F, (mob.getRandom().nextFloat() - 0.5F) * 0.2F,
                        (mob.getRandom().nextFloat() - 0.5F) * 0.2F);
            }
        }
        level.playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
        mob.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return true;
    }

    private static Field goalSelectorField;

    /** Mob.goalSelector of another mob (the pigs and cows the humans ride get AIAnimalMountedByEntity). */
    public static GoalSelector goals(Mob mob) {
        try {
            if (goalSelectorField == null) {
                goalSelectorField = Mob.class.getDeclaredField("goalSelector");
                goalSelectorField.setAccessible(true);
            }
            return (GoalSelector) goalSelectorField.get(mob);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** distanceTo between living entities, as getDistanceToEntity. */
    public static float dist(LivingEntity a, Entity b) {
        return a.distanceTo(b);
    }
}
