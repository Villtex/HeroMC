package twilightforest.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

import java.util.Random;

/** ModelTFGhast: a 16³ body with nine tentacles of random length (Random(1660)), swinging; the body turns with the head. */
public class TowerGhastModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart body;
    private final ModelPart[] tentacles = new ModelPart[9];

    public TowerGhastModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        for (int i = 0; i < this.tentacles.length; i++) {
            this.tentacles[i] = this.body.getChild("tentacle" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        int yOffset = -16;
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 24 + yOffset, 0.0F));
        Random rand = new Random(1660L);
        for (int i = 0; i < 9; i++) {
            float xPoint = ((i % 3 - i / 3 % 2 * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
            float zPoint = (i / 3 / 2.0F * 2.0F - 1.0F) * 5.0F;
            int length = rand.nextInt(7) + 8;
            body.addOrReplaceChild("tentacle" + i, CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, length, 2.0F),
                    PartPose.offset(xPoint, 23 + yOffset, zPoint));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        for (int i = 0; i < this.tentacles.length; i++) {
            this.tentacles[i].xRot = 0.2F * Mth.sin(state.ageInTicks * 0.3F + i) + 0.4F;
        }
        this.body.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.body.yRot = state.yRot * Mth.DEG_TO_RAD;
    }
}
