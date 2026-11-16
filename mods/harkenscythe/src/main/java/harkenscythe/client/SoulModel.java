package harkenscythe.client;

import com.mojang.math.Axis;
import harkenscythe.entity.SoulEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.Quaternionf;

/**
 * ModelHSSoul: the crystal of the ender crystal without its base, two glass cubes and a core, spinning and bobbing
 * 0.3 blocks above the feet. Drawn see-through.
 */
public class SoulModel extends EntityModel<SoulRenderState> {
    private static final float SIN_45 = (float) Math.sin(Math.PI / 4);
    private final ModelPart outerGlass;
    private final ModelPart innerGlass;
    private final ModelPart cube;

    public SoulModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        this.outerGlass = root.getChild("outer_glass");
        this.innerGlass = this.outerGlass.getChild("inner_glass");
        this.cube = this.innerGlass.getChild("cube");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeListBuilder glass = CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
        PartDefinition outer = root.addOrReplaceChild("outer_glass", glass, PartPose.ZERO);
        PartDefinition inner = outer.addOrReplaceChild("inner_glass", glass, PartPose.ZERO.withScale(0.875F));
        inner.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO.withScale(0.765625F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(SoulRenderState state) {
        super.setupAnim(state);
        float spin = state.time * 3.0F;
        this.outerGlass.y = (0.3F + SoulEntity.bob(state.time)) * 16.0F;
        this.outerGlass.rotateBy(Axis.YP.rotationDegrees(spin).rotateAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
        this.innerGlass.rotateBy(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45).rotateY(spin * (float) (Math.PI / 180.0)));
        this.cube.rotateBy(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45).rotateY(spin * (float) (Math.PI / 180.0)));
    }
}
