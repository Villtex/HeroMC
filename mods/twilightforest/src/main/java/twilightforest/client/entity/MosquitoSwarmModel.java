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

/**
 * ModelTFMosquitoSwarm: six spinning nodes around a core, each with a ring of 16 bugs at random spots and texture
 * offsets (chosen anew every game start, as the original's new Random()), drawn at half scale.
 */
public class MosquitoSwarmModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart core;
    private final ModelPart[] nodes = new ModelPart[6];

    public MosquitoSwarmModel(ModelPart root) {
        super(root);
        this.core = root.getChild("core");
        for (int i = 0; i < 6; i++) {
            this.nodes[i] = this.core.getChild("node" + (i + 1));
        }
    }

    public static LayerDefinition createBodyLayer() {
        Random rand = new Random();
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition core = root.addOrReplaceChild("core", CubeListBuilder.create().texOffs(rand.nextInt(28), rand.nextInt(28))
                .addBox(-4.0F, 0.0F, -2.0F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, -4.0F, 0.0F));
        float[][] boxes = {{-5.5F, -5.0F, -13.0F}, {-5.5F, -13.0F, -5.0F}, {-13.0F, -5.0F, -5.0F}, {-5.5F, -5.0F, -13.0F},
                {-5.5F, -13.0F, -5.0F}, {-13.0F, -5.0F, -5.0F}};
        float[][] points = {{2.0F, -1.0F, -6.0F}, {0.0F, -7.0F, -1.0F}, {5.0F, -2.0F, -1.0F}, {2.0F, -1.0F, -6.0F},
                {0.0F, -7.0F, -1.0F}, {5.0F, -2.0F, -1.0F}};
        PartDefinition[] nodes = new PartDefinition[6];
        for (int i = 0; i < 6; i++) {
            nodes[i] = core.addOrReplaceChild("node" + (i + 1), CubeListBuilder.create().texOffs(rand.nextInt(28), rand.nextInt(28))
                    .addBox(boxes[i][0], boxes[i][1], boxes[i][2], 1.0F, 1.0F, 1.0F), PartPose.offset(points[i][0], points[i][1], points[i][2]));
        }
        for (int n = 0; n < 6; n++) {
            int bugs = 16;
            for (int i = 0; i < bugs; i++) {
                float rotateY = i * (360.0F / bugs) * 3.141593F / 180.0F;
                // Vec3(11, 0, 0).rotateAroundY(rotateY) of 1.5.2
                float c = Mth.cos(rotateY);
                float s = Mth.sin(rotateY);
                float vx = 11.0F * c;
                float vz = 11.0F * s;
                int u = rand.nextInt(28);
                int v = rand.nextInt(28);
                float bugX = (rand.nextFloat() - rand.nextFloat()) * 4.0F;
                float bugY = (rand.nextFloat() - rand.nextFloat()) * 4.0F;
                float bugZ = (rand.nextFloat() - rand.nextFloat()) * 4.0F;
                nodes[n].addOrReplaceChild("bug" + i, CubeListBuilder.create().texOffs(u, v).addBox(bugX, bugY, bugZ, 1.0F, 1.0F, 1.0F),
                        PartPose.offsetAndRotation(vx, 0.0F, vz, 0.0F, rotateY, 0.0F));
            }
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        // render(par7 / 2): the whole swarm at half scale
        this.core.y = -2.0F;
        this.core.xScale = 0.5F;
        this.core.yScale = 0.5F;
        this.core.zScale = 0.5F;
        this.core.yRot = t / 5.0F;
        this.core.xRot = Mth.sin(t / 5.0F) / 4.0F;
        this.core.zRot = Mth.cos(t / 5.0F) / 4.0F;
        this.nodes[0].yRot = t / 2.0F;
        this.nodes[0].xRot = Mth.sin(t / 6.0F) / 2.0F;
        this.nodes[0].zRot = Mth.cos(t / 5.0F) / 4.0F;
        this.nodes[1].yRot = Mth.sin(t / 2.0F) / 3.0F;
        this.nodes[1].xRot = t / 5.0F;
        this.nodes[1].zRot = Mth.cos(t / 5.0F) / 4.0F;
        this.nodes[2].yRot = Mth.sin(t / 7.0F) / 3.0F;
        this.nodes[2].xRot = Mth.cos(t / 4.0F) / 2.0F;
        this.nodes[2].zRot = t / 5.0F;
        this.nodes[3].xRot = t / 2.0F;
        this.nodes[3].zRot = Mth.sin(t / 6.0F) / 2.0F;
        this.nodes[3].yRot = Mth.sin(t / 5.0F) / 4.0F;
        this.nodes[4].zRot = Mth.sin(t / 2.0F) / 3.0F;
        this.nodes[4].yRot = Mth.cos(t / 5.0F) / 4.0F;
        this.nodes[4].xRot = Mth.cos(t / 5.0F) / 4.0F;
        this.nodes[5].zRot = Mth.cos(t / 7.0F) / 3.0F;
        this.nodes[5].xRot = Mth.cos(t / 4.0F) / 2.0F;
        this.nodes[5].yRot = t / 5.0F;
    }
}
