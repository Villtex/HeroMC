package twilightforest.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import twilightforest.entity.passive.QuestRam;

/**
 * ModelTFQuestRam (128 x 128): the body grows 2 pixels longer for every colour eaten. The wool bands are
 * {@link Segment}s, one model per colour, drawn tinted by the renderer.
 */
public class QuestRamModel extends EntityModel<TFRenderStates.QuestRam> {
    /** The order the bands line up in, from the front. */
    static final int[] COLOR_ORDER = {0, 8, 7, 15, 14, 1, 4, 5, 13, 3, 9, 11, 10, 2, 6, 12};
    private final ModelPart rearbody;
    private final ModelPart leg1;
    private final ModelPart haunch1;
    private final ModelPart leg2;
    private final ModelPart haunch2;
    private final ModelPart leg3;
    private final ModelPart haunch3;
    private final ModelPart leg4;
    private final ModelPart haunch4;
    private final ModelPart neck;
    private final ModelPart head;

    public QuestRamModel(ModelPart root) {
        super(root);
        this.rearbody = root.getChild("rearbody");
        this.leg1 = root.getChild("leg1");
        this.haunch1 = root.getChild("haunch1");
        this.leg2 = root.getChild("leg2");
        this.haunch2 = root.getChild("haunch2");
        this.leg3 = root.getChild("leg3");
        this.haunch3 = root.getChild("haunch3");
        this.leg4 = root.getChild("leg4");
        this.haunch4 = root.getChild("haunch4");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("frontbody", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9.0F, -7.5F, -15.0F, 18.0F, 15.0F, 15.0F),
                PartPose.offset(0.0F, -1.0F, 2.0F));
        root.addOrReplaceChild("rearbody", CubeListBuilder.create()
                .texOffs(0, 30).addBox(-9.0F, -7.5F, 0.0F, 18.0F, 15.0F, 15.0F),
                PartPose.offset(0.0F, -1.0F, 4.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create()
                .texOffs(66, 0).addBox(-3.0F, 10.0F, -3.0F, 6.0F, 12.0F, 6.0F),
                PartPose.offset(-6.0F, 2.0F, 13.0F));
        root.addOrReplaceChild("haunch1", CubeListBuilder.create()
                .texOffs(90, 0).addBox(-3.5F, 0.0F, -6.0F, 7.0F, 10.0F, 10.0F),
                PartPose.offset(-6.0F, 2.0F, 13.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create()
                .texOffs(66, 0).addBox(-3.0F, 10.0F, -3.0F, 6.0F, 12.0F, 6.0F),
                PartPose.offset(6.0F, 2.0F, 13.0F));
        root.addOrReplaceChild("haunch2", CubeListBuilder.create()
                .texOffs(90, 0).addBox(-3.5F, 0.0F, -6.0F, 7.0F, 10.0F, 10.0F),
                PartPose.offset(6.0F, 2.0F, 13.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create()
                .texOffs(66, 18).addBox(-3.0F, 10.0F, -3.0F, 6.0F, 13.0F, 6.0F),
                PartPose.offset(-6.0F, 1.0F, -8.0F));
        root.addOrReplaceChild("haunch3", CubeListBuilder.create()
                .texOffs(90, 20).addBox(-3.5F, 0.0F, -4.0F, 7.0F, 10.0F, 7.0F),
                PartPose.offset(-6.0F, 1.0F, -8.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create()
                .texOffs(66, 18).addBox(-3.0F, 10.0F, -3.0F, 6.0F, 13.0F, 6.0F),
                PartPose.offset(6.0F, 1.0F, -8.0F));
        root.addOrReplaceChild("haunch4", CubeListBuilder.create()
                .texOffs(90, 20).addBox(-3.5F, 0.0F, -4.0F, 7.0F, 10.0F, 7.0F),
                PartPose.offset(6.0F, 1.0F, -8.0F));
        root.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(66, 37).addBox(-5.5F, -8.0F, -8.0F, 11.0F, 14.0F, 12.0F),
                PartPose.offsetAndRotation(0.0F, -8.0F, -7.0F, (float) (Math.PI / 12), 0.0F, 0.0F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 70).addBox(-6.0F, -4.5F, -15.0F, 12.0F, 9.0F, 15.0F)
                .texOffs(0, 94).addBox(5.0F, -9.0F, -7.0F, 4.0F, 4.0F, 6.0F)
                .texOffs(20, 96).addBox(7.0F, -8.0F, -2.0F, 3.0F, 4.0F, 4.0F)
                .texOffs(34, 95).addBox(8.0F, -6.0F, 0.0F, 3.0F, 6.0F, 3.0F)
                .texOffs(46, 98).addBox(9.5F, -2.0F, -2.0F, 3.0F, 3.0F, 3.0F)
                .texOffs(58, 95).addBox(11.0F, 0.0F, -7.0F, 3.0F, 3.0F, 6.0F)
                .texOffs(76, 95).addBox(12.0F, -4.0F, -9.0F, 3.0F, 6.0F, 3.0F)
                .texOffs(88, 97).addBox(13.0F, -6.0F, -7.0F, 3.0F, 3.0F, 4.0F)
                .texOffs(0, 94).addBox(-9.0F, -9.0F, -7.0F, 4.0F, 4.0F, 6.0F)
                .texOffs(20, 96).addBox(-10.0F, -8.0F, -2.0F, 3.0F, 4.0F, 4.0F)
                .texOffs(34, 95).addBox(-11.0F, -6.0F, 0.0F, 3.0F, 6.0F, 3.0F)
                .texOffs(46, 98).addBox(-12.5F, -2.0F, -2.0F, 3.0F, 3.0F, 3.0F)
                .texOffs(58, 95).addBox(-14.0F, 0.0F, -7.0F, 3.0F, 3.0F, 6.0F)
                .texOffs(76, 95).addBox(-15.0F, -4.0F, -9.0F, 3.0F, 6.0F, 3.0F)
                .texOffs(88, 97).addBox(-16.0F, -6.0F, -7.0F, 3.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, -13.0F, -5.0F));
        head.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(54, 73).addBox(-5.5F, -5.0F, -13.0F, 11.0F, 9.0F, 12.0F),
                PartPose.offsetAndRotation(0.0F, -7.0F, -1.0F, (float) (Math.PI / 6), 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(TFRenderStates.QuestRam state) {
        super.setupAnim(state);
        float f = state.walkAnimationPos;
        float f1 = state.walkAnimationSpeed;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.neck.yRot = this.head.yRot;
        this.leg1.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1 * 0.5F;
        this.leg2.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1 * 0.5F;
        this.leg3.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1 * 0.5F;
        this.leg4.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1 * 0.5F;
        this.haunch1.xRot = this.leg1.xRot;
        this.haunch2.xRot = this.leg2.xRot;
        this.haunch3.xRot = this.leg3.xRot;
        this.haunch4.xRot = this.leg4.xRot;
        // setLivingAnimations
        int count = QuestRam.countColors(state.colorFlags);
        this.rearbody.z = 2 + 2 * count;
        this.leg1.z = 11 + 2 * count;
        this.leg2.z = 11 + 2 * count;
        this.haunch1.z = 11 + 2 * count;
        this.haunch2.z = 11 + 2 * count;
    }

    /** One wool band: shown when its colour has been eaten, behind the bands of the colours before it in COLOR_ORDER. */
    public static class Segment extends EntityModel<TFRenderStates.QuestRam> {
        private final ModelPart segment;
        private final int color;

        public Segment(ModelPart root, int color) {
            super(root);
            this.segment = root.getChild("segment");
            this.color = color;
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            mesh.getRoot().addOrReplaceChild("segment", CubeListBuilder.create().texOffs(0, 104).addBox(-9.0F, -7.5F, 0.0F, 18.0F, 15.0F, 2.0F),
                    PartPose.offset(0.0F, -1.0F, 2.0F));
            return LayerDefinition.create(mesh, 128, 128);
        }

        @Override
        public void setupAnim(TFRenderStates.QuestRam state) {
            super.setupAnim(state);
            int offset = 2;
            for (int c : COLOR_ORDER) {
                if ((state.colorFlags & 1 << c) != 0) {
                    if (c == this.color) {
                        this.segment.z = offset;
                        this.segment.visible = true;
                        return;
                    }
                    offset += 2;
                }
            }
            this.segment.visible = false;
        }
    }
}
