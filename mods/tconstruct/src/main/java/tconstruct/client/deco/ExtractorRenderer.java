package tconstruct.client.deco;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import tconstruct.machine.EssenceExtractorBlockEntity;

/** The book on the essence extractor: the enchanting table renderer with the extractor's own book entity. Client check open. */
public class ExtractorRenderer implements BlockEntityRenderer<EssenceExtractorBlockEntity, EnchantTableRenderState> {
    private final EnchantTableRenderer book;

    public ExtractorRenderer(BlockEntityRendererProvider.Context context) {
        this.book = new EnchantTableRenderer(context);
    }

    @Override
    public EnchantTableRenderState createRenderState() {
        return this.book.createRenderState();
    }

    @Override
    public void extractRenderState(EssenceExtractorBlockEntity extractor, EnchantTableRenderState state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        this.book.extractRenderState(extractor.book(), state, partialTick, camera, crumbling);
    }

    @Override
    public void submit(EnchantTableRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        this.book.submit(state, pose, collector, camera);
    }
}
