package harkenscythe.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import harkenscythe.HarkenScythe;
import harkenscythe.altar.AltarBlock;
import harkenscythe.altar.AltarBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** RenderHSSoulAltar / RenderHSBloodAltar: the book of the enchanting table above the altar, as the Book of Shadows or of Carnage. */
public class AltarRenderer implements BlockEntityRenderer<AltarBlockEntity, AltarRenderer.State> {
    private static final Identifier SHADOWS = HarkenScythe.id("textures/entity/altar/book_of_shadows.png");
    private static final Identifier CARNAGE = HarkenScythe.id("textures/entity/altar/book_of_carnage.png");
    private final BookModel bookModel;

    public static class State extends BlockEntityRenderState {
        float time;
        float yRot;
        float flip;
        float open;
        boolean blood;
    }

    public AltarRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AltarBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.flip = Mth.lerp(partialTicks, entity.oFlip, entity.flip);
        state.open = Mth.lerp(partialTicks, entity.oOpen, entity.open);
        state.time = entity.time + partialTicks;
        float diff = entity.rot - entity.oRot;
        while (diff >= (float) Math.PI) {
            diff -= (float) (Math.PI * 2);
        }
        while (diff < (float) -Math.PI) {
            diff += (float) (Math.PI * 2);
        }
        state.yRot = entity.oRot + diff * partialTicks;
        state.blood = entity.getBlockState().getBlock() instanceof AltarBlock altar && altar.isBlood();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        poseStack.translate(0.0F, 0.1F + Mth.sin(state.time * 0.1F) * 0.01F, 0.0F);
        poseStack.rotate(Axis.YP, -state.yRot);
        poseStack.rotateDegrees(Axis.ZP, 80.0F);
        float ff1 = Mth.frac(state.flip + 0.25F) * 1.6F - 0.3F;
        float ff2 = Mth.frac(state.flip + 0.75F) * 1.6F - 0.3F;
        BookModel.State book = BookModel.State.forAnimation(state.time, Mth.clamp(ff1, 0.0F, 1.0F), Mth.clamp(ff2, 0.0F, 1.0F), state.open);
        collector.submitModel(this.bookModel, book, poseStack, state.blood ? CARNAGE : SHADOWS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(AltarBlockEntity entity) {
        return new AABB(entity.getBlockPos().getX(), entity.getBlockPos().getY(), entity.getBlockPos().getZ(), entity.getBlockPos().getX() + 1.0,
                entity.getBlockPos().getY() + 1.5, entity.getBlockPos().getZ() + 1.0);
    }
}
