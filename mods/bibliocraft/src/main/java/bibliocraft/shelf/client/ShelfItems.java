package bibliocraft.shelf.client;

import bibliocraft.client.BiblioRender;
import bibliocraft.common.BiblioBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * The items on the shelves: the renderers drew each stack as a dropped item (RenderItem.doRenderItem of an EntityItem
 * with hoverStart 0, no bobbing, one copy, so no spin). That is the GROUND display transform now; 1.5.2 put the
 * bottom of both a flat item (0.5 scale, 0.25 below the centre) and a block (0.25 scale, centred) 0.125 below the
 * origin, so the model is moved to that height. With fast graphics 1.5.2 turned flat items to the camera; here they
 * always lie as with fancy graphics.
 */
final class ShelfItems {
    private ShelfItems() {
    }

    /** The GROUND item models of all slots (null for empty ones), resolved in extractRenderState. */
    static @Nullable ItemStackRenderState[] extract(ItemModelResolver resolver, BiblioBlockEntity be) {
        int n = be.getContainerSize();
        ItemStackRenderState[] items = new ItemStackRenderState[n];
        int seed = HashCommon.long2int(be.getBlockPos().asLong());
        for (int i = 0; i < n; i++) {
            items[i] = BiblioRender.item(resolver, be.getItem(i), ItemDisplayContext.GROUND, be, seed + i);
        }
        return items;
    }

    /** doRenderItem at the current pose (after the renderer's translate, rotate and scale). */
    static void submit(@Nullable ItemStackRenderState item, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (item == null) {
            return;
        }
        AABB box = item.getModelBoundingBox();
        poseStack.pushPose();
        poseStack.translate(0.0, -0.125 - box.minY, 0.0);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
