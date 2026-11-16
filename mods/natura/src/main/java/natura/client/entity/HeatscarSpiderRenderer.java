package natura.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import natura.Natura;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.spider.Spider;

/** RenderSpider with flamespider.png, scaled by spiderScaleAmount (2.0 for the heatscar, 0.75 for the baby). */
public class HeatscarSpiderRenderer<T extends Spider> extends SpiderRenderer<T> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Natura.MOD_ID, "textures/entity/heatscar_spider.png");
    private final float scale;

    public HeatscarSpiderRenderer(EntityRendererProvider.Context context, float scale) {
        super(context);
        this.scale = scale;
        this.shadowRadius *= scale;
    }

    @Override
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
