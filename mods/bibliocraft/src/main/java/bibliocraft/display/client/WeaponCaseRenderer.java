package bibliocraft.display.client;

import bibliocraft.BiblioCraft;
import bibliocraft.client.BiblioRender;
import bibliocraft.common.BCConfig;
import bibliocraft.display.WeaponCaseBlock;
import bibliocraft.display.WeaponCaseBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayerLocation;
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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityWeaponCaseRenderer: the case in the texture of its wood with the lining of its cloth, the lid open
 * (0.8 rad on the floor, 1.5 on the wall) or shut, the item lying in it (standing for a wall case), and with the
 * reading glasses its name and up to four enchantments (HighlightHandler: whenever the player looks at the case).
 * Only the "fancy graphics" item placement of the original is ported.
 */
public class WeaponCaseRenderer implements BlockEntityRenderer<WeaponCaseBlockEntity, WeaponCaseRenderer.State> {
    private final Parts parts;
    private final Font font;
    private final ItemModelResolver itemModels;

    public static class State extends BlockEntityRenderState {
        int angle;
        int wood;
        int cloth;
        boolean lidOpen;
        @Nullable ItemStackRenderState item;
        boolean itemIsBlock;
        @Nullable String text;
        List<String> enchantments = List.of();
    }

    /** Shell, inner and the lid baked once per lid angle (0, 0.2 item, 0.8 open on the floor, 1.5 open on the wall). */
    public record Parts(ModelPart[] shell, ModelPart[] inner, ModelPart lidShut, ModelPart lidItem, ModelPart lidOpen, ModelPart lidWallOpen) {
        static Parts bake(Function<ModelLayerLocation, ModelPart> baker) {
            ModelPart root = baker.apply(DisplayModels.WEAPON_CASE);
            ModelPart[] shell = {root.getChild("Bottom"), root.getChild("backboard"), root.getChild("frontboard"), root.getChild("leftboard"),
                    root.getChild("rightboard")};
            ModelPart[] inner = {root.getChild("innerLeft"), root.getChild("innerRight"), root.getChild("innerBack"), root.getChild("innerFront"),
                    root.getChild("innterBottom")};
            return new Parts(shell, inner, lid(baker, 0.0F), lid(baker, 0.2F), lid(baker, 0.8F), lid(baker, 1.5F));
        }

        private static ModelPart lid(Function<ModelLayerLocation, ModelPart> baker, float zRot) {
            ModelPart lid = baker.apply(DisplayModels.WEAPON_CASE).getChild("lid");
            lid.zRot = zRot;
            return lid;
        }

        /** renderShell + renderLid in the case texture, renderInner in the lining texture, at the current pose (1/16). */
        void submit(PoseStack poseStack, SubmitNodeCollector collector, int wood, int cloth, ModelPart lid, int light) {
            Identifier caseTex = caseTexture(wood);
            for (ModelPart p : this.shell) {
                collector.submitModelPart(p, poseStack, RenderTypes.entityCutout(caseTex), light, OverlayTexture.NO_OVERLAY, null);
            }
            collector.submitModelPart(lid, poseStack, RenderTypes.entityCutout(caseTex), light, OverlayTexture.NO_OVERLAY, null);
            Identifier innerTex = innerTexture(cloth);
            for (ModelPart p : this.inner) {
                collector.submitModelPart(p, poseStack, RenderTypes.entityCutout(innerTex), light, OverlayTexture.NO_OVERLAY, null);
            }
        }
    }

    /** getModelTexture: weaponcase1..4 by wood. */
    static Identifier caseTexture(int wood) {
        return BiblioCraft.id("textures/model/weaponcase" + (wood + 1) + ".png");
    }

    /** weaponcaseinner1..4 by cloth (metadata % 4). */
    static Identifier innerTexture(int cloth) {
        return BiblioCraft.id("textures/model/weaponcaseinner" + (cloth + 1) + ".png");
    }

    public WeaponCaseRenderer(BlockEntityRendererProvider.Context context) {
        this.parts = Parts.bake(context::bakeLayer);
        this.font = context.font();
        this.itemModels = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WeaponCaseBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.angle = WeaponCaseBlock.caseAngle(be.getBlockState());
        if (be.getBlockState().getBlock() instanceof WeaponCaseBlock b) {
            state.wood = b.wood;
            state.cloth = b.cloth;
        }
        state.lidOpen = be.isLidOpen();
        ItemStack stack = be.getCaseStack();
        state.item = BiblioRender.item(this.itemModels, stack, ItemDisplayContext.GROUND, be, 0);
        state.itemIsBlock = !stack.isEmpty() && BCConfig.isBlock(stack);
        state.text = null;
        state.enchantments = List.of();
        if (!stack.isEmpty() && BiblioRender.look(be.getBlockPos()) != null) {
            state.text = stack.getHoverName().getString();
            state.enchantments = DisplayText.enchantments(stack);
        }
    }

    /** caseAngle -> degreeAngle (270, 180, 90, 0 for 0..3 and again for 4..7). */
    static float degrees(int angle) {
        return switch (angle % 4) {
            case 0 -> 270.0F;
            case 1 -> 180.0F;
            case 2 -> 90.0F;
            default -> 0.0F;
        };
    }

    /** The model transform: on the floor up-side down from y 1.5; on the wall laid on its back against the wall. */
    static void caseTransform(PoseStack poseStack, int angle) {
        float deg = degrees(angle);
        switch (angle) {
            case 4 -> poseStack.translate(-0.5, 0.5, 0.5);
            case 5 -> poseStack.translate(0.5, 0.5, -0.5);
            case 6 -> poseStack.translate(1.5, 0.5, 0.5);
            case 7 -> poseStack.translate(0.5, 0.5, 1.5);
            default -> poseStack.translate(0.5, 1.5, 0.5);
        }
        if (angle > 3) {
            poseStack.rotateDegrees(Axis.YP, deg);
            poseStack.rotateDegrees(Axis.XP, -90.0F);
        } else {
            poseStack.rotateDegrees(Axis.YP, deg + 90.0F);
            poseStack.rotateDegrees(Axis.XP, 180.0F);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int angle = state.angle;
        poseStack.pushPose();
        caseTransform(poseStack, angle);
        ModelPart lid = !state.lidOpen ? this.parts.lidShut() : angle < 4 ? this.parts.lidOpen() : this.parts.lidWallOpen();
        this.parts.submit(poseStack, collector, state.wood, state.cloth, lid, state.lightCoords);
        if (state.text != null) {
            this.renderText(poseStack, collector, state.text, state.enchantments, angle > 3, state.lightCoords);
        }
        poseStack.popPose();
        if (state.item != null) {
            // ichange / kchange of the original (optifineAdjust 0)
            float[] ichange = {0.35F, 0.55F, 0.65F, 0.45F, 0.78F, 0.5F, 0.22F, 0.5F};
            float[] kchange = {0.45F, 0.35F, 0.55F, 0.65F, 0.5F, 0.78F, 0.5F, 0.22F};
            float deg = degrees(angle);
            poseStack.pushPose();
            poseStack.translate(ichange[angle], angle < 4 ? 0.25F : 0.35F, kchange[angle]);
            if (angle < 4) {
                poseStack.rotateDegrees(Axis.YP, deg + 165.0F);
                poseStack.rotateDegrees(Axis.XP, 90.0F);
            } else {
                poseStack.rotateDegrees(Axis.YP, deg + 180.0F);
            }
            if (state.itemIsBlock) {
                poseStack.translate(0.0F, 0.12F, 0.0F);
                poseStack.rotateDegrees(Axis.YP, 180.0F);
            }
            poseStack.scale(1.2F, 1.2F, 1.2F);
            DisplayText.entityItem(state.item, state.itemIsBlock, poseStack, collector, state.lightCoords);
            poseStack.popPose();
        }
    }

    /** renderText: name in Config.color, enchantments in Config.color2 below (lines 3 and 4 of a wall case jump to 92/102). */
    private void renderText(PoseStack poseStack, SubmitNodeCollector collector, String text, List<String> ench, boolean wallmount, int light) {
        poseStack.pushPose();
        poseStack.scale(0.008F, 0.008F, 0.008F);
        poseStack.rotateDegrees(Axis.YP, 90.0F);
        poseStack.translate(0.0, 145.0, -63.0);
        if (wallmount) {
            poseStack.rotateDegrees(Axis.XP, -90.0F);
            poseStack.rotateDegrees(Axis.ZP, -90.0F);
            poseStack.translate(63.0, -50.0, -24.0);
        }
        DisplayText.centered(poseStack, collector, this.font, text, -8, BCConfig.textColor(), light);
        int[] ys = wallmount ? new int[]{2, 12, 92, 102} : new int[]{2, 12, 22, 32};
        for (int i = 0; i < ench.size(); i++) {
            DisplayText.centered(poseStack, collector, this.font, ench.get(i), ys[i], DisplayText.ENCHANT_COLOR, light);
        }
        poseStack.popPose();
    }

    /** getRenderBoundingBox: one block around, the open lid reaches out of the block. */
    @Override
    public AABB getRenderBoundingBox(WeaponCaseBlockEntity be) {
        BlockPos p = be.getBlockPos();
        return new AABB(p.getX() - 1, p.getY(), p.getZ() - 1, p.getX() + 2, p.getY() + 1, p.getZ() + 2);
    }
}
