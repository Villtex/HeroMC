package projectzulu.client.entity;

import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import projectzulu.ProjectZulu;
import projectzulu.entity.ZuluHorseEntity;

/**
 * RenderLiving(ModelHorse, shadow 1.0) with the texture of EntityHorse&lt;Colour&gt;.getTexture: one renderer per
 * colour, the saddled variant of the texture while the horse wears a saddle.
 */
public class ZuluHorseRenderer extends AgeableMobRenderer<ZuluHorseEntity, ZuluHorseModel.State, ZuluHorseModel> {
    private final Identifier texture;
    private final Identifier saddledTexture;

    public ZuluHorseRenderer(EntityRendererProvider.Context context, String colour) {
        super(context, new ZuluHorseModel(context.bakeLayer(HorseRenderers.HORSE_LAYER)),
                new ZuluHorseModel(context.bakeLayer(HorseRenderers.HORSE_BABY_LAYER)), 1.0F);
        this.texture = Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "textures/entity/horse_" + colour + ".png");
        this.saddledTexture = Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "textures/entity/horse_" + colour + "_saddled.png");
    }

    @Override
    public ZuluHorseModel.State createRenderState() {
        return new ZuluHorseModel.State();
    }

    @Override
    public void extractRenderState(ZuluHorseEntity entity, ZuluHorseModel.State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.saddled = entity.hasSaddle();
    }

    @Override
    public Identifier getTextureLocation(ZuluHorseModel.State state) {
        return state.saddled ? this.saddledTexture : this.texture;
    }
}
