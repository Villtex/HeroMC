package betterdungeons.client.human;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * client.model.ModelHuman and its subclasses (ModelHumanZombie, ModelSkeletonWarrior, ModelWalker, ModelSpecter(Boss),
 * ModelOgre, ModelGolemSmall, ModelGremlin, ModelMinotaur, ModelMonkey, ModelNaga, ModelEndermenArmored) on the
 * vanilla humanoid parts. setupAnim is ModelBiped.setRotationAngles of 1.5.2 (not today's humanoid animation), then
 * setHumanRotationAngles and what the kind adds. Geometry by {@link Geo}, animation by {@link Anim}.
 */
public class HumanModel extends HumanoidModel<HumanRenderState> {
    /** The constructors (geometry) of the models. */
    public enum Geo {
        BIPED, SKELETON, OGRE, GOLEM, GREMLIN, MINOTAUR, MONKEY, NAGA, ENDERMAN
    }

    /** The setRotationAngles overrides. */
    public enum Anim {
        HUMAN, ZOMBIE, MONKEY, GREMLIN, NAGA, ENDERMAN
    }

    private static final float DEG = Mth.DEG_TO_RAD;
    private final Anim anim;
    private final ModelPart @Nullable [] tail;
    private final ModelPart @Nullable [] mouths;

    public HumanModel(ModelPart root, Anim anim) {
        super(root);
        this.anim = anim;
        if (root.hasChild("tail0")) {
            this.tail = new ModelPart[8];
            for (int i = 0; i < 8; i++) {
                this.tail[i] = root.getChild("tail" + i);
            }
        } else {
            this.tail = null;
        }
        if (this.head.hasChild("mouth0")) {
            this.mouths = new ModelPart[4];
            for (int i = 0; i < 4; i++) {
                this.mouths[i] = this.head.getChild("mouth" + i);
            }
        } else {
            this.mouths = null;
        }
    }

    // ---------------------------------------------------------------- geometry

    /** ModelHuman(f) / ModelBiped(f): legs at x +-2 (today +-1.9). */
    private static MeshDefinition biped(CubeDeformation g) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, g),
                PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, g.extend(0.5F)), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, g), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, g),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, g),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, g),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, g),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        return mesh;
    }

    /** The head of the ogre and the gremlin (moved forward) with its hat. */
    private static void forwardHead(PartDefinition root, CubeDeformation g) {
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -5.0F, -7.0F, 8.0F, 8.0F, 8.0F, g),
                PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -5.0F, -7.0F, 8.0F, 8.0F, 8.0F, g.extend(0.5F)),
                PartPose.ZERO);
    }

    public static MeshDefinition mesh(Geo geo, CubeDeformation g) {
        MeshDefinition mesh = biped(g);
        PartDefinition root = mesh.getRoot();
        switch (geo) {
            case SKELETON -> {
                root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, g),
                        PartPose.offset(-5.0F, 2.0F, 0.0F));
                root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, g),
                        PartPose.offset(5.0F, 2.0F, 0.0F));
                root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, g),
                        PartPose.offset(-2.0F, 12.0F, 0.0F));
                root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, g),
                        PartPose.offset(2.0F, 12.0F, 0.0F));
            }
            case OGRE -> {
                root.getChild("body").addOrReplaceChild("body2", CubeListBuilder.create().texOffs(16, 22).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 6.0F, 4.0F),
                        PartPose.offset(0.0F, 6.0F, -3.0F));
                forwardHead(root, g);
                // render() put the legs at x -+2.5 before setRotationAngles set y 12
                legs(root, g, 2.5F);
            }
            case GOLEM -> {
                PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 6.0F, 4.0F, g),
                        PartPose.ZERO);
                body.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(16, 22).addBox(-3.0F, 6.0F, -2.0F, 6.0F, 6.0F, 4.0F, g), PartPose.ZERO);
                root.getChild("head").addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(25, 0).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F),
                        PartPose.ZERO);
                legs(root, g, 3.0F);
            }
            case GREMLIN -> {
                // setRotationAngles moved both legs to (2, 7, 8)
                root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(0.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, g),
                        PartPose.offset(2.0F, 7.0F, 8.0F));
                root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-8.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, g),
                        PartPose.offset(2.0F, 7.0F, 8.0F));
                forwardHead(root, g);
            }
            case MINOTAUR -> {
                PartDefinition head = root.getChild("head");
                PartPose horn = PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, -0.0569039F, 0.0F, 0.0F);
                head.addOrReplaceChild("horn1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -4.0F, 2.0F, 2.0F, 2.0F), horn);
                head.addOrReplaceChild("horn11", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -5.0F, 2.0F, 2.0F, 2.0F), horn);
                head.addOrReplaceChild("horn12", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -8.0F, 1.0F, 1.0F, 3.0F), horn);
                head.addOrReplaceChild("horn2", CubeListBuilder.create().texOffs(0, 0).addBox(2.0F, -7.0F, -4.0F, 2.0F, 2.0F, 2.0F), horn);
                head.addOrReplaceChild("horn21", CubeListBuilder.create().texOffs(0, 0).addBox(3.0F, -8.0F, -5.0F, 2.0F, 2.0F, 2.0F), horn);
                head.addOrReplaceChild("horn22", CubeListBuilder.create().texOffs(0, 0).addBox(4.0F, -8.0F, -8.0F, 1.0F, 1.0F, 3.0F), horn);
                head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(25, 0).addBox(-2.0F, -3.0F, -6.0F, 4.0F, 3.0F, 3.0F), PartPose.ZERO);
            }
            case MONKEY -> {
                root.getChild("head").addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(25, 0).addBox(-2.0F, -3.0F, -6.0F, 4.0F, 3.0F, 3.0F),
                        PartPose.ZERO);
                // the one tail cube of ModelMonkey was drawn eight times along the tail
                for (int i = 0; i < 8; i++) {
                    root.addOrReplaceChild("tail" + i, CubeListBuilder.create().texOffs(0, 20).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                            PartPose.offset(0.0F, -3.0F, 14.0F));
                }
            }
            case NAGA -> {
                PartDefinition head = root.getChild("head");
                for (int i = 0; i < 4; i++) {
                    head.addOrReplaceChild("mouth" + i, CubeListBuilder.create().texOffs(24, 0).addBox(-2.0F + i, -3.0F, -7.0F, 1.0F, 1.0F, 4.0F),
                            PartPose.ZERO);
                }
                // ModelNaga drew no legs; three tail cubes were drawn eight times (3 x big, 2 x middle, 3 x tip)
                root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-2.0F, 12.0F, 0.0F));
                root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(2.0F, 12.0F, 0.0F));
                int[] kinds = {0, 0, 0, 1, 1, 2, 2, 2};
                for (int i = 0; i < 8; i++) {
                    CubeListBuilder cube = switch (kinds[i]) {
                        case 0 -> CubeListBuilder.create().texOffs(34, 0).addBox(-6.0F, 0.0F, -2.0F, 8.0F, 4.0F, 4.0F);
                        case 1 -> CubeListBuilder.create().texOffs(34, 8).addBox(-5.0F, 0.0F, -2.0F, 6.0F, 4.0F, 4.0F);
                        default -> CubeListBuilder.create().texOffs(1, 17).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 3.0F, 3.0F);
                    };
                    root.addOrReplaceChild("tail" + i, cube, PartPose.offset(0.0F, -3.0F, 14.0F));
                }
            }
            case ENDERMAN -> {
                float y = -14.0F;
                PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, g),
                        PartPose.offset(0.0F, y, 0.0F));
                head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, g.extend(-0.5F)),
                        PartPose.ZERO);
                root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, g),
                        PartPose.offset(0.0F, y, 0.0F));
                root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, g),
                        PartPose.offset(-3.0F, 2.0F + y, 0.0F));
                root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, g),
                        PartPose.offset(5.0F, 2.0F + y, 0.0F));
                root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, g),
                        PartPose.offset(-2.0F, 12.0F + y, 0.0F));
                root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, g),
                        PartPose.offset(2.0F, 12.0F + y, 0.0F));
            }
            default -> {
            }
        }
        return mesh;
    }

    private static void legs(PartDefinition root, CubeDeformation g, float x) {
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, g),
                PartPose.offset(-x, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, g),
                PartPose.offset(x, 12.0F, 0.0F));
    }

    private static final Map<String, Set<String>> SLOTS = Map.of("head", Set.of("head"), "chest", Set.of("body", "left_arm", "right_arm"),
            "legs", Set.of("left_leg", "right_leg", "body"), "feet", Set.of("left_leg", "right_leg"));

    /** The armour models of RenderBiped (ModelX(1.0) for helmet, plate and boots, ModelX(0.5) for leggings), one per slot. */
    public static ArmorModelSet<MeshDefinition> armorMeshes(Geo geo) {
        return new ArmorModelSet<>(retain(mesh(geo, new CubeDeformation(1.0F)), SLOTS.get("head")),
                retain(mesh(geo, new CubeDeformation(1.0F)), SLOTS.get("chest")), retain(mesh(geo, new CubeDeformation(0.5F)), SLOTS.get("legs")),
                retain(mesh(geo, new CubeDeformation(1.0F)), SLOTS.get("feet")));
    }

    /** Keeps the cubes of the named top parts (with their children), empties everything else. */
    private static MeshDefinition retain(MeshDefinition mesh, Set<String> keep) {
        PartDefinition root = mesh.getRoot();
        for (String name : Set.of("head", "body", "right_arm", "left_arm", "right_leg", "left_leg", "tail0", "tail1", "tail2", "tail3", "tail4",
                "tail5", "tail6", "tail7")) {
            if (!keep.contains(name)) {
                try {
                    empty(root, name);
                } catch (IllegalArgumentException missing) {
                    // the geometry has no such part
                }
            }
        }
        return mesh;
    }

    private static void empty(PartDefinition parent, String name) {
        PartDefinition child = parent.clearChild(name);
        for (String sub : childNames(name)) {
            try {
                empty(child, sub);
            } catch (IllegalArgumentException missing) {
                // not in this geometry
            }
        }
    }

    private static Set<String> childNames(String part) {
        return switch (part) {
            case "head" -> Set.of("hat", "mouth", "mouth0", "mouth1", "mouth2", "mouth3", "horn1", "horn11", "horn12", "horn2", "horn21", "horn22");
            case "body" -> Set.of("body2");
            default -> Set.of();
        };
    }

    // ---------------------------------------------------------------- animation

    @Override
    public void setupAnim(HumanRenderState s) {
        this.resetPose();
        this.bipedAngles(s);
        this.humanAngles(s);
        switch (this.anim) {
            case MONKEY -> {
                if (s.twoHanded && !s.bannerman) {
                    this.rightArm.xRot += 0.5F;
                    this.leftArm.xRot += 0.5F;
                } else {
                    this.rightArm.xRot -= 0.7F;
                    this.leftArm.xRot -= 0.7F;
                }
                this.monkeyTail(s);
            }
            case GREMLIN -> {
                if (s.twoHanded && !s.bannerman) {
                    this.rightArm.xRot += 0.5F;
                    this.leftArm.xRot += 0.5F;
                } else if (!s.aiming) {
                    this.rightArm.xRot -= 0.7F;
                    this.leftArm.xRot -= 0.7F;
                }
                if (s.defending) {
                    this.setShiedRotation(s.ageInTicks);
                }
                this.rightLeg.x = 2.0F;
                this.rightLeg.y = 7.0F;
                this.rightLeg.z = 8.0F;
                this.leftLeg.x = 2.0F;
                this.leftLeg.y = 7.0F;
                this.leftLeg.z = 8.0F;
                this.body.xRot = 0.7F;
            }
            case NAGA -> this.nagaTail(s);
            case ENDERMAN -> this.endermanAngles(s);
            default -> {
            }
        }
    }

    /** ModelBiped.setRotationAngles of 1.5.2 (f = walk position, f1 = walk speed, f2 = age in ticks). */
    private void bipedAngles(HumanRenderState s) {
        float f = s.walkAnimationPos;
        float f1 = s.walkAnimationSpeed;
        float f2 = s.ageInTicks;
        this.head.yRot = s.yRot * DEG;
        this.head.xRot = s.xRot * DEG;
        this.rightArm.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 2.0F * f1 * 0.5F;
        this.leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * f1 * 0.5F;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        this.leftLeg.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
        this.rightLeg.yRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
        if (s.riding) {
            this.rightArm.xRot += -Mth.PI / 5.0F;
            this.leftArm.xRot += -Mth.PI / 5.0F;
            this.rightLeg.xRot = -Mth.PI * 2.0F / 5.0F;
            this.leftLeg.xRot = -Mth.PI * 2.0F / 5.0F;
            this.rightLeg.yRot = Mth.PI / 10.0F;
            this.leftLeg.yRot = -Mth.PI / 10.0F;
        }
        if (s.hasMainHandItem) {
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - Mth.PI / 10.0F;
        }
        this.rightArm.yRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        float swing = s.swingAnimation;
        this.body.yRot = Mth.sin(Mth.sqrt(swing) * Mth.PI * 2.0F) * 0.2F;
        this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
        this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
        this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
        this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;
        this.rightArm.yRot += this.body.yRot;
        this.leftArm.yRot += this.body.yRot;
        this.leftArm.xRot += this.body.yRot;
        float var8 = 1.0F - swing;
        var8 *= var8;
        var8 *= var8;
        var8 = 1.0F - var8;
        float var9 = Mth.sin(var8 * Mth.PI);
        float var10 = Mth.sin(swing * Mth.PI) * -(this.head.xRot - 0.7F) * 0.75F;
        this.rightArm.xRot = (float) (this.rightArm.xRot - (var9 * 1.2 + var10));
        this.rightArm.yRot += this.body.yRot * 2.0F;
        this.rightArm.zRot = Mth.sin(swing * Mth.PI) * -0.4F;
        if (s.sneakPose) {
            this.body.xRot = 0.5F;
            this.rightArm.xRot += 0.4F;
            this.leftArm.xRot += 0.4F;
            this.rightLeg.z = 4.0F;
            this.leftLeg.z = 4.0F;
            this.rightLeg.y = 9.0F;
            this.leftLeg.y = 9.0F;
            this.head.y = 1.0F;
        } else {
            this.body.xRot = 0.0F;
            this.rightLeg.z = 0.1F;
            this.leftLeg.z = 0.1F;
            this.rightLeg.y = 12.0F;
            this.leftLeg.y = 12.0F;
            this.head.y = 0.0F;
        }
        this.rightArm.zRot += Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(f2 * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(f2 * 0.067F) * 0.05F;
    }

    /** ModelHuman.setHumanRotationAngles (with the zombie arms of ModelHumanZombie first). */
    private void humanAngles(HumanRenderState s) {
        float f2 = s.ageInTicks;
        if (this.anim == Anim.ZOMBIE && !s.sitting) {
            float f6 = Mth.sin(s.swingAnimation * Mth.PI);
            float f7 = Mth.sin((1.0F - (1.0F - s.swingAnimation) * (1.0F - s.swingAnimation)) * Mth.PI);
            this.rightArm.zRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightArm.yRot = 0.1F - f6 * 0.6F;
            this.rightArm.xRot = -Mth.HALF_PI;
            this.leftArm.xRot = -Mth.HALF_PI;
            this.rightArm.xRot += f6 * 1.2F - f7 * 0.4F;
            this.rightArm.zRot += Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
            this.leftArm.zRot -= Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
            this.rightArm.xRot += Mth.sin(f2 * 0.067F) * 0.05F;
            this.leftArm.xRot -= Mth.sin(f2 * 0.067F) * 0.05F;
        }
        if (s.sitting) {
            this.rightArm.xRot += -Mth.PI / 5.0F;
            this.leftArm.xRot += -Mth.PI / 5.0F;
            this.rightLeg.xRot = -Mth.HALF_PI;
            this.leftLeg.xRot = -Mth.HALF_PI;
            this.rightLeg.yRot = Mth.PI / 10.0F;
            this.leftLeg.yRot = -Mth.PI / 10.0F;
        } else {
            if (s.sneakPose) {
                this.body.xRot = 0.5F;
                this.rightArm.xRot += 0.4F;
                this.leftArm.xRot += 0.4F;
                this.rightLeg.z = 4.0F;
                this.leftLeg.z = 4.0F;
                this.rightLeg.y = 9.0F;
                this.leftLeg.y = 9.0F;
                this.head.y = 1.0F;
            }
            if (s.aiming) {
                if (s.gunner) {
                    this.setAimingAnglesLeft(f2);
                } else {
                    this.setAimingAngles(f2);
                }
            }
            if (s.defending) {
                this.setShiedRotation(f2);
            }
            if (s.twoHanded) {
                this.setTwoHandedAngles(f2, s.swingAnimation);
            }
        }
    }

    private void setTwoHandedAngles(float time, float swingProgress) {
        float swing = Mth.sin(swingProgress * Mth.PI);
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = -0.3F;
        this.rightArm.yRot = -0.6F;
        this.leftArm.yRot = 0.6F;
        this.rightArm.xRot = -0.8F + swing;
        this.leftArm.xRot = -0.8F + swing;
        this.rightArm.zRot += Mth.cos(time * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(time * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(time * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(time * 0.067F) * 0.05F;
        float f6 = 1.0F - swingProgress;
        f6 *= f6;
        f6 *= f6;
        f6 = 1.0F - f6;
        float f7 = Mth.sin(f6 * Mth.PI);
        float f8 = Mth.sin(swingProgress * Mth.PI) * -(this.head.xRot - 0.7F) * 0.75F;
        this.rightArm.xRot = (float) (this.rightArm.xRot - (f7 * 1.2 + f8));
        this.rightArm.yRot += this.body.yRot * 2.0F;
        this.rightArm.zRot = Mth.sin(swingProgress * Mth.PI) * -0.4F;
        this.leftArm.xRot = (float) (this.rightArm.xRot - (f7 * 1.2 + f8));
        this.leftArm.yRot += this.body.yRot * 2.0F;
        this.leftArm.zRot = Mth.sin(swingProgress * Mth.PI) * -0.4F;
    }

    private void setAimingAngles(float time) {
        if (this.anim == Anim.ENDERMAN) {
            return;
        }
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightArm.yRot = -0.1F + this.head.yRot;
        this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
        this.rightArm.xRot = -Mth.HALF_PI + this.head.xRot;
        this.leftArm.xRot = -Mth.HALF_PI + this.head.xRot;
        this.rightArm.zRot += Mth.cos(time * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(time * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(time * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(time * 0.067F) * 0.05F;
    }

    private void setAimingAnglesLeft(float time) {
        this.leftArm.zRot = 0.0F;
        this.leftArm.yRot = 0.1F + this.head.yRot;
        this.leftArm.xRot = -Mth.HALF_PI + this.head.xRot;
        this.rightArm.zRot += Mth.cos(time * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(time * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(time * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(time * 0.067F) * 0.05F;
        if (this.anim == Anim.MONKEY) {
            this.leftArm.xRot += 0.7F;
        }
    }

    private void setShiedRotation(float time) {
        // ModelMonkey used f9 = 2: the arm 0.8 lower
        float f9 = this.anim == Anim.MONKEY ? 2.0F : 0.0F;
        this.leftArm.zRot = -0.7F;
        this.leftArm.yRot = 1.2F;
        this.leftArm.xRot = -Mth.HALF_PI + this.head.xRot;
        this.leftArm.xRot -= -f9 * 0.4F;
        this.leftArm.zRot -= Mth.cos(time * 0.09F) * 0.05F + 0.05F;
        this.leftArm.xRot -= Mth.sin(time * 0.067F) * 0.05F;
    }

    /** ModelMonkey.render: the tail cube eight times along a waving curve. */
    private void monkeyTail(HumanRenderState s) {
        if (this.tail == null) {
            return;
        }
        float px = -0.5F;
        float py = 8.0F;
        float pz = 8.0F;
        float dist = 1.3F;
        for (int i = 0; i < 8; i++) {
            float f10 = (float) Math.cos((15.0 - i / 15 + s.walkAnimationPos / 10.0F) * Math.PI * 2.0);
            float anim = (float) Math.cos((i / 20.0 + s.ageInTicks / 50.0F) * Math.PI * 2.0);
            ModelPart t = this.tail[i];
            t.xRot = 3.86F + f10 / 10.0F;
            t.yRot = anim;
            t.zRot = 0.0F;
            t.x = px;
            t.y = py;
            t.z = pz;
            px = (float) (px - Math.sin(t.yRot) * Math.cos(t.xRot) * dist) * i / 5.0F;
            py = (float) (py + Math.sin(t.xRot) * 0.8);
            pz = (float) (pz - Math.cos(t.yRot) * Math.cos(t.xRot) * dist);
        }
    }

    /** ModelNaga.render: moving mouth parts and the tail behind the body. */
    private void nagaTail(HumanRenderState s) {
        if (this.mouths != null) {
            for (int i = 0; i < this.mouths.length; i++) {
                this.mouths[i].xRot = (float) Math.sin(s.ageInTicks / 10.0F + i * 20) / 6.0F + 0.5F;
            }
        }
        if (this.tail == null) {
            return;
        }
        float px = 1.5F;
        float py = 10.0F;
        float pz = 2.0F;
        float dist = 3.0F;
        float sway = Mth.cos(s.walkAnimationPos * 0.6662F) * 1.4F * s.walkAnimationSpeed;
        for (int i = 0; i < 5; i++) {
            py += i;
            pz -= (float) Math.sin(i + 1);
            px += sway;
            this.tail[i].x = px;
            this.tail[i].y = py;
            this.tail[i].z = pz;
        }
        for (int i = 0; i < 3; i++) {
            px += sway;
            this.tail[5 + i].x = px;
            this.tail[5 + i].y = py;
            this.tail[5 + i].z = pz + i * dist;
        }
    }

    /** ModelEndermenArmored.setRotationAngles. */
    private void endermanAngles(HumanRenderState s) {
        float y = -14.0F;
        this.body.xRot = 0.0F;
        this.body.y = y;
        this.body.z = 0.0F;
        this.rightArm.xRot *= 0.5F;
        this.leftArm.xRot *= 0.5F;
        this.rightLeg.xRot *= 0.5F;
        this.leftLeg.xRot *= 0.5F;
        float max = 0.4F;
        this.rightArm.xRot = Mth.clamp(this.rightArm.xRot, -max, max);
        this.leftArm.xRot = Mth.clamp(this.leftArm.xRot, -max, max);
        this.rightLeg.xRot = Mth.clamp(this.rightLeg.xRot, -max, max);
        this.leftLeg.xRot = Mth.clamp(this.leftLeg.xRot, -max, max);
        this.rightArm.z = 0.0F;
        this.leftArm.z = 0.0F;
        this.rightLeg.z = 0.0F;
        this.leftLeg.z = 0.0F;
        this.rightLeg.y = 9.0F + y;
        this.leftLeg.y = 9.0F + y;
        this.head.z = 0.0F;
        this.head.y = y + 1.0F;
        if (s.swingInProgress || s.aiming) {
            this.head.y -= 5.0F;
        }
        if (s.aiming) {
            // setAimingAngles(par2): 1.5.2 passed the walk speed as the time
            float time = s.walkAnimationSpeed;
            float f7 = 0.1F;
            float f9 = 1.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightArm.yRot = -(0.2F - f7 * 0.6F) + this.head.yRot;
            this.leftArm.yRot = -0.1F - f7 * 0.6F + this.head.yRot + 0.4F;
            this.rightArm.xRot = -Mth.HALF_PI + this.head.xRot - (f7 * 1.2F - f9 * 0.4F);
            this.leftArm.xRot = -Mth.HALF_PI + this.head.xRot - (f7 * 1.2F - f9 * 0.4F);
            this.rightArm.zRot += Mth.cos(time * 0.09F) * 0.05F + 0.05F;
            this.leftArm.zRot -= Mth.cos(time * 0.09F) * 0.05F + 0.05F;
            this.rightArm.xRot += Mth.sin(time * 0.067F) * 0.05F;
            this.leftArm.xRot -= Mth.sin(time * 0.067F) * 0.05F;
        }
        if (s.defending) {
            this.setShiedRotation(s.walkAnimationSpeed);
        }
    }
}
