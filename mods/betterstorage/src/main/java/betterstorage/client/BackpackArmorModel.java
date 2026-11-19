package betterstorage.client;

import betterstorage.BetterStorage;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.context.ContextKey;

/**
 * ModelBackpackArmor: the backpack on the back (ItemBackpack.getArmorModel), drawn by the vanilla armor layer with the
 * textures of the equipment asset. Only the body has cubes; the original rendered it at scale 0.05 instead of 1/16
 * (the "pack" part is 0.8 times as big) and 18 of those units higher on the frienderman.
 */
public class BackpackArmorModel extends HumanoidModel<HumanoidRenderState> {
    /** prevLidAngle + (lidAngle - prevLidAngle) * partialTicks of the wearer, set by {@link BSClient}. */
    public static final ContextKey<Float> LID = new ContextKey<>(BetterStorage.id("backpack_lid"));
    private final ModelPart top;

    public BackpackArmorModel(ModelPart root) {
        super(root);
        this.top = root.getChild("body").getChild("pack").getChild("top");
    }

    public static LayerDefinition create() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        PartDefinition pack = body.addOrReplaceChild("pack", CubeListBuilder.create(), PartPose.ZERO.withScale(0.8F));
        pack.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 8).addBox(-5.0F, 4.0F, 2.5F, 10.0F, 9.0F, 5.0F), PartPose.ZERO);
        pack.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -3.0F, 0.0F, 10.0F, 3.0F, 5.0F),
                PartPose.offset(0.0F, 4.0F, 2.5F));
        pack.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 22).addBox(-4.0F, 6.0F, 7.5F, 8.0F, 6.0F, 2.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        Float lid = state.getRenderData(LID);
        float angle = lid == null ? 0.0F : lid;
        angle = 1.0F - angle;
        angle = 1.0F - angle * angle;
        this.top.xRot = (float) (angle * Math.PI / 4.0);
        if (state instanceof EndermanRenderState) {
            this.body.y -= 18.0F * 0.05F * 16.0F;
        }
    }
}
