package chococraft.client;

import chococraft.ChocoCraft;
import chococraft.entity.Chicobo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/** RenderChicobo: ModelChicobo at 1.32, shadow 0.5; tamed chicobos have their own textures; the name within 20 blocks. */
public class ChicoboRenderer extends MobRenderer<Chicobo, ChicoboModel.State, ChicoboModel> {
    private final Map<String, Identifier> textures = new HashMap<>();

    public ChicoboRenderer(EntityRendererProvider.Context context) {
        super(context, new ChicoboModel(context.bakeLayer(ChocoCraftClient.CHICOBO_LAYER)), 0.5F);
    }

    @Override
    public ChicoboModel.State createRenderState() {
        return new ChicoboModel.State();
    }

    @Override
    public void extractRenderState(Chicobo chicobo, ChicoboModel.State state, float partialTick) {
        super.extractRenderState(chicobo, state, partialTick);
        state.texture = (chicobo.isTame() ? "tamed/" : "") + chicobo.getColor().texture;
    }

    @Override
    public Identifier getTextureLocation(ChicoboModel.State state) {
        return this.textures.computeIfAbsent(state.texture, t -> ChocoCraft.id("textures/entity/chicobo/" + t + ".png"));
    }

    @Override
    protected void scale(ChicoboModel.State state, PoseStack poseStack) {
        poseStack.scale(1.32F, 1.32F, 1.32F);
    }

    @Override
    protected boolean shouldShowName(Chicobo chicobo, double distanceSq) {
        return chicobo.showsName() && distanceSq <= 20.0 * 20.0;
    }

    @Override
    protected Component getNameTag(Chicobo chicobo) {
        return Component.literal(chicobo.getChocoboName());
    }
}
