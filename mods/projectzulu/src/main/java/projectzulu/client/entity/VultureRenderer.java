package projectzulu.client.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import projectzulu.entity.AerialAnimal.EntityState;
import projectzulu.entity.VultureEntity;

/** The vulture with the per-entity easing of ModelVulture.setLivingAnimations (EntityModelRotation fields). */
public class VultureRenderer extends AerialRenderer<VultureEntity, VultureModel.State> {
    private static final float PI = (float) Math.PI;

    private final EasingCache<Easing> easing = new EasingCache<>(entity -> new Easing());

    /** rotateZ(increment, min, max, 2) of EntityModelRotation: bounces between the limits. */
    private static final class PingPong {
        float value;
        int direction = 1;

        float step(float increment, float min, float max) {
            if (this.value + increment * this.direction >= max) {
                this.direction *= -1;
                this.value = max;
            } else if (this.value + increment * this.direction <= min) {
                this.direction *= -1;
                this.value = min;
            } else {
                this.value += increment * this.direction;
            }
            return this.value;
        }

        /** Mode 0: plain clamp (the direction is kept for the next ping-pong). */
        float clamp(float increment, float min, float max) {
            this.value = Math.max(Math.min(this.value + increment, max), min);
            return this.value;
        }
    }

    private static final class Easing {
        final Eased feet = new Eased(0.0F);
        final Eased neck1 = new Eased(0.0F);
        final Eased neck2 = new Eased(0.0F);
        final Eased neck3 = new Eased(0.0F);
        final PingPong leftWing = new PingPong();
        final PingPong rightWing = new PingPong();
        final Eased body = new Eased(0.0F);
        float headBase;
    }

    public VultureRenderer(EntityRendererProvider.Context context) {
        super(context, new VultureModel(context.bakeLayer(ZuluEntityRenderers.VULTURE_LAYER)), VultureModel.State::new, "vulture");
    }

    @Override
    public void extractRenderState(VultureEntity entity, VultureModel.State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        Easing e = this.easing.get(entity);
        float animSpeed = 2.0F;
        if (state.onGround) {
            e.feet.step(0.09F * animSpeed, -PI / 2.0F, 0.0F);
            e.neck1.step(0.09F * animSpeed, -PI / 6.0F, PI / 6.0F);
            e.neck2.step(-0.09F * animSpeed, PI / 12.0F, PI * 2.0F / 9.0F);
            e.neck3.step(-0.09F * animSpeed, 0.61086524F, PI / 3.0F);
            e.leftWing.clamp(-0.09F * animSpeed, 0.0F, PI / 2.0F);
            e.rightWing.clamp(0.09F * animSpeed, -PI / 2.0F, 0.0F);
            e.body.step(-0.09F * animSpeed, 0.0F, PI / 12.0F);
            e.headBase = -1.2217305F;
        } else if (state.entityState == EntityState.ATTACKING || state.entityState == EntityState.FOLLOWING) {
            e.feet.step(-0.09F * animSpeed, -PI / 2.0F, PI / 4.0F);
            e.neck1.step(-0.09F * animSpeed, -PI / 6.0F, PI / 6.0F);
            e.neck2.step(0.09F * animSpeed, PI / 9.0F, PI * 2.0F / 9.0F);
            e.neck3.step(0.09F * animSpeed, PI * 2.0F / 9.0F, PI / 3.0F);
            e.leftWing.step(0.09F * animSpeed, 0.0F, PI / 2.0F);
            e.rightWing.step(-0.09F * animSpeed, -PI / 2.0F, 0.0F);
            e.body.step(-0.09F * animSpeed, PI / 12.0F, PI * 2.0F / 9.0F);
            e.headBase = -PI / 3.0F;
        } else if (state.entityState == EntityState.IDLE) {
            e.feet.step(0.09F * animSpeed, -PI / 2.0F, 0.9599311F);
            e.neck1.step(0.09F * animSpeed, -PI / 6.0F, PI / 6.0F);
            e.neck2.step(-0.09F * animSpeed, 0.08726646F, PI * 2.0F / 9.0F);
            e.neck3.step(-0.09F * animSpeed, PI / 18.0F, PI / 3.0F);
            e.leftWing.step(0.09F * animSpeed, 0.0F, PI / 2.0F);
            e.rightWing.step(-0.09F * animSpeed, -PI / 2.0F, 0.0F);
            e.body.step(0.09F * animSpeed, 0.0F, PI / 3.0F);
            e.headBase = -PI * 4.0F / 9.0F;
        }
        state.feetX = e.feet.value;
        state.neck1X = e.neck1.value;
        state.neck2X = e.neck2.value;
        state.neck3X = e.neck3.value;
        state.leftWingZ = e.leftWing.value;
        state.rightWingZ = e.rightWing.value;
        state.bodyX = e.body.value;
        state.headBaseX = e.headBase;
    }
}
