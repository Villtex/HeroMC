package bibliocraft.shelf.client;

import bibliocraft.BiblioCraft;
import bibliocraft.client.BiblioRender;
import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlock;
import bibliocraft.shelf.PotionShelfBlock;
import bibliocraft.shelf.PotionShelfBlockEntity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityPotionShelfRenderer: the shelf (scale 1/32, turned upside down), the twelve potions at 0.7 size, and with
 * the reading glasses the name of the potion under the cross-hair (HighlightHandler.potshelf sets showPotText).
 */
public class PotionShelfRenderer implements BlockEntityRenderer<PotionShelfBlockEntity, PotionShelfRenderer.State> {
    /** ipotion1..4 / kpotion1..4 by angle: the x and z of the four places on a shelf. */
    private static final float[][] I = {{0.75F, 0.75F, 0.75F, 0.75F}, {0.86F, 0.63F, 0.4F, 0.17F}, {0.25F, 0.25F, 0.25F, 0.25F}, {0.17F, 0.4F, 0.63F, 0.86F}};
    private static final float[][] K = {{0.17F, 0.4F, 0.63F, 0.86F}, {0.75F, 0.75F, 0.75F, 0.75F}, {0.86F, 0.63F, 0.4F, 0.17F}, {0.25F, 0.25F, 0.25F, 0.25F}};
    /** The height of the top, middle and bottom shelf's potions. */
    private static final float[] Y = {0.78F, 0.45F, 0.1F};
    /** The y adjust of the text for the top, middle and bottom shelf. */
    private static final double[] TEXT_Y = {-66.0, 3.0, 72.0};
    private final ModelPart model;
    private final ItemModelResolver itemModelResolver;
    private final Font font;

    public static class State extends BlockEntityRenderState {
        int angle;
        int wood;
        @Nullable ItemStackRenderState[] items = new ItemStackRenderState[12];
        final boolean[] minechem = new boolean[12];
        @Nullable String text;
        int textShelf;
    }

    public PotionShelfRenderer(BlockEntityRendererProvider.Context context) {
        this.model = context.bakeLayer(ShelfModels.POTION_SHELF);
        this.itemModelResolver = context.itemModelResolver();
        this.font = context.font();
    }

    /** getModelTexture: potionshelf1..4 by wood. */
    public static Identifier texture(int wood) {
        return BiblioCraft.id("textures/model/potionshelf" + (wood + 1) + ".png");
    }

    /** angle 0..3 -> degreeAngle 270, 180, 90, 0. */
    static float degrees(int angle) {
        return switch (angle) {
            case 0 -> 270.0F;
            case 1 -> 180.0F;
            case 2 -> 90.0F;
            default -> 0.0F;
        };
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PotionShelfBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.angle = BiblioBlock.angle(be.getBlockState());
        state.wood = be.getBlockState().getBlock() instanceof PotionShelfBlock b ? b.wood : 0;
        state.items = ShelfItems.extract(this.itemModelResolver, be);
        for (int i = 0; i < 12; i++) {
            state.minechem[i] = mineChem(be.getItem(i));
        }
        state.text = null;
        BiblioRender.Look look = BiblioRender.look(be.getBlockPos());
        if (look != null) {
            // HighlightHandler.potshelf: potnumber = pot + shelf + 1 (a hit at hitY = 1 gives shelf -1)
            int potNumber = PotionShelfBlock.whatPot(state.angle, look.hitX(), look.hitZ()) + PotionShelfBlock.whatShelf(look.hitY()) + 1;
            if (potNumber > 0 && potNumber <= 12 && !be.getItem(potNumber - 1).isEmpty()) {
                state.text = be.getItem(potNumber - 1).getHoverName().getString();
                state.textShelf = (potNumber - 1) / 4;
            }
        }
    }

    /** mineChemTest: Minechem molecules and elements sit 0.11 higher. */
    static boolean mineChem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String name = BCConfig.codeName(stack);
        return name.contains("molecule") || name.contains("element");
    }

    /** The shelf at the block centre (0.5, 0.5 - 1/32, 0.5), turned by angle and upside down, at scale 1/32. */
    static void submitShelf(ModelPart model, PoseStack poseStack, SubmitNodeCollector collector, int wood, int angle, int light) {
        poseStack.translate(0.5, 0.5 - 0.03125, 0.5);
        poseStack.rotateDegrees(Axis.YP, degrees(angle));
        poseStack.rotateDegrees(Axis.XP, 180.0F);
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        collector.submitModelPart(model, poseStack, RenderTypes.entityCutout(texture(wood)), light, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        submitShelf(this.model, poseStack, collector, state.wood, state.angle, state.lightCoords);
        if (state.text != null) {
            // renderText: scale 0.005, translate (0, 15 + yadjust, 12)
            poseStack.pushPose();
            poseStack.scale(0.005F, 0.005F, 0.005F);
            poseStack.translate(0.0, 15.0 + TEXT_Y[state.textShelf], 12.0);
            BiblioRender.drawCentered(poseStack, collector, this.font, state.text, state.lightCoords);
            poseStack.popPose();
        }
        poseStack.popPose();
        float deg = degrees(state.angle);
        for (int slot = 0; slot < 12; slot++) {
            if (state.items[slot] == null) {
                continue;
            }
            int place = slot % 4;
            poseStack.pushPose();
            poseStack.translate(I[state.angle][place], Y[slot / 4] + (state.minechem[slot] ? 0.11F : 0.0F), K[state.angle][place]);
            poseStack.rotateDegrees(Axis.YP, deg);
            poseStack.scale(0.7F, 0.7F, 0.7F);
            ShelfItems.submit(state.items[slot], poseStack, collector, state.lightCoords);
            poseStack.popPose();
        }
    }
}
