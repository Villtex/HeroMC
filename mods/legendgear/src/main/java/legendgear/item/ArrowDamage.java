package legendgear.item;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

/** EntityArrow.getDamage: the base damage is private in 26.3 without a getter, so it is read by reflection. */
final class ArrowDamage {
    private static java.lang.reflect.Field field;

    private ArrowDamage() {
    }

    static double get(AbstractArrow arrow) {
        try {
            if (field == null) {
                field = AbstractArrow.class.getDeclaredField("baseDamage");
                field.setAccessible(true);
            }
            return field.getDouble(arrow);
        } catch (ReflectiveOperationException e) {
            return 2.0;
        }
    }
}
