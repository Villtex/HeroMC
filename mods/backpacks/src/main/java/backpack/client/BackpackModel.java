package backpack.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * ModelBackpack (texture 64 x 32 units; the file is 256 x 128): the bag, pockets, front ledges and the straps over the
 * shoulders. In 1.5.2 the boxes did not follow the body; here they are children of the body (same place when
 * standing, they follow sneaking). All other parts of the humanoid model are empty.
 */
public final class BackpackModel {
    private BackpackModel() {
    }

    private static void box(PartDefinition body, String name, int u, int v, float x, float y, float z, int w, int h, int d, float px, float py, float pz) {
        body.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).mirror().addBox(x, y, z, w, h, d), PartPose.offset(px, py, pz));
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
        box(body, "bag_top", 44, 0, 0, 0, 0, 6, 1, 4, -3.0F, -0.5F, 2.0F);
        box(body, "bag_main", 42, 6, 0, 0, 0, 7, 10, 4, -3.5F, 0.0F, 2.0F);
        box(body, "pocket_left", 33, 5, 0, 0, 0, 1, 4, 3, 3.5F, 5.0F, 2.5F);
        box(body, "pocket_right", 33, 13, 0, 0, 0, 1, 4, 3, -4.5F, 4.7F, 2.5F);
        box(body, "pocket_front", 15, 27, 0, 0, 1, 4, 4, 1, -2.0F, 4.7F, 5.2F);
        box(body, "ledge_front1", 0, 23, 0, 0, 0, 6, 8, 1, -3.0F, 1.0F, 5.3F);
        box(body, "ledge_front2", 1, 20, 0, 0, 0, 4, 1, 1, -2.0F, 0.6F, 5.3F);
        box(body, "ledge_front3", 1, 17, 0, 0, 0, 4, 1, 1, -2.0F, 8.5F, 5.3F);
        box(body, "string_top_left", 54, 21, 0, 0, 0, 1, 0, 5, 2.5F, 0.0F, -2.0F);
        box(body, "string_top_right", 41, 21, 0, 0, 0, 1, 0, 5, -3.5F, 0.0F, -2.0F);
        box(body, "string_back_left", 62, 21, 0, 0, 0, 1, 10, 0, 2.5F, 0.0F, -2.0F);
        box(body, "string_back_right", 49, 21, 0, 0, 0, 1, 10, 0, -3.5F, 0.0F, -2.0F);
        box(body, "string_bottom_left", 54, 27, 0, 0, 0, 1, 0, 5, 2.5F, 10.0F, -2.0F);
        box(body, "string_bottom_right", 41, 27, 0, 0, 0, 1, 0, 5, -3.5F, 10.0F, -2.0F);
        return LayerDefinition.create(mesh, 64, 32);
    }

    static HumanoidModel<HumanoidRenderState> bake() {
        return new HumanoidModel<>(create().bakeRoot());
    }
}
