package bibliocraft.decor.client;

import bibliocraft.BiblioCraft;
import bibliocraft.common.BiblioBlock;
import bibliocraft.decor.LightBlock;
import bibliocraft.decor.LightBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityLanternRenderer and TileEntityLampRenderer: the model turned by the angle about the block corner it starts
 * from, the body in lantern.png / lamp.png, then the candle (lanternCandle&lt;colour&gt;) or the lamp hood
 * (lampLight&lt;colour&gt;). The OBJ models lie in x 0..1, z -1..0.
 */
public class LightRenderer implements BlockEntityRenderer<LightBlockEntity, LightRenderer.State> {
    public static final Identifier LANTERN_MODEL = BiblioCraft.id("models/obj/objmodellantern.obj");
    public static final Identifier LAMP_MODEL = BiblioCraft.id("models/obj/objmodellamp.obj");
    private final ObjModel model;
    private final boolean lantern;

    public static class State extends BlockEntityRenderState {
        int angle;
        int style;
        int color;
    }

    public LightRenderer(boolean lantern) {
        this.lantern = lantern;
        this.model = ObjModel.load(lantern ? LANTERN_MODEL : LAMP_MODEL);
    }

    public static BlockEntityRendererProvider<LightBlockEntity, State> provider(boolean lantern) {
        return context -> new LightRenderer(lantern);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LightBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.angle = BiblioBlock.angle(be.getBlockState());
        state.style = LightBlock.style(be.getBlockState());
        state.color = be.getBlockState().getBlock() instanceof LightBlock b ? b.color : 0;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        double adjusti = 0.0;
        double adjustk = 0.0;
        float degrees = switch (state.angle) {
            case 0 -> {
                adjusti = 1.0;
                yield 180.0F;
            }
            case 1 -> {
                adjusti = 1.0;
                adjustk = 1.0;
                yield 90.0F;
            }
            case 2 -> {
                adjustk = 1.0;
                yield 0.0F;
            }
            default -> -90.0F;
        };
        poseStack.translate(adjusti, 0.0, adjustk);
        poseStack.rotateDegrees(Axis.YP, degrees);
        this.submitModel(poseStack, collector, state.style, state.color, state.lightCoords);
        poseStack.popPose();
    }

    /** The model in the given style at the current pose (ModelLantern.renderLantern + renderCandle, ModelLamp.renderBase + renderLamp). */
    void submitModel(PoseStack poseStack, SubmitNodeCollector collector, int style, int color, int light) {
        if (this.lantern) {
            Identifier body = BiblioCraft.id("textures/model/lantern.png");
            switch (style) {
                case 1 -> this.model.submit(poseStack, collector, body, light, "lamp", "lampHanger", "wallPlate");
                case 2 -> this.model.submit(poseStack, collector, body, light, "lamp", "lampHanger", "topPlate");
                default -> this.model.submit(poseStack, collector, body, light, "lamp");
            }
            this.model.submit(poseStack, collector, BiblioCraft.id("textures/model/lantern_candle" + color + ".png"), light, "candle");
        } else {
            Identifier body = BiblioCraft.id("textures/model/lamp.png");
            Identifier hood = BiblioCraft.id("textures/model/lamp_light" + color + ".png");
            switch (style) {
                case 1 -> {
                    this.model.submit(poseStack, collector, body, light, "wallPlate");
                    this.model.submit(poseStack, collector, hood, light, "lampTopWall");
                }
                case 2 -> {
                    this.model.submit(poseStack, collector, body, light, "ceilingPlate");
                    this.model.submit(poseStack, collector, hood, light, "lampTopCeiling");
                }
                default -> {
                    this.model.submit(poseStack, collector, body, light, "baseFloor");
                    this.model.submit(poseStack, collector, hood, light, "lampTopFloor");
                }
            }
        }
    }
}
