package harkenscythe.client.mob;

import harkenscythe.HarkenScythe;
import java.util.Random;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/**
 * The ModelHSSpectral* of Harken Scythe rebuilt cube by cube (copies of the 1.5.2 vanilla models that switch on
 * blending: drawn with {@link RenderTypes#entityTranslucent}, the textures carry alpha 170). Parts of one model are
 * named after their old fields; the animation of each is in {@link Anim}.
 */
public final class SpectralModels {
    public static final ModelLayerLocation CHICKEN = layer("spectral_chicken");
    public static final ModelLayerLocation COW = layer("spectral_cow");
    public static final ModelLayerLocation PIG = layer("spectral_pig");
    public static final ModelLayerLocation SHEEP = layer("spectral_sheep");
    public static final ModelLayerLocation SHEEP_FUR = layer("spectral_sheep_fur");
    public static final ModelLayerLocation WOLF = layer("spectral_wolf");
    public static final ModelLayerLocation OCELOT = layer("spectral_ocelot");
    public static final ModelLayerLocation BAT = layer("spectral_bat");
    public static final ModelLayerLocation SQUID = layer("spectral_squid");
    public static final ModelLayerLocation VILLAGER = layer("spectral_villager");
    public static final ModelLayerLocation WITCH = layer("spectral_witch");
    public static final ModelLayerLocation CREEPER = layer("spectral_creeper");
    public static final ModelLayerLocation CREEPER_ARMOR = layer("spectral_creeper_armor");
    public static final ModelLayerLocation SPIDER = layer("spectral_spider");
    public static final ModelLayerLocation SILVERFISH = layer("spectral_silverfish");
    public static final ModelLayerLocation ENDERMAN = layer("spectral_enderman");
    public static final ModelLayerLocation BLAZE = layer("spectral_blaze");
    public static final ModelLayerLocation GHAST = layer("spectral_ghast");
    public static final ModelLayerLocation BIPED = layer("spectral_biped");
    public static final ModelLayerLocation BIPED_64 = layer("spectral_biped_64");
    public static final ModelLayerLocation SKELETON = layer("spectral_skeleton");
    public static final ModelLayerLocation SLIME = layer("essence_slime");

    private SpectralModels() {
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(HarkenScythe.id(name), "main");
    }

    /** The animation of a model: (state, root). */
    public interface Anim {
        void apply(SpectralRenderState s, ModelPart root);
    }

    /** A spectral model: translucent, posed by its {@link Anim} after resetting to the layer's pose. */
    public static class Model extends EntityModel<SpectralRenderState> {
        private final Anim anim;

        public Model(ModelPart root, Anim anim) {
            super(root, RenderTypes::entityTranslucent);
            this.anim = anim;
        }

        @Override
        public void setupAnim(SpectralRenderState state) {
            super.setupAnim(state);
            this.anim.apply(state, this.root());
        }
    }

    /** ModelHSSpectralBiped / Zombie / Skeleton / Miner and the Harbinger's ModelZombie: vanilla humanoid poses, zombie arms. */
    public static class Biped extends HumanoidModel<SpectralRenderState> {
        private final boolean zombieArms;

        public Biped(ModelPart root, boolean zombieArms, boolean translucent) {
            super(root, translucent ? RenderTypes::entityTranslucent : RenderTypes::entityCutout);
            this.zombieArms = zombieArms;
        }

        @Override
        public void setupAnim(SpectralRenderState s) {
            super.setupAnim(s);
            if (this.zombieArms) {
                float f6 = Mth.sin(s.swingAnimation * Mth.PI);
                float f7 = Mth.sin((1.0F - (1.0F - s.swingAnimation) * (1.0F - s.swingAnimation)) * Mth.PI);
                this.rightArm.zRot = 0.0F;
                this.leftArm.zRot = 0.0F;
                this.rightArm.yRot = -(0.1F - f6 * 0.6F);
                this.leftArm.yRot = 0.1F - f6 * 0.6F;
                this.rightArm.xRot = -Mth.HALF_PI - (f6 * 1.2F - f7 * 0.4F);
                this.leftArm.xRot = -Mth.HALF_PI - (f6 * 1.2F - f7 * 0.4F);
                this.rightArm.zRot += Mth.cos(s.ageInTicks * 0.09F) * 0.05F + 0.05F;
                this.leftArm.zRot -= Mth.cos(s.ageInTicks * 0.09F) * 0.05F + 0.05F;
                this.rightArm.xRot += Mth.sin(s.ageInTicks * 0.067F) * 0.05F;
                this.leftArm.xRot -= Mth.sin(s.ageInTicks * 0.067F) * 0.05F;
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ helpers
    private static CubeListBuilder box(int u, int v, float x, float y, float z, int w, int h, int d) {
        return CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d);
    }

    private static CubeListBuilder box(int u, int v, float x, float y, float z, int w, int h, int d, float g) {
        return CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d, new CubeDeformation(g));
    }

    private static CubeListBuilder mirrored(int u, int v, float x, float y, float z, int w, int h, int d, float g) {
        return CubeListBuilder.create().texOffs(u, v).mirror().addBox(x, y, z, w, h, d, new CubeDeformation(g));
    }

    private static PartPose at(float x, float y, float z) {
        return PartPose.offset(x, y, z);
    }

    static float walk(SpectralRenderState s, float phase, float scale) {
        return Mth.cos(s.walkAnimationPos * 0.6662F + phase) * scale * s.walkAnimationSpeed;
    }

    static void look(ModelPart head, SpectralRenderState s) {
        head.xRot = s.xRot * Mth.DEG_TO_RAD;
        head.yRot = s.yRot * Mth.DEG_TO_RAD;
    }

    // ------------------------------------------------------------------------------------------------ chicken
    public static LayerDefinition chicken() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("head", box(0, 0, -2, -6, -2, 4, 6, 3), at(0, 15, -4));
        r.addOrReplaceChild("bill", box(14, 0, -2, -4, -4, 4, 2, 2), at(0, 15, -4));
        r.addOrReplaceChild("chin", box(14, 4, -1, -2, -3, 2, 2, 2), at(0, 15, -4));
        r.addOrReplaceChild("body", box(0, 9, -3, -4, -3, 6, 8, 6), at(0, 16, 0));
        r.addOrReplaceChild("right_leg", box(26, 0, -1, 0, -3, 3, 5, 3), at(-2, 19, 1));
        r.addOrReplaceChild("left_leg", box(26, 0, -1, 0, -3, 3, 5, 3), at(1, 19, 1));
        r.addOrReplaceChild("right_wing", box(24, 13, 0, 0, -3, 1, 4, 6), at(-4, 13, 0));
        r.addOrReplaceChild("left_wing", box(24, 13, -1, 0, -3, 1, 4, 6), at(4, 13, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static final Anim CHICKEN_ANIM = (s, r) -> {
        for (String p : new String[] {"head", "bill", "chin"}) {
            look(r.getChild(p), s);
        }
        r.getChild("body").xRot = Mth.HALF_PI;
        r.getChild("right_leg").xRot = walk(s, 0, 1.4F);
        r.getChild("left_leg").xRot = walk(s, Mth.PI, 1.4F);
        r.getChild("right_wing").zRot = s.flap;
        r.getChild("left_wing").zRot = -s.flap;
    };

    // ------------------------------------------------------------------------------------------------ quadrupeds
    private static void legs(PartDefinition r, int h, float g, float frontZ, float dx) {
        r.addOrReplaceChild("leg1", box(0, 16, -2, 0, -2, 4, h, 4, g), at(-3 - dx, 24 - h, 7));
        r.addOrReplaceChild("leg2", box(0, 16, -2, 0, -2, 4, h, 4, g), at(3 + dx, 24 - h, 7));
        r.addOrReplaceChild("leg3", box(0, 16, -2, 0, -2, 4, h, 4, g), at(-3 - dx, 24 - h, frontZ));
        r.addOrReplaceChild("leg4", box(0, 16, -2, 0, -2, 4, h, 4, g), at(3 + dx, 24 - h, frontZ));
    }

    /** ModelHSSpectralCow: horns and udder, legs a pixel further out. */
    public static LayerDefinition cow() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("head", box(0, 0, -4, -4, -6, 8, 8, 6).texOffs(22, 0).addBox(-5, -5, -4, 1, 3, 1).texOffs(22, 0).addBox(4, -5, -4, 1, 3, 1),
                at(0, 4, -8));
        r.addOrReplaceChild("body", box(18, 4, -6, -10, -7, 12, 18, 10).texOffs(52, 0).addBox(-2, 2, -8, 4, 6, 1), at(0, 5, 2));
        legs(r, 12, 0, -6, 1);
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelHSSpectralPig: ModelQuadruped(6) with a snout. */
    public static LayerDefinition pig() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("head", box(0, 0, -4, -4, -8, 8, 8, 8).texOffs(16, 16).addBox(-2, 0, -9, 4, 3, 1), at(0, 12, -6));
        r.addOrReplaceChild("body", box(28, 8, -5, -10, -7, 10, 16, 8), at(0, 11, 2));
        legs(r, 6, 0, -5, 0);
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelHSSpectralSheep2 (body) / Sheep1 (fur, inflated). */
    public static LayerDefinition sheep(boolean fur) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        if (fur) {
            r.addOrReplaceChild("head", box(0, 0, -3, -4, -4, 6, 6, 6, 0.6F), at(0, 6, -8));
            r.addOrReplaceChild("body", box(28, 8, -4, -10, -7, 8, 16, 6, 1.75F), at(0, 5, 2));
            r.addOrReplaceChild("leg1", box(0, 16, -2, 0, -2, 4, 6, 4, 0.5F), at(-3, 12, 7));
            r.addOrReplaceChild("leg2", box(0, 16, -2, 0, -2, 4, 6, 4, 0.5F), at(3, 12, 7));
            r.addOrReplaceChild("leg3", box(0, 16, -2, 0, -2, 4, 6, 4, 0.5F), at(-3, 12, -5));
            r.addOrReplaceChild("leg4", box(0, 16, -2, 0, -2, 4, 6, 4, 0.5F), at(3, 12, -5));
        } else {
            r.addOrReplaceChild("head", box(0, 0, -3, -4, -6, 6, 6, 8), at(0, 6, -8));
            r.addOrReplaceChild("body", box(28, 8, -4, -10, -7, 8, 16, 6), at(0, 5, 2));
            legs(r, 12, 0, -5, 0);
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static final Anim QUADRUPED_ANIM = (s, r) -> {
        look(r.getChild("head"), s);
        r.getChild("body").xRot = Mth.HALF_PI;
        r.getChild("leg1").xRot = walk(s, 0, 1.4F);
        r.getChild("leg2").xRot = walk(s, Mth.PI, 1.4F);
        r.getChild("leg3").xRot = walk(s, Mth.PI, 1.4F);
        r.getChild("leg4").xRot = walk(s, 0, 1.4F);
    };

    public static final Anim SHEEP_ANIM = (s, r) -> {
        QUADRUPED_ANIM.apply(s, r);
        ModelPart head = r.getChild("head");
        head.y = 6.0F + s.headEatPos * 9.0F;
        head.xRot = s.headEatAngle;
    };

    // ------------------------------------------------------------------------------------------------ wolf
    public static LayerDefinition wolf() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("head", box(0, 0, -3, -3, -2, 6, 6, 4).texOffs(16, 14).addBox(-3, -5, 0, 2, 2, 1).texOffs(16, 14).addBox(1, -5, 0, 2, 2, 1)
                .texOffs(0, 10).addBox(-1.5F, 0, -5, 3, 3, 4), at(-1, 13.5F, -7));
        r.addOrReplaceChild("body", box(18, 14, -4, -2, -3, 6, 9, 6), at(0, 14, 2));
        r.addOrReplaceChild("mane", box(21, 0, -4, -3, -3, 8, 6, 7), at(-1, 14, 2));
        r.addOrReplaceChild("leg1", box(0, 18, -1, 0, -1, 2, 8, 2), at(-2.5F, 16, 7));
        r.addOrReplaceChild("leg2", box(0, 18, -1, 0, -1, 2, 8, 2), at(0.5F, 16, 7));
        r.addOrReplaceChild("leg3", box(0, 18, -1, 0, -1, 2, 8, 2), at(-2.5F, 16, -4));
        r.addOrReplaceChild("leg4", box(0, 18, -1, 0, -1, 2, 8, 2), at(0.5F, 16, -4));
        r.addOrReplaceChild("tail", box(9, 18, -1, 0, -1, 2, 8, 2), at(-1, 12, 8));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static final Anim WOLF_ANIM = (s, r) -> {
        ModelPart body = r.getChild("body");
        ModelPart mane = r.getChild("mane");
        ModelPart tail = r.getChild("tail");
        ModelPart[] leg = {r.getChild("leg1"), r.getChild("leg2"), r.getChild("leg3"), r.getChild("leg4")};
        tail.yRot = walk(s, 0, 1.4F);
        if (s.sitting) {
            mane.setPos(-1, 16, -3);
            mane.xRot = Mth.PI * 2.0F / 5.0F;
            body.setPos(0, 18, 0);
            body.xRot = Mth.PI / 4;
            tail.setPos(-1, 21, 6);
            leg[0].setPos(-2.5F, 22, 2);
            leg[0].xRot = Mth.PI * 3.0F / 2.0F;
            leg[1].setPos(0.5F, 22, 2);
            leg[1].xRot = Mth.PI * 3.0F / 2.0F;
            leg[2].xRot = 5.811947F;
            leg[2].setPos(-2.49F, 17, -4);
            leg[3].xRot = 5.811947F;
            leg[3].setPos(0.51F, 17, -4);
        } else {
            body.xRot = Mth.HALF_PI;
            mane.setPos(-1, 14, -3);
            mane.xRot = body.xRot;
            leg[0].xRot = walk(s, 0, 1.4F);
            leg[1].xRot = walk(s, Mth.PI, 1.4F);
            leg[2].xRot = walk(s, Mth.PI, 1.4F);
            leg[3].xRot = walk(s, 0, 1.4F);
        }
        ModelPart head = r.getChild("head");
        head.zRot = s.shakeHead;
        mane.zRot = s.shakeMane;
        body.zRot = s.shakeBody;
        tail.zRot = s.shakeTail;
        look(head, s);
        tail.xRot = s.tailAngle;
    };

    // ------------------------------------------------------------------------------------------------ ocelot
    public static LayerDefinition ocelot() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("head", box(0, 0, -2.5F, -2, -3, 5, 4, 5).texOffs(0, 24).addBox(-1.5F, 0, -4, 3, 2, 2).texOffs(0, 10)
                .addBox(-2, -3, 0, 1, 1, 2).texOffs(6, 10).addBox(1, -3, 0, 1, 1, 2), at(0, 15, -9));
        r.addOrReplaceChild("body", box(20, 0, -2, 3, -8, 4, 16, 6), at(0, 12, -10));
        r.addOrReplaceChild("tail", box(0, 15, -0.5F, 0, 0, 1, 8, 1), at(0, 15, 8));
        r.addOrReplaceChild("tail2", box(4, 15, -0.5F, 0, 0, 1, 8, 1), at(0, 20, 14));
        r.addOrReplaceChild("back_left_leg", box(8, 13, -1, 0, 1, 2, 6, 2), at(1.1F, 18, 5));
        r.addOrReplaceChild("back_right_leg", box(8, 13, -1, 0, 1, 2, 6, 2), at(-1.1F, 18, 5));
        r.addOrReplaceChild("front_left_leg", box(40, 0, -1, 0, 0, 2, 10, 2), at(1.2F, 13.8F, -5));
        r.addOrReplaceChild("front_right_leg", box(40, 0, -1, 0, 0, 2, 10, 2), at(-1.2F, 13.8F, -5));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static final Anim OCELOT_ANIM = (s, r) -> {
        ModelPart head = r.getChild("head");
        ModelPart body = r.getChild("body");
        ModelPart tail = r.getChild("tail");
        ModelPart tail2 = r.getChild("tail2");
        ModelPart bl = r.getChild("back_left_leg");
        ModelPart br = r.getChild("back_right_leg");
        ModelPart fl = r.getChild("front_left_leg");
        ModelPart fr = r.getChild("front_right_leg");
        tail.xRot = 0.9F;
        look(head, s);
        if (s.sitting) {
            body.xRot = Mth.PI / 4;
            body.y += -4.0F;
            body.z += 5.0F;
            head.y += -3.3F;
            head.z += 1.0F;
            tail.y += 8.0F;
            tail.z += -2.0F;
            tail2.y += 2.0F;
            tail2.z += -0.8F;
            tail.xRot = 1.7278761F;
            tail2.xRot = 2.670354F;
            fl.xRot = fr.xRot = -Mth.PI / 20;
            fl.y = fr.y = 15.8F;
            fl.z = fr.z = -7.0F;
            bl.xRot = br.xRot = -Mth.HALF_PI;
            bl.y = br.y = 21.0F;
            bl.z = br.z = 1.0F;
        } else {
            body.xRot = Mth.HALF_PI;
            float f = s.walkAnimationPos;
            float f1 = s.walkAnimationSpeed;
            bl.xRot = Mth.cos(f * 0.6662F) * f1;
            br.xRot = Mth.cos(f * 0.6662F + 0.3F) * f1;
            fl.xRot = Mth.cos(f * 0.6662F + Mth.PI + 0.3F) * f1;
            fr.xRot = Mth.cos(f * 0.6662F + Mth.PI) * f1;
            tail2.xRot = 1.7278761F + 0.47123894F * Mth.cos(f) * f1;
        }
    };

    // ------------------------------------------------------------------------------------------------ bat
    public static LayerDefinition bat() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        PartDefinition head = r.addOrReplaceChild("head", box(0, 0, -3, -3, -3, 6, 6, 6), PartPose.ZERO);
        head.addOrReplaceChild("right_ear", box(24, 0, -4, -6, -2, 3, 4, 1), PartPose.ZERO);
        head.addOrReplaceChild("left_ear", mirrored(24, 0, 1, -6, -2, 3, 4, 1, 0), PartPose.ZERO);
        PartDefinition body = r.addOrReplaceChild("body", box(0, 16, -3, 4, -3, 6, 12, 6).texOffs(0, 34).addBox(-5, 16, 0, 10, 6, 1), PartPose.ZERO);
        PartDefinition rw = body.addOrReplaceChild("right_wing", box(42, 0, -12, 1, 1.5F, 10, 16, 1), PartPose.ZERO);
        rw.addOrReplaceChild("right_wing_tip", box(24, 16, -8, 1, 0, 8, 12, 1), at(-12, 1, 1.5F));
        PartDefinition lw = body.addOrReplaceChild("left_wing", mirrored(42, 0, 2, 1, 1.5F, 10, 16, 1, 0), PartPose.ZERO);
        lw.addOrReplaceChild("left_wing_tip", mirrored(24, 16, 0, 1, 0, 8, 12, 1, 0), at(12, 1, 1.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static final Anim BAT_ANIM = (s, r) -> {
        ModelPart head = r.getChild("head");
        ModelPart body = r.getChild("body");
        ModelPart rw = body.getChild("right_wing");
        ModelPart lw = body.getChild("left_wing");
        ModelPart rt = rw.getChild("right_wing_tip");
        ModelPart lt = lw.getChild("left_wing_tip");
        if (s.resting) {
            head.xRot = s.xRot * Mth.DEG_TO_RAD;
            head.yRot = Mth.PI - s.yRot * Mth.DEG_TO_RAD;
            head.zRot = Mth.PI;
            head.setPos(0, -2, 0);
            rw.setPos(-3, 0, 3);
            lw.setPos(3, 0, 3);
            body.xRot = Mth.PI;
            rw.xRot = -Mth.PI / 20;
            rw.yRot = -Mth.PI * 2.0F / 5.0F;
            rt.yRot = -1.7278761F;
            lw.xRot = rw.xRot;
            lw.yRot = -rw.yRot;
            lt.yRot = -rt.yRot;
        } else {
            look(head, s);
            body.xRot = Mth.PI / 4 + Mth.cos(s.ageInTicks * 0.1F) * 0.15F;
            rw.yRot = Mth.cos(s.ageInTicks * 1.3F) * Mth.PI * 0.25F;
            lw.yRot = -rw.yRot;
            rt.yRot = rw.yRot * 0.5F;
            lt.yRot = -rw.yRot * 0.5F;
        }
    };

    // ------------------------------------------------------------------------------------------------ squid
    public static LayerDefinition squid() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("body", box(0, 0, -6, -8, -6, 12, 16, 12), at(0, 8, 0));
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI * 2.0 / 8;
            float yRot = (float) (i * Math.PI * -2.0 / 8 + Math.PI / 2);
            r.addOrReplaceChild("tentacle" + i, box(48, 0, -1, 0, -1, 2, 18, 2),
                    PartPose.offsetAndRotation((float) Math.cos(a) * 5.0F, 15, (float) Math.sin(a) * 5.0F, 0, yRot, 0));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static final Anim SQUID_ANIM = (s, r) -> {
        for (int i = 0; i < 8; i++) {
            r.getChild("tentacle" + i).xRot = s.tentacleAngle;
        }
    };

    // ------------------------------------------------------------------------------------------------ villager and witch
    private static PartDefinition villagerParts(PartDefinition r) {
        PartDefinition head = r.addOrReplaceChild("head", box(0, 0, -4, -10, -4, 8, 10, 8), PartPose.ZERO);
        head.addOrReplaceChild("nose", box(24, 0, -1, -1, -6, 2, 4, 2), at(0, -2, 0));
        r.addOrReplaceChild("body", box(16, 20, -4, 0, -3, 8, 12, 6).texOffs(0, 38).addBox(-4, 0, -3, 8, 18, 6, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        r.addOrReplaceChild("arms", box(44, 22, -8, -2, -2, 4, 8, 4).texOffs(44, 22).addBox(4, -2, -2, 4, 8, 4).texOffs(40, 38).addBox(-4, 2, -2, 8, 4, 4),
                at(0, 2, 0));
        r.addOrReplaceChild("right_leg", box(0, 22, -2, 0, -2, 4, 12, 4), at(-2, 12, 0));
        r.addOrReplaceChild("left_leg", mirrored(0, 22, -2, 0, -2, 4, 12, 4, 0), at(2, 12, 0));
        return head;
    }

    public static LayerDefinition villager() {
        MeshDefinition mesh = new MeshDefinition();
        villagerParts(mesh.getRoot());
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition witch() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition head = villagerParts(mesh.getRoot());
        head.getChild("nose").addOrReplaceChild("mole", box(0, 0, 0, 3, -6.75F, 1, 1, 1, -0.25F), at(0, -2, 0));
        PartDefinition hat = head.addOrReplaceChild("hat", box(0, 64, 0, 0, 0, 10, 2, 10), at(-5, -10.03125F, -5));
        PartDefinition hat2 = hat.addOrReplaceChild("hat2", box(0, 76, 0, 0, 0, 7, 4, 7),
                PartPose.offsetAndRotation(1.75F, -4, 2, -0.05235988F, 0, 0.02617994F));
        PartDefinition hat3 = hat2.addOrReplaceChild("hat3", box(0, 87, 0, 0, 0, 4, 4, 4),
                PartPose.offsetAndRotation(1.75F, -4, 2, -0.10471976F, 0, 0.05235988F));
        hat3.addOrReplaceChild("hat4", box(0, 95, 0, 0, 0, 1, 2, 1, 0.25F), PartPose.offsetAndRotation(1.75F, -2, 2, -0.20943952F, 0, 0.10471976F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static final Anim VILLAGER_ANIM = (s, r) -> {
        look(r.getChild("head"), s);
        ModelPart arms = r.getChild("arms");
        arms.y = 3.0F;
        arms.z = -1.0F;
        arms.xRot = -0.75F;
        r.getChild("right_leg").xRot = walk(s, 0, 1.4F) * 0.5F;
        r.getChild("left_leg").xRot = walk(s, Mth.PI, 1.4F) * 0.5F;
    };

    // ------------------------------------------------------------------------------------------------ creeper
    public static LayerDefinition creeper(float g) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("head", box(0, 0, -4, -8, -4, 8, 8, 8, g), at(0, 4, 0));
        r.addOrReplaceChild("body", box(16, 16, -4, 0, -2, 8, 12, 4, g), at(0, 4, 0));
        r.addOrReplaceChild("leg1", box(0, 16, -2, 0, -2, 4, 6, 4, g), at(-2, 16, 4));
        r.addOrReplaceChild("leg2", box(0, 16, -2, 0, -2, 4, 6, 4, g), at(2, 16, 4));
        r.addOrReplaceChild("leg3", box(0, 16, -2, 0, -2, 4, 6, 4, g), at(-2, 16, -4));
        r.addOrReplaceChild("leg4", box(0, 16, -2, 0, -2, 4, 6, 4, g), at(2, 16, -4));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static final Anim CREEPER_ANIM = (s, r) -> {
        look(r.getChild("head"), s);
        r.getChild("leg1").xRot = walk(s, 0, 1.4F);
        r.getChild("leg2").xRot = walk(s, Mth.PI, 1.4F);
        r.getChild("leg3").xRot = walk(s, Mth.PI, 1.4F);
        r.getChild("leg4").xRot = walk(s, 0, 1.4F);
    };

    // ------------------------------------------------------------------------------------------------ spider
    public static LayerDefinition spider() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("head", box(32, 4, -4, -4, -8, 8, 8, 8), at(0, 15, -3));
        r.addOrReplaceChild("neck", box(0, 0, -3, -3, -3, 6, 6, 6), at(0, 15, 0));
        r.addOrReplaceChild("body", box(0, 12, -5, -4, -6, 10, 8, 12), at(0, 15, 9));
        float[] z = {2, 2, 1, 1, 0, 0, -1, -1};
        for (int i = 0; i < 8; i++) {
            boolean right = i % 2 == 0;
            r.addOrReplaceChild("leg" + (i + 1), box(18, 0, right ? -15 : -1, -1, -1, 16, 2, 2), at(right ? -4 : 4, 15, z[i]));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static final Anim SPIDER_ANIM = (s, r) -> {
        look(r.getChild("head"), s);
        float q = Mth.PI / 4;
        float[] zr = {-q, q, -q * 0.74F, q * 0.74F, -q * 0.74F, q * 0.74F, -q, q};
        float e = Mth.PI / 8;
        float[] yr = {e * 2, -e * 2, e, -e, -e, e, -e * 2, e * 2};
        float f = s.walkAnimationPos;
        float f1 = s.walkAnimationSpeed;
        float[] swingY = new float[4];
        float[] swingZ = new float[4];
        float[] phase = {0, Mth.PI, Mth.HALF_PI, Mth.PI * 3 / 2};
        for (int i = 0; i < 4; i++) {
            swingY[i] = -(Mth.cos(f * 0.6662F * 2.0F + phase[i]) * 0.4F) * f1;
            swingZ[i] = Math.abs(Mth.sin(f * 0.6662F + phase[i]) * 0.4F) * f1;
        }
        for (int i = 0; i < 8; i++) {
            ModelPart leg = r.getChild("leg" + (i + 1));
            float sign = i % 2 == 0 ? 1 : -1;
            leg.zRot = zr[i] + sign * swingZ[i / 2];
            leg.yRot = yr[i] + sign * swingY[i / 2];
        }
    };

    // ------------------------------------------------------------------------------------------------ silverfish
    private static final int[][] SILVERFISH_BOX = {{3, 2, 2}, {4, 3, 2}, {6, 4, 3}, {3, 3, 3}, {2, 2, 3}, {2, 1, 2}, {1, 1, 2}};
    private static final int[][] SILVERFISH_UV = {{0, 0}, {0, 4}, {0, 9}, {0, 16}, {0, 22}, {11, 0}, {13, 4}};

    public static LayerDefinition silverfish() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        float[] zs = new float[7];
        float z = -3.5F;
        for (int i = 0; i < 7; i++) {
            int[] b = SILVERFISH_BOX[i];
            r.addOrReplaceChild("segment" + i, box(SILVERFISH_UV[i][0], SILVERFISH_UV[i][1], b[0] * -0.5F, 0, b[2] * -0.5F, b[0], b[1], b[2]),
                    at(0, 24 - b[1], z));
            zs[i] = z;
            if (i < 6) {
                z += (b[2] + SILVERFISH_BOX[i + 1][2]) * 0.5F;
            }
        }
        r.addOrReplaceChild("layer0", box(20, 0, -5, 0, SILVERFISH_BOX[2][2] * -0.5F, 10, 8, SILVERFISH_BOX[2][2]), at(0, 16, zs[2]));
        r.addOrReplaceChild("layer1", box(20, 11, -3, 0, SILVERFISH_BOX[4][2] * -0.5F, 6, 4, SILVERFISH_BOX[4][2]), at(0, 20, zs[4]));
        r.addOrReplaceChild("layer2", box(20, 18, -3, 0, SILVERFISH_BOX[4][2] * -0.5F, 6, 5, SILVERFISH_BOX[1][2]), at(0, 19, zs[1]));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static final Anim SILVERFISH_ANIM = (s, r) -> {
        float t = s.ageInTicks;
        for (int i = 0; i < 7; i++) {
            ModelPart seg = r.getChild("segment" + i);
            seg.yRot = Mth.cos(t * 0.9F + i * 0.15F * Mth.PI) * Mth.PI * 0.05F * (1 + Math.abs(i - 2));
            seg.x = Mth.sin(t * 0.9F + i * 0.15F * Mth.PI) * Mth.PI * 0.2F * Math.abs(i - 2);
        }
        r.getChild("layer0").yRot = r.getChild("segment2").yRot;
        r.getChild("layer1").yRot = r.getChild("segment4").yRot;
        r.getChild("layer1").x = r.getChild("segment4").x;
        r.getChild("layer2").yRot = r.getChild("segment1").yRot;
        r.getChild("layer2").x = r.getChild("segment1").x;
    };

    // ------------------------------------------------------------------------------------------------ enderman
    public static LayerDefinition enderman() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        float f = -14.0F;
        r.addOrReplaceChild("head", box(0, 0, -4, -8, -4, 8, 8, 8), at(0, f, 0));
        r.addOrReplaceChild("hat", box(0, 16, -4, -8, -4, 8, 8, 8, -0.5F), at(0, f, 0));
        r.addOrReplaceChild("body", box(32, 16, -4, 0, -2, 8, 12, 4), at(0, f, 0));
        r.addOrReplaceChild("right_arm", box(56, 0, -1, -2, -1, 2, 30, 2), at(-3, 2 + f, 0));
        r.addOrReplaceChild("left_arm", mirrored(56, 0, -1, -2, -1, 2, 30, 2, 0), at(5, 2 + f, 0));
        r.addOrReplaceChild("right_leg", box(56, 0, -1, 0, -1, 2, 30, 2), at(-2, 12 + f, 0));
        r.addOrReplaceChild("left_leg", mirrored(56, 0, -1, 0, -1, 2, 30, 2, 0), at(2, 12 + f, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelEnderman: half the swing of a biped, limited to 0.4; the head sinks when it screams. */
    public static final Anim ENDERMAN_ANIM = (s, r) -> {
        ModelPart head = r.getChild("head");
        ModelPart hat = r.getChild("hat");
        look(head, s);
        hat.xRot = head.xRot;
        hat.yRot = head.yRot;
        ModelPart ra = r.getChild("right_arm");
        ModelPart la = r.getChild("left_arm");
        ModelPart rl = r.getChild("right_leg");
        ModelPart ll = r.getChild("left_leg");
        ra.xRot = Mth.clamp(walk(s, Mth.PI, 1.0F) * 0.5F, -0.4F, 0.4F);
        la.xRot = Mth.clamp(walk(s, 0, 1.0F) * 0.5F, -0.4F, 0.4F);
        rl.xRot = Mth.clamp(walk(s, 0, 1.4F) * 0.5F, -0.4F, 0.4F);
        ll.xRot = Mth.clamp(walk(s, Mth.PI, 1.4F) * 0.5F, -0.4F, 0.4F);
        ra.zRot = Mth.cos(s.ageInTicks * 0.09F) * 0.05F + 0.05F;
        la.zRot = -ra.zRot;
        if (s.screaming) {
            head.y -= 5.0F;
        }
    };

    // ------------------------------------------------------------------------------------------------ blaze and ghast
    public static LayerDefinition blaze() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        for (int i = 0; i < 12; i++) {
            r.addOrReplaceChild("stick" + i, box(0, 16, 0, 0, 0, 2, 8, 2), PartPose.ZERO);
        }
        r.addOrReplaceChild("head", box(0, 0, -4, -4, -4, 8, 8, 8), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static final Anim BLAZE_ANIM = (s, r) -> {
        float t = s.ageInTicks;
        float a = t * Mth.PI * -0.1F;
        for (int i = 0; i < 4; i++) {
            ModelPart p = r.getChild("stick" + i);
            p.setPos(Mth.cos(a) * 9.0F, -2.0F + Mth.cos((i * 2 + t) * 0.25F), Mth.sin(a) * 9.0F);
            a++;
        }
        a = Mth.PI / 4 + t * Mth.PI * 0.03F;
        for (int i = 4; i < 8; i++) {
            ModelPart p = r.getChild("stick" + i);
            p.setPos(Mth.cos(a) * 7.0F, 2.0F + Mth.cos((i * 2 + t) * 0.25F), Mth.sin(a) * 7.0F);
            a++;
        }
        a = 0.47123894F + t * Mth.PI * -0.05F;
        for (int i = 8; i < 12; i++) {
            ModelPart p = r.getChild("stick" + i);
            p.setPos(Mth.cos(a) * 5.0F, 11.0F + Mth.cos((i * 1.5F + t) * 0.5F), Mth.sin(a) * 5.0F);
            a++;
        }
        look(r.getChild("head"), s);
    };

    public static LayerDefinition ghast() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("body", box(0, 0, -8, -8, -8, 16, 16, 16), at(0, 8, 0));
        Random random = new Random(1660L);
        for (int i = 0; i < 9; i++) {
            float x = ((i % 3 - i / 3 % 2 * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
            float z = (i / 3 / 2.0F * 2.0F - 1.0F) * 5.0F;
            int len = random.nextInt(7) + 8;
            r.addOrReplaceChild("tentacle" + i, box(0, 0, -1, 0, -1, 2, len, 2), at(x, 15, z));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static final Anim GHAST_ANIM = (s, r) -> {
        for (int i = 0; i < 9; i++) {
            r.getChild("tentacle" + i).xRot = 0.2F * Mth.sin(s.ageInTicks * 0.3F + i) + 0.4F;
        }
    };

    // ------------------------------------------------------------------------------------------------ bipeds
    /** ModelHSSpectralBiped: the vanilla humanoid mesh (64x32, or 64x64 for the zombies' textures). */
    public static LayerDefinition biped(int texHeight) {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, texHeight);
    }

    /** ModelHSSpectralSkeleton / SkeletonWither / ModelHSHarbinger: thin arms and legs. */
    public static LayerDefinition skeleton() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("right_arm", box(40, 16, -1, -2, -1, 2, 12, 2), at(-5, 2, 0));
        r.addOrReplaceChild("left_arm", mirrored(40, 16, -1, -2, -1, 2, 12, 2, 0), at(5, 2, 0));
        r.addOrReplaceChild("right_leg", box(0, 16, -1, 0, -1, 2, 12, 2), at(-2, 12, 0));
        r.addOrReplaceChild("left_leg", mirrored(0, 16, -1, 0, -1, 2, 12, 2, 0), at(2, 12, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** RenderHSSlime(new ModelSlime(1), ...): the inner cube one pixel down the texture, as in the original. */
    public static LayerDefinition slimeInner() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("cube", box(0, 1, -3, 17, -3, 6, 6, 6), PartPose.ZERO);
        r.addOrReplaceChild("right_eye", box(32, 0, -3.25F, 18, -3.5F, 2, 2, 2), PartPose.ZERO);
        r.addOrReplaceChild("left_eye", box(32, 4, 1.25F, 18, -3.5F, 2, 2, 2), PartPose.ZERO);
        r.addOrReplaceChild("mouth", box(32, 8, 0, 21, -3.5F, 1, 1, 1), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }
}
