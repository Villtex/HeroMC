package projectzulu.client.entity;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Helpers for the Batch C models. The 1.5.2 models rendered their top-level parts at a scale (render(f5 * s)) and
 * drew babies with their own glScalef/glTranslatef per part group (the head usually larger than half size). Both come
 * down to point = k * (rotation point + rotated box) + t, which an empty group part with offset t and scale k
 * reproduces exactly; the original top-level parts hang below such groups.
 */
final class BigModelParts {
    private BigModelParts() {
    }

    /** A group part: everything below it is scaled by k about the model origin and moved by t (in pixels). */
    static PartDefinition group(PartDefinition root, String name, Transform transform) {
        return root.addOrReplaceChild(name, CubeListBuilder.create(),
                new PartPose(transform.tx, transform.ty, transform.tz, 0.0F, 0.0F, 0.0F, transform.k, transform.k, transform.k));
    }

    /** ModelHelper.abs(Math.log(amount + 1)): the dampened walk amplitude of the Project Zulu models. */
    static float logSwing(float amount) {
        double value = Math.log(amount + 1.0F);
        return value < 0.0 ? 0.0F : (float) value;
    }

    /** ModelHelper.mapValueofSet1ToSet2. */
    static float map(float value, float min1, float max1, float min2, float max2) {
        return (value - min1) * ((max2 - min2) / (max1 - min1)) + min2;
    }

    static float clampDeg(float degrees, float min, float max) {
        return Math.min(Math.max(degrees, min), max) * Mth.DEG_TO_RAD;
    }

    /** Scale k, then translation (tx, ty, tz) in model pixels. */
    record Transform(float k, float tx, float ty, float tz) {
        static Transform scale(float k) {
            return new Transform(k, 0.0F, 0.0F, 0.0F);
        }

        /** glScalef(1 / 2) + glTranslatef(0, 24 * f5, 0): the usual half-size baby body standing on the ground. */
        static final Transform HALF_BODY = new Transform(0.5F, 0.0F, 12.0F, 0.0F);
    }
}
