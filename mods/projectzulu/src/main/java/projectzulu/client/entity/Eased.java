package projectzulu.client.entity;

import net.minecraft.util.Mth;

/**
 * Port of EntityModelRotation: a value that walks toward a clamped target by a fixed step each frame. The original
 * stored these on the entity; here each renderer keeps one easing object per entity (see {@link EasingCache}).
 */
public final class Eased {
    public float value;

    public Eased(float initial) {
        this.value = initial;
    }

    /** rotateX/translateZ of the original: add increment, clamp to [min, max]. */
    public float step(float increment, float min, float max) {
        this.value = Mth.clamp(this.value + increment, min, max);
        return this.value;
    }
}
