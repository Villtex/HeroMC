package projectzulu.client.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

/**
 * ModelMummy and ModelMummyPharaoh (identical): a ModelBiped (64 x 32) holding both arms straight forward like the
 * 1.5.2 zombie, swaying slightly, swinging them on attack.
 */
public class MummyModel extends HumanoidModel<HumanoidRenderState> {
    public MummyModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        float swing = Mth.sin(state.swingAnimation * Mth.PI);
        float swing2 = Mth.sin((1.0F - (1.0F - state.swingAnimation) * (1.0F - state.swingAnimation)) * Mth.PI);
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightArm.yRot = -(0.1F - swing * 0.6F);
        this.leftArm.yRot = 0.1F - swing * 0.6F;
        this.rightArm.xRot = -Mth.HALF_PI - (swing * 1.2F - swing2 * 0.4F);
        this.leftArm.xRot = -Mth.HALF_PI - (swing * 1.2F - swing2 * 0.4F);
        this.rightArm.zRot += Mth.cos(state.ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(state.ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(state.ageInTicks * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(state.ageInTicks * 0.067F) * 0.05F;
    }
}
