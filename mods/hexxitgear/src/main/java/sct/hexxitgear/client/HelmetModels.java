package sct.hexxitgear.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * ModelSkullHelmet, ModelScaleHelmet and ModelHoodHelmet. The originals rendered each box with a larger scale
 * than the 1/16 of the player model ({@code f5 + 0.01}, {@code f5 + 0.009} or {@code f5 + 0.002}), which
 * enlarges the box around its rotation point (the neck); here that is the part's scale, with the rotation
 * point scaled the same way. All boxes copied the head's rotation; here they are children of the head.
 */
public final class HelmetModels {
    private static final float F5 = 0.0625F;
    /** ModelSkullHelmet: every box at f5 + 0.01. */
    private static final float SKULL = (F5 + 0.01F) / F5;
    /** ModelScaleHelmet and ModelHoodHelmet: the head box at f5 + 0.009 ("scaledUp"), the others at f5 + 0.002 ("suLarge"). */
    private static final float SCALED_UP = (F5 + 0.009F) / F5;
    private static final float SU_LARGE = (F5 + 0.002F) / F5;

    private HelmetModels() {
    }

    /** A humanoid model whose parts are all empty: only the head carries the helmet's boxes. */
    private static MeshDefinition emptyHumanoid() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        return mesh;
    }

    /** One ModelRenderer of the original: texture offset, box, rotation point, mirror = true, rendered at {@code scale}. */
    private static void box(PartDefinition head, String name, int u, int v, float x, float y, float z, int w, int h, int d,
                            float px, float py, float pz, float scale) {
        head.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).mirror().addBox(x, y, z, w, h, d),
                PartPose.offset(px * scale, py * scale, pz * scale).withScale(scale));
    }

    private static void box(PartDefinition head, String name, int u, int v, float x, float y, float z, int w, int h, int d, float scale) {
        box(head, name, u, v, x, y, z, w, h, d, 0.0F, 0.0F, 0.0F, scale);
    }

    /** ModelSkullHelmet (tribal helmet), texture 64x32. */
    public static LayerDefinition skull() {
        MeshDefinition mesh = emptyHumanoid();
        PartDefinition head = mesh.getRoot().getChild("head");
        box(head, "skull", 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, SKULL);
        box(head, "shape4", 25, 0, 4.0F, -7.0F, 0.0F, 3, 2, 2, SKULL);
        box(head, "shape1", 25, 0, -7.0F, -7.0F, 0.0F, 3, 2, 2, SKULL);
        box(head, "shape2", 25, 5, 6.0F, -8.0F, 0.0F, 1, 1, 1, SKULL);
        box(head, "shape3", 25, 5, -7.0F, -8.0F, 0.0F, 1, 1, 1, SKULL);
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelScaleHelmet, texture 64x64. */
    public static LayerDefinition scale() {
        MeshDefinition mesh = emptyHumanoid();
        PartDefinition head = mesh.getRoot().getChild("head");
        box(head, "helmet", 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, SCALED_UP);
        box(head, "shape1", 0, 16, -5.0F, -9.0F, -5.0F, 10, 9, 10, SU_LARGE);
        box(head, "shape2", 24, 0, -6.0F, -7.0F, -2.0F, 1, 5, 3, SU_LARGE);
        box(head, "shape3", 4, 20, -7.0F, -8.0F, 0.0F, 1, 4, 2, SU_LARGE);
        box(head, "shape4", 2, 2, -9.0F, -9.0F, 2.0F, 2, 1, 1, SU_LARGE);
        box(head, "shape5", 4, 4, -8.0F, -8.0F, 1.0F, 1, 3, 1, SU_LARGE);
        box(head, "shape6", 2, 2, -9.0F, -7.0F, 2.0F, 2, 1, 1, SU_LARGE);
        box(head, "shape7", 24, 0, 5.0F, -7.0F, -2.0F, 1, 5, 3, SU_LARGE);
        box(head, "shape8", 4, 20, 6.0F, -8.0F, 0.0F, 1, 4, 2, SU_LARGE);
        // the only box with a rotation point off the neck
        box(head, "shape9", 4, 4, 7.0F, -9.0F, 1.0F, 1, 3, 1, 0.0F, 1.0F, 0.0F, SU_LARGE);
        box(head, "shape10", 2, 2, 7.0F, -9.0F, 2.0F, 2, 1, 1, SU_LARGE);
        box(head, "shape11", 2, 2, 7.0F, -7.0F, 2.0F, 2, 1, 1, SU_LARGE);
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** ModelHoodHelmet (thief's hood), texture 64x64. */
    public static LayerDefinition hood() {
        MeshDefinition mesh = emptyHumanoid();
        PartDefinition head = mesh.getRoot().getChild("head");
        box(head, "hood", 0, 0, -4.0F, -7.5F, -4.0F, 8, 8, 8, SCALED_UP);
        box(head, "shape1", 48, 0, -3.5F, -8.8F, 5.0F, 7, 5, 1, SU_LARGE);
        box(head, "shape2", 48, 6, -3.0F, -8.6F, 6.0F, 6, 3, 1, SU_LARGE);
        box(head, "shape3", 48, 11, -1.333333F, -8.5F, 7.0F, 3, 1, 1, SU_LARGE);
        box(head, "shape4", 0, 33, -5.0F, -9.0F, -5.0F, 10, 9, 10, SU_LARGE);
        return LayerDefinition.create(mesh, 64, 64);
    }

    static HumanoidModel<HumanoidRenderState> bake(LayerDefinition layer) {
        return new HumanoidModel<>(layer.bakeRoot());
    }
}
