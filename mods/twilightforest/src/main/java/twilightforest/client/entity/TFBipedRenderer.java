package twilightforest.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

import java.util.function.BiConsumer;

/** RenderBiped of 1.5.2: a biped model, the held item, the armour on vanilla armour models, one texture. */
public class TFBipedRenderer<T extends Mob> extends HumanoidMobRenderer<T, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
    private final Identifier texture;
    private final BiConsumer<T, HumanoidRenderState> extractor;
    private float scale = 1.0F;

    public TFBipedRenderer(EntityRendererProvider.Context context, HumanoidModel<HumanoidRenderState> model, float shadow, String texture,
            boolean armor, BiConsumer<T, HumanoidRenderState> extractor) {
        super(context, model, shadow);
        this.texture = TFMobRenderer.texture(texture);
        this.extractor = extractor;
        if (armor) {
            this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.SKELETON_ARMOR, context.getModelSet(), HumanoidModel::new),
                    context.getEquipmentRenderer()));
        }
    }

    public TFBipedRenderer(EntityRendererProvider.Context context, HumanoidModel<HumanoidRenderState> model, float shadow, String texture) {
        this(context, model, shadow, texture, true, (e, s) -> {
        });
    }

    public TFBipedRenderer<T> scaled(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }

    @Override
    public void extractRenderState(T entity, HumanoidRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        this.extractor.accept(entity, state);
    }

    @Override
    protected void scale(HumanoidRenderState state, PoseStack poseStack) {
        if (this.scale != 1.0F) {
            poseStack.scale(this.scale, this.scale, this.scale);
        }
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return this.texture;
    }
}
