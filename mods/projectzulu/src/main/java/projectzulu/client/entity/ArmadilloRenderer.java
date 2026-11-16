package projectzulu.client.entity;

import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import projectzulu.ProjectZulu;
import projectzulu.entity.ArmadilloEntity;

/** RenderGenericLiving for the armadillo, with the easing of ModelArmadillo.setLivingAnimations per entity. */
public class ArmadilloRenderer extends AgeableMobRenderer<ArmadilloEntity, ArmadilloModel.State, ArmadilloModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "textures/entity/armadillo.png");

    private final EasingCache<Easing> easing = new EasingCache<>(entity -> new Easing());

    private static final class Easing {
        final Eased whole = new Eased(0.0F);
        final Eased rear = new Eased(0.0F);
        final Eased tail = new Eased(0.0F);
        final Eased tailZ = new Eased(4.0F);
        final Eased legZ = new Eased(5.0F);
        final Eased head = new Eased(0.0F);
    }

    public ArmadilloRenderer(EntityRendererProvider.Context context) {
        super(context, new ArmadilloModel(context.bakeLayer(ZuluEntityRenderers.ARMADILLO_LAYER)),
                new ArmadilloModel(context.bakeLayer(ZuluEntityRenderers.ARMADILLO_BABY_LAYER)), 0.5F);
    }

    @Override
    public ArmadilloModel.State createRenderState() {
        return new ArmadilloModel.State();
    }

    @Override
    public void extractRenderState(ArmadilloEntity entity, ArmadilloModel.State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        Easing eased = this.easing.get(entity);
        boolean charging = entity.isCharging();
        state.curled = entity.isCovering() || charging;
        if (state.curled) {
            float animSpeed = charging ? 5.0F : 2.0F;
            state.headX = eased.head.step(0.1F, 0.0F, 2.5307274F);
            if (charging) {
                // rolling: loop the full turns so the shell keeps spinning
                if (eased.whole.value == 13.08997F) {
                    eased.whole.value -= Mth.TWO_PI;
                }
                state.wholeX = eased.whole.step(0.09F * animSpeed, 0.0F, 13.08997F);
            } else {
                state.wholeX = eased.whole.step(0.09F * animSpeed, 0.0F, 6.806784F);
            }
            state.rearX = eased.rear.step(-0.01F * animSpeed, -0.7679449F, 0.0F);
            state.tailX = eased.tail.step(0.1F, 0.0F, Mth.HALF_PI);
            state.tailZ = eased.tailZ.step(-0.5F * animSpeed, 2.0F, 4.0F);
            state.rearLegZ = eased.legZ.step(-0.5F * animSpeed, -4.0F, 4.0F);
        } else {
            float animSpeed = 2.0F;
            state.wholeX = eased.whole.step(-0.09F * animSpeed, 0.0F, 13.08997F);
            state.rearX = eased.rear.step(0.01F * animSpeed, -0.7679449F, 0.0F);
            state.tailX = eased.tail.step(-0.1F * animSpeed, -Mth.PI / 12.0F, Mth.PI / 6.0F);
            state.tailZ = eased.tailZ.step(0.5F * animSpeed, 0.0F, 4.0F);
            state.rearLegZ = eased.legZ.step(0.5F * animSpeed, -4.0F, 4.0F);
        }
    }

    @Override
    public Identifier getTextureLocation(ArmadilloModel.State state) {
        return TEXTURE;
    }
}
