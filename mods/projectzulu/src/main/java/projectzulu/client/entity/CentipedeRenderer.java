package projectzulu.client.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import projectzulu.entity.CentipedeEntity;

/**
 * RenderLiving for the centipede head and, in the same model, the nine body segments (RenderLiving of the
 * EntityFollower in the original). The segment positions of the part entities are turned into the head's model
 * space: undo the body rotation (180 - bodyRot) and the model flip (x, y negated), in pixels, feet at y 24.
 */
public class CentipedeRenderer extends MobRenderer<CentipedeEntity, CentipedeModel.State, CentipedeModel> {
    private static final Identifier TEXTURE = MonsterRenderer.tex("serpent");

    public CentipedeRenderer(EntityRendererProvider.Context context) {
        super(context, new CentipedeModel(context.bakeLayer(MonsterRenderers.CENTIPEDE_LAYER)), 0.5F);
    }

    @Override
    public CentipedeModel.State createRenderState() {
        return new CentipedeModel.State();
    }

    @Override
    public void extractRenderState(CentipedeEntity entity, CentipedeModel.State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        CentipedeEntity.Segment[] segments = entity.getSegments();
        state.hasSegments = entity.tickCount > 1;
        float angle = (180.0F - state.bodyRot) * Mth.DEG_TO_RAD;
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        float scale = state.scale;
        for (int i = 0; i < segments.length; i++) {
            CentipedeEntity.Segment segment = segments[i];
            double dx = Mth.lerp(partialTick, segment.xo, segment.getX()) - state.x;
            double dy = Mth.lerp(partialTick, segment.yo, segment.getY()) - state.y;
            double dz = Mth.lerp(partialTick, segment.zo, segment.getZ()) - state.z;
            // inverse of the rotation about Y by "angle"
            double qx = dx * cos - dz * sin;
            double qz = dx * sin + dz * cos;
            state.segX[i] = (float) (-qx * 16.0 / scale);
            state.segY[i] = (float) ((1.501 - dy / scale) * 16.0);
            state.segZ[i] = (float) (qz * 16.0 / scale);
            float yaw = Mth.rotLerp(partialTick, segment.yRotO, segment.getYRot());
            state.segYaw[i] = Mth.wrapDegrees(yaw - state.bodyRot) * Mth.DEG_TO_RAD;
        }
    }

    /** The body trails up to ~6 blocks behind the head. */
    @Override
    protected AABB getBoundingBoxForCulling(CentipedeEntity entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(6.5);
    }

    @Override
    public Identifier getTextureLocation(CentipedeModel.State state) {
        return TEXTURE;
    }
}
