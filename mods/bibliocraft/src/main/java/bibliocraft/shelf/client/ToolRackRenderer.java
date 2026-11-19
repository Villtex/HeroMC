package bibliocraft.shelf.client;

import bibliocraft.BiblioCraft;
import bibliocraft.client.BiblioRender;
import bibliocraft.common.BiblioBlock;
import bibliocraft.shelf.ToolRackBlock;
import bibliocraft.shelf.ToolRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityWeaponRackRenderer: the rack, the four tools at full size turned by the angle + 180, and with the reading
 * glasses the name of the tool under the cross-hair (HighlightHandler.weaponRack sets showWText).
 */
public class ToolRackRenderer implements BlockEntityRenderer<ToolRackBlockEntity, ToolRackRenderer.State> {
    /** ichange1..4 and kchange1..4 by angle. */
    private static final float[][] I = {{0.6F, 0.6F, 0.58F, 0.58F}, {0.7F, 0.3F, 0.7F, 0.3F}, {0.4F, 0.4F, 0.42F, 0.42F}, {0.3F, 0.7F, 0.3F, 0.7F}};
    private static final float[][] K = {{0.3F, 0.7F, 0.3F, 0.7F}, {0.6F, 0.6F, 0.58F, 0.58F}, {0.7F, 0.3F, 0.7F, 0.3F}, {0.4F, 0.4F, 0.42F, 0.42F}};
    private final ModelPart model;
    private final ItemModelResolver itemModelResolver;
    private final Font font;

    public static class State extends BlockEntityRenderState {
        int angle;
        int wood;
        @Nullable ItemStackRenderState[] items = new ItemStackRenderState[4];
        @Nullable String text;
    }

    public ToolRackRenderer(BlockEntityRendererProvider.Context context) {
        this.model = context.bakeLayer(ShelfModels.TOOL_RACK);
        this.itemModelResolver = context.itemModelResolver();
        this.font = context.font();
    }

    /** getModelTexture: toolrack1..4 by wood. */
    public static Identifier texture(int wood) {
        return BiblioCraft.id("textures/model/toolrack" + (wood + 1) + ".png");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ToolRackBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.angle = BiblioBlock.angle(be.getBlockState());
        state.wood = be.getBlockState().getBlock() instanceof ToolRackBlock b ? b.wood : 0;
        state.items = ShelfItems.extract(this.itemModelResolver, be);
        state.text = null;
        BiblioRender.Look look = BiblioRender.look(be.getBlockPos());
        if (look != null) {
            int side = whatWeapon(state.angle, look.hitX(), look.hitZ()) + 1;
            int number = look.hitY() > 0.5 ? side : look.hitY() < 0.5 ? side + 2 : 0;
            if (number > 0 && number < 5 && !be.getItem(number - 1).isEmpty()) {
                state.text = be.getItem(number - 1).getHoverName().getString();
            }
        }
    }

    /** HighlightHandler.isWhatWeapon, with the fall-through of the original switch (angle 1 into 2 into 3). */
    static int whatWeapon(int angle, double hitX, double hitZ) {
        int xt = (int) (hitX * 2.0);
        int zt = (int) (hitZ * 2.0);
        switch (angle) {
            case 0:
                return zt;
            case 1:
                if (xt == 0 || xt == 1) {
                    return 1 - xt;
                }
            case 2:
                if (zt == 0 || zt == 1) {
                    return 1 - zt;
                }
            case 3:
                return xt;
            default:
                return -1;
        }
    }

    /** The rack frame: (0.5, 1.5, 0.5), turned by degreeAngle + 180, upside down, and 90 about y; scale 1/16. */
    static void submitRack(ModelPart model, PoseStack poseStack, SubmitNodeCollector collector, int wood, int angle, int light) {
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.rotateDegrees(Axis.YP, PotionShelfRenderer.degrees(angle) + 180.0F);
        poseStack.rotateDegrees(Axis.XP, 180.0F);
        poseStack.rotateDegrees(Axis.YP, 90.0F);
        collector.submitModelPart(model, poseStack, RenderTypes.entityCutout(texture(wood)), light, OverlayTexture.NO_OVERLAY, null);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        submitRack(this.model, poseStack, collector, state.wood, state.angle, state.lightCoords);
        if (state.text != null) {
            // renderText: scale 0.005, turn 90, translate (0, 100, -1)
            poseStack.scale(0.005F, 0.005F, 0.005F);
            poseStack.rotateDegrees(Axis.YP, 90.0F);
            poseStack.translate(0.0, 100.0, -1.0);
            BiblioRender.drawCentered(poseStack, collector, this.font, state.text, state.lightCoords);
        }
        poseStack.popPose();
        float deg = PotionShelfRenderer.degrees(state.angle) + 180.0F;
        for (int slot = 0; slot < 4; slot++) {
            if (state.items[slot] == null) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(I[state.angle][slot], slot < 2 ? 0.56F : 0.19F, K[state.angle][slot]);
            poseStack.rotateDegrees(Axis.YP, deg);
            ShelfItems.submit(state.items[slot], poseStack, collector, state.lightCoords);
            poseStack.popPose();
        }
    }
}
