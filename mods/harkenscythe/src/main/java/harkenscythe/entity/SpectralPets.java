package harkenscythe.entity;

import harkenscythe.HarkenScythe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/**
 * Where the core meets the mobs of Teil 9, part 3 (spectral pets, spectral miners, the Harbinger and the ectoplasm and
 * hemoglobin slimes). The core only calls these methods; part 3 sets {@link #hooks} in its mod constructor and puts its
 * entity types into the entity tags {@code harkenscythe_core:spectral_pets} (no souls, no blood, never raised again),
 * {@code harkenscythe_core:harbingers} (may hurt souls; kills with a Harbinger go to {@link Hooks#harbingerSoulReap})
 * and {@code harkenscythe_core:soulless} (essence slimes and the evil spectral miner leave no soul). Until then every
 * hook does nothing and returns false.
 */
public final class SpectralPets {
    /** The mob side of the core. All methods run on the server. */
    public interface Hooks {
        /** EntityHSSoul.createSpectralPet: raises the soul of a {@code fallenSoulName} mob ("Chicken", "Cat_Black", ...). */
        default boolean create(String fallenSoulName, ServerLevel level, double x, double y, double z, RandomSource random, boolean needsMaster) {
            return false;
        }

        /** EntityHSSoulLost.createSpectralPet: the Necronomicon raises a lost soul as a (friendly) spectral miner. */
        default boolean createMiner(ServerLevel level, double x, double y, double z, RandomSource random) {
            return false;
        }

        /** A keeper used on a lost soul turns it into an evil spectral miner (EntityHSSoulLost.interact). */
        default boolean spawnEvilMiner(ServerLevel level, double x, double y, double z) {
            return false;
        }

        /** A broken crucible frees its content as an ectoplasm (souls) or hemoglobin (blood) slime of size 1-3 worth {@code worth}. */
        default boolean spawnEssenceSlime(ServerLevel level, double x, double y, double z, int size, int worth, boolean blood) {
            return false;
        }

        /** EventHookContainerClassHarkenScythe.harbingerSoulReap: a Harbinger killed {@code victim}. */
        default void harbingerSoulReap(LivingEntity harbinger, LivingEntity victim) {
        }

        /** A spectral miner holding a torch keeps soul lights alive (BlockHSSoulLight). */
        default boolean keepsSoulLight(LivingEntity entity) {
            return false;
        }
    }

    public static Hooks hooks = new Hooks() {
    };

    private SpectralPets() {
    }

    public static boolean create(String fallenSoulName, ServerLevel level, double x, double y, double z, RandomSource random, boolean needsMaster) {
        return !"null".equals(fallenSoulName) && hooks.create(fallenSoulName, level, x, y, z, random, needsMaster);
    }

    public static boolean createMiner(ServerLevel level, double x, double y, double z, RandomSource random) {
        return hooks.createMiner(level, x, y, z, random);
    }

    public static boolean spawnEvilMiner(ServerLevel level, double x, double y, double z) {
        return hooks.spawnEvilMiner(level, x, y, z);
    }

    public static boolean spawnEssenceSlime(ServerLevel level, double x, double y, double z, int size, int worth, boolean blood) {
        return hooks.spawnEssenceSlime(level, x, y, z, size, worth, blood);
    }

    public static boolean keepsSoulLight(LivingEntity entity) {
        return hooks.keepsSoulLight(entity);
    }

    /** EventHookContainerClassHarkenScythe.isSpectralPet. */
    public static boolean isSpectralPet(@Nullable Entity entity) {
        return entity != null && entity.getType().builtInRegistryHolder().is(HarkenScythe.SPECTRAL_PETS);
    }

    public static boolean isHarbinger(@Nullable Entity entity) {
        return entity != null && entity.getType().builtInRegistryHolder().is(HarkenScythe.HARBINGERS);
    }

    public static boolean isSoulless(@Nullable Entity entity) {
        return entity != null && entity.getType().builtInRegistryHolder().is(HarkenScythe.SOULLESS);
    }
}
