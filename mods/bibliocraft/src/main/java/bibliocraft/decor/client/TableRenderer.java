package bibliocraft.decor.client;

import bibliocraft.BiblioCraft;
import bibliocraft.client.BiblioRender;
import bibliocraft.common.BCConfig;
import bibliocraft.decor.TableBlock;
import bibliocraft.decor.TableBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
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
 * TileEntityTableRenderer: the table model (ModelTable: OBJModelTable.obj, legs and corners by the neighbour tables)
 * in the texture of its wood, and the item on the table as a dropped item (EntityItem without bobbing or spinning) at
 * (0.5, 1.14, 0.5). A block (except rails) stands and turns about the vertical with slotxangle; anything else lies
 * flat and turns with slotxangle in its plane; slotyangle then tilts both.
 */
public class TableRenderer implements BlockEntityRenderer<TableBlockEntity, TableRenderer.State> {
    public static final Identifier MODEL = BiblioCraft.id("models/obj/objmodeltable.obj");
    private final ObjModel model;
    private final ItemModelResolver itemModelResolver;

    public static class State extends BlockEntityRenderState {
        int wood;
        boolean[] legs = {false, false, false, false, true};
        boolean[] tops = {true, true, true, true};
        @Nullable ItemStackRenderState item;
        boolean block;
        int slotX;
        int slotY;
    }

    public TableRenderer(BlockEntityRendererProvider.Context context) {
        this.model = ObjModel.load(MODEL);
        this.itemModelResolver = context.itemModelResolver();
    }

    /** getModelTexture: table0..3 by metadata. */
    public static Identifier texture(int wood) {
        return BiblioCraft.id("textures/model/table" + wood + ".png");
    }

    /** ModelTable.renderLeg + renderTop: the parts to draw. */
    static String[] parts(boolean[] legs, boolean[] tops) {
        List<String> parts = new ArrayList<>(9);
        for (int q = 0; q < 4; q++) {
            if (legs[q]) {
                parts.add("leg0" + (q + 1));
            }
        }
        if (legs[4]) {
            parts.add("monoleg");
        }
        for (int q = 0; q < 4; q++) {
            parts.add((tops[q] ? "tableBevel0" : "tableSquare0") + (q + 1));
        }
        return parts.toArray(String[]::new);
    }

    /** The table at the current pose, with (0, 0, 0) at the bottom middle of the block. */
    static void submitTable(ObjModel model, PoseStack poseStack, SubmitNodeCollector collector, int wood, boolean[] legs, boolean[] tops, int light) {
        model.submit(poseStack, collector, texture(wood), light, parts(legs, tops));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TableBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.wood = be.getBlockState().getBlock() instanceof TableBlock t ? t.wood : 0;
        state.legs = be.legs();
        state.tops = be.tops();
        state.slotX = be.slotX();
        state.slotY = be.slotY();
        ItemStack stack = be.getItem(0);
        state.block = BCConfig.isBlock(stack) && !BCConfig.codeName(stack).contains("rail");
        state.item = BiblioRender.item(this.itemModelResolver, stack, ItemDisplayContext.NONE, be, (int) be.getBlockPos().asLong());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        submitTable(this.model, poseStack, collector, state.wood, state.legs, state.tops, state.lightCoords);
        poseStack.popPose();
        if (state.item == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 1.14, 0.5);
        if (state.block) {
            poseStack.rotateDegrees(Axis.YP, turn(state.slotX));
        } else {
            poseStack.rotateDegrees(Axis.XP, 90.0F);
            poseStack.translate(0.0, 0.0, 0.12);
            poseStack.rotateDegrees(Axis.ZP, turn(state.slotX));
        }
        switch (state.slotY) {
            case 1 -> poseStack.rotateDegrees(Axis.XP, 90.0F);
            case 2 -> poseStack.rotateDegrees(Axis.XP, 180.0F);
            case 3 -> poseStack.rotateDegrees(Axis.XP, -90.0F);
            default -> {
            }
        }
        // RenderItem.doRenderItem: blocks drawn in 3D at a quarter of their size around the middle, flat items at half
        // size with their lower quarter below the middle (fancy graphics)
        if (state.item.usesBlockLight()) {
            poseStack.scale(0.25F, 0.25F, 0.25F);
        } else {
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(0.0, 0.25, 0.0);
        }
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    /** slotxangle 0..3 -> 0, 90, 180, -90 degrees. */
    private static float turn(int angle) {
        return switch (angle) {
            case 1 -> 90.0F;
            case 2 -> 180.0F;
            case 3 -> -90.0F;
            default -> 0.0F;
        };
    }

    /** getRenderBoundingBox: the block and the one above it (the item on the table). */
    @Override
    public AABB getRenderBoundingBox(TableBlockEntity be) {
        BlockPos p = be.getBlockPos();
        return new AABB(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 2, p.getZ() + 1);
    }
}
