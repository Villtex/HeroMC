package twilightforest.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

import java.util.function.BiConsumer;

/**
 * RenderBiped of the lower goblin knight and RenderTFGoblinKnightUpper: doublegoblin.png; the upper knight's body
 * pitches with the heavy spear thrust (getPitchForAttack in rotateCorpse).
 */
public class GoblinKnightRenderer<T extends Mob> extends HumanoidMobRenderer<T, TFRenderStates.GoblinKnight, HumanoidModel<TFRenderStates.GoblinKnight>> {
    private static final Identifier TEXTURE = TFMobRenderer.texture("doublegoblin");
    private final BiConsumer<T, TFRenderStates.GoblinKnight> extractor;

    public GoblinKnightRenderer(EntityRendererProvider.Context context, HumanoidModel<TFRenderStates.GoblinKnight> model,
            BiConsumer<T, TFRenderStates.GoblinKnight> extractor) {
        super(context, model, 0.625F);
        this.extractor = extractor;
    }

    @Override
    public TFRenderStates.GoblinKnight createRenderState() {
        return new TFRenderStates.GoblinKnight();
    }

    @Override
    public void extractRenderState(T entity, TFRenderStates.GoblinKnight state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        this.extractor.accept(entity, state);
    }

    @Override
    protected void setupRotations(TFRenderStates.GoblinKnight state, PoseStack poseStack, float bodyRot, float scale) {
        super.setupRotations(state, poseStack, bodyRot, scale);
        if (state.heavySpearTimer > 0) {
            poseStack.rotateDegrees(Axis.XP, GoblinKnightUpperModel.pitchForAttack(60 - state.heavySpearTimer + state.partialTick));
        }
    }

    @Override
    public Identifier getTextureLocation(TFRenderStates.GoblinKnight state) {
        return TEXTURE;
    }
}
