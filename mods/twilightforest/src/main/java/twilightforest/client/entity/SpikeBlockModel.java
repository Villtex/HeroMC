package twilightforest.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** ModelTFSpikeBlock (the block goblin's spiked ball, 27 spikes round an 8³ block) and ModelTFGoblinChain (one link). */
public class SpikeBlockModel extends EntityModel<EntityRenderState> {
    public SpikeBlockModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition block = mesh.getRoot().addOrReplaceChild("block", CubeListBuilder.create().texOffs(32, 16)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        float[] x = new float[27];
        float[] y = new float[27];
        float[] z = new float[27];
        float[] rx = new float[27];
        float[] ry = new float[27];
        float[] rz = new float[27];
        for (int i : new int[]{2, 3, 4, 11, 13, 20, 21, 22}) {
            x[i] = 4.0F;
        }
        x[12] = 5.0F;
        for (int i : new int[]{6, 7, 8, 15, 17, 24, 25, 26}) {
            x[i] = -4.0F;
        }
        x[16] = -5.0F;
        y[0] = -9.0F;
        for (int i = 1; i <= 8; i++) {
            y[i] = -8.0F;
        }
        for (int i = 9; i <= 17; i++) {
            y[i] = -4.0F;
        }
        y[18] = 1.0F;
        for (int i : new int[]{1, 2, 8, 10, 17, 19, 20, 26}) {
            z[i] = 4.0F;
        }
        z[11] = 5.0F;
        for (int i : new int[]{4, 5, 6, 13, 15, 22, 23, 24}) {
            z[i] = -4.0F;
        }
        z[14] = -5.0F;
        float fourtyFive = (float) (Math.PI / 4);
        for (int i : new int[]{1, 5, 19, 23}) {
            rx[i] = fourtyFive;
        }
        for (int i : new int[]{11, 13, 15, 17}) {
            ry[i] = fourtyFive;
        }
        for (int i : new int[]{3, 7, 21, 25}) {
            rz[i] = fourtyFive;
        }
        rx[2] = -0.95993114F;
        ry[2] = fourtyFive;
        rx[24] = -0.95993114F;
        ry[24] = fourtyFive;
        rx[4] = -0.6108653F;
        ry[4] = -fourtyFive;
        rx[26] = -0.6108653F;
        ry[26] = -fourtyFive;
        ry[6] = fourtyFive;
        rx[6] = -0.6108653F;
        ry[20] = fourtyFive;
        rx[20] = -0.6108653F;
        rx[8] = -0.95993114F;
        ry[8] = -fourtyFive;
        rx[22] = -0.95993114F;
        ry[22] = -fourtyFive;
        for (int i = 0; i < 27; i++) {
            block.addOrReplaceChild("spike" + i, CubeListBuilder.create().texOffs(56, 16).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                    PartPose.offsetAndRotation(x[i], y[i], z[i], rx[i], ry[i], rz[i]));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelTFGoblinChain: one 2³ link. */
    public static LayerDefinition createChainLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("chain", CubeListBuilder.create().texOffs(56, 16).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }
}
