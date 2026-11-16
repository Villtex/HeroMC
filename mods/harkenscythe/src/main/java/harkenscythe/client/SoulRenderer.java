package harkenscythe.client;

import com.mojang.blaze3d.vertex.PoseStack;
import harkenscythe.HarkenScythe;
import harkenscythe.entity.LostSoulEntity;
import harkenscythe.entity.SoulEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/** RenderHSSoul / RenderHSSoulLost: the soul crystal with the texture of its kind (shadow 0.3 like the original). */
public class SoulRenderer<T extends Mob> extends EntityRenderer<T, SoulRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(HarkenScythe.id("soul"), "main");
    private final SoulModel model;
    private final Identifier texture;

    public SoulRenderer(EntityRendererProvider.Context context, String kind) {
        super(context);
        this.shadowRadius = 0.3F;
        this.model = new SoulModel(context.bakeLayer(LAYER));
        this.texture = HarkenScythe.id("textures/entity/soul/" + kind + ".png");
    }

    @Override
    public SoulRenderState createRenderState() {
        return new SoulRenderState();
    }

    @Override
    public void extractRenderState(T entity, SoulRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        int rotation = entity instanceof SoulEntity soul ? soul.innerRotation : entity instanceof LostSoulEntity lost ? lost.innerRotation : 0;
        state.time = rotation + entity.tickCount + partialTicks;
        state.texture = this.texture;
    }

    @Override
    public void submit(SoulRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        collector.submitModel(this.model, state, poseStack, state.texture, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        super.submit(state, poseStack, collector, camera);
    }
}
