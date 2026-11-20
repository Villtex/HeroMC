package projectzulu.client.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jspecify.annotations.Nullable;
import projectzulu.ProjectZulu;
import projectzulu.block.gear.MobHead;
import projectzulu.block.gear.MobHeadBlock;
import projectzulu.block.gear.MobHeadBlockEntity;
import projectzulu.client.entity.FinchModel;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * TileEntityMobHeadsRenderer: each head with its model, the creature's texture, its scale and its offset for the
 * side it hangs on (floor or one of four walls). On the floor it turns with the rotation of the block entity, on a
 * wall it looks away from the wall. Giraffe, mammoth, ostrich, vulture (and elephant) take a second pose on the
 * floor (skullState 1). Drawn without back-face culling like the original (GL_CULL_FACE disabled).
 */
public class MobHeadRenderer implements BlockEntityRenderer<MobHeadBlockEntity, MobHeadRenderer.State> {
    private static final float PI = (float) Math.PI;

    /** The models of blocks/heads, one layer each. */
    enum Kind {
        FINCH(FinchModel::createBodyLayer), CROCODILE(MobHeadModels::crocodile), ARMADILLO(MobHeadModels::armadillo), BEAR(MobHeadModels::bear),
        BEAVER(MobHeadModels::beaver), BOAR(MobHeadModels::boar), GIRAFFE(MobHeadModels::giraffe), GORILLA(MobHeadModels::gorilla),
        LIZARD(MobHeadModels::lizard), MAMMOTH(MobHeadModels::mammoth), OSTRICH(MobHeadModels::ostrich), PENGUIN(MobHeadModels::penguin),
        RHINO(MobHeadModels::rhino), TREE_ENT(MobHeadModels::treeEnt), VULTURE(MobHeadModels::vulture), ELEPHANT(MobHeadModels::elephant);

        final ModelLayerLocation layer = new ModelLayerLocation(Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "mob_head"),
                this.name().toLowerCase());
        final Supplier<LayerDefinition> definition;

        Kind(Supplier<LayerDefinition> definition) {
            this.definition = definition;
        }
    }

    /** HeadRender of the original: model, scale (f5) and the translation for metadata 0-5. */
    private record Render(Kind model, float scale, float[][] offsets) {
    }

    private static final Map<MobHead, Render> RENDERS = new EnumMap<>(MobHead.class);

    static {
        RENDERS.put(MobHead.RED_FINCH, new Render(Kind.FINCH, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.74F}, {0.5F, 1.5F, 0.26F}, {0.74F, 1.5F, 0.5F}, {0.26F, 1.5F, 0.5F}}));
        RENDERS.put(MobHead.ALLIGATOR, new Render(Kind.CROCODILE, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.5F}, {0.5F, 1.75F, 0.55F}, {0.5F, 1.75F, 0.4F}, {0.55F, 1.75F, 0.5F}, {0.45F, 1.75F, 0.5F}}));
        RENDERS.put(MobHead.ARMADILLO, new Render(Kind.ARMADILLO, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.5F}, {0.5F, 1.75F, 0.8F}, {0.5F, 1.75F, 0.2F}, {0.8F, 1.75F, 0.5F}, {0.2F, 1.75F, 0.5F}}));
        RENDERS.put(MobHead.BLACK_BEAR, new Render(Kind.BEAR, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.5F}, {0.5F, 1.75F, 0.85F}, {0.5F, 1.75F, 0.15F}, {0.85F, 1.75F, 0.5F}, {0.15F, 1.75F, 0.5F}}));
        RENDERS.put(MobHead.BROWN_BEAR, new Render(Kind.BEAR, 0.08F, new float[][] {
                {0.26F, 2.0F, 0.5F}, {0.5F, 1.9F, 0.5F}, {0.5F, 2.1F, 0.85F}, {0.5F, 2.1F, 0.15F}, {0.85F, 2.1F, 0.5F}, {0.15F, 2.1F, 0.5F}}));
        RENDERS.put(MobHead.POLAR_BEAR, new Render(Kind.BEAR, 0.1F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 2.4F, 0.5F}, {0.5F, 2.5F, 0.8F}, {0.5F, 2.5F, 0.2F}, {0.8F, 2.5F, 0.5F}, {0.2F, 2.5F, 0.5F}}));
        RENDERS.put(MobHead.BEAVER, new Render(Kind.BEAVER, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.5F}, {0.5F, 1.75F, 0.85F}, {0.5F, 1.75F, 0.15F}, {0.85F, 1.75F, 0.5F}, {0.15F, 1.75F, 0.5F}}));
        RENDERS.put(MobHead.BOAR, new Render(Kind.BOAR, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.5F}, {0.5F, 1.75F, 0.7F}, {0.5F, 1.75F, 0.3F}, {0.7F, 1.75F, 0.5F}, {0.3F, 1.75F, 0.5F}}));
        RENDERS.put(MobHead.GIRAFFE, new Render(Kind.GIRAFFE, 0.035F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 0.85F, 0.5F}, {0.5F, 1.15F, 1.0F}, {0.5F, 1.15F, 0.0F}, {1.0F, 1.15F, 0.5F}, {0.0F, 1.15F, 0.5F}}));
        RENDERS.put(MobHead.GORILLA, new Render(Kind.GORILLA, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.5F}, {0.5F, 1.75F, 0.8F}, {0.5F, 1.75F, 0.2F}, {0.8F, 1.75F, 0.5F}, {0.2F, 1.75F, 0.5F}}));
        RENDERS.put(MobHead.LIZARD, new Render(Kind.LIZARD, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.5F}, {0.5F, 1.75F, 0.8F}, {0.5F, 1.75F, 0.2F}, {0.8F, 1.75F, 0.5F}, {0.2F, 1.75F, 0.5F}}));
        RENDERS.put(MobHead.MAMMOTH, new Render(Kind.MAMMOTH, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.4F, 0.5F}, {0.5F, 1.56F, 0.7F}, {0.5F, 1.56F, 0.3F}, {0.7F, 1.56F, 0.5F}, {0.3F, 1.56F, 0.5F}}));
        RENDERS.put(MobHead.OSTRICH, new Render(Kind.OSTRICH, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.5F}, {0.5F, 1.75F, 1.2F}, {0.5F, 1.75F, -0.1F}, {1.15F, 1.75F, 0.5F}, {-0.15F, 1.75F, 0.5F}}));
        RENDERS.put(MobHead.PENGUIN, new Render(Kind.PENGUIN, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.5F}, {0.5F, 1.85F, 0.75F}, {0.5F, 1.85F, 0.25F}, {0.75F, 1.85F, 0.5F}, {0.25F, 1.85F, 0.5F}}));
        RENDERS.put(MobHead.RHINO, new Render(Kind.RHINO, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.5F}, {0.5F, 1.75F, 0.6F}, {0.5F, 1.75F, 0.4F}, {0.6F, 1.75F, 0.5F}, {0.4F, 1.75F, 0.5F}}));
        RENDERS.put(MobHead.TREE_ENT, new Render(Kind.TREE_ENT, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.5F, 0.5F}, {0.5F, 1.75F, 0.55F}, {0.5F, 1.75F, 0.4F}, {0.55F, 1.75F, 0.5F}, {0.45F, 1.75F, 0.5F}}));
        RENDERS.put(MobHead.VULTURE, new Render(Kind.VULTURE, 0.0625F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 1.55F, 0.5F}, {0.5F, 2.1F, 0.95F}, {0.5F, 2.1F, 0.05F}, {0.95F, 2.1F, 0.5F}, {0.05F, 2.1F, 0.5F}}));
        RENDERS.put(MobHead.ELEPHANT, new Render(Kind.ELEPHANT, 0.0505F, new float[][] {
                {0.26F, 1.5F, 0.5F}, {0.5F, 0.9F, 0.5F}, {0.5F, 0.9F, 0.59F}, {0.5F, 0.9F, 0.41F}, {0.59F, 0.9F, 0.5F}, {0.41F, 0.9F, 0.5F}}));
    }

    public static class State extends BlockEntityRenderState {
        MobHead head = MobHead.RED_FINCH;
        int side = 1;
        int rotation;
    }

    /** The arguments of setRotationAngles the head models used: f3 (rotation in degrees) and f4 (skullState). */
    record Pose(float degrees, int skullState) {
    }

    /** One baked head model; the pose is applied when the collector draws it. */
    static final class HeadModel extends net.minecraft.client.model.Model<Pose> {
        private final Kind kind;
        private final Function<String, ModelPart> part;

        HeadModel(Kind kind, ModelPart root) {
            super(root, RenderTypes::entityCutoutNoCull);
            this.kind = kind;
            this.part = root.createPartLookup();
        }

        @Override
        public void setupAnim(Pose pose) {
            super.setupAnim(pose);
            this.resetPose();
            MobHeadRenderer.pose(this.kind, this.part, pose.degrees() * (PI / 180.0F), pose.degrees(), pose.skullState());
        }
    }

    private final Map<Kind, HeadModel> models = new EnumMap<>(Kind.class);

    public MobHeadRenderer(BlockEntityRendererProvider.Context context) {
        for (Kind model : Kind.values()) {
            this.models.put(model, new HeadModel(model, context.bakeLayer(model.layer)));
        }
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (Kind model : Kind.values()) {
            event.registerLayerDefinition(model.layer, model.definition);
        }
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MobHeadBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.head = entity.getHead();
        state.side = MobHeadBlock.side(entity.getBlockState().getValue(MobHeadBlock.FACING));
        state.rotation = entity.getRotation();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Render render = RENDERS.get(state.head);
        float rotation = switch (state.side) {
            case 1, 2 -> state.rotation * 360 / 16.0F;
            case 3 -> 180.0F;
            case 4 -> 270.0F;
            default -> 90.0F;
        };
        int skullState = 0;
        if (state.side == 1 && (render.model == Kind.GIRAFFE || render.model == Kind.MAMMOTH || render.model == Kind.OSTRICH
                || render.model == Kind.VULTURE || render.model == Kind.ELEPHANT)) {
            skullState = 1;
        }
        float[] offset = render.offsets[state.side];
        poseStack.pushPose();
        poseStack.translate(offset[0], offset[1], offset[2]);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        float scale = render.scale / 0.0625F;
        poseStack.scale(scale, scale, scale);
        Identifier texture = Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "textures/entity/" + state.head.texture + ".png");
        collector.submitModel(this.models.get(render.model), new Pose(rotation, skullState), poseStack, RenderTypes.entityCutoutNoCull(texture),
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    /** setRotationAngles(0, 0, 0, rotation, skullState, scale) of the head models. */
    static void pose(Kind model, Function<String, ModelPart> part, float yRot, float degrees, int skullState) {
        switch (model) {
            case FINCH -> {
                ModelPart head = part.apply("headrot");
                head.xRot = Math.min(Math.max(skullState, -15.0F), 15.0F) * (PI / 180.0F);
                head.yRot = Math.min(Math.max(degrees, -45.0F), 45.0F) * (PI / 180.0F);
            }
            case GIRAFFE -> {
                boolean standing = skullState == 1;
                part.apply("NECKROT2").xRot = standing ? 0.0F : PI / 2;
                part.apply("NECKROT3").xRot = 0.0F;
                part.apply("NECKROT4").xRot = standing ? 0.0F : -PI / 6;
                part.apply("NECKROT5").xRot = standing ? 0.0F : -PI / 6;
                part.apply("NECKROT6").xRot = standing ? 0.0F : -PI / 6;
                part.apply("NECKROT7").xRot = 0.0F;
                part.apply("HEADROT").xRot = standing ? 0.08726646F : 0.0F;
                part.apply("NECKROT2").yRot = yRot;
            }
            case GORILLA -> part.apply("HEADROT").yRot = yRot;
            case LIZARD -> {
                part.apply("CENTERROT").yRot = yRot;
                part.apply("FRILL3ROT").zRot = -PI / 4;
                part.apply("FRILL4ROT").zRot = PI / 4;
            }
            case ELEPHANT -> {
                part.apply("CENTERROT").yRot = yRot;
                for (int i = 1; i <= 7; i++) {
                    part.apply("TRUNK" + i + "ROT").xRot = i <= 3 ? 0.0F : PI / 4;
                }
            }
            case MAMMOTH -> {
                part.apply("CENTERROT").yRot = yRot;
                for (int i = 1; i <= 9; i++) {
                    boolean bent = skullState == 1 ? i >= 4 && i <= 7 : i <= 4;
                    part.apply("NOSEROT" + i).xRot = bent ? PI / 8 : 0.0F;
                }
                part.apply("TUSKRIGROT").yRot = -PI / 8;
                part.apply("TUSKLEFROT").yRot = PI / 8;
            }
            case OSTRICH -> {
                part.apply("BODYROT").yRot = yRot;
                boolean standing = skullState == 1;
                part.apply("BODYROT").xRot = standing ? -PI / 2 : 0.0F;
                part.apply("NECK1ROT").xRot = standing ? -0.08726646F : -0.61086524F;
                part.apply("NECK3ROT").xRot = standing ? -0.08726646F : -PI / 6;
                part.apply("NECK4ROT").xRot = standing ? -PI / 18 : -0.43633232F;
                part.apply("NECK5ROT").xRot = standing ? 0.43633232F : -PI / 9;
                part.apply("NECK6ROT").xRot = standing ? PI / 6 : 0.43633232F;
                part.apply("NECK7ROT").xRot = standing ? PI * 2.0F / 9.0F : PI / 6;
                part.apply("NECK8ROT").xRot = standing ? PI / 6 : 0.61086524F;
                part.apply("HEADROT").xRot = standing ? PI / 9 : 0.87266463F;
            }
            case PENGUIN -> part.apply("BODYROT").yRot = yRot;
            case TREE_ENT -> {
                part.apply("BACKTREESTEM5").xRot = -PI / 18;
                part.apply("BACKTREESTEM5").zRot = PI / 9;
                part.apply("BACKTREESTEM6").xRot = -PI / 18;
                part.apply("BACKTREESTEM6").zRot = -PI / 9;
                part.apply("CENTERROT").yRot = yRot;
            }
            case VULTURE -> {
                part.apply("BODYROT").yRot = yRot;
                boolean standing = skullState == 1;
                part.apply("NECKROT1").xRot = standing ? 0.0F : PI / 2;
                part.apply("NECKROT2").xRot = standing ? PI / 6 : PI / 18;
                part.apply("NECKROT3").xRot = standing ? PI * 2.0F / 9.0F : PI / 18;
                part.apply("HEADROT").xRot = standing ? -PI / 3 : -PI * 4.0F / 9.0F;
            }
            default -> part.apply("CENTERROT").yRot = yRot;
        }
    }
}
