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

/**
 * ModelTFDeathTome: the enchanting-table book of 1.5.2 (ModelBook), tilted, bobbing and flapping, with four loose
 * pages circling it; the whole turns with the head (rotate + 90°).
 */
public class DeathTomeModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart everything;
    private final ModelPart book;
    private final ModelPart coverRight;
    private final ModelPart coverLeft;
    private final ModelPart pagesRight;
    private final ModelPart pagesLeft;
    private final ModelPart flippingPageRight;
    private final ModelPart flippingPageLeft;
    private final ModelPart[] loose = new ModelPart[4];

    public DeathTomeModel(ModelPart root) {
        super(root);
        this.everything = root.getChild("everything");
        this.book = this.everything.getChild("book");
        this.coverRight = this.book.getChild("cover_right");
        this.coverLeft = this.book.getChild("cover_left");
        this.pagesRight = this.book.getChild("pages_right");
        this.pagesLeft = this.book.getChild("pages_left");
        this.flippingPageRight = this.book.getChild("flipping_page_right");
        this.flippingPageLeft = this.book.getChild("flipping_page_left");
        for (int i = 0; i < 4; i++) {
            this.loose[i] = this.everything.getChild("loose_page" + (i + 1));
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition everything = mesh.getRoot().addOrReplaceChild("everything", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition book = everything.addOrReplaceChild("book", CubeListBuilder.create(), PartPose.ZERO);
        book.addOrReplaceChild("cover_right", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, 0.0F, 6.0F, 10.0F, 0.0F),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        book.addOrReplaceChild("cover_left", CubeListBuilder.create().texOffs(16, 0).addBox(0.0F, -5.0F, 0.0F, 6.0F, 10.0F, 0.0F),
                PartPose.offset(0.0F, 0.0F, 1.0F));
        book.addOrReplaceChild("spine", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.0F),
                PartPose.rotation(0.0F, Mth.HALF_PI, 0.0F));
        book.addOrReplaceChild("pages_right", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -4.0F, -0.99F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
        book.addOrReplaceChild("pages_left", CubeListBuilder.create().texOffs(12, 10).addBox(0.0F, -4.0F, -0.01F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
        book.addOrReplaceChild("flipping_page_right", CubeListBuilder.create().texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.0F), PartPose.ZERO);
        book.addOrReplaceChild("flipping_page_left", CubeListBuilder.create().texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.0F), PartPose.ZERO);
        float[] z = {-8.0F, 9.0F, 11.0F, 7.0F};
        for (int i = 0; i < 4; i++) {
            everything.addOrReplaceChild("loose_page" + (i + 1), CubeListBuilder.create().texOffs(24, 10).addBox(0.0F, -4.0F, z[i], 5.0F, 8.0F, 0.0F),
                    PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float bounce = state.ageInTicks;
        float open = 0.9F;
        float flipRight = 0.4F;
        float flipLeft = 0.6F;
        this.book.zRot = -0.87266463F;
        // the original passed the head pitch as "rotate"
        this.everything.yRot = state.xRot * Mth.DEG_TO_RAD + Mth.HALF_PI;
        this.book.y = 4.0F + Mth.sin(bounce * 0.3F) * 2.0F;
        float openAngle = (Mth.sin(bounce * 0.4F) * 0.3F + 1.25F) * open;
        this.coverRight.yRot = Mth.PI + openAngle;
        this.coverLeft.yRot = -openAngle;
        this.pagesRight.yRot = openAngle;
        this.pagesLeft.yRot = -openAngle;
        this.flippingPageRight.yRot = openAngle - openAngle * 2.0F * flipRight;
        this.flippingPageLeft.yRot = openAngle - openAngle * 2.0F * flipLeft;
        this.pagesRight.x = Mth.sin(openAngle);
        this.pagesLeft.x = Mth.sin(openAngle);
        this.flippingPageRight.x = Mth.sin(openAngle);
        this.flippingPageLeft.x = Mth.sin(openAngle);
        this.loose[0].yRot = bounce / 4.0F;
        this.loose[0].xRot = Mth.sin(bounce / 5.0F) / 3.0F;
        this.loose[0].zRot = Mth.cos(bounce / 5.0F) / 5.0F;
        this.loose[1].yRot = bounce / 3.0F;
        this.loose[1].xRot = Mth.sin(bounce / 5.0F) / 3.0F;
        this.loose[1].zRot = Mth.cos(bounce / 5.0F) / 4.0F + 2.0F;
        this.loose[2].yRot = bounce / 4.0F;
        this.loose[2].xRot = -Mth.sin(bounce / 5.0F) / 3.0F;
        this.loose[2].zRot = Mth.cos(bounce / 5.0F) / 5.0F - 1.0F;
        this.loose[3].yRot = bounce / 4.0F;
        this.loose[3].xRot = -Mth.sin(bounce / 2.0F) / 4.0F;
        this.loose[3].zRot = Mth.cos(bounce / 7.0F) / 5.0F;
    }
}
