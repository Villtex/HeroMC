package chococraft.client;

import chococraft.ChocoConfig;
import chococraft.ChocoCraft;
import chococraft.entity.Chocobo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

/**
 * RenderChocobo: ModelChocobo at 0.8, shadow 1.0. The texture follows the gear (pack bags, saddle bags, saddle, tamed,
 * untamed), the gender and the colour. The name of a tamed chocobo (not hidden, no rider) within 20 blocks, raised by
 * renderNameHeight. The original pushed the ridden chocobo half a block aside to make room for its rider, who sat
 * 0.4 off the middle; both sit centred here.
 */
public class ChocoboRenderer extends MobRenderer<Chocobo, ChocoboModel.State, ChocoboModel> {
    private final Map<String, Identifier> textures = new HashMap<>();

    public ChocoboRenderer(EntityRendererProvider.Context context) {
        super(context, new ChocoboModel(context.bakeLayer(ChocoCraftClient.CHOCOBO_LAYER)), 1.0F);
    }

    @Override
    public ChocoboModel.State createRenderState() {
        return new ChocoboModel.State();
    }

    @Override
    public void extractRenderState(Chocobo chocobo, ChocoboModel.State state, float partialTick) {
        super.extractRenderState(chocobo, state, partialTick);
        float rot = Mth.lerp(partialTick, chocobo.oWingRotation, chocobo.wingRotation);
        float dest = Mth.lerp(partialTick, chocobo.oDestPos, chocobo.destPos);
        state.wing = (Mth.sin(rot) + 1.0F) * dest;
        state.flutter = ChocoConfig.CHOCOBO_WING_FLUTTER.get();
        String gear = chocobo.isPackBagged() ? "pack_bagged" : chocobo.isSaddleBagged() ? "saddle_bagged"
                : chocobo.isSaddled() ? "saddled" : chocobo.isTame() ? "tamed" : "untamed";
        state.texture = gear + "/" + (chocobo.isMale() ? "male" : "female") + "/" + chocobo.getColor().texture;
        if (state.nameTagAttachment != null) {
            state.nameTagAttachment = state.nameTagAttachment.add(0.0, ChocoConfig.RENDER_NAME_HEIGHT.get(), 0.0);
        }
    }

    @Override
    public Identifier getTextureLocation(ChocoboModel.State state) {
        return this.textures.computeIfAbsent(state.texture, t -> ChocoCraft.id("textures/entity/chocobo/" + t + ".png"));
    }

    @Override
    protected void scale(ChocoboModel.State state, PoseStack poseStack) {
        poseStack.scale(0.8F, 0.8F, 0.8F);
    }

    @Override
    protected boolean shouldShowName(Chocobo chocobo, double distanceSq) {
        return chocobo.showsName() && distanceSq <= 20.0 * 20.0;
    }

    @Override
    protected Component getNameTag(Chocobo chocobo) {
        return Component.literal(chocobo.getChocoboName());
    }
}
