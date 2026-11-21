package twilightforest.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import twilightforest.block.TrophyBlock;
import twilightforest.block.TrophyBlockEntity;
import twilightforest.client.entity.BossRenderers;
import twilightforest.client.entity.TFMobRenderer;

import java.util.function.Function;

/**
 * TileEntityTFTrophyRenderer: the boss heads. Standing they turn with the block's rotation, on a wall they face away
 * from it (north 0°, south 180°, west 270°, east 90°). Naga head a quarter of its size, lich head full size, the
 * ur-ghast half size with moving tentacles; the hydra head comes from the hydra's renderer ({@link #HYDRA_HEAD}).
 */
public class TrophyRenderer implements BlockEntityRenderer<TrophyBlockEntity, TrophyRenderer.State> {
    /** Draws the hydra head (ModelTFHydraHead with the mouth open for trophies): pose already at the block centre. */
    public interface HydraHead {
        void submit(PoseStack pose, SubmitNodeCollector collector, int light, float rotation, boolean onGround);
    }

    public static Function<BlockEntityRendererProvider.Context, @Nullable HydraHead> HYDRA_HEAD = c -> null;
    private static final Identifier NAGA = TFMobRenderer.texture("nagahead");
    private static final Identifier LICH = TFMobRenderer.texture("twilightlich64");
    private static final Identifier UR_GHAST = TFMobRenderer.texture("towerboss");

    private final ModelPart nagaHead;
    private final ModelPart lichHead;
    private final BossRenderers.UrGhastModel urGhast;
    private final @Nullable HydraHead hydra;

    public static class State extends BlockEntityRenderState {
        TrophyBlock.Kind kind = TrophyBlock.Kind.NAGA;
        float rotation;
        boolean onGround;
        float age;
        final LivingEntityRenderState ghast = new LivingEntityRenderState();
    }

    public TrophyRenderer(BlockEntityRendererProvider.Context context) {
        this.nagaHead = context.bakeLayer(BossRenderers.NAGA_HEAD);
        this.lichHead = context.bakeLayer(BossRenderers.LICH).getChild("head");
        this.urGhast = new BossRenderers.UrGhastModel(context.bakeLayer(BossRenderers.UR_GHAST));
        this.hydra = HYDRA_HEAD.apply(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TrophyBlockEntity be, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, camera, breakProgress);
        BlockState block = be.getBlockState();
        if (block.getBlock() instanceof TrophyBlock trophy) {
            state.kind = trophy.kind();
            state.onGround = true;
            // the 1.5.2 skull rotation is today's + 8 segments (today's counts from the opposite heading)
            state.rotation = block.getValue(TrophyBlock.ROTATION) * 22.5F + 180.0F;
        } else if (block.getBlock() instanceof TrophyBlock.Wall wall) {
            state.kind = wall.kind();
            state.onGround = false;
            state.rotation = switch (block.getValue(TrophyBlock.Wall.FACING)) {
                case SOUTH -> 180.0F;
                case WEST -> 270.0F;
                case EAST -> 90.0F;
                default -> 0.0F;
            };
        }
        state.age = be.ticksExisted + partialTicks;
        state.ghast.ageInTicks = state.age;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        switch (state.kind) {
            case HYDRA -> {
                if (this.hydra != null) {
                    this.hydra.submit(pose, collector, light, state.rotation, state.onGround);
                }
            }
            case NAGA -> {
                pose.translate(0.0F, -0.125F, 0.0F);
                pose.scale(0.25F, 0.25F, 0.25F);
                pose.scale(1.0F, -1.0F, -1.0F);
                pose.rotateDegrees(Axis.YP, state.rotation);
                pose.rotateDegrees(Axis.YP, 180.0F);
                pose.translate(0.0F, state.onGround ? 1.0F : 0.0F, state.onGround ? 0.0F : 1.0F);
                pose.scale(2.0F, 2.0F, 2.0F);
                collector.submitModelPart(this.nagaHead, pose, RenderTypes.entityCutout(NAGA), light, OverlayTexture.NO_OVERLAY, null);
            }
            case LICH -> {
                pose.translate(0.0F, 1.0F, 0.0F);
                pose.scale(1.0F, -1.0F, -1.0F);
                pose.rotateDegrees(Axis.YP, state.rotation);
                pose.rotateDegrees(Axis.YP, 180.0F);
                pose.translate(0.0F, state.onGround ? 1.75F : 1.5F, state.onGround ? 0.0F : 0.24F);
                collector.submitModelPart(this.lichHead, pose, RenderTypes.entityCutout(LICH), light, OverlayTexture.NO_OVERLAY, null);
            }
            case UR_GHAST -> {
                pose.translate(0.0F, 1.0F, 0.0F);
                pose.scale(0.5F, 0.5F, 0.5F);
                pose.scale(1.0F, -1.0F, -1.0F);
                pose.rotateDegrees(Axis.YP, state.rotation);
                pose.rotateDegrees(Axis.YP, 180.0F);
                pose.translate(0.0F, 1.0F, 0.0F);
                this.urGhast.setupAnim(state.ghast);
                collector.submitModel(this.urGhast, state.ghast, pose, RenderTypes.entityCutout(UR_GHAST), light, OverlayTexture.NO_OVERLAY, -1);
            }
        }
        pose.popPose();
    }
}
