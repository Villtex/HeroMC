package twilightforest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.gen.TFFeature;
import twilightforest.world.TFChunkGenerator;

import java.util.Map;

/** Helpers the Twilight mobs share: 1.5.2 light, the feature grid at a position, 1.5.2 weapon damage, particles. */
public final class TFMobUtil {
    private TFMobUtil() {
    }

    /** World.getLightBrightness of 1.5.2 (the default table): raw light 0..15 to 0..1. */
    public static float lightBrightness(LevelReader level, BlockPos pos) {
        int light = level.getMaxLocalRawBrightness(pos);
        float f = 1.0F - light / 15.0F;
        return (1.0F - f) / (f * 3.0F + 1.0F);
    }

    /** Entity.getBrightness of 1.5.2 at the entity's feet plus 66 % of its height. */
    public static float brightness(Entity entity) {
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().minY + entity.getBbHeight() * 0.66, entity.getZ());
        return lightBrightness(entity.level(), pos);
    }

    /** TFFeature.getNearestFeature of the chunk the entity stands in; NOTHING outside the Twilight Forest. */
    public static TFFeature nearestFeature(Entity entity) {
        return nearestFeature(entity.level(), entity.blockPosition());
    }

    public static TFFeature nearestFeature(LevelReader levelReader, BlockPos pos) {
        if (levelReader instanceof net.minecraft.world.level.WorldGenLevel gen && gen.getLevel().getChunkSource().getGenerator() instanceof TFChunkGenerator generator) {
            return TFFeature.getNearestFeature(pos.getX() >> 4, pos.getZ() >> 4, generator.world().layout());
        }
        if (levelReader instanceof ServerLevel server && server.getChunkSource().getGenerator() instanceof TFChunkGenerator generator) {
            return TFFeature.getNearestFeature(pos.getX() >> 4, pos.getZ() >> 4, generator.world().layout());
        }
        return TFFeature.NOTHING;
    }

    private static final Map<net.minecraft.world.item.Item, Integer> OLD_DAMAGE = Map.ofEntries(
            Map.entry(Items.WOODEN_SWORD, 4), Map.entry(Items.STONE_SWORD, 5), Map.entry(Items.IRON_SWORD, 6),
            Map.entry(Items.DIAMOND_SWORD, 7), Map.entry(Items.GOLDEN_SWORD, 4),
            Map.entry(Items.WOODEN_AXE, 3), Map.entry(Items.STONE_AXE, 4), Map.entry(Items.IRON_AXE, 5),
            Map.entry(Items.DIAMOND_AXE, 6), Map.entry(Items.GOLDEN_AXE, 3),
            Map.entry(Items.WOODEN_PICKAXE, 2), Map.entry(Items.STONE_PICKAXE, 3), Map.entry(Items.IRON_PICKAXE, 4),
            Map.entry(Items.DIAMOND_PICKAXE, 5), Map.entry(Items.GOLDEN_PICKAXE, 2),
            Map.entry(Items.WOODEN_SHOVEL, 1), Map.entry(Items.STONE_SHOVEL, 2), Map.entry(Items.IRON_SHOVEL, 3),
            Map.entry(Items.DIAMOND_SHOVEL, 4), Map.entry(Items.GOLDEN_SHOVEL, 1),
            Map.entry(Items.WOODEN_HOE, 1), Map.entry(Items.STONE_HOE, 1), Map.entry(Items.IRON_HOE, 1),
            Map.entry(Items.DIAMOND_HOE, 1), Map.entry(Items.GOLDEN_HOE, 1));

    /**
     * ItemStack.getDamageVsEntity of 1.5.2 for the vanilla tools; 1 for anything else (empty hand, blocks). The ironwood
     * tools (Teil 24f) count as iron.
     */
    public static int oldWeaponDamage(ItemStack stack) {
        if (stack.isEmpty()) {
            return 1;
        }
        Integer old = OLD_DAMAGE.get(stack.getItem());
        if (old != null) {
            return old;
        }
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (path.equals("ironwood_pickaxe")) {
            return 4;
        }
        if (path.equals("ironwood_sword")) {
            return 6;
        }
        return 1;
    }

    /** spawnParticle of 1.5.2 seen by everyone near: one particle with the given motion. */
    public static void particle(Entity near, ParticleOptions type, double x, double y, double z, double vx, double vy, double vz) {
        if (near.level() instanceof ServerLevel server) {
            server.sendParticles(type, x, y, z, 0, vx, vy, vz, 1.0);
        } else {
            near.level().addParticle(type, x, y, z, vx, vy, vz);
        }
    }

    /** isTargetLookingAtMe of the redcaps and the boggard: the target's yaw within 60 degrees of the line to me. */
    public static boolean isLookingAt(LivingEntity target, Entity me) {
        double dx = me.getX() - target.getX();
        double dz = me.getZ() - target.getZ();
        float angle = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float difference = Math.abs((target.getYRot() - angle) % 360.0F);
        return difference < 60.0F || difference > 300.0F;
    }

    /** Block.isLeaves or isBlockSolidOnSide(UP) of 1.5.2. */
    public static boolean isLandable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(net.minecraft.tags.BlockTags.LEAVES) || state.isFaceSturdy(level, pos, net.minecraft.core.Direction.UP);
    }

    /** A vanilla sound that 26.3 keeps only per variant (cow, pig, wolf): looked up by its id. */
    public static net.minecraft.sounds.SoundEvent vanillaSound(String id) {
        return net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(id));
    }

    public static float wrap(float degrees) {
        return Mth.wrapDegrees(degrees);
    }
}
