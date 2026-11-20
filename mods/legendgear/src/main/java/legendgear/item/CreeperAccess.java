package legendgear.item;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Creeper;

/** The data watcher 17 of 1.5.2 (powered) that the magic powder sets; private in 26.3, so read once by reflection. */
final class CreeperAccess {
    private static EntityDataAccessor<Boolean> powered;

    private CreeperAccess() {
    }

    @SuppressWarnings("unchecked")
    static void power(Creeper creeper) {
        try {
            if (powered == null) {
                java.lang.reflect.Field field = Creeper.class.getDeclaredField("DATA_IS_POWERED");
                field.setAccessible(true);
                powered = (EntityDataAccessor<Boolean>) field.get(null);
            }
            creeper.getEntityData().set(powered, true);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Creeper.DATA_IS_POWERED", e);
        }
    }
}
