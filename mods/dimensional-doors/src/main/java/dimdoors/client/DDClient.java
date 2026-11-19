package dimdoors.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dimdoors.DimDoors;
import dimdoors.block.DimDoorBlock;
import dimdoors.block.DimDoorBlockEntity;
import dimdoors.entity.Monolith;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jspecify.annotations.Nullable;

/**
 * The client of Dimensional Doors: RenderDimDoor (the starry portal in a linked door, drawn here with the end
 * portal's shader instead of the original's sixteen texture-generated layers) and RenderMobObelisk.
 */
public final class DDClient {
    static final ModelLayerLocation MONOLITH = new ModelLayerLocation(DimDoors.id("monolith"), "main");

    private DDClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, event ->
                event.registerLayerDefinition(MONOLITH, MonolithModel::createBodyLayer));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerEntityRenderer(DimDoors.MONOLITH.get(), MonolithRenderer::new);
            event.registerBlockEntityRenderer(DimDoors.DOOR_ENTITY.get(), DoorRenderer::new);
        });
    }

    // ---- monolith ----

    public static class MonolithState extends LivingEntityRenderState {
        int texture;
        float scale = 1.0F;
    }

    /** ModelMobObelisk: one box 48 x 108 x 12 on a 256 x 256 texture, scaled by the monolith's size. */
    static class MonolithModel extends EntityModel<MonolithState> {
        private final ModelPart monolith;

        MonolithModel(ModelPart root) {
            super(root);
            this.monolith = root.getChild("wholemonolith");
        }

        static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            mesh.getRoot().addOrReplaceChild("wholemonolith", CubeListBuilder.create().texOffs(0, 0).mirror()
                    .addBox(-24.0F, -83.07693F, -6.0F, 48, 108, 12), PartPose.ZERO);
            return LayerDefinition.create(mesh, 256, 256);
        }

        @Override
        public void setupAnim(MonolithState state) {
            super.setupAnim(state);
            this.monolith.xScale = state.scale;
            this.monolith.yScale = state.scale;
            this.monolith.zScale = state.scale;
        }
    }

    static class MonolithRenderer extends MobRenderer<Monolith, MonolithState, MonolithModel> {
        private static final Identifier[] TEXTURES = new Identifier[19];

        static {
            for (int i = 0; i < TEXTURES.length; i++) {
                TEXTURES[i] = DimDoors.id("textures/entity/monolith/monolith" + i + ".png");
            }
        }

        MonolithRenderer(EntityRendererProvider.Context context) {
            super(context, new MonolithModel(context.bakeLayer(MONOLITH)), 0.5F);
        }

        @Override
        public MonolithState createRenderState() {
            return new MonolithState();
        }

        @Override
        public void extractRenderState(Monolith entity, MonolithState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.texture = Math.max(0, Math.min(TEXTURES.length - 1, entity.textureState()));
            state.scale = entity.scaleFactor;
        }

        @Override
        public Identifier getTextureLocation(MonolithState state) {
            return TEXTURES[state.texture];
        }
    }

    // ---- door portal ----

    public static class DoorState extends BlockEntityRenderState {
        boolean linked;
        int orientation;
    }

    static class DoorRenderer implements BlockEntityRenderer<DimDoorBlockEntity, DoorState> {
        DoorRenderer(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public DoorState createRenderState() {
            return new DoorState();
        }

        @Override
        public void extractRenderState(DimDoorBlockEntity entity, DoorState state, float partialTicks, Vec3 cameraPosition,
                                       ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
            BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
            state.linked = false;
            BlockState self = entity.getBlockState();
            if (!entity.isLinked() || !(self.getBlock() instanceof DimDoorBlock) || self.getValue(DimDoorBlock.HALF) != DoubleBlockHalf.UPPER
                    || entity.getLevel() == null) {
                return;
            }
            BlockState lower = entity.getLevel().getBlockState(entity.getBlockPos().below());
            if (!lower.is(self.getBlock())) {
                return;
            }
            state.linked = true;
            state.orientation = DimDoorBlock.lowerMeta(lower) & 7;
        }

        @Override
        public void submit(DoorState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
            if (!state.linked) {
                return;
            }
            float[] q = switch (state.orientation) {
                case 0 -> new float[] {0.01F, 0, 0.01F, 1};
                case 1 -> new float[] {0, 0.01F, 1, 0.01F};
                case 2 -> new float[] {0.99F, 0, 0.99F, 1};
                case 3 -> new float[] {0, 0.99F, 1, 0.99F};
                case 4 -> new float[] {0.15F, 0, 0.15F, 1};
                case 5 -> new float[] {0, 0.15F, 1, 0.15F};
                case 6 -> new float[] {0.85F, 0, 0.85F, 1};
                default -> new float[] {0, 0.85F, 1, 0.85F};
            };
            collector.submitCustomGeometry(poseStack, RenderTypes.endPortal(), (pose, buffer) -> {
                quad(buffer, pose, q, false);
                quad(buffer, pose, q, true);
            });
        }

        /** The quad from the bottom of the lower half to the top of the upper one, both sides. */
        private static void quad(VertexConsumer buffer, PoseStack.Pose pose, float[] q, boolean back) {
            float[][] corners = {{q[0], -1, q[1]}, {q[2], -1, q[3]}, {q[2], 1, q[3]}, {q[0], 1, q[1]}};
            for (int i = 0; i < 4; i++) {
                float[] c = corners[back ? 3 - i : i];
                buffer.addVertex(pose, c[0], c[1], c[2]);
            }
        }

        @Override
        public AABB getRenderBoundingBox(DimDoorBlockEntity entity) {
            return new AABB(entity.getBlockPos()).expandTowards(0, -1, 0);
        }
    }
}
